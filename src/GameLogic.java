import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class GameLogic {
    private final int[][] grid;
    private final ArrayList<Piece> activePieces;
    private final Random random;
    private int score;
    private int highScore; // Tracks persistent all-time top score
    private boolean isGameOver;

    private int comboStreak;
    private int movesSinceLastClear;

    private final String HIGH_SCORE_FILE = "highscore.txt";

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
        movesSinceLastClear = 0;
        isGameOver = false;
        loadHighScore(); // Load historical score at launch
        generateThreePieces();
    }

    /**
     * Loads the top score from a local text file. Creates it if missing.
     */
    private void loadHighScore() {
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextInt()) {
                    this.highScore = scanner.nextInt();
                }
                scanner.close();
            } else {
                this.highScore = 0;
            }
        } catch (Exception e) {
            this.highScore = 0;
            System.out.println("Error reading high score file.");
        }
    }

    /**
     * Saves a new record score permanently to disk.
     */
    private void saveHighScore() {
        try {
            FileWriter writer = new FileWriter(HIGH_SCORE_FILE);
            writer.write(Integer.toString(this.highScore));
            writer.close();
        } catch (Exception e) {
            System.out.println("Error saving high score file.");
        }
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

    public void restartGame() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                grid[r][c] = 0;
            }
        }
        this.score = 0;
        this.comboStreak = 0;
        this.movesSinceLastClear = 0;
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

        score += tilesPlaced;

        // Update high score in real-time if live score beats it
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }

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
            comboStreak++;
            movesSinceLastClear = 0;

            int cMultiplier = comboStreak;
            int multiLineBonus = 1;

            switch (totalLinesCleared) {
                case 1: multiLineBonus = 1; break;
                case 2: multiLineBonus = 2; break;
                case 3: multiLineBonus = 2; break;
                case 4: multiLineBonus = 3; break;
                default: multiLineBonus = 4; break;
            }

            int turnPoints = cMultiplier * 10 * totalLinesCleared * multiLineBonus;
            score += turnPoints;

            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }

            System.out.println("Lines Cleared: " + totalLinesCleared + " | Points: " + turnPoints);
        } else {
            if (comboStreak > 0) {
                movesSinceLastClear++;
                if (movesSinceLastClear >= 3) {
                    comboStreak = 0;
                    movesSinceLastClear = 0;
                }
            }
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
    public int getHighScore() { return highScore; }
    public int[][] getGrid() { return grid; }
    public ArrayList<Piece> getActivePieces() { return activePieces; }
}
