package org.novastack.iposca.stock.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class AddController implements Initializable {

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
    private ChoiceBox<?> packageType;

    @FXML
    private Label packageTypeWarning;

    @FXML
    private TextField quantity;

    @FXML
    private Label qunatityWarning;

    @FXML
    private TextField stockLimit;

    @FXML
    private Label stockLimitWarning;

    @FXML
    private Button submitButton;

    @FXML
    private Label unitPackWarning;

    @FXML
    private ChoiceBox<?> units;

    @FXML
    private TextField unitsInAPack;

    @FXML
    private Label unitsWarning;

    @FXML
    void addStock(MouseEvent event) {

    }

    @FXML
    void returnToParent(MouseEvent event) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
