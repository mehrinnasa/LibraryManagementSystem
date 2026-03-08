package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import mehrin.loginpage.Model.Announcement;
import mehrin.loginpage.Service.AnnouncementService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AnnouncementController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea  announcementArea;
    @FXML private ListView<Announcement> announcementList;

    // ── Date/time info labels shown below the form ─────────────
    @FXML private Label postedLabel;
    @FXML private Label updatedLabel;

    private AnnouncementService            announcementService;
    private ObservableList<Announcement>   announcementObservableList;
    private Announcement                   selectedAnnouncement;

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        announcementService          = new AnnouncementService();
        announcementObservableList   = FXCollections.observableArrayList();
        announcementList.setItems(announcementObservableList);

        // ── Custom cell: show title + posted date ──────────────
        announcementList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                VBox cell = new VBox(3);
                cell.setPadding(new Insets(6, 8, 6, 8));

                // Title
                Label title = new Label(item.getTitle());
                title.setFont(Font.font("System", FontWeight.BOLD, 13));
                title.setStyle("-fx-text-fill: #0B2A4A;");

                // Posted date
                Label posted = new Label("📅 Posted: " + item.getCreatedDateTime());
                posted.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                cell.getChildren().addAll(title, posted);

                // Updated badge (only if updated)
                if (item.wasUpdated()) {
                    Label upd = new Label("✏ Updated: " + item.getUpdatedDateTime());
                    upd.setStyle("-fx-text-fill: #1a6b3c; -fx-font-size: 11px;");
                    cell.getChildren().add(upd);
                }

                setGraphic(cell);
            }
        });

        // ── Row click → fill form + show dates ────────────────
        announcementList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedAnnouncement = newVal;
                        titleField.setText(newVal.getTitle());
                        announcementArea.setText(newVal.getMessage());
                        showDateInfo(newVal);
                    }
                });

        loadAnnouncements();
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD
    // ─────────────────────────────────────────────────────────────
    private void loadAnnouncements() {
        announcementObservableList.clear();
        List<Announcement> all = announcementService.getAllAnnouncements();
        // Show newest first
        for (int i = all.size() - 1; i >= 0; i--) {
            announcementObservableList.add(all.get(i));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  POST
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handlePost(ActionEvent event) {
        String title   = titleField.getText().trim();
        String message = announcementArea.getText().trim();

        if (title.isEmpty() || message.isEmpty()) {
            showAlert("Validation", "Title and message cannot be empty.");
            return;
        }

        Announcement a = new Announcement();
        a.setTitle(title);
        a.setMessage(message);
        announcementService.addAnnouncement(a);

        loadAnnouncements();
        clearFields();
        showAlert("Success", "Announcement posted successfully.");
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showAlert("Error", "Select an announcement to update.");
            return;
        }
        selectedAnnouncement.setTitle(titleField.getText().trim());
        selectedAnnouncement.setMessage(announcementArea.getText().trim());
        announcementService.updateAnnouncement(selectedAnnouncement);

        loadAnnouncements();
        clearFields();
        showAlert("Updated", "Announcement updated successfully.");
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedAnnouncement == null) {
            showAlert("Error", "Select an announcement to delete.");
            return;
        }
        announcementService.deleteAnnouncement(selectedAnnouncement.getId());
        loadAnnouncements();
        clearFields();
        showAlert("Deleted", "Announcement deleted successfully.");
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleClear(ActionEvent event) { clearFields(); }

    private void clearFields() {
        titleField.clear();
        announcementArea.clear();
        selectedAnnouncement = null;
        announcementList.getSelectionModel().clearSelection();
        hideDateInfo();
    }

    // ─────────────────────────────────────────────────────────────
    //  DATE INFO HELPERS
    // ─────────────────────────────────────────────────────────────
    private void showDateInfo(Announcement a) {
        if (postedLabel != null) {
            postedLabel.setText("📅 Posted: " + a.getCreatedDateTime());
            postedLabel.setVisible(true);
        }
        if (updatedLabel != null) {
            if (a.wasUpdated()) {
                updatedLabel.setText("✏ Last updated: " + a.getUpdatedDateTime());
                updatedLabel.setVisible(true);
            } else {
                updatedLabel.setText("✏ Never updated");
                updatedLabel.setVisible(true);
            }
        }
    }

    private void hideDateInfo() {
        if (postedLabel  != null) { postedLabel.setText(""); }
        if (updatedLabel != null) { updatedLabel.setText(""); }
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)             { new LoadStage("/mehrin/loginpage/Dashboard.fxml",       (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)            { new LoadStage("/mehrin/loginpage/Books.fxml",           (Node)e.getSource(), true); }
    @FXML private void loadStudentPanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/Students.fxml",        (Node)e.getSource(), true); }
    @FXML private void loadIssueBooksPanel(ActionEvent e)       { new LoadStage("/mehrin/loginpage/IssueBooks.fxml",      (Node)e.getSource(), true); }
    @FXML private void viewAllIssuedBooks(ActionEvent e)        { new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",  (Node)e.getSource(), true); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/Announcements.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadExportDataPanel(ActionEvent e)       { new LoadStage("/mehrin/loginpage/Export.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)        { new LoadStage("/mehrin/loginpage/Clearance.fxml",       (Node)e.getSource(), true); }
    @FXML private void handleLogout(ActionEvent e)              { new LoadStage("/mehrin/loginpage/Login.fxml",           (Node)e.getSource(), true); }

    // ─────────────────────────────────────────────────────────────
    //  ALERT
    // ─────────────────────────────────────────────────────────────
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }
}