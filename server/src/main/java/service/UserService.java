package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

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

        // save
        dataAccess.createUser(user);

        // AuthToken
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        dataAccess.createAuth(auth);

        return auth;
    }
    public AuthData login(UserData user) throws DataAccessException {
        // find the user
        UserData confirmedUser = dataAccess.getUser(user.username());

        // is user right? is the password right?
        if (confirmedUser == null || !confirmedUser.password().equals(user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }

        // new authToken
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        dataAccess.createAuth(auth);

        return auth;
    }
}