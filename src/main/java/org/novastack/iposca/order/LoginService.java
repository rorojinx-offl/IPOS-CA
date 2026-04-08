package org.novastack.iposca.order;

import java.awt.Desktop;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginService {

    private static final String BASE_URL = "http://localhost:8080";

    public LoginResult loginToIPOSSA(String username, String password) throws Exception {
        URL url = new URL(BASE_URL + "/api/auth/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        String requestBody = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(escapeJson(username), escapeJson(password));

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int statusCode = connection.getResponseCode();

        InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        String responseBody = readStream(responseStream);

        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("Server returned " + statusCode + ": " + responseBody);
        }

        String returnedUsername = extractJsonValue(responseBody, "username");
        String role = extractJsonValue(responseBody, "role");
        String merchantId = extractJsonValue(responseBody, "merchantId");
        String sessionToken = extractJsonValue(responseBody, "sessionToken");

        if (sessionToken == null || sessionToken.isBlank()) {
            throw new RuntimeException("No sessionToken found in response.");
        }

        return new LoginResult(returnedUsername, role, merchantId, sessionToken);
    }

    public void openMerchantSite(String sessionToken) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop browsing is not supported on this machine.");
        }

        String merchantUrl = BASE_URL + "/merchant.html?sessionToken=" + sessionToken;
        Desktop.getDesktop().browse(new URI(merchantUrl));
    }

    private String readStream(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}