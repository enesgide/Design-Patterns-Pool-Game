package PoolGame.difficulty;

import PoolGame.*;

public class Normal implements DifficultyLevel {
    @Override
    public void handle() {
        /**
         * Sets the config path to the normal level
         */
        App.setConfigPath("src/main/resources/config_normal.json");
    }
}