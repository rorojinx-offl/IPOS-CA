package org.novastack.iposca.login;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.novastack.iposca.DashboardController;
import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.user.User;

import java.io.IOException;

public class Login {
    public static void login(String username, String password, Node node) throws AuthenticationException, IOException {
        User user = User.authenticateUser(username, password);

        Stage stage = (Stage) node.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Login.class.getResource("/ui/dashboard/dashboard.fxml"));
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        controller.receive(user);

        stage.setTitle("IPOS-CA Dashboard");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }
}
