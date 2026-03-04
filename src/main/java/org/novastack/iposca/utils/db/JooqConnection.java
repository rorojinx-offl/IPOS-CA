package org.novastack.iposca.utils.db;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;

public class JooqConnection {
    public static DSLContext getDSLContext() {
        Connection conn = new SQLiteConnection().getConnection();
        return DSL.using(conn, SQLDialect.SQLITE);
    }
}
