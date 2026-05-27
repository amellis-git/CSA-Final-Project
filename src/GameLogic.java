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
    private int highScore;
    private boolean isGameOver;

    private int comboStreak;
    private int movesSinceLastClear;

    private final String HIGH_SCORE_FILE = "highscore.txt";

    private final int[][][] SHAPE_DATABASE = {
            {{1}}, {{1, 1}}, {{1}, {1}}, {{1, 1, 1}}, {{1, 1, 1, 1}}, {{1, 1, 1, 1, 1}},
            {{1, 1}, {1, 1}}, {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}}, {{1, 1}, {1, 0}},
            {{1, 1, 1}, {1, 0, 0}, {1, 0, 0}}, {{0, 1, 1}, {1, 1, 0}}, {{1, 1, 1}, {0, 1, 0}}
    };

    private boolean isAnimating = false;
    private int animationTick = 0;
    private boolean[] rowsToClear = new boolean[8];
    private boolean[] colsToClear = new boolean[8];

    public GameLogic() {
        grid = new int[8][8];
        activePieces = new ArrayList<>();
        random = new Random();
        score = 0;
        comboStreak = 0;
        movesSinceLastClear = 0;
        isGameOver = false;
        loadHighScore();
        generateThreePieces();
    }

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
        this.isAnimating = false;
        this.animationTick = 0;
        this.rowsToClear = new boolean[8];
        this.colsToClear = new boolean[8];
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
        if (isGameOver || isAnimating || !canPlacePiece(piece, startRow, startCol)) {
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

        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }

        checkAndClearLines();
        activePieces.remove(piece);

        if (activePieces.isEmpty() && !isAnimating) {
            generateThreePieces();
        } else if (!isAnimating) {
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
        rowsToClear = new boolean[8];
        colsToClear = new boolean[8];
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
            isAnimating = true;
            animationTick = 0;
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
        } else {
            if (comboStreak > 0) {
                movesSinceLastClear++;
                if (movesSinceLastClear >= 3) {
                    comboStreak = 0;
                    movesSinceLastClear = 0;
                }
            }
        }
    }

    public void advanceAnimation() {
        if (!isAnimating) return;
        animationTick++;

        if (animationTick >= 35) {
            finalizeLineClearing();
        }
    }

    private void finalizeLineClearing() {
        // THIS RESTORES THE DATA REMOVAL THAT WAS ACCIDENTALLY DELETED
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
        isAnimating = false;
        animationTick = 0;

        if (activePieces.isEmpty()) {
            generateThreePieces();
        } else {
            checkGameOver();
        }
    }

    public boolean[][] predictLinesToClear(Piece piece, int startRow, int startCol) {
        boolean[][] predictions = new boolean[2][8];
        if (!canPlacePiece(piece, startRow, startCol)) {
            return predictions;
        }

        int[][] tempGrid = new int[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                tempGrid[r][c] = grid[r][c];
            }
        }

        int[][] shape = piece.getShape();
        for (int r = 0; r < piece.getRows(); r++) {
            for (int c = 0; c < piece.getCols(); c++) {
                if (shape[r][c] == 1) {
                    tempGrid[startRow + r][startCol + c] = 1;
                }
            }
        }

        for (int r = 0; r < 8; r++) {
            boolean rowFull = true;
            for (int c = 0; c < 8; c++) {
                if (tempGrid[r][c] == 0) { rowFull = false; break; }
            }
            predictions[0][r] = rowFull;
        }

        for (int c = 0; c < 8; c++) {
            boolean colFull = true;
            for (int r = 0; r < 8; r++) {
                if (tempGrid[r][c] == 0) { colFull = false; break; }
            }
            predictions[1][c] = colFull;
        }

        return predictions;
    }

    public boolean isAnimating() { return isAnimating; }
    public int getAnimationTick() { return animationTick; }
    public boolean[] getRowsToClear() { return rowsToClear; }
    public boolean[] getColsToClear() { return colsToClear; }
    public boolean isGameOver() { return isGameOver; }
    public int getScore() { return score; }
    public int getHighScore() { return highScore; }
    public int[][] getGrid() { return grid; }
    public ArrayList<Piece> getActivePieces() { return activePieces; }
}
