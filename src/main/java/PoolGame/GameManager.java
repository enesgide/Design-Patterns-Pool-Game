package PoolGame;

import PoolGame.objects.*;
import PoolGame.undo.*;
import PoolGame.difficulty.*;

import java.util.*;
import java.lang.Math;

import javafx.geometry.Point2D;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.scene.shape.Line;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Paint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import javafx.util.*;

/**
 * Controls the game interface; drawing objects, handling logic and collisions.
 */
public class GameManager {
    private Table table;
    private ArrayList<Ball> balls = new ArrayList<Ball>();
    private static BallCaretaker ballCaretaker = new BallCaretaker();
    private GameCaretaker gameCaretaker = new GameCaretaker();
    private Line cue;
    private boolean cueSet;
    private boolean cueActive;
    private boolean winFlag;
    private int score;
    private int timer;
    private int frameCount;
    private DifficultyLevel level;

    private final double TABLEBUFFER = Config.getTableBuffer();
    private final double TABLEEDGE = Config.getTableEdge();
    private final double FORCEFACTOR = 0.1;

    private Scene scene;
    private GraphicsContext gc;

    /**
     * Initialises timeline and cycle count.
     */
    public void run() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(17),
                t -> this.draw()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Builds GameManager properties such as initialising pane, canvas,
     * graphicscontext, and setting events related to clicks.
     */
    public void buildManager() {
        Pane pane = new Pane();
        this.scene = new Scene(pane, table.getxLength() + TABLEBUFFER * 2, table.getyLength() + TABLEBUFFER * 2);
        setClickEvents(this.scene, pane);
        Canvas canvas = new Canvas(table.getxLength() + TABLEBUFFER * 2, table.getyLength() + TABLEBUFFER * 2);
        gc = canvas.getGraphicsContext2D();
        pane.getChildren().add(canvas);
        setupCheatButtons(pane);

        this.score = 0;
        this.timer = 0;
        this.frameCount = 1;
        this.cueSet = false;
        this.cueActive = false;
        this.winFlag = false;
    }

    /**
     * Draws all relevant items - table, cue, balls, pockets - onto Canvas.
     * Used Exercise 6 as reference.
     */
    private void draw() {
        tick();

        // Fill in background
        gc.setFill(Paint.valueOf("white"));
        gc.fillRect(0, 0, table.getxLength() + TABLEBUFFER * 2, table.getyLength() + TABLEBUFFER * 2);

        // Fill in edges
        gc.setFill(Paint.valueOf("brown"));
        gc.fillRect(TABLEBUFFER - TABLEEDGE, TABLEBUFFER - TABLEEDGE, table.getxLength() + TABLEEDGE * 2,
                table.getyLength() + TABLEEDGE * 2);

        // Fill in Table
        gc.setFill(table.getColour());
        gc.fillRect(TABLEBUFFER, TABLEBUFFER, table.getxLength(), table.getyLength());

        // Fill in Pockets
        for (Pocket pocket : table.getPockets()) {
            gc.setFill(Paint.valueOf("black"));
            gc.fillOval(pocket.getxPos() - pocket.getRadius(), pocket.getyPos() - pocket.getRadius(),
                    pocket.getRadius() * 2, pocket.getRadius() * 2);
        }

        // Cue
        if (this.cue != null && cueActive) {
            // Stick
            gc.setStroke(Paint.valueOf("burlywood"));
            gc.setLineWidth(3);
            gc.strokeLine(cue.getStartX(), cue.getStartY(), cue.getEndX(), cue.getEndY());
            // Tip
            gc.setFill(Paint.valueOf("darkred"));
            int cueRectW = 10;
            gc.fillRect(cue.getEndX() - cueRectW/2, cue.getEndY() - cueRectW/2, cueRectW, cueRectW);
        }

        for (Ball ball : balls) {
            if (ball.isActive()) {
                gc.setFill(ball.getColour());
                gc.fillOval(ball.getxPos() - ball.getRadius(),
                        ball.getyPos() - ball.getRadius(),
                        ball.getRadius() * 2,
                        ball.getRadius() * 2);
            }

        }

        // Score
        gc.setFill(Paint.valueOf("black"));
        Font newFont = Font.font("Calibri", FontWeight.BOLD, 25);
        gc.setFont(newFont);
        gc.fillText(String.format("Score: %s", score), table.getxLength() / 2 + TABLEBUFFER - 100, 30);

        // Timer
        int minutes = Math.floorDiv(timer, 60);
        int seconds = timer - (minutes * 60);
        gc.fillText(String.format("Timer: %d:%02d", minutes, seconds), table.getxLength() / 2 + TABLEBUFFER + 30, 30);

        // Win
        if (winFlag) {
            gc.setStroke(Paint.valueOf("white"));
            gc.setFont(new Font("Impact", 80));
            gc.strokeText("Win and bye", table.getxLength() / 2 + TABLEBUFFER - 180,
                    table.getyLength() / 2 + TABLEBUFFER);
        }

    }

