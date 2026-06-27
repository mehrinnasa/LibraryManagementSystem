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
import mehrin.loginpage.Util.FileUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ResetPassController {
    @FXML private TextField usernameField;
    @FXML private PasswordField oldPasswordField;
    @FXML private TextField oldPasswordVisible;
    @FXML private Button oshoworhide;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordVisible;
    @FXML private Button nshoworhide;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Button cshoworhide;

    private boolean oldShown= false;
    private boolean newShown= false;
    private boolean confirmShown= false;
    private static final String CSV_PATH = "data/loginInfo.csv";
    @FXML private void oldshoworhide(ActionEvent e) {
        oldShown=!oldShown;
        showorhide(oldShown, oldPasswordField, oldPasswordVisible, oshoworhide);
    }
    @FXML private void newshoworhide(ActionEvent e) {
        newShown=!newShown;
        showorhide(newShown, newPasswordField, newPasswordVisible, nshoworhide);
    }
    @FXML private void confirmshoworhide(ActionEvent e) {
        confirmShown=!confirmShown;
        showorhide(confirmShown, confirmPasswordField, confirmPasswordVisible, cshoworhide);
    }
    private void showorhide(boolean show, PasswordField pf, TextField tf, Button btn) {
        if(show){
            tf.setText(pf.getText());
            tf.setVisible(true);
            tf.setManaged(true);
            pf.setVisible(false);
            pf.setManaged(false);
            btn.setText("Hide");
        }
        else {
            pf.setText(tf.getText());
            pf.setVisible(true);
            pf.setManaged(true);
            tf.setVisible(false);
            tf.setManaged(false);
            btn.setText("Show");
        }
    }
    private String getOldPassword(){
        return oldShown ? oldPasswordVisible.getText() : oldPasswordField.getText();
    }
    private String getNewPassword(){
        return newShown ? newPasswordVisible.getText() : newPasswordField.getText();
    }
    private String getConfirmPassword(){
        return confirmShown ? confirmPasswordVisible.getText() : confirmPasswordField.getText();
    }
    @FXML private void handleReset(ActionEvent event) {
        String username = usernameField.getText().trim();
        String oldPassword= getOldPassword();
        String newPassword= getNewPassword();
        String confirmPassword = getConfirmPassword();

        if (username.isEmpty()||oldPassword.isEmpty()||newPassword.isEmpty()||confirmPassword.isEmpty()){
            new MyAlert(AlertType.ERROR, "Validation Error", "Please fill all fields!");
            return;
        }
        if (!newPassword.equals(confirmPassword)){
            new MyAlert(AlertType.ERROR, "Error", "New passwords do not match!");
            return;
        }
        if (newPassword.equals(oldPassword)){
            new MyAlert(AlertType.ERROR, "Error", "New password must be different from old password!");
            return;
        }
        if (newPassword.length()<3){
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

    private static final String LOGIN_HEADER ="Username,Email,Password,UserType,Status,LastPasswordChange,AccountCreatedDate";

    private boolean updatePasswordInCSV(String username,String oldPassword,String newPassword) {
        List<String> lines=FileUtil.readFile("loginInfo.csv");
        if (lines.isEmpty()) {
            new MyAlert(AlertType.ERROR, "File Error", "loginInfo.csv not found or empty!");
            return false;
        }
        boolean found = false;
        for (int i=0; i<lines.size(); i++){
            String[] f=lines.get(i).split(",", -1);
            if (f.length < 5)
                continue;
            boolean matches=(f[0].trim().equals(username)||f[1].trim().equalsIgnoreCase(username))&& f[2].trim().equals(oldPassword)&& "Active".equalsIgnoreCase(f[4].trim());
            if (matches) {
                String today=LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String createdDate=(f.length>=7) ? f[6].trim():today;
                lines.set(i,String.join(",",
                        f[0].trim(),
                        f[1].trim(),
                        newPassword,
                        f[3].trim(),
                        f[4].trim(),
                        today,
                        createdDate));
                found = true;
                break;
            }
        }

        if (!found)
            return false;
        FileUtil.writeFile("loginInfo.csv", lines, LOGIN_HEADER);
        return true;
    }

    @FXML private void onUsernameKeyPressed(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER) {
            (oldShown ? oldPasswordVisible : oldPasswordField).requestFocus();
            e.consume();
        }
    }
    @FXML private void onOldPasswordKeyPressed(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER) {
            (newShown ? newPasswordVisible : newPasswordField).requestFocus();
            e.consume();
        }
    }
    @FXML private void onNewPasswordKeyPressed(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER) {
            (confirmShown ? confirmPasswordVisible : confirmPasswordField).requestFocus();
            e.consume();
        }
    }
    @FXML private void onConfirmPasswordKeyPressed(KeyEvent e){
        if (e.getCode() == KeyCode.ENTER) {
            handleReset(new ActionEvent());
            e.consume();
        }
    }
    @FXML private void backToLogin(ActionEvent event){
        new LoadStage("/mehrin/loginpage/Login.fxml",(Node)event.getSource(),true);
    }
    private void clearForm() {
        usernameField.clear();
        oldPasswordField.clear();
        oldPasswordVisible.clear();
        newPasswordField.clear();
        newPasswordVisible.clear();
        confirmPasswordField.clear();
        confirmPasswordVisible.clear();
    }
}