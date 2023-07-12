package PoolGame.objects;

import java.util.*;

import PoolGame.strategy.PocketStrategy;
import PoolGame.strategy.BallStrategy;
import PoolGame.strategy.BlueStrategy;
import PoolGame.strategy.BlackStrategy;

/** Builds pool balls. */
public class PoolBallBuilder implements BallBuilder {
    // Required Parameters
    private String colour;
    private double xPosition;
    private double yPosition;
    private double xVelocity;
    private double yVelocity;
    private double mass;

    // Variable Parameters
    private boolean isCue = false;
    private int score;
    public PocketStrategy strategy;

    @Override
    public void setColour(String colour) {
        this.colour = colour;
    };

    @Override
    public void setxPos(double xPosition) {
        this.xPosition = xPosition;
    };

    @Override
    public void setyPos(double yPosition) {
        this.yPosition = yPosition;
    };

    @Override
    public void setxVel(double xVelocity) {
        this.xVelocity = xVelocity;
    };

  @Override
    public void setyVel(double yVelocity) {
        this.yVelocity = yVelocity;
    };

    @Override
    public void setMass(double mass) {
        this.mass = mass;
    };



    /**
     * Builds the ball.
     * 
     * @return ball
     */
    public Ball build() {
        /**
         * Contains the colour, score, and initial lives of each ball
         */
        HashMap<String, ArrayList<Integer>> values = new HashMap<String, ArrayList<Integer>>();
        values.put("white", new ArrayList<Integer>(Arrays.asList(0, 1)));
        values.put("red", new ArrayList<Integer>(Arrays.asList(1, 1)));
        values.put("yellow", new ArrayList<Integer>(Arrays.asList(2, 1)));
        values.put("green", new ArrayList<Integer>(Arrays.asList(3, 2)));
        values.put("brown", new ArrayList<Integer>(Arrays.asList(4, 3)));
        values.put("blue", new ArrayList<Integer>(Arrays.asList(5, 2)));
        values.put("purple", new ArrayList<Integer>(Arrays.asList(6, 2)));
        values.put("black", new ArrayList<Integer>(Arrays.asList(7, 3)));
        values.put("orange", new ArrayList<Integer>(Arrays.asList(8, 1)));

        score = values.get(colour).get(0);

        if (colour.equals("white")) {
            isCue = true;
        }

        /**
         * Set the strategy for each ball
         */
        int myLives = values.get(colour).get(1);
        if (myLives == 1) {
            strategy = new BallStrategy();
        } else if (myLives == 2) {
            strategy = new BlueStrategy();
        } else if (myLives == 3) {
            strategy = new BlackStrategy();
        }

        return new Ball(colour, xPosition, yPosition, xVelocity, yVelocity, mass, isCue, strategy, score);
    }
}
