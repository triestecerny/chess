package dataaccess;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLDAOTests {
    // DAOs being testing
    private final SqlUserDAO userDAO = new SqlUserDAO();
    private final SqlAuthDAO authDAO = new SqlAuthDAO();
    private final SqlGameDAO gameDAO = new SqlGameDAO();

    @BeforeEach
    void setup() throws DataAccessException {
        // clear everything
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    //positive and negative user tests
    @Test
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("jimmy", "secret", "j@chess.com");
        assertDoesNotThrow(() -> userDAO.createUser(user));
    }

    @Test
    void createUserNegative() throws DataAccessException {
        // null
        UserData badUser = new UserData(null, null, null);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(badUser));
    }

    // positive and negative auth tests
    @Test
    void createAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("token-123", "jimmy");
        assertDoesNotThrow(() -> authDAO.createAuth(auth));
        assertEquals("jimmy", authDAO.getAuth("token-123").username());
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        // token never actually created
        assertNull(authDAO.getAuth("fake-token"));
    }

    // positive and negative game tests
    @Test
    void createGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("Grandmaster Match");
        assertTrue(id > 0);
    }

    @Test
    void getGameNegative() throws DataAccessException {
        // not allowed to get gamID 999
        assertNull(gameDAO.getGame(999));
    }
    //listing and joining tests
    @Test
    void listGamesPositive() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        assertEquals(2, gameDAO.listGames().size());
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        // return empty list
        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("Empty Game");
        GameData updated = new GameData(id, "whitePlayer", null, "Empty Game", null);
        assertDoesNotThrow(() -> gameDAO.updateGame(updated));
        assertEquals("whitePlayer", gameDAO.getGame(id).whiteUsername());
    }

    @Test
    void updateGameNegative() throws DataAccessException {
        //try to update game ID
        GameData fakeGame = new GameData(12345, "bad", "bad", "fake", null);

        // throw?
        assertDoesNotThrow(() -> gameDAO.updateGame(fakeGame));

        // database still empty
        assertNull(gameDAO.getGame(12345));
    }
    @Test
    void deleteAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("valid-token", "jimmy"));
        assertDoesNotThrow(() -> authDAO.deleteAuth("valid-token"));
        assertNull(authDAO.getAuth("valid-token"));
    }

    @Test
    void deleteAuthNegative() throws DataAccessException {
        // attempt delete
        assertDoesNotThrow(() -> authDAO.deleteAuth("non-existent-token"));
    }
    @Test
    void getUserPositive() throws DataAccessException {
        userDAO.createUser(new UserData("jimmy", "secret", "j@c.com"));
        assertNotNull(userDAO.getUser("jimmy"));
    }

    @Test
    void getUserNegative() throws DataAccessException {
        assertNull(userDAO.getUser("someone-who-does-not-exist"));
    }
    @Test
    void clearTest() throws DataAccessException {
        userDAO.createUser(new UserData("user", "pass", "email"));
        gameDAO.createGame("game");

        userDAO.clear();
        gameDAO.clear();

        assertNull(userDAO.getUser("user"));
        assertTrue(gameDAO.listGames().isEmpty());
    }
}