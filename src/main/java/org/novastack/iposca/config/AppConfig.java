package org.novastack.iposca.config;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import static schema.tables.AppConfig.APP_CONFIG;

public class AppConfig {
    public enum ConfigKey{
        VAT
    }
    private ConfigKey key;
    private String value;

    public AppConfig(ConfigKey key, String value) {
        this.key = key;
        this.value = value;
    }

    public ConfigKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void configure(AppConfig appConfig) {
        DSLContext ctx = JooqConnection.getDSLContext();
        switch (appConfig.getKey()) {
            case ConfigKey.VAT:{
                ctx.insertInto(APP_CONFIG)
                        .set(APP_CONFIG.KEY, appConfig.getKey().name())
                        .set(APP_CONFIG.VALUE, appConfig.getValue())
                        .onConflict(APP_CONFIG.KEY)
                        .doUpdate()
                        .set(APP_CONFIG.VALUE, appConfig.getValue())
                        .execute();
            }
        }
    }
}
