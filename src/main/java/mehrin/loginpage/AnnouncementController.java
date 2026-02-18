package mehrin.loginpage;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mehrin.loginpage.Model.Announcement;
import mehrin.loginpage.Service.AnnouncementService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * AnnouncementController
 * Structured similar to BooksController
 */
public class AnnouncementController implements Initializable {

    // ================== FXML FIELDS ==================

    @FXML private TextField titleField;
    @FXML private TextArea announcementArea;
    @FXML private ListView<String> announcementList;

    // ================== VARIABLES ==================

    private AnnouncementService announcementService;
    private ObservableList<String> announcementObservableList;

    // ================== INITIALIZE ==================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        announcementService = new AnnouncementService();
        announcementObservableList = FXCollections.observableArrayList();
        announcementList.setItems(announcementObservableList);

        loadAnnouncements();
    }

    // ================== LOAD ANNOUNCEMENTS ==================

    private void loadAnnouncements() {
        announcementObservableList.clear();

        List<Announcement> announcements = announcementService.getAllAnnouncements();

        for (Announcement a : announcements) {
            announcementObservableList.add(
                    "Title: " + a.getTitle() + "\n" +
                            "Message: " + a.getMessage()
            );
        }
    }

    // ================== POST ==================

    @FXML
    private void handlePost() {

        String title = titleField.getText().trim();
        String message = announcementArea.getText().trim();

        if (title.isEmpty() || message.isEmpty()) {
            showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Title and Message cannot be empty!");
            return;
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setMessage(message);

        announcementService.addAnnouncement(announcement);

        loadAnnouncements();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION,
                "Success",
                "Announcement posted successfully!");
    }

    // ================== CLEAR ==================

    @FXML
    private void handleClear() {
        clearFields();
    }

    private void clearFields() {
        titleField.clear();
        announcementArea.clear();
    }

    // ================== NAVIGATION ==================

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
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml", node,true);
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
