package org.novastack.iposca.utils.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class Controller {
    @FXML
    private ImageView image;
    @FXML
    private TextField nameIn;
    @FXML
    private Label nameOut;
    @FXML
    private ToggleGroup Animal;
    @FXML
    private RadioButton catRadio;
    @FXML
    private RadioButton dogRadio;


    @FXML
    void displayName(MouseEvent event) {
        nameOut.setText(String.format("Your name is: %s", nameIn.getText()));
    }

    @FXML
    void printHelloWorld(MouseEvent event) {
        System.out.println("Hello World!");
    }

    @FXML
    void showImage(MouseEvent event) {
        image.setVisible(true);
    }

    @FXML
    void revealAnimal(MouseEvent event) {
        if (Animal.getSelectedToggle() == catRadio) {
            nameOut.setText("You're a cat person!");
        } else if (Animal.getSelectedToggle() == dogRadio) {
            nameOut.setText("You're a dog person!");
        }
    }
}
