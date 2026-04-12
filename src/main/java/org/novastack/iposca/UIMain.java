package org.novastack.iposca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.novastack.iposca.http.Server;
import org.novastack.iposca.session.SessionTimeoutManager;

public class UIMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/splash/splashScreen.fxml"));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void stop() {
        Server.stop();
    }

    static void main(String[] args) {
        launch(args);
    }


}
