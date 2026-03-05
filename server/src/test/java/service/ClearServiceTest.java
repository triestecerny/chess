package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    @Test
    void clearPositive() throws DataAccessException {
        var dao = new MemoryDataAccess();
        dao.createUser(new model.UserData("trieste", "pass", "email"));

        var service = new ClearService(dao);
        service.clear();

        assertNull(dao.getUser("trieste"));
    }
}
