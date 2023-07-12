package PoolGame.strategy;

/** Holds strategy for when balls enter a pocket. */
public abstract class PocketStrategy {
    /** Number of lives the ball has. */
    protected int lives;

    public int getLives() {
        return this.lives;
    }

    public void setLives(int newLives) {
        this.lives = newLives;
    }

    /**
     * Removes a life from the ball and determines if ball should be active.
     * 
     * @return true if ball should be removed, false otherwise.
     */
    public boolean remove() {
        this.lives--;

        if (this.lives == 0) {
            return true;
        }
        return false;
    }

    /**
     * Resets the ball to its original state.
     */
    public abstract void reset();
}
