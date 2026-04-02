package org.novastack.iposca.login;

import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.time.LocalDate;

public class Login {
    public static void login(String username, String password) throws AuthenticationException, IOException {
        User user = User.authenticateUser(username, password);
        new CommonCalls().openErrorDialog("Hello, " + user.getFullName() + "! You have successfully logged in!");
    }
}
