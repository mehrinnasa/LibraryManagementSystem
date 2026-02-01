package mehrin.loginpage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadStage {

    /**
     * @param url   FXML path (e.g., "/mehrin/loginpage/Main.fxml")
     * @param node  Any Node from current scene to get the Stage
     * @param fullScreen  true = full screen (Main page), false = small undecorated (Login/Reset)
     */
    public LoadStage(String url, Node node, boolean fullScreen) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(url));

            Stage stage = (Stage) node.getScene().getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

            if (fullScreen) {
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
            } else {
                stage.initStyle(StageStyle.UNDECORATED);
                stage.centerOnScreen();
            }

            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(LoadStage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
