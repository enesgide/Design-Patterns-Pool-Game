package PoolGame.difficulty;

import PoolGame.*;

public class Easy implements DifficultyLevel {
    @Override
    public void handle() {
        /**
         * Sets the config path to the easy level
         */
        App.setConfigPath("src/main/resources/config_easy.json");
    }
}