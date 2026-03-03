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
    private record JoinRequest(String playerColor, int gameID) {}

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
            //
            if (e.getMessage().contains("bad request")) {
                ctx.status(400);
            } else {
                ctx.status(401); // not authorized
            }
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
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

            // get all the games
            java.util.Collection<model.GameData> games = gameService.listGames(authToken);
            ctx.status(200).result(gson.toJson(java.util.Map.of("games", games)));

        } catch (DataAccessException e) {
            ctx.status(401).result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void createGame(io.javalin.http.Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            // read through it
            model.GameData gameRequest = gson.fromJson(ctx.body(), model.GameData.class);

            // nulls?
            if (gameRequest == null || gameRequest.gameName() == null || gameRequest.gameName().isEmpty()) {
                ctx.status(400).result(gson.toJson(new ErrorResponse("Error: bad request")));
                return;
            }

            int gameID = gameService.createGame(authToken, gameRequest.gameName());

            ctx.status(200).result(gson.toJson(java.util.Map.of("gameID", gameID)));

        } catch (DataAccessException e) {
            // 401 error
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(400);
            }
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
        }
    }
    private void joinGame(io.javalin.http.Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            JoinRequest joinReq = gson.fromJson(ctx.body(), JoinRequest.class);

            gameService.joinGame(authToken, joinReq.playerColor(), joinReq.gameID());

            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                ctx.status(401);
            } else if (e.getMessage().contains("already taken")) {
                ctx.status(403);
            } else {
                ctx.status(400); // bad request
            }
            ctx.result(gson.toJson(new ErrorResponse(e.getMessage())));
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