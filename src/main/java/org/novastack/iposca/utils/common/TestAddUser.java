package org.novastack.iposca.utils.common;

import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;

import java.time.LocalDate;

public class TestAddUser {
    static void main() {
        User user = new User(
                "TestManager",
                "apple",
                UserEnums.UserRole.MANAGER,
                "John Smith",
                LocalDate.now()
        );

        user.createUser(user);
    }
}
