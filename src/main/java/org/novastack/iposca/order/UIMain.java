package org.novastack.iposca.order;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UIMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/user/createUserPage.fxml"));

        stage.setTitle("Create User Account");
        stage.setScene(new Scene(root));
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
