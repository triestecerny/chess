package dataaccess;

import model.AuthData;
import java.sql.SQLException;

public class SqlAuthDAO implements DataAccess{

    public void createAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth.authToken());
                ps.setString(2, auth.username());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create auth: %s", e.getMessage()));
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read auth: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to delete auth: %s", e.getMessage()));
        }
    }
}
