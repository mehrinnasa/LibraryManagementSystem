package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Util.FileUtil;

import java.awt.Desktop;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExportController {

    // ── Static handshake: BooksController sets this before navigating ──
    public static String prefilledIsbn = null;

    @FXML private ComboBox<String> reportComboBox;
    @FXML private TextField        bookSearchField;
    @FXML private Text             bookName;
    @FXML private Text             bookAuthor;
    @FXML private Text             bookPublisher;
    @FXML private Text             availability;
    @FXML private Label            selectedFileLabel;   // shows chosen file path

    private Book   selectedBook = null;
    private File   chosenFile   = null;

    private static final String BOOKS_CSV = "data/books.csv";

    // ─────────────────────────────────────────────────────────────
    //  INIT
    // ─────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        reportComboBox.getItems().addAll("PDF Document", "Word Document", "Excel Document");
        reportComboBox.setValue("PDF Document");

        // If arriving from "Add PDF" button in Books, pre-fill the book
        if (prefilledIsbn != null) {
            bookSearchField.setText(prefilledIsbn);
            fillBookInfo(prefilledIsbn);
            prefilledIsbn = null; // consume it
        } else {
            clearBookInfo();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH BOOK  (called on key release)
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void searchBook() {
        String query = bookSearchField.getText().trim().toLowerCase();
        if (query.isEmpty()) { clearBookInfo(); return; }

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
            String line;
            boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; }
                Book book = Book.fromCSV(line);
                if (book == null) continue;
                // Match by ISBN or title (contains)
                if (book.getIsbn().equalsIgnoreCase(query)
                        || book.getTitle().toLowerCase().contains(query)) {
                    selectedBook = book;
                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        clearBookInfo();
    }

    // ─────────────────────────────────────────────────────────────
    //  BROWSE — pick the file to attach
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void browseFile(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select File");

        String format = reportComboBox.getValue();
        if ("PDF Document".equals(format)) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else if ("Word Document".equals(format)) {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Files", "*.docx", "*.doc"));
        } else {
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls", "*.csv"));
        }
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            chosenFile = file;
            selectedFileLabel.setText(file.getAbsolutePath());
            selectedFileLabel.setStyle("-fx-text-fill: #2D6A4F;");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  EXPORT — save the file path into books.csv
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void Export(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Please search and select a book first.");
            return;
        }
        if (chosenFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File", "Please browse and select a file first.");
            return;
        }

        // Save the file path into books.csv for this book
        String filePath = chosenFile.getAbsolutePath();
        if (savePdfPathToCSV(selectedBook.getIsbn(), filePath)) {
            selectedBook.setPdf(filePath);
            showAlert(Alert.AlertType.INFORMATION, "Saved",
                    "File linked to \"" + selectedBook.getTitle() + "\" successfully.\n"
                            + "You can now view it from the Books page.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update books.csv.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE PATH TO books.csv
    // ─────────────────────────────────────────────────────────────
    private boolean savePdfPathToCSV(String isbn, String filePath) {
        try {
            List<String> lines   = new ArrayList<>();
            boolean      changed = false;

            try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
                String line;
                boolean isFirst = true;
                while ((line = br.readLine()) != null) {
                    if (isFirst) { lines.add(line); isFirst = false; continue; }
                    String[] p = line.split(",", -1);
                    if (p.length >= 9 && p[0].trim().equalsIgnoreCase(isbn)) {
                        // Ensure 10 columns
                        if (p.length < 10) {
                            line = line + "," + filePath;
                        } else {
                            p[9] = filePath;
                            line = String.join(",", p);
                        }
                        changed = true;
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

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private void fillBookInfo(String isbn) {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {
            String line; boolean isFirst = true;
            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; }
                Book book = Book.fromCSV(line);
                if (book != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                    selectedBook = book;
                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    if (book.hasPdf()) {
                        chosenFile = new File(book.getPdf());
                        selectedFileLabel.setText(book.getPdf());
                        selectedFileLabel.setStyle("-fx-text-fill: #2D6A4F;");
                    }
                    return;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearBookInfo() {
        selectedBook = null; chosenFile = null;
        bookName.setText("-"); bookAuthor.setText("-");
        bookPublisher.setText("-"); availability.setText("-");
        if (selectedFileLabel != null) {
            selectedFileLabel.setText("No file selected");
            selectedFileLabel.setStyle("-fx-text-fill: #999;");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
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