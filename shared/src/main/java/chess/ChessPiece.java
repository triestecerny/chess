package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to (DRAW IT OUT)
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.BISHOP) {
            return this.pieceMovesDiagonal(board, myPosition, null);
        }
        return List.of();
    }
    private Collection<ChessMove> pieceMovesDiagonal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int currentRowChange = 1;
        int currentColumnChange = 1;

        while (currentRowChange >= 1 && currentRowChange < 8 && currentColumnChange >= 1 && currentColumnChange < 8) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + currentRowChange, myPosition.getColumn() + currentColumnChange);

            if (newPosition.validate()) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else {
                break;
            }

            currentRowChange++;
            currentColumnChange++;
        }
        //Left and up
        int upRow = 1;
        int leftCol = 1;
        while (upRow >= 1 && upRow < 8 && leftCol >= 1 && leftCol < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + upRow, myPosition.getColumn() - leftCol);

            if (newPosition.validate()) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else {
                break;
            }
            upRow++;
            leftCol++;
        }
        int downRow = 1;
        int leftCol2 = 1;
        while (downRow >= 1 && downRow < 8 && leftCol >= 1 && leftCol < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - downRow, myPosition.getColumn() - leftCol2);

            if (newPosition.validate()) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else {
                break;
            }
            downRow++;
            leftCol2++;
        }
        int downRow2 = 1;
        int rightCol = 1;
        while (downRow2 >= 1 && downRow2 < 8 && leftCol >= 1 && leftCol < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - downRow2, myPosition.getColumn() + rightCol);

            if (newPosition.validate()) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else {
                break;
            }
            downRow2++;
            rightCol++;
        }

        return chessMoves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece.getPieceType() == PieceType.ROOK) {
            return this.pieceMovesHorizonal(board, myPosition, null);
        }
        return List.of();
    }
    private Collection<ChessMove> pieceMovesHorizonal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int rowLesser


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
