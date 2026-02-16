package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mehrin.loginpage.Util.FileUtil;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label issuedBooksLabel;

    @FXML
    private Label totalStudentsLabel;

    @FXML
    private VBox chartContainer;
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
        new LoadStage("/mehrin/loginpage/IssuedBooks.fxml", node,true);
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
    private void logout() {
        System.out.println("Logout clicked");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStats();
        setupBookStatusChart();
    }

    // ================== STAT VALUES ==================
    private void setupStats() {
        int totalBooks = FileUtil.getTotalBooks();
        int issuedBooks = FileUtil.getIssuedBooks();
        int totalStudents = FileUtil.getTotalStudents();

        totalBooksLabel.setText(String.valueOf(totalBooks));
        issuedBooksLabel.setText(String.valueOf(issuedBooks));
        totalStudentsLabel.setText(String.valueOf(totalStudents));
    }


    // ================== PIE CHART ==================
    private void setupBookStatusChart() {

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

}
