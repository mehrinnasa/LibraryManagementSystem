module mehrin.loginpage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;


    opens mehrin.loginpage to javafx.fxml;
    exports mehrin.loginpage;
    exports mehrin.loginpage.Service;
    opens mehrin.loginpage.Service to javafx.fxml;
    exports mehrin.loginpage.Run;
    opens mehrin.loginpage.Run to javafx.fxml;
}