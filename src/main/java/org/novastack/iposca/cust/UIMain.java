package org.novastack.iposca.cust;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class UIMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/cust/sample.fxml"));
        stage.setTitle("Customer");
        stage.setScene(new Scene(root));
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }


}
