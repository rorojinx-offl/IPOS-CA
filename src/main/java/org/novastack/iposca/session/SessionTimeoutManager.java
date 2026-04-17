package org.novastack.iposca.session;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.novastack.iposca.utils.ui.CommonCalls;

public class SessionTimeoutManager {
    private static final Duration TIMEOUT = Duration.minutes(15);
    private static final PauseTransition inactivityTimer = new PauseTransition(TIMEOUT);
    private static Scene currentScene;

    public static void install(Stage stage) {
        inactivityTimer.setOnFinished(event -> logout(stage));

        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            attachToScene(newScene);
            resetTimer();
        });

        attachToScene(stage.getScene());
        resetTimer();
    }

    private static void attachToScene(Scene scene) {
        if (scene == null) {
            return;
        }

        currentScene = scene;
        scene.addEventFilter(InputEvent.ANY, event -> resetTimer());
    }

    public static void resetTimer() {
        if (SessionManager.getCurrentSession() != null) {
            inactivityTimer.playFromStart();
        }
    }

    public static void stopTimer() {
        inactivityTimer.stop();
    }

    public static void logout(Stage stage) {
        SessionManager.end();
        stopTimer();

        Platform.runLater(() -> {
            try {
                new CommonCalls().traverse(stage, "/ui/login/login.fxml", "Login");
            } catch (Exception e) {
                System.exit(1);
            }
        });
    }
}
