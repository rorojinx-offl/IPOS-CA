package org.novastack.iposca.order;

import static schema.tables.Users.USERS;

public class TestUsers {
    public static void main(String[] args) {
        try {
            User userService = new User();

            User user1 = new User(
                    "admin1",
                    "admin123",
                    "Admin User",
                    "ADMIN",
                    1,
                    0
            );

            User user2 = new User(
                    "pharmacist1",
                    "pharma123",
                    "Sarah Ahmed",
                    "PHARMACIST",
                    1,
                    0
            );

            userService.addUser(user1);
            userService.addUser(user2);

            System.out.println("Two users added successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}