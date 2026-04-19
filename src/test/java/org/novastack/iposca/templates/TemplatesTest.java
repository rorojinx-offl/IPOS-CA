package org.novastack.iposca.templates;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.session.Session;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.utils.db.JooqConnection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static schema.tables.AppConfig.APP_CONFIG;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TemplatesTest {
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private final Map<AppConfig.ConfigKey, byte[]> configBackup = new EnumMap<>(AppConfig.ConfigKey.class);
    private boolean configBackupCaptured;

    @RegisterExtension
    static TestWatcher testWatcher = new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
            System.out.println(GREEN + "SUCCESS: " + context.getDisplayName() + RESET);
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            String reason = cause == null ? "Unknown error" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
            System.out.println(RED + "ERROR: " + context.getDisplayName() + " -> " + reason + RESET);
        }
    };

    @AfterEach
    void cleanup() {
        if (!configBackupCaptured) {
            return;
        }

        DSLContext ctx = JooqConnection.getDSLContext();
        for (Map.Entry<AppConfig.ConfigKey, byte[]> entry : configBackup.entrySet()) {
            AppConfig.ConfigKey key = entry.getKey();
            byte[] value = entry.getValue();

            if (value == null) {
                ctx.deleteFrom(APP_CONFIG)
                        .where(APP_CONFIG.KEY.eq(key.name()))
                        .execute();
                continue;
            }

            AppConfig config = new AppConfig(key, value);
            config.configure(config);
        }

        configBackup.clear();
        configBackupCaptured = false;
    }

    @Test
    @DisplayName("TC-01: saves merchant identity details to app config")
    @Order(1)
    void testMerchantIdentityConfigIsSaved() {
        backupConfig(
                AppConfig.ConfigKey.MERCHANT_NAME,
                AppConfig.ConfigKey.MERCHANT_ADDRESS,
                AppConfig.ConfigKey.MERCHANT_EMAIL,
                AppConfig.ConfigKey.MERCHANT_LOGO
        );

        String suffix = String.valueOf(System.nanoTime());
        String merchantName = "TC01 Pharmacy " + suffix;
        String merchantAddress = "TC01 Address " + suffix;
        String merchantEmail = "tc01_" + suffix + "@test.local";
        byte[] merchantLogo = ("logo_" + suffix).getBytes();

        saveConfig(AppConfig.ConfigKey.MERCHANT_NAME, AppConfigAPI.encodeString(merchantName));
        saveConfig(AppConfig.ConfigKey.MERCHANT_ADDRESS, AppConfigAPI.encodeString(merchantAddress));
        saveConfig(AppConfig.ConfigKey.MERCHANT_EMAIL, AppConfigAPI.encodeString(merchantEmail));
        saveConfig(AppConfig.ConfigKey.MERCHANT_LOGO, merchantLogo);

        assertTrue(AppConfig.configExists());
        assertEquals(merchantEmail, AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_EMAIL)));
        assertArrayEquals(merchantLogo, AppConfig.get(AppConfig.ConfigKey.MERCHANT_LOGO));
    }

    @Test
    @DisplayName("TC-02: templates package is available to the current app roles")
    @Order(2)
    void testTemplateAccessByRolePolicy() {
        Session adminSession = new Session(new User("tc04_admin", "password123", UserEnums.UserRole.ADMIN, "TC04 Admin", LocalDate.now()));
        Session managerSession = new Session(new User("tc04_manager", "password123", UserEnums.UserRole.MANAGER, "TC04 Manager", LocalDate.now()));
        Session pharmacistSession = new Session(new User("tc04_pharmacist", "password123", UserEnums.UserRole.PHARMACIST, "TC04 Pharmacist", LocalDate.now()));

        for (UserEnums.UserRole role : Arrays.asList(
                UserEnums.UserRole.ADMIN,
                UserEnums.UserRole.MANAGER,
                UserEnums.UserRole.PHARMACIST
        )) {
            assertTrue(adminSession.hasAccess(role, UserEnums.UserAccess.TEMPLATES));
        }
    }

    private void backupConfig(AppConfig.ConfigKey... keys) {
        configBackup.clear();
        for (AppConfig.ConfigKey key : keys) {
            configBackup.put(key, cloneBytes(AppConfig.get(key)));
        }
        configBackupCaptured = true;
    }

    private void saveConfig(AppConfig.ConfigKey key, byte[] value) {
        AppConfig config = new AppConfig(key, value);
        config.configure(config);
    }

    private byte[] cloneBytes(byte[] value) {
        return value == null ? null : value.clone();
    }
}
