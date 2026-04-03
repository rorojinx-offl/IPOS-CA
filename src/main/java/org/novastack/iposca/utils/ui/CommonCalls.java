package org.novastack.iposca.utils.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.novastack.iposca.DashboardController;
import org.novastack.iposca.session.SessionManager;

import java.io.IOException;

public class CommonCalls {
    public void traverse(Stage stage, String targetFXML, String title) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(targetFXML));
        stage.setTitle(title);
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    public void openErrorDialog(String error) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/common/error.fxml"));
        Parent root = loader.load();

        ErrorController controller = loader.getController();
        controller.setError(error);

        stage.setTitle("Error");
        stage.setScene(new javafx.scene.Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public boolean openConfirmationDialog(String prompt) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/common/confirm.fxml"));
        Parent root = loader.load();

        ConfirmController controller = loader.getController();
        controller.setPrompt(prompt);

        stage.setTitle("Confirmation");
        stage.setScene(new javafx.scene.Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        return controller.getResult();
    }
}
