package com.github.leblancjs.counter_strike.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.github.leblancjs.counter_strike.model.Actor;
import com.github.leblancjs.counter_strike.model.Actor.ActorType;
import com.github.leblancjs.counter_strike.model.Actor.Job;
import com.github.leblancjs.counter_strike.model.Actor.State;
import com.github.leblancjs.counter_strike.model.Bullet;
import com.github.leblancjs.counter_strike.model.Hostage;
import com.github.leblancjs.counter_strike.model.Path;
import com.github.leblancjs.counter_strike.model.PathFinder;
import com.github.leblancjs.counter_strike.model.PathNode;
import com.github.leblancjs.counter_strike.model.Weapon;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponState;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponType;
import com.github.leblancjs.counter_strike.model.World;

public class ActorController {

    /**
     * Constants
     */
    public static final float VELOCITY_MAX = 4f;
    public static final float VELOCITY_AI = 3f;
    public static final float ACCELERATION = 20f;
    public static final float DAMP = 0.9f;

    private static final float CAMPING_TIME = 20f;

    private static final float LINEOFSIGHT_RANGE = 10f;
    private static final float LINEOFSIGHT_ANGLE = 90f;

    private static final float RANGE = 7f;
    private static final float ANGLE_THRESHOLD = 5f;

    /**
     * Keys
     */
    private enum Keys {
        LEFT, RIGHT, UP, DOWN, FIRE, RELOAD, USE
    }

    private static Map<Keys, Boolean> keys = new HashMap<Keys, Boolean>();

    static {
        keys.put(Keys.LEFT, false);
        keys.put(Keys.RIGHT, false);
        keys.put(Keys.UP, false);
        keys.put(Keys.DOWN, false);
        keys.put(Keys.FIRE, false);
        keys.put(Keys.RELOAD, false);
        keys.put(Keys.USE, false);
    }

    /**
     * World
     */
    private World world;

    /**
     * Actor
     */
    private Actor actor;
    private boolean playable;

    private float fireTimer;
    private float campingTimer = 0f;

    /**
     * Path Finder
     */
    private static PathFinder pathFinder;

    private Array<Vector2> route = null;
    private Vector2 campingSpot = null;
    private Vector2 lastPosition;
    private Vector2 nextPosition;

    /**
     * Sounds
     */
    private static ArrayList<Sound> hostageSounds = new ArrayList<Sound>();

    static {
        hostageSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/hostage/hos1.wav")));
        hostageSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/hostage/hos2.wav")));
        hostageSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/hostage/hos3.wav")));
        hostageSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/hostage/hos4.wav")));
        hostageSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/hostage/hos5.wav")));
    }

    private static ArrayList<Sound> dyingSounds = new ArrayList<Sound>();

    static {
        dyingSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/die1.wav")));
        dyingSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/die2.wav")));
        dyingSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/actors/die3.wav")));
    }

    private static HashMap<WeaponType, Sound> fireSounds = new HashMap<WeaponType, Sound>();

    static {
        fireSounds.put(WeaponType.M4A1, Gdx.audio.newSound(Gdx.files.internal("sounds/weapons/m4a1_silenced.wav")));
        fireSounds.put(WeaponType.AK47, Gdx.audio.newSound(Gdx.files.internal("sounds/weapons/ak47.wav")));
    }

    Random random = new Random();

    /**
     * Constructor for an actor controller. This initializes a controller for an actor to control
     * its behavior.
     *
     * @param actor : actor to control
     */
    public ActorController(World world, Actor actor) {
        this.world = world;
        this.actor = actor;
        this.playable = this.actor.getPlayable();

        if (pathFinder == null) {
            pathFinder = new PathFinder(this.world);
        }

        lastPosition = new Vector2();
    }

    /**
     * Updates the actor's state
     *
     * @param delta : time elapsed since the last update
     */
    public void update(float delta) {
        if (!getAlive()) {
            return;
        }

        if (playable) {
            updatePlayable(delta);
        } else {
            updateAI(delta);
        }

        updateWeapon(delta);

        actor.update(delta);
    }

    /**
     * Frees the resources used by the controller.
     */
    public void dispose() {
        for (Sound sound : hostageSounds) {
            sound.dispose();
        }

        for (Sound sound : dyingSounds) {
            sound.dispose();
        }

        fireSounds.get(WeaponType.M4A1).dispose();
        fireSounds.get(WeaponType.AK47).dispose();
    }

    /**
     * Checks if the actor is still alive.
     *
     * @return TRUE if the actor is alive, FALSE if it is dead
     */
    private boolean getAlive() {
        if (actor.getHealth() <= 0) {
            if (actor.getState() != State.DYING) {
                die();
            }

            return false;
        } else {
            return true;
        }
    }

    /**
     * Updates the player's state.
     *
     * @param delta : the time elapsed since the last update
     */
    private void updatePlayable(float delta) {
        processInput();

        // Movement
        Vector2 acceleration = actor.getAcceleration();
        Vector2 velocity = actor.getVelocity();

        acceleration.scl(delta);

        velocity.add(acceleration);

        updateVelocity(delta, actor.getVelocity());
    }

    /**
     * Update the actor's state according to it's AI.
     *
     * @param delta : the time elapsed since the last update
     */
    private void updateAI(float delta) {
        switch (actor.getType()) {
            case HOSTAGE:
                aiHostage();
                break;

            default:
                aiGeneric(delta);
                break;
        }

        // Movement
        Vector2 velocity = actor.getVelocity();

        updateVelocity(delta, velocity);

        if (velocity.x + velocity.y == 0) {
            actor.setState(State.IDLE);
        }

        // Rotation
        updateRotation();
    }

    /**
     * Updates the actor's velocity and checks for collisions.
     *
     * @param delta    : the time that has elapsed since the last update
     * @param velocity : the actor's velocity
     */
    private void updateVelocity(float delta, Vector2 velocity) {
        checkCollisions(delta);

        // Deceleration
        velocity.x *= DAMP;
        velocity.y *= DAMP;

        // Limit the velocity
        if (velocity.x > VELOCITY_MAX) {
            velocity.x = VELOCITY_MAX;
        }

        if (velocity.y > VELOCITY_MAX) {
            velocity.y = VELOCITY_MAX;
        }

        if (velocity.x < -VELOCITY_MAX) {
            velocity.x = -VELOCITY_MAX;
        }

        if (velocity.y < -VELOCITY_MAX) {
            velocity.y = -VELOCITY_MAX;
        }
    }

    /**
     * Updates the actor's rotation.
     */
    private void updateRotation() {
        float delta = actor.getRotation() - actor.getNextRotation();
        float direction;
        float rotation;

        if (actor.getRotation() < actor.getNextRotation()) {
            direction = 1f;
        } else {
            direction = -1f;
        }

        if (Math.abs(delta) < Actor.ROTATION_SPEED) {
            rotation = actor.getNextRotation();
        } else {
            rotation = actor.getRotation() + Actor.ROTATION_SPEED * direction;

        }

        actor.setRotation(rotation);
    }

    /**
     * Updates the weapon. If the actor is firing, the weapon will fire.
     *
     * @param delta : time elapsed since the last update
     */
    private void updateWeapon(float delta) {
        Weapon weapon = actor.getWeapon();

        if (weapon.getState() == WeaponState.FIRING) {
            if (fireTimer > weapon.getFireRate()) {
                fire();
            }
        }

        fireTimer += delta;
    }

    /**
     * Checks for collisions with walls, other actors and bullets. If the actor is
     * not controlled by a player, the collisions with walls is not done since it is taken care of in
     * the path finding algorithm.
     *
     * @param delta : the time elapsed since the last update
     */
    private void checkCollisions(float delta) {
        Vector2 velocity = actor.getVelocity();
        velocity.scl(delta);

        Rectangle bounds = new Rectangle(actor.getBounds().x, actor.getBounds().y, actor.getBounds().width, actor.getBounds().height);

        Array<Rectangle> walls;

        int startX;
        int endX;
        int startY;
        int endY;

        // Check for collisions on the X axis
        startY = (int) bounds.y;
        endY = (int) (bounds.y + bounds.height);

        if (velocity.x < 0) {
            startX = endX = (int) Math.floor(bounds.x + velocity.x);
        } else {
            startX = endX = (int) Math.floor(bounds.x + bounds.width + velocity.x);
        }

        walls = world.getCollisionRectangles(startX, startY, endX, endY);

        bounds.x += velocity.x;

        for (Rectangle wall : walls) {
            if (bounds.overlaps(wall)) {
                velocity.x = 0;

                world.getCollisions().add(wall);
            }
        }

        for (Actor actor : world.getActors()) {
            if (bounds.overlaps(actor.getBounds()) && actor != this.actor) {
                velocity.x = 0;

                world.getCollisions().add(actor.getBounds());
            }
        }

        // Check for collisions on the Y axis
        bounds.x = actor.getPosition().x;

        startX = (int) bounds.x;
        endX = (int) (bounds.x + bounds.width);

        if (velocity.y < 0) {
            startY = endY = (int) Math.floor(bounds.y + velocity.y);
        } else {
            startY = endY = (int) Math.floor(bounds.y + bounds.width + velocity.y);
        }

        walls = world.getCollisionRectangles(startX, startY, endX, endY);

        bounds.y += velocity.y;

        for (Rectangle wall : walls) {
            if (bounds.overlaps(wall)) {
                velocity.y = 0;

                world.getCollisions().add(wall);
            }
        }

        for (Actor actor : world.getActors()) {
            if (bounds.overlaps(actor.getBounds()) && actor != this.actor) {
                velocity.y = 0;

                world.getCollisions().add(actor.getBounds());
            }
        }

        bounds.y = actor.getPosition().y;

        // Update the position
        actor.getPosition().add(velocity);

        actor.getBounds().x = actor.getPosition().x;
        actor.getBounds().y = actor.getPosition().y;

        velocity.scl(1 / delta);
    }

    /**
     * Makes the actor fire a bullet.
     */
    private void fire() {
        Vector2 position = actor.getPosition().cpy();
        position.x += Actor.SIZE / 2;
        position.y += Actor.SIZE / 2;

        world.getBullets().add(new Bullet(actor));

        fireTimer = 0f;

        fireSounds.get(actor.getWeapon().getType()).play();
    }

    /**
     * Makes the actor die.
     */
    private void die() {
        actor.setState(State.DYING);

        removeFromQueue();

        actor.setHead(null);

        world.getDeadActors().add(this.actor);
        world.getActors().removeValue(this.actor, true);
        world.getPaths().removeValue(actor.getPath(), false);

        dyingSounds.get(random.nextInt(dyingSounds.size())).play();
    }

    /* General */

    /**
     * Gets a path to reach the actor's target.
     */
    private void aiGetPath() {
        // Check if the target has moved
        Vector2 target = actor.getTarget();

        if ((int) target.x != (int) lastPosition.x || (int) target.y != (int) lastPosition.y) {
            lastPosition = target.cpy();

            // Find a path
            world.removePath(actor.getPath());

            Path path = pathFinder.getPath(new Vector2((int) actor.getPosition().x, (int) actor.getPosition().y),
                    new Vector2((int) target.x, (int) target.y));

            actor.setPath(path);

            world.addPath(path);
        }
    }

    /**
     * Makes the actor follow a path.
     */
    private void aiFollowPath() {
        aiGetPath();

        Path path = actor.getPath();

        if (path != null) {
            // Move towards the next position
            PathNode node = path.getNextNode();

            if (node != null) {
                nextPosition = node.getPosition();

                if (nextPosition != null) {
                    Vector2 delta = nextPosition.cpy().sub(actor.getPosition());

                    float distance = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);

                    if (distance < 1f) {
                        path.removeNode();
                    } else {
                        aiTurnTowardsTarget(delta);
                        aiMoveTowardsTarget();
                    }
                }
            } else {
                world.removePath(path);

                actor.setPath(null);

                actor.setState(State.IDLE);
            }
        }
    }

    /**
     * Makes the actor follow a route to "wander" the map. A route is composed of key points (Vector2)
     * which are used along with a path finder to navigate.
     */
    private void aiFollowRoute() {
        Path path = actor.getPath();

        if (path == null) {
            if (route.size > 0) {
                actor.setTarget(route.first());
            }
        }

        aiFollowPath();

        if (path == null) {
            if (route.size > 0) {
                route.removeIndex(0);
            } else {
                route = null;
            }
        }
    }

    /**
     * Makes the actor turn towards its target.
     *
     * @param delta : the delta x and y between the current position and the target
     */
    private void aiTurnTowardsTarget(Vector2 delta) {
        float angle = Math.abs((float) (Math.atan2(delta.y, delta.x) * (180 / Math.PI)));

        if (actor.getTarget().y <= (actor.getPosition().y + Actor.SIZE / 2)) {
            angle = 360 - angle;
        }

        actor.setNextRotation(angle);
    }

    /**
     * Makes the actor move towards its target.
     */
    private void aiMoveTowardsTarget() {
        if (nextPosition.x > actor.getPosition().x) {
            actor.setState(Actor.State.WALKING);
            actor.getVelocity().x = VELOCITY_AI;
        } else if (nextPosition.x < actor.getPosition().x) {
            actor.setState(Actor.State.WALKING);
            actor.getVelocity().x = -VELOCITY_AI;
        }

        if (nextPosition.y > actor.getPosition().y) {
            actor.setState(Actor.State.WALKING);
            actor.getVelocity().y = VELOCITY_AI;
        } else if (nextPosition.y < actor.getPosition().y) {
            actor.setState(Actor.State.WALKING);
            actor.getVelocity().y = -VELOCITY_AI;
        }
    }

    /**
     * Makes the actor attack.
     */
    private void aiAttack() {
        // Check if the enemy is still alive
        if (actor.getHead().getState() == State.DYING) {
            actor.setHead(null);

            return;
        }

        // Attack
        Vector2 position = actor.getPosition();
        Vector2 enemyPos = actor.getHead().getPosition();

        actor.setTarget(actor.getHead().getPosition().cpy());

        if (getDistance(position, enemyPos) < RANGE) {
            aiTurnTowardsTarget(enemyPos.cpy().sub(position));

            float dAngle = Math.abs(actor.getNextRotation() - actor.getRotation());

            if (dAngle < ANGLE_THRESHOLD) {
                actor.getWeapon().setState(WeaponState.FIRING);
            }
        } else {
            actor.getWeapon().setState(WeaponState.IDLE);

            // Move towards it
            aiFollowPath();
        }
    }

    /**
     * Makes the actor camp.
     *
     * @param delta : the time elapsed since the last update
     */
    private void aiCamp(float delta) {
        Vector2 target = actor.getTarget();

        // Find a camping spot
        if (campingSpot == null) {
            actor.setTarget(world.getCampingSpot());
            campingSpot = target;
        }

        aiFollowPath();

        // Camp
        if (actor.getPath() == null) {
            actor.setNextRotation(world.getTerroristCampingSpotAngle(target));

            campingTimer += delta;
            if (campingTimer > CAMPING_TIME) {
                campingTimer = 0f;
                campingSpot = null;
            }
        }
    }

    /**
     * Makes the actor explore the map by following a route.
     */
    private void aiExplore() {
        // Find a route
        if (route == null) {
            switch (actor.getType()) {
                case TERRORIST:
                    route = world.getTerroristRoute();
                    break;

                case COUNTER_TERRORIST:
                    route = world.getTerroristRoute();
                    break;

                default:
                    break;
            }
        }

        // Follow a route
        aiFollowRoute();

        if (route == null) {
            actor.setJob(Actor.Job.NONE);
        }
    }

    /**
     * Makes the actor investigate where the shot came from.
     */
    private void aiInvestigate() {
        aiFollowPath();
    }

    /**
     * Makes the actor look for enemies that are in range. If an enemy is found within range and in sight,
     * it becomes the target.
     */
    private void checkEnemies() {
        // Filter the actors to only get enemies
        Array<Actor> enemies = new Array<Actor>();

        for (Actor enemy : world.getActors()) {
            if (enemy.equals(actor) || enemy.getType() == actor.getType() || enemy.getType() == ActorType.HOSTAGE) {
                continue;
            }

            enemies.add(enemy);
        }

        // Filter the enemies to keep only those who are within range and in front of him
        for (Actor enemy : enemies) {
            Vector2 delta = actor.getPosition().cpy().sub(enemy.getPosition());

            float distance = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);

            if (distance > LINEOFSIGHT_RANGE || !isInLineOfSight(enemy.getPosition())) {
                enemies.removeValue(enemy, false);
            }
        }

        if (enemies.size < 1) {
            actor.setHead(null);
        } else {
            // Check if any of the enemies can be seen
            for (Actor enemy : enemies) {
                if (isVisible(enemy)) {
                    actor.setHead(enemy);
                } else {
                    actor.setHead(null);
                }
            }
        }
    }

    /**
     * Checks whether an enemy is visible to the actor.
     *
     * @param enemy : the enemy
     * @return whether it is visible
     */
    private boolean isVisible(Actor enemy) {
        Vector2 position = actor.getPosition().cpy();
        Vector2 delta = enemy.getPosition().cpy().sub(position);

        Ray los = new Ray(new Vector3(position.x + Actor.SIZE / 2, position.y + Actor.SIZE / 2, 0f), new Vector3(delta.x, delta.y, 0f));

        // Check if a wall is in the way
        Array<Vector2> walls = getWallsInSight(position, enemy.getPosition(), los);

        if (walls.size > 0) {
            Vector2 wall = getFirstWall(walls, position);

            if (getClosest(position, wall, enemy.getPosition()).equals(enemy)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * Returns a list of all the walls in the line of sight
     *
     * @param start : the actor's position
     * @param end   : the enemy's position
     * @param line  : the line of sight
     * @return a list of walls in the line of sight
     */
    private Array<Vector2> getWallsInSight(Vector2 start, Vector2 end, Ray line) {
        Array<Vector2> walls = new Array<Vector2>();

        int startX, endX;
        int startY, endY;

        if (start.x < end.x) {
            startX = (int) start.x;
            endX = (int) end.x;
        } else {
            startX = (int) end.x;
            endX = (int) start.x;
        }

        if (start.y < end.y) {
            startY = (int) start.y;
            endY = (int) end.y;
        } else {
            startY = (int) end.y;
            endY = (int) start.y;
        }

        for (Rectangle wall : world.getCollisionRectangles(startX, startY, endX, endY)) {
            BoundingBox box = new BoundingBox(new Vector3(wall.x, wall.y, 0), new Vector3(wall.x + World.WALL_SIZE, wall.y + World.WALL_SIZE, 0));

            if (Intersector.intersectRayBoundsFast(line, box)) {
                walls.add(new Vector2(wall.x, wall.y));
            }
        }

        return walls;
    }

    /**
     * Returns which wall is seen first
     *
     * @param walls    : the walls detected
     * @param position : the position of the actor
     * @return which wall was seen first
     */
    private Vector2 getFirstWall(Array<Vector2> walls, Vector2 position) {
        Vector2 firstWall = null;

        for (Vector2 wall : walls) {
            if (firstWall == null) {
                firstWall = wall;
            } else {
                firstWall = getClosest(position, firstWall, wall);
            }
        }

        return firstWall;
    }

    /**
     * Returns which of the two given points is closest to the origin.
     *
     * @param origin : the starting point
     * @param p1     : the first point
     * @param p2     : the second point
     * @return the closest point
     */
    private Vector2 getClosest(Vector2 origin, Vector2 p1, Vector2 p2) {
        float d1 = getDistance(origin, p1);
        float d2 = getDistance(origin, p2);

        if (d1 < d2) {
            return p1;
        } else {
            return p2;
        }
    }

    /**
     * Returns the distance between the two given points.
     *
     * @param p1 : the first point
     * @param p2 : the second point
     * @return the distance between the points
     */
    private float getDistance(Vector2 p1, Vector2 p2) {
        Vector2 delta = p1.cpy().sub(p2);

        return (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);
    }

    /**
     * Returns whether the point is in the actor's line of sight
     *
     * @param point : the point to check
     * @return TRUE if the point is in sight, FALSE otherwise
     */
    private boolean isInLineOfSight(Vector2 point) {
        // Calculate the actor's field of vision
        float lowerBound = actor.getRotation() - LINEOFSIGHT_ANGLE;

        if (lowerBound < 0) {
            lowerBound = 360f + lowerBound;
        }

        float upperBound = actor.getRotation() + LINEOFSIGHT_ANGLE;

        if (upperBound > 360f) {
            upperBound = upperBound - 360f;
        }

        // Calculate the angle to the point
        Vector2 delta = point.cpy().sub(actor.getPosition());

        float angle = Math.abs((float) (Math.atan2(delta.y, delta.x) * (180 / Math.PI)));

        if (point.y < (actor.getPosition().y + Actor.SIZE / 2)) {
            angle = 360 - angle;
        }

        if (lowerBound > upperBound) {
            if (angle <= upperBound || angle >= lowerBound) {
                return true;
            } else {
                return false;
            }
        } else {
            if (angle >= lowerBound && angle <= upperBound) {
                return true;
            } else {
                return false;
            }
        }
    }

    /* Generic AI */

    /**
     * Makes the actor follow a generic AI pattern.
     *
     * @param delta : the time elapsed since the last update
     */
    private void aiGeneric(float delta) {
        checkEnemies();

        if (actor.getHead() != null) {
            if (actor.getJob() == Job.INVESTIGATE && !isVisible(actor.getHead())) {
                aiInvestigate();
            } else {
                aiAttack();
            }
        } else {
            actor.getWeapon().setState(WeaponState.IDLE);

            if (actor.getJob() == Actor.Job.NONE) {
                Actor.Job job = Actor.Job.NONE;

                while (job == Actor.Job.INVESTIGATE || job == Actor.Job.NONE) {
                    job = Actor.Job.values()[random.nextInt(Actor.Job.values().length)];
                }

                actor.setJob(job);
            } else {
                switch (actor.getJob()) {
                    case NONE:
                        break;

                    case INVESTIGATE:
                        aiInvestigate();
                        break;

                    case CAMP:
                        aiCamp(delta);
                        break;

                    case EXPLORE:
                        aiExplore();
                        break;
                }
            }
        }
    }

    /* Hostage */

    /**
     * Makes the actor behave like a hostage. If the hostage has not yet been rescued, it will follow its leader
     * (head) until it reaches the rescue zone.
     */
    private void aiHostage() {
        Hostage actor = (Hostage) this.actor;

        // Check if the hostage has been rescued
        if (actor.isRescued()) {
            return;
        }

        // Check if the hostage has reached the rescue zone
        if (actor.getBounds().overlaps(world.getRescueZone())) {
            // Make the hostage rescued
            actor.setRescued(true);
            world.setRescueCount(world.getRescueCount() + 1);

            removeFromQueue();
        } else {
            // Follow the leader :)
            Actor head = actor.getHead();

            if (head != null) {
                actor.setTarget(head.getPosition());

                aiFollowPath();
            }
        }
    }

    /**
     * Rescues a hostage if he is in contact with one
     */
    private void rescueHostage() {
        Actor hostage = null;

        // Check whether or not he is close enough to a hostage
        for (Actor actor : world.getActors()) {
            if (actor.getType() == ActorType.HOSTAGE) {
                if (actor.getHead() == null) {
                    Vector2 delta = this.actor.getPosition().cpy().sub(actor.getPosition().cpy());

                    float distance = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);

                    if (distance < 1f) {
                        hostage = actor;
                    }
                }
            }
        }

        if (hostage != null) {
            addToQueue(hostage);
            hostageSounds.get(random.nextInt(hostageSounds.size())).play();
        }
    }

    /**
     * Adds a hostage to the queue.
     *
     * @param hostage : the hostage to add
     */
    private void addToQueue(Actor hostage) {
        Actor tail = actor.getTail();

        if (tail == null) {
            actor.setTail(hostage);
            hostage.setHead(actor);
        } else {
            while (tail.getTail() != null) {
                tail = tail.getTail();
            }

            tail.setTail(hostage);
            hostage.setHead(tail);
        }
    }

    /**
     * Remove a hostage from the queue.
     */
    private void removeFromQueue() {
        if (actor.getTail() != null) {
            actor.getTail().setHead(actor.getHead());
        }

        if (actor.getHead() != null) {
            actor.getHead().setTail(actor.getTail());
        }
    }

    /**
     * Input Processing Methods
     */
    private void processInput() {
        // Left/Right
        if (keys.get(Keys.LEFT)) {
            if (!actor.getState().equals(State.DYING)) {
                actor.setState(Actor.State.WALKING);
                actor.getAcceleration().x = -ACCELERATION;
            }
        } else if (keys.get(Keys.RIGHT)) {
            if (!actor.getState().equals(State.DYING)) {
                actor.setState(Actor.State.WALKING);
                actor.getAcceleration().x = ACCELERATION;
            }
        } else {
            actor.setState(State.IDLE);
            actor.getAcceleration().x = 0;
        }

        // Up/Down
        if (keys.get(Keys.UP)) {
            if (!actor.getState().equals(State.DYING)) {
                actor.setState(Actor.State.WALKING);
                actor.getAcceleration().y = ACCELERATION;
            }
        } else if (keys.get(Keys.DOWN)) {
            if (!actor.getState().equals(State.DYING)) {
                actor.setState(State.WALKING);
                actor.getAcceleration().y = -ACCELERATION;
            }
        } else if (!keys.get(Keys.LEFT) && !keys.get(Keys.RIGHT)) {
            actor.setState(State.IDLE);
            actor.getAcceleration().y = 0;
        }

        // Fire
        if (keys.get(Keys.FIRE)) {
            if (!actor.getWeapon().getState().equals(WeaponState.RELOADING)) {
                actor.getWeapon().setState(WeaponState.FIRING);
            }
        } else {
            if (!actor.getWeapon().getState().equals(WeaponState.RELOADING)) {
                actor.getWeapon().setState(WeaponState.IDLE);
            }
        }

        // Reload
        if (keys.get(Keys.RELOAD)) {
            actor.getWeapon().setState(WeaponState.RELOADING);
        }

        // Use
        if (keys.get(Keys.USE)) {
            //rescueHostage();
        }
    }

    /* Pressed */
    public void leftPressed() {
        keys.get(keys.put(Keys.LEFT, true));
    }

    public void rightPressed() {
        keys.get(keys.put(Keys.RIGHT, true));
    }

    public void upPressed() {
        keys.get(keys.put(Keys.UP, true));
    }

    public void downPressed() {
        keys.get(keys.put(Keys.DOWN, true));
    }

    public void firePressed() {
        keys.get(keys.put(Keys.FIRE, true));
    }

    public void reloadPressed() {
        keys.get(keys.put(Keys.RELOAD, true));
    }

    public void usePressed() {
        keys.get(keys.put(Keys.USE, true));

        rescueHostage();
    }

    /* Released */
    public void leftReleased() {
        keys.get(keys.put(Keys.LEFT, false));
    }

    public void rightReleased() {
        keys.get(keys.put(Keys.RIGHT, false));
    }

    public void upReleased() {
        keys.get(keys.put(Keys.UP, false));
    }

    public void downReleased() {
        keys.get(keys.put(Keys.DOWN, false));
    }

    public void fireReleased() {
        keys.get(keys.put(Keys.FIRE, false));
    }

    public void reloadReleased() {
        keys.get(keys.put(Keys.RELOAD, false));
    }

    public void useReleased() {
        keys.get(keys.put(Keys.USE, false));
    }

}
