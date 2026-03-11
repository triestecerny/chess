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

    //USER TESTS
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
}