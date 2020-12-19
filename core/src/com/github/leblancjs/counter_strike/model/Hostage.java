package com.github.leblancjs.counter_strike.model;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponType;

public class Hostage extends Actor {

    private boolean rescued;

    /**
     * Constructor class for a hostage. This will create a new actor at the given position.
     *
     * @param position : the start position
     */
    public Hostage(Vector2 position) {
        super(position, false);

        rescued = false;

        // Type
        type = ActorType.HOSTAGE;

        // Body
        Random random = new Random();
        body = random.nextInt(BODY_H_COUNT);

        // Weapon
        weapon = new Weapon(WeaponType.NONE);
    }

    public boolean isRescued() {
        return rescued;
    }

    public void setRescued(boolean rescued) {
        this.rescued = rescued;
    }

}
