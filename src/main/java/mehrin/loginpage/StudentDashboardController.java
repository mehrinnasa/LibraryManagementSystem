package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Service.StudentService;
import mehrin.loginpage.Util.FileUtil;
import java.util.Map;
import java.net.URL;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label rollLabel;
    @FXML private Label regLabel;
    @FXML private Label sessionLabel;
    @FXML private Label yearLabel;
    @FXML private Label semesterLabel;
    @FXML private Label contactLabel;
    @FXML private Label totalBooksLabel;
    @FXML private Label issuedBooksLabel;
    @FXML private VBox chartContainer;
    private String StudentId;
    private StudentService studentService;
    private static String staticStudentId;
    @FXML private void loadHomePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", node, true);
    }

    @FXML private void loadBooksPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentBooks.fxml", node, true);
    }

    @FXML private void loadAddToCartPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAddToCart.fxml", node, true);
    }

    @FXML private void loadAllIssuedBooks(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml", node, true);
    }

    @FXML private void AnnouncementPanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml", node, true);
    }

    @FXML private void loadClearancePanel(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/StudentClearance.fxml", node, true);
    }

    @FXML private void logout(ActionEvent event) {
        staticStudentId = null; // Clear student ID on logout
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Login.fxml", node, true);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentService=new StudentService();
        if (staticStudentId!=null && !staticStudentId.isEmpty()) {
            setStudentId(staticStudentId);
        }
        setHeader();
        setPieChart();
        FileUtil.syncBooks();
    }

    public void setStudentId(String studentId) {
        this.StudentId=studentId;
        staticStudentId=studentId; //static variable এ store করলে dynamically use করা যাবে reload করলেও
        if (studentService==null) {
            studentService=new StudentService();
        }
        loadStudentInfo();
    }
    private void loadStudentInfo() {
        if (StudentId==null || StudentId.isEmpty()) {
            nameLabel.setText("N/A");
            rollLabel.setText("N/A");
            regLabel.setText("N/A");
            sessionLabel.setText("N/A");
            yearLabel.setText("N/A");
            semesterLabel.setText("N/A");
            contactLabel.setText("N/A");
            return;
        }
        Student student = studentService.getStudentById(StudentId);
        if (student != null) {
            nameLabel.setText(student.getName());
            rollLabel.setText(student.getStudentId());
            regLabel.setText(student.getRegistration());
            sessionLabel.setText(student.getSession());
            yearLabel.setText(student.getYear());
            semesterLabel.setText(student.getSemester());
            contactLabel.setText(student.getPhone());
        } else {
            nameLabel.setText("Not found");
            rollLabel.setText("Not found");
            regLabel.setText("Not found");
            sessionLabel.setText("Not found");
            yearLabel.setText("Not found");
            semesterLabel.setText("Not found");
            contactLabel.setText("Not found");
        }
    }
    private void setHeader() {
        int totalBooks = FileUtil.getTotalBooks();
        int issuedBooks = FileUtil.getIssuedBooks();
        totalBooksLabel.setText(String.valueOf(totalBooks));
        issuedBooksLabel.setText(String.valueOf(issuedBooks));
    }
    private void setPieChart() {
        Map<String, Integer> status = FileUtil.BookStatusCount();
        PieChart pie = new PieChart();
        pie.getData().addAll(
                new PieChart.Data("Available", status.get("Available")),
                new PieChart.Data("Issued", status.get("Issued"))
        );
        pie.setPrefSize(460, 460);
        pie.setMinSize(460, 460);
        pie.setMaxSize(460, 460);
        pie.setLabelsVisible(true);
        pie.setTitle(null);
        chartContainer.getChildren().clear();
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.getChildren().add(pie);
    }
}