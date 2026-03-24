package ui;

public class BoardDrawer {

    private static final String[][] INITIAL_BOARD = {
            {"R", "N", "B", "Q", "K", "B", "N", "R"},
            {"P", "P", "P", "P", "P", "P", "P", "P"},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {"P", "P", "P", "P", "P", "P", "P", "P"},
            {"R", "N", "B", "Q", "K", "B", "N", "R"}
    };
    public static void drawBoard(boolean isWhitePerspective) {
        String[] cols = {"a", "b", "c", "d", "e", "f", "g", "h"};

        //direction depending on perspective
        int rowStart = isWhitePerspective ? 7 : 0;
        int rowEnd = isWhitePerspective ? -1 : 8;
        int rowStep = isWhitePerspective ? -1 : 1;


        int colStart = isWhitePerspective ? 0 : 7;
        int colEnd = isWhitePerspective ? 8 : -1;
        int colStep = isWhitePerspective ? 1 : -1;

        System.out.print("   ");
        for (int c = colStart; c != colEnd; c += colStep) {
            System.out.print(" " + cols[c] + " ");
        }
        System.out.println();

        for (int r = rowStart; r != rowEnd; r += rowStep) {
            System.out.print(" " + (r + 1) + " ");
            for (int c = colStart; c != colEnd; c += colStep) {
                boolean lightSquare = (r + c) % 2 != 0;
                String bg = lightSquare ? "\u001b[47m" : "\u001b[100m";
                String piece = INITIAL_BOARD[7 - r][c];

                boolean isWhitePiece = (7 - r) >= 6;
                boolean isBlackPiece = (7 - r) <= 1;

                String pieceColor = isWhitePiece ? "\u001b[31m"
                        : isBlackPiece ? "\u001b[34m"
                        : "";

                System.out.print(bg + pieceColor + " " + piece + " " + "\u001b[0m");
            }
            System.out.println(" " + (r + 1));
        }

        System.out.print("   ");
        for (int c = colStart; c != colEnd; c += colStep) {
            System.out.print(" " + cols[c] + " ");
        }
        System.out.println();
    }
}