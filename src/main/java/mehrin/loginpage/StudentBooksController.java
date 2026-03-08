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

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class StudentBooksController implements Initializable {

    @FXML private TableView<Book>           booksTable;
    @FXML private TableColumn<Book, String> bookId;
    @FXML private TableColumn<Book, String> title;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;
    @FXML private TableColumn<Book, String> publisher;
    @FXML private TableColumn<Book, String> edition;
    @FXML private TableColumn<Book, String> quantity;
    @FXML private TableColumn<Book, String> remainingBooks;
    @FXML private TableColumn<Book, Void>   pdfCol;          // button/label column

    @FXML private TextField        searchField;
    @FXML private TextField        bookIdField;
    @FXML private TextField        bookTitleField;
    @FXML private TextField        authorField;
    @FXML private ComboBox<String> statusComboBox;

    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService = new BookService();

        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Not Available"));

        bookId.setCellValueFactory(d        -> new javafx.beans.property.SimpleStringProperty(d.getValue().getIsbn()));
        title.setCellValueFactory(d         -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTitle()));
        author.setCellValueFactory(d        -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAuthor()));
        status.setCellValueFactory(d        -> new javafx.beans.property.SimpleStringProperty(d.getValue().getAvailability()));
        publisher.setCellValueFactory(d     -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPublisher()));
        edition.setCellValueFactory(d       -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getEdition())));
        quantity.setCellValueFactory(d      -> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(d-> new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getRemaining())));

        // ── PDF column: "View PDF" button if linked, "N/A" label if not ──
        pdfCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View PDF");
            private final Label  naLabel = new Label("N/A");
            {
                viewBtn.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; "
                        + "-fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                naLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

                viewBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    openPdf(book.getPdf());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Book book = getTableView().getItems().get(getIndex());
                setGraphic(book.hasPdf() ? viewBtn : naLabel);
            }
        });

        loadBooks();
        setupSearch();
        setupTableClick();
    }

    // ─────────────────────────────────────────────────────────────
    private void openPdf(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                new Alert(Alert.AlertType.ERROR, "File not found:\n" + path).showAndWait();
                return;
            }
            Desktop.getDesktop().open(f);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not open file.").showAndWait();
        }
    }

    private void loadBooks() {
        booksList.setAll(bookService.getAllBooks());
        booksTable.setItems(booksList);
    }

    // ================= SEARCH =================
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) { booksTable.setItems(booksList); return; }
            String q = newVal.toLowerCase();
            ObservableList<Book> filtered = FXCollections.observableArrayList();
            for (Book book : booksList) {
                if (book.getTitle().toLowerCase().contains(q)
                        || book.getAuthor().toLowerCase().contains(q)
                        || book.getIsbn().contains(newVal)) {
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
    private void handleCancel() { clearForm(); booksTable.getSelectionModel().clearSelection(); }

    private void clearForm() {
        bookIdField.clear(); bookTitleField.clear();
        authorField.clear(); statusComboBox.setValue(null);
    }

    // ================= NAVIGATION =================
    @FXML private void loadHomePanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",    (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)         { new LoadStage("/mehrin/loginpage/StudentBooks.fxml",        (Node)e.getSource(), true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml",    (Node)e.getSource(), true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml",(Node)e.getSource(), true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e)  { new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml", (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentClearance.fxml",    (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                 { new LoadStage("/mehrin/loginpage/Login.fxml",               (Node)e.getSource(), true); }
}