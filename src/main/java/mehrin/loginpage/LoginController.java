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

public class LoginController{
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibility;
    @FXML private Button show;
    private boolean passwordShown=false;
    //private static final String CSV_PATH = "data/loginInfo.csv";
    //hide show button
    @FXML private void showOrHide(ActionEvent event) {
        passwordShown=!passwordShown;
         if(passwordShown){//password dekha jacce
            passwordVisibility.setText(passwordField.getText());
            passwordVisibility.setVisible(true);
            passwordVisibility.setManaged(true);
            passwordField.setVisible(false);//password field dekha jacce na
            passwordField.setManaged(false);
            show.setText("Hide");
        }
         else{
            passwordField.setText(passwordVisibility.getText());//password field show korce password hide kore ⚫️⚫️⚫️diye
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibility.setVisible(false);
            passwordVisibility.setManaged(false);
            show.setText("Show");
        }
    }
    private String getCurrentPassword(){
        return passwordShown? passwordVisibility.getText():passwordField.getText();//jodi dekha jay taile passwordvisibility theke nebo na dekha gelew pass ta pass textfield ace okahne theke nebo
    }
    //login process
    @FXML private void handleLogin(ActionEvent event){
        if (!validateFields()){
            new MyAlert(AlertType.INFORMATION,"Validation Error","Please enter username and password");
            return;
        }
        performLogin(event);
    }
    private void performLogin(ActionEvent event){
        String username=usernameField.getText().trim();//age picher extra space trim korbe majher space na
        String password=getCurrentPassword();
        LoginInfo loginInfo=validateInfo(username, password);
        if (loginInfo==null) {
            new MyAlert(AlertType.ERROR,"Login Failed", "Invalid username or password");
            return;
        }
        Node node=(Node) event.getSource();
        //age thekei usertype a admin na student bola ace oita just check debo
        if ("student".equalsIgnoreCase(loginInfo.getUserType())){//equalsIgnoreCase amr usertype er sate "student" string k compare korce case sensitivity charai
            SessionManager.getInstance().setLoggedInStudentId(loginInfo.getUsername());
            String studentName=studentName(loginInfo.getUsername());
            SessionManager.getInstance().setLoggedInStudentName(studentName);
            LoadStage loadStage=new LoadStage("/mehrin/loginpage/StudentDashboard.fxml", node, true);
            //student dashboard a loadstage diye sorasori student er info pass korlam
            StudentDashboardController sdcontroller=(StudentDashboardController)loadStage.getController();
            sdcontroller.setStudentId(loginInfo.getUsername());
        }
        else if("admin".equalsIgnoreCase(loginInfo.getUserType())) {
            new LoadStage("/mehrin/loginpage/Dashboard.fxml", node, true);
        }
        else{
            new MyAlert(AlertType.ERROR, "Error", "Unknown user type: " + loginInfo.getUserType());
        }
    }
    private String studentName(String studentId) {
        for (String line : FileUtil.readFile("students.csv")) {
            String[] p = line.split(",", -1);//last emply column jeno ignore na hoy
            if (p.length > 1 && p[0].trim().equalsIgnoreCase(studentId)) {
                return p[1].trim();
            }
        }
        return studentId;//nam na pelew id show korte parbo
    }

    @FXML private void onUsernameKeyPressed(KeyEvent event){
        if (event.getCode()==KeyCode.ENTER) {//event.getCode() check dicci enter key chapa hoice naki
            //then passfield a niye jacce check diye konta pass shown hoile if na hoile else
            if (passwordShown)
                passwordVisibility.requestFocus();
            else
                passwordField.requestFocus();
            event.consume();//enter er kaj ses event close
        }
    }
    @FXML private void onPasswordKeyPressed(KeyEvent event) {
        if (event.getCode()==KeyCode.ENTER) {
            if (validateFields()) {
                performLogin(new ActionEvent((Node) event.getSource(), null));//getsource orbject type return kore korbo na tai null
            }
            event.consume();
        }
    }

    private LoginInfo validateInfo(String username, String password) {
        for (String line:FileUtil.readFile("loginInfo.csv")) {
            String[] field=line.split(",", -1);
            if (field.length<5) continue;
            boolean userNameChecker=field[0].trim().equals(username)||field[1].trim().equalsIgnoreCase(username);//username er sate millew hobe email er satew
            boolean  passwordChecker=field[2].trim().equals(password);
            if (userNameChecker && passwordChecker)
                return new LoginInfo(field[0].trim(),field[1].trim(),field[2].trim(),field[3].trim(),field[4].trim());
        }
        return null;
    }

    @FXML private void changePass(ActionEvent event){
        new LoadStage("/mehrin/loginpage/ResetPassword.fxml",(Node)event.getSource(),true);
    }
    private boolean validateFields() {
        return !usernameField.getText().isEmpty() && !getCurrentPassword().isEmpty();
    }
}