package mehrin.loginpage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mehrin.loginpage.Util.FileUtil;

public class LoginApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // 🔥 VERY IMPORTANT LINE (Auto create data files)
        FileUtil.initializeDataFiles();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("RUET Library");

        String css = this.getClass().getResource("loginStyle.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
