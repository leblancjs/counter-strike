package com.github.leblancjs.counter_strike.model;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponType;

public class Terrorist extends Actor {

    /**
     * Constructor class for a terrorist. This will create a new actor at the given position.
     *
     * @param position : the start position
     * @param playable : whether the actor is controlled by a human player
     */
    public Terrorist(Vector2 position, boolean playable) {
        super(position, playable);

        // Type
        type = ActorType.TERRORIST;

        // Body
        Random random = new Random();
        body = random.nextInt(BODY_T_COUNT);

        // Weapon
        weapon = new Weapon(WeaponType.AK47);
    }

}
