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

    private FXMLLoader loader;   // ✅ Store loader globally

    public LoadStage(String url, Node node, boolean fullScreen) {

        try {

            loader = new FXMLLoader(
                    LoadStage.class.getResource(url)
            );

            Parent root = loader.load();

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

                stage.centerOnScreen();
            }

            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // ✅ VERY IMPORTANT METHOD
    public Object getController() {
        return loader.getController();
    }
}