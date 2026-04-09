package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData user) throws DataAccessException {
        // input right thing?
        if (user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Error: bad request");
        }

        // valid user?
        if (dataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // hash and then ave
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(user.username(), hashedPassword, user.email());
        dataAccess.createUser(hashedUser);

        // AuthToken
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        dataAccess.createAuth(auth);

        return auth;
    }
    public AuthData login(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null) {
            throw new DataAccessException("Error: bad request");
        }
        // find the user
        UserData confirmedUser = dataAccess.getUser(user.username());

        // is user right? is the password right? but make it BCrypt
        if (confirmedUser == null || !BCrypt.checkpw(user.password(), confirmedUser.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        // new authToken
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        dataAccess.createAuth(auth);

        return auth;
    }
    public void logout(String authToken) throws DataAccessException {
        // token valid?
        if (authToken == null || dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // delete from database
        dataAccess.deleteAuth(authToken);
    }
}