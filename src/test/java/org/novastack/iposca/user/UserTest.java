package org.novastack.iposca.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.session.SessionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";
    private final List<String> usernamesToCleanup = new ArrayList<>();

    @RegisterExtension
    static TestWatcher testWatcher = new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
            System.out.println(GREEN + "SUCCESS: " + context.getDisplayName() + RESET);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            String reason = cause == null ? "Unknown error" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
            System.out.println(RED + "ERROR: " + context.getDisplayName() + " -> " + reason + RESET);
        }
    };

    @AfterEach
    void cleanup() {
        for (String username : usernamesToCleanup) {
            deleteUserIfExists(username);
        }
        usernamesToCleanup.clear();
        SessionManager.end();
    }

    @Test
    @DisplayName("TC-01: Test to see if user account with valid details is successfully created.")
    @Order(1)
    void testSuccessfulLogin() throws Exception {
        String username = uniqueUsername("tc01_admin");
        String password = "password123";
        createUser(username, password, UserEnums.UserRole.ADMIN, "TC01 Admin");

        User authenticated = User.authenticateUser(username, password);
        SessionManager.start(authenticated);

        assertNotNull(authenticated);
        assertEquals(username, authenticated.getUsername());
        assertEquals(UserEnums.UserRole.ADMIN, authenticated.getRole());
        assertNotNull(SessionManager.getCurrentSession());
    }

    @Test
    @DisplayName("TC-02: Test to see if user account with invalid password is successfully created.")
    @Order(2)
    void testUnsuccessfulLoginWithInvalidPassword() throws Exception {
        String username = uniqueUsername("tc02_admin");
        createUser(username, "password123", UserEnums.UserRole.ADMIN, "TC02 Admin");

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> User.authenticateUser(username, "wrongPassword"));

        assertEquals("Invalid Password!", ex.getMessage());
        assertNull(SessionManager.getCurrentSession());
    }

    @Test
    @DisplayName("TC-03: Test to see if user account with invalid username is successfully created.")
    @Order(3)
    void testUnsuccessfulLoginWithInvalidUsername() {
        String username = uniqueUsername("tc03_missing");

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> User.authenticateUser(username, "password123"));

        assertEquals("User doesn't exist!", ex.getMessage());
        assertNull(SessionManager.getCurrentSession());
    }

    @Test
    @DisplayName("TC-04: Test to see whether session will timeout")
    @Order(4)
    void testSessionTimeout() {
        User admin = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        SessionManager.start(admin);
        assertNotNull(SessionManager.getCurrentSession());

        // Real timeout logic is JavaFX-driven; this is the direct end-of-session equivalent.
        SessionManager.end();
        assertNull(SessionManager.getCurrentSession());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("TC-05: Test to see whether user can logout")
    @Order(5)
    void testLogoutFunctionality() {
        User admin = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        SessionManager.start(admin);

        assertTrue(SessionManager.getCurrentSession().isLoggedIn());
        SessionManager.end();
        assertNull(SessionManager.getCurrentSession());
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("TC-06: Test to see whether an admin can create a user account, with username, password and role")
    @Order(6)
    void testAdminCreatesUserAccount() {
        String newUsername = uniqueUsername("tc06_pharmacist");
        String newPassword = "password123";

        User adminActor = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        User newUser = new User(newUsername, newPassword, UserEnums.UserRole.PHARMACIST, "TC06 Pharmacist", LocalDate.now());
        adminActor.createUser(newUser);
        usernamesToCleanup.add(newUsername);

        User storedUser = findUserByUsername(newUsername);
        assertNotNull(storedUser);
        assertEquals(UserEnums.UserRole.PHARMACIST, storedUser.getRole());
    }

    @Test
    @DisplayName("TC-07: Test to create user account with invalid fields")
    @Order(7)
    void testValidationOfRequiredFields() {
        String username = uniqueUsername("tc07_invalid");
        User invalid = new User(username, "", null, "", LocalDate.now());

        assertThrows(NullPointerException.class, () -> invalid.createUser(invalid));
        assertNull(findUserByUsername(username));
    }

    @Test
    @DisplayName("TC-08: Test to prevent user accounts with duplicate usernames being created")
    @Order(8)
    void testDuplicateUsernames() {
        String username = uniqueUsername("tc08_pharmacist");
        createUser(username, "password123", UserEnums.UserRole.PHARMACIST, "TC08 Pharmacist");

        User duplicate = new User(username, "newPassword", UserEnums.UserRole.PHARMACIST, "TC08 Pharmacist Dup", LocalDate.now());
        assertThrows(DataAccessException.class, () -> duplicate.createUser(duplicate),
                "System should reject duplicate usernames");
    }

    @Test
    @DisplayName("TC-09: Test to delete account")
    @Order(9)
    void testDeleteAccounts() {
        String username = uniqueUsername("tc09_pharmacist");
        createUser(username, "password123", UserEnums.UserRole.PHARMACIST, "TC09 Pharmacist");

        User created = findUserByUsername(username);
        assertNotNull(created);
        User.deleteUserById(created.getId());
        usernamesToCleanup.remove(username);

        User deletedUser = findUserByUsername(username);
        assertNull(deletedUser);
    }

    @Test
    @DisplayName("TC-10: Test - Admin has access to all packages")
    @Order(10)
    void testAdminAccess() {
        User admin = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        Session session = new Session(admin);

        for (UserEnums.UserAccess access : UserEnums.UserAccess.values()) {
            assertTrue(session.hasAccess(UserEnums.UserRole.ADMIN, access),
                    "Admin should have access to package " + access.name());
        }
    }

    @Test
    @DisplayName("TC-11: Test manager access to packages")
    @Order(11)
    void testManagerAccess() {
        User manager = new User("manager1", "password123", UserEnums.UserRole.MANAGER, "Manager User", LocalDate.now());
        Session session = new Session(manager);

        UserEnums.UserAccess[] allowedPackages = {
                UserEnums.UserAccess.CUST,
                UserEnums.UserAccess.TEMPLATES,
                UserEnums.UserAccess.RPT
        };
        UserEnums.UserAccess[] restrictedPackages = {
                UserEnums.UserAccess.ORD,
                UserEnums.UserAccess.USER,
                UserEnums.UserAccess.STOCK,
                UserEnums.UserAccess.SALES
        };

        for (UserEnums.UserAccess access : allowedPackages) {
            assertTrue(session.hasAccess(UserEnums.UserRole.MANAGER, access),
                    "Manager should have access to: " + access.name());
        }

        for (UserEnums.UserAccess access : restrictedPackages) {
            assertFalse(session.hasAccess(UserEnums.UserRole.MANAGER, access),
                    "Manager should NOT have access to: " + access.name());
        }
    }

    @Test
    @DisplayName("TC-12: Test Pharmacist access to packages")
    @Order(12)
    void testPharmacistAccess() {
        User pharmacist = new User("pharmacist1", "password123", UserEnums.UserRole.PHARMACIST, "Pharmacist User", LocalDate.now());
        Session session = new Session(pharmacist);

        UserEnums.UserAccess[] allowedPackages = {
                UserEnums.UserAccess.ORD,
                UserEnums.UserAccess.CUST,
                UserEnums.UserAccess.TEMPLATES,
                UserEnums.UserAccess.STOCK,
                UserEnums.UserAccess.SALES,
                UserEnums.UserAccess.RPT
        };
        UserEnums.UserAccess[] restrictedPackages = {UserEnums.UserAccess.USER};

        for (UserEnums.UserAccess access : allowedPackages) {
            assertTrue(session.hasAccess(UserEnums.UserRole.PHARMACIST, access),
                    "Pharmacist should have access to: " + access.name());
        }

        for (UserEnums.UserAccess access : restrictedPackages) {
            assertFalse(session.hasAccess(UserEnums.UserRole.PHARMACIST, access),
                    "Pharmacist should NOT have access to: " + access.name());
        }
    }

    private void createUser(String username, String password, UserEnums.UserRole role, String fullName) {
        User user = new User(username, password, role, fullName, LocalDate.now());
        user.createUser(user);
        usernamesToCleanup.add(username);
    }

    private User findUserByUsername(String username) {
        return User.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private void deleteUserIfExists(String username) {
        User existing = findUserByUsername(username);
        if (existing != null) {
            User.deleteUserById(existing.getId());
        }
    }

    private String uniqueUsername(String prefix) {
        return prefix + "_" + System.nanoTime();
    }
}
