package experimental.database;

import org.jooq.DSLContext;
import static experimental.database.generated.tables.Company.COMPANY;

public class JooqTest {
    static void main() {
        DSLContext ctx = JooqConnection.getDSLContext();
        selectQuery(ctx);
    }

    private static void insertQuery(DSLContext ctx) {
        ctx.insertInto(COMPANY)
                .set(COMPANY.NAME, "Rohit")
                .set(COMPANY.AGE, 29)
                .set(COMPANY.ADDRESS, "London")
                .set(COMPANY.SALARY, 90000f)
                .execute();
    }

    private static void selectQuery(DSLContext ctx) {
        ctx.select(COMPANY.AGE)
                .from(COMPANY)
                .where(COMPANY.NAME.eq("Rohit"))
                .execute();
    }
}