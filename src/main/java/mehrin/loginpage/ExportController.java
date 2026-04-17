package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ExportController {

    public static String prefilledIsbn = null;

    @FXML private TextField bookSearchField;
    @FXML private TextField driveUrlField;
    @FXML private Text bookName;
    @FXML private Text bookAuthor;
    @FXML private Text bookPublisher;
    @FXML private Text availability;
    @FXML private Label currentLinkLabel;

    private Book selectedBook = null;
    private static final String BOOKS_DATA_FILE = "data/books.csv";

    @FXML
    public void initialize() {
        if (prefilledIsbn != null) {
            bookSearchField.setText(prefilledIsbn);
            fillBookInfoByQuery(prefilledIsbn.toLowerCase());
            prefilledIsbn = null;
        } else {
            clearBookInfo();
        }
    }

    @FXML
    private void searchBook() {
        String query = bookSearchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { 
            clearBookInfo(); 
            return; 
        }
        fillBookInfoByQuery(query);
    }

    private void fillBookInfo(String isbn) {
        fillBookInfoByQuery(isbn.toLowerCase());
    }

    private void fillBookInfoByQuery(String query) {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_DATA_FILE))) {
            String line; 
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { 
                    isFirstLine = false; 
                    continue; 
                }
                
                Book book = Book.fromCSV(line);
                if (book == null) continue;
                
                if (book.getIsbn().toLowerCase().contains(query) || book.getTitle().toLowerCase().contains(query)) {
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
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        
        clearBookInfo();
    }

    @FXML
    private void handleSaveLink(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first."); 
            return;
        }
        
        String url = driveUrlField.getText().trim();
        if (url.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No URL", "Paste a Google Drive link first."); 
            return;
        }
        
        if (!url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "Invalid URL", "URL must start with https://\nGo to Google Drive → Share → Copy link."); 
            return;
        }

        url = normalizeDriveUrl(url);

        if (saveLinkToCSV(selectedBook.getIsbn(), url)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF added successfully for \"" + selectedBook.getTitle() + "\"!");
            bookSearchField.clear();
            clearBookInfo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update books.csv.");
        }
    }

    @FXML
    private void handleClearBookInfo() {
        bookSearchField.clear();
        clearBookInfo();
    }

    @FXML
    private void handleClearLink(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first."); 
            return;
        }
        
        if (!selectedBook.hasPdf()) {
            showAlert(Alert.AlertType.INFORMATION, "Nothing to Clear", "This book has no link saved."); 
            return;
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

    @FXML
    private void handleView(ActionEvent event) {
        String url = driveUrlField.getText().trim();
        if (url.isEmpty() || !url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "No Link", "Paste or save a legitimate link first."); 
            return;
        }
        openUrl(url);
    }

    private String normalizeDriveUrl(String url) {
        if (url.contains("drive.google.com/file/d/") && url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private void openUrl(String url) {
        try { 
            Desktop.getDesktop().browse(new URI(url)); 
        } catch (Exception e) { 
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open link: " + e.getMessage()); 
        }
    }

    private boolean saveLinkToCSV(String isbn, String url) {
        try {
            List<String> lines = new ArrayList<>();
            boolean hasChanged = false;

            try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_DATA_FILE))) {
                String line; 
                boolean isFirstLine = true;
                
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        lines.add("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");
                        isFirstLine = false;
                        continue;
                    }
                    
                    Book book = Book.fromCSV(line);
                    if (book != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                        book.setPdf(url);
                        line = book.toCSV();
                        hasChanged = true;
                    } else if (book != null) {
                        line = book.toCSV();
                    }
                    lines.add(line);
                }
            }

            if (!hasChanged) return false;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_DATA_FILE))) {
                for (String updatedLine : lines) { 
                    bw.write(updatedLine); 
                    bw.newLine(); 
                }
            }
            return true;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return false; 
        }
    }

    private void clearBookInfo() {
        selectedBook = null;
        bookName.setText("-"); 
        bookAuthor.setText("-");
        bookPublisher.setText("-"); 
        availability.setText("-");
        driveUrlField.clear();
        if (currentLinkLabel != null) {
            currentLinkLabel.setText("No link saved yet.");
            currentLinkLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        }
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        Node node = (Node) event.getSource();
        new LoadStage(fxmlPath, node, true);
    }

    @FXML private void handleHome(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void handleBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void handleStudents(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void handleIssueBook(ActionEvent event) { loadPage(event, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void handleAllIssuedBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAnnouncement(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void handleExport(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void handleClearance(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void logout(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Login.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); 
        alert.setTitle(title);
        alert.setHeaderText(null); 
        alert.setContentText(msg); 
        alert.showAndWait();
    }
}