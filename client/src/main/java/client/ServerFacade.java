package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }
    private <T> T makeRequest(String method, String path, String authToken, Object body, Class<T> responseClass) throws Exception {
        URL url = new URI(serverUrl + path).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type", "application/json");
        if (authToken != null) {
            http.setRequestProperty("authorization", authToken);
        }
        if (body != null) {
            http.setDoOutput(true);
            try (var out = http.getOutputStream()) {
                out.write(gson.toJson(body).getBytes());
            }
        }
        http.connect();
        if (http.getResponseCode() >= 300) {
            throw new Exception("Error: " + readBody(http.getErrorStream()));
        }
        if (responseClass == null) return null;
        return gson.fromJson(readBody(http.getInputStream()), responseClass);
    }

    private String readBody(InputStream stream) throws IOException {
        if (stream == null) return "";
        return new String(stream.readAllBytes());
    }
}