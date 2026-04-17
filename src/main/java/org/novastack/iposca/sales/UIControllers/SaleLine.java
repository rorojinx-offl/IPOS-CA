package org.novastack.iposca.sales.UIControllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import org.novastack.iposca.stock.Stock;

public class SaleLine {
    private final ObjectProperty<Stock> product = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);
    private final ReadOnlyDoubleWrapper price = new ReadOnlyDoubleWrapper();
    private final ReadOnlyDoubleWrapper subtotal = new ReadOnlyDoubleWrapper();

    public SaleLine(Stock product, int quantity) {
        this.product.set(product);
        this.quantity.set(quantity);

        price.bind(Bindings.createDoubleBinding(
                () -> this.product.get() == null ? 0.0 : this.product.get().getBulkCost(), this.product
        ));

        subtotal.bind(Bindings.createDoubleBinding(
                () -> getUnitPrice() * getQuantity(), this.quantity, this.price
        ));
    }

    public Stock getProduct() {
        return product.get();
    }

    public ObjectProperty<Stock> productProperty() {
        return product;
    }

    public void setProduct(Stock product) {
        this.product.set(product);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getUnitPrice() {
        return price.get();
    }

    public ReadOnlyDoubleProperty unitPriceProperty() {
        return price.getReadOnlyProperty();
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public ReadOnlyDoubleProperty subtotalProperty() {
        return subtotal.getReadOnlyProperty();
    }
}
