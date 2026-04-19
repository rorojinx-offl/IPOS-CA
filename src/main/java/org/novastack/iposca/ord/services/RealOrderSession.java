package org.novastack.iposca.ord.services;

public final class RealOrderSession {
    private static String email;
    private static String role;
    private static String merchantId;
    private static String sessionToken;

    private RealOrderSession() {
    }

    public static void set(String email, String role, String merchantId, String sessionToken) {
        RealOrderSession.email = email;
        RealOrderSession.role = role;
        RealOrderSession.merchantId = merchantId;
        RealOrderSession.sessionToken = sessionToken;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static String getMerchantId() {
        return merchantId;
    }

    public static String getSessionToken() {
        return sessionToken;
    }

    public static void clear() {
        email = null;
        role = null;
        merchantId = null;
        sessionToken = null;
    }
}
