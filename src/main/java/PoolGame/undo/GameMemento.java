package PoolGame.undo;

public class GameMemento {
    private boolean win;
    private int score;
    private int timer;

    public GameMemento(boolean win, int score, int timer) {
        this.win = win;
        this.score = score;
        this.timer = timer;
    }

    /**
     * Getter method for win
     *
     * @return win
     */
    public boolean getWin() {
        return this.win;
    }

    /**
     * Getter method for game score
     *
     * @return score
     */
    public int getScore() {
        return this.score;
    }

    /**
     * Getter method for timer
     *
     * @return timer
     */
    public int getTimer() {
        return this.timer;
    }
}