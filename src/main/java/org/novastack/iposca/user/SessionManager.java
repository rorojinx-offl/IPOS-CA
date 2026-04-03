package org.novastack.iposca.user;

public final class SessionManager {
    private static Session currentSession;

    private SessionManager() {}

    public static void start(User user) {
        currentSession = new Session(user);
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public static User getCurrentUser() {
        return currentSession == null ? null : currentSession.getCurrentUser();
    }

    public static void end() {
        if (currentSession != null) {
            currentSession.clear();
            currentSession = null;
        }
    }

}
