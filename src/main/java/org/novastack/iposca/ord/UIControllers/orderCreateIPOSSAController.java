package org.novastack.iposca.ord.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.OrderSaFacade;
import org.novastack.iposca.ord.services.RealOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class orderCreateIPOSSAController {
    private final OrderSaFacade facade = new OrderSaFacade();

    @FXML
    private Button backButton;

    @FXML
    private TableView<ProductRow> catalogueTable;

    @FXML
    private TableColumn<ProductRow, String> productIdColumn;

    @FXML
    private TableColumn<ProductRow, String> nameColumn;

    @FXML
    private TableColumn<ProductRow, Double> unitPriceColumn;

    @FXML
    private TableColumn<ProductRow, Integer> stockColumn;

    @FXML
    private TableColumn<ProductRow, Integer> minimumStockColumn;

    @FXML
    private TableView<OrderItemRow> orderLinesTable;

    @FXML
    private TableColumn<OrderItemRow, String> orderProductIdColumn;

    @FXML
    private TableColumn<OrderItemRow, String> orderNameColumn;

    @FXML
    private TableColumn<OrderItemRow, Integer> orderQuantityColumn;

    @FXML
    private TableColumn<OrderItemRow, Double> orderUnitPriceColumn;

    @FXML
    private TableColumn<OrderItemRow, Double> orderTotalColumn;

    @FXML
    private TextField quantityField;

    @FXML
    private Label warning;

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockLevel"));
        minimumStockColumn.setCellValueFactory(new PropertyValueFactory<>("minimumStockLevel"));

        orderProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        orderNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        orderQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderMenuIPOSSA.fxml", "Order Menu (IPOS-SA)");
        } catch (IOException e) {
            warning.setText("Unable to return to order menu.");
        }
    }

    @FXML
    void loadCatalogue(ActionEvent event) {
        try {
            ObservableList<ProductRow> rows = FXCollections.observableArrayList();
            for (var p : facade.getProducts()) {
                rows.add(new ProductRow(p.productId(), p.name(), p.unitPrice(), p.stockLevel(), p.minimumStockLevel()));
            }
            catalogueTable.setItems(rows);
            warning.setText("");
        } catch (Exception e) {
            warning.setText(e.getMessage() == null ? "Unable to load catalogue." : e.getMessage());
        }
    }

    @FXML
    void addToOrder(ActionEvent event) {
        ProductRow selected = catalogueTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            warning.setText("Select a catalogue item first.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                warning.setText("Quantity must be greater than 0.");
                return;
            }
        } catch (Exception e) {
            warning.setText("Enter a valid quantity.");
            return;
        }

        int merged = quantity + existingQty(selected.productId);
        if (merged > selected.stockLevel) {
            warning.setText("Quantity exceeds available stock.");
            return;
        }

        upsert(selected, merged);
        warning.setText("");
        quantityField.clear();
    }

    @FXML
    void submitOrder(ActionEvent event) {
        if (orderLinesTable.getItems().isEmpty()) {
            warning.setText("Add at least one order line before submitting.");
            return;
        }

        List<OrderSaFacade.OrderItemDto> items = new ArrayList<>();
        for (OrderItemRow row : orderLinesTable.getItems()) {
            items.add(new OrderSaFacade.OrderItemDto(row.productId, row.quantity));
        }

        try {
            var submitted = facade.placeOrder(items);
            orderLinesTable.getItems().clear();
            warning.setTextFill(Color.GREEN);
            warning.setText("Order sent and pending approval. Order ID: " + submitted.orderId() + " Total: " + submitted.total());
        } catch (Exception e) {
            warning.setTextFill(Color.RED);
            warning.setText(e.getMessage() == null ? "Unable to submit order." : e.getMessage());
        }
    }

    private int existingQty(String productId) {
        for (OrderItemRow row : orderLinesTable.getItems()) {
            if (row.productId.equals(productId)) {
                return row.quantity;
            }
        }
        return 0;
    }

    private void upsert(ProductRow selected, int mergedQuantity) {
        int idx = -1;
        for (int i = 0; i < orderLinesTable.getItems().size(); i++) {
            if (orderLinesTable.getItems().get(i).productId.equals(selected.productId)) {
                idx = i;
                break;
            }
        }
        OrderItemRow merged = new OrderItemRow(
                selected.productId,
                selected.name,
                mergedQuantity,
                selected.unitPrice,
                Math.round(selected.unitPrice * mergedQuantity * 100.0) / 100.0
        );
        if (idx >= 0) {
            orderLinesTable.getItems().set(idx, merged);
        } else {
            orderLinesTable.getItems().add(merged);
        }
    }

    public static class ProductRow {
        private final String productId;
        private final String name;
        private final double unitPrice;
        private final int stockLevel;
        private final int minimumStockLevel;

        public ProductRow(String productId, String name, double unitPrice, int stockLevel, int minimumStockLevel) {
            this.productId = productId;
            this.name = name;
            this.unitPrice = unitPrice;
            this.stockLevel = stockLevel;
            this.minimumStockLevel = minimumStockLevel;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public int getStockLevel() {
            return stockLevel;
        }

        public int getMinimumStockLevel() {
            return minimumStockLevel;
        }
    }

    public static class OrderItemRow {
        private final String productId;
        private final String name;
        private final int quantity;
        private final double unitPrice;
        private final double total;

        public OrderItemRow(String productId, String name, int quantity, double unitPrice, double total) {
            this.productId = productId;
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }

        public String getProductId() {
            return productId;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public double getTotal() {
            return total;
        }
    }
}
