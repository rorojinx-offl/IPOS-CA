package org.novastack.iposca.stock;

public class StockEnums {
    /**
     * The package type of the stock item. Can be a box or a bottle.
     * */
    public enum PackageType {
        BOX,BOTTLE
    }
    /**
     * The unit used for the stock item. Can be capsules, millilitres, or other.
     * */
    public enum UnitType {
        CAPS,ML,OTHER
    }
    /**
     * The source of the stock item. Can be IPOS or non-IPOS.
     * */
    public enum ProductType {
        IPOS,NON_IPOS
    }
}
