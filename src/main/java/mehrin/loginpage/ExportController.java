package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ExportController {

    // ── Static handshake from BooksController ──────────────────
    public static String prefilledIsbn = null;

    @FXML private TextField bookSearchField;
    @FXML private TextField driveUrlField;
    @FXML private Text      bookName;
    @FXML private Text      bookAuthor;
    @FXML private Text      bookPublisher;
    @FXML private Text      availability;
    @FXML private Label     currentLinkLabel;

    private Book selectedBook = null;

    private static final String BOOKS_CSV = "data/books.csv";

    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        if (prefilledIsbn != null) {
            bookSearchField.setText(prefilledIsbn);
            fillBookInfo(prefilledIsbn);
            prefilledIsbn = null;
        } else {
            clearBookInfo();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchBook() {
        String query = bookSearchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { clearBookInfo(); return; }
        fillBookInfoByQuery(query);
    }

    private void fillBookInfo(String isbn) {
        fillBookInfoByQuery(isbn.toLowerCase());
    }

    private void fillBookInfoByQuery(String query) {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
            String line; boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; }
                Book book = Book.fromCSV(line);
                if (book == null) continue;
                if (book.getIsbn().toLowerCase().contains(query)
                        || book.getTitle().toLowerCase().contains(query)) {
                    selectedBook = book;
                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    if (book.hasPdf()) {
                        driveUrlField.setText(book.getPdf());
                        currentLinkLabel.setText("✅ Current link: " + book.getPdf());
                        currentLinkLabel.setStyle("-fx-text-fill: #2D6A4F; -fx-font-size: 11px;");
                    } else {
                        driveUrlField.clear();
                        currentLinkLabel.setText("No link saved yet.");
                        currentLinkLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                    }
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        clearBookInfo();
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE LINK
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleSaveLink(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first."); return;
        }
        String url = driveUrlField.getText().trim();
        if (url.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No URL", "Paste a Google Drive link first."); return;
        }
        if (!url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "Invalid URL",
                    "URL must start with https://\nGo to Google Drive → Share → Copy link."); return;
        }

        url = normalizeDriveUrl(url);

        if (saveLinkToCSV(selectedBook.getIsbn(), url)) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "PDF added successfully for \"" + selectedBook.getTitle() + "\"!");
            bookSearchField.clear();
            clearBookInfo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update books.csv.");
        }
    }
    // ─────────────────────────────────────────────────────────────
    //  CLEAR BOOK SEARCH (left panel Clear button)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleClearBookInfo() {
        bookSearchField.clear();
        clearBookInfo();
    }


    // ─────────────────────────────────────────────────────────────
    //  CLEAR LINK
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleClearLink(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first."); return;
        }
        if (!selectedBook.hasPdf()) {
            showAlert(Alert.AlertType.INFORMATION, "Nothing to Clear", "This book has no link saved."); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Remove the PDF link from \"" + selectedBook.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (saveLinkToCSV(selectedBook.getIsbn(), "")) {
                    selectedBook.setPdf("");
                    driveUrlField.clear();
                    currentLinkLabel.setText("Link cleared.");
                    currentLinkLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 11px;");
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  VIEW IN BROWSER
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleView(ActionEvent event) {
        String url = driveUrlField.getText().trim();
        if (url.isEmpty() || !url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "No Link", "Paste or save a link first."); return;
        }
        openUrl(url);
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private String normalizeDriveUrl(String url) {
        if (url.contains("drive.google.com/file/d/") && url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private void openUrl(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Error", "Could not open link: " + e.getMessage()); }
    }

    /**
     * Finds the row by ISBN and rewrites it as a clean 9-col row:
     *   ISBN,Title,Author,Publisher,Edition,Qty,Remaining,Availability,PDF
     * This also fixes any pre-existing malformed rows (double "Available") on save.
     */
    private boolean saveLinkToCSV(String isbn, String url) {
        try {
            List<String> lines = new ArrayList<>();
            boolean changed = false;

            try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
                String line; boolean isFirst = true;
                while ((line = br.readLine()) != null) {
                    if (isFirst) {
                        // Normalize header to clean 9-col
                        lines.add("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");
                        isFirst = false;
                        continue;
                    }
                    Book book = Book.fromCSV(line);
                    if (book != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                        book.setPdf(url);
                        line = book.toCSV(); // always writes clean 9-col
                        changed = true;
                    } else if (book != null) {
                        line = book.toCSV(); // clean up any other malformed rows too
                    }
                    lines.add(line);
                }
            }

            if (!changed) return false;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_CSV))) {
                for (String l : lines) { bw.write(l); bw.newLine(); }
            }
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void clearBookInfo() {
        selectedBook = null;
        bookName.setText("-"); bookAuthor.setText("-");
        bookPublisher.setText("-"); availability.setText("-");
        driveUrlField.clear();
        if (currentLinkLabel != null) {
            currentLinkLabel.setText("No link saved yet.");
            currentLinkLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void handleHome(ActionEvent e)          { new LoadStage("/mehrin/loginpage/Dashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void handleBooks(ActionEvent e)         { new LoadStage("/mehrin/loginpage/Books.fxml",          (Node)e.getSource(), true); }
    @FXML private void handleStudents(ActionEvent e)      { new LoadStage("/mehrin/loginpage/Students.fxml",       (Node)e.getSource(), true); }
    @FXML private void handleIssueBook(ActionEvent e)     { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",     (Node)e.getSource(), true); }
    @FXML private void handleAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void handleAnnouncement(ActionEvent e)  { new LoadStage("/mehrin/loginpage/Announcements.fxml",  (Node)e.getSource(), true); }
    @FXML private void handleExport(ActionEvent e)        { new LoadStage("/mehrin/loginpage/Export.fxml",         (Node)e.getSource(), true); }
    @FXML private void handleClearance(ActionEvent e)     { new LoadStage("/mehrin/loginpage/Clearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)              { new LoadStage("/mehrin/loginpage/Login.fxml",          (Node)e.getSource(), true); }
}