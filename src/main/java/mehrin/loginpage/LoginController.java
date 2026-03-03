package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mehrin.loginpage.Model.LoginInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private static final String CSV_PATH = "data/loginInfo.csv";

    // ================= LOGIN =================
    @FXML
    private void handleLogin(ActionEvent event) {
        if (!validateFields()) {
            new MyAlert(AlertType.INFORMATION, "Validation Error",
                    "Please enter username/email and password");
            return;
        }

        performLogin(event);
    }

    // ================= PERFORM LOGIN =================
    private void performLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        LoginInfo loginInfo = validateCredentials(username, password);

        if (loginInfo == null) {
            new MyAlert(AlertType.ERROR, "Login Failed",
                    "Invalid username/email or password");
            return;
        }

        if ("Blocked".equalsIgnoreCase(loginInfo.getStatus())) {
            new MyAlert(AlertType.WARNING, "Account Blocked",
                    "Your account has been blocked. Please contact administrator.");
            return;
        }

        Node node = (Node) event.getSource();

        // ================= STUDENT =================
        if ("student".equalsIgnoreCase(loginInfo.getUserType())) {

            LoadStage loadStage = new LoadStage(
                    "/mehrin/loginpage/StudentDashboard.fxml",
                    node,
                    true
            );

            StudentDashboardController controller =
                    (StudentDashboardController) loadStage.getController();

            controller.setCurrentStudentId(loginInfo.getUsername());
        }

        // ================= ADMIN =================
        else if ("admin".equalsIgnoreCase(loginInfo.getUserType())) {

            new LoadStage(
                    "/mehrin/loginpage/Dashboard.fxml",
                    node,
                    true
            );
        }

        else {
            new MyAlert(AlertType.ERROR,
                    "Error",
                    "Unknown user type: " + loginInfo.getUserType());
        }

    }

    // ================= KEYBOARD NAVIGATION =================
    @FXML
    private void onUsernameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
            event.consume();
        }
    }

    @FXML
    private void onPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (validateFields()) {
                Node source = (Node) event.getSource();
                ActionEvent actionEvent = new ActionEvent(source, null);
                performLogin(actionEvent);
            }
            event.consume();
        }
    }

    // ================= VALIDATE CREDENTIALS =================
    private LoginInfo validateCredentials(String username, String password) {

        String csvFilePath = findCSVPath();

        if (csvFilePath == null) {
            new MyAlert(AlertType.ERROR, "File Error",
                    "loginInfo.csv not found.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",", -1);

                if (fields.length >= 5) {

                    String csvUsername = fields[0].trim();
                    String csvEmail = fields[1].trim();
                    String csvPassword = fields[2].trim();
                    String userType = fields[3].trim();
                    String status = fields[4].trim();

                    boolean usernameMatches = csvUsername.equals(username);
                    boolean emailMatches = csvEmail.equalsIgnoreCase(username);
                    boolean passwordMatches = csvPassword.equals(password);
                    boolean isActive = "Active".equalsIgnoreCase(status);

                    if ((usernameMatches || emailMatches)
                            && passwordMatches
                            && isActive) {

                        return new LoginInfo(
                                csvUsername,
                                csvEmail,
                                csvPassword,
                                userType,
                                status
                        );
                    }
                }
            }

        } catch (IOException e) {
            new MyAlert(AlertType.ERROR,
                    "File Error",
                    "Error reading loginInfo.csv: " + e.getMessage());
        }

        return null;
    }

    // ================= FIND CSV =================
    private String findCSVPath() {

        String[] possiblePaths = {
                CSV_PATH,
                "src/" + CSV_PATH,
                System.getProperty("user.dir") + "/" + CSV_PATH,
                System.getProperty("user.dir") + "/src/" + CSV_PATH
        };

        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }

        return null;
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