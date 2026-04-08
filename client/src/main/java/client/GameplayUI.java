package client;

import chess.*;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements WebSocketCommunicator.ServerMessageObserver {

    private final WebSocketCommunicator ws;
    private final String authToken;
    private final int gameID;
    private final String playerColor; // "WHITE", "BLACK", or null for observer
    private final Gson gson = new Gson();
    private ChessGame currentGame;
    private boolean inGame = true;

    public GameplayUI(String serverUrl, String authToken, int gameID, String playerColor) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.ws = new WebSocketCommunicator(serverUrl, this);

        // send CONNECT
        UserGameCommand connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.sendMessage(gson.toJson(connect));
    }

    @Override
    public void onMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame().game();
                boolean whiteBottom = !"BLACK".equals(playerColor);
                // Pass the board and perspective no need for highlights
                ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, null);
            }
            case NOTIFICATION -> System.out.println("\n*** " + ((NotificationMessage) message).getMessage() + " ***");
            case ERROR -> System.out.println("\nError: " + ((ErrorMessage) message).getErrorMessage());
        }
    }

    public void run(Scanner scanner) {
        System.out.println("Type 'help' for commands.");
        while (inGame) {
            System.out.print("[game] >>> ");
            String input = scanner.nextLine().trim();
            String result = eval(input, scanner);
            if (result != null) System.out.println(result);
        }
    }

    private String eval(String input, Scanner scanner) {
        var tokens = input.split(" ");
        var cmd = tokens[0].toLowerCase();
        return switch (cmd) {
            case "help" -> help();
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> makeMove(tokens);
            case "resign" -> resign(scanner);
            case "highlight" -> highlight(tokens);
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String help() {
        return """
                - help
                - redraw
                - leave
                - move <FROM> <TO> (e.g. move e2 e4)
                - resign
                - highlight <SQUARE> (e.g. highlight e2)
                """;
    }

    private String redraw() {
        if (currentGame == null) return "No game loaded yet.";
        boolean whiteBottom = !"BLACK".equals(playerColor);
        ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, null);
        return null;
    }

    private String leave() {
        try {
            UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            ws.sendMessage(gson.toJson(cmd));
            ws.close();
            inGame = false;
            return "You left the game.";
        } catch (Exception e) {
            return "Error leaving: " + e.getMessage();
        }
    }

    private String makeMove(String[] tokens) {
        if (tokens.length < 3) return "Usage: move <FROM> <TO> (e.g. move e2 e4)";
        try {
            ChessPosition from = parsePosition(tokens[1]);
            ChessPosition to = parsePosition(tokens[2]);
            ChessPiece.PieceType promotion = null;
            if (tokens.length >= 4) {
                promotion = parsePromotion(tokens[3]);
            }
            ChessMove move = new ChessMove(from, to, promotion);
            MakeMoveCommand cmd = new MakeMoveCommand(authToken, gameID, move);
            ws.sendMessage(gson.toJson(cmd));
            return null;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String resign(Scanner scanner) {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) return "Resign cancelled.";
        try {
            UserGameCommand cmd = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            ws.sendMessage(gson.toJson(cmd));
            return null;
        } catch (Exception e) {
            return "Error resigning: " + e.getMessage();
        }
    }

    private String highlight(String[] tokens) {
        if (tokens.length < 2) return "Usage: highlight <SQUARE> (e.g. highlight e2)";
        if (currentGame == null) return "No game loaded yet.";
        try {
            ChessPosition pos = parsePosition(tokens[1]);
            Collection<ChessMove> moves = currentGame.validMoves(pos);
            boolean whiteBottom = !"BLACK".equals(playerColor);
            ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, moves);

            return null;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private ChessPosition parsePosition(String s) {
        if (s.length() != 2) throw new IllegalArgumentException("Invalid position: " + s);
        int col = s.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(s.charAt(1));
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String s) {
        return switch (s.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece: " + s);
        };
    }

    public boolean isInGame() {
        return inGame;
    }
}