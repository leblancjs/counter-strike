package com.github.leblancjs.counter_strike.controller;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.leblancjs.counter_strike.model.Actor;
import com.github.leblancjs.counter_strike.model.Actor.ActorType;
import com.github.leblancjs.counter_strike.model.Actor.Job;
import com.github.leblancjs.counter_strike.model.Blood;
import com.github.leblancjs.counter_strike.model.Bullet;
import com.github.leblancjs.counter_strike.model.World;

public class GameController {

    /**
     * Game State
     */
    public enum State {
        PLAYING, LOST, WON
    }

    private State state;

    /**
     * Controllers
     */
    private Array<ActorController> actorControllers;

    /**
     * World
     */
    private World world;

    /**
     * Constructor for the game controller.
     *
     * @param world
     */
    public GameController(World world) {
        this.world = world;

        state = State.PLAYING;

        actorControllers = new Array<ActorController>();

        for (Actor actor : world.getActors()) {
            actorControllers.add(new ActorController(this.world, actor));
        }
    }

    /**
     * Updates the game and the actors.
     *
     * @param delta : the time elapsed since the last update
     */
    public void update(float delta) {
        if (state != State.PLAYING) {
            return;
        }

        // Check the game state
        if (world.getActorCount(ActorType.TERRORIST) < 1 || world.getRescueCount() == world.getActorCount(ActorType.HOSTAGE)) {
            state = State.WON;
        } else if (world.getActorCount(ActorType.COUNTER_TERRORIST) < 1 || world.getPlayer().getState().equals(Actor.State.DYING)) {
            state = State.LOST;
        }
        // Check bullet collisions
        checkBulletCollisions();

        // Update the actors
        for (ActorController controller : actorControllers) {
            controller.update(delta);
        }
    }

    /**
     * Frees the resources used by the controllers.
     */
    public void dispose() {
        actorControllers.get(0).dispose();
    }

    /**
     * Checks the collisions of bullets with walls and actors.
     */
    private void checkBulletCollisions() {
        for (Bullet bullet : world.getBullets()) {
            Vector2 start = bullet.getStart();
            Vector2 end = bullet.getEnd();

            // Check collisions with walls
            Array<Vector2> collisions = getWallCollisions(start, end, bullet);
            Vector2 wallHit = getFirstCollision(collisions, start);

            // Check collisions with actors
            Array<Actor> victims = getActorCollisions(bullet);
            Actor victim = getFirstVictim(victims, start);

            // Check which collision came first
            if (victim != null) {
                Vector2 hit;

                if (wallHit != null) {
                    hit = getClosest(start, wallHit, victim.getPosition());
                } else {
                    hit = victim.getPosition();
                }

                if (getDistance(start, hit) < Bullet.RANGE) {
                    if (hit.equals(victim.getPosition())) {
                        world.getCollisions().add(new Rectangle(victim.getBounds()));

                        // Set target
                        if (victim.getType() != bullet.getShooter().getType() && victim.getType() != ActorType.HOSTAGE) {
                            if (victim.getHead() == null) {
                                if (victim.getJob() != Job.INVESTIGATE) {
                                    victim.setPath(null);
                                }

                                victim.setJob(Job.INVESTIGATE);
                                victim.setHead(bullet.getShooter());
                                victim.setTarget(victim.getHead().getPosition());
                            }
                        }

                        // Hit
                        if (victim.getType() != bullet.getShooter().getType()) {
                            victim.setHealth(victim.getHealth() - bullet.getShooter().getWeapon().getDamage());
                        }

                        world.getBlood().add(new Blood(victim.getPosition().cpy()));
                    } else {
                        world.getCollisions().add(new Rectangle(hit.x, hit.y, World.WALL_SIZE, World.WALL_SIZE));
                    }
                }
            } else {
                if (wallHit != null) {
                    if (getDistance(start, wallHit) < Bullet.RANGE) {
                        world.getCollisions().add(new Rectangle(wallHit.x, wallHit.y, World.WALL_SIZE, World.WALL_SIZE));
                    }
                }
            }
        }

        world.getBullets().clear();
    }

    /**
     * Returns a list of all the walls that the bullet potentially hit.
     *
     * @param start  : the bullet's start point
     * @param end    : the bullet's end point
     * @param bullet : the bullet
     * @return a list of walls potentially hit
     */
    private Array<Vector2> getWallCollisions(Vector2 start, Vector2 end, Bullet bullet) {
        Array<Vector2> collisions = new Array<Vector2>();

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

            if (Intersector.intersectRayBoundsFast(bullet.getTrajectory(), box)) {
                collisions.add(new Vector2(wall.x, wall.y));
            }
        }

        return collisions;
    }

    /**
     * Returns a list of all the actors that the bullet potentially hit.
     *
     * @param bullet : the bullet
     * @return a list of actors potentially hit
     */
    private Array<Actor> getActorCollisions(Bullet bullet) {
        Array<Actor> actors = new Array<Actor>();

        for (Actor actor : world.getActors()) {
            // Make sure it isn't the shooter
            if (actor != bullet.getShooter()) {
                Rectangle bounds = actor.getBounds();
                BoundingBox box = new BoundingBox(new Vector3(bounds.x, bounds.y, 0), new Vector3(bounds.x + bounds.width, bounds.y + bounds.height, 0));

                if (Intersector.intersectRayBoundsFast(bullet.getTrajectory(), box)) {
                    actors.add(actor);
                }
            }
        }

        return actors;
    }

    /**
     * Returns which hit came first from a list of detected collisions.
     *
     * @param collisions : the collisions detected
     * @param origin     : the origin of the bullet
     * @return the collision that came first
     */
    private Vector2 getFirstCollision(Array<Vector2> collisions, Vector2 origin) {
        Vector2 hit = null;

        for (Vector2 collision : collisions) {
            if (hit == null) {
                hit = collision;
            } else {
                hit = getClosest(origin, hit, collision);
            }
        }

        return hit;
    }

    /**
     * Returns which victim was hit first from a list of victims.
     *
     * @param victims : the list of victims
     * @param origin  : the origin of the bullet
     * @return the victim that was hit first
     */
    private Actor getFirstVictim(Array<Actor> victims, Vector2 origin) {
        Actor victimHit = null;

        for (Actor victim : victims) {
            if (victimHit == null) {
                victimHit = victim;
            } else {
                Vector2 hit = getClosest(origin, victimHit.getPosition(), victim.getPosition());

                if (hit.x == victim.getPosition().x && hit.y == victim.getPosition().y) {
                    victimHit = victim;
                }
            }
        }

        return victimHit;
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
     * Getters and Setters
     */
    public ActorController getPlayerController() {
        return actorControllers.get(0);
    }

    public State getState() {
        return state;
    }

}
