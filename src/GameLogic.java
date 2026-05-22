import java.util.ArrayList;
import java.util.Random;

public class GameLogic {
    private final int[][] grid;
    private final ArrayList<Piece> activePieces;
    private final Random random;
    private int score;
    private boolean isGameOver;

    // Combo Streak Tracking Variables
    private int comboStreak;          // Tracks current consecutive line-clearing turns
    private int movesSinceLastClear;  // New counter: tracks grace-period moves (0 to 3)

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
        comboStreak = 0;
        movesSinceLastClear = 0; // Initialize our move buffer counter
        isGameOver = false;
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
        checkGameOver();
    }

    /**
     * Resets the entire game state to defaults, including all combo buffer counters.
     */
    public void restartGame() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = 0;
            }
        }
        this.score = 0;
        this.comboStreak = 0;
        this.movesSinceLastClear = 0; // Clear move counter
        this.isGameOver = false;
        generateThreePieces();
        System.out.println("Game successfully restarted!");
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

        // Award block placement points (1pt per tile)
        score += tilesPlaced;

        // Check lines and process our updated 3-move buffer combo calculation
        checkAndClearLines();

        activePieces.remove(piece);

        if (activePieces.isEmpty()) {
            generateThreePieces();
        } else {
            checkGameOver();
        }
        return true;
    }

    private void checkGameOver() {
        if (activePieces.isEmpty()) return;

        for (Piece piece : activePieces) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (canPlacePiece(piece, row, col)) {
                        isGameOver = false;
                        return;
                    }
                }
            }
        }
        isGameOver = true;
        System.out.println("Game Over Sequence Triggered! No moves left.");
    }

    /**
     * Checks all rows and columns. Clears lines simultaneously,
     * tracks the 3-move buffer grace system, and calculates combo score multipliers.
     */
    private void checkAndClearLines() {
        boolean[] rowsToClear = new boolean[8];
        boolean[] colsToClear = new boolean[8];
        int totalLinesCleared = 0;

        // 1. Identify full horizontal rows
        for (int r = 0; r < 8; r++) {
            boolean rowFull = true;
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] == 0) { rowFull = false; break; }
            }
            rowsToClear[r] = rowFull;
            if (rowFull) totalLinesCleared++;
        }

        // 2. Identify full vertical columns
        for (int c = 0; c < 8; c++) {
            boolean colFull = true;
            for (int r = 0; r < 8; r++) {
                if (grid[r][c] == 0) { colFull = false; break; }
            }
            colsToClear[c] = colFull;
            if (colFull) totalLinesCleared++;
        }

        // 3. Process Scoring and 3-Move Combo Mechanics
        if (totalLinesCleared > 0) {
            // A line was cleared! Increase combo streak and reset our grace counter to 0
            comboStreak++;
            movesSinceLastClear = 0;

            // Determine Combo Multiplier base: C = current streak (which just increased)
            int cMultiplier = comboStreak;
            int multiLineBonus = 1;

            // Map out multi-line bonus scaling rules
            switch (totalLinesCleared) {
                case 1: multiLineBonus = 1; break; // No multiplier
                case 2: multiLineBonus = 2; break; // Double bonus
                case 3: multiLineBonus = 2; break; // Triple bonus
                case 4: multiLineBonus = 3; break; // Quadruple bonus
                default: multiLineBonus = 4; break; // Quintuple bonus or higher
            }

            // Calculation formula: Points = C * 10 * Lines * Multi-line Bonus
            int turnPoints = cMultiplier * 10 * totalLinesCleared * multiLineBonus;
            score += turnPoints;

            // Debug breakdown printed to console
            System.out.println("========== SCORE BREAKDOWN ==========");
            System.out.println("Lines Cleared: " + totalLinesCleared);
            System.out.println("Combo Streak Level: " + comboStreak);
            System.out.println("Grace Counter Reset To: 0/3");
            System.out.println("Points Awarded: " + turnPoints);
            System.out.println("Updated Total Score: " + score);
            System.out.println("=====================================");

        } else {
            // No lines cleared this move.
            if (comboStreak > 0) {
                movesSinceLastClear++; // Increment the grace period move counter

                System.out.println("No lines cleared. Combo Grace Counter: " + movesSinceLastClear + "/3 moves used.");

                // If this was the third move without a clear, drop the combo completely
                if (movesSinceLastClear >= 3) {
                    System.out.println("Streak broken! 3 moves passed without clearing a line. Combo reset from " + comboStreak + " to 0.");
                    comboStreak = 0;
                    movesSinceLastClear = 0;
                }
            }
        }

        // 4. Clear identified horizontal rows
        for (int r = 0; r < 8; r++) {
            if (rowsToClear[r]) {
                for (int c = 0; c < 8; c++) grid[r][c] = 0;
            }
        }

        // 5. Clear identified vertical columns
        for (int c = 0; c < 8; c++) {
            if (colsToClear[c]) {
                for (int r = 0; r < 8; r++) grid[r][c] = 0;
            }
        }
    }

    // Pure boolean method matches GridPanel expectations cleanly
    public boolean isGameOver() {
        return isGameOver;
    }

    public int getScore() { return score; }
    public int getComboStreak() { return comboStreak; }
    public int getMovesSinceLastClear() { return movesSinceLastClear; }
    public int[][] getGrid() { return grid; }
    public ArrayList<Piece> getActivePieces() { return activePieces; }
}
