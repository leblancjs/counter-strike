package com.github.leblancjs.counter_strike.model;

import java.util.Random;

import com.badlogic.gdx.math.Vector2;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponType;

public class CounterTerrorist extends Actor {

    /**
     * Constructor class for a counter terrorist. This will create an actor at the given position.
     *
     * @param position : the start position
     * @param playable : whether the actor is controlled by a human player
     */
    public CounterTerrorist(Vector2 position, boolean playable) {
        super(position, playable);

        // Type
        type = ActorType.COUNTER_TERRORIST;

        // Body
        Random random = new Random();
        body = random.nextInt(BODY_CT_COUNT);

        // Weapon
        weapon = new Weapon(WeaponType.M4A1);
    }

}
