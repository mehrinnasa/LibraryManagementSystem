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

public class StudentBooksController implements Initializable {

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookId;
    @FXML private TableColumn<Book, String> title;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;
    @FXML private TableColumn<Book, String> publisher;
    @FXML private TableColumn<Book, String> edition;
    @FXML private TableColumn<Book, String> quantity;
    @FXML private TableColumn<Book, String> remainingBooks;
    @FXML private TableColumn<Book, String> pdfCol;

    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> statusComboBox;

    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService = new BookService();

        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Not Available"));

        // TableColumn CellValueFactories
        bookId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getIsbn()));
        title.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        author.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        status.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAvailability()));
        publisher.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPublisher()));
        edition.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getEdition())));
        quantity.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getRemaining())));
        pdfCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPdf()));
        loadBooks();
        setupSearch();
        setupTableClick();
    }

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
        new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", node, true);
    }

    @FXML
    private void loadBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentBooks.fxml", node, true);
    }

    @FXML
    private void loadAddToCartBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml", node, true);
    }

    @FXML
    private void loadAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", node, true);
    }

    @FXML
    private void loadAnnouncementPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml", node, true);
    }

    @FXML
    private void loadClearancePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentClearance.fxml", node, true);
    }

    @FXML
    private void logout(ActionEvent event) {
        //staticStudentId = null; // Clear student ID on logout
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node, true);
    }
}