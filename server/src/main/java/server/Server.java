package server;

import io.javalin.Javalin;

public class Server {

    private Javalin app;

    public int run(int port) {
        app = Javalin.create();

        app.get("/", ctx -> ctx.result("Chess Server Running"));

        app.start(port);
        return port;
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
}