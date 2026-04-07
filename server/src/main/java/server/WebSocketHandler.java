package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final dataaccess.DataAccess dataAccess;
    private final Gson gson = new Gson();
    private final Map<Integer, Map<String, Session>> gameSessions = new ConcurrentHashMap<>();

    public WebSocketHandler(dataaccess.DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        // making sure JSON is straightforward
        UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);

        switch (baseCommand.getCommandType()) {
            case CONNECT -> handleConnect(session, baseCommand);
            case LEAVE -> handleLeave(session, baseCommand);
            case RESIGN -> handleResign(session, baseCommand);
            case MAKE_MOVE -> {
                // need the move field
                MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                handleMakeMove(session, moveCommand);
            }
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.values().forEach(map -> map.values().remove(session));
    }

    private void handleConnect(Session session, UserGameCommand command) throws Exception {
        String username = getUsernameOrError(session, command.getAuthToken());
        if (username == null) return;

        GameData game = getGameOrError(session, command.getGameID());
        if (game == null) return;

        addSession(command.getGameID(), username, session);
        sendMessage(session, gson.toJson(new LoadGameMessage(game)));

        String color = getPlayerColor(game, username);
        String note = color != null ? username + " joined as " + color : username + " is observing";
        broadcastExcept(command.getGameID(), username, new NotificationMessage(note));
    }

    private void handleMakeMove(Session session, MakeMoveCommand command) throws Exception {
        String username = getUsernameOrError(session, command.getAuthToken());
        if (username == null) return;

        GameData game = getGameOrError(session, command.getGameID());
        if (game == null) return;

        String color = getPlayerColor(game, username);
        if (color == null) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: observers cannot make moves")));
            return;
        }

        ChessGame.TeamColor turn = game.game().getTeamTurn();
        if ((turn == ChessGame.TeamColor.WHITE && !username.equals(game.whiteUsername())) ||
                (turn == ChessGame.TeamColor.BLACK && !username.equals(game.blackUsername()))) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: not your turn")));
            return;
        }

        if (isGameOver(game)) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: game is already over")));
            return;
        }

        ChessMove move = command.getMove();
        try {
            game.game().makeMove(move);
        } catch (chess.InvalidMoveException e) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: invalid move")));
            return;
        }

        dataAccess.updateGame(game);
        broadcastAll(command.getGameID(), new LoadGameMessage(game));

        String moveDesc = move.getStartPosition().toString() + " -> " + move.getEndPosition().toString();
        broadcastExcept(command.getGameID(), username, new NotificationMessage(username + " moved " + moveDesc));

        ChessGame.TeamColor opponent = turn == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String opponentName = opponent == ChessGame.TeamColor.WHITE ? game.whiteUsername() : game.blackUsername();

        if (game.game().isInCheckmate(opponent)) {
            broadcastAll(command.getGameID(), new NotificationMessage(opponentName + " is in checkmate!"));
        } else if (game.game().isInStalemate(opponent)) {
            broadcastAll(command.getGameID(), new NotificationMessage("Stalemate! Game over."));
        } else if (game.game().isInCheck(opponent)) {
            broadcastAll(command.getGameID(), new NotificationMessage(opponentName + " is in check!"));
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws Exception {
        String username = getUsernameOrError(session, command.getAuthToken());
        if (username == null) return;

        GameData game = getGameOrError(session, command.getGameID());
        if (game == null) return;

        if (username.equals(game.whiteUsername()) || username.equals(game.blackUsername())) {
            String newWhite = username.equals(game.whiteUsername()) ? null : game.whiteUsername();
            String newBlack = username.equals(game.blackUsername()) ? null : game.blackUsername();
            dataAccess.updateGame(new GameData(game.gameID(), newWhite, newBlack, game.gameName(), game.game()));
        }

        removeSession(command.getGameID(), username);
        broadcastExcept(command.getGameID(), username, new NotificationMessage(username + " left the game"));
    }

    private void handleResign(Session session, UserGameCommand command) throws Exception {
        String username = getUsernameOrError(session, command.getAuthToken());
        if (username == null) return;

        GameData game = getGameOrError(session, command.getGameID());
        if (game == null) return;

        if (getPlayerColor(game, username) == null) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: observers cannot resign")));
            return;
        }

        if (isGameOver(game)) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: game is already over")));
            return;
        }

        game.game().setResigned(true);
        dataAccess.updateGame(game);
        broadcastAll(command.getGameID(), new NotificationMessage(username + " resigned. Game over."));
    }

    private boolean isGameOver(GameData game) {
        ChessGame g = game.game();
        return g.isResigned() ||
                g.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                g.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                g.isInStalemate(ChessGame.TeamColor.WHITE) ||
                g.isInStalemate(ChessGame.TeamColor.BLACK);
    }

    private String getUsernameOrError(Session session, String authToken) throws IOException {
        try {
            model.AuthData auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                sendMessage(session, gson.toJson(new ErrorMessage("Error: unauthorized")));
                return null;
            }
            return auth.username();
        } catch (DataAccessException e) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: " + e.getMessage())));
            return null;
        }
    }

    private GameData getGameOrError(Session session, Integer gameID) throws IOException {
        try {
            GameData game = dataAccess.getGame(gameID);
            if (game == null) {
                sendMessage(session, gson.toJson(new ErrorMessage("Error: game not found")));
                return null;
            }
            return game;
        } catch (DataAccessException e) {
            sendMessage(session, gson.toJson(new ErrorMessage("Error: " + e.getMessage())));
            return null;
        }
    }

    private String getPlayerColor(GameData game, String username) {
        if (username.equals(game.whiteUsername())) return "WHITE";
        if (username.equals(game.blackUsername())) return "BLACK";
        return null;
    }

    private void addSession(int gameID, String username, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(username, session);
    }

    private void removeSession(int gameID, String username) {
        Map<String, Session> sessions = gameSessions.get(gameID);
        if (sessions != null) sessions.remove(username);
    }

    private void sendMessage(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }

    private void broadcastAll(int gameID, websocket.messages.ServerMessage msg) throws IOException {
        String json = gson.toJson(msg);
        for (Session s : gameSessions.getOrDefault(gameID, Map.of()).values()) {
            sendMessage(s, json);
        }
    }

    private void broadcastExcept(int gameID, String excludeUsername, websocket.messages.ServerMessage msg) throws IOException {
        String json = gson.toJson(msg);
        for (var entry : gameSessions.getOrDefault(gameID, Map.of()).entrySet()) {
            if (!entry.getKey().equals(excludeUsername)) {
                sendMessage(entry.getValue(), json);
            }
        }
    }
}