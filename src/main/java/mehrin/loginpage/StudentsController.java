package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Service.StudentService;

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
    
    // form fields
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField registrationField;
    @FXML private TextField sessionField;
    @FXML private TextField yearField;
    @FXML private TextField semesterField;

    private ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private StudentService stdService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        stdService = new StudentService();
        
        // table cols setup
        studentIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentId()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        registrationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegistration()));
        sessionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSession()));
        yearColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getYear()));
        semesterColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemester()));

        refreshTable();

        // search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                studentsTable.setItems(studentsList);
                return;
            }

            String searchTxt = newValue.toLowerCase();
            ObservableList<Student> filtered = FXCollections.observableArrayList();

            for (int i = 0; i < studentsList.size(); i++) {
                Student s = studentsList.get(i);
                if (s.getName().toLowerCase().contains(searchTxt) ||
                    s.getStudentId().toLowerCase().contains(searchTxt) ||
                    s.getEmail().toLowerCase().contains(searchTxt)) {
                    filtered.add(s);
                }
            }

            studentsTable.setItems(filtered);
        });

        // when clicking on a row, fill the form fields
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                studentIdField.setText(newSelection.getStudentId());
                studentNameField.setText(newSelection.getName());
                emailField.setText(newSelection.getEmail());
                phoneField.setText(newSelection.getPhone());
                registrationField.setText(newSelection.getRegistration());
                sessionField.setText(newSelection.getSession());
                yearField.setText(newSelection.getYear());
                semesterField.setText(newSelection.getSemester());
            }
        });
    }

    private void refreshTable() {
        // fetching from service
        studentsList.setAll(stdService.getAllStudents());
        studentsTable.setItems(studentsList);
    }

    @FXML
    private void handleSave() {
        // validate inputs
        boolean empty = studentIdField.getText().trim().isEmpty() ||
                        studentNameField.getText().trim().isEmpty() ||
                        emailField.getText().trim().isEmpty() ||
                        phoneField.getText().trim().isEmpty() ||
                        registrationField.getText().trim().isEmpty() ||
                        sessionField.getText().trim().isEmpty() ||
                        yearField.getText().trim().isEmpty() ||
                        semesterField.getText().trim().isEmpty();

        if (empty) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setTitle("Validation Error");
            alert.setContentText("Please fill all required fields before saving.");
            alert.showAndWait();
            return;
        }

        Student currentStud = studentsTable.getSelectionModel().getSelectedItem();

        if (currentStud != null) {
            // update existing record
            currentStud.setStudentId(studentIdField.getText().trim());
            currentStud.setName(studentNameField.getText().trim());
            currentStud.setEmail(emailField.getText().trim());
            currentStud.setPhone(phoneField.getText().trim());
            currentStud.setRegistration(registrationField.getText().trim());
            currentStud.setSession(sessionField.getText().trim());
            currentStud.setYear(yearField.getText().trim());
            currentStud.setSemester(semesterField.getText().trim());
            
            stdService.updateStudent(currentStud);
        } else {
            // it's a new student
            Student s = new Student(
                studentIdField.getText().trim(),
                studentNameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                registrationField.getText().trim(),
                sessionField.getText().trim(),
                yearField.getText().trim(),
                semesterField.getText().trim()
            );
            stdService.addStudent(s);
        }

        clearFields();
        refreshTable();
        
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Success");
        success.setHeaderText(null);
        success.setContentText("Student data saved successfully!");
        success.showAndWait();
    }

    @FXML
    private void handleDelete() {
        Student toDelete = studentsTable.getSelectionModel().getSelectedItem();

        if (toDelete == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Please select a student to delete first.");
            a.showAndWait();
            return;
        }

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Delete Student");
        conf.setHeaderText(null);
        conf.setContentText("Are you sure you want to delete this student?");

        Optional<ButtonType> res = conf.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            stdService.deleteStudent(toDelete.getStudentId());
            refreshTable();
            clearFields();
            
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText("Student deleted successfully!");
            a.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        clearFields();
        studentsTable.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        studentIdField.setText("");
        studentNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        registrationField.setText("");
        sessionField.setText("");
        yearField.setText("");
        semesterField.setText("");
    }

    // --- Side Menu Navigation ---
    private void switchScreen(ActionEvent event, String path) {
        Node n = (Node) event.getSource();
        new LoadStage(path, n, true);
    }

    @FXML private void handleHome(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void handleBooks(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void handleStudents(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void handleIssueBook(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void handleReturnBook(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAllIssuedBooks(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAnnouncement(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void handleExport(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void handleClearance(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void handleLogout(ActionEvent evt) { switchScreen(evt, "/mehrin/loginpage/Login.fxml"); }
}