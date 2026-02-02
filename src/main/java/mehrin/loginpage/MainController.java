package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane contentPane;

    // Load Dashboard on app start
    @FXML
    public void initialize(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Dashboard.fxml", node,true);
    }


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
}
