package org.novastack.iposca.stock;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.novastack.iposca.utils.db.JooqConnection;
import schema.tables.records.StockTableRecord;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import static org.novastack.iposca.stock.Stock.*;
import static schema.tables.StockTable.STOCK_TABLE;

public class TestJooq {
    public static void main(String[] args) {
        DSLContext ctx = JooqConnection.getDSLContext();


       /* Stock stock= new Stock("Paracetamol","IPOS","Box", "Caps", 20, 0.10f, 10, 20, 20000);
        //createItem("Aspirin","IPOS","Box", "Caps", 20, 0.50f, 12453);
      //  stock.createItem(stock);


        ArrayList<Stock> meow = Stock.getLowStock();
        if (meow.isEmpty() || meow==null) {
            System.out.println("No meow");
            return;
        }

        System.out.println(meow.getFirst().getName());
        System.out.println(meow.getFirst().getQuantity()); */

    }

    private enum ProductType {
        IPOS, NON_IPOS
    }

}
