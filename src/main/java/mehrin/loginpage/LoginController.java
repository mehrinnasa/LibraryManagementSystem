package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import mehrin.loginpage.Model.LoginInfo;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // ================= LOGIN =================
    @FXML
    private void handleLogin(ActionEvent event) {
        if (!validateFields()) {
            new MyAlert(AlertType.INFORMATION, "Validation Error", "Please enter username/email and password");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate credentials from CSV
        LoginInfo loginInfo = validateCredentials(username, password);

        if (loginInfo == null) {
            new MyAlert(AlertType.ERROR, "Login Failed", "Invalid username/email or password");
            return;
        }

        if ("Blocked".equalsIgnoreCase(loginInfo.getStatus())) {
            new MyAlert(AlertType.WARNING, "Account Blocked", "Your account has been blocked. Please contact administrator.");
            return;
        }

        // Login successful - Route based on UserType
        Node node = (Node) event.getSource();

        if ("student".equalsIgnoreCase(loginInfo.getUserType())) {
            new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", node, true);
        } else if ("admin".equalsIgnoreCase(loginInfo.getUserType())) {
            new LoadStage("/mehrin/loginpage/Dashboard.fxml", node, true);
        } else {
            new MyAlert(AlertType.ERROR, "Error", "Unknown user type");
        }
    }

    // ================= VALIDATE CREDENTIALS FROM CSV =================
    private LoginInfo validateCredentials(String username, String password) {
        String csvFilePath = "data/loginInfo.csv"; // Adjust path if needed

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Parse CSV line
                String[] fields = line.split(",");

                if (fields.length >= 5) {
                    String csvUsername = fields[0].trim();
                    String csvEmail = fields[1].trim();
                    String csvPassword = fields[2].trim();
                    String userType = fields[3].trim();
                    String status = fields[4].trim();

                    // Check if login is with username OR email
                    boolean usernameMatches = csvUsername.equals(username);
                    boolean emailMatches = csvEmail.equalsIgnoreCase(username);
                    boolean passwordMatches = csvPassword.equals(password);

                    if ((usernameMatches || emailMatches) && passwordMatches) {
                        return new LoginInfo(csvUsername, csvEmail, csvPassword, userType, status);
                    }
                }
            }
        } catch (java.io.IOException e) {
            new MyAlert(AlertType.ERROR, "File Error", "Could not read loginInfo.csv: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Credentials not found or invalid
    }

    // ================= FORGOT PASSWORD =================
    @FXML
    private void changePass(ActionEvent event) {
        Node node = (Node) event.getSource();
        new LoadStage("/mehrin/loginpage/ResetPassword.fxml", node, true);
    }

    // ================= VALIDATION =================
    private boolean validateFields() {
        return !usernameField.getText().isEmpty()
                && !passwordField.getText().isEmpty();
    }
}