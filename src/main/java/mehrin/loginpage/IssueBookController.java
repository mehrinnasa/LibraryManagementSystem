package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import mehrin.loginpage.Util.FileUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IssueBookController {

    @FXML private TextField bookSearchField, studentSearchTextField;
    @FXML private Text bookName, bookAuthor, bookPublisher, availability;
    @FXML private Text studentName, studentEmail, contact;

    private String boName = "";
    private String stuName = "";

    private static final String ISSUED_FILE = "issueBooks.csv";

    // ================== SEARCH BOOK ==================
    @FXML
    private void searchBook(KeyEvent event) {

        if (event.getCode() != KeyCode.ENTER) return;

        String input = bookSearchField.getText().trim();
        if (input.isEmpty()) {
            showAlert("Field validation", "Please enter ISBN / barcode", Alert.AlertType.WARNING);
            return;
        }

        boolean found = false;

        for (String line : FileUtil.readFile("books.csv")) {
            String[] parts = line.split(",");
            if (parts.length > 8 && parts[0].equalsIgnoreCase(input)) {
                boName = parts[1];
                bookName.setText(parts[1]);
                bookAuthor.setText(parts[2]);
                bookPublisher.setText(parts[3]);
                availability.setText(parts[8]);
                found = true;
                break;
            }
        }

        if (!found) {
            showAlert("Not Found", "No such book in the system", Alert.AlertType.INFORMATION);
            clearBookFields();
        }
    }

    // ================== SEARCH STUDENT ==================
    @FXML
    private void searchStudent(KeyEvent event) {

        if (event.getCode() != KeyCode.ENTER) return;

        String input = studentSearchTextField.getText().trim();
        if (input.isEmpty()) {
            showAlert("Field validation", "Please enter Student ID", Alert.AlertType.WARNING);
            return;
        }

        boolean found = false;

        for (String line : FileUtil.readFile("students.csv")) {
            String[] parts = line.split(",");
            if (parts.length > 3 && parts[0].equalsIgnoreCase(input)) {
                stuName = parts[1];
                studentName.setText(parts[1]);
                contact.setText(parts[2]);
                studentEmail.setText(parts[3]);
                found = true;
                break;
            }
        }

        if (!found) {
            showAlert("Not Found", "Student not found", Alert.AlertType.INFORMATION);
            clearStudentFields();
        }
    }

    // ================== ISSUE BOOK ==================
    @FXML
    private void issueBook(ActionEvent event) {

        if (boName.isEmpty() || stuName.isEmpty()) {
            showAlert("Missing Info", "Please search both book and student", Alert.AlertType.WARNING);
            return;
        }

        // Duplicate check: student already has this book
        boolean alreadyIssued = FileUtil.readFile(ISSUED_FILE).stream()
                .anyMatch(line -> {
                    String[] parts = line.split(",");
                    return parts.length > 3 &&
                            parts[1].equalsIgnoreCase(bookSearchField.getText().trim()) &&
                            parts[2].equalsIgnoreCase(studentSearchTextField.getText().trim());
                });

        if (alreadyIssued) {
            showAlert("Already Issued", stuName + " already has this book", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Issue");
        confirm.setHeaderText(null);
        confirm.setContentText("Issue \"" + boName + "\" to \"" + stuName + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                issueBookInFile();
                clearAllFields();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to issue book", Alert.AlertType.ERROR);
            }
        }
    }

    // ================== FILE UPDATE LOGIC ==================
    private void issueBookInFile() throws IOException {

        // 1️⃣ Update books.csv
        List<String> books = FileUtil.readFile("books.csv");
        List<String> updatedBooks = new ArrayList<>();
        boolean bookFound = false;

        for (String line : books) {
            String[] parts = line.split(",");
            if (parts[0].equalsIgnoreCase(bookSearchField.getText().trim())) {
                bookFound = true;
                int remaining = Integer.parseInt(parts[6].trim());
                if (remaining <= 0) {
                    showAlert("Not Available", "This book is not available", Alert.AlertType.WARNING);
                    return;
                }
                remaining--;
                parts[6] = String.valueOf(remaining);
                parts[8] = (remaining == 0) ? "Not Available" : "Available";
            }
            updatedBooks.add(String.join(",", parts));
        }

        if (!bookFound) {
            showAlert("Error", "Book not found", Alert.AlertType.ERROR);
            return;
        }

        FileUtil.writeFile("books.csv", updatedBooks,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");

        // 2️⃣ Update issueBooks.csv
        List<String> issued = new ArrayList<>(FileUtil.readFile(ISSUED_FILE));

        // Generate unique IssuedID
        int lastId = -1;
        for (String line : issued) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 1 && !parts[0].equalsIgnoreCase("IssuedID")) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id > lastId) lastId = id;
                } catch (Exception ignored) {}
            }
        }
        int newIssuedId = lastId + 1;

        // Add new issued book (LateFee = 0 initially)
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(14);

        String newLine = String.join(",",
                String.valueOf(newIssuedId),
                bookSearchField.getText().trim(),
                studentSearchTextField.getText().trim(),
                stuName,
                today.toString(),
                dueDate.toString(),
                "0"
        );

        issued.add(newLine);

        FileUtil.writeFile(ISSUED_FILE, issued,
                "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");

        showAlert("Success", "Book issued successfully", Alert.AlertType.INFORMATION);
    }

    // ================== CLEAR METHODS ==================
    private void clearBookFields() {
        boName = "";
        bookSearchField.clear();
        bookName.setText("Book Title");
        bookAuthor.setText("Book Author");
        bookPublisher.setText("Book Publisher");
        availability.setText("Availability");
    }

    private void clearStudentFields() {
        stuName = "";
        studentSearchTextField.clear();
        studentName.setText("Student Name");
        studentEmail.setText("Email Address");
        contact.setText("Contact");
    }

    private void clearAllFields() {
        clearBookFields();
        clearStudentFields();
    }

    @FXML
    private void cancel(ActionEvent event) {
        clearAllFields();
    }

    // ================== NAVIGATION ==================
    @FXML private void loadHomePanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Dashboard.fxml",(Node) event.getSource(),true); }
    @FXML private void loadBooksPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Books.fxml",(Node) event.getSource(),true); }
    @FXML private void loadStudentPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Students.fxml",(Node) event.getSource(),true); }
    @FXML private void loadIssueBooksPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",(Node) event.getSource(),true); }
    @FXML private void loadReturnBooksPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node) event.getSource(),true); }
    @FXML private void viewAllIssuedBooks(ActionEvent event) { new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node) event.getSource(),true); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Announcements.fxml",(Node) event.getSource(),true); }
    @FXML private void loadExportDataPanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Export.fxml",(Node) event.getSource(),true); }
    @FXML private void loadClearancePanel(ActionEvent event) { new LoadStage("/mehrin/loginpage/Clearance.fxml",(Node) event.getSource(),true); }
    @FXML private void logout(ActionEvent event) { new LoadStage("/mehrin/loginpage/Login.fxml",(Node) event.getSource(),true); }

    // ================== ALERT ==================
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}