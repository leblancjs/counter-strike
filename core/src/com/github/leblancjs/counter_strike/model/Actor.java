package com.github.leblancjs.counter_strike.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Actor {

    public enum State {
        IDLE, WALKING, DYING
    }

    public enum Job {
        NONE, CAMP, EXPLORE, INVESTIGATE
    }

    public enum ActorType {
        COUNTER_TERRORIST, TERRORIST, HOSTAGE
    }

    /**
     * Constants
     */
    public final static float SIZE = 1f;
    public final static float BOUNDS_SIZE = 0.875f;

    public final static float ROTATION_SPEED = 10f;

    public final static float HEALTH_MAX = 100;

    protected final static int BODY_CT_COUNT = 4;
    protected final static int BODY_T_COUNT = 4;
    protected final static int BODY_H_COUNT = 8;

    /**
     * Local Variables
     */
    protected Actor head;
    protected Actor tail;

    protected State state = State.IDLE;

    protected Job job = Job.NONE;

    protected Vector2 target;
    protected Path path;

    protected float stateTime = 0;

    protected Vector2 position = new Vector2();
    protected Vector2 acceleration = new Vector2();
    protected Vector2 velocity = new Vector2();

    protected Rectangle bounds = new Rectangle();

    protected ActorType type;

    protected float health = HEALTH_MAX;

    protected int body = 0;

    protected float rotation;
    protected float nextRotation;

    protected Weapon weapon;

    protected boolean playable;

    /**
     * Constructor class for the actor. This will create an actor at the given position and initialize
     * its parameters.
     *
     * @param position : the start position
     */
    public Actor(Vector2 position, boolean playable) {
        this.position = position;
        this.playable = playable;

        head = null;
        tail = null;

        rotation = 0f;
        nextRotation = 0f;

        // Set the bounds
        bounds.width = BOUNDS_SIZE;
        bounds.height = BOUNDS_SIZE;
        bounds.x = this.position.x;
        bounds.y = this.position.y;
    }

    /**
     * This function will update the actor's stateTime and position.
     *
     * @param delta : the time elapsed since the last update
     */
    public void update(float delta) {
        stateTime += delta;
    }

    /**
     * Getters and Setters
     */
    public State getState() {
        return state;
    }

    public Actor getHead() {
        return head;
    }

    public void setHead(Actor head) {
        this.head = head;
    }

    public Actor getTail() {
        return tail;
    }

    public void setTail(Actor tail) {
        this.tail = tail;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;

        bounds.x = this.position.x;
        bounds.y = this.position.y;
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector2 acceleration) {
        this.acceleration = acceleration;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public ActorType getType() {
        return type;
    }

    public int getBody() {
        return body;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public boolean getPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getNextRotation() {
        return this.nextRotation;
    }

    public void setNextRotation(float nextRotation) {
        this.nextRotation = nextRotation;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void setTarget(Vector2 target) {
        this.target = target;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

}
