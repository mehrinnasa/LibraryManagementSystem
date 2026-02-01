package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // ================= LOGIN =================
    @FXML
    private void handleLogin(ActionEvent event) {
        if (!validateFields()) {
            new MyAlert(AlertType.INFORMATION, "Validation Error", "Please enter username and password");
            return;
        }

        // For now, direct success (DB logic can be added later)
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/Main.fxml", node,true); // full screen
    }

    // ================= FORGOT PASSWORD =================
    @FXML
    private void changePass(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/ResetPassword.fxml", node,false); // small window
    }

    // ================= VALIDATION =================
    private boolean validateFields() {
        return !usernameField.getText().isEmpty()
                && !passwordField.getText().isEmpty();
    }
}
