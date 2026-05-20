public class Piece {
    private final int[][] shape;

    public Piece(int[][] shape) {
        this.shape = shape;
    }

    public int[][] getShape() {
        return shape;
    }

    public int getRows() {
        return shape.length;
    }

    public int getCols() {
        return shape[0].length; // Fixed to read column length
    }
}
