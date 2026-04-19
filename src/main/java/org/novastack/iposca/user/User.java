package org.novastack.iposca.user;

import org.jooq.DSLContext;
import org.mindrot.jbcrypt.BCrypt;
import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.session.SessionManager;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.UserRecord;

import java.time.LocalDate;
import java.util.List;

import static schema.tables.User.USER;

/**
 * Class that represents a user in the system.
 * */
public class User {
    private int id;
    private String username;
    private String password;
    private UserEnums.UserRole role;
    private String fullName;
    private LocalDate createdAt;

    /**
     * Constructor for the User class that is used to authenticate and fetch all users.
     * @param id The ID of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role The role of the user.
     * @param fullName The full name of the user.
     * @param createdAt The date and time when the user was created.
     * */
    public User(int id, String username, String password, UserEnums.UserRole role, String fullName, LocalDate createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    /**
     * Constructor for the User class that is used to create users.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param role The role of the user.
     * @param fullName The full name of the user.
     * @param createdAt The date and time when the user was created.
     * */
    public User(String username, String password, UserEnums.UserRole role, String fullName, LocalDate createdAt) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    /**
     * Adds a new user to the database.
     * @param user The user to be added.
     * */
    public void createUser(User user) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(USER)
                .set(USER.USERNAME, user.getUsername())
                .set(USER.PASSWORD, hashPassword(user.getPassword()))
                .set(USER.ROLE, user.getRole().name())
                .set(USER.FULL_NAME, user.getFullName())
                .set(USER.CREATED_AT, user.getCreatedAt().toString())
                .execute();
    }

    /**
     * Fetches all users from the database.
     * @return A list of {@link User} objects representing all users.
     * */
    public static List<User> getAllUsers() {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(USER)
                .orderBy(USER.ID.asc())
                .fetch(record -> new User(
                        record.getId(),
                        record.getUsername(),
                        record.getPassword(),
                        UserEnums.UserRole.valueOf(record.getRole()),
                        record.getFullName(),
                        LocalDate.parse(record.getCreatedAt())
                ));
    }

    /**
     * Deletes a user from the database based on their ID.
     * @param id The ID of the user to be deleted.
     * */
    public static void deleteUserById(int id) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(USER)
                .where(USER.ID.eq(id))
                .execute();
    }

    /**
     * Updates the role of a user in the database.
     * @param id The ID of the user to be updated.
     * @param role The new role of the user.
     * @throws AuthenticationException If the user is trying to change their own role while logged in.
     * */
    public static void updateUserRole(int id, UserEnums.UserRole role) throws AuthenticationException {
        if (SessionManager.getCurrentSession().getCurrentUser().getId() == id) {
            throw new AuthenticationException("Cannot change your own role while logged in!");
        }
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(USER)
                .set(USER.ROLE, role.name())
                .where(USER.ID.eq(id))
                .execute();
    }

    /**
     * Authenticates the user by getting the user record from the database, then verifying the user's existence. Then,
     * it verifies the user's password by checking the hashed password in the database against the plaintext password.
     * If the user is authenticated, it returns a new {@link User} object so that a session can be started.
     * @param username The username of the user to be authenticated.
     * @param password The password of the user to be authenticated.
     * @return A new {@link User} object if the user is authenticated.
     * @throws AuthenticationException If the user is not authenticated.
     * */
    public static User authenticateUser(String username, String password) throws AuthenticationException {
        DSLContext ctx = JooqConnection.getDSLContext();
        UserRecord record = ctx.selectFrom(USER)
                .where(USER.USERNAME.eq(username))
                .fetchOne();

        if (record == null) {
            throw new AuthenticationException("User doesn't exist!");
        }

        if (!verifyPassword(password, record.getPassword())) {
            throw new AuthenticationException("Invalid Password!");
        }

        return new User(
                record.getId(),
                record.getUsername(),
                record.getPassword(),
                UserEnums.UserRole.valueOf(record.getRole()),
                record.getFullName(),
                LocalDate.parse(record.getCreatedAt())
        );
    }

    /**
     * Hashes a plaintext password using BCrypt.
     * @param plaintextPwd The plaintext password to be hashed.
     * @return The hashed password as a string.
     * */
    private String hashPassword(String plaintextPwd) {
        return BCrypt.hashpw(plaintextPwd, BCrypt.gensalt());
    }

    /**
     * Verifies a plaintext password against a hashed password using BCrypt.
     * @param plaintextPwd The plaintext password to be verified.
     * @param hashedPwd The hashed password to be verified against.
     * @return True if the plaintext password matches the hashed password, false otherwise.
     * */
    private static boolean verifyPassword(String plaintextPwd, String hashedPwd) {
        return BCrypt.checkpw(plaintextPwd, hashedPwd);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public UserEnums.UserRole getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}