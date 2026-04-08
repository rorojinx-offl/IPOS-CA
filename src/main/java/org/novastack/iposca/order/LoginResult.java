package org.novastack.iposca.order;

public class LoginResult {
    private final String username;
    private final String role;
    private final String merchantId;
    private final String sessionToken;

    public LoginResult(String username, String role, String merchantId, String sessionToken) {
        this.username = username;
        this.role = role;
        this.merchantId = merchantId;
        this.sessionToken = sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}