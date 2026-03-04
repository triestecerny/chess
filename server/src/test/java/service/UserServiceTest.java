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

    @Test
    void loginNegativeWrongPassword() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        service.register(new UserData("bob", "pass", "email"));

        assertThrows(DataAccessException.class, () ->
                service.login(new UserData("bob", "wrong", null))
        );
    }

    @Test
    void logoutPositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        var auth = service.register(new UserData("bob", "pass", "email"));
        service.logout(auth.authToken());

        assertNull(dao.getAuth(auth.authToken()));
    }

    @Test
    void logoutNegativeInvalidToken() {
        var dao = new MemoryDataAccess();
        var service = new UserService(dao);

        assertThrows(DataAccessException.class, () ->
                service.logout("fake-token")
        );
    }
}
