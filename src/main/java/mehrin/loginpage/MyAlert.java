package mehrin.loginpage;
import javafx.scene.control.Alert.AlertType;

public class  MyAlert {
    public MyAlert(AlertType type, String title, String text){
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}