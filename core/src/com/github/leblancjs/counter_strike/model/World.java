package com.github.leblancjs.counter_strike.model;


import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.github.leblancjs.counter_strike.model.Actor.ActorType;

public class World {

    /**
     * Constants
     */
    public final static float WALL_SIZE = 1f;
    private final static float RESCUE_ZONE_SIZE = 5f;

    private final static int COUNTER_TERRORIST_COUNT = 1;
    private final static int TERRORIST_COUNT = 4;
    private final static int HOSTAGE_COUNT = 3;

    /**
     * Contents
     */
    private Actor player;

    private Array<Actor> actors;
    private Array<Actor> deadActors;
    private Array<Rectangle> collisions;
    private Array<Rectangle> blocks;
    private Array<Bullet> bullets;
    private Array<Blood> blood;

    /**
     * Map
     */
    private TiledMap map;

    /**
     * Spawn Points
     */
    private Array<Vector2> hostageSpawnPoints;

    private Rectangle ctSpawn;
    private Rectangle tSpawn;

    /**
     * Rescue
     */
    private Vector2 rescueZonePoint;
    private Rectangle rescueZone;
    private int rescueCount;

    /**
     * Paths
     */
    private Array<Path> paths;
    private Array<Array<Vector2>> terroristPaths;

    /**
     * Camping Spots
     */
    private Array<Vector2> campingSpots;
    private Array<Float> campingAngles;
    private HashMap<Vector2, Float> campingSpotAngles;

    private Random random = new Random();

    /**
     * Debugging
     */
    private boolean debug;

    /**
     * Constructor for the world. It will create the player and his counter-terrorist team, the terrorist team,
     * the hostages and load the map.
     */
    public World(boolean debug) {
        this.debug = debug;

        loadMap();

        paths = new Array<Path>();

        // Blocks
        blocks = getCollisionRectangles(0, 0, getMapWidth() - 1, getMapHeight() - 1);

        // Collisions
        collisions = new Array<Rectangle>();

        // Spawn Points
        hostageSpawnPoints = new Array<Vector2>();

        hostageSpawnPoints.add(new Vector2(28f, convertYForInitialization(39f)));
        hostageSpawnPoints.add(new Vector2(28f, convertYForInitialization(40f)));
        hostageSpawnPoints.add(new Vector2(28f, convertYForInitialization(41f)));

        ctSpawn = new Rectangle(39f, convertYForInitialization(43f), 5f, 5f);
        tSpawn = new Rectangle(7f, convertYForInitialization(39f), 5f, 5f);

        // Rescue Zone
        rescueZonePoint = new Vector2(39f, convertYForInitialization(43f));
        rescueZone = new Rectangle(rescueZonePoint.x, rescueZonePoint.y, RESCUE_ZONE_SIZE, RESCUE_ZONE_SIZE);

        // Paths
        // Terrorist
        terroristPaths = new Array<Array<Vector2>>();

        Array<Vector2> path = new Array<Vector2>();

        path.add(new Vector2(7f, convertYForInitialization(26f)));
        path.add(new Vector2(16f, convertYForInitialization(23f)));
        path.add(new Vector2(22f, convertYForInitialization(10f)));
        path.add(new Vector2(28f, convertYForInitialization(19f)));
        path.add(new Vector2(32f, convertYForInitialization(28f)));
        path.add(new Vector2(41f, convertYForInitialization(30f)));
        path.add(new Vector2(37f, convertYForInitialization(39f)));

        terroristPaths.add(path);

        path = new Array<Vector2>();

        path.add(new Vector2(7f, convertYForInitialization(26f)));
        path.add(new Vector2(10f, convertYForInitialization(14f)));
        path.add(new Vector2(22f, convertYForInitialization(10f)));
        path.add(new Vector2(28f, convertYForInitialization(19f)));
        path.add(new Vector2(41f, convertYForInitialization(22f)));
        path.add(new Vector2(43f, convertYForInitialization(43f)));

        terroristPaths.add(path);

        // Camping Spots
        // Terrorist
        campingSpots = new Array<Vector2>();

        campingSpots.add(new Vector2(14f, convertYForInitialization(32f)));
        campingSpots.add(new Vector2(26f, convertYForInitialization(41f)));
        campingSpots.add(new Vector2(15f, convertYForInitialization(26f)));
        campingSpots.add(new Vector2(18f, convertYForInitialization(19f)));
        campingSpots.add(new Vector2(6f, convertYForInitialization(16f)));
        campingSpots.add(new Vector2(43f, convertYForInitialization(43f)));
        campingSpots.add(new Vector2(38f, convertYForInitialization(43f)));
        campingSpots.add(new Vector2(37f, convertYForInitialization(34f)));

        campingAngles = new Array<Float>();

        campingAngles.add(90f);
        campingAngles.add(45f);
        campingAngles.add(0f);
        campingAngles.add(180f);
        campingAngles.add(0f);
        campingAngles.add(90f);
        campingAngles.add(0f);
        campingAngles.add(0f);

        campingSpotAngles = new HashMap<Vector2, Float>();

        for (int i = 0; i < campingSpots.size; i++) {
            campingSpotAngles.put(campingSpots.get(i), campingAngles.get(i));
        }

        // Actors
        actors = new Array<Actor>();
        deadActors = new Array<Actor>();

        spawn(ActorType.COUNTER_TERRORIST);

        if (actors.size > 0) {
            player = actors.get(0);
            player.setPlayable(true);
        }

        for (int i = 0; i < COUNTER_TERRORIST_COUNT; i++) {
            spawn(ActorType.COUNTER_TERRORIST);
        }

        for (int i = 0; i < TERRORIST_COUNT; i++) {
            spawn(ActorType.TERRORIST);
        }

        for (int i = 0; i < HOSTAGE_COUNT; i++) {
            actors.add(new Hostage(hostageSpawnPoints.get(i)));
        }

        // Bullets
        bullets = new Array<Bullet>();

        // Blood
        blood = new Array<Blood>();
    }

    private void loadMap() {
        map = new AtlasTmxMapLoader().load("dust/dust.tmx");
    }

    /**
     * Returns all the walls within the area delimited by the given bounds.
     *
     * @param startX : start X position of the area
     * @param startY : start Y position of the area
     * @param endX   : end X position of the area
     * @param endY   : end Y position of the area
     * @return all the walls within the specified area
     */
    public Array<Rectangle> getCollisionRectangles(int startX, int startY, int endX, int endY) {
        Array<Rectangle> walls = new Array<Rectangle>();

        TiledMapTileLayer layer = getWallLayer();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (x >= 0 && x < getMapWidth() && y >= 0 && y < getMapHeight() && layer.getCell(x, (int) convertY(y)) != null) {
                    walls.add(new Rectangle(x, y, WALL_SIZE, WALL_SIZE));
                }
            }
        }

        return walls;
    }

    public int getMapWidth() {
        return ((TiledMapTileLayer) map.getLayers().get(0)).getWidth();
    }

    public int getMapHeight() {
        return ((TiledMapTileLayer) map.getLayers().get(0)).getHeight();
    }

    /* Paths */

    /**
     * Adds the path to the list of active paths.
     *
     * @param path : the path to add
     */
    public void addPath(Path path) {
        paths.add(path);
    }

    /**
     * Removes the path from the list of active paths.
     *
     * @param path : the path to remove
     */
    public void removePath(Path path) {
        if (path != null) {
            paths.removeValue(path, false);
        }
    }

    public Array<Path> getPaths() {
        return paths;
    }

    /* Camping */

    /**
     * Returns the angle to face when at the given camping spot.
     *
     * @param spot : the camping spot
     * @return the angle to be facing
     */
    public float getTerroristCampingSpotAngle(Vector2 spot) {
        if (campingSpots.contains(spot, false)) {
            return campingSpotAngles.get(spot);
        } else {
            return 0f;
        }
    }

    /* Coordinates */

    /**
     * Converts the Y coordinate from the Tiled format to the LibGDX format.
     *
     * @param y : the Y coordinate to convert
     * @return the converted Y coordinate
     */
    public float convertY(float y) {
        return y; //(getMapHeight() - 1) - y;
    }

    /**
     * Converts the Y coordinate from the Tiled format to the LibGDX format.
     *
     * @param y : the Y coordinate to convert
     * @return the converted Y coordinate
     */
    public float convertYForInitialization(float y) {
        return (getMapHeight() - 1) - y;
    }

    /* Spawning */

    /**
     * Spawns an actor.
     *
     * @param type
     */
    private void spawn(ActorType type) {
        float lowerBoundX = 0f;
        float lowerBoundY = 0f;
        float upperBoundX = 0f;
        float upperBoundY = 0f;

        Actor actor = null;

        switch (type) {
            case TERRORIST:
                lowerBoundX = tSpawn.x;
                lowerBoundY = tSpawn.y;
                upperBoundX = tSpawn.x + tSpawn.width;
                upperBoundY = tSpawn.y + tSpawn.height;

                actor = new Terrorist(new Vector2(), false);

                break;

            case COUNTER_TERRORIST:
                lowerBoundX = ctSpawn.x;
                lowerBoundY = ctSpawn.y;
                upperBoundX = ctSpawn.x + ctSpawn.width;
                upperBoundY = ctSpawn.y + ctSpawn.height;

                actor = new CounterTerrorist(new Vector2(), false);

                break;

            default:
                break;
        }

        upperBoundX -= 1;
        upperBoundY -= 1;

        if (actor == null) {
            return;
        }

        // Spawn the actor
        Vector2 position = new Vector2(randomBound(lowerBoundX, upperBoundX), randomBound(lowerBoundY, upperBoundY));
        actor.setPosition(position);

        boolean free = false;

        while (!free) {
            free = true;

            for (Actor other : actors) {
                if (actor.getBounds().overlaps(other.getBounds())) {
                    free = false;
                }
            }

            if (!free) {
                position = new Vector2(randomBound(lowerBoundX, upperBoundX), randomBound(lowerBoundY, upperBoundY));
                actor.setPosition(position);
            }
        }

        actors.add(actor);
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
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Array<Actor> getActors() {
        return actors;
    }

    public Actor getPrimaryActor() {
        return actors.get(0);
    }

    public Actor getPlayer() {
        return player;
    }

    public TiledMap getMap() {
        return map;
    }

    public TiledMapTileLayer getWallLayer() {
        return (TiledMapTileLayer) map.getLayers().get("Walls");
    }

    public Array<Rectangle> getBlocks() {
        return blocks;
    }

    public Array<Rectangle> getCollisions() {
        return collisions;
    }

    public Rectangle getRescueZone() {
        return rescueZone;
    }

    public int getRescueCount() {
        return rescueCount;
    }

    public void setRescueCount(int rescueCount) {
        this.rescueCount = rescueCount;
    }

    public Array<Vector2> getTerroristRoute() {
        Array<Vector2> path = new Array<Vector2>();

        for (Vector2 position : terroristPaths.get(random.nextInt(terroristPaths.size))) {
            path.add(position);
        }

        return path;
    }

    public Vector2 getCampingSpot() {
        return campingSpots.get(random.nextInt(campingSpots.size));
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public Array<Actor> getDeadActors() {
        return deadActors;
    }

    public Array<Blood> getBlood() {
        return blood;
    }

    public int getActorCount(ActorType type) {
        int count = 0;

        for (Actor actor : actors) {
            if (actor.getType().equals(type)) {
                count++;
            }
        }

        return count;
    }

}
