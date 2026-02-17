package mehrin.loginpage;

import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentsController implements Initializable {

    // ================= TABLE =================
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> phoneColumn;
    @FXML private TableColumn<Student, String> emailColumn;

    // ================= FORM =================
    @FXML private TextField searchField;
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;

    private final ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private StudentService studentService;

    // ================= INITIALIZE =================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        studentService = new StudentService();

        studentIdColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentId()));

        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));

        phoneColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));

        emailColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        loadStudents();
        setupSearch();
        setupTableClick();
    }

    // ================= LOAD =================
    private void loadStudents() {
        studentsList.setAll(studentService.getAllStudents());
        studentsTable.setItems(studentsList);
    }

    // ================= SEARCH =================
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null || newVal.isEmpty()) {
                studentsTable.setItems(studentsList);
                return;
            }

            ObservableList<Student> filtered = FXCollections.observableArrayList();
            String q = newVal.toLowerCase();

            for (Student s : studentsList) {
                if (s.getName().toLowerCase().contains(q) ||
                        s.getStudentId().toLowerCase().contains(q) ||
                        s.getEmail().toLowerCase().contains(q)) {
                    filtered.add(s);
                }
            }

            studentsTable.setItems(filtered);
        });
    }

    // ================= TABLE CLICK =================
    private void setupTableClick() {
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, student) -> {
            if (student != null) {
                studentIdField.setText(student.getStudentId());
                studentNameField.setText(student.getName());
                emailField.setText(student.getEmail());
                phoneField.setText(student.getPhone());
            }
        });
    }

    // ================= SAVE =================
    @FXML
    private void handleSave() {

        String id = studentIdField.getText().trim();
        String name = studentNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Please fill all required fields.");
            return;
        }

        Student selected = studentsTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // UPDATE
            selected.setStudentId(id);
            selected.setName(name);
            selected.setEmail(email);
            selected.setPhone(phone);
            studentService.updateStudent(selected);
        } else {
            // ADD
            Student student = new Student(id, name, phone, email);
            studentService.addStudent(student);
        }

        clearForm();
        loadStudents();
    }

    // ================= DELETE =================
    @FXML
    private void handleDelete() {

        Student selected = studentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Student");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this student?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            studentService.deleteStudent(selected.getStudentId());
            loadStudents();
            clearForm();
        }
    }

    // ================= CANCEL =================
    @FXML
    private void handleCancel() {
        clearForm();
        studentsTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        studentIdField.clear();
        studentNameField.clear();
        emailField.clear();
        phoneField.clear();
    }

    // ================= NAVIGATION (Same as BooksController) =================

    @FXML
    private void handleHome(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", node,true); }

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
    private void handleReturnBook(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/ReturnBooks.fxml", node,true);
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
    private void handleLogout(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node,true);
    }
    // ================= ALERT =================
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
