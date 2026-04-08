package org.novastack.iposca.config;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.AppConfig.APP_CONFIG;

public class AppConfig {
    public enum ConfigKey{
        VAT, MERCHANT_NAME, MERCHANT_ADDRESS, MERCHANT_EMAIL, MERCHANT_LOGO
    }
    private ConfigKey key;
    private byte[] value;

    public AppConfig(ConfigKey key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public ConfigKey getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public void configure(AppConfig appConfig) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(APP_CONFIG)
                .set(APP_CONFIG.KEY, appConfig.getKey().name())
                .set(APP_CONFIG.VALUE, appConfig.getValue())
                .onConflict(APP_CONFIG.KEY)
                .doUpdate()
                .set(APP_CONFIG.VALUE, appConfig.getValue())
                .execute();
    }

    public static byte[] get(ConfigKey key) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(APP_CONFIG)
                .where(APP_CONFIG.KEY.eq(key.name()))
                .fetchOptional()
                .map(record -> record.getValue())
                .orElse(null);
    }
}
