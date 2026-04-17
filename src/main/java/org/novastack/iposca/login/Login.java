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

public class Login {
    public static void login(String username, String password, Node node) throws AuthenticationException, IOException {
        User user = User.authenticateUser(username, password);

        Stage stage = (Stage) node.getScene().getWindow();
        SessionManager.start(user);
        SessionTimeoutManager.resetTimer();
        new CommonCalls().traverse(stage, "/ui/dashboard/dashboard.fxml", "IPOS-CA Dashboard");
    }
}
