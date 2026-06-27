package mehrin.loginpage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import mehrin.loginpage.Util.FileUtil;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable{
    @FXML private Label totalBooksLabel;
    @FXML private Label issuedBooksLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private VBox chartContainer;
    //@FXML private BorderPane contentPane;
    @FXML private void loadHomePanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Dashboard.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadBooksPanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Books.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadStudentPanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Students.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadIssueBooksPanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/IssueBooks.fxml",(Node)event.getSource(),true);
    }
    @FXML private void viewAllIssuedBooks(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadAnnouncementsPanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Announcements.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadExportPanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Export.fxml",(Node)event.getSource(),true);
    }
    @FXML private void loadClearancePanel(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Clearance.fxml",(Node)event.getSource(),true);
    }
    @FXML private void logout(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/Login.fxml",(Node)event.getSource(),true);
    }
    private void setHeader() {
        int totalBooks=FileUtil.getTotalBooks();
        int issuedBooks= FileUtil.getIssuedBooks();
        int totalStudents=FileUtil.getTotalStudents();
        totalBooksLabel.setText(String.valueOf(totalBooks));// label text string value bosate int value k str a convert
        issuedBooksLabel.setText(String.valueOf(issuedBooks));
        totalStudentsLabel.setText(String.valueOf(totalStudents));
    }
    private void setPieChart() {
        Map<String,Integer>status =FileUtil.BookStatusCount();
        PieChart pie =new PieChart();
        pie.getData().addAll(
                new PieChart.Data("Available",status.get("Available")),
                new PieChart.Data("Issued",status.get("Issued")));
        pie.setPrefSize(460, 460);
        pie.setMinSize(460, 460);
        pie.setMaxSize(460, 460);
        pie.setLabelsVisible(true);
        pie.setTitle(null);
        chartContainer.getChildren().clear();
        chartContainer.setAlignment(Pos.CENTER);
        chartContainer.getChildren().add(pie);
    }
    @Override
    public void initialize(URL location,ResourceBundle resources) {
        FileUtil.syncBooks();
        setHeader();
        setPieChart();
    }

}
