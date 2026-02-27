package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert;

import mehrin.loginpage.Model.Book;

import java.io.*;
import java.awt.Desktop;

public class ExportController {

    @FXML private ComboBox<String> reportComboBox;
    @FXML private TextField bookSearchField;

    @FXML private Text bookName;
    @FXML private Text bookAuthor;
    @FXML private Text bookPublisher;
    @FXML private Text availability;

    private Book selectedBook;

    private static final String CSV_PATH = "data/books.csv";

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {
        reportComboBox.getItems().addAll(
                "Books Report",
                "Issued Books Report"
        );
        clearBookInfo();
    }

    // ================= SEARCH BOOK =================
    @FXML
    private void searchBook(KeyEvent event) {

        String isbn = bookSearchField.getText().trim();

        if (isbn.isEmpty()) {
            clearBookInfo();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {

            String line;
            br.readLine(); // header skip
            boolean found = false;

            while ((line = br.readLine()) != null) {

                Book book = Book.fromCSV(line);
                if (book == null) continue;

                if (book.getIsbn().equals(isbn)) {

                    selectedBook = book;

                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());

                    found = true;
                    break;
                }
            }

            if (!found) clearBookInfo();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CLEAR =================
    private void clearBookInfo() {
        selectedBook = null;
        bookName.setText("-");
        bookAuthor.setText("-");
        bookPublisher.setText("-");
        availability.setText("-");
    }

    // ================= SHOW PDF =================
    @FXML
    private void exportPDF() {

        if (selectedBook == null) {
            showAlert("Please search and select a book first.");
            return;
        }

        try {
            File pdfFile = new File(selectedBook.getPdf());

            if (!pdfFile.exists()) {
                showAlert("PDF file not found!");
                return;
            }

            Desktop.getDesktop().open(pdfFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EXPORT WORD =================
    @FXML
    private void exportWord() {

        if (selectedBook == null) return;

        try (PrintWriter pw = new PrintWriter("exported_book.doc")) {

            pw.println("BOOK INFORMATION");
            pw.println("================");
            writeBook(pw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EXPORT EXCEL =================
    @FXML
    private void exportExcel() {

        if (selectedBook == null) return;

        try (PrintWriter pw = new PrintWriter("exported_book.csv")) {

            pw.println("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");
            pw.println(selectedBook.toCSV());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= COMMON WRITE =================
    private void writeBook(PrintWriter pw) {

        pw.println("ISBN: " + selectedBook.getIsbn());
        pw.println("Title: " + selectedBook.getTitle());
        pw.println("Author: " + selectedBook.getAuthor());
        pw.println("Publisher: " + selectedBook.getPublisher());
        pw.println("Edition: " + selectedBook.getEdition());
        pw.println("Quantity: " + selectedBook.getQuantity());
        pw.println("Remaining: " + selectedBook.getRemaining());
        pw.println("Status: " + selectedBook.getAvailability());
        pw.println("PDF: " + selectedBook.getPdf());
    }

    // ================= ALERT =================
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleHome(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", node,true);
    }

    @FXML
    private void handleBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Books.fxml", node,true);
    }

    @FXML
    private void handleStudents(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Students.fxml", node,true);
    }

    @FXML
    private void handleIssueBook(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/IssueBooks.fxml", node,true);
    }

    @FXML
    private void handleAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", node,true);
    }

    @FXML
    private void handleAnnouncement(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Announcements.fxml", node,true);
    }

    @FXML
    private void handleExport(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Export.fxml", node,true);
    }

    @FXML
    private void handleClearance(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Clearance.fxml", node,true);
    }

    @FXML
    private void logout(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node,true);
    }

}