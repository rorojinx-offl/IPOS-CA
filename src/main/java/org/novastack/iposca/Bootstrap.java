package org.novastack.iposca;

import org.novastack.iposca.utils.db.DDLEngine;
import org.novastack.iposca.utils.db.SQLiteConnection;
import org.novastack.iposca.utils.db.spawn.*;

import java.sql.Connection;

public class Bootstrap {
    public static void init() {
        dbInit();
    }

    private static void dbInit() {
        Connection connection = new SQLiteConnection().getConnection();
        DDLEngine ddl = new DDLEngine();

        try {
            AppConfig.create(connection, ddl);
            Customer.create(connection, ddl);
            CustomerCharge.create(connection, ddl);
            CustomerDebt.create(connection, ddl);
            CustomerMonthlyBalance.create(connection, ddl);
            CustomerMonthlySpend.create(connection, ddl);
            CustomerReminder.create(connection, ddl);
            CustomerRepayment.create(connection, ddl);
            FixedDiscount.create(connection, ddl);
            FlexiDiscount.create(connection, ddl);
            Sale.create(connection, ddl);
            SaleItem.create(connection, ddl);
            Stock.create(connection, ddl);
            User.create(connection, ddl);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