    /**
     * Updates positions of all balls, handles logic related to collisions.
     * Used Exercise 6 as reference.
     */
    public void tick() {
        if (winFlag != true) {
            if (frameCount == 60) {
                frameCount = 1;
                timer++;
            } else {
                frameCount++;
            }
        }

        boolean winning = true;
        for (Ball ball: balls) {
            if (ball.isActive() && !ball.isCue()) {
                winning = false;
            }
        }

        if (winning) {
            winFlag = true;
        }

        /*if (score == balls.size() - 1) {
            winFlag = true;
        }*/

        for (Ball ball : balls) {
            ball.tick();

            if (ball.isCue() && cueSet) {
                hitBall(ball);
            }

            double width = table.getxLength();
            double height = table.getyLength();

            // Check if ball landed in pocket
            for (Pocket pocket : table.getPockets()) {
                if (pocket.isInPocket(ball)) {
                    if (ball.isCue()) {
                        this.reset();
                    } else {
                        if (ball.remove()) {
                            score += ball.getScore();
                        } else {
                            // Check if when ball is removed, any other balls are present in its space. (If
                            // another ball is present, blue ball is removed)
                            for (Ball otherBall : balls) {
                                double deltaX = ball.getxPos() - otherBall.getxPos();
                                double deltaY = ball.getyPos() - otherBall.getyPos();
                                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                                if (otherBall != ball && otherBall.isActive() && distance < 10) {
                                    ball.remove();
                                }
                            }
                        }
                    }
                    break;
                }
            }

            // Handle the edges (balls don't get a choice here)
            if (ball.getxPos() + ball.getRadius() > width + TABLEBUFFER) {
                ball.setxPos(width - ball.getRadius());
                ball.setxVel(ball.getxVel() * -1);
            }
            if (ball.getxPos() - ball.getRadius() < TABLEBUFFER) {
                ball.setxPos(ball.getRadius());
                ball.setxVel(ball.getxVel() * -1);
            }
            if (ball.getyPos() + ball.getRadius() > height + TABLEBUFFER) {
                ball.setyPos(height - ball.getRadius());
                ball.setyVel(ball.getyVel() * -1);
            }
            if (ball.getyPos() - ball.getRadius() < TABLEBUFFER) {
                ball.setyPos(ball.getRadius());
                ball.setyVel(ball.getyVel() * -1);
            }

            // Apply table friction
            double friction = table.getFriction();
            ball.setxVel(ball.getxVel() * friction);
            ball.setyVel(ball.getyVel() * friction);

            // Check ball collisions
            for (Ball ballB : balls) {
                if (ball.isActive() && ballB.isActive() && checkCollision(ball, ballB)) {
                    Point2D ballPos = new Point2D(ball.getxPos(), ball.getyPos());
                    Point2D ballBPos = new Point2D(ballB.getxPos(), ballB.getyPos());
                    Point2D ballVel = new Point2D(ball.getxVel(), ball.getyVel());
                    Point2D ballBVel = new Point2D(ballB.getxVel(), ballB.getyVel());
                    Pair<Point2D, Point2D> changes = calculateCollision(ballPos, ballVel, ball.getMass(), ballBPos,
                            ballBVel, ballB.getMass(), false);
                    calculateChanges(changes, ball, ballB);
                }
            }
        }
    }

    /**
     * Resets the game.
     */
    public void reset() {
        for (Ball ball : balls) {
            ball.reset();
        }

        this.score = 0;
        this.frameCount = 0;
        this.timer = 0;
    }

    /**
     * @return level
     */
    public DifficultyLevel getDifficultyLevel() {
        return this.level;
    }

    /**
     * @param level
     */
    public void setDifficultyLevel(DifficultyLevel level) {
        this.level = level;
    }

    /**
     * @return scene.
     */
    public Scene getScene() {
        return this.scene;
    }

