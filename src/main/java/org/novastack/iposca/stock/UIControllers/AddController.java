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

public class AddController implements Initializable {

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
    private TextField markupRate;

    @FXML
    private Label markupRateWarning;

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

    private void clearWarnings () {
        nameWarning.setText("");
        packageTypeWarning.setText("");
        unitsWarning.setText("");
        unitPackWarning.setText("");
        bulkCostWarning.setText("");
        markupRateWarning.setText("");
        quantityWarning.setText("");
        stockLimitWarning.setText("");
    }

    @FXML
    void addStock(MouseEvent event) throws IOException {
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
        if (markupRate.getText().isEmpty()) {
            markupRateWarning.setText("Markup rate cannot be empty");
            allFieldsFilled = false;
        }
        if (!IValid.checkRate(markupRate.getText()) && !markupRate.getText().isEmpty()) {
            markupRateWarning.setText("Markup rate must be numerical value");
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
                    name.getText(),
                    StockEnums.ProductType.NON_IPOS.name(),
                    packageType.getValue(),
                    units.getValue(),
                    Integer.parseInt(unitsInAPack.getText()),
                    Float.parseFloat(bulkCost.getText()),
                    Integer.parseInt(markupRate.getText()),
                    Integer.parseInt(quantity.getText()),
                    Integer.parseInt(stockLimit.getText())
            );

            try {
                stock.createItem(stock);
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
