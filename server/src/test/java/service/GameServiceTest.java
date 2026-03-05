package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Test
    void listGamesPositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        dao.createAuth(new AuthData("token", "trieste"));
        dao.createGame("game1");

        var games = service.listGames("token");

        assertEquals(1, games.size());
    }

    @Test
    void listGamesNegativeUnauthorized() {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        assertThrows(DataAccessException.class, () ->
                service.listGames("bad-token")
        );
    }

    @Test
    void createGamePositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        dao.createAuth(new AuthData("token", "trieste"));

        int id = service.createGame("token", "myGame");

        assertEquals(1, id);
    }

    @Test
    void createGameNegativeBadRequest() {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        dao.createAuth(new AuthData("token", "trieste"));

        assertThrows(DataAccessException.class, () ->
                service.createGame("token", "")
        );
    }

    @Test
    void joinGamePositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        dao.createAuth(new AuthData("token", "trieste"));
        int id = dao.createGame("game1");

        service.joinGame("token", "WHITE", id);

        assertEquals("trieste", dao.getGame(id).whiteUsername());
    }

    @Test
    void joinGameNegativeAlreadyTaken() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new GameService(dao);

        dao.createAuth(new AuthData("t1", "trieste"));
        dao.createAuth(new AuthData("t2", "sean"));

        int id = dao.createGame("game1");

        service.joinGame("t1", "WHITE", id);

        assertThrows(DataAccessException.class, () ->
                service.joinGame("t2", "WHITE", id)
        );
    }
}

