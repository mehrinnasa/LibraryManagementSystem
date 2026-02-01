package mehrin.loginpage;

//import com.jfoenix.controls.JFXButton;
//import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // ======= Logout button =======
    @FXML
    private void logout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }

    // ======= Navigation buttons =======
    @FXML
    private void loadHomePanel(ActionEvent event) {
        System.out.println("Home panel loaded");
    }

    @FXML
    private void loadBooksPanel(ActionEvent event) {
        System.out.println("Books panel loaded");
    }

    @FXML
    private void loadStudentPanel(ActionEvent event) {
        System.out.println("Students panel loaded");
    }

    @FXML
    private void loadIssueBooksPanel(ActionEvent event) {
        System.out.println("Issue Books panel loaded");
    }

    @FXML
    private void loadReturnBooksPanel(ActionEvent event) {
        System.out.println("Return Books panel loaded");
    }

    @FXML
    private void viewAllIssuedBooks(ActionEvent event) {
        System.out.println("All issued books panel loaded");
    }

    @FXML
    private void loadSendAnnouncementsPanel(ActionEvent event) {
        System.out.println("Announcements panel loaded");
    }

    @FXML
    private void loadExportDataPanel(ActionEvent event) {
        System.out.println("Export panel loaded");
    }

    @FXML
    private void loadClearancePanel(ActionEvent event) {
        System.out.println("Clearance panel loaded");
    }

    @FXML
    private void loadSettingsPanel(ActionEvent event) {
        System.out.println("Settings panel loaded");
    }


    // ======= Stage Dragging =======
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void stagePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void stageDragged(MouseEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }
}
