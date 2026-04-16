package org.novastack.iposca.utils.common;

import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;

import java.time.LocalDate;

public class TestAddUser {
    static void main() {
        User user = new User(
                "sysdba",
                "masterkey",
                UserEnums.UserRole.ADMIN,
                "Rohit Gurunathan",
                LocalDate.now()
        );
        user.createUser(user);

        user = new User(
                "manager",
                "Get_it_done",
                UserEnums.UserRole.MANAGER,
                "Ahmed Hassan",
                LocalDate.now()
        );
        user.createUser(user);

        user = new User(
                "accountant",
                "Count_money",
                UserEnums.UserRole.PHARMACIST,
                "Hetal Patel",
                LocalDate.now()
        );
        user.createUser(user);

        user = new User(
                "clerk",
                "Paperwork",
                UserEnums.UserRole.PHARMACIST,
                "Wasil Barits",
                LocalDate.now()
        );
        user.createUser(user);
    }
}
