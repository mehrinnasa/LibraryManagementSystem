package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import mehrin.loginpage.Model.LoginInfo;
import mehrin.loginpage.Util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;      // masked (default)
    @FXML private TextField     passwordVisible;    // plain text (when shown)
    @FXML private Button        eyeButton;

    private boolean passwordShown = false;

    private static final String CSV_PATH = "data/loginInfo.csv";

    // ================= SHOW / HIDE TOGGLE =================
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordShown = !passwordShown;

        if (passwordShown) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            eyeButton.setText("Hide");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            eyeButton.setText("Show");
        }
    }

    /** Returns password from whichever field is currently active */
    private String getCurrentPassword() {
        return passwordShown
                ? passwordVisible.getText()
                : passwordField.getText();
    }

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

        String    username  = usernameField.getText().trim();
        String    password  = getCurrentPassword();
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

        // ── STUDENT ──────────────────────────────────────────
        if ("student".equalsIgnoreCase(loginInfo.getUserType())) {

            SessionManager.getInstance().setLoggedInStudentId(loginInfo.getUsername());
            SessionManager.getInstance().setRole("student");

            String studentName = resolveStudentName(loginInfo.getUsername());
            SessionManager.getInstance().setLoggedInStudentName(studentName);

            LoadStage loadStage = new LoadStage(
                    "/mehrin/loginpage/StudentDashboard.fxml", node, true);

            StudentDashboardController controller =
                    (StudentDashboardController) loadStage.getController();
            controller.setCurrentStudentId(loginInfo.getUsername());
        }

        // ── ADMIN ─────────────────────────────────────────────
        else if ("admin".equalsIgnoreCase(loginInfo.getUserType())) {
            SessionManager.getInstance().setRole("admin");
            new LoadStage("/mehrin/loginpage/Dashboard.fxml", node, true);
        }

        else {
            new MyAlert(AlertType.ERROR, "Error",
                    "Unknown user type: " + loginInfo.getUserType());
        }
    }

    // ================= RESOLVE STUDENT NAME =================
    private String resolveStudentName(String studentId) {
        for (String line : FileUtil.readFile("students.csv")) {
            String[] p = line.split(",", -1);
            if (p.length > 1 && p[0].trim().equalsIgnoreCase(studentId)) {
                return p[1].trim();
            }
        }
        return studentId;
    }

    // ================= KEYBOARD NAVIGATION =================
    @FXML
    private void onUsernameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (passwordShown) passwordVisible.requestFocus();
            else               passwordField.requestFocus();
            event.consume();
        }
    }

    @FXML
    private void onPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (validateFields()) {
                performLogin(new ActionEvent((Node) event.getSource(), null));
            }
            event.consume();
        }
    }

    // ================= VALIDATE CREDENTIALS =================
    private LoginInfo validateCredentials(String username, String password) {

        String csvFilePath = findCSVPath();
        if (csvFilePath == null) {
            new MyAlert(AlertType.ERROR, "File Error", "loginInfo.csv not found.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String  line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }

                String[] fields = line.split(",", -1);
                if (fields.length >= 5) {
                    String csvUsername = fields[0].trim();
                    String csvEmail    = fields[1].trim();
                    String csvPassword = fields[2].trim();
                    String userType    = fields[3].trim();
                    String status      = fields[4].trim();

                    if ((csvUsername.equals(username) ||
                            csvEmail.equalsIgnoreCase(username))
                            && csvPassword.equals(password)) {
                        return new LoginInfo(csvUsername, csvEmail,
                                csvPassword, userType, status);
                    }
                }
            }
        } catch (IOException e) {
            new MyAlert(AlertType.ERROR, "File Error",
                    "Error reading loginInfo.csv: " + e.getMessage());
        }

        return null;
    }

    // ================= FIND CSV =================
    private String findCSVPath() {
        String[] paths = {
                CSV_PATH,
                "src/" + CSV_PATH,
                System.getProperty("user.dir") + "/" + CSV_PATH,
                System.getProperty("user.dir") + "/src/" + CSV_PATH
        };
        for (String path : paths) {
            if (Files.exists(Paths.get(path))) return path;
        }
        return null;
    }

    // ================= FORGOT PASSWORD =================
    @FXML
    private void changePass(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/ResetPassword.fxml",
                (Node) event.getSource(), true);
    }

    // ================= VALIDATION =================
    private boolean validateFields() {
        return !usernameField.getText().isEmpty()
                && !getCurrentPassword().isEmpty();
    }
}