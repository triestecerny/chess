package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.Map;

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
    public void clear() throws Exception {
        makeRequest("DELETE", "/db", null, null, null);
    }
    public record AuthData(String authToken, String username) {}

    public AuthData register(String username, String password, String email) throws Exception {
        var body = Map.of("username", username, "password", password, "email", email);
        return makeRequest("POST", "/user", null, body, AuthData.class);
    }
    public AuthData login(String username, String password) throws Exception {
        var body = Map.of("username", username, "password", password);
        return makeRequest("POST", "/session", null, body, AuthData.class);
    }
    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", authToken, null, null);
    }
    public void createGame(String authToken, String gameName) throws Exception {
        var body = Map.of("gameName", gameName);
        makeRequest("POST", "/game", authToken, body, null);
    }
    public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {}
    private record GamesResponse(GameData[] games) {}

    public GameData[] listGames(String authToken) throws Exception {
        var response = makeRequest("GET", "/game", authToken, null, GamesResponse.class);
        return response.games();
    }
    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        var body = Map.of("gameID", gameID, "playerColor", playerColor);
        makeRequest("PUT", "/game", authToken, body, null);
    }
}