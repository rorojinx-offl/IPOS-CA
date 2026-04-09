package org.novastack.iposca.utils.common;

import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;

public class TestAppConfig {
    static void main() {
        byte[] vat = AppConfigAPI.encodeInt(30);

        AppConfig appConfig = new AppConfig(org.novastack.iposca.config.AppConfig.ConfigKey.VAT,vat);
        appConfig.configure(appConfig);

        int vatRate = AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT));
        System.out.println(vatRate);

        byte[] mName = AppConfigAPI.encodeString("TestPharma");

        appConfig = new AppConfig(org.novastack.iposca.config.AppConfig.ConfigKey.MERCHANT_NAME,mName);
        appConfig.configure(appConfig);

        String merchantName = AppConfigAPI.decodeByteToString(AppConfig.get(AppConfig.ConfigKey.MERCHANT_NAME));
        System.out.println(merchantName);
    }
}
