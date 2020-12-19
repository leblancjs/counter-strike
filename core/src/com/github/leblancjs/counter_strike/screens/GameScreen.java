package com.github.leblancjs.counter_strike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.github.leblancjs.counter_strike.controller.GameController;
import com.github.leblancjs.counter_strike.controller.GameController.State;
import com.github.leblancjs.counter_strike.model.CounterStrike;
import com.github.leblancjs.counter_strike.model.World;
import com.github.leblancjs.counter_strike.view.GameRenderer;

public class GameScreen implements Screen, InputProcessor {

    private static final float RESET_TIME = 10f;

    private CounterStrike game;

    private World world;
    private GameRenderer renderer;
    private GameController controller;

    private float resetTimer = 0f;

    private int width, height;

    private boolean debug = false;

    /**
     * Constructor class for the game screen. It has a reference to the game class to
     * be able to switch between screens.
     *
     * @param game   : the game instance
     * @param width  : the width of the screen
     * @param height : the height of the screen
     */
    public GameScreen(CounterStrike game, int width, int height) {
        this.game = game;

        // Screen size
        this.width = width;
        this.height = height;
    }

    /**
     * Screen Method Implementations
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (controller.getState() != State.PLAYING) {
            if (resetTimer > RESET_TIME) {
                resetTimer = 0f;

                game.setScreen(game.menuScreen);
            }

            resetTimer += delta;
        }

        controller.update(delta);
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        renderer.setSize(this.width, this.height);
    }

    @Override
    public void show() {
        world = new World(debug);
        controller = new GameController(world);
        renderer = new GameRenderer(world, width, height);

        Gdx.input.setInputProcessor(this);
        //Gdx.input.setCursorCatched(true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        if (controller != null) {
            controller.dispose();
        }

        if (renderer != null) {
            renderer.dispose();
        }

        Gdx.input.setInputProcessor(null);
    }

    /**
     * InputProcessor Method Implementations
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.A) {
            controller.getPlayerController().leftPressed();
        }

        if (keycode == Keys.D) {
            controller.getPlayerController().rightPressed();
        }

        if (keycode == Keys.W) {
            controller.getPlayerController().upPressed();
        }

        if (keycode == Keys.S) {
            controller.getPlayerController().downPressed();
        }

        if (keycode == Keys.R) {
            controller.getPlayerController().reloadPressed();
        }

        if (keycode == Keys.E) {
            controller.getPlayerController().usePressed();
        }

        if (keycode == Keys.P) {
            debug = !debug;

            world.setDebug(debug);
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.ESCAPE) {
            game.setScreen(game.menuScreen);
        }

        if (keycode == Keys.A) {
            controller.getPlayerController().leftReleased();
        }

        if (keycode == Keys.D) {
            controller.getPlayerController().rightReleased();
        }

        if (keycode == Keys.W) {
            controller.getPlayerController().upReleased();
        }

        if (keycode == Keys.S) {
            controller.getPlayerController().downReleased();
        }

        if (keycode == Keys.R) {
            controller.getPlayerController().reloadReleased();
        }

        if (keycode == Keys.E) {
            controller.getPlayerController().useReleased();
        }

        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        controller.getPlayerController().firePressed();

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        controller.getPlayerController().fireReleased();

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // TODO Auto-generated method stub
        return false;
    }
}
