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

public class StudentAnnouncementController implements Initializable {

    @FXML private ListView<Announcement> announcementList;
    @FXML private Label announcementTitle;
    @FXML private Label announcementMessage;
    @FXML private Label announcementDateTime;

    private AnnouncementService          announcementService;
    private ObservableList<Announcement> announcementObservableList;

    // ─────────────────────────────────────────────────────────────
    //  INITIALIZE
    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        announcementService        = new AnnouncementService();
        announcementObservableList = FXCollections.observableArrayList();
        announcementList.setItems(announcementObservableList);

        // ── Card-style cell (same as admin) ───────────────────
        announcementList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }

                VBox cell = new VBox(3);
                cell.setPadding(new Insets(6, 8, 6, 8));

                Label title = new Label(item.getTitle());
                title.setFont(Font.font("System", FontWeight.BOLD, 13));
                title.setStyle("-fx-text-fill: #0B2A4A;");

                Label posted = new Label("📅 Posted: " + item.getCreatedDateTime());
                posted.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

                cell.getChildren().addAll(title, posted);

                if (item.wasUpdated()) {
                    Label upd = new Label("✏ Updated: " + item.getUpdatedDateTime());
                    upd.setStyle("-fx-text-fill: #1a6b3c; -fx-font-size: 11px;");
                    cell.getChildren().add(upd);
                }

                setGraphic(cell);
            }
        });

        // ── Row click → show full content ─────────────────────
        announcementList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) displayAnnouncement(newVal);
                });

        loadAnnouncements();

        // Auto-select newest (first in list)
        if (!announcementObservableList.isEmpty()) {
            announcementList.getSelectionModel().selectFirst();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD  (newest first)
    // ─────────────────────────────────────────────────────────────
    private void loadAnnouncements() {
        announcementObservableList.clear();
        List<Announcement> all = announcementService.getAllAnnouncements();
        for (int i = all.size() - 1; i >= 0; i--) {
            announcementObservableList.add(all.get(i));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DISPLAY SELECTED ANNOUNCEMENT
    // ─────────────────────────────────────────────────────────────
    private void displayAnnouncement(Announcement a) {
        announcementTitle.setText(a.getTitle());
        announcementMessage.setText(a.getMessage());

        // Build date line
        StringBuilder dateLine = new StringBuilder("📅 Posted: ")
                .append(a.getCreatedDateTime());
        if (a.wasUpdated()) {
            dateLine.append("   ✏ Updated: ").append(a.getUpdatedDateTime());
        }
        announcementDateTime.setText(dateLine.toString());
    }

    // ─────────────────────────────────────────────────────────────
    //  NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void loadHomePanel(ActionEvent e)          { new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadBooksPanel(ActionEvent e)         { new LoadStage("/mehrin/loginpage/StudentBooks.fxml",          (Node)e.getSource(), true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml",      (Node)e.getSource(), true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", (Node)e.getSource(), true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e)  { new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml",   (Node)e.getSource(), true); }
    @FXML private void loadClearancePanel(ActionEvent e)     { new LoadStage("/mehrin/loginpage/StudentClearance.fxml",      (Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e)                 {
        SessionManager.getInstance().clear();
        new LoadStage("/mehrin/loginpage/Login.fxml", (Node)e.getSource(), true);
    }
}