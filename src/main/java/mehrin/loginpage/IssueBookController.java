package mehrin.loginpage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mehrin.loginpage.Util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class IssueBookController {

    @FXML
    private TextField bookSearchField, studentSearchTextField;

    @FXML
    private Text bookName, bookAuthor, bookPublisher, availability;

    @FXML
    private Text studentName, studentEmail, contact;

    private String boName = "";
    private String stuName = "";

    private static final String ISSUED_FILE = "issueBooks.csv";

    // ================== SEARCH BOOK ==================
    @FXML
    private void searchBook(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String input = bookSearchField.getText().trim();
            if (input.isEmpty()) {
                showAlert("Field validation", "Please enter ISBN / barcode", Alert.AlertType.WARNING);
                return;
            }

            boolean found = false;
            for (String line : FileUtil.readFile("books.csv")) {
                String[] parts = line.split(",");
                if (parts.length > 1 && parts[0].equalsIgnoreCase(input)) {
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
    }

    // ================== SEARCH STUDENT ==================
    @FXML
    private void searchStudent(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String input = studentSearchTextField.getText().trim();
            if (input.isEmpty()) {
                showAlert("Field validation", "Please enter Student ID", Alert.AlertType.WARNING);
                return;
            }

            boolean found = false;
            for (String line : FileUtil.readFile("students.csv")) {
                String[] parts = line.split(",");
                if (parts.length > 1 && parts[0].equalsIgnoreCase(input)) {
                    stuName = parts[1];
                    studentName.setText(parts[1]);
                    studentEmail.setText(parts[3]);
                    contact.setText(parts[2]);
                    found = true;
                    break;
                }
            }

            if (!found) {
                showAlert("Not Found", "Student not found", Alert.AlertType.INFORMATION);
                clearStudentFields();
            }
        }
    }

    // ================== ISSUE BOOK ==================
    @FXML
    private void issueBook(ActionEvent event) {
        if (boName.isEmpty() || stuName.isEmpty()) {
            showAlert("Missing Info", "Please search both book and student", Alert.AlertType.WARNING);
            return;
        }

        // Check if student already has this book
        boolean alreadyIssued = FileUtil.readFile(ISSUED_FILE).stream()
                .anyMatch(line -> {
                    String[] parts = line.split(",");
                    return parts[0].equalsIgnoreCase(bookSearchField.getText().trim()) &&
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
                showAlert("Success", "Book issued successfully", Alert.AlertType.INFORMATION);
                clearAllFields();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to issue book", Alert.AlertType.ERROR);
            }
        }
    }

    private void issueBookInFile() throws IOException {
        List<String> books = FileUtil.readFile("books.csv");
        List<String> updatedBooks = new ArrayList<>();
        for (String line : books) {
            String[] parts = line.split(",");
            if (parts[0].equalsIgnoreCase(bookSearchField.getText().trim())) {
                int remaining = Integer.parseInt(parts[6].trim());
                if (remaining > 0) {
                    remaining--;
                    parts[6] = String.valueOf(remaining);
                    parts[8] = (remaining == 0) ? "Not Available" : "Available";
                }
                line = String.join(",", parts);
            }
            updatedBooks.add(line);
        }
        FileUtil.writeFile("books.csv", updatedBooks,
                "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");

        // Append to issuedBooks.csv
        List<String> issued = new ArrayList<>(FileUtil.readFile(ISSUED_FILE));
        LocalDate today = LocalDate.now();
        issued.add(String.join(",", bookSearchField.getText().trim(), boName,
                studentSearchTextField.getText().trim(), stuName, today.toString(), today.plusDays(14).toString()));
        FileUtil.writeFile(ISSUED_FILE, issued,
                "BookID,BookName,StudentID,StudentName,IssuedDate,ReturnDate");
    }

    @FXML
    private void cancel(ActionEvent event) {
        clearAllFields();
    }

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
    private void loadHomePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", node,true); }

    @FXML
    private void loadBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Books.fxml", node,true);
    }

    @FXML
    private void loadStudentPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Students.fxml", node,true);
    }

    @FXML
    private void loadIssueBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/IssueBooks.fxml", node,true);
    }

    @FXML
    private void loadReturnBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/ReturnBooks.fxml", node,true);
    }

    @FXML
    private void viewAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", node,true);
    }

    @FXML
    private void loadSendAnnouncementsPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Announcements.fxml", node,true);
    }

    @FXML
    private void loadExportDataPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Export.fxml", node,true);
    }

    @FXML
    private void loadClearancePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Clearance.fxml", node,true);
    }

    @FXML
    private void logout(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node,true);
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
