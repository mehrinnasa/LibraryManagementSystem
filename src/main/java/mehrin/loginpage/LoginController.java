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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // LOGIN BUTTON → Opens Main.fxml
    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Please fill all fields");
            return;
        }

        System.out.println("Login clicked! Username: " + username);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) usernameField.getScene().getWindow(); // current stage
        double width = stage.getWidth();
        double height = stage.getHeight();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.show();
    }

    // FORGOT PASSWORD → Opens ResetPassword.fxml
    @FXML
    private void changePass(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ResetPassword.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) usernameField.getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Reset Password");
        stage.show();
    }
}
