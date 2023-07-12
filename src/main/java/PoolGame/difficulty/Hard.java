package PoolGame.difficulty;

import PoolGame.*;

public class Hard implements DifficultyLevel {
    @Override
    public void handle() {
        /**
         * Sets the config path to the hard level
         */
        App.setConfigPath("src/main/resources/config_hard.json");
    }
}