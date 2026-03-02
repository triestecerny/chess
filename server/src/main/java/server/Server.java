package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import service.ClearService;
import io.javalin.Javalin;

public class Server {

    private final Javalin javalin;
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public Server() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register endpoints here
        javalin.delete("/db", this::clear);
    }

    private void clear(io.javalin.http.Context ctx) {
        try {
            clearService.clear();
            ctx.status(200).result("{}");
        } catch (DataAccessException e) {
            ctx.status(500).result(gson.toJson(new ErrorResponse("Error: " + e.getMessage())));
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