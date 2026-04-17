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
import javafx.beans.property.SimpleStringProperty;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentsController implements Initializable {

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> phoneColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> registrationColumn;
    @FXML private TableColumn<Student, String> sessionColumn;
    @FXML private TableColumn<Student, String> yearColumn;
    @FXML private TableColumn<Student, String> semesterColumn;

    @FXML private TextField searchField;
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField registrationField;
    @FXML private TextField sessionField;
    @FXML private TextField yearField;
    @FXML private TextField semesterField;

    private final ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private StudentService studentService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        studentService = new StudentService();
        setupTableColumns();
        loadStudents();
        setupSearch();
        setupTableClick();
    }

    private void setupTableColumns() {
        studentIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        registrationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegistration()));
        sessionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSession()));
        yearColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getYear()));
        semesterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester()));
    }

    private void loadStudents() {
        studentsList.setAll(studentService.getAllStudents());
        studentsTable.setItems(studentsList);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                studentsTable.setItems(studentsList);
                return;
            }

            String query = newVal.toLowerCase();
            ObservableList<Student> filteredStudents = FXCollections.observableArrayList();

            for (Student student : studentsList) {
                if (student.getName().toLowerCase().contains(query) ||
                    student.getStudentId().toLowerCase().contains(query) ||
                    student.getEmail().toLowerCase().contains(query)) {
                    filteredStudents.add(student);
                }
            }

            studentsTable.setItems(filteredStudents);
        });
    }

    private void setupTableClick() {
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, student) -> {
            if (student != null) {
                studentIdField.setText(student.getStudentId());
                studentNameField.setText(student.getName());
                emailField.setText(student.getEmail());
                phoneField.setText(student.getPhone());
                registrationField.setText(student.getRegistration());
                sessionField.setText(student.getSession());
                yearField.setText(student.getYear());
                semesterField.setText(student.getSemester());
            }
        });
    }

    @FXML
    private void handleSave() {
        if (areAnyFieldsEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all required fields.");
            return;
        }

        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            updateExistingStudent(selectedStudent);
        } else {
            addNewStudent();
        }

        clearForm();
        loadStudents();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Student saved successfully!");
    }

    private boolean areAnyFieldsEmpty() {
        return studentIdField.getText().trim().isEmpty() ||
               studentNameField.getText().trim().isEmpty() ||
               emailField.getText().trim().isEmpty() ||
               phoneField.getText().trim().isEmpty() ||
               registrationField.getText().trim().isEmpty() ||
               sessionField.getText().trim().isEmpty() ||
               yearField.getText().trim().isEmpty() ||
               semesterField.getText().trim().isEmpty();
    }

    private void updateExistingStudent(Student student) {
        student.setStudentId(studentIdField.getText().trim());
        student.setName(studentNameField.getText().trim());
        student.setEmail(emailField.getText().trim());
        student.setPhone(phoneField.getText().trim());
        student.setRegistration(registrationField.getText().trim());
        student.setSession(sessionField.getText().trim());
        student.setYear(yearField.getText().trim());
        student.setSemester(semesterField.getText().trim());
        studentService.updateStudent(student);
    }

    private void addNewStudent() {
        Student newStudent = new Student(
            studentIdField.getText().trim(),
            studentNameField.getText().trim(),
            phoneField.getText().trim(),
            emailField.getText().trim(),
            registrationField.getText().trim(),
            sessionField.getText().trim(),
            yearField.getText().trim(),
            semesterField.getText().trim()
        );
        studentService.addStudent(newStudent);
    }

    @FXML
    private void handleDelete() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a student to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Student");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this student?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            studentService.deleteStudent(selectedStudent.getStudentId());
            loadStudents();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully!");
        }
    }

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
        registrationField.clear();
        sessionField.clear();
        yearField.clear();
        semesterField.clear();
    }

    // Navigation handlers
    private void loadPage(ActionEvent event, String fxmlPath) {
        Node node = (Node) event.getSource();
        new LoadStage(fxmlPath, node, true);
    }

    @FXML private void handleHome(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void handleBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void handleStudents(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void handleIssueBook(ActionEvent event) { loadPage(event, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void handleReturnBook(ActionEvent event) { loadPage(event, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAllIssuedBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAnnouncement(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void handleExport(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void handleClearance(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void handleLogout(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Login.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}