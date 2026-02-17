package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Service.BookService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController implements Initializable {

    // ================= TABLE =================
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookId;
    @FXML private TableColumn<Book, String> title;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;
    @FXML private TableColumn<Book, String> publisher;
    @FXML private TableColumn<Book, String> edition;
    @FXML private TableColumn<Book, String> quantity;
    @FXML private TableColumn<Book, String> remainingBooks;
    @FXML private TableColumn<Book, String> sectionCol;

    // ================= FORM =================
    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> statusComboBox;

    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;

    // ================= INITIALIZE =================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService = new BookService();

        // Status ComboBox
        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Not Available"));

        // TableColumn CellValueFactories
        bookId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        title.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        author.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        status.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAvailability()));
        publisher.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPublisher()));
        edition.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getEdition())));

        // Int fields converted to String
        quantity.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getRemaining())));

        sectionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSection()));

        // Load books and setup
        loadBooks();
        setupSearch();
        setupTableClick();
    }

    // ================= LOAD =================
    private void loadBooks() {
        booksList.setAll(bookService.getAllBooks());
        booksTable.setItems(booksList);
    }

    // ================= SEARCH =================
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                booksTable.setItems(booksList);
                return;
            }

            ObservableList<Book> filtered = FXCollections.observableArrayList();
            String q = newVal.toLowerCase();

            for (Book book : booksList) {
                if (book.getTitle().toLowerCase().contains(q) ||
                        book.getAuthor().toLowerCase().contains(q) ||
                        book.getIsbn().contains(newVal)) {
                    filtered.add(book);
                }
            }
            booksTable.setItems(filtered);
        });
    }

    // ================= TABLE SELECTION =================
    private void setupTableClick() {
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldBook, book) -> {
            if (book != null) {
                bookIdField.setText(book.getIsbn());
                bookTitleField.setText(book.getTitle());
                authorField.setText(book.getAuthor());
                statusComboBox.setValue(book.getAvailability());
            }
        });
    }

    // ================= SAVE =================
    @FXML
    private void handleSave() {
        String isbn = bookIdField.getText().trim();
        String titleVal = bookTitleField.getText().trim();
        String authorVal = authorField.getText().trim();
        String statusVal = statusComboBox.getValue();

        if (isbn.isEmpty() || titleVal.isEmpty() || authorVal.isEmpty() || statusVal == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required fields.");
            return;
        }

        Book selected = booksTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // UPDATE
            selected.setIsbn(isbn);
            selected.setTitle(titleVal);
            selected.setAuthor(authorVal);
            selected.setAvailability(statusVal);
            bookService.updateBook(selected);
        } else {
            // ADD
            Book book = new Book(isbn, titleVal, authorVal, "Unknown", 1, 1, 1, "General", statusVal);
            bookService.addBook(book);
        }

        clearForm();
        loadBooks();
    }

    // ================= DELETE =================
    @FXML
    private void handleDelete() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a book to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Book");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this book?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            bookService.deleteBook(selected.getIsbn());
            loadBooks();
            clearForm();
        }
    }

    // ================= CANCEL =================
    @FXML
    private void handleCancel() {
        clearForm();
        booksTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        bookIdField.clear();
        bookTitleField.clear();
        authorField.clear();
        statusComboBox.setValue(null);
    }

    // ================= NAVIGATION =================
    @FXML
    private void loadHomePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", node,true);
    }

    @FXML
    private void loadBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Books.fxml", node,true);
    }

    @FXML
    private void loadStudentPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Students.fxml", node,true);
    }

    @FXML
    private void loadIssueBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/IssueBooks.fxml", node,true);
    }

    @FXML
    private void loadReturnBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/ReturnBooks.fxml", node,true);
    }

    @FXML
    private void viewAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", node,true);
    }

    @FXML
    private void loadSendAnnouncementsPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Announcements.fxml", node,true);
    }

    @FXML
    private void loadExportDataPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Export.fxml", node,true);
    }

    @FXML
    private void loadClearancePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Clearance.fxml", node,true);
    }

    @FXML
    private void logout(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node,true);
    }

    // ================= ALERT =================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
