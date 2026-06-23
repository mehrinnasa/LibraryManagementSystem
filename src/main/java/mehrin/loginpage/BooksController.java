package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Service.BookService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController implements Initializable {

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookId;
    @FXML private TableColumn<Book, String> title;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;
    @FXML private TableColumn<Book, String> publisher;
    @FXML private TableColumn<Book, String> edition;
    @FXML private TableColumn<Book, String> quantity;
    @FXML private TableColumn<Book, String> remainingBooks;
    @FXML private TableColumn<Book, Void> pdfCol;

    @FXML private TextField searchField;
    
    // book data fields
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private TextField editionField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> statusComboBox;

    private ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService service;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new BookService();
        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Not Available"));

        // init table columns
        bookId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn()));
        title.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        author.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        status.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAvailability()));
        publisher.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPublisher()));
        edition.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getEdition())));
        quantity.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRemaining())));

        setupPdfBtn();
        
        // initial load
        booksList.setAll(service.getAllBooks());
        booksTable.setItems(booksList);

        // live search
        searchField.textProperty().addListener((observable, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) { 
                booksTable.setItems(booksList); 
                return; 
            }
            
            String needle = newText.toLowerCase();
            ObservableList<Book> results = FXCollections.observableArrayList();
            
            for (int i = 0; i < booksList.size(); i++) {
                Book b = booksList.get(i);
                if (b.getTitle().toLowerCase().contains(needle) || 
                    b.getAuthor().toLowerCase().contains(needle) || 
                    b.getIsbn().contains(newText)) {
                    results.add(b);
                }
            }
            booksTable.setItems(results);
        });

        // select from table
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            if (selected != null) {
                bookIdField.setText(selected.getIsbn());
                bookTitleField.setText(selected.getTitle());
                authorField.setText(selected.getAuthor());
                publisherField.setText(selected.getPublisher());
                editionField.setText(String.valueOf(selected.getEdition()));
                quantityField.setText(String.valueOf(selected.getQuantity()));
                statusComboBox.setValue(selected.getAvailability());
            }
        });
    }

    /**
     * Detects whether Java is running inside WSL (Windows Subsystem for Linux).
     * WSL kernels report "microsoft" or "WSL" in /proc/version.
     */
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
     * Opens a URL in the default browser, handling:
     *   - WSL  : uses powershell.exe / cmd.exe from the Windows host
     *   - Windows (native) : cmd /c start
     *   - macOS : open
     *   - Linux : xdg-open
     * Each method is tried in sequence; the first that succeeds wins.
     */
    private static void openInBrowser(String rawUrl, javafx.scene.Scene scene) {
        // Convert Google Drive /view links to /preview so they render in-browser
        String url = rawUrl.replaceAll("/view(\\?.*)?$", "/preview");
        Thread t = new Thread(() -> {
            String lastErr = "Unknown error";

            // ── 1. WSL: open via Windows tools accessible from the Linux layer ──
            if (isRunningInWSL()) {
                // powershell.exe is in PATH inside WSL
                try {
                    Process p = new ProcessBuilder(
                            "powershell.exe", "-NoProfile", "-NonInteractive",
                            "-Command", "Start-Process", "'" + url + "'")
                            .redirectErrorStream(true).start();
                    p.waitFor();
                    return;   // success
                } catch (Exception e) { lastErr = e.getMessage(); }

                // Fallback: reach Windows cmd.exe directly via /mnt/c
                try {
                    new ProcessBuilder(
                            "/mnt/c/Windows/System32/cmd.exe", "/c", "start", "", url)
                            .start();
                    return;   // success
                } catch (Exception e) { lastErr = e.getMessage(); }
            }

            // ── 2. Native Windows ──
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                try {
                    new ProcessBuilder("cmd", "/c", "start", "", url).start();
                    return;
                } catch (Exception e) { lastErr = e.getMessage(); }
            }

            // ── 3. macOS ──
            if (os.contains("mac")) {
                try {
                    new ProcessBuilder("open", url).start();
                    return;
                } catch (Exception e) { lastErr = e.getMessage(); }
            }

            // ── 4. Generic Linux / xdg-open ──
            try {
                new ProcessBuilder("xdg-open", url).start();
                return;
            } catch (Exception e) { lastErr = e.getMessage(); }

            // ── All methods failed ── show error on FX thread ──
            final String msg = lastErr;
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Could not open PDF link:\n" + msg);
                a.showAndWait();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void setupPdfBtn() {
        pdfCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                btn.setOnAction(evt -> {
                    Book b = getTableView().getItems().get(getIndex());
                    if (b.hasPdf()) {
                        openInBrowser(b.getPdf(), btn.getScene());
                    } else {
                        ExportController.prefilledIsbn = b.getIsbn();
                        new LoadStage("/mehrin/loginpage/Export.fxml", btn.getScene().getRoot(), true);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { 
                    setGraphic(null); 
                    return; 
                }
                
                Book bk = getTableView().getItems().get(getIndex());
                if (bk.hasPdf()) {
                    btn.setText("View PDF");
                    btn.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                } else {
                    btn.setText("Add PDF");
                    btn.setStyle("-fx-background-color: #143F73; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                }
                setGraphic(btn);
            }
        });
    }

    @FXML
    private void handleSave() {
        // check required
        boolean missing = bookIdField.getText().trim().isEmpty() || 
                          bookTitleField.getText().trim().isEmpty() || 
                          authorField.getText().trim().isEmpty() || 
                          statusComboBox.getValue() == null;
                          
        if (missing) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Validation Error");
            a.setHeaderText(null);
            a.setContentText("Book ID, Title, Author and Status are required.");
            a.showAndWait();
            return;
        }

        int ed = 1;
        int qty = 1;
        
        try {
            if (!editionField.getText().trim().isEmpty()) {
                ed = Integer.parseInt(editionField.getText().trim());
            }
        } catch(NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Edition must be a valid number.");
            a.showAndWait();
            return;
        }
        
        try {
            if (!quantityField.getText().trim().isEmpty()) {
                qty = Integer.parseInt(quantityField.getText().trim());
            }
        } catch(NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Quantity must be a valid number.");
            a.showAndWait();
            return;
        }

        Book current = booksTable.getSelectionModel().getSelectedItem();
        
        if (current != null) {
            int diff = qty - current.getQuantity();
            int newRemain = Math.max(0, current.getRemaining() + diff);

            String pub = publisherField.getText().trim();

            current.setIsbn(bookIdField.getText().trim());
            current.setTitle(bookTitleField.getText().trim());
            current.setAuthor(authorField.getText().trim());
            current.setPublisher(pub.isEmpty() ? current.getPublisher() : pub);
            current.setEdition(ed);
            current.setQuantity(qty);
            current.setRemaining(newRemain);
            current.setAvailability(statusComboBox.getValue());

            writeBookCsv(current);
        } else {
            String pub = publisherField.getText().trim();
            Book newBk = new Book(
                bookIdField.getText().trim(),
                bookTitleField.getText().trim(),
                authorField.getText().trim(),
                pub.isEmpty() ? "Unknown" : pub,
                ed, 
                qty, 
                qty, 
                "General", 
                statusComboBox.getValue()
            );
            writeBookCsv(newBk);
        }
        
        bookIdField.setText(""); 
        bookTitleField.setText(""); 
        authorField.setText("");
        publisherField.setText(""); 
        editionField.setText(""); 
        quantityField.setText("");
        statusComboBox.setValue(null);
        
        // refresh data
        booksList.setAll(service.getAllBooks());
        booksTable.setItems(booksList);
    }

    private void writeBookCsv(Book targetBook) {
        File f = new File("data/books.csv");
        List<String> textLines = new ArrayList<>();
        boolean isFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String l;
            boolean first = true;
            while ((l = reader.readLine()) != null) {
                if (first) {
                    textLines.add("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");
                    first = false;
                    continue;
                }
                
                Book parsed = Book.fromCSV(l);
                if (parsed != null && parsed.getIsbn().equalsIgnoreCase(targetBook.getIsbn())) {
                    targetBook.setPdf(parsed.getPdf());
                    textLines.add(targetBook.toCSV());
                    isFound = true;
                } else {
                    if (parsed != null) textLines.add(parsed.toCSV());
                    else textLines.add(l);
                }
            }
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Cannot read books.csv: " + ex.getMessage());
            a.showAndWait();
            return;
        }

        if (!isFound) {
            textLines.add(targetBook.toCSV());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, false))) {
            for (int i = 0; i < textLines.size(); i++) { 
                writer.write(textLines.get(i)); 
                writer.newLine(); 
            }
            writer.flush();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Cannot write books.csv: " + ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void handleDelete() {
        Book toDelete = booksTable.getSelectionModel().getSelectedItem();
        
        if (toDelete == null) { 
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Please select a book to delete.");
            a.showAndWait();
            return; 
        }
        
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setHeaderText(null);
        conf.setContentText("Are you sure you want to delete \"" + toDelete.getTitle() + "\"?");
        
        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) { 
            service.deleteBook(toDelete.getIsbn()); 
            
            // force refresh
            booksList.setAll(service.getAllBooks());
            booksTable.setItems(booksList);
            
            bookIdField.setText(""); 
            bookTitleField.setText(""); 
            authorField.setText("");
            publisherField.setText(""); 
            editionField.setText(""); 
            quantityField.setText("");
            statusComboBox.setValue(null);
        }
    }

    @FXML 
    private void handleCancel() { 
        bookIdField.setText(""); 
        bookTitleField.setText(""); 
        authorField.setText("");
        publisherField.setText(""); 
        editionField.setText(""); 
        quantityField.setText("");
        statusComboBox.setValue(null);
        
        booksTable.getSelectionModel().clearSelection(); 
    }

    // Nav stuff
    private void switchScene(ActionEvent evt, String fxml) {
        Node sourceNode = (Node) evt.getSource();
        new LoadStage(fxml, sourceNode, true);
    }

    @FXML private void loadHomePanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void loadBooksPanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void loadStudentPanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void loadIssueBooksPanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void viewAllIssuedBooks(ActionEvent e) { switchScene(e, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void loadExportDataPanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void loadClearancePanel(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void logout(ActionEvent e) { switchScene(e, "/mehrin/loginpage/Login.fxml"); }
}