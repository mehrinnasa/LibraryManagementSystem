package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResetPassController {

    // ── Form fields ────────────────────────────────────────────
    @FXML private TextField     usernameField;

    @FXML private PasswordField oldPasswordField;
    @FXML private TextField     oldPasswordVisible;
    @FXML private Button        eyeOld;

    @FXML private PasswordField newPasswordField;
    @FXML private TextField     newPasswordVisible;
    @FXML private Button        eyeNew;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     confirmPasswordVisible;
    @FXML private Button        eyeConfirm;

    private boolean oldShown     = false;
    private boolean newShown     = false;
    private boolean confirmShown = false;

    private static final String CSV_PATH = "data/loginInfo.csv";

    // ─────────────────────────────────────────────────────────────
    //  SHOW / HIDE TOGGLES
    // ─────────────────────────────────────────────────────────────
    @FXML private void toggleOld(ActionEvent e) {
        oldShown = !oldShown;
        toggle(oldShown, oldPasswordField, oldPasswordVisible, eyeOld);
    }

    @FXML private void toggleNew(ActionEvent e) {
        newShown = !newShown;
        toggle(newShown, newPasswordField, newPasswordVisible, eyeNew);
    }

    @FXML private void toggleConfirm(ActionEvent e) {
        confirmShown = !confirmShown;
        toggle(confirmShown, confirmPasswordField, confirmPasswordVisible, eyeConfirm);
    }

    private void toggle(boolean show, PasswordField pf, TextField tf, Button btn) {
        if (show) {
            tf.setText(pf.getText());
            tf.setVisible(true);  tf.setManaged(true);
            pf.setVisible(false); pf.setManaged(false);
            btn.setText("Hide");
        } else {
            pf.setText(tf.getText());
            pf.setVisible(true);  pf.setManaged(true);
            tf.setVisible(false); tf.setManaged(false);
            btn.setText("Show");
        }
    }

    // ── Read current value regardless of which field is active ──
    private String getOldPassword()     { return oldShown     ? oldPasswordVisible.getText()     : oldPasswordField.getText(); }
    private String getNewPassword()     { return newShown     ? newPasswordVisible.getText()     : newPasswordField.getText(); }
    private String getConfirmPassword() { return confirmShown ? confirmPasswordVisible.getText() : confirmPasswordField.getText(); }

    // ─────────────────────────────────────────────────────────────
    //  RESET PASSWORD
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleReset(ActionEvent event) {
        String username        = usernameField.getText().trim();
        String oldPassword     = getOldPassword();
        String newPassword     = getNewPassword();
        String confirmPassword = getConfirmPassword();

        if (username.isEmpty() || oldPassword.isEmpty()
                || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            new MyAlert(AlertType.ERROR, "Validation Error", "Please fill all fields!");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            new MyAlert(AlertType.ERROR, "Error", "New passwords do not match!");
            return;
        }
        if (newPassword.equals(oldPassword)) {
            new MyAlert(AlertType.ERROR, "Error", "New password must be different from old password!");
            return;
        }
        if (newPassword.length() < 3) {
            new MyAlert(AlertType.ERROR, "Error", "Password must be at least 3 characters!");
            return;
        }

        if (updatePasswordInCSV(username, oldPassword, newPassword)) {
            new MyAlert(AlertType.INFORMATION, "Success", "Password changed successfully!");
            clearForm();
        } else {
            new MyAlert(AlertType.ERROR, "Error",
                    "Failed to change password. Check your username and current password.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE CSV  (preserves AccountCreatedDate)
    // ─────────────────────────────────────────────────────────────
    private boolean updatePasswordInCSV(String username, String oldPassword, String newPassword) {
        String csvFilePath = findCSVPath();
        if (csvFilePath == null) {
            new MyAlert(AlertType.ERROR, "File Error", "loginInfo.csv not found!");
            return false;
        }

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(csvFilePath, StandardCharsets.UTF_8))) {

            String  line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { lines.add(line); isFirstLine = false; continue; }

                String[] fields = line.split(",", -1);

                if (fields.length >= 5) {
                    String csvUsername = fields[0].trim();
                    String csvEmail    = fields[1].trim();
                    String csvPassword = fields[2].trim();
                    String userType    = fields[3].trim();
                    String status      = fields[4].trim();

                    if ((csvUsername.equals(username) || csvEmail.equalsIgnoreCase(username))
                            && csvPassword.equals(oldPassword)
                            && "Active".equalsIgnoreCase(status)) {

                        String today = LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        // col 5 = LastPasswordChange → today
                        // col 6 = AccountCreatedDate → keep original
                        String accountCreatedDate = (fields.length >= 7)
                                ? fields[6].trim() : today;

                        lines.add(csvUsername + "," + csvEmail + "," + newPassword
                                + "," + userType + "," + status
                                + "," + today
                                + "," + accountCreatedDate);
                        found = true;
                        continue;
                    }
                }
                lines.add(line);
            }
        } catch (IOException e) {
            new MyAlert(AlertType.ERROR, "File Error", "Error reading file: " + e.getMessage());
            return false;
        }

        if (!found) return false;

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(csvFilePath, StandardCharsets.UTF_8))) {
            for (String line : lines) { writer.write(line); writer.newLine(); }
        } catch (IOException e) {
            new MyAlert(AlertType.ERROR, "File Error", "Error writing file: " + e.getMessage());
            return false;
        }

        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  KEYBOARD NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML private void onUsernameKeyPressed(KeyEvent e)        { if (e.getCode() == KeyCode.ENTER) { (oldShown ? oldPasswordVisible : oldPasswordField).requestFocus();         e.consume(); } }
    @FXML private void onOldPasswordKeyPressed(KeyEvent e)     { if (e.getCode() == KeyCode.ENTER) { (newShown ? newPasswordVisible : newPasswordField).requestFocus();         e.consume(); } }
    @FXML private void onNewPasswordKeyPressed(KeyEvent e)     { if (e.getCode() == KeyCode.ENTER) { (confirmShown ? confirmPasswordVisible : confirmPasswordField).requestFocus(); e.consume(); } }
    @FXML private void onConfirmPasswordKeyPressed(KeyEvent e) { if (e.getCode() == KeyCode.ENTER) { handleReset(new ActionEvent()); e.consume(); } }

    // ─────────────────────────────────────────────────────────────
    //  BACK TO LOGIN
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void backToLogin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
        stage.setTitle("Login");
        stage.show();
    }

    // ─────────────────────────────────────────────────────────────
    //  CLEAR
    // ─────────────────────────────────────────────────────────────
    private void clearForm() {
        usernameField.clear();
        oldPasswordField.clear();     oldPasswordVisible.clear();
        newPasswordField.clear();     newPasswordVisible.clear();
        confirmPasswordField.clear(); confirmPasswordVisible.clear();
    }

    // ─────────────────────────────────────────────────────────────
    //  FIND CSV
    // ─────────────────────────────────────────────────────────────
    private String findCSVPath() {
        String[] paths = {
                CSV_PATH, "src/" + CSV_PATH,
                System.getProperty("user.dir") + "/" + CSV_PATH,
                System.getProperty("user.dir") + "/src/" + CSV_PATH
        };
        for (String path : paths) if (Files.exists(Paths.get(path))) return path;
        return null;
    }
}