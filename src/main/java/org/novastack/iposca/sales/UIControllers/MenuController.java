package org.novastack.iposca.sales.UIControllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.sales.SaleService;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private VBox acButton;

    @FXML
    private VBox ocButton;


    @FXML
    void accountSale(MouseEvent event) throws IOException {
        Stage stage = (Stage) acButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/sales/salesAccount.fxml", "Sale for Account Holder");
    }


    @FXML
    void occasionalSale(MouseEvent event) throws IOException {
        Stage stage = (Stage) ocButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/sales/selectItems.fxml"));
        Parent root = loader.load();

        SelectController controller = loader.getController();
        controller.receive(null, SaleService.CartMode.GUEST);

        stage.setTitle("Select Items");
        stage.setScene(new javafx.scene.Scene(root));
        stage.show();
    }

    @FXML
    void highlight(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.RED);
        option.setCursor(Cursor.HAND);
    }

    @FXML
    void restoreCol(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.BLACK);
        option.setCursor(Cursor.DEFAULT);
    }

    @FXML
    void returnToParent(MouseEvent event) throws IOException {
        Stage stage = (Stage) acButton.getScene().getWindow();
        CommonCalls.dashboardInit(stage);
    }
}
