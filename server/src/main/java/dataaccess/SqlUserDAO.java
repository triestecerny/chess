package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class SqlUserDAO{

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE user";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.executeUpdate();
            }
        } catch (java.sql.SQLException e) {
            throw new DataAccessException(String.format("Unable to clear user table: %s", e.getMessage()));
        }
    }

    public void createUser(UserData user) throws DataAccessException {
        // before saving
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        // start SQL
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";

        // go
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                ps.setString(2, hashedPassword);
                ps.setString(3, user.email());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to create user: %s", e.getMessage()));
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        var statement = "SELECT username, password, email FROM user WHERE username=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }
}
