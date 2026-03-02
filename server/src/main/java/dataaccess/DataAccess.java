package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {

    void clear() throws DataAccessException;

    // User operations
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Auth operations
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

}