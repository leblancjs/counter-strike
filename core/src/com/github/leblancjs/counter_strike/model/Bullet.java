package com.github.leblancjs.counter_strike.model;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Bullet {

    public static final float RANGE = 12f;

    private Actor shooter;

    private Vector2 start;
    private Vector2 end;

    private Ray trajectory;

    /**
     * Constructor for a bullet instance.
     *
     * @param shooter : the actor who shot the bullet
     */
    public Bullet(Actor shooter) {
        this.shooter = shooter;

        // Calculate the bullet's end point
        float direction = getAngle() * (float) (Math.PI / 180f);

        float dx = (float) Math.cos(direction) * RANGE;
        float dy = (float) Math.sin(direction) * RANGE;

        start = this.shooter.getPosition().cpy().add(new Vector2(Actor.SIZE / 2, Actor.SIZE / 2));
        end = new Vector2(start.x + dx, start.y + dy);

        // Calculate the bullet's trajectory
        trajectory = new Ray(new Vector3(start.x, start.y, 0f), new Vector3(dx, dy, 0f));
    }

    /**
     * Returns the angle of the shot with recoil added
     */
    private float getAngle() {
        float angle = shooter.getRotation();
        float recoil = shooter.getWeapon().getRecoil();
        float offset = randomBound(-recoil, recoil);

        if (angle + offset > 360f) {
            angle = (angle + offset) - 360f;
        } else if (angle + offset < 0f) {
            angle = 360f - (angle + offset);
        } else {
            angle += offset;
        }

        return angle;
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
    public Actor getShooter() {
        return shooter;
    }

    public Vector2 getStart() {
        return start;
    }

    public void setStart(Vector2 start) {
        this.start = start;
    }

    public Vector2 getEnd() {
        return end;
    }

    public void setEnd(Vector2 end) {
        this.end = end;
    }

    public Ray getTrajectory() {
        return trajectory;
    }

}
