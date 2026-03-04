package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    void registerPositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        var user = new UserData("trieste", "pass", "email");
        var auth = service.register(user);

        assertEquals("trieste", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void registerNegativeAlreadyTaken() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        var user = new UserData("trieste", "pass", "email");
        service.register(user);

        assertThrows(DataAccessException.class, () ->
                service.register(user)
        );
    }

    @Test
    void loginPositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        var user = new UserData("trieste", "pass", "email");
        service.register(user);

        var auth = service.login(new UserData("trieste", "pass", null));

        assertEquals("trieste", auth.username());
    }
}
