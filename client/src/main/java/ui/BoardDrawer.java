package ui;

import chess.*;
import java.util.Collection;
import java.util.HashSet;

public class BoardDrawer {

    public static void drawBoard(ChessBoard board, boolean isWhitePerspective, Collection<ChessMove> legalMoves) {
        System.out.println(); //space for the board better looking
        // Extract end positions for highlighting
        HashSet<ChessPosition> highlightedSquares = new HashSet<>();
        if (legalMoves != null) {
            for (ChessMove move : legalMoves) {
                highlightedSquares.add(move.getEndPosition());
            }
        }

        String[] cols = {"a", "b", "c", "d", "e", "f", "g", "h"};

        int rowStart = isWhitePerspective ? 8 : 1;
        int rowEnd = isWhitePerspective ? 0 : 9;
        int rowStep = isWhitePerspective ? -1 : 1;

        int colStart = isWhitePerspective ? 1 : 8;
        int colEnd = isWhitePerspective ? 9 : 0;
        int colStep = isWhitePerspective ? 1 : -1;

        printColumnHeaders(cols, colStart, colEnd, colStep);

        for (int r = rowStart; r != rowEnd; r += rowStep) {
            System.out.print(" " + r + " ");
            for (int c = colStart; c != colEnd; c += colStep) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);

                //check if highlighted
                String bg = getSquareColor(r, c, highlightedSquares.contains(pos));

                String pieceStr = getPieceString(piece);
                String pieceColor = getPieceColor(piece);

                System.out.print(bg + pieceColor + " " + pieceStr + " " + "\u001b[0m");
            }
            System.out.println(" " + r);
        }

        printColumnHeaders(cols, colStart, colEnd, colStep);
    }

    private static void printColumnHeaders(String[] cols, int start, int end, int step) {
        System.out.print("   ");
        for (int c = start; c != end; c += step) {
            System.out.print(" " + cols[c - 1] + " ");
        }
        System.out.println();
    }

    private static String getSquareColor(int r, int c, boolean isHighlighted) {
        if (isHighlighted) {
            return "\u001b[42m"; // green for highlights
        }
        return (r + c) % 2 != 0 ? "\u001b[47m" : "\u001b[100m"; // gray
    }

    private static String getPieceString(ChessPiece piece) {
        if (piece == null) return " ";
        return switch (piece.getPieceType()) {
            case PAWN -> "P";
            case ROOK -> "R";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case QUEEN -> "Q";
            case KING -> "K";
        };
    }

    private static String getPieceColor(ChessPiece piece) {
        if (piece == null) return "";
        return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u001b[31m" : "\u001b[34m";
    }
}