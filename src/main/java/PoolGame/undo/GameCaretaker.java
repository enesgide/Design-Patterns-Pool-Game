package PoolGame.undo;

import PoolGame.undo.GameMemento;
import PoolGame.GameManager;

public class GameCaretaker {
    private GameMemento save;

    public void setSave(GameMemento memento) {
        this.save = memento;
    }

    /**
     * Saves the game manager's state
     *
     * @return ball memento
     */
    public GameMemento getSave() {
        GameMemento saveCache = this.save;
        clearSave();
        return saveCache;
    }

    /**
     * Removes the game manager's saved state
     *
     * @param ball
     */
    private void clearSave() {
        save = null;
    }
}