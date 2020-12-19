package com.github.leblancjs.counter_strike.model;

import com.badlogic.gdx.math.Vector2;

public class PathNode {

    private Vector2 position;

    private float g;
    private float h;
    private float f;

    private PathNode parent;

    /**
     * Constructor class for a path node. It receives its position on the map along
     * with the cost to get to it and the heuristic cost.
     *
     * @param position
     * @param g
     * @param h
     */
    public PathNode(Vector2 position, PathNode parent, float g, float h) {
        this.position = position;
        this.parent = parent;
        this.g = g;
        this.h = h;
        this.f = g + h;
    }

    /**
     * Getters and Setters
     */
    public Vector2 getPosition() {
        return position;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getH() {
        return h;
    }

    public float getF() {
        return f;
    }

    public PathNode getParent() {
        return parent;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

}
