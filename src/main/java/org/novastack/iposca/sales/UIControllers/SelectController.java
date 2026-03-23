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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.exceptions.InvalidOperation;
import org.novastack.iposca.sales.PaymentService;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SelectController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        memberButtons.setManaged(false);
        guestButtons.setManaged(false);
        cardButtonGuest.setManaged(false);
        cardButtonMember.setManaged(false);
        cashButton.setManaged(false);
        credButton.setManaged(false);

        loadProducts();
        setupSearch();
        setupCartTable();
    }

    @FXML
    private Button backButton;

    private Customer customer;

    @FXML
    private Button cardButtonGuest;

    @FXML
    private Button cardButtonMember;

    @FXML
    private TableView<SaleLine> cart;

    @FXML
    private Button cashButton;

    @FXML
    private Button credButton;

    @FXML
    private GridPane guestButtons;

    @FXML
    private GridPane memberButtons;

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
    private ObservableList<SaleLine> cartItems = FXCollections.observableArrayList();
    private SaleService.CartMode currentMode;

    public void receive(Customer cust, SaleService.CartMode mode) {
        switch (mode) {
            case MEMBER -> {
                customer = cust;
                currentMode = mode;

                memberButtons.setVisible(true);
                memberButtons.setManaged(true);

                cardButtonMember.setVisible(true);
                cardButtonMember.setManaged(true);
                credButton.setVisible(true);
                credButton.setManaged(true);

            }
            case GUEST -> {
                customer = null;
                currentMode = mode;

                guestButtons.setVisible(true);
                guestButtons.setManaged(true);

                cardButtonGuest.setVisible(true);
                cardButtonGuest.setManaged(true);
                cashButton.setVisible(true);
                cashButton.setManaged(true);
            }
        }
    }

    public void cartCallback(SaleService.Callback cb) {
        if (cb != null) {
            customer = cb.customer();
            cartItems = cb.cartSession();
            currentMode = cb.mode();
            receive(customer, currentMode);

            cart.setItems(cartItems);
            updateTotals();
        }
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
    void cardPayment(MouseEvent event) throws IOException {
        SaleService.SaleDraft draft = null;

        try {
            draft = collectCart();
        } catch (Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
            return;
        }

        if (currentMode == SaleService.CartMode.MEMBER && customer == null) {
            new CommonCalls().openErrorDialog("Unable to map customer to order!");
            returnToParent(event);
            return;
        }

        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/cardPay.fxml"));
        Parent root = loader.load();

        CardController controller = loader.getController();
        controller.receive(customer, draft, cartItems, currentMode);

        stage.setTitle("Pay with a Card");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void cashPayment(MouseEvent event) throws IOException {
        SaleService.SaleDraft draft = null;

        try {
            draft = collectCart();
        } catch (Exception e) {
            new CommonCalls().openErrorDialog(e.getMessage());
            return;
        }

        boolean ok = new CommonCalls().openConfirmationDialog("Has the cash been paid?");
        if (!ok) {
            return;
        }

        SaleService.Sale sale = new SaleService.Sale(
                null,
                null,
                CustomerEnums.PaymentMethod.CASH.name(),
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                draft.totalAmount()
        );
        int saleID = SaleService.recordSale(sale);
        for (SaleService.SaleItem item : draft.items()) {
            SaleService.recordSaleItem(new SaleService.SaleItem(
                    null,
                    saleID,
                    item.productID(),
                    item.quantity(),
                    item.price(),
                    item.subtotal()
            ));
        }
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

        boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to charge credits?");
        if (!ok) {
            return;
        }

       try {
           PaymentService.processCreditPayment(draft, customer);
       } catch (Exception e) {
           new CommonCalls().openErrorDialog(e.getMessage());
           return;
       }

        SaleService.Sale sale = new SaleService.Sale(
                null,
                customer.getCustomerID(),
                CustomerEnums.PaymentMethod.CREDITS.name(),
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                draft.totalAmount()
        );
       int saleID = SaleService.recordSale(sale);
       for (SaleService.SaleItem item : draft.items()) {
           SaleService.recordSaleItem(new SaleService.SaleItem(
                   null,
                   saleID,
                   item.productID(),
                   item.quantity(),
                   item.price(),
                   item.subtotal()
           ));
       }
       SaleService.checkFlexiRateChange(customer, draft.totalAmount());

       Stage stage = (Stage) backButton.getScene().getWindow();
       new CommonCalls().traverse(stage, "/ui/sales/success.fxml", "Success");
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
        Integer customerID = currentMode == SaleService.CartMode.MEMBER ? customer.getCustomerID() : null;

        return new SaleService.SaleDraft(customerID, items, total);
    }

    private double collectOrderTotal() {
        double total = 0.0;
        for (SaleLine item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }
}