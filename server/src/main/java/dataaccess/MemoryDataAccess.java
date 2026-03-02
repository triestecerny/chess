package dataaccess;

import model.AuthData;
import model.UserData;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() throws DataAccessException {
        users.clear();
        auths.clear();
        games.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {}

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {}

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {}
}