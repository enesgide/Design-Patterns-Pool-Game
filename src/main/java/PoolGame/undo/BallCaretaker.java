package PoolGame.undo;

import PoolGame.undo.BallMemento;
import PoolGame.objects.Ball;

import java.util.*;

public class BallCaretaker {
    private HashMap<Ball, BallMemento> saves = new HashMap<>();

    public void setSave(Ball ball, BallMemento memento) {
        saves.put(ball, memento);
    }

    /**
     * Saves the ball's state
     *
     * @param ball
     * @return ball memento
     */
    public BallMemento getSave(Ball ball) {
        BallMemento saveCache = saves.get(ball);
        clearSave(ball);
        return saveCache;
    }

    /**
     * Removes the ball's saved state
     *
     * @param ball
     */
    private void clearSave(Ball ball) {
        saves.put(ball, null);
    }
}