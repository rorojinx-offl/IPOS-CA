package org.novastack.iposca.user;

import org.jooq.DSLContext;
import org.mindrot.jbcrypt.BCrypt;
import org.novastack.iposca.exceptions.AuthenticationException;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.UserRecord;

import java.time.LocalDate;
import java.util.List;

import static schema.tables.User.USER;

public class User {
    private int id;
    private String username;
    private String password;
    private UserEnums.UserRole role;
    private String fullName;
    private LocalDate createdAt;

    public User(int id, String username, String password, UserEnums.UserRole role, String fullName, LocalDate createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

    public User(String username, String password, UserEnums.UserRole role, String fullName, LocalDate createdAt) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }

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

        return fromRecord(record);
    }

    public static List<User> getAllUsers() {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(USER)
                .orderBy(USER.ID.asc())
                .fetch(User::fromRecord);
    }

    public static void deleteUserById(int id) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.deleteFrom(USER)
                .where(USER.ID.eq(id))
                .execute();
    }

    public static void updateUserRole(int id, UserEnums.UserRole role) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.update(USER)
                .set(USER.ROLE, role.name())
                .where(USER.ID.eq(id))
                .execute();
    }

    private static User fromRecord(UserRecord record) {
        return new User(
                record.getId(),
                record.getUsername(),
                record.getPassword(),
                UserEnums.UserRole.valueOf(record.getRole()),
                record.getFullName(),
                LocalDate.parse(record.getCreatedAt())
        );
    }

    private String hashPassword(String plaintextPwd) {
        return BCrypt.hashpw(plaintextPwd, BCrypt.gensalt());
    }

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
