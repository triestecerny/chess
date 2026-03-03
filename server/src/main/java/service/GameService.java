package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // LIST GAMES
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return dataAccess.listGames();
    }

    // CREATE GAME
    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        return dataAccess.createGame(gameName);
    }
}
