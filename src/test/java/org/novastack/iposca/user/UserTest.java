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
    @DisplayName("TC-01: login succeeds with valid credentials")
    @Order(1)
    void testSuccessfulLogin() throws Exception {
        String username = uniqueUsername("tc01_admin");
        createUser(username, "password123", UserEnums.UserRole.ADMIN, "TC01 Admin");

        User authenticated = User.authenticateUser(username, "password123");
        SessionManager.start(authenticated);

        assertEquals(username, authenticated.getUsername());
        assertNotNull(SessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("TC-02: login fails when password is wrong")
    @Order(2)
    void testUnsuccessfulLoginWithInvalidPassword() throws Exception {
        String username = uniqueUsername("tc02_admin");
        createUser(username, "password123", UserEnums.UserRole.ADMIN, "TC02 Admin");

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> User.authenticateUser(username, "wrongPassword"));
        assertEquals("Invalid Password!", ex.getMessage());
    }

    @Test
    @DisplayName("TC-03: login fails for an unknown username")
    @Order(3)
    void testUnsuccessfulLoginWithInvalidUsername() {
        String username = uniqueUsername("tc03_missing");

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> User.authenticateUser(username, "password123"));
        assertEquals("User doesn't exist!", ex.getMessage());
    }

    @Test
    @DisplayName("TC-04: timeout path ends the active session")
    @Order(4)
    void testSessionTimeout() {
        User admin = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        SessionManager.start(admin);

        assertTrue(SessionManager.getCurrentSession().isLoggedIn());
        SessionManager.end();
        assertNull(SessionManager.getCurrentSession());
    }

    @Test
    @DisplayName("TC-05: logout clears current user")
    @Order(5)
    void testLogoutFunctionality() {
        User admin = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        SessionManager.start(admin);

        SessionManager.end();
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("TC-06: admin can create a pharmacist user")
    @Order(6)
    void testAdminCreatesUserAccount() {
        String newUsername = uniqueUsername("tc06_pharmacist");
        User adminActor = new User("admin", "password123", UserEnums.UserRole.ADMIN, "Admin User", LocalDate.now());
        User newUser = new User(newUsername, "password123", UserEnums.UserRole.PHARMACIST, "TC06 Pharmacist", LocalDate.now());
        adminActor.createUser(newUser);
        usernamesToCleanup.add(newUsername);

        User storedUser = findUserByUsername(newUsername);
        assertNotNull(storedUser);
        assertEquals(UserEnums.UserRole.PHARMACIST, storedUser.getRole());
    }

    @Test
    @DisplayName("TC-07: creating a user with missing required fields fails")
    @Order(7)
    void testValidationOfRequiredFields() {
        String username = uniqueUsername("tc07_invalid");
        User invalid = new User(username, "", null, "", LocalDate.now());

        assertThrows(NullPointerException.class, () -> invalid.createUser(invalid));
        assertNull(findUserByUsername(username));
    }

    @Test
    @DisplayName("TC-08: duplicate usernames are rejected")
    @Order(8)
    void testDuplicateUsernames() {
        String username = uniqueUsername("tc08_pharmacist");
        createUser(username, "password123", UserEnums.UserRole.PHARMACIST, "TC08 Pharmacist");

        User duplicate = new User(username, "newPassword", UserEnums.UserRole.PHARMACIST, "TC08 Pharmacist Dup", LocalDate.now());
        assertThrows(DataAccessException.class, () -> duplicate.createUser(duplicate));
    }

    @Test
    @DisplayName("TC-09: deleting a user removes it from the list")
    @Order(9)
    void testDeleteAccounts() {
        String username = uniqueUsername("tc09_pharmacist");
        createUser(username, "password123", UserEnums.UserRole.PHARMACIST, "TC09 Pharmacist");

        User created = findUserByUsername(username);
        assertNotNull(created);

        User.deleteUserById(created.getId());
        usernamesToCleanup.remove(username);

        assertNull(findUserByUsername(username));
    }

    @Test
    @DisplayName("TC-10: admin role can access every package")
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
    @DisplayName("TC-11: manager access matches policy")
    @Order(11)
    void testManagerAccess() {
        User manager = new User("manager1", "password123", UserEnums.UserRole.MANAGER, "Manager User", LocalDate.now());
        Session session = new Session(manager);

        assertTrue(session.hasAccess(UserEnums.UserRole.MANAGER, UserEnums.UserAccess.CUST));
        assertTrue(session.hasAccess(UserEnums.UserRole.MANAGER, UserEnums.UserAccess.TEMPLATES));
        assertTrue(session.hasAccess(UserEnums.UserRole.MANAGER, UserEnums.UserAccess.RPT));
        assertFalse(session.hasAccess(UserEnums.UserRole.MANAGER, UserEnums.UserAccess.SALES));
    }

    @Test
    @DisplayName("TC-12: pharmacist can access sales/stock but not user admin")
    @Order(12)
    void testPharmacistAccess() {
        User pharmacist = new User("pharmacist1", "password123", UserEnums.UserRole.PHARMACIST, "Pharmacist User", LocalDate.now());
        Session session = new Session(pharmacist);

        assertTrue(session.hasAccess(UserEnums.UserRole.PHARMACIST, UserEnums.UserAccess.SALES));
        assertTrue(session.hasAccess(UserEnums.UserRole.PHARMACIST, UserEnums.UserAccess.STOCK));
        assertFalse(session.hasAccess(UserEnums.UserRole.PHARMACIST, UserEnums.UserAccess.USER));
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
