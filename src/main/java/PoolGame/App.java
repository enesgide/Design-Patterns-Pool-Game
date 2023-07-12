package PoolGame;

import PoolGame.config.*;
import PoolGame.difficulty.*;

import java.util.*;

import javafx.application.Application;
import javafx.stage.Stage;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

/** Main application entry point. */
public class App extends Application {
    private static String configPath;
    /**
     * @param args First argument is the path to the config file
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    /**
     * Starts the application.
     * 
     * @param primaryStage The primary stage for the application.
     */
    public void start(Stage primaryStage) {
        GameManager gameManager = new GameManager();
        gameManager.setDifficultyLevel(new Easy());

        List<String> args = getParameters().getRaw();
        checkConfig(args);

        startGame(primaryStage, gameManager);
        gameManager.run();
    }

    /**
     * Starts the game by setting up a new level and scene in gameManager
     *
     * @param primaryStage The primary stage for the application.
     * @param gameManager The game manager for the application.
     */
    private void startGame(Stage primaryStage, GameManager gameManager) {
        setLevel(gameManager);

        primaryStage.setTitle("Pool");
        primaryStage.setScene(gameManager.getScene());
        primaryStage.show();

        connectKeyEvents(primaryStage, gameManager);
    }

    /**
     * Restarts the game by calling startGame() again
     *
     * @param primaryStage The primary stage for the application.
     * @param gameManager The game manager for the application.
     */
    private void restart(Stage stage, GameManager gameManager) {
        startGame(stage, gameManager);
    }

    /**
     * Updates the game's difficulty level by reading the config file and building the game manager.
     *
     * @param gameManager The game manager for the application.
     */
    private static void setLevel(GameManager gameManager) {
        ReaderFactory tableFactory = new TableReaderFactory();
        Reader tableReader = tableFactory.buildReader();
        tableReader.parse(configPath, gameManager);

        ReaderFactory pocketFactory = new PocketReaderFactory();
        Reader pocketReader = pocketFactory.buildReader();
        pocketReader.parse(configPath, gameManager);

        ReaderFactory ballFactory = new BallReaderFactory();
        Reader ballReader = ballFactory.buildReader();
        ballReader.parse(configPath, gameManager);

        gameManager.buildManager();
    }

    /**
     * Updates the config path based on the game's difficulty level state
     *
     * @param stage The primary stage for the application.
     * @param gameManager The game manager for the application.
     */
    private void changeLevel(Stage stage, GameManager gameManager) {
        gameManager.getDifficultyLevel().handle();
        restart(stage, gameManager);
    }

    /**
     * Connects the key events for changing the difficulty level state
     *
     * @param stage The primary stage for the application.
     * @param gameManager The game manager for the application.
     */
    private void connectKeyEvents(Stage stage, GameManager gameManager) {
        gameManager.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            // May fire key events extra times after changing difficulty level
            public void handle(KeyEvent key) {
                // Easy difficulty
                if (key.getCode() == KeyCode.DIGIT1) {
                    //System.out.println("Easy key pressed: " + key.getCode());
                    gameManager.setDifficultyLevel(new Easy());
                    changeLevel(stage, gameManager);
                }
                // Normal difficulty
                if (key.getCode() == KeyCode.DIGIT2) {
                    gameManager.setDifficultyLevel(new Normal());
                    changeLevel(stage, gameManager);
                }
                // Hard difficulty
                if (key.getCode() == KeyCode.DIGIT3) {
                    gameManager.setDifficultyLevel(new Hard());
                    changeLevel(stage, gameManager);
                }
                key.consume();
            }
        });
    }

    /**
     * Checks if the initial config file path is given as an argument.
     * 
     * @param args
     * @return config path.
     */
    private static void checkConfig(List<String> args) {
        if (args.size() > 0) {
            configPath = args.get(0);
        } else {
            configPath = "src/main/resources/config_easy.json";
        }
    }

    /**
     * Updates the config file path when the difficulty level is changed
     *
     * @param path The new config pile path
     */
    public static void setConfigPath(String path) {
        configPath = path;
    }
}
