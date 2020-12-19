package com.github.leblancjs.counter_strike.model;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.github.leblancjs.counter_strike.screens.CreditsScreen;
import com.github.leblancjs.counter_strike.screens.GameScreen;
import com.github.leblancjs.counter_strike.screens.InstructionsScreen;
import com.github.leblancjs.counter_strike.screens.MenuScreen;

public class CounterStrike extends Game {

    public MenuScreen menuScreen;
    public InstructionsScreen instructionsScreen;
    public CreditsScreen creditsScreen;
    public GameScreen gameScreen;

    /**
     * Constructor class for a game instance. It initializes the screens.
     */
    @Override
    public void create() {
        // Get the initial dimensions of the screen
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Create the screens
        menuScreen = new MenuScreen(this, width, height);
        instructionsScreen = new InstructionsScreen(this, width, height);
        creditsScreen = new CreditsScreen(this, width, height);
        gameScreen = new GameScreen(this, width, height);

        setScreen(menuScreen);
    }

    /**
     * Disposes of the screens.
     */
    public void dispose() {
        if (menuScreen != null) {
            menuScreen.dispose();
        }

        if (gameScreen != null) {
            gameScreen.dispose();
        }
    }
}
