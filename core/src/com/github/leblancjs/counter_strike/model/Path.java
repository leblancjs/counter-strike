package com.github.leblancjs.counter_strike.model;

import com.badlogic.gdx.utils.Array;

public class Path {

    private Array<PathNode> nodes;

    /**
     * Constructor class for a path.
     *
     * @param nodes : the nodes the path is composed of
     */
    public Path(Array<PathNode> nodes) {
        this.nodes = nodes;
    }

    public void append(PathNode node) {
        nodes.add(node);
    }

    /**
     * Getters and Setters
     */
    public Array<PathNode> getNodes() {
        return nodes;
    }

    public PathNode getNode(int index) {
        if (nodes.size > 0 && index >= 0 && index < nodes.size) {
            return nodes.get(index);
        } else {
            System.out.println("Failed to fetch node.");
            return null;
        }
    }

    public PathNode getNextNode() {
        if (nodes.size > 0) {
            return nodes.get(nodes.size - 1);
        } else {
            return null;
        }
    }

    public void removeNode() {
        nodes.removeIndex(nodes.size - 1);
    }

}
