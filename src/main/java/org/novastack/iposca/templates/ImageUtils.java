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

public class ImageUtils {
    private static File selectAFile;
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

    public static byte[] getImageBytes() throws IOException {
        if (selectAFile == null) {
            return null;
        }
        return Files.readAllBytes(selectAFile.toPath());
    }

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
