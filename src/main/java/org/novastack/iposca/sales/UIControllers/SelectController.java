package org.novastack.iposca.sales.UIControllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.exceptions.InvalidOperation;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SelectController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProducts();
        setupSearch();
        setupCartTable();
    }

    @FXML
    private Button backButton;

    private Customer customer;

    @FXML
    private TableView<SaleLine> cart;

    @FXML
    private TableColumn<SaleLine, Number> price;

    @FXML
    private TableColumn<SaleLine, String> productName;

    @FXML
    private TableColumn<SaleLine, Integer> quantity;

    @FXML
    private TableColumn<SaleLine, Void> remove;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<Stock> searchResults;

    @FXML
    private TableColumn<SaleLine, Number> subtotal;

    @FXML
    private Label total;

    private final ObservableList<Stock> allProducts = FXCollections.observableArrayList();
    private final ObservableList<SaleLine> cartItems = FXCollections.observableArrayList();

    public void setCustomer(Customer cust) {
        customer = cust;
        System.out.println(customer.getName());
    }

    @FXML
    void addToCart(MouseEvent event) throws IOException {
        Stock product = searchResults.getSelectionModel().getSelectedItem();
        if (product == null) {
            new CommonCalls().openErrorDialog("Please select a product!");
            return;
        }

        for (SaleLine item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                if (item.getQuantity() < product.getQuantity()) {
                    item.setQuantity(item.getQuantity() + 1);
                    updateTotals();
                } else {
                    String error = product.getQuantity() == 0 ? "This product is out of stock!" : "There is insufficient stock for this product!";
                    new CommonCalls().openErrorDialog(error);
                }
                return;
            }
        }

        SaleLine newLine = new SaleLine(product, 1);
        newLine.quantityProperty().addListener((obs, oldQ, newQ) -> updateTotals());
        newLine.subtotalProperty().addListener((obs, oldS, newS) -> updateTotals());

        cartItems.add(newLine);
        updateTotals();
    }

    @FXML
    void cardPayment(MouseEvent event) {

    }

    @FXML
    void creditPayment(MouseEvent event) throws IOException {
        SaleService.SaleDraft draft = null;

        try {
            draft = collectCart();
        } catch (Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
            return;
        }

        if (customer == null) {
            new CommonCalls().openErrorDialog("Unable to map customer to order!");
            returnToParent(event);
            return;
        }

        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/creditPay.fxml"));
        Parent root = loader.load();

        CreditController controller = loader.getController();
        controller.receiver(draft, customer);

        stage.setTitle("Pay with Merchant Credits");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/sales/salesAccount.fxml", "Sale for Account Holder");
    }

    private void loadProducts() {
        ArrayList<Stock> list = Stock.getAllStock();
        allProducts.setAll(list);
    }

    private void setupSearch() {
        FilteredList<Stock> filteredData = new FilteredList<>(allProducts, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()) {return true;}
                String filter = newVal.toLowerCase();
                return product.getName().toLowerCase().contains(filter);
            });
        });

        searchResults.setItems(filteredData);
    }

    private void setupCartTable() {
        cart.setItems(cartItems);
        productName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProduct().getName()));
        price.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty());
        quantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        subtotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty());

        setupSearchResults();
        setupColCurrency();
        setupColQuantitySpinner();
        setupColRemove();
    }

    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(SaleLine::getSubtotal).sum();
        double total = subtotal;

        this.total.setText(String.format("Total: £%.2f", total));
    }

    private void setupSearchResults() {
        searchResults.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Stock product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setText(null);
                } else {
                    setText(product.getName());
                }
            }
        });
    }

    private void setupColCurrency() {
        price.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("£%.2f", item.doubleValue()));
            }
        });

        subtotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("£%.2f", item.doubleValue()));
            }
        });
    }

    private void setupColQuantitySpinner() {
        quantity.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1,999, 1);
            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        SaleLine line = getTableView().getItems().get(getIndex());
                        if (newVal != null && line.getQuantity() != newVal) {
                            line.setQuantity(newVal);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    SaleLine line = getTableView().getItems().get(getIndex());
                    spinner.getValueFactory().setValue(line.getQuantity());
                    setGraphic(spinner);
                }
            }
        });
    }

    private void setupColRemove() {
        remove.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.setOnAction(event -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        SaleLine line = getTableView().getItems().get(getIndex());
                        cartItems.remove(line);
                        updateTotals();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });
    }

    private SaleService.SaleDraft collectCart() throws InvalidOperation {
        if (cart.getItems().isEmpty() || cartItems.isEmpty()) {
            throw new InvalidOperation("You cannot place an order without any items in the cart!");
        }

        List<SaleService.SaleItem> itemsTemp = cartItems.stream().map(line ->
                new SaleService.SaleItem(
                        null,
                        null,
                        line.getProduct().getId(),
                        line.getQuantity(),
                        (float) line.getUnitPrice(),
                        (float) line.getSubtotal()
                )).toList();

        ArrayList<SaleService.SaleItem> items = new ArrayList<>(itemsTemp);
        float total = (float) collectOrderTotal();

        return new SaleService.SaleDraft(customer.getCustomerID(), items, total);
    }

    private double collectOrderTotal() {
        double total = 0.0;
        for (SaleLine item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }
}