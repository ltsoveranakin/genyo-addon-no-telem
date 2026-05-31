package com.genyo.core.stats;

import com.genyo.Genyo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class WebStats {

    private final String clientId;

    private static final String CLIENT_SECRET = "asd";

    private static final String backendUrl = "https://genyo-stats-557dfbb57e5f.herokuapp.com/event";
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;

    public WebStats(String clientId) {
        this.clientId = clientId;
    }

    public void sendLogin() {
        CompletableFuture.runAsync(() -> {
            try {
                sendBackendEvent("login");
            } catch (Exception e) {
                Genyo.LOG.error("Error sending login event: ", e);
            }
        });
    }

    public void sendLogout() {
        try {
            sendBackendEvent("logout");
        } catch (Exception e) {
            Genyo.LOG.error("Error sending logout event: ", e);
        }
    }

    private void sendBackendEvent(String eventType) throws IOException {
        String jsonBody = "{"
            + "\"event\":\"" + eventType + "\","
            + "\"clientId\":\"" + clientId + "\""
            + "}";

        URL url = new URL(backendUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-client-secret", CLIENT_SECRET);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();

        try (InputStream stream = responseCode >= 400
                ? conn.getErrorStream() : conn.getInputStream()) {
            if (stream != null) stream.readAllBytes();
        }

        if (responseCode != 200) {
            System.out.println("Backend responded with: " + responseCode);
        }
    }

}
