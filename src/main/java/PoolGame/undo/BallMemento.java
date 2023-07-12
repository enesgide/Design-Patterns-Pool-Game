package PoolGame.undo;

public class BallMemento {
    private double x;
    private double y;
    private boolean active;
    private int lives;

    public BallMemento(boolean active, double x, double y, int lives) {
        this.active = active;
        this.x = x;
        this.y = y;
        this.lives = lives;
    }

    /**
     * Getter method for ball's x position
     *
     * @return x position
     */
    public double getX() {
        return this.x;
    }

    /**
     * Getter method for ball's y position
     *
     * @return y position
     */
    public double getY() {
        return this.y;
    }

    /**
     * Check if ball is active
     *
     * @return true if ball is active
     */
    public boolean getActive() {
        return this.active;
    }

    /**
     * Getter method for ball's lives
     *
     * @return lives
     */
    public int getLives() {
        return this.lives;
    }
}