package org.novastack.iposca.templates;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.print.DocFlavor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility class for handling image operations for the templates functionality.
 * */
public class ImageUtils {
    //Image file
    private static File selectAFile;

    /**
     * Allows user to select an image file and display it in an {@link ImageView}.
     * @param window The {@link Window} from which the file chooser will be displayed.
     * @param imageView The {@link ImageView} to display the selected image.
     * */
    public static void chooseImage(Window window, ImageView imageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            selectAFile = file;
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
        }
    }

    /**
     * Converts the selected image file to a byte array, suitable for storage as a BLOB in the database.
     * @return {@code byte[]} representing the image file.
     * */
    public static byte[] getImageBytes() throws IOException {
        if (selectAFile == null) {
            return null;
        }
        return Files.readAllBytes(selectAFile.toPath());
    }

    /**
     * Loads an image from a {@code byte[]} array into an {@link ImageView}.
     * @param image The {@code byte[]} array representing the image.
     * @param imageView The {@link ImageView} to display the image.
     * */
    public static void loadImage(byte[] image, ImageView imageView) {
        if (image == null || image.length == 0) {
            imageView.setImage(null);
            return;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
        Image logo = new Image(byteArrayInputStream);
        imageView.setImage(logo);
    }
}
