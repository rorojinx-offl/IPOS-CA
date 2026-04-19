package org.novastack.iposca.session;

import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;

import java.util.*;

import static java.util.Map.entry;

/**
 * Class that stores the current user's session.
 * */
public class Session {
    /**
     * The user information
     * */
    private User user;
    /**
     * A map that stores the access permissions for each role.
     * */
    private final Map<UserEnums.UserRole, ArrayList<UserEnums.UserAccess>> roleAccessMap = Map.ofEntries(
            entry(UserEnums.UserRole.ADMIN, new ArrayList<>(List.copyOf(Arrays.asList(UserEnums.UserAccess.values())))),
            entry(UserEnums.UserRole.PHARMACIST, new ArrayList<>(List.of(
                    UserEnums.UserAccess.ORD,
                    UserEnums.UserAccess.TEMPLATES,
                    UserEnums.UserAccess.CUST,
                    UserEnums.UserAccess.STOCK,
                    UserEnums.UserAccess.SALES,
                    UserEnums.UserAccess.RPT
            ))),
            entry(UserEnums.UserRole.MANAGER, new ArrayList<>(List.of(
                    UserEnums.UserAccess.RPT,
                    UserEnums.UserAccess.TEMPLATES,
                    UserEnums.UserAccess.CUST
            )))
    );

    public Session(User user) {
        this.user = user;
    }

    public User getCurrentUser() {
        return user;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    /**
     * Checks whether the user has access to a specific feature.
     * @param role The role of the user.
     * @param access The access to check.
     * @return True if the user has access, false otherwise.
     * */
    public boolean hasAccess(UserEnums.UserRole role, UserEnums.UserAccess access) {
        return roleAccessMap.get(role).contains(access);
    }

    public void clear() {
        user = null;
    }
}
