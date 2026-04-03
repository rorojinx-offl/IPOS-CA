package org.novastack.iposca.user;

import java.util.*;

import static java.util.Map.entry;

public class Session {
    private User user;
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
                    UserEnums.UserAccess.TEMPLATES
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

    public boolean hasAccess(UserEnums.UserRole role, UserEnums.UserAccess access) {
        return roleAccessMap.get(role).contains(access);
    }

    public void clear() {
        user = null;
    }
}
