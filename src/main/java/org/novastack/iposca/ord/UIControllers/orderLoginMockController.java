package org.novastack.iposca.ord.UIControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.novastack.iposca.ord.services.MockOrderSession;
import org.novastack.iposca.utils.ui.CommonCalls;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class orderLoginMockController {
    private static final String MOCK_DB_URL = "jdbc:sqlite:ipos-sa-mock-database.db";

    @FXML
    private Button backButton;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginButton;

    @FXML
    private Label warning;

    @FXML
    void login(ActionEvent event) {
        String enteredUsername = username.getText() == null ? "" : username.getText().trim();
        String enteredPassword = password.getText() == null ? "" : password.getText().trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            warning.setTextFill(Color.RED);
            warning.setText("Please enter username and password.");
            return;
        }

        try {
            MockLoginResult result = findMerchant(enteredUsername, enteredPassword);
            if (!result.found()) {
                warning.setTextFill(Color.RED);
                warning.setText("Invalid username or password.");
                return;
            }

            if (!"NORMAL".equalsIgnoreCase(result.accountStatus())) {
                warning.setTextFill(Color.RED);
                warning.setText("Account status is not NORMAL.");
                return;
            }

            MockOrderSession.set(result.merchantId(), result.username(), result.merchantName());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderMenuMock.fxml", "Order Menu (Mock)");
        } catch (SQLException e) {
            warning.setTextFill(Color.RED);
            warning.setText("Unable to access mock database.");
        } catch (IOException e) {
            warning.setTextFill(Color.RED);
            warning.setText("Login succeeded but menu page could not be opened.");
        }
    }

    @FXML
    void back(ActionEvent event) {
        try {
            MockOrderSession.clear();
            Stage stage = (Stage) backButton.getScene().getWindow();
            new CommonCalls().traverse(stage, "/ui/ord/orderModeSelect.fxml", "Order Mode Select");
        } catch (IOException e) {
            warning.setText("Unable to return to mode select.");
        }
    }

    private MockLoginResult findMerchant(String enteredUsername, String enteredPassword) throws SQLException {
        String sql = """
                SELECT merchant_id, username, merchant_name, account_status
                FROM ord_merchants
                WHERE username = ? AND password = ?
                LIMIT 1
                """;

        try (Connection connection = DriverManager.getConnection(MOCK_DB_URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, enteredUsername);
            statement.setString(2, enteredPassword);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new MockLoginResult(
                            true,
                            rs.getInt("merchant_id"),
                            rs.getString("username"),
                            rs.getString("merchant_name"),
                            rs.getString("account_status")
                    );
                }
            }
        }

        return new MockLoginResult(false, null, null, null, null);
    }

    private record MockLoginResult(boolean found, Integer merchantId, String username, String merchantName,
                                   String accountStatus) {
    }
}
