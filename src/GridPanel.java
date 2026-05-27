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
    private final int SCOREBOARD_HEIGHT = 85;

    private final int BTN_W = 60;
    private final int BTN_H = 40;
    private final int BTN_X = (400 - BTN_W) / 2;
    private final int BTN_Y = 400;

    private Piece selectedPiece = null;
    private int selectedIndex = -1;
    private Point mousePos = null;

    private Image tileImage = null;
    private Image gridBgImage = null;
    private Image crownImage = null;

    private final javax.swing.Timer animationTimer;
    private final ArrayList<Particle> particles = new ArrayList<>();
    public GridPanel(GameLogic game) {
        this.game = game;
        this.setPreferredSize(new Dimension(400, 685));
        this.setBackground(Color.decode("#364a85"));

        try {
            URL imgURL = getClass().getResource("/tile.png");
            if (imgURL != null) this.tileImage = new ImageIcon(imgURL).getImage();
        } catch (Exception e) { System.out.println("Failed to load tile image."); }

        try {
            URL bgURL = getClass().getResource("/grid_bg.png");
            if (bgURL != null) this.gridBgImage = new ImageIcon(bgURL).getImage();
        } catch (Exception e) { System.out.println("Failed to load grid background."); }

        try {
            URL crownURL = getClass().getResource("/crown.png");
            if (crownURL != null) this.crownImage = new ImageIcon(crownURL).getImage();
        } catch (Exception e) { System.out.println("Failed to load crown image."); }

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        this.animationTimer = new javax.swing.Timer(16, e -> {
            if (game.isAnimating()) {
                game.advanceAnimation();
                if (game.getAnimationTick() == 12) {
                    triggerParticleGeneration();
                }
                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.update();
                    if (p.isDead()) particles.remove(i);
                }
                repaint();
            } else if (!particles.isEmpty()) {
                for (int i = particles.size() - 1; i >= 0; i--) {
                    Particle p = particles.get(i);
                    p.update();
                    if (p.isDead()) particles.remove(i);
                }
                repaint();
            }
        });
        this.animationTimer.start();
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

    private void triggerParticleGeneration() {
        boolean[] rClear = game.getRowsToClear();
        boolean[] cClear = game.getColsToClear();
        java.util.Random rand = new java.util.Random();

        for (int r = 0; r < 8; r++) {
            if (rClear[r]) {
                int yCenter = (r * CELL_SIZE) + SCOREBOARD_HEIGHT + (CELL_SIZE / 2);
                for (int c = 0; c < 8; c++) {
                    int xCenter = (c * CELL_SIZE) + (CELL_SIZE / 2);
                    for (int p = 0; p < 4; p++) {
                        double speed = 1.5 + rand.nextDouble() * 3.5;
                        double velX = (rand.nextBoolean() ? 1 : -1) * speed;
                        double velY = (rand.nextDouble() - 0.5) * 1.5;
                        particles.add(new Particle(xCenter, yCenter, velX, velY, 6 + rand.nextInt(6)));
                    }
                }
            }
        }

        for (int c = 0; c < 8; c++) {
            if (cClear[c]) {
                int xCenter = (c * CELL_SIZE) + (CELL_SIZE / 2);
                for (int r = 0; r < 8; r++) {
                    int yCenter = (r * CELL_SIZE) + SCOREBOARD_HEIGHT + (CELL_SIZE / 2);
                    for (int p = 0; p < 4; p++) {
                        double speed = 1.5 + rand.nextDouble() * 3.5;
                        double velX = (rand.nextDouble() - 0.5) * 1.5;
                        double velY = (rand.nextBoolean() ? 1 : -1) * speed;
                        particles.add(new Particle(xCenter, yCenter, velX, velY, 6 + rand.nextInt(6)));
                    }
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. High Score
        g.setColor(Color.decode("#fcb42b"));
        g.setFont(new Font("my_font", Font.BOLD, 18));
        String topScoreText = Integer.toString(game.getHighScore());
        if (crownImage != null) {
            g.drawImage(crownImage, 15, 12, 22, 22, this);
            g.drawString(topScoreText, 43, 30);
        } else {
            g.drawString("👑 " + topScoreText, 15, 30);
        }

        // 2. Score Layout
        g.setColor(Color.WHITE);
        g.setFont(new Font("my_font", Font.BOLD, 32));
        String scoreText = Integer.toString(game.getScore());
        int scoreX = (400 - g.getFontMetrics().stringWidth(scoreText)) / 2;
        g.drawString(scoreText, scoreX, 68);

        // 3. Grid Background
        if (gridBgImage != null) {
            g.drawImage(gridBgImage, 0, SCOREBOARD_HEIGHT, 400, 400, this);
        }

        // 4. Board Grid Layout
        int[][] board = game.getGrid();
        boolean[] rClear = game.getRowsToClear();
        boolean[] cClear = game.getColsToClear();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int x = col * CELL_SIZE;
                int y = (row * CELL_SIZE) + SCOREBOARD_HEIGHT;

                if (game.isAnimating() && (rClear[row] || cClear[col])) {
                    if (game.getAnimationTick() < 12) {
                        drawIndividualTile(g, x, y, CELL_SIZE);
                    } else {
                        if (gridBgImage == null) {
                            g.setColor(Color.WHITE);
                            g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        }
                        g.setColor(new Color(0, 0, 0, 40));
                        g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    }
                } else if (board[row][col] == 1) {
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

        // Real-Time Hover Prediction Glow Highlight Layer
        if (selectedPiece != null && mousePos != null && !game.isGameOver() && !game.isAnimating()) {
            int targetCol = (mousePos.x - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;
            int targetRow = (mousePos.y - SCOREBOARD_HEIGHT - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;

            boolean[][] clearPredictions = game.predictLinesToClear(selectedPiece, targetRow, targetCol);
            boolean[] predRows = clearPredictions[0];
            boolean[] predCols = clearPredictions[1];

            g.setColor(new Color(0, 210, 255, 120)); // Semi-transparent Cyan

            for (int r = 0; r < 8; r++) {
                if (predRows[r]) g.fillRect(0, (r * CELL_SIZE) + SCOREBOARD_HEIGHT, 400, CELL_SIZE);
            }
            for (int c = 0; c < 8; c++) {
                if (predCols[c]) g.fillRect(c * CELL_SIZE, SCOREBOARD_HEIGHT, CELL_SIZE, 400);
            }
        }

        // Laser Beams Layer (Phase 1)
        if (game.isAnimating() && game.getAnimationTick() < 15) {
            int tick = game.getAnimationTick();
            int alpha = (tick > 8) ? Math.max(0, 255 - ((tick - 8) * 35)) : 255;
            g.setColor(new Color(0, 220, 255, alpha));

            for (int r = 0; r < 8; r++) {
                if (rClear[r]) g.fillRect(0, (r * CELL_SIZE) + SCOREBOARD_HEIGHT + 2, 400, CELL_SIZE - 4);
            }
            for (int c = 0; c < 8; c++) {
                if (cClear[c]) g.fillRect((c * CELL_SIZE) + 2, SCOREBOARD_HEIGHT, CELL_SIZE - 4, 400);
            }
        }

        // Particle Drift Layer (Phase 2)
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).draw(g);
        }
        // 5. Available Pieces Tray
        ArrayList<Piece> activePieces = game.getActivePieces();
        int slotWidth = 400 / 3;
        int startY = 535;

        for (int i = 0; i < activePieces.size(); i++) {
            if (i == selectedIndex) continue;

            Piece piece = activePieces.get(i);
            int[][] shape = piece.getShape();
            int slotXStart = i * slotWidth;
            int pieceWidthPixels = piece.getCols() * PREVIEW_CELL_SIZE;
            int offsetX = (slotWidth - pieceWidthPixels) / 2;

            for (int r = 0; r < piece.getRows(); r++) {
                for (int c = 0; c < piece.getCols(); c++) {
                    if (shape[r][c] == 1) {
                        drawIndividualTile(g, slotXStart + offsetX + (c * PREVIEW_CELL_SIZE), startY + (r * PREVIEW_CELL_SIZE), PREVIEW_CELL_SIZE);
                    }
                }
            }
        }

        // 6. Floating Dragged Piece
        if (selectedPiece != null && mousePos != null) {
            int[][] shape = selectedPiece.getShape();
            for (int r = 0; r < selectedPiece.getRows(); r++) {
                for (int c = 0; c < selectedPiece.getCols(); c++) {
                    if (shape[r][c] == 1) {
                        drawIndividualTile(g, mousePos.x + (c * CELL_SIZE) - (CELL_SIZE / 2), mousePos.y + (r * CELL_SIZE) - (CELL_SIZE / 2), CELL_SIZE);
                    }
                }
            }
        }

        // 7. Game Over Overlay
        if (game.isGameOver()) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, 400, 685);

            g.setColor(Color.RED);
            g.setFont(new Font("my_font", Font.BOLD, 36));
            String overText = "GAME OVER";
            g.drawString(overText, (400 - g.getFontMetrics().stringWidth(overText)) / 2, 300);

            g.setColor(Color.WHITE);
            g.setFont(new Font("my_font", Font.BOLD, 20));
            String finalScoreText = "Final Score: " + game.getScore();
            g.drawString(finalScoreText, (400 - g.getFontMetrics().stringWidth(finalScoreText)) / 2, 350);

            g.setColor(new Color(34, 177, 76));
            g.fillRect(BTN_X, BTN_Y, BTN_W, BTN_H);
            g.setColor(Color.WHITE);
            g.drawRect(BTN_X, BTN_Y, BTN_W, BTN_H);

            int triWidth = 16, triHeight = 18;
            int startTriX = BTN_X + (BTN_W - triWidth) / 2;
            int startTriY = BTN_Y + (BTN_H - triHeight) / 2;
            int[] xPoints = { startTriX, startTriX + triWidth, startTriX };
            int[] yPoints = { startTriY, startTriY + (triHeight / 2), startTriY + triHeight };
            g.fillPolygon(new Polygon(xPoints, yPoints, 3));
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if (game.isGameOver()) {
            int mx = e.getX(), my = e.getY();
            if (mx >= BTN_X && mx <= (BTN_X + BTN_W) && my >= BTN_Y && my <= (BTN_Y + BTN_H)) {
                game.restartGame();
                repaint();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (game.isGameOver() || game.isAnimating()) return;

        int x = e.getX(), y = e.getY();
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
