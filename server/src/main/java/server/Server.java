package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import service.UserService;
import io.javalin.Javalin;

public class Server {

    private final Javalin javalin;
    private final ClearService clearService;
    private final UserService userService;

    private final Gson gson = new Gson();

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
        userService = new service.UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register endpoints here
        javalin.delete("/db", this::clear);
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
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
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private record ErrorResponse(String message) {}
}