package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    private static final String CSV_PATH = "data/loginInfo.csv";

    // ================= RESET PASSWORD =================
    @FXML
    private void handleReset(ActionEvent event) {
        String username = usernameField.getText().trim();
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        System.out.println("Reset Password attempt - Username: " + username);

        // Validation
        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
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
            new MyAlert(AlertType.ERROR, "Error", "New password must be at least 3 characters!");
            return;
        }

        // Update password in loginInfo.csv
        if (updatePasswordInCSV(username, oldPassword, newPassword)) {
            new MyAlert(AlertType.INFORMATION, "Success", "Password changed successfully!");
            clearForm();
        } else {
            new MyAlert(AlertType.ERROR, "Error", "Failed to change password. Check your username and old password.");
        }
    }

    // ================= KEYBOARD NAVIGATION =================
    @FXML
    private void onUsernameKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            oldPasswordField.requestFocus();
            event.consume();
        }
    }

    @FXML
    private void onOldPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            newPasswordField.requestFocus();
            event.consume();
        }
    }

    @FXML
    private void onNewPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            confirmPasswordField.requestFocus();
            event.consume();
        }
    }

    @FXML
    private void onConfirmPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleReset(new ActionEvent());
            event.consume();
        }
    }

    // ================= UPDATE PASSWORD IN CSV =================
    private boolean updatePasswordInCSV(String username, String oldPassword, String newPassword) {
        String csvFilePath = findCSVPath();

        if (csvFilePath == null) {
            new MyAlert(AlertType.ERROR, "File Error", "loginInfo.csv not found!");
            return false;
        }

        System.out.println("Using CSV path: " + csvFilePath);

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    lines.add(line); // Keep header
                    System.out.println("Header: " + line);
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

                    System.out.println("Checking user: " + csvUsername + " (Email: " + csvEmail + ")");

                    // Check if this is the user
                    if ((csvUsername.equals(username) || csvEmail.equalsIgnoreCase(username))
                            && csvPassword.equals(oldPassword)
                            && "Active".equalsIgnoreCase(status)) {

                        System.out.println("✓ User found! Updating password...");

                        // Update password with current date
                        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String updatedLine = csvUsername + "," + csvEmail + "," + newPassword + "," + userType + "," + status + "," + currentDate + "," + currentDate;
                        lines.add(updatedLine);
                        found = true;
                    } else {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
            new MyAlert(AlertType.ERROR, "File Error", "Error reading file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (found) {
            // Write back to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
                System.out.println("✓ Password updated successfully and saved to CSV");
                return true;
            } catch (IOException e) {
                System.out.println("❌ Error writing file: " + e.getMessage());
                new MyAlert(AlertType.ERROR, "File Error", "Error writing file: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        } else {
            System.out.println("❌ User not found or credentials don't match");
        }

        return false;
    }

    // ================= FIND CSV PATH =================
    private String findCSVPath() {
        String[] possiblePaths = {
                CSV_PATH,
                "src/" + CSV_PATH,
                System.getProperty("user.dir") + "/" + CSV_PATH,
                System.getProperty("user.dir") + "/src/" + CSV_PATH
        };

        for (String path : possiblePaths) {
            if (Files.exists(Paths.get(path))) {
                System.out.println("✓ Found CSV at: " + path);
                return path;
            }
        }
        return null;
    }

    // ================= BACK TO LOGIN =================
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

    // ================= CLEAR FORM =================
    private void clearForm() {
        usernameField.clear();
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}