package org.novastack.iposca.cust;

import org.jooq.DSLContext;
import org.novastack.iposca.utils.db.JooqConnection;

import java.util.ArrayList;

import static schema.tables.CustomerDebt.CUSTOMER_DEBT;

public class DAUtils {
    protected ArrayList<Float> checkBalances() {
        ArrayList<Float> balances = new ArrayList<>();
        DSLContext ctx = JooqConnection.getDSLContext();
        ctx.select(CUSTOMER_DEBT.BALANCE)
                .from(CUSTOMER_DEBT)
                .forEach(record -> {
                    balances.add(CUSTOMER_DEBT.BALANCE.getValue(record));
                });
        return balances;
    }
}
