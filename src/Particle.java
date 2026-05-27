import java.awt.Color;
import java.awt.Graphics;

public class Particle {
    private double x, y;
    private final double velX, velY;
    private int lifespan; // Remaining frames to render
    private final int size;

    public Particle(double x, double y, double velX, double velY, int size) {
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.size = size;
        this.lifespan = 25; // Lasts for 25 frames
    }

    public void update() {
        x += velX;
        y += velY;
        lifespan--;
    }

    public boolean isDead() {
        return lifespan <= 0;
    }

    public void draw(Graphics g) {
        // Bright cyan color matching Block Blast particles
        g.setColor(new Color(0, 210, 255, (int)(lifespan * 10)));
        g.fillRect((int)x, (int)y, size, size);
    }
}
