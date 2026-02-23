package experimental.database;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;

public class JooqConnection {
    public static DSLContext getDSLContext() {
        ConnectionObject c = new ConnectionObject();
        Connection connection = c.getConnection();
        return DSL.using(connection, SQLDialect.SQLITE);
    }
}
