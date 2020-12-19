package com.github.leblancjs.counter_strike.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.leblancjs.counter_strike.GameStarter;
import com.github.leblancjs.counter_strike.model.CounterStrike;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new CounterStrike(), config);
    }
}
