package com.github.leblancjs.counter_strike.model;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;

public class Blood {

    public final static float SIZE = 1f;

    private final static float OFFSET = 0.1f;

    private Vector2 position;
    private float rotation;
    private float scale;

    /**
     * Constructor class for a blood stain.
     *
     * @param position : the stain's position
     */
    public Blood(Vector2 position) {
        this.position = position;

        this.position.x += randomBound(-OFFSET, OFFSET);
        this.position.x += randomBound(-OFFSET, OFFSET);

        rotation = randomBound(0f, 360f);
        scale = randomBound(0.3f, 1f);
    }

    /**
     * Returns a random float value between two bounds.
     *
     * @param lowerBound  : Lower bound
     * @param higherBound : Higher bound
     * @return random value
     */
    private float randomBound(float lowerBound, float higherBound) {
        Random random = new Random();

        return (random.nextFloat() * (higherBound - lowerBound) + lowerBound);
    }

    /**
     * Getters and Setters
     */
    public Vector2 getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

}
