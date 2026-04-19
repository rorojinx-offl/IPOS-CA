package org.novastack.iposca.login;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.novastack.iposca.DashboardController;
import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.session.SessionManager;
import org.novastack.iposca.session.SessionTimeoutManager;
import org.novastack.iposca.user.User;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;

/**
 * Class that handles the login process.
 * */
public class Login {
    /**
     * Method that handles the login process and is invoked by {@link LoginController} when the user clicks the login button.
     * It authenticates the user and if the credentials are invalid, it throws an {@link AuthenticationException}. If it
     * is valid, it starts a user session which stores the user's information in memory, so that session can be retained
     * throughout the application's lifecycle. It then resets the session timeout timer and navigates the user to the
     * dashboard.
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @param node The JavaFX {@link Node} that invoked the login process, for which we can get a reference to the stage
     * and navigate to the dashboard.
     * @throws AuthenticationException If the credentials are invalid.
     * @throws IOException If there is an error in the FXMLLoader.
     * */
    public static void login(String username, String password, Node node) throws AuthenticationException, IOException {
        User user = User.authenticateUser(username, password);

        Stage stage = (Stage) node.getScene().getWindow();
        SessionManager.start(user);
        SessionTimeoutManager.resetTimer();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }
}
