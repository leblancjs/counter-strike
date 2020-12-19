package com.github.leblancjs.counter_strike.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PathFinder {

    private final static float COST = 10f;

    private World world;

    /**
     * Constructor for a path finder instance.
     *
     * @param world : the world
     */
    public PathFinder(World world) {
        this.world = world;
    }

    /**
     * Returns the best path to reach the end point.
     *
     * @param start : the start point
     * @param end   : the end point
     * @return the best path to the end point
     */
    public Path getPath(Vector2 start, Vector2 end) {
        float[][] heuristics = getHeuristics(end);

        Array<PathNode> closed = new Array<PathNode>();
        Array<PathNode> open = new Array<PathNode>();

        // Create the start node
        open.add(createNode(null, start.cpy(), heuristics));

        float heuristic = 1;

        while (open.size > 0 && heuristic > 0) {
            PathNode parent = getNextNode(open);

            // Add parent to the closed list
            closed.add(parent);
            open.removeValue(parent, false);

            // Check if the objective is in the closed list
            heuristic = parent.getH();

            if (heuristic != 0) {
                // Get adjacent nodes
                Array<PathNode> neighbours = getNeighbours(parent, heuristics);

                // Put them in the right list
                for (PathNode node : neighbours) {
                    if (findNode(closed, node) < 0) {
                        if (findNode(open, node) < 0) {
                            open.add(node);
                        } else {
                            PathNode old = open.get(findNode(open, node));

                            if (old.getG() > node.getG()) {
                                old.setG(node.getG());
                            }
                        }
                    }
                }
            }
        }

        return new Path(tracePath(closed));
    }

    /**
     * Returns the best path to take to reach the end point.
     *
     * @param list : the list of possible nodes to use
     * @return the best path to reach the end point
     */
    private Array<PathNode> tracePath(Array<PathNode> list) {
        Array<PathNode> path = new Array<PathNode>();

        PathNode node = list.get(list.size - 1);

        while (node.getParent() != null) {
            path.add(node);
            node = node.getParent();
        }

        return path;
    }

    /**
     * Creates a new node at the given position with a cost relative to the parent node (if not null).
     *
     * @param parent     : the parent node (leave null if this is the first node of the path)
     * @param position   : the position of the node
     * @param heuristics : an array of heuristic values
     * @return a new node
     */
    private PathNode createNode(PathNode parent, Vector2 position, float[][] heuristics) {
        float cost = COST;

        if (parent != null) {
            cost += parent.getG();
        }

        return new PathNode(position, parent, cost, heuristics[(int) position.y][(int) position.x]);
    }

    /**
     * Returns the node with the lowest F score (the best choice).
     *
     * @param open : the list of open nodes
     * @return
     */
    private PathNode getNextNode(Array<PathNode> open) {
        PathNode next = null;

        for (PathNode node : open) {
            if (next == null) {
                next = node;
            } else {
                if (next.getF() > node.getF()) {
                    next = node;
                }
            }
        }

        return next;
    }

    /**
     * Returns all the nodes neighboring the current position.
     *
     * @param parent     : the parent node
     * @param heuristics : array of heuristic values
     * @return an array containing the neighboring nodes
     */
    private Array<PathNode> getNeighbours(PathNode parent, float[][] heuristics) {
        Array<PathNode> neighbours = new Array<PathNode>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x == y) || (x != 0 && y != 0)) {
                    continue;
                }

                Vector2 position = parent.getPosition().cpy();

                if (position.x + x >= 0 && position.x + x < (world.getMapWidth() - 1) &&
                        position.y + y >= 0 && position.y + y < (world.getMapHeight() - 1)) {
                    position.x += x;
                    position.y += y;

                    if (world.getWallLayer().getCell((int) position.x, (int) world.convertY(position.y)) == null) {
                        neighbours.add(createNode(parent, position, heuristics));
                    }
                }
            }
        }

        return neighbours;
    }

    /**
     * Checks if a given node is in the given list.
     *
     * @param list : the list to check
     * @param node : the node to find in the list
     * @return TRUE if the node is in the list, FALSE otherwise
     */
    private int findNode(Array<PathNode> list, PathNode node) {
        int index = 0;

        for (PathNode currentNode : list) {
            if (currentNode.getPosition().equals(node.getPosition())) {
                return index;
            }

            index++;
        }

        return -1;
    }

    /**
     * Calculates the heuristics for each node on the map according to the end point.
     *
     * @param end : the end point
     * @return the heuristics for each node on the map
     */
    private float[][] getHeuristics(Vector2 end) {
        float[][] heuristics = new float[world.getMapHeight()][world.getMapWidth()];

        for (int x = 0; x < world.getMapWidth(); x++) {
            for (int y = 0; y < world.getMapHeight(); y++) {
                heuristics[y][x] = Math.abs(end.x - x) + Math.abs(end.y - y);
            }
        }

        return heuristics;
    }

}
