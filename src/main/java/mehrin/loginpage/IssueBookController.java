package mehrin.loginpage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mehrin.loginpage.Util.FileUtil;

import java.util.Optional;

public class IssueBookController {

    @FXML
    private Label minimise, fullscreen, unfullscreen, close;

    @FXML
    private TextField bookSearchField, studentSearchTextField;

    @FXML
    private Text bookName, bookAuthor, bookPublisher, availability;

    @FXML
    private Text studentName, studentEmail, contact;

    @FXML
    private javafx.scene.control.Button issueBook;

    private String boName = "";
    private String stuName = "";

    // ================== WINDOW CONTROLS ==================
    @FXML
    private void minimize(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void fullscreen(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setFullScreen(true);
    }

    @FXML
    private void unfullscreen(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setFullScreen(false);
    }

    @FXML
    private void close(MouseEvent event) {
        Platform.exit();
    }

    // ================== HELPER METHODS ==================
    private void clearFieldsAndLabels() {
        bookSearchField.clear();
        studentSearchTextField.clear();
        bookName.setText("Book Title");
        bookAuthor.setText("Book Author");
        bookPublisher.setText("Book Publisher");
        availability.setText("Availability");
        studentName.setText("Student Name");
        studentEmail.setText("Email Address");
        contact.setText("Contact");
        boName = "";
        stuName = "";
    }

    // ================== SEARCH BOOK ==================
    @FXML
    private void searchBook(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String input = bookSearchField.getText().trim();
            if (input.isEmpty()) {
                showAlert("Field validation", "Please enter ISBN / barcode", Alert.AlertType.WARNING);
                return;
            }

            // Simple demo: lookup in CSV
            FileUtil.readFile("books.csv").forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length > 1 && parts[0].equalsIgnoreCase(input)) {
                    boName = parts[1];
                    bookName.setText(parts[1]);
                    bookAuthor.setText(parts[2]);
                    bookPublisher.setText(parts[3]);
                    availability.setText(parts[8]);
                }
            });
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

            // Simple demo: lookup in CSV
            FileUtil.readFile("students.csv").forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length > 1 && parts[0].equalsIgnoreCase(input)) {
                    stuName = parts[1];
                    studentName.setText(parts[1]);
                    studentEmail.setText(parts[3]);
                    contact.setText(parts[2]);
                }
            });
        }
    }

    // ================== ISSUE BOOK ==================
    @FXML
    private void issueBook(ActionEvent event) {
        if (boName.isEmpty() || stuName.isEmpty()) {
            showAlert("Missing Info", "Please search and select both book and student", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Issue");
        confirm.setHeaderText(null);
        confirm.setContentText("Issue \"" + boName + "\" to \"" + stuName + "\"?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Demo: simulate issuing by updating CSV (or your DB in real app)
            showAlert("Success", "Book issued successfully", Alert.AlertType.INFORMATION);
            clearFieldsAndLabels();
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        clearFieldsAndLabels();
    }
    @FXML
    private BorderPane contentPane;


    // ===== Sidebar buttons =====
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
    // ================== UTILITY ==================
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }

}
