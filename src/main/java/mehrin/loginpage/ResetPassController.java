package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ResetPassController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField newPasswordField;

    // RESET PASSWORD
    @FXML
    private void handleReset(ActionEvent event) {
        String username = usernameField.getText();
        String newPassword = newPasswordField.getText();

        if (username.isEmpty() || newPassword.isEmpty()) {
            System.out.println("Please fill all fields!");
            return;
        }

        System.out.println("Password reset for " + username + " to: " + newPassword);
        // TODO: Add database or storage logic here
        System.out.println("Password reset successful!");
    }

    // BACK TO LOGIN
    @FXML
    private void backToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) usernameField.getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
    }
}