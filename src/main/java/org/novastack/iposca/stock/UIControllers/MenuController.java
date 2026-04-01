package org.novastack.iposca.stock.UIControllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.novastack.iposca.stock.Stock;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MenuController implements Initializable {



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lowStockObservableList.setAll(Stock.getLowStock());
        lowStockList.setItems(lowStockObservableList);
        setUpListView();
    }

    public void setUpListView(){
        lowStockList.setCellFactory(list -> new ListCell<Stock>() {
            @Override
            public void updateItem(Stock item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText(null);
                } else{
                    setText(item.getName());
                }
            }
        });
    }

    @FXML
    private ListView<Stock> lowStockList;
    private ObservableList<Stock> lowStockObservableList= FXCollections.observableArrayList();

    @FXML
    private VBox manageButton;

    @FXML
    void highlight(MouseEvent event) {

    }

    @FXML
    void manageStock(MouseEvent event) {

    }

    @FXML
    void restoreCol(MouseEvent event) {
    }
}


