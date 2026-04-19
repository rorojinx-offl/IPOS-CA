package org.novastack.iposca.ord.services;

public final class MockOrderSession {
    private static Integer merchantId;
    private static String username;
    private static String merchantName;

    private MockOrderSession() {
    }

    public static void set(Integer merchantId, String username, String merchantName) {
        MockOrderSession.merchantId = merchantId;
        MockOrderSession.username = username;
        MockOrderSession.merchantName = merchantName;
    }

    public static Integer getMerchantId() {
        return merchantId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getMerchantName() {
        return merchantName;
    }

    public static void clear() {
        merchantId = null;
        username = null;
        merchantName = null;
    }
}
