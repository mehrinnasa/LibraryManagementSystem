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
import mehrin.loginpage.Util.FileUtil;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController implements Initializable {
    @FXML private TableView<Book>booksTable;
    @FXML private TableColumn<Book,String>bookId;
    @FXML private TableColumn<Book,String>title;
    @FXML private TableColumn<Book,String>author;
    @FXML private TableColumn<Book,String>status;
    @FXML private TableColumn<Book,String>publisher;
    @FXML private TableColumn<Book,String>edition;
    @FXML private TableColumn<Book,String>quantity;
    @FXML private TableColumn<Book,String>remainingBooks;
    @FXML private TableColumn<Book,Void>pdfCol;
    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private TextField editionField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> Status;
    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService=new BookService();
        Status.setItems(FXCollections.observableArrayList("Available","Not Available"));
        bookId.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getIsbn()));
        title.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));
        author.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getAuthor()));
        status.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getAvailability()));
        publisher.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(d.getValue().getPublisher()));
        edition.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getEdition())));
        quantity.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getRemaining())));
        pdfCol.setCellFactory(col->new TableCell<>() {
            private final Button btn=new Button();
            {
                btn.setOnAction(e-> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (book.hasPdf()) {
                        openUrl(book.getPdf());
                    } else {
                        ExportController.prefilledIsbn = book.getIsbn();
                        new LoadStage("/mehrin/loginpage/Export.fxml",
                                btn.getScene().getRoot(), true);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Book book = getTableView().getItems().get(getIndex());
                if (book.hasPdf()) {
                    btn.setText("View PDF");
                    btn.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; "
                            + "-fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                } else {
                    btn.setText("Add PDF");
                    btn.setStyle("-fx-background-color: #143F73; -fx-text-fill: white; "
                            + "-fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                }
                setGraphic(btn);
            }
        });

        loadBooks();
        setupSearch();
        setupTableClick();
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open link:\n" + e.getMessage());
        }
    }

    private void loadBooks() {
        booksList.setAll(bookService.getAllBooks());
        booksTable.setItems(booksList);
        booksTable.refresh();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) { booksTable.setItems(booksList); return; }
            String q = newVal.toLowerCase();
            ObservableList<Book> filtered = FXCollections.observableArrayList();
            for (Book b : booksList) {
                if (b.getTitle().toLowerCase().contains(q)||b.getAuthor().toLowerCase().contains(q)||b.getIsbn().contains(newVal)) filtered.add(b);
            }
            booksTable.setItems(filtered);
        });
    }

    private void setupTableClick() {
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldBook, book) -> {
            if (book != null) {
                bookIdField.setText(book.getIsbn());
                bookTitleField.setText(book.getTitle());
                authorField.setText(book.getAuthor());
                publisherField.setText(book.getPublisher());
                editionField.setText(String.valueOf(book.getEdition()));
                quantityField.setText(String.valueOf(book.getQuantity()));
                Status.setValue(book.getAvailability());
            }
        });
    }
    @FXML
    private void handleSave() {
        String isbn= bookIdField.getText().trim();
        String titleVal= bookTitleField.getText().trim();
        String authorVal= authorField.getText().trim();
        String publisherVal= publisherField.getText().trim();
        String editionStr= editionField.getText().trim();
        String quantityStr= quantityField.getText().trim();
        String statusVal= Status.getValue();

        if (isbn.isEmpty() || titleVal.isEmpty() || authorVal.isEmpty() || statusVal == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Book ID, Title, Author and Status are required.");
            return;
        }

        int editionVal = 1, quantityVal = 1;
        try { if (!editionStr.isEmpty())  editionVal  = Integer.parseInt(editionStr);  }
        catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Error", "Edition must be a number."); return; }
        try { if (!quantityStr.isEmpty()) quantityVal = Integer.parseInt(quantityStr); }
        catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Error", "Quantity must be a number."); return; }

        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            int quantityDiff = quantityVal - selected.getQuantity();
            int newRemaining = Math.max(0, selected.getRemaining() + quantityDiff);

            selected.setIsbn(isbn);
            selected.setTitle(titleVal);
            selected.setAuthor(authorVal);
            selected.setPublisher(publisherVal.isEmpty() ? selected.getPublisher() : publisherVal);
            selected.setEdition(editionVal);
            selected.setQuantity(quantityVal);
            selected.setRemaining(newRemaining);
            selected.setAvailability(statusVal);

            if (!writeBookToCSV(selected)) return; // error already shown inside
        } else {
            Book newBook = new Book(isbn, titleVal, authorVal,
                    publisherVal.isEmpty() ? "Unknown" : publisherVal,
                    editionVal, quantityVal, quantityVal, "General", statusVal);
            if (!writeBookToCSV(newBook)) return;
        }
        clearForm(); loadBooks();
    }
    private static final String BOOKS_HEADER =
            "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF";

    private boolean writeBookToCSV(Book book) {
        List<String> lines = FileUtil.readFile("books.csv");
        boolean found = false;

        for (int i = 0; i < lines.size(); i++) {
            Book existing = Book.fromCSV(lines.get(i));
            if (existing != null && existing.getIsbn().equalsIgnoreCase(book.getIsbn())) {
                book.setPdf(existing.getPdf());
                lines.set(i, book.toCSV());
                found = true;
            } else if (existing != null) {
                lines.set(i, existing.toCSV());
            }
        }

        if (!found) lines.add(book.toCSV());

        FileUtil.writeFile("books.csv", lines, BOOKS_HEADER);
        return true;
    }
    @FXML
    private void handleDelete() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.ERROR, "Error", "Select a book to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + selected.getTitle() + "\"?");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) { bookService.deleteBook(selected.getIsbn()); loadBooks(); clearForm(); }
    }

    @FXML private void handleCancel() { clearForm(); booksTable.getSelectionModel().clearSelection(); }

    private void clearForm() {
        bookIdField.clear(); bookTitleField.clear(); authorField.clear();
        publisherField.clear(); editionField.clear(); quantityField.clear();
        Status.setValue(null);
    }
    @FXML private void loadHomePanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Dashboard.fxml",(Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Books.fxml",(Node)e.getSource(), true); }
    @FXML private void loadStudentPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Students.fxml",(Node)e.getSource(), true); }
    @FXML private void loadIssueBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/IssueBooks.fxml",(Node)e.getSource(), true); }
    @FXML private void viewAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node)e.getSource(), true); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Announcements.fxml",(Node)e.getSource(), true); }
    @FXML private void loadExportDataPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Export.fxml",(Node)e.getSource(),true); }
    @FXML private void loadClearancePanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Clearance.fxml",(Node)e.getSource(),true); }
    @FXML private void logout(ActionEvent e){ new LoadStage("/mehrin/loginpage/Login.fxml",(Node)e.getSource(),true); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}