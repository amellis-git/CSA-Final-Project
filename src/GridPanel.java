import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
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

    // Variables to manage dragging state
    private Piece selectedPiece = null;
    private int selectedIndex = -1; // Tracks which of the 3 slots was picked
    private Point mousePos = null;   // Current location of mouse cursor

    public GridPanel(GameLogic game) {
        this.game = game;
        this.setPreferredSize(new Dimension(400, 600));
        this.setBackground(Color.LIGHT_GRAY);

        // Register the mouse listeners to this panel
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Draw the main 8x8 board
        int[][] board = game.getGrid();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int x = col * CELL_SIZE;
                int y = row * CELL_SIZE;

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

        // 2. Draw the available pieces below the board
        ArrayList<Piece> activePieces = game.getActivePieces();
        int slotWidth = 400 / 3;
        int startY = 450;

        for (int i = 0; i < activePieces.size(); i++) {
            // Skip rendering the piece in the bottom slot if it is actively being dragged
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

        // 3. Draw the piece dynamically moving under the cursor (scaled to full size)
        if (selectedPiece != null && mousePos != null) {
            int[][] shape = selectedPiece.getShape();

            // Offset the drawing so the mouse cursor grips roughly the top-left section
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
    }

    // --- MOUSE LISTENERS ---

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Only allow selecting a piece if you clicked inside the bottom workspace area
        if (y >= 430 && y <= 580) {
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
            mousePos = e.getPoint(); // Continually track the moving mouse pointer
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (selectedPiece != null) {
            // Find out which grid row/column the top-left segment of the piece is hovering over
            // We subtract half a cell size to align with the drawing offset in paintComponent
            int targetCol = (mousePos.x - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;
            int targetRow = (mousePos.y - (CELL_SIZE / 2) + (CELL_SIZE / 2)) / CELL_SIZE;

            // Attempt to permanently lock the piece onto the grid array backend
            boolean success = game.placePiece(selectedPiece, targetRow, targetCol);

            // Reset drag variables
            selectedPiece = null;
            selectedIndex = -1;
            mousePos = null;

            // Refresh screen to show locked blocks or to send the piece home
            repaint();
        }
    }

    // Unused mandatory listener interfaces
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
}
