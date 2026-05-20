import java.util.ArrayList;
import java.util.Random;

public class GameLogic {
    private final int[][] grid;
    private final ArrayList<Piece> activePieces;
    private final Random random;
    private int score; // Tracks the player's total score

    private final int[][][] SHAPE_DATABASE = {
            {{1}}, {{1, 1}}, {{1}, {1}}, {{1, 1, 1}}, {{1, 1, 1, 1}}, {{1, 1, 1, 1, 1}},
            {{1, 1}, {1, 1}}, {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{1, 1}, {1, 0}},
            {{1, 1, 1}, {1, 0, 0}, {1, 0, 0}}, {{0, 1, 1}, {1, 1, 0}}, {{1, 1, 1}, {0, 1, 0}}
    };

    public GameLogic() {
        grid = new int[8][8];
        activePieces = new ArrayList<>();
        random = new Random();
        score = 0; // Initialize score to zero
        generateThreePieces();
    }

    public void generateThreePieces() {
        activePieces.clear();
        ArrayList<Integer> chosenIndices = new ArrayList<>();
        while (chosenIndices.size() < 3) {
            int randomIndex = random.nextInt(SHAPE_DATABASE.length);
            if (!chosenIndices.contains(randomIndex)) {
                chosenIndices.add(randomIndex);
                activePieces.add(new Piece(SHAPE_DATABASE[randomIndex]));
            }
        }
    }

    public boolean canPlacePiece(Piece piece, int startRow, int startCol) {
        int[][] shape = piece.getShape();
        for (int r = 0; r < piece.getRows(); r++) {
            for (int c = 0; c < piece.getCols(); c++) {
                if (shape[r][c] == 1) {
                    int targetRow = startRow + r;
                    int targetCol = startCol + c;
                    if (targetRow < 0 || targetRow >= 8 || targetCol < 0 || targetCol >= 8) {
                        return false;
                    }
                    if (grid[targetRow][targetCol] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean placePiece(Piece piece, int startRow, int startCol) {
        if (!canPlacePiece(piece, startRow, startCol)) {
            return false;
        }

        int[][] shape = piece.getShape();
        int tilesPlaced = 0;

        // Place the piece on the grid and count individual tiles
        for (int r = 0; r < piece.getRows(); r++) {
            for (int c = 0; c < piece.getCols(); c++) {
                if (shape[r][c] == 1) {
                    grid[startRow + r][startCol + c] = 1;
                    tilesPlaced++;
                }
            }
        }

        // Rule 1: Award 1 point per tile within the placed piece
        score += tilesPlaced;

        // Check and clear filled lines (handles Rule 2 & Rule 3 internally)
        checkAndClearLines();

        activePieces.remove(piece);
        if (activePieces.isEmpty()) {
            generateThreePieces();
        }
        return true;
    }

    /**
     * Checks all rows and columns. Clears fully completed lines simultaneously
     * and calculates line clearing scores with multi-line exponential bonuses.
     */
    private void checkAndClearLines() {
        boolean[] rowsToClear = new boolean[8];
        boolean[] colsToClear = new boolean[8];
        int totalLinesCleared = 0;

        // 1. Identify full horizontal rows
        for (int r = 0; r < 8; r++) {
            boolean rowFull = true;
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] == 0) {
                    rowFull = false;
                    break;
                }
            }
            rowsToClear[r] = rowFull;
            if (rowFull) {
                totalLinesCleared++;
            }
        }

        // 2. Identify full vertical columns
        for (int c = 0; c < 8; c++) {
            boolean colFull = true;
            for (int r = 0; r < 8; r++) {
                if (grid[r][c] == 0) {
                    colFull = false;
                    break;
                }
            }
            colsToClear[c] = colFull;
            if (colFull) {
                totalLinesCleared++;
            }
        }

        // Calculate and add line clearing scores if lines were completed
        if (totalLinesCleared > 0) {
            // Rule 2 & 3: 10 base points per line, multiplied exponentially for combos
            // Example: 1 line = 10 pts, 2 lines = 40 pts, 3 lines = 90 pts...
            int linePoints = (int) (10 * Math.pow(totalLinesCleared, 2));
            score += linePoints;

            // Console print for testing/debugging purposes
            System.out.println("Cleared " + totalLinesCleared + " line(s)! Points gained: " + linePoints + ". Total Score: " + score);
        }

        // 3. Clear identified horizontal rows
        for (int r = 0; r < 8; r++) {
            if (rowsToClear[r]) {
                for (int c = 0; c < 8; c++) {
                    grid[r][c] = 0;
                }
            }
        }

        // 4. Clear identified vertical columns
        for (int c = 0; c < 8; c++) {
            if (colsToClear[c]) {
                for (int r = 0; r < 8; r++) {
                    grid[r][c] = 0;
                }
            }
        }
    }

    // Getter method to retrieve current score for display
    public int getScore() {
        return score;
    }

    public int[][] getGrid() { return grid; }
    public ArrayList<Piece> getActivePieces() { return activePieces; }
}