    /**
     * Sets the table of the game.
     * 
     * @param table
     */
    public void setTable(Table table) {
        this.table = table;
    }

    /**
     * @return table
     */
    public Table getTable() {
        return this.table;
    }

    /**
     * Sets the balls of the game.
     * 
     * @param balls
     */
    public void setBalls(ArrayList<Ball> balls) {
        this.balls = balls;
    }


    /**
     * Gets the cue ball
     *
     * @return cue ball
     */
    public Ball getCueBall() {
        for (Ball ball: balls) {
            if (ball.isCue()) {
                return ball;
            }
        }
        return null;
    }

    /**
     * Hits the ball with the cue, distance of the cue indicates the strength of the
     * strike.
     *
     * @param ball
     */
    private void hitBall(Ball ball) {
        if (this.cue == null) {
            return;
        }

        double deltaX = ball.getxPos() - cue.getStartX();
        double deltaY = ball.getyPos() - cue.getStartY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Check that start of cue is within cue ball
        if (distance < ball.getRadius()) {
            // Collide ball with cue
            double hitxVel = (cue.getStartX() - cue.getEndX()) * FORCEFACTOR;
            double hityVel = (cue.getStartY() - cue.getEndY()) * FORCEFACTOR;

            ball.setxVel(hitxVel);
            ball.setyVel(hityVel);
        }

        for (Ball b: balls) {
            ballCaretaker.setSave(b, b.save());
        }

        gameCaretaker.setSave(save());

        cueSet = false;
    }

    /**
     * Changes values of balls based on collision (if ball is null ignore it)
     * 
     * @param changes
     * @param ballA
     * @param ballB
     */
    private void calculateChanges(Pair<Point2D, Point2D> changes, Ball ballA, Ball ballB) {
        ballA.setxVel(changes.getKey().getX());
        ballA.setyVel(changes.getKey().getY());
        if (ballB != null) {
            ballB.setxVel(changes.getValue().getX());
            ballB.setyVel(changes.getValue().getY());
        }
    }

    /**
     * Sets the cue to be drawn on click, and manages cue actions
     * 
     * @param pane
     */
    private void setClickEvents(Scene scene, Pane pane) {
        GameManager gameManager = this;

        pane.setOnMousePressed(event -> {
            cueSet = false;

            Ball cueBall = getCueBall();
            if (cueBall != null) {
                double vecX = event.getX() - cueBall.getxPos();
                double vecY = event.getY() - cueBall.getyPos();
                double mag = Math.sqrt(Math.pow(vecX, 2) + Math.pow(vecY, 2));
                if (mag <= cueBall.getRadius()) {
                    cue = new Line(cueBall.getxPos(), cueBall.getyPos(), event.getX(), event.getY());
                    cueActive = true;
                }
            }
        });

        pane.setOnMouseDragged(event -> {
            if (cue != null) {
                cue.setEndX(event.getX());
                cue.setEndY(event.getY());
            }
        });

        pane.setOnMouseReleased(event -> {
            if (cueActive) {
                cueSet = true;
                cueActive = false;
            }
        });

        /**
         * Event that fires when the undo key is pressed (U), and then undo the game state
         */
        scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            public void handle(KeyEvent key) {
                // Undo
                if (key.getCode() == KeyCode.U) {
                    // Retrieve ball save
                    for (Ball ball: balls) {
                        BallMemento ballSave = ballCaretaker.getSave(ball);
                        if (ballSave != null) {
                            ball.recover(ballSave);
                        }
                    }
                    // Retrieve game save
                    GameMemento gameSave = gameCaretaker.getSave();
                    if (gameSave != null) {
                        recover(gameSave);
                    }

                    key.consume();
                }
            }
        });
    }

    /**
     * Sets up the cheat buttons for each ball colour
     *
     * @param pane
     */
    private void setupCheatButtons(Pane pane) {
        Label label = new Label("Cheats:");
        label.setStyle("-fx-font-weight: bold");
        label.setPrefWidth(60);
        label.setPrefHeight(25);
        label.setLayoutX(10);
        label.setLayoutY(table.getyLength() + TABLEBUFFER * 2 - 30);
        pane.getChildren().add(label);

        List<String> colourNames = new ArrayList<String>(Arrays.asList("Red", "Yellow", "Green", "Brown", "Blue", "Purple", "Black", "Orange"));

        for (int i = 0; i < colourNames.size(); i++) {
            String name = colourNames.get(i);
            Paint colour = Paint.valueOf(name);

            Button button = new Button(name);
            button.setPrefWidth(60);
            button.setPrefHeight(25);
            button.setLayoutX(65 * (1 + i));
            button.setLayoutY(table.getyLength() + TABLEBUFFER * 2 - 30);
            pane.getChildren().add(button);

            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    // Save before cheat
                    for (Ball b: balls) {
                        ballCaretaker.setSave(b, b.save());
                    }
                    gameCaretaker.setSave(save());

                    cheat(colour);
                }
            });
        }
    }

    /**
     * Performs the cheat by removing balls of the specified colour
     *
     * @param colour The paint colour of balls that should be removed
     */
    public void cheat(Paint colour) {
        for (Ball ball: balls) {
            if (ball.isActive() && ball.getColour() == colour) {
                ball.setActive(false);
                this.score += ball.getScore();
            }
        }
    }

    /**
     * Checks if two balls are colliding.
     * Used Exercise 6 as reference.
     * 
     * @param ballA
     * @param ballB
     * @return true if colliding, false otherwise
     */
    private boolean checkCollision(Ball ballA, Ball ballB) {
        if (ballA == ballB) {
            return false;
        }

        return Math.abs(ballA.getxPos() - ballB.getxPos()) < ballA.getRadius() + ballB.getRadius() &&
                Math.abs(ballA.getyPos() - ballB.getyPos()) < ballA.getRadius() + ballB.getRadius();
    }

    /**
     * Collision function adapted from assignment, using physics algorithm:
     * http://www.gamasutra.com/view/feature/3015/pool_hall_lessons_fast_accurate_.php?page=3
     *
     * @param positionA The coordinates of the centre of ball A
     * @param velocityA The delta x,y vector of ball A (how much it moves per tick)
     * @param massA     The mass of ball A (for the moment this should always be the
     *                  same as ball B)
     * @param positionB The coordinates of the centre of ball B
     * @param velocityB The delta x,y vector of ball B (how much it moves per tick)
     * @param massB     The mass of ball B (for the moment this should always be the
     *                  same as ball A)
     *
     * @return A Pair in which the first (key) Point2D is the new
     *         delta x,y vector for ball A, and the second (value) Point2D is the
     *         new delta x,y vector for ball B.
     */
    public static Pair<Point2D, Point2D> calculateCollision(Point2D positionA, Point2D velocityA, double massA,
            Point2D positionB, Point2D velocityB, double massB, boolean isCue) {

        // Find the angle of the collision - basically where is ball B relative to ball
        // A. We aren't concerned with
        // distance here, so we reduce it to unit (1) size with normalize() - this
        // allows for arbitrary radii
        Point2D collisionVector = positionA.subtract(positionB);
        collisionVector = collisionVector.normalize();

        // Here we determine how 'direct' or 'glancing' the collision was for each ball
        double vA = collisionVector.dotProduct(velocityA);
        double vB = collisionVector.dotProduct(velocityB);

        // If you don't detect the collision at just the right time, balls might collide
        // again before they leave
        // each others' collision detection area, and bounce twice.
        // This stops these secondary collisions by detecting
        // whether a ball has already begun moving away from its pair, and returns the
        // original velocities
        if (vB <= 0 && vA >= 0 && !isCue) {
            return new Pair<>(velocityA, velocityB);
        }

        // This is the optimisation function described in the gamasutra link. Rather
        // than handling the full quadratic
        // (which as we have discovered allowed for sneaky typos)
        // this is a much simpler - and faster - way of obtaining the same results.
        double optimizedP = (2.0 * (vA - vB)) / (massA + massB);

        // Now we apply that calculated function to the pair of balls to obtain their
        // final velocities
        Point2D velAPrime = velocityA.subtract(collisionVector.multiply(optimizedP).multiply(massB));
        Point2D velBPrime = velocityB.add(collisionVector.multiply(optimizedP).multiply(massA));

        return new Pair<>(velAPrime, velBPrime);
    }

    /**
     * Saves the current state of the game
     *
     * @return GameMemento The game's new saved state
     */
    public GameMemento save() {
        return new GameMemento(winFlag, score, timer);
    }

    /**
     * Recovers the previous state of the game
     *
     * @param memento The game's previous saved state
     */
    public void recover(GameMemento memento) {
        this.winFlag = memento.getWin();
        this.score = memento.getScore();
        this.timer = memento.getTimer();
        this.frameCount = 1;
        this.cueActive = false;
        this.cueSet = false;
    }
}
