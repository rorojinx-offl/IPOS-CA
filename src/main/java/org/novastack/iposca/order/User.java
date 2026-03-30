package org.novastack.iposca.order;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static org.jooq.impl.DSL.max;
import static schema.tables.Users.USERS;

public class User {
    private int userID;
    private String username;
    private String password;
    private String fullName;
    private String role;
    private int isActive;
    private int failedAttempts;

    public User(String username, String password, String fullName, String role, int isActive, int failedAttempts) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.failedAttempts = failedAttempts;
    }

    public User(int userID, String username, String password, String fullName, String role, int isActive, int failedAttempts) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.failedAttempts = failedAttempts;
    }

    public User() {}

    public void addUser(User user) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        ctx.insertInto(USERS)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.PASSWORD, user.getPassword())
                .set(USERS.FULL_NAME, user.getFullName())
                .set(USERS.ROLE, user.getRole())
                .set(USERS.IS_ACTIVE, user.getIsActive())
                .set(USERS.FAILED_ATTEMPTS, user.getFailedAttempts())
                .set(USERS.CREATED_AT, createdAt)
                .execute();
    }

    public ArrayList<User> getAllUsers() throws DataAccessException {
        ArrayList<User> users = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.selectFrom(USERS).fetch().forEach(record -> {
            users.add(new User(
                    USERS.ID.getValue(record),
                    USERS.USERNAME.getValue(record),
                    USERS.PASSWORD.getValue(record),
                    USERS.FULL_NAME.getValue(record),
                    USERS.ROLE.getValue(record),
                    USERS.IS_ACTIVE.getValue(record),
                    USERS.FAILED_ATTEMPTS.getValue(record)
            ));
        });
        return users;
    }

    public User getUser(int userID) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(USERS)
                .where(USERS.ID.eq(userID))
                .fetchOne(record -> new User(
                        USERS.ID.getValue(record),
                        USERS.USERNAME.getValue(record),
                        USERS.PASSWORD.getValue(record),
                        USERS.FULL_NAME.getValue(record),
                        USERS.ROLE.getValue(record),
                        USERS.IS_ACTIVE.getValue(record),
                        USERS.FAILED_ATTEMPTS.getValue(record)
                ));
    }

    public User getUserByUsername(String username) throws DataAccessException {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOne(record -> new User(
                        USERS.ID.getValue(record),
                        USERS.USERNAME.getValue(record),
                        USERS.PASSWORD.getValue(record),
                        USERS.FULL_NAME.getValue(record),
                        USERS.ROLE.getValue(record),
                        USERS.IS_ACTIVE.getValue(record),
                        USERS.FAILED_ATTEMPTS.getValue(record)
                ));
    }

    public int getLatestID() {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.select(max(USERS.ID))
                .from(USERS)
                .fetchOneInto(Integer.class);
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
