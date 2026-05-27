import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.ArrayList;

public class GridPanel extends JPanel implements MouseListener, MouseMotionListener {
    private final GameLogic game;
    private final int CELL_SIZE = 50;
    private final int GRID_SIZE = 8;
    private final int PREVIEW_CELL_SIZE = 20;

    // Visual offsets expanded to 85px to stack high score above current score safely
    private final int SCOREBOARD_HEIGHT = 85;

    // Restart Button layout bounds (Centered horizontally adjusted for new vertical scale)
    private final int BTN_W = 60;
    private final int BTN_H = 40;
    private final int BTN_X = (400 - BTN_W) / 2;
    private final int BTN_Y = 400;

    // Variables to manage dragging state
    private Piece selectedPiece = null;
    private int selectedIndex = -1;
    private Point mousePos = null;

    // Image Asset Variables
    private Image tileImage = null;
    private Image gridBgImage = null;
    private Image crownImage = null;

    public GridPanel(GameLogic game) {
        this.game = game;
        // Total panel height bumped to 685px to account for the header expansion
        this.setPreferredSize(new Dimension(400, 685));
        this.setBackground(Color.decode("#364a85"));

        // 1. Load the TILE image
        try {
            URL imgURL = getClass().getResource("/tile.png");
            if (imgURL != null) {
                this.tileImage = new ImageIcon(imgURL).getImage();
            } else {
                System.out.println("Error: Could not find tile.png in src folder.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load tile image asset.");
        }

        // 2. Load the GRID BACKGROUND image
        try {
            URL bgURL = getClass().getResource("/grid_bg.png");
            if (bgURL != null) {
                this.gridBgImage = new ImageIcon(bgURL).getImage();
            } else {
                System.out.println("Error: Could not find grid_bg.png in src folder.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load grid background asset.");
        }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        try {
            URL crownURL = getClass().getResource("/crown.png");
            if (crownURL != null) {
                this.crownImage = new ImageIcon(crownURL).getImage();
            } else {
                System.out.println("Error: Could not find crown.png in src folder. Using text fallback.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load crown image asset.");
        }
    }

    private void drawIndividualTile(Graphics g, int x, int y, int size) {
        if (tileImage != null) {
            g.drawImage(tileImage, x, y, size, size, this);
        } else {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Top-Left Box: All-Time Top Score Display (#fcb42b)
        // 1. Top-Left Box: All-Time Top Score Display (#fcb42b)
        g.setColor(Color.decode("#fcb42b"));
        g.setFont(new Font("my_font", Font.BOLD, 18));
        String topScoreText = Integer.toString(game.getHighScore());

// If the crown image loaded successfully, draw it as a sprite icon
        if (crownImage != null) {
            // Draws a 22x22 pixel crown icon at coordinates X:15, Y:12
            g.drawImage(crownImage, 15, 12, 22, 22, this);
            // Draw the top score text offset to the right of the crown image (X:43)
            g.drawString(topScoreText, 43, 30);
        } else {
            // Text emoji fallback in case the image fails to load or compile
            g.drawString("👑 " + topScoreText, 15, 30);
        }


        // 2. Center Header: Current Live Score Layout
        g.setColor(Color.WHITE);
        g.setFont(new Font("my_font", Font.BOLD, 32));
        String scoreText = Integer.toString(game.getScore());
        int textWidth = g.getFontMetrics().stringWidth(scoreText);
        int scoreX = (400 - textWidth) / 2;
        g.drawString(scoreText, scoreX, 68); // Placed safely below the crown display line

        // 3. Draw your custom grid image as a background layer
        if (gridBgImage != null) {
            g.drawImage(gridBgImage, 0, SCOREBOARD_HEIGHT, 400, 400, this);
        }

        // 4. Draw the main 8x8 board grid and placed items
        int[][] board = game.getGrid();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int x = col * CELL_SIZE;
                int y = (row * CELL_SIZE) + SCOREBOARD_HEIGHT;

                if (board[row][col] == 1) {
                    drawIndividualTile(g, x, y, CELL_SIZE);
                } else {
                    if (gridBgImage == null) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    }
                    g.setColor(new Color(0, 0, 0, 40));
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        // 5. Draw the available pieces below the board
        ArrayList<Piece> activePieces = game.getActivePieces();
        int slotWidth = 400 / 3;
        int startY = 535; // Shifted down 35px to clear header update layout bounds

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
                        drawIndividualTile(g, drawX, drawY, PREVIEW_CELL_SIZE);
                    }
                }
            }
        }

        // 6. Draw the piece dynamically moving under the cursor
        if (selectedPiece != null && mousePos != null) {
            int[][] shape = selectedPiece.getShape();

            for (int r = 0; r < selectedPiece.getRows(); r++) {
                for (int c = 0; c < selectedPiece.getCols(); c++) {
                    if (shape[r][c] == 1) {
                        int drawX = mousePos.x + (c * CELL_SIZE) - (CELL_SIZE / 2);
                        int drawY = mousePos.y + (r * CELL_SIZE) - (CELL_SIZE / 2);
                        drawIndividualTile(g, drawX, drawY, CELL_SIZE);
                    }
                }
            }
        }

        // 7. Draw Game Over Overlay Screen
        if (game.isGameOver()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 400, 685);

            g.setColor(Color.RED);
            g.setFont(new Font("my_font", Font.BOLD, 36));
            String overText = "GAME OVER";
            int overX = (400 - g.getFontMetrics().stringWidth(overText)) / 2;
            g.drawString(overText, overX, 300);

            g.setColor(Color.WHITE);
            g.setFont(new Font("my_font", Font.BOLD, 20));
            String finalScoreText = "Final Score: " + game.getScore();
            int finalScoreX = (400 - g.getFontMetrics().stringWidth(finalScoreText)) / 2;
            g.drawString(finalScoreText, finalScoreX, 350);

            g.setColor(new Color(34, 177, 76));
            g.fillRect(BTN_X, BTN_Y, BTN_W, BTN_H);
            g.setColor(Color.WHITE);
            g.drawRect(BTN_X, BTN_Y, BTN_W, BTN_H);

            int triWidth = 16;
            int triHeight = 18;
            int startTriX = BTN_X + (BTN_W - triWidth) / 2;
            int startTriY = BTN_Y + (BTN_H - triHeight) / 2;

            int[] xPoints = { startTriX, startTriX + triWidth, startTriX };
            int[] yPoints = { startTriY, startTriY + (triHeight / 2), startTriY + triHeight };

            g.fillPolygon(new Polygon(xPoints, yPoints, 3));
        }
    }

    // --- MOUSE LISTENERS ---

    @Override
    public void mouseClicked(MouseEvent e) {
        if (game.isGameOver()) {
            int mx = e.getX();
            int my = e.getY();
            if (mx >= BTN_X && mx <= (BTN_X + BTN_W) && my >= BTN_Y && my <= (BTN_Y + BTN_H)) {
                game.restartGame();
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (game.isGameOver()) return;

        int x = e.getX();
        int y = e.getY();
        // Dynamic tray boundary limits shifted to reflect the extended board layout
        if (y >= 515 && y <= 665) {
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
            int targetCol = (mousePos.x - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;
            int targetRow = (mousePos.y - SCOREBOARD_HEIGHT - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;

            game.placePiece(selectedPiece, targetRow, targetCol);

            selectedPiece = null;
            selectedIndex = -1;
            mousePos = null;
            repaint();
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
