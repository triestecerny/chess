package dataaccess;

import model.*;
import java.util.Collection;

public class SqlDataAccess implements DataAccess {
    // similar to MemoryDataAccess but for my SQL DAO's
    private final SqlUserDAO userDAO = new SqlUserDAO();
    private final SqlAuthDAO authDAO = new SqlAuthDAO();
    private final SqlGameDAO gameDAO = new SqlGameDAO();

    @Override
    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    //go to SqlUserDAO
    @Override
    public void createUser(UserData user) throws DataAccessException {
        userDAO.createUser(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return userDAO.getUser(username);
    }

    // go to SqlAuthDAO
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authDAO.createAuth(auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDAO.getAuth(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }

    // go to SqlGameDAO
    @Override
    public int createGame(String gameName) throws DataAccessException {
        return gameDAO.createGame(gameName);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return gameDAO.getGame(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        gameDAO.updateGame(game);
    }
}