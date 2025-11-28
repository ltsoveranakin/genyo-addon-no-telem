package com.genyo.core.stats;

import com.genyo.Genyo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebStats {

    private final String clientId;

    public WebStats(String clientId) {
        this.clientId = clientId;
    }

    public void sendLogin() {
        try {
            sendBackendEvent("login");
        } catch (Exception e) {
            Genyo.LOG.error("Error sending login event: ", e);
        }
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

        String backendUrl = "https://genyo-stats-557dfbb57e5f.herokuapp.com/event";

        URL url = new URL(backendUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Backend responded with: " + responseCode);
        }
        conn.getInputStream().close();
    }

}
