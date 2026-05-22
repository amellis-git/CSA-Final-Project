import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class GridPanel extends JPanel implements MouseListener, MouseMotionListener {
    private final GameLogic game;
    private final int CELL_SIZE = 50;
    private final int GRID_SIZE = 8;
    private final int PREVIEW_CELL_SIZE = 20;

    // Visual offsets to make room for the scoreboard at the top
    private final int SCOREBOARD_HEIGHT = 50;

    // Variables to manage dragging state
    private Piece selectedPiece = null;
    private int selectedIndex = -1;
    private Point mousePos = null;

    public GridPanel(GameLogic game) {
        this.game = game;
        // Width: 400 | Height: 650 (50px Score + 400px Grid + 200px Tray)
        this.setPreferredSize(new Dimension(400, 650));
        this.setBackground(Color.LIGHT_GRAY);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Draw the Scoreboard at the top
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreText = "Score: " + game.getScore();

        // Center the score text horizontally in the 400px wide window
        int textWidth = g.getFontMetrics().stringWidth(scoreText);
        int scoreX = (400 - textWidth) / 2;
        g.drawString(scoreText, scoreX, 35); // 35px down places it cleanly in the 50px zone

        // 2. Draw the main 8x8 board (shifted down by SCOREBOARD_HEIGHT)
        int[][] board = game.getGrid();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int x = col * CELL_SIZE;
                int y = (row * CELL_SIZE) + SCOREBOARD_HEIGHT;

                if (board[row][col] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
                g.setColor(Color.BLACK);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        // 3. Draw the available pieces below the board
        ArrayList<Piece> activePieces = game.getActivePieces();
        int slotWidth = 400 / 3;
        int startY = 500; // Shifted down to match layout sizing

        for (int i = 0; i < activePieces.size(); i++) {
            if (i == selectedIndex) {
                continue;
            }

            Piece piece = activePieces.get(i);
            int[][] shape = piece.getShape();
            int slotXStart = i * slotWidth;
            int pieceWidthPixels = piece.getCols() * PREVIEW_CELL_SIZE;
            int offsetX = (slotWidth - pieceWidthPixels) / 2;

            for (int r = 0; r < piece.getRows(); r++) {
                for (int c = 0; c < piece.getCols(); c++) {
                    if (shape[r][c] == 1) {
                        int drawX = slotXStart + offsetX + (c * PREVIEW_CELL_SIZE);
                        int drawY = startY + (r * PREVIEW_CELL_SIZE);

                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(drawX, drawY, PREVIEW_CELL_SIZE, PREVIEW_CELL_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(drawX, drawY, PREVIEW_CELL_SIZE, PREVIEW_CELL_SIZE);
                    }
                }
            }
        }

        // 4. Draw the piece dynamically moving under the cursor
        if (selectedPiece != null && mousePos != null) {
            int[][] shape = selectedPiece.getShape();

            for (int r = 0; r < selectedPiece.getRows(); r++) {
                for (int c = 0; c < selectedPiece.getCols(); c++) {
                    if (shape[r][c] == 1) {
                        int drawX = mousePos.x + (c * CELL_SIZE) - (CELL_SIZE / 2);
                        int drawY = mousePos.y + (r * CELL_SIZE) - (CELL_SIZE / 2);

                        g.setColor(Color.BLUE);
                        g.fillRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLACK);
                        g.drawRect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

        // 5. Draw Game Over Overlay Screen
        if (game.isGameOver()) {
            // Draw a dark semi-transparent tint overlay across the entire window
            g.setColor(new Color(0, 0, 0, 180)); // 180 alpha handles the transparency blend
            g.fillRect(0, 0, 400, 650);

            // Draw "GAME OVER" message text
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String overText = "GAME OVER";
            int overX = (400 - g.getFontMetrics().stringWidth(overText)) / 2;
            g.drawString(overText, overX, 280);

            // Draw final score notice
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            String finalScoreText = "Final Score: " + game.getScore();
            int finalScoreX = (400 - g.getFontMetrics().stringWidth(finalScoreText)) / 2; // Resolved variable name duplication
            g.drawString(finalScoreText, finalScoreX, 330);
        }
    }

    // --- MOUSE LISTENERS ---

    @Override
    public void mousePressed(MouseEvent e) {
        // Block player interaction entirely if the game is over
        if (game.isGameOver()) {
            return;
        }

        int x = e.getX();
        int y = e.getY();

        // Adjusted tray boundaries to account for new vertical scoreboard spacing
        if (y >= 480 && y <= 630) {
            int slotWidth = 400 / 3;
            int clickedSlot = x / slotWidth;

            ArrayList<Piece> activePieces = game.getActivePieces();
            if (clickedSlot >= 0 && clickedSlot < activePieces.size()) {
                selectedPiece = activePieces.get(clickedSlot);
                selectedIndex = clickedSlot;
                mousePos = e.getPoint();
                repaint();
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedPiece != null) {
            mousePos = e.getPoint();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (selectedPiece != null) {
            // Subtracting SCOREBOARD_HEIGHT ensures grid math maps correctly to rows 0-7
            int targetCol = (mousePos.x - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;
            int targetRow = (mousePos.y - SCOREBOARD_HEIGHT - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;

            game.placePiece(selectedPiece, targetRow, targetCol);

            selectedPiece = null;
            selectedIndex = -1;
            mousePos = null;

            repaint();
        }
    }

    // Mandatory compiler requirement overrides for interfaces
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
