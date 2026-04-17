package org.novastack.iposca.config;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.AppConfigRecord;

import static schema.tables.AppConfig.APP_CONFIG;

/**
 * Class that manages the storage and retrieval of application configurations.
 * */
public class AppConfig {
    /**
     * A set of configurations that can be used in IPOS-CA.
     * */
    public enum ConfigKey{
        VAT, MERCHANT_NAME, MERCHANT_ADDRESS, MERCHANT_EMAIL, MERCHANT_LOGO
    }

    /**
     * The key stores a defined set of configurations which are encapsulated in the enum ConfigKey.
     * */
    private ConfigKey key;

    /**
     * The value stores the configuration value as a byte array as it is stored in the database as a BLOB which can
     * allow us to store any kind of data.
     * */
    private byte[] value;

    /**
     * Constructor for the AppConfig class.
     * @param key The key of the configuration.
     * @param value The value for the configuration.
     * */
    public AppConfig(ConfigKey key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Getter for the key.
     * @return The key of the configuration which returns {@link ConfigKey}
     * */
    public ConfigKey getKey() {
        return key;
    }

    /**
     * Getter for the value.
     * @return The value of the configuration which returns {@code byte[]}
     * */
    public byte[] getValue() {
        return value;
    }

    /**
     * Register a configuration into the database (upsert). {@link AppConfig} is passed in as a parameter and the config
     * key and the pertaining value are stored in the database. If a configuration key already exists, the value is
     * updated instead. The value is stored as a BLOB, and thus the value must be of type {@code byte[]}. The table
     * schema does not impose a NOT NULL constraint as the UI enforces that selectively via {@link org.novastack.iposca.templates.TemplatesController}
     * and {@link org.novastack.iposca.stock.UIControllers.RateController}. This method is best used with {@link AppConfigAPI}
     * that will encode the primitive values into a byte array. <br>
     * Example usage: <pre> {@code AppConfig appConfig = new AppConfig(ConfigKey.VAT, AppConfigAPI.encodeInt(10));
     * appConfig.configure(appConfig);} </pre>
     * @param appConfig The configuration to be stored in the database.
     * */
    public void configure(AppConfig appConfig) {
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.insertInto(APP_CONFIG)
                .set(APP_CONFIG.KEY, appConfig.getKey().name())
                .set(APP_CONFIG.VALUE, appConfig.getValue() == null ? new byte[0] : appConfig.getValue())
                .onConflict(APP_CONFIG.KEY)
                .doUpdate()
                .set(APP_CONFIG.VALUE, appConfig.getValue() == null ? new byte[0] : appConfig.getValue())
                .execute();
    }

    /**
     * Gets a configuration value from the database, with the given configuration key. If the configuration key does not
     * exist, it returns null. This method is best used with {@link AppConfigAPI} that will decode the primitive values
     * for use in the application. <br>
     * Example usage: <pre> {@code int vat = AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT));} </pre>
     * @param key The configuration key to retrieve the value for.
     * @return The value of the configuration key as {@code byte[]}, or {@code null} if the key does not exist.
     * */
    public static byte[] get(ConfigKey key) {
        DSLContext ctx = JooqConnection.getDSLContext();
        return ctx.selectFrom(APP_CONFIG)
                .where(APP_CONFIG.KEY.eq(key.name()))
                .fetchOptional()
                .map(record -> record.getValue())
                .orElse(null);
    }

    /**
     * Checks if a full set of configurations are present in the database. Even if a single configuration is missing,
     * the method returns false. For {@link org.novastack.iposca.templates.TemplatesController}, a false return value from
     * this function indicates a corrupted configuration. It then prompts the user to re-configure the application.
     * @return {@code boolean} to check if the configurations are present in the database.
     * */
    public static boolean configExists() {
        ConfigKey[] keys = ConfigKey.values();
        DSLContext ctx = JooqConnection.getDSLContext();
        for (ConfigKey key : keys) {
            AppConfigRecord record = ctx.selectFrom(APP_CONFIG).where(APP_CONFIG.KEY.eq(key.name())).fetchOne();
            // If the record is null, it means that the configuration is missing. But an exception is made for the logo and VAT as they are optional.
            if (record == null && key != ConfigKey.MERCHANT_LOGO && key != ConfigKey.VAT) {
                return false;
            }
        }
        return true;
    }
}
