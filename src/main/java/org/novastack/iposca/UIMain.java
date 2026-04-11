package org.novastack.iposca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.novastack.iposca.http.Server;
import org.novastack.iposca.session.SessionTimeoutManager;

public class UIMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/login/login.fxml"));
        stage.setTitle("Customer");
        stage.setScene(new Scene(root));
        SessionTimeoutManager.install(stage);
        Bootstrap.init();
        Server.start();
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }


}
