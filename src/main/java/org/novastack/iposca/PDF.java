package org.novastack.iposca;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Engine that opens PDF files.
 * */
public class PDF {
    /**
     * Spawns a new thread to open the PDF file to prevent crashes, mutex locks, etc.
     * @param pdfFile The PDF file to open.
     * */
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

    /**
     * Resolves the PDF opening process based on the operating system. For Windows, we can use the standard {@link Desktop}
     * library to open the default PDF viewer. For Linux, we can use the {@code xdg-open} command, and build a process
     * with it.
     * @param pdfFile The PDF file to open.
     * @throws IOException If the PDF file cannot be opened.
     * */
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
