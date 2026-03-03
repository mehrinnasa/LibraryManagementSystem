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
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Service.StudentService;
import mehrin.loginpage.Util.FileUtil;
import java.util.Map;
import java.net.URL;
import java.util.ResourceBundle;
import mehrin.loginpage.Service.CartService;

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

    private String currentStudentId;
    private StudentService studentService;

    // Static variable to store student ID between page loads
    private static String staticStudentId;

    // ================= INITIALIZE =================
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        CartService.cleanExpiredCarts();
        System.out.println("StudentDashboardController initialized");
        studentService = new StudentService();

        // If student ID was set via static variable, use it
        if (staticStudentId != null && !staticStudentId.isEmpty()) {
            System.out.println("Loading student ID from static: " + staticStudentId);
            setCurrentStudentId(staticStudentId);
        }

        setupStats();
        setupPieChart();
        FileUtil.syncBooksWithIssuedBooks();
    }

    // ================= SET CURRENT STUDENT ID =================
    public void setCurrentStudentId(String studentId) {
        System.out.println("setCurrentStudentId called with: " + studentId);
        this.currentStudentId = studentId;
        staticStudentId = studentId; // Store in static variable

        // Call loadStudentInfo after student ID is set
        if (studentService == null) {
            studentService = new StudentService();
        }
        loadStudentInfo();
    }

    // ================= GET CURRENT STUDENT ID =================
    public String getCurrentStudentId() {
        return currentStudentId;
    }

    // ================= LOAD STUDENT INFO =================
    private void loadStudentInfo() {
        System.out.println("loadStudentInfo called, currentStudentId: " + currentStudentId);

        if (currentStudentId == null || currentStudentId.isEmpty()) {
            nameLabel.setText("N/A");
            rollLabel.setText("N/A");
            regLabel.setText("N/A");
            sessionLabel.setText("N/A");
            yearLabel.setText("N/A");
            semesterLabel.setText("N/A");
            contactLabel.setText("N/A");
            return;
        }

        Student student = studentService.getStudentById(currentStudentId);

        if (student != null) {
            nameLabel.setText(student.getName());
            rollLabel.setText(student.getStudentId());
            regLabel.setText(student.getRegistration());
            sessionLabel.setText(student.getSession());
            yearLabel.setText(student.getYear());
            semesterLabel.setText(student.getSemester());
            contactLabel.setText(student.getPhone());
        } else {
            nameLabel.setText("Student not found");
            rollLabel.setText("N/A");
            regLabel.setText("N/A");
            sessionLabel.setText("N/A");
            yearLabel.setText("N/A");
            semesterLabel.setText("N/A");
            contactLabel.setText("N/A");
        }
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
        staticStudentId = null; // Clear student ID on logout
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node, true);
    }
}