package org.novastack.iposca.session;

import org.novastack.iposca.user.User;

/**
 * Class that holds the current session in memory throughout the lifetime of the application.
 * */
public final class SessionManager {
    /**
     * A {@link Session} object that stores the current user's session.
     * */
    private static Session currentSession;

    private SessionManager() {}

    /**
     * Starts a new session for the user.
     * @param user The user for whom the session is being started.
     * */
    public static void start(User user) {
        currentSession = new Session(user);
    }

    /**
     * Gets the current session in memory.
     * @return The current {@link Session} object.
     * */
    public static Session getCurrentSession() {
        return currentSession;
    }

    /**
     * Get the user associated with the current session.
     * @return The {@link User} associated with the current session.
     * */
    public static User getCurrentUser() {
        return currentSession == null ? null : currentSession.getCurrentUser();
    }

    /**
     * Upon logout, session timeout, or invalid session, this method is called to end the current session.
     * */
    public static void end() {
        if (currentSession != null) {
            currentSession.clear();
            currentSession = null;
        }
    }

}
