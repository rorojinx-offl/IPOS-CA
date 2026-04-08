package org.novastack.iposca;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class UIMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/ui/temporaryMenuDashboard.fxml")));

        stage.setTitle("Temporary Menu Dashboard");
        stage.setScene(new Scene(root));
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
