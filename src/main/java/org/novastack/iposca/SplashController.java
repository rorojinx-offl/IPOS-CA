package org.novastack.iposca;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.novastack.iposca.exceptions.BootstrapException;
import org.novastack.iposca.http.Server;
import org.novastack.iposca.session.SessionTimeoutManager;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SplashController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::startBootstrap);
    }

    @FXML
    private ProgressBar progressBar;

    private void startBootstrap() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Bootstrap.init((step, message) -> {
                    updateMessage(message);
                    updateProgress(step, Bootstrap.TOTAL_STEPS);
                });
                updateMessage("Bootstrap completed!");
                updateProgress(Bootstrap.TOTAL_STEPS, Bootstrap.TOTAL_STEPS);
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(event -> {
            try {
                Stage stage = (Stage) progressBar.getScene().getWindow();
                stage.close();

                stage = new Stage();
                stage.setResizable(false);
                new CommonCalls().traverse(stage, "/ui/login/login.fxml", "Login");
                SessionTimeoutManager.install(stage);
                Server.start();
            } catch (Exception e) {
                throw new BootstrapException("Error starting up the application: " + e.getMessage());
            }
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            if (error != null) {
                try {
                    new CommonCalls().openErrorDialog(error.getMessage());
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
