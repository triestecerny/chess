package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor turn;
    private ChessBoard board;

    private ArrayList<ChessBoard> history = new ArrayList<ChessBoard>();


    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return turn == chessGame.turn && Objects.equals(board, chessGame.board) && Objects.equals(history, chessGame.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board, history);
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private boolean suicide(ChessMove move, ChessBoard workingBoard) {
        ChessBoard potentialBoard = new ChessBoard(workingBoard);
        ChessPiece piece = potentialBoard.getPiece(move.getStartPosition());
        if (piece == null) return false;

        potentialBoard.removePiece(move.getStartPosition());
        potentialBoard.addPiece(move.getEndPosition(), piece);

        return isInCheck(piece.getTeamColor(), potentialBoard);
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition, ChessBoard testBoard) {
        ArrayList<ChessMove> finalizedMoves = new ArrayList<>();

        if (testBoard.getPiece(startPosition) == null)
            return null;

        for (ChessMove move : testBoard.getPiece(startPosition).pieceMoves(testBoard, startPosition)) {
            if (!suicide(move, testBoard)) {
                finalizedMoves.add(move);
            }
        }

        return finalizedMoves;
    }
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return validMoves(startPosition, board);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != turn) {
            throw new InvalidMoveException();
        }

        Collection<ChessMove> moves = validMoves(move.getStartPosition(), board);
        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException();
        }

        ChessBoard newBoard = new ChessBoard(board);
        newBoard.removePiece(move.getStartPosition());
        newBoard.addPiece(move.getEndPosition(), piece);

        history.add(board);
        board = newBoard;

        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private ChessPosition findKing(TeamColor teamColor, ChessBoard testBoard) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition testPos = new ChessPosition(i, j);
                if (testBoard.getPiece(testPos) == null) continue;
                if (testBoard.getPiece(testPos).getPieceType() == ChessPiece.PieceType.KING
                        && testBoard.getPiece(testPos).getTeamColor() == teamColor) {
                    return testPos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }
    public boolean isInCheck(TeamColor teamColor, ChessBoard testBoard) {
        ChessPosition kingPos = findKing(teamColor, testBoard);
        if (kingPos == null) return false;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = testBoard.getPiece(pos);
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(testBoard, pos)) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor, board)) return false;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos, board);
                    if (moves != null && !moves.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor, board)) return false;

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos, board);
                    if (moves != null && !moves.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}