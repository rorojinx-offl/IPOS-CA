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
import org.novastack.iposca.config.AppConfig;
import org.novastack.iposca.config.AppConfigAPI;
import org.novastack.iposca.cust.customer.Customer;
import org.novastack.iposca.cust.customer.CustomerEnums;
import org.novastack.iposca.cust.plans.FixedDiscountPlan;
import org.novastack.iposca.cust.plans.FlexiDiscountPlan;
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

    @FXML
    private Label vtotal;

    @FXML
    private Label dtotal;

    private final ObservableList<Stock> allProducts = FXCollections.observableArrayList();
    private ObservableList<SaleLine> cartItems = FXCollections.observableArrayList();
    private SaleService.CartMode currentMode;
    /**
     * A record that stores the 3 different totals: Cart Sum, Total with VAT and Total with Discounts.
     * @param sum The sum of all the items in the cart.
     * @param vat The sum of all the items in the cart multiplied by the VAT rate.
     * @param discount The sum of all the items in the cart multiplied by the discount rate.
     * */
    public record Totals(float sum, float vat, float discount) {}

    /**
     * Receiver function that gets customer and cart mode from the parent. If the cart mode is MEMBER, then the customer
     * has to be set. If the cart mode is GUEST, then the customer has to be null. Depending on the cart mode, the buttons
     * are shown or hidden.
     * @param cust The customer object.
     * @param mode The cart mode.
     * */
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

    /**
     * Another receiver function that gets the cart session from another scene.
     * */
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
        controller.receive(customer, draft, cartItems, currentMode, new Totals(draft.totalAmount(), draft.totalWithTax(), draft.grandTotal()));

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
                draft.totalWithTax()
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

            Stock.minusStock(item.productID(), item.quantity());
        }

        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/success.fxml"));
        Parent root = loader.load();

        SuccessController controller = loader.getController();
        controller.receive(cartItems, null, SaleService.CartMode.GUEST, new Totals(draft.totalAmount(), draft.totalWithTax(), draft.grandTotal()));

        stage.setTitle("Payment Successful");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
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
                draft.grandTotal()
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

            Stock.minusStock(item.productID(), item.quantity());
        }
        SaleService.checkFlexiRateChange(customer, draft.grandTotal());

        Stage stage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/success.fxml"));
        Parent root = loader.load();

        SuccessController controller = loader.getController();
        controller.receive(cartItems, customer, SaleService.CartMode.MEMBER, new Totals(draft.totalAmount(), draft.totalWithTax(), draft.grandTotal()));

        stage.setTitle("Payment Successful");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        String fxmlPath = currentMode == SaleService.CartMode.MEMBER ? "/ui/sales/salesAccount.fxml" : "/ui/sales/salesMenu.fxml";

        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, fxmlPath, "Sale for Account Holder");
    }

    private void loadProducts() {
        ArrayList<Stock> list = Stock.getAllStockForSale();
        allProducts.setAll(list);
    }

    private void setupSearch() {
        FilteredList<Stock> filteredData = new FilteredList<>(allProducts, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
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

        int vatRate = AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT));
        double mult = 1 + (vatRate / 100.0);
        double vat = total * mult;

        this.total.setText(String.format("Total: £%.2f  ", total));
        this.vtotal.setText(String.format("Total with VAT: £%.2f  ", vat)); //Adjust for national VAT rate

        if (currentMode == SaleService.CartMode.MEMBER && customer != null) {
            int rate;
            if (customer.getDiscountPlan().equals(CustomerEnums.DiscountPlan.FIXED.name())) {
                rate = new FixedDiscountPlan().getCurrentDiscountRate(customer.getCustomerID());
            } else {
                rate = new FlexiDiscountPlan().getCurrentDiscountRate(customer.getCustomerID());
                if (rate == 0) {
                    return;
                }
            }

            double multiplier = 1.0 - (rate / 100.0);
            double discount = vat * multiplier;

            this.dtotal.setText(String.format("Grand Total After Discounts (%d%%): £%.2f", rate, discount));
        }
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
            private final Spinner<Integer> spinner = new Spinner<>(1, 999, 1);

            {
                spinner.setEditable(true);
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        SaleLine line = getTableView().getItems().get(getIndex());

                        if (newVal == null) {
                            return;
                        }

                        int available = line.getProduct().getQuantity();
                        if (newVal > available) {
                            spinner.getValueFactory().setValue(oldVal);
                            try {
                                new CommonCalls().openErrorDialog(available == 0 ? "This product is out of stock!" : "There is insufficient stock for this product!");
                            } catch (IOException e) {
                                return;
                            }
                            return;
                        }

                        if (line.getQuantity() != newVal) {
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
        Totals total = collectOrderTotal();
        Integer customerID = currentMode == SaleService.CartMode.MEMBER ? customer.getCustomerID() : null;

        return new SaleService.SaleDraft(customerID, items, total.sum(), total.vat(), total.discount());
    }

    private Totals collectOrderTotal() {
        double sum = 0.0;
        double vat = 0.0;
        double discount = 0.0;
        for (SaleLine item : cartItems) {
            sum += item.getSubtotal();
        }

        int vatRate = AppConfigAPI.decodeByteToInt(AppConfig.get(AppConfig.ConfigKey.VAT));
        double mult = 1 + (vatRate / 100.0);
        vat = sum * mult; //Adjust for national VAT rate

        if (currentMode == SaleService.CartMode.MEMBER && customer != null) {
            int rate;
            if (customer.getDiscountPlan().equals(CustomerEnums.DiscountPlan.FIXED.name())) {
                rate = new FixedDiscountPlan().getCurrentDiscountRate(customer.getCustomerID());
            } else {
                rate = new FlexiDiscountPlan().getCurrentDiscountRate(customer.getCustomerID());
            }

            double multiplier = 1.0 - (rate / 100.0);
            discount = vat * multiplier;
        }
        return new Totals((float) sum, (float) vat, (float) discount);
    }
}