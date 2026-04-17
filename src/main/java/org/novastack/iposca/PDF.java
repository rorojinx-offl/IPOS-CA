package org.novastack.iposca;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PDF {
    public static void openPDF(File pdfFile) {
        Thread thread = new Thread(() -> {
            try {
                pdfResolve(pdfFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "pdf-opener");

        thread.setDaemon(true);
        thread.start();
    }

    public static void pdfResolve(File pdfFile) throws IOException {
        Path path = pdfFile.toPath().toAbsolutePath();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            try {
                new ProcessBuilder("xdg-open", path.toString()).start();
            } catch (Exception e) {
                throw new IOException("Failed to open PDF file: " + e.getMessage());
            }
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(path.toFile());
                }
                return;
            }
        } catch (Exception e) {
            throw new IOException("Failed to open PDF file: " + e.getMessage());
        }

        throw new IOException("Unsupported operating system: " + os);
    }
}
