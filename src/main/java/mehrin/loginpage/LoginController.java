package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;


import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // ===== TEMPORARY HARDCODED DATA (FOR FUTURE DB) =====
    /*
    private final String DB_USERNAME = "ruet_student";
    private final String DB_PASSWORD = "ruet123";
    */

    // LOGIN BUTTON
    @FXML
    private void handleLogin(ActionEvent event) throws IOException {

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Empty field check (this stays)
        if (username.isEmpty() || password.isEmpty()) {
            new MyAlert(
                    AlertType.INFORMATION,
                    "Validation Error",
                    "Please enter all the required fields"
            );

            return;
        }

        // ===== INCORRECT USERNAME / PASSWORD CHECK =====
        // (COMMENTED FOR NOW – WILL USE DATABASE LATER)
        /*
        if (!username.equals(DB_USERNAME) || !password.equals(DB_PASSWORD)) {
            showAlert(
                    MyAlert.AlertType.ERROR,
                    "Login Failed",
                    "Username or Password incorrect"
            );
            return;
        }
        */

        // ---- TEMP: DIRECT LOGIN SUCCESS ----
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) usernameField.getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Library Main Menu");
        stage.show();
    }

    // FORGOT PASSWORD
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

    // ALERT METHOD (Reusable)

}
