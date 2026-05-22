import java.util.ArrayList;
import java.util.Random;

public class GameLogic {
    private final int[][] grid;
    private final ArrayList<Piece> activePieces;
    private final Random random;
    private int score;
    private boolean isGameOver; // Tracks whether the player has lost

    private final int[][][] SHAPE_DATABASE = {
            {{1}}, {{1, 1}}, {{1}, {1}}, {{1, 1, 1}}, {{1, 1, 1, 1}}, {{1, 1, 1, 1, 1}},
            {{1, 1}, {1, 1}}, {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{1, 1}, {1, 0}},
            {{1, 1, 1}, {1, 0, 0}, {1, 0, 0}}, {{0, 1, 1}, {1, 1, 0}}, {{1, 1, 1}, {0, 1, 0}}
    };

    public GameLogic() {
        grid = new int[8][8];
        activePieces = new ArrayList<>();
        random = new Random();
        score = 0;
        isGameOver = false; // Game starts active
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

        // Scan the board state immediately when a fresh set of shapes is presented
        checkGameOver();
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
        // Prevent moves if the game is already lost
        if (isGameOver || !canPlacePiece(piece, startRow, startCol)) {
            return false;
        }

        int[][] shape = piece.getShape();
        int tilesPlaced = 0;

        for (int r = 0; r < piece.getRows(); r++) {
            for (int c = 0; c < piece.getCols(); c++) {
                if (shape[r][c] == 1) {
                    grid[startRow + r][startCol + c] = 1;
                    tilesPlaced++;
                }
            }
        }

        score += tilesPlaced;
        checkAndClearLines();
        activePieces.remove(piece);

        if (activePieces.isEmpty()) {
            generateThreePieces();
        } else {
            // Also scan the board state when an individual piece is placed and active pieces change
            checkGameOver();
        }
        return true;
    }

    /**
     * Efficiently scans all available pieces against every cell on the 8x8 matrix.
     * Triggers the losing condition if absolutely no legal placements remain.
     */
    private void checkGameOver() {
        // If there are no pieces, the game cannot be over yet (waiting for generation)
        if (activePieces.isEmpty()) {
            return;
        }

        // Iterate through each available shape currently in the tray
        for (Piece piece : activePieces) {
            // Iterate through every cell on the 8x8 grid
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    // Optimization check: If the shape fits here, a move remains!
                    if (canPlacePiece(piece, row, col)) {
                        isGameOver = false;
                        return; // Exit completely at the first valid move found
                    }
                }
            }
        }

        // If the loops finish without returning, no shapes can fit anywhere
        isGameOver = true;
        System.out.println("Game Over Sequence Triggered! No moves left.");
    }

    private void checkAndClearLines() {
        boolean[] rowsToClear = new boolean[8];
        boolean[] colsToClear = new boolean[8];
        int totalLinesCleared = 0;

        for (int r = 0; r < 8; r++) {
            boolean rowFull = true;
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] == 0) { rowFull = false; break; }
            }
            rowsToClear[r] = rowFull;
            if (rowFull) totalLinesCleared++;
        }

        for (int c = 0; c < 8; c++) {
            boolean colFull = true;
            for (int r = 0; r < 8; r++) {
                if (grid[r][c] == 0) { colFull = false; break; }
            }
            colsToClear[c] = colFull;
            if (colFull) totalLinesCleared++;
        }

        if (totalLinesCleared > 0) {
            int linePoints = (int) (10 * Math.pow(totalLinesCleared, 2));
            score += linePoints;
        }

        for (int r = 0; r < 8; r++) {
            if (rowsToClear[r]) {
                for (int c = 0; c < 8; c++) grid[r][c] = 0;
            }
        }

        for (int c = 0; c < 8; c++) {
            if (colsToClear[c]) {
                for (int r = 0; r < 8; r++) grid[r][c] = 0;
            }
        }
    }

    public boolean isGameOver() { return isGameOver; }
    public int getScore() { return score; }
    public int[][] getGrid() { return grid; }
    public ArrayList<Piece> getActivePieces() { return activePieces; }
}
