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
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        // 1. Verify the user is logged in
        model.AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // 2. Find the game they want to join
        model.GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // 3. Logic to "sit down" in the right chair
        String username = auth.username();
        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();

        if ("WHITE".equals(playerColor)) {
            if (whiteUser != null) {
                throw new DataAccessException("Error: already taken");
            }
            whiteUser = username;
        } else if ("BLACK".equals(playerColor)) {
            if (blackUser != null) {
                throw new DataAccessException("Error: already taken");
            }
            blackUser = username;
        } else {
            // If color isn't WHITE or BLACK, it's a bad request
            throw new DataAccessException("Error: bad request");
        }

        // 4. Create a new GameData object with the updated player and save it
        model.GameData updatedGame = new model.GameData(
                game.gameID(),
                whiteUser,
                blackUser,
                game.gameName(),
                game.game()
        );

        dataAccess.updateGame(updatedGame);
    }
}
