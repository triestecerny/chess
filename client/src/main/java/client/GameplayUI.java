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
    private final String playerColor;
    private final Gson gson = new Gson();
    private ChessGame currentGame;
    private boolean inGame = true;
    private String currentTurn;

    public GameplayUI(String serverUrl, String authToken, int gameID, String playerColor) throws Exception {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.ws = new WebSocketCommunicator(serverUrl, this);
        UserGameCommand connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.sendMessage(gson.toJson(connect));
    }

    @Override
    public void onMessage(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame().game();
                currentTurn = currentGame.getTeamTurn().name();
                boolean whiteBottom = !"BLACK".equals(playerColor);
                ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, null);
                System.out.println("It is " + currentTurn + "'s turn.");
                System.out.print("[game] >>> ");
            }
            case NOTIFICATION -> {
                String note = ((NotificationMessage) message).getMessage();
                System.out.println("\n[NOTIFICATION] " + note);

                if (message instanceof LoadGameMessage loadMsg) {
                    currentGame = loadMsg.getGame().game();
                    currentTurn = currentGame.getTeamTurn().name();
                    boolean whiteBottom = !"BLACK".equals(playerColor);
                    ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, null);
                } else if (currentGame != null) {
                    currentTurn = currentGame.getTeamTurn().name();
                    boolean whiteBottom = !"BLACK".equals(playerColor);
                    ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, null);
                }

                System.out.println("It is " + currentTurn + "'s turn.");
                System.out.print("[game] >>> ");
            }
            case ERROR -> {
                String msg = ((ErrorMessage) message).getErrorMessage();
                msg = msg.replaceFirst("(?i)^error:?\\s*", "");
                System.out.println("\nError: " + msg);
            }
        }
    }

    public void run(Scanner scanner) {
        System.out.println("Type 'help' for commands.");
        while (inGame) {
            System.out.print("[game] >>> ");
            String input = scanner.nextLine().trim();
            String result = eval(input, scanner);
            if (result != null) {
                System.out.println(result);
            }
        }
    }

    private String eval(String input, Scanner scanner) {
        input = input.toLowerCase().trim();
        input = input.replace(" to ", " ");
        var tokens = input.split("\\s+");
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
            Thread.sleep(50);
            ws.close();
        } catch (Exception e) {
            if (!e.getMessage().contains("closed")) {
                System.out.println("Warning: " + e.getMessage());
            }
        }
        inGame = false;
        return "You left the game.";
    }

    private String makeMove(String[] tokens) {
        if (tokens.length < 3) return "Error: Usage -> move <from> <to>";
        if (!playerColor.equalsIgnoreCase(currentTurn)) return "Error: It's not your turn. It is " + currentTurn + "'s turn.";

        try {
            ChessPosition from = parsePosition(tokens[1]);
            ChessPosition to = parsePosition(tokens[2]);
            ChessPiece.PieceType promotion = null;
            if (tokens.length >= 4) promotion = parsePromotion(tokens[3]);
            ChessMove move = new ChessMove(from, to, promotion);

            if (!currentGame.isValidMove(move)) return "Error: invalid move. Try again.";

            MakeMoveCommand cmd = new MakeMoveCommand(authToken, gameID, move);
            ws.sendMessage(gson.toJson(cmd));

            return null;
        } catch (Exception e) {
            return "Error: Could not send move. Connection issue.";
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
            return "Error: Could not send resign request.";
        }
    }

    private String highlight(String[] tokens) {
        if (tokens.length < 2) return "Usage: highlight <SQUARE> (e.g. highlight e2)";
        if (currentGame == null) return "No game loaded yet.";
        try {
            ChessPosition pos = parsePosition(tokens[1]);
            Collection<ChessMove> moves = currentGame.validMoves(pos);
            boolean whiteBottom = !"BLACK".equals(playerColor);
            ui.BoardDrawer.drawBoard(currentGame.getBoard(), whiteBottom, moves, pos);
            return null;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private ChessPosition parsePosition(String s) {
        if (s.length() != 2 || s.charAt(0) < 'a' || s.charAt(0) > 'h'
                || !Character.isDigit(s.charAt(1))) throw new IllegalArgumentException("Invalid position: " + s);
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