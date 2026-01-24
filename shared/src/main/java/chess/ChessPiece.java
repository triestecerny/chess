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

    public enum MoveState{
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
            Collection<ChessMove> chessMovesPawn = this.pieceMovesPawn(board, myPosition, piece.getPieceType(), this.pieceColor);
            List<ChessMove> chessMoves = new ArrayList<>();
            chessMoves.addAll(chessMovesPawn);
            return chessMoves;
        }
        return List.of();
    }
    private Collection<ChessMove> pieceMovesPawn(ChessBoard board, ChessPosition myPosition, PieceType pieceType, ChessGame.TeamColor color){
        Collection<ChessMove> chessMoves = new ArrayList<>();
        //rules for the pawn depending on color it can go up or down depending on what row 2,7 has the opportunity to move 2
        //WHITE PAWNS
        if (color == ChessGame.TeamColor.WHITE) {
            // move one space in front of me
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
            MoveState ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.VALID) {
                //if i am on row 8 promote to queen,rook,knight,bishop
                if (newPosition.getRow() == 8){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                // move off home 2 space
                if (myPosition.getRow() == 2) {
                    newPosition = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());
                    ms = validateMove(board, this, myPosition, newPosition);
                    if (ms == MoveState.VALID) {
                        //if i am on row 8 promote to queen,rook,knight,bishop
                        if (newPosition.getRow() == 8){
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        } else {
                            chessMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }

            // check capture up and to the right
            newPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
            ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.CAPTURE) {
                //if i am on row 8 promote to queen,rook,knight,bishop
                if (newPosition.getRow() == 8){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            // check capture up and to the left
            newPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
            ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.CAPTURE) {
                //if i am on row 8 promote to queen,rook,knight,bishop
                if (newPosition.getRow() == 8){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        //BLACK PAWNS
        if (color == ChessGame.TeamColor.BLACK) {
            // move one space in front of me
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
            MoveState ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.VALID) {
                //if i am on row 1 promote to queen,rook,knight,bishop
                if (newPosition.getRow() == 1){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
                // move off home 2 space
                if (myPosition.getRow() == 7) {
                    newPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());
                    ms = validateMove(board, this, myPosition, newPosition);
                    if (ms == MoveState.VALID) {
                        if (newPosition.getRow() == 1){
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                            chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                        } else {
                            chessMoves.add(new ChessMove(myPosition, newPosition, null));
                        }
                    }
                }
            }

            // check capture up and to the right
            newPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
            ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.CAPTURE) {
                if (newPosition.getRow() == 1){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
            // check capture up and to the left
            newPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
            ms = validateMove(board, this, myPosition, newPosition);
            if (ms == MoveState.CAPTURE) {
                if (newPosition.getRow() == 1){
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                    chessMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                } else {
                    chessMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
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
        newPosition = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1 );

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

    private Collection<ChessMove> pieceMovesDiagonal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int currentRowChange = 1;
        int currentColumnChange = 1;

        while (currentRowChange >= 1 && currentRowChange < 8 && currentColumnChange >= 1 && currentColumnChange < 8) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + currentRowChange, myPosition.getColumn() + currentColumnChange);

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
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

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
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

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
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

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            downRow2++;
            rightCol++;
        }

        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesVertical(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int rowMove = 1;
        while (rowMove >= 1 && rowMove < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() - rowMove, myPosition.getColumn());

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            rowMove++;
        }
        rowMove= 1;
        while (rowMove >= 1 && rowMove < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowMove, myPosition.getColumn());

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            rowMove++;
        }
        return chessMoves;
    }
    private Collection<ChessMove> pieceMovesHorizontal(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int colMove = 1;
        while (colMove >= 1 && colMove < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - colMove);

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            colMove++;
        }
        colMove = 1;
        while (colMove >= 1 && colMove < 8) {
            ;
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + colMove);

            MoveState ms = validateMove(board, this, myPosition, newPosition);

            if (ms == MoveState.VALID) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
            } else if (ms == MoveState.CAPTURE) {
                chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
                break;
            } else {
                break;
            }
            colMove++;
        }
        return chessMoves;
    }
    private Collection<ChessMove> pieceMovesVertical1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int rowMove1 = 1;

        ChessPosition newPosition = new ChessPosition(myPosition.getRow() - rowMove1, myPosition.getColumn());

        MoveState ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }



        int rowMovePos1 = 1;

        newPosition = new ChessPosition(myPosition.getRow() + rowMovePos1, myPosition.getColumn());

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        return chessMoves;
    }
    private Collection<ChessMove> pieceMovesHorizontal1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int colMoveNeg1 = 1;
        ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - colMoveNeg1);

        MoveState ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        int colMovePos1 = 1;
        newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + colMovePos1);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }
        return chessMoves;
    }

    private Collection<ChessMove> pieceMovesDiagonal1(ChessBoard board, ChessPosition myPosition, PieceType pieceType) {
            Collection<ChessMove> chessMoves = new ArrayList<>();

        int currentRowChange1 = 1;
        int currentColumnChange1 = 1;


        ChessPosition newPosition = new ChessPosition(myPosition.getRow() + currentRowChange1, myPosition.getColumn() + currentColumnChange1);

        MoveState ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        //Left and up
        int upRow1 = 1;
        int leftCol1 = 1;

        newPosition = new ChessPosition(myPosition.getRow() + upRow1, myPosition.getColumn() - leftCol1);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        int downRow1 = 1;
        int leftCol21 = 1;

        newPosition = new ChessPosition(myPosition.getRow() - downRow1, myPosition.getColumn() - leftCol21);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

        int downRow21 = 1;
        int rightCol2 = 1;

        newPosition = new ChessPosition(myPosition.getRow() - downRow21, myPosition.getColumn() + rightCol2);

        ms = validateMove(board, this, myPosition, newPosition);
        if (ms == MoveState.VALID || ms == MoveState.CAPTURE) {
            chessMoves.add(new ChessMove(myPosition, newPosition, pieceType));
        }

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
        if(newChessPositon.getRow() > 8){
            return MoveState.INVALID;
        }
        if (newChessPositon.getRow() < 1) {
            return MoveState.INVALID;
        }

        if (newChessPositon.getColumn() > 8){
            return MoveState.INVALID;
        }

        if (newChessPositon.getColumn() < 1){
            return MoveState.INVALID;
        }

        // if the piece in the new position is the same color I can't move here
        var newChessPiece = board.getPiece(newChessPositon);
        if (newChessPiece != null) {
            if (newChessPiece.getTeamColor() == chessPiece.getTeamColor()){
                return MoveState.INVALID;
            }
            //we know the color is the opposite
            //we can not take the opposing colors king
            if (newChessPiece.getPieceType() == PieceType.KING) {
                return MoveState.INVALID;
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
