package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import service.*;
import io.javalin.Javalin;
import model.*;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private record JoinRequest(String playerColor, int gameID) {}

    private final Gson gson = new Gson();

    public Server() {

        try {
            DatabaseManager.configureDatabase();
        } catch (DataAccessException e) {
            System.out.printf("ERROR: Database initialization failed: %s%n", e.getMessage());
        }


        dataaccess.SqlDataAccess dataAccess = new dataaccess.SqlDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        // start javalin
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        //wire to websocket
        WebSocketHandler wsHandler = new WebSocketHandler(dataAccess);
        javalin.ws("/ws", ws -> {
            ws.onMessage(ctx -> {
                try {
                    // fix session
                    wsHandler.onMessage(ctx.session, ctx.message());
                } catch (Exception e) {
                    System.out.println("Error handling message: " + e.getMessage());
                }
            });
            ws.onClose(ctx -> {
                wsHandler.onClose(ctx.session, ctx.status(), ctx.reason());
            });
            ws.onError(ctx -> System.out.println("WS Error: " + ctx.error().getMessage()));
        });

        // Register endpoints here
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGames);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
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
            UserData user = gson.fromJson(ctx.body(), UserData.class);
            AuthData auth = userService.register(user);
            ctx.status(200).result(gson.toJson(auth));
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void login(io.javalin.http.Context ctx) {
        try {
            UserData user = gson.fromJson(ctx.body(), UserData.class);
            AuthData auth = userService.login(user);
            ctx.status(200).result(gson.toJson(auth));
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void logout(io.javalin.http.Context ctx) {
        try {
            userService.logout(ctx.header("Authorization"));
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void listGames(io.javalin.http.Context ctx) {
        try {
            var games = gameService.listGames(ctx.header("Authorization"));
            ctx.status(200).result(gson.toJson(Map.of("games", games)));
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void createGame(io.javalin.http.Context ctx) {
        try {
            String token = ctx.header("Authorization");
            GameData req = gson.fromJson(ctx.body(), GameData.class);

            // missing the game name
            if (req == null || req.gameName() == null || req.gameName().isEmpty()) {
                throw new DataAccessException("Error: bad request");
            }

            int id = gameService.createGame(token, req.gameName());
            ctx.status(200).result(gson.toJson(Map.of("gameID", id)));
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void joinGame(io.javalin.http.Context ctx) {
        try {
            JoinRequest req = gson.fromJson(ctx.body(), JoinRequest.class);
            gameService.joinGame(ctx.header("Authorization"), req.playerColor(), req.gameID());
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            handleException(ctx, e);
        }
    }
    private void handleException(io.javalin.http.Context ctx, DataAccessException e) {
        String msg = e.getMessage();

        // check if error is already there if not add!
        if (msg == null || !msg.startsWith("Error")) {
            msg = "Error: " + msg;
        }

        int status = 500; // default error

        if (msg.contains("bad request")) {
            status = 400;
        } else if (msg.contains("unauthorized")) {
            status = 401;
        } else if (msg.contains("already taken")) {
            status = 403;
        }

        ctx.status(status).result(gson.toJson(Map.of("message", msg)));
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