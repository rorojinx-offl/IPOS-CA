package org.novastack.iposca.session;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.InputEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.novastack.iposca.utils.ui.CommonCalls;

/**
 * Class for managing session timeout.
 * */
public class SessionTimeoutManager {
    /**
     * Timeout duration in minutes.
     * */
    private static final Duration TIMEOUT = Duration.minutes(15);
    /**
     * A {@link PauseTransition} object that is used to track the user's inactivity.
     * */
    private static final PauseTransition inactivityTimer = new PauseTransition(TIMEOUT);
    private static Scene currentScene;

    /**
     * Allows the timeout manager to be installed onto a stage and track user inactivity on every scene.
     * @param stage The stage to install the timeout manager on.
     * */
    public static void install(Stage stage) {
        inactivityTimer.setOnFinished(event -> logout(stage));

        //Automatically attach the timeout manager to the current scene when the stage's scene changes. Reset the timer.
        stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            attachToScene(newScene);
            resetTimer();
        });

        attachToScene(stage.getScene());
        resetTimer();
    }

    /**
     * Attaches the timeout manager to a scene.
     * @param scene The scene to attach the timeout manager to.
     * */
    private static void attachToScene(Scene scene) {
        if (scene == null) {
            return;
        }

        currentScene = scene;
        scene.addEventFilter(InputEvent.ANY, event -> resetTimer());
    }

    /**
     * Start/restarts the inactivity timer.
     * */
    public static void resetTimer() {
        if (SessionManager.getCurrentSession() != null) {
            inactivityTimer.playFromStart();
        }
    }

    public static void stopTimer() {
        inactivityTimer.stop();
    }

    /**
     * Logs out the user automatically after a timeout.
     * @param stage The stage used to navigate to the login page.
     * */
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
