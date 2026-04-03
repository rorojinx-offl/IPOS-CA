package org.novastack.iposca;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.user.Session;
import org.novastack.iposca.user.User;
import org.novastack.iposca.user.UserEnums;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private Session session;

    public void receive(User user) {
        session = new Session(user);
        welcome.setText("Welcome, " + getForename(user.getFullName()));
        applyAccess(user);
    }

    private void applyAccess(User user) {
        setTileAccess(custButton, UserEnums.UserAccess.CUST, user);
        setTileAccess(ordButton, UserEnums.UserAccess.ORD, user);
        setTileAccess(rptButton, UserEnums.UserAccess.RPT, user);
        setTileAccess(saleButton, UserEnums.UserAccess.SALES, user);
        setTileAccess(stockButton, UserEnums.UserAccess.STOCK, user);
        setTileAccess(templatesButton, UserEnums.UserAccess.TEMPLATES, user);
        setTileAccess(userButton, UserEnums.UserAccess.USER, user);
    }

    private void setTileAccess(VBox tile, UserEnums.UserAccess access, User user) {
        boolean allowed = session.hasAccess(user.getRole(), access);

        if (allowed) {
            tile.setDisable(false);
            tile.setOpacity(1);
            tile.getStyleClass().remove("tile-disabled");
            if (!tile.getStyleClass().contains("tile-enabled")) {
                tile.getStyleClass().add("tile-enabled");
            }
            tile.setMouseTransparent(false);
            tile.setCursor(Cursor.HAND);
        } else {
            tile.setDisable(true);
            tile.setOpacity(0.45);
            tile.getStyleClass().add("tile-enabled");
            if (!tile.getStyleClass().contains("tile-disabled")) {
                tile.getStyleClass().add("tile-disabled");
            }
            tile.setMouseTransparent(true);
            tile.setCursor(Cursor.DEFAULT);
        }
    }

    private String getForename(String fullName) {
        String forename = fullName;

        if (fullName != null && !fullName.trim().isEmpty()) {
            forename = fullName.trim().split("\\s+")[0];
        }

        return forename;
    }

    @FXML
    private VBox custButton;

    @FXML
    private VBox logOutButton;

    @FXML
    private VBox ordButton;

    @FXML
    private VBox rptButton;

    @FXML
    private VBox saleButton;

    @FXML
    private VBox stockButton;

    @FXML
    private VBox templatesButton;

    @FXML
    private VBox userButton;

    @FXML
    private Label welcome;

    @FXML
    void cust(MouseEvent event) throws IOException {
        Stage stage = (Stage) custButton.getScene().getWindow();
        User user = session.getCurrentUser();
        if (user == null) {
            new CommonCalls().traverse(stage, "/ui/login/login.fxml", "Login");
            return;
        }

        if (!session.hasAccess(user.getRole(), UserEnums.UserAccess.CUST)) {
            return;
        }
        new CommonCalls().traverse(stage, "/ui/cust/custMenu.fxml", "Customer Portal");
    }

    @FXML
    void highlight(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.RED);
        option.setCursor(Cursor.HAND);
    }

    @FXML
    void logout(MouseEvent event) throws IOException {
        session.clear();
        Stage stage = (Stage) logOutButton.getScene().getWindow();
        new CommonCalls().traverse(stage, "/ui/login/login.fxml", "Login");
    }

    @FXML
    void ord(MouseEvent event) {

    }

    @FXML
    void restoreCol(MouseEvent event) {
        VBox option = (VBox) event.getSource();
        Label childLabel = (Label) option.getChildren().get(1);
        childLabel.setTextFill(Color.BLACK);
        option.setCursor(Cursor.DEFAULT);
    }

    @FXML
    void rpt(MouseEvent event) {

    }

    @FXML
    void sales(MouseEvent event) throws IOException {
        Stage stage = (Stage) saleButton.getScene().getWindow();
        User user = session.getCurrentUser();
        if (user == null) {
            new CommonCalls().traverse(stage, "/ui/login/login.fxml", "Login");
            return;
        }

        if (!session.hasAccess(user.getRole(), UserEnums.UserAccess.SALES)) {
            return;
        }
        new CommonCalls().traverse(stage, "/ui/sales/salesMenu.fxml", "Sales Portal");
    }

    @FXML
    void stock(MouseEvent event) {

    }

    @FXML
    void templates(MouseEvent event) {

    }

    @FXML
    void user(MouseEvent event) {

    }

}
