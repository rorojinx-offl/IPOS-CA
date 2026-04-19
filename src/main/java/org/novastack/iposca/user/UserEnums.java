package org.novastack.iposca.user;

public class UserEnums {
    /**
     * The available roles for a user.
     * */
    public enum UserRole {
        ADMIN, PHARMACIST, MANAGER
    }

    /**
     * The packages/functionality that a user has access to.
     * */
    public enum UserAccess {
        ORD, USER, CUST, TEMPLATES, STOCK, SALES, RPT
    }
}