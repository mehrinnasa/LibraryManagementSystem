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
    @FXML private TextField        publisherField;
    @FXML private TextField        editionField;
    @FXML private TextField        quantityField;
    @FXML private TextField        statusField;

    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService = new BookService();

        //statusField.setText(String.valueOf(FXCollections.observableArrayList("Available", "Not Available")));

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
    /** True when Java is running inside WSL (checks /proc/version for "microsoft"/"wsl"). */
    private static boolean isRunningInWSL() {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("/proc/version");
            if (java.nio.file.Files.exists(p)) {
                String v = new String(java.nio.file.Files.readAllBytes(p)).toLowerCase();
                return v.contains("microsoft") || v.contains("wsl");
            }
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Opens a URL in the default browser.
     * Works in WSL, native Windows, macOS, and plain Linux.
     */
    private void openPdf(String rawUrl) {
        // Convert Google Drive /view links to /preview so they render in-browser
        String url = rawUrl.replaceAll("/view(\\?.*)?$", "/preview");
        Thread t = new Thread(() -> {
            String lastErr = "Unknown error";

            // 1. WSL: use Windows host tools
            if (isRunningInWSL()) {
                try {
                    Process p = new ProcessBuilder(
                            "powershell.exe", "-NoProfile", "-NonInteractive",
                            "-Command", "Start-Process", "'" + url + "'")
                            .redirectErrorStream(true).start();
                    p.waitFor();
                    return;
                } catch (Exception e) { lastErr = e.getMessage(); }
                try {
                    new ProcessBuilder("/mnt/c/Windows/System32/cmd.exe", "/c", "start", "", url).start();
                    return;
                } catch (Exception e) { lastErr = e.getMessage(); }
            }

            // 2. Native Windows
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                try { new ProcessBuilder("cmd", "/c", "start", "", url).start(); return; }
                catch (Exception e) { lastErr = e.getMessage(); }
            }

            // 3. macOS
            if (os.contains("mac")) {
                try { new ProcessBuilder("open", url).start(); return; }
                catch (Exception e) { lastErr = e.getMessage(); }
            }

            // 4. Generic Linux
            try { new ProcessBuilder("xdg-open", url).start(); return; }
            catch (Exception e) { lastErr = e.getMessage(); }

            // All methods failed
            final String msg = lastErr;
            javafx.application.Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, "Could not open PDF:\n" + msg).showAndWait()
            );
        });
        t.setDaemon(true);
        t.start();
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
                publisherField.setText(book.getPublisher());
                editionField.setText(String.valueOf(book.getEdition()));
                quantityField.setText(String.valueOf(book.getQuantity()));
                statusField.setText(book.getAvailability());
            }
        });
    }

    // ================= CANCEL =================
    @FXML
    private void handleCancel() { clearForm(); booksTable.getSelectionModel().clearSelection(); }

    private void clearForm() {
        bookIdField.clear(); bookTitleField.clear();
        authorField.clear(); statusField.setText(null);
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