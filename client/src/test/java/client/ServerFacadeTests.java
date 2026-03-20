package client;

import org.junit.jupiter.api.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        facade.clear();
    }

    // one negative and one positive test for register
    @Test
    void registerPositive() throws Exception {
        var authData = facade.register("trieste", "passwor28", "trieste@email.com");
        assertNotNull(authData.authToken());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerNegative() throws Exception {
        facade.register("trieste", "password28", "trieste@email.com");
        assertThrows(Exception.class, () -> facade.register("trieste", "password28", "trieste@email.com"));
    }
    @Test
    void loginPositive() throws Exception {
        facade.register("trieste", "password28", "trieste@gmail.com");
        var authData = facade.login("trieste", "password28");
        assertNotNull(authData.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.login("trieste", "wrongpassword"));
    }
    @Test
    void logoutPositive() throws Exception {
        var authData = facade.register("trieste", "password28", "trieste@gmail.com");
        facade.logout(authData.authToken());
    }

    @Test
    void logoutNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.logout("invalidtoken"));
    }
    @Test
    void createGamePositive() throws Exception {
        var authData = facade.register("trieste", "password28", "trieste@gmail.com");
        facade.createGame(authData.authToken(), "trieste's game");
    }

    @Test
    void createGameNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.createGame("invalidtoken", "trieste's game"));
    }
    @Test
    void listGamesPositive() throws Exception {
        var authData = facade.register("trieste", "password28", "trieste@gmail.com");
        facade.createGame(authData.authToken(), "trieste's game");
        var games = facade.listGames(authData.authToken());
        assertEquals(1, games.length);
    }

    @Test
    void listGamesNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.listGames("invalidtoken"));
    }
    @Test
    void joinGamePositive() throws Exception {
        var authData = facade.register("trieste", "password28", "trieste@gmail.com");
        facade.createGame(authData.authToken(), "trieste's game");
        var games = facade.listGames(authData.authToken());
        facade.joinGame(authData.authToken(), games[0].gameID(), "WHITE");
    }

    @Test
    void joinGameNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.joinGame("invalidtoken", 9999, "WHITE"));
    }

}
