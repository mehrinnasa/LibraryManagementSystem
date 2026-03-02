package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mehrin.loginpage.Util.FileUtil;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    // ================= STUDENT INFO LABELS =================

    @FXML private Label nameLabel;
    @FXML private Label rollLabel;
    @FXML private Label regLabel;
    @FXML private Label sessionLabel;
    @FXML private Label yearLabel;
    @FXML private Label semesterLabel;
    @FXML private Label contactLabel;

    // ================= DASHBOARD STATS =================

    @FXML private Label totalBooksLabel;
    @FXML private Label issuedBooksLabel;

    @FXML private VBox chartContainer;

    // ================= INITIALIZE =================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadStudentInfo();
        setupStats();
        setupPieChart();
        FileUtil.syncBooksWithIssuedBooks();
        setupStats();
    }

    // ================= LOAD STUDENT INFO =================

    private void loadStudentInfo() {

        // Replace these with real logged-in student data later
        nameLabel.setText("MAISUM MALIHA");
        rollLabel.setText("2303007");
        regLabel.setText("549");
        sessionLabel.setText("2023-2024");
        yearLabel.setText("2nd Year");
        semesterLabel.setText("Odd");
        contactLabel.setText("1516725678");
    }

    // ================= LOAD STATS =================

    private void setupStats() {
        int totalBooks = FileUtil.getTotalBooks();
        int issuedBooks = FileUtil.getIssuedBooksFromIssueFile();
        totalBooksLabel.setText(String.valueOf(totalBooks));
        issuedBooksLabel.setText(String.valueOf(issuedBooks));
    }
    // ================= PIE CHART =================

    private void setupPieChart() {

        Map<String, Integer> statusCount = FileUtil.getBookStatusCount();

        PieChart pieChart = new PieChart();

        pieChart.getData().addAll(
                new PieChart.Data("Available", statusCount.get("Available")),
                new PieChart.Data("Issued", statusCount.get("Issued"))
        );

        // ⭐ chart size control
        pieChart.setPrefSize(460, 460);
        pieChart.setMinSize(460, 460);
        pieChart.setMaxSize(460, 460);

        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setTitle(null);

        VBox.setVgrow(pieChart, Priority.NEVER);

        chartContainer.getChildren().clear();
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.getChildren().add(pieChart);
    }

    // ================= SIDEBAR NAVIGATION =================

    @FXML
    private void loadHomePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", node, true);
    }

    @FXML
    private void loadBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentBooks.fxml", node, true);
    }

    @FXML
    private void loadAddToCartBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAddToCartBooks.fxml", node, true);
    }

    @FXML
    private void loadAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", node, true);
    }

    @FXML
    private void loadAnnouncementPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml", node, true);
    }
    @FXML
    private void loadClearancePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentClearance.fxml", node, true);
    }
    @FXML
    private void logout(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node, true);
    }
}