package org.novastack.iposca.stock.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jooq.exception.DataAccessException;
import org.novastack.iposca.stock.Stock;
import org.novastack.iposca.stock.StockEnums;
import org.novastack.iposca.utils.ui.CommonCalls;
import org.novastack.iposca.utils.ui.IValid;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        packageType.getItems().addAll(StockEnums.PackageType.BOX.name(), StockEnums.PackageType.BOTTLE.name());
        units.getItems().addAll(StockEnums.UnitType.CAPS.name(), StockEnums.UnitType.ML.name(), StockEnums.UnitType.OTHER.name());
    }

    @FXML
    private Button backButton;

    @FXML
    private TextField bulkCost;

    @FXML
    private Label bulkCostWarning;

    @FXML
    private TextField name;

    @FXML
    private Label nameWarning;

    @FXML
    private ChoiceBox<String> packageType;

    @FXML
    private Label packageTypeWarning;

    @FXML
    private TextField quantity;

    @FXML
    private Label quantityWarning;

    @FXML
    private TextField stockLimit;

    @FXML
    private Label stockLimitWarning;

    @FXML
    private Button submitButton;

    @FXML
    private Label unitPackWarning;

    @FXML
    private ChoiceBox<String> units;

    @FXML
    private TextField unitsInAPack;

    @FXML
    private Label unitsWarning;

    private int id;

    public void receiver(Stock stock){
        id= stock.getId();
        name.setText(stock.getName());
        packageType.getSelectionModel().select(stock.getPackageType());
        units.getSelectionModel().select(stock.getUnits());
        unitsInAPack.setText(String.valueOf(stock.getUnitsInAPack()));
        bulkCost.setText(String.valueOf(stock.getBulkCost()));
        quantity.setText(String.valueOf(stock.getQuantity()));
        stockLimit.setText(String.valueOf(stock.getStockLimit()));

    }

    private void clearWarnings () {
        nameWarning.setText("");
        packageTypeWarning.setText("");
        unitsWarning.setText("");
        unitPackWarning.setText("");
        bulkCostWarning.setText("");
        quantityWarning.setText("");
        stockLimitWarning.setText("");
    }

    @FXML
    void editStock(MouseEvent event) throws IOException {
        boolean allFieldsFilled = true;
        clearWarnings();

        if (name.getText().isEmpty()) {
            nameWarning.setText("Name cannot be empty");
            allFieldsFilled = false;
        }
        if (packageType.getSelectionModel().isEmpty()) {
            packageTypeWarning.setText("Package Type cannot be empty");
            allFieldsFilled = false;
        }
        if (units.getSelectionModel().isEmpty()) {
            unitsWarning.setText("Unit Type cannot be empty");
            allFieldsFilled = false;
        }
        if (unitsInAPack.getText().isEmpty()) {
            unitPackWarning.setText("Units In Pack cannot be empty");
            allFieldsFilled = false;
        }
        if (!IValid.checkInt(unitsInAPack.getText()) && !unitsInAPack.getText().isEmpty()) {
            unitPackWarning.setText("Units in a pack must be a numerical value");
            allFieldsFilled = false;
        }
        if (bulkCost.getText().isEmpty()) {
            bulkCostWarning.setText("Bulk Cost cannot be empty");
            allFieldsFilled = false;
        }
        if (!IValid.checkCreditLimit(bulkCost.getText()) && !bulkCost.getText().isEmpty()) {
            bulkCostWarning.setText("Bulk cost must be a numerical value");
            allFieldsFilled = false;
        }
        if (quantity.getText().isEmpty()) {
            quantityWarning.setText("Quantity cannot be empty");
            allFieldsFilled = false;
        }
        if (!IValid.checkInt(quantity.getText()) && !quantity.getText().isEmpty()) {
            quantityWarning.setText("Quantity must be a numerical value");
            allFieldsFilled = false;
        }
        if (stockLimit.getText().isEmpty()) {
            stockLimitWarning.setText("Stock Limit cannot be empty");
            allFieldsFilled = false;
        }
        if (!IValid.checkInt(stockLimit.getText()) && !stockLimit.getText().isEmpty()) {
            stockLimitWarning.setText("Stock Limit must be a numerical value");
            allFieldsFilled = false;
        }

        if (allFieldsFilled) {
            boolean ok = new CommonCalls().openConfirmationDialog("Are you sure you want to add this stock item?");
            if (!ok) {
                return;
            }

            Stock stock = new Stock(
                    id,
                    name.getText(),
                    StockEnums.ProductType.NON_IPOS.name(),
                    packageType.getValue(),
                    units.getValue(),
                    Integer.parseInt(unitsInAPack.getText()),
                    Float.parseFloat(bulkCost.getText()),
                    Integer.parseInt(quantity.getText()),
                    Integer.parseInt(stockLimit.getText())
            );

            try {
                stock.editStock(stock);
            } catch (DataAccessException e) {
                new CommonCalls().openErrorDialog(e.getMessage());
            }

            returnToParent(event);
        }
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/stock/StockManagement.fxml", "Stock");
    }
}
