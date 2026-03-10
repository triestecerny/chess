package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import chess.ChessGame;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO {

    private final Gson gson = new Gson();

    public int createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO game (gameName, game) VALUES (?, ?)";
        // new game state
        String jsonGame = gson.toJson(new ChessGame());

        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, gameName);
                ps.setString(2, jsonGame);
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to create game: " + e.getMessage());
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                gson.fromJson(rs.getString("game"), ChessGame.class)
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read game: " + e.getMessage());
        }
        return null;
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        var statement = "SELECT * FROM game";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        games.add(new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                gson.fromJson(rs.getString("game"), ChessGame.class)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
        return games;
    }

    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE game SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, game.whiteUsername());
                ps.setString(2, game.blackUsername());
                ps.setString(3, gson.toJson(game.game()));
                ps.setInt(4, game.gameID());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update game: " + e.getMessage());
        }
    }
}