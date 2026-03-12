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

    public enum MoveState {
        CAPTURE,
        VALID,
        INVALID
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
        //ROOK
        if (piece.getPieceType() == PieceType.ROOK) {
            Collection<ChessMove> chessMovesVertical = this.pieceMovesVertical(board, myPosition, null);
            Collection<ChessMove> chessMovesHorizontal = this.pieceMovesHorizontal(board, myPosition, null);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesVertical);
            chessMoves.addAll(chessMovesHorizontal);
            return chessMoves;
        }

        //QUEEN
        if (piece.getPieceType() == PieceType.QUEEN) {
            Collection<ChessMove> chessMovesVertical = this.pieceMovesVertical(board, myPosition, null);
            Collection<ChessMove> chessMovesHorizontal = this.pieceMovesHorizontal(board, myPosition, null);
            Collection<ChessMove> pieceMovesDiagonal1 = this.pieceMovesDiagonal(board, myPosition, null);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesVertical);
            chessMoves.addAll(chessMovesHorizontal);
            chessMoves.addAll(pieceMovesDiagonal1);
            return chessMoves;
        }
//KING
        if (piece.getPieceType() == PieceType.KING) {
            Collection<ChessMove> chessMovesVertical1 = this.pieceMovesVertical1(board, myPosition, null);
            Collection<ChessMove> chessMovesHorizontal1 = this.pieceMovesHorizontal1(board, myPosition, null);
            Collection<ChessMove> pieceMovesDiagonal1 = this.pieceMovesDiagonal1(board, myPosition, null);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesVertical1);
            chessMoves.addAll(chessMovesHorizontal1);
            chessMoves.addAll(pieceMovesDiagonal1);
            return chessMoves;
        }
        //KNIGHT
        if (piece.getPieceType() == PieceType.KNIGHT) {
            Collection<ChessMove> chessMovesL = this.pieceMovesL(board, myPosition, null);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesL);
            return chessMoves;
        }
        if (piece.getPieceType() == PieceType.PAWN) {
            Collection<ChessMove> chessMovesPawn = this.pieceMovesPawn(board, myPosition, this.pieceColor);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesPawn);
            return chessMoves;
        }
        return List.of();
    }

    /** Adds all four promotion piece moves to the collection for the given positions. */
    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, PieceType.QUEEN));
        moves.add(new ChessMove(from, to, PieceType.ROOK));
        moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        moves.add(new ChessMove(from, to, PieceType.BISHOP));
    }

    /** Adds either promotion moves or a normal move depending on whether the destination row triggers promotion. */
    private void addPawnMove(Collection<ChessMove> moves, ChessPosition from, ChessPosition to, int promotionRow) {
        if (to.getRow() == promotionRow) {
            addPromotionMoves(moves, from, to);
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }

    /** Handles white pawn forward movement (one and two squares). */
    private void addWhitePawnForwardMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        ChessPosition oneForward = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
        MoveState ms = validateMove(board, this, myPosition, oneForward);
        if (ms != MoveState.VALID) {
            return;
        }
        addPawnMove(moves, myPosition, oneForward, 8);

        // move off home 2 space
        if (myPosition.getRow() == 2) {
            ChessPosition twoForward = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
            MoveState ms2 = validateMove(board, this, myPosition, twoForward);
            if (ms2 == MoveState.VALID) {
                addPawnMove(moves, myPosition, twoForward, 8);
            }
        }
    }

    /** Handles white pawn diagonal capture moves. */
    private void addWhitePawnCaptureMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        // check capture up and to the right
        ChessPosition captureRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
        if (validateMove(board, this, myPosition, captureRight) == MoveState.CAPTURE) {
            addPawnMove(moves, myPosition, captureRight, 8);
        }
        // check capture up and to the left
        ChessPosition captureLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
        if (validateMove(board, this, myPosition, captureLeft) == MoveState.CAPTURE) {
            addPawnMove(moves, myPosition, captureLeft, 8);
        }
    }

    /** Handles black pawn forward movement (one and two squares). */
    private void addBlackPawnForwardMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        ChessPosition oneForward = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
        MoveState ms = validateMove(board, this, myPosition, oneForward);
        if (ms != MoveState.VALID) {
            return;
        }
        //if i am on row 1 promote to queen,rook,knight,bishop
        addPawnMove(moves, myPosition, oneForward, 1);

        // move off home 2 space
        if (myPosition.getRow() == 7) {
            ChessPosition twoForward = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
            MoveState ms2 = validateMove(board, this, myPosition, twoForward);
            if (ms2 == MoveState.VALID) {
                addPawnMove(moves, myPosition, twoForward, 1);
            }
        }
    }

    /** Handles black pawn diagonal capture moves. */
    private void addBlackPawnCaptureMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        // check capture up and to the right
        ChessPosition captureRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
        if (validateMove(board, this, myPosition, captureRight) == MoveState.CAPTURE) {
            addPawnMove(moves, myPosition, captureRight, 1);
        }
        // check capture up and to the left
        ChessPosition captureLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
        if (validateMove(board, this, myPosition, captureLeft) == MoveState.CAPTURE) {
            addPawnMove(moves, myPosition, captureLeft, 1);
        }
    }

    private Collection<ChessMove> pieceMovesPawn(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        //rules for the pawn depending on color it can go up or down depending on what row 2,7 has the opportunity to move 2
        //WHITE PAWNS
        if (color == ChessGame.TeamColor.WHITE) {
            addWhitePawnForwardMoves(board, myPosition, chessMoves);
            addWhitePawnCaptureMoves(board, myPosition, chessMoves);
        }

        //BLACK PAWNS
        if (color == ChessGame.TeamColor.BLACK) {
            addBlackPawnForwardMoves(board, myPosition, chessMoves);
            addBlackPawnCaptureMoves(board, myPosition, chessMoves);
        }

        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesL(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        //up left, up right
        ChessPosition newPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);

        MoveState ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }
        newPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        //left up and right up
        newPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        //
        newPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        //left down right down
        newPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        newPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        //up right
        newPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        newPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        return chessMoves;
    }

    /** Slides a piece in one diagonal direction until blocked or off-board. */
    private void slideDiagonal(ChessBoard board, ChessPosition myPosition, PieceType pieceType,
                               int rowDir, int colDir, Collection<ChessMove> chessMoves) {
        int step = 1;
        while (step < 8) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDir * step,
                    myPosition.getColumn() + colDir * step);
            MoveState ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            step++;
        }
    }

    private Collection<ChessMove> pieceMovesDiagonal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        slideDiagonal(board, myPosition, pieceType, 1, 1, chessMoves);
        //Left and up
        slideDiagonal(board, myPosition, pieceType, 1, -1, chessMoves);
        slideDiagonal(board, myPosition, pieceType, -1, -1, chessMoves);
        slideDiagonal(board, myPosition, pieceType, -1, 1, chessMoves);
        return chessMoves;
    }

    /** Slides a piece along one axis direction until blocked or off-board. */
    private void slideAxis(ChessBoard board, ChessPosition myPosition, PieceType pieceType,
                           int rowDir, int colDir, Collection<ChessMove> chessMoves) {
        int step = 1;
        while (step < 8) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDir * step,
                    myPosition.getColumn() + colDir * step);
            MoveState ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            step++;
        }
    }

    private Collection<ChessMove> pieceMovesVertical(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        slideAxis(board, myPosition, pieceType, -1, 0, chessMoves);
        slideAxis(board, myPosition, pieceType, 1, 0, chessMoves);
        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesHorizontal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        slideAxis(board, myPosition, pieceType, 0, -1, chessMoves);
        slideAxis(board, myPosition, pieceType, 0, 1, chessMoves);
        return chessMoves;
    }

    /** Steps one square in a given direction and adds the move if valid or a capture. */
    private void stepOne(ChessBoard board, ChessPosition myPosition, PieceType pieceType,
                         int rowDir, int colDir, Collection<ChessMove> chessMoves) {
        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDir, myPosition.getColumn() + colDir);
        MoveState ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }
    }

    private Collection<ChessMove> pieceMovesVertical1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        stepOne(board, myPosition, pieceType, -1, 0, chessMoves);
        stepOne(board, myPosition, pieceType, 1, 0, chessMoves);
        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesHorizontal1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        stepOne(board, myPosition, pieceType, 0, -1, chessMoves);
        stepOne(board, myPosition, pieceType, 0, 1, chessMoves);
        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesDiagonal1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        stepOne(board, myPosition, pieceType, 1, 1, chessMoves);
        //Left and up
        stepOne(board, myPosition, pieceType, 1, -1, chessMoves);
        stepOne(board, myPosition, pieceType, -1, -1, chessMoves);
        stepOne(board, myPosition, pieceType, -1, 1, chessMoves);
        return chessMoves;
    }

    public MoveState validateMove(ChessBoard board, ChessPiece chessPiece, ChessPosition currentChessPosition, ChessPosition newChessPositon) {
        // validate currentChessPosition is on the board
        if (currentChessPosition.getRow() > 8) {
            return MoveState.INVALID;
        }

        if (currentChessPosition.getRow() < 1) {
            return MoveState.INVALID;
        }

        if (currentChessPosition.getColumn() > 8) {
            return MoveState.INVALID;
        }

        if (currentChessPosition.getColumn() < 1) {
            return MoveState.INVALID;
        }

        // validate newChessPosition is on the board
        if (newChessPositon.getRow() > 8) {
            return MoveState.INVALID;
        }
        if (newChessPositon.getRow() < 1) {
            return MoveState.INVALID;
        }

        if (newChessPositon.getColumn() > 8) {
            return MoveState.INVALID;
        }

        if (newChessPositon.getColumn() < 1) {
            return MoveState.INVALID;
        }

        // if the piece in the new position is the same color I can't move here
        var newChessPiece = board.getPiece(newChessPositon);
        if (newChessPiece != null) {
            if (newChessPiece.getTeamColor() == chessPiece.getTeamColor()) {
                return MoveState.INVALID;
            }
            //we know the color is the opposite
            //we can not take the opposing colors king
            if (newChessPiece.getPieceType() == PieceType.KING) {
                return MoveState.CAPTURE;
            }

            return MoveState.CAPTURE;
        }

        return MoveState.VALID;
    }

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
