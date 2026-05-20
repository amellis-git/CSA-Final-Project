import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("BlockBlast - Version 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Instantiate the game state engine
        GameLogic game = new GameLogic();

        // Pass the engine to the visual panel
        GridPanel gridPanel = new GridPanel(game);
        frame.add(gridPanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
