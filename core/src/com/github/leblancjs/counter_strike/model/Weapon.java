package com.github.leblancjs.counter_strike.model;

import java.util.HashMap;

public class Weapon {

    public enum WeaponState {
        IDLE, FIRING, RELOADING
    }

    public enum WeaponType {
        NONE, M4A1, AK47
    }

    private WeaponState state = WeaponState.IDLE;
    private WeaponType type = WeaponType.NONE;

    private static HashMap<WeaponType, Float> damage = new HashMap<WeaponType, Float>();
    private static HashMap<WeaponType, Float> recoil = new HashMap<WeaponType, Float>();
    private static HashMap<WeaponType, Float> fireRate = new HashMap<WeaponType, Float>();

    static {
        // Damage
        damage.put(WeaponType.NONE, 0f);
        damage.put(WeaponType.M4A1, 10f);
        damage.put(WeaponType.AK47, 10f);

        // Recoil
        recoil.put(WeaponType.NONE, 0f);
        recoil.put(WeaponType.M4A1, 5f);
        recoil.put(WeaponType.AK47, 10f);

        // Fire Rate
        fireRate.put(WeaponType.NONE, 0f);
        fireRate.put(WeaponType.M4A1, 0.1f);
        fireRate.put(WeaponType.AK47, 0.2f);
    }

    /**
     * Constructor class for a weapon.
     */
    public Weapon(WeaponType type) {
        this.type = type;
    }

    /**
     * Getters and Setters
     */
    public WeaponState getState() {
        return state;
    }

    public void setState(WeaponState state) {
        this.state = state;
    }

    public WeaponType getType() {
        return type;
    }

    public float getDamage() {
        return damage.get(type);
    }

    public float getRecoil() {
        return recoil.get(type);
    }

    public float getFireRate() {
        return fireRate.get(type);
    }

}
