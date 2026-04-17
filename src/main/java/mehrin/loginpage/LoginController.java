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

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibility;
    @FXML private Button show;

    private boolean isPasswordVisible = false;
    private static final String LOGIN_DATA_FILE = "data/loginInfo.csv";
    private static final String STUDENTS_DATA_FILE = "data/students.csv";

    @FXML
    private void showOrHide(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordVisibility.setText(passwordField.getText());
            passwordVisibility.setVisible(true);
            passwordVisibility.setManaged(true);
            
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            show.setText("Hide");
        } else {
            passwordField.setText(passwordVisibility.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            
            passwordVisibility.setVisible(false);
            passwordVisibility.setManaged(false);
            show.setText("Show");
        }
    }

    private String getEnteredPassword() {
        return isPasswordVisible ? passwordVisibility.getText() : passwordField.getText();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (!areFieldsValid()) {
            new MyAlert(AlertType.INFORMATION, "Validation Error", "Please enter both username and password.");
            return;
        }
        performLogin(event);
    }

    private void performLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = getEnteredPassword();
        
        LoginInfo userAccount = findValidAccount(username, password);

        if (userAccount == null) {
            new MyAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
            return;
        }

        Node sourceNode = (Node) event.getSource();
        String userRole = userAccount.getUserType().toLowerCase();

        if (userRole.equals("student")) {
            setupStudentSessionAndNavigate(userAccount, sourceNode);
        } else if (userRole.equals("admin")) {
            new LoadStage("/mehrin/loginpage/Dashboard.fxml", sourceNode, true);
        } else {
            new MyAlert(AlertType.ERROR, "Error", "Unknown user type: " + userRole);
        }
    }

    private void setupStudentSessionAndNavigate(LoginInfo account, Node sourceNode) {
        String studentId = account.getUsername();
        SessionManager.getInstance().setLoggedInStudentId(studentId);
        
        String fullName = fetchStudentName(studentId);
        SessionManager.getInstance().setLoggedInStudentName(fullName);
        
        LoadStage loadStage = new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", sourceNode, true);
        StudentDashboardController controller = (StudentDashboardController) loadStage.getController();
        controller.setStudentId(studentId);
    }

    private String fetchStudentName(String studentId) {
        for (String line : FileUtil.readFile(STUDENTS_DATA_FILE)) {
            String[] data = line.split(",", -1);
            if (data.length > 1 && data[0].trim().equalsIgnoreCase(studentId)) {
                return data[1].trim();
            }
        }
        return studentId;
    }

    @FXML
    private void onUsernameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (isPasswordVisible) {
                passwordVisibility.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            event.consume();
        }
    }

    @FXML
    private void onPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (areFieldsValid()) {
                performLogin(new ActionEvent((Node) event.getSource(), null));
            }
            event.consume();
        }
    }

    private LoginInfo findValidAccount(String username, String password) {
        for (String line : FileUtil.readFile(LOGIN_DATA_FILE)) {
            String[] fields = line.split(",", -1);
            if (fields.length < 5) continue;
            
            boolean matchesUsername = fields[0].trim().equals(username) || fields[1].trim().equalsIgnoreCase(username);
            boolean matchesPassword = fields[2].trim().equals(password) || fields[3].trim().equalsIgnoreCase(password);
            
            if (matchesUsername && matchesPassword) {
                return new LoginInfo(fields[0].trim(), fields[1].trim(), fields[2].trim(), fields[3].trim(), fields[4].trim());
            }
        }
        return null;
    }

    @FXML
    private void changePass(ActionEvent event) {
        new LoadStage("/mehrin/loginpage/ResetPassword.fxml", (Node) event.getSource(), true);
    }

    private boolean areFieldsValid() {
        return !usernameField.getText().isEmpty() && !getEnteredPassword().isEmpty();
    }
}