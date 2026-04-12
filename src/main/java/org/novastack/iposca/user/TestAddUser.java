package org.novastack.iposca.user;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.JooqConnection;
import org.novastack.iposca.utils.db.SQLiteConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static schema.tables.User.USER;

public class TestAddUser {
    public static void main(String[] args) throws SQLException {
        String username = "admin";

        Connection connection = new SQLiteConnection().getConnection();
        org.novastack.iposca.utils.db.spawn.User.create(connection, new DDLEngine());

        DSLContext ctx = JooqConnection.getDSLContext();

        boolean userExists = ctx.fetchExists(
                ctx.selectFrom(USER)
                        .where(USER.USERNAME.eq(username))
        );

        if (userExists) {
            System.out.println("User already exists: " + username);
            return;
        }

        org.novastack.iposca.user.User user = new org.novastack.iposca.user.User(
                username,
                "admin123",
                org.novastack.iposca.user.UserEnums.UserRole.ADMIN,
                "Admin User",
                LocalDate.now()
        );

        org.novastack.iposca.user.User secondUser = new org.novastack.iposca.user.User(
                "TestPharmacist",
                "banana",
                UserEnums.UserRole.PHARMACIST,
                "Jane Smith",
                LocalDate.now()
        );

        user.createUser(user);
        secondUser.createUser(secondUser);
        System.out.println("Created admin user: " + username);
    }
}
