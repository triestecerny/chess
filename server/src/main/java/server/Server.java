package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import service.UserService;
import io.javalin.Javalin;
import service.GameService;

public class Server {

    private final Javalin javalin;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;

    private final Gson gson = new Gson();

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register endpoints here
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
    }

    private void clear(io.javalin.http.Context ctx) {
        try {
            clearService.clear();
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
        }
    }
    private void register(io.javalin.http.Context ctx) {
        try {
            model.UserData user = gson.fromJson(ctx.body(), model.UserData.class);

            model.AuthData auth = userService.register(user);

            ctx.status(200).result(gson.toJson(auth));

        } catch (DataAccessException e) {

            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else if (e.getMessage().contains("already taken")) {
                ctx.status(403);
            } else {
                ctx.status(500);
            }
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void login(io.javalin.http.Context ctx) {
        try {
            model.UserData user = gson.fromJson(ctx.body(), model.UserData.class);
            model.AuthData auth = userService.login(user);
            ctx.status(200).result(gson.toJson(auth));
        } catch (DataAccessException e) {
            // if login fails
            ctx.status(401).result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void logout(io.javalin.http.Context ctx) {
        try {
            // read header
            String authToken = ctx.header("Authorization");

            userService.logout(authToken);

            ctx.status(200).result("{}"); // Success returns empty JSON
        } catch (DataAccessException e) {
            ctx.status(401).result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void listGames(io.javalin.http.Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            // Get the collection of games from the service
            java.util.Collection<model.GameData> games = gameService.listGames(authToken);

            // The spec requires: { "games": [ {...}, {...} ] }
            ctx.status(200).result(gson.toJson(java.util.Map.of("games", games)));

        } catch (DataAccessException e) {
            ctx.status(401).result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void createGame(io.javalin.http.Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            String gameName = gson.fromJson(ctx.body(), java.util.Map.class).get("gameName").toString();
            int gameID = gameService.createGame(authToken, gameName);
            ctx.status(200).result(gson.toJson(java.util.Map.of("gameID", gameID)));
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401).result(gson.toJson(new ErrorResponse(e.getMessage())));
            } else if (e.getMessage().contains("bad request")) {
                ctx.status(400).result(gson.toJson(new ErrorResponse(e.getMessage())));
            } else {
                ctx.status(500).result(gson.toJson(new ErrorResponse(e.getMessage())));
            }
        }
    }
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private record ErrorResponse(String message) {}
}