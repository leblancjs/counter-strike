package com.github.leblancjs.counter_strike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.*;
import com.github.leblancjs.counter_strike.model.CounterStrike;

public class MenuScreen implements Screen {

    /**
     * These are the various buttons states. To add a new one, simply add it above
     * the BUTTON_STATE_LAST, and update the index.
     */
    public static enum buttonStates {
        BUTTON_STATE_UP(0),
        BUTTON_STATE_OVER(1),
        BUTTON_STATE_DOWN(2),
        BUTTON_STATE_LAST(3);

        private final int value;

        private buttonStates(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * These are the positions of the buttons in the menu.
     */
    private static enum buttonPositions {
        BUTTON_X(0.1f),

        BUTTON_PLAY_Y(0.5f),
        BUTTON_CONTROLS_Y(0.4f),
        BUTTON_CREDITS_Y(0.3f),
        BUTTON_EXIT_Y(0.2f);

        private final float value;

        private buttonPositions(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    private CounterStrike game;

    private SpriteBatch batch;
    private Stage stage;

    private TextureRegion texBackground;
    private TextureRegion texTitle;
    private TextureRegion[] texButton;

    private Image imageBackground;
    private Image imageTitle;

    private TextButton buttonPlay;
    private TextButton buttonInstructions;
    private TextButton buttonCredits;
    private TextButton buttonExit;

    private int width, height;

    /**
     * Constructor class for the menu screen. It has a reference to the game class to
     * be able to switch between screens.
     *
     * @param game   : the game instance
     * @param width  : the width of the screen
     * @param height : the height of the screen
     */
    public MenuScreen(CounterStrike game, int width, int height) {
        this.game = game;

        // Screen size
        this.width = width;
        this.height = height;
    }

    /**
     * Loads the textures needed to render the stage
     */
    private void loadTextures() {
        TextureAtlas atlas = new TextureAtlas("textures/menu/menu.pack");

        // Background
        texBackground = atlas.findRegion("background");

        // Title
        texTitle = atlas.findRegion("title");

        // Button
        texButton = new TextureRegion[buttonStates.BUTTON_STATE_LAST.getValue()];

        texButton[buttonStates.BUTTON_STATE_UP.getValue()] = atlas.findRegion("buttonUp");
        texButton[buttonStates.BUTTON_STATE_OVER.getValue()] = atlas.findRegion("buttonOver");
        texButton[buttonStates.BUTTON_STATE_DOWN.getValue()] = atlas.findRegion("buttonDown");
    }

    /**
     * Initializes the stage and creates all of its components
     */
    private void initStage() {
        // Load the textures
        loadTextures();

        // Initialize the button font
        BitmapFont buttonFont = new BitmapFont();

        // Initialize the button skin
        Skin buttonSkin = new Skin();

        buttonSkin.add("font", buttonFont);

        buttonSkin.add("buttonUp", texButton[buttonStates.BUTTON_STATE_UP.getValue()], TextureRegion.class);
        buttonSkin.add("buttonOver", texButton[buttonStates.BUTTON_STATE_OVER.getValue()], TextureRegion.class);
        buttonSkin.add("buttonDown", texButton[buttonStates.BUTTON_STATE_DOWN.getValue()], TextureRegion.class);

        // Initialize the button style
        TextButtonStyle buttonStyle = new TextButtonStyle();

        buttonStyle.font = buttonSkin.getFont("font");
        buttonStyle.fontColor = Color.WHITE;

        buttonStyle.up = new TextureRegionDrawable(buttonSkin.getRegion("buttonUp"));
        buttonStyle.over = new TextureRegionDrawable(buttonSkin.getRegion("buttonOver"));
        buttonStyle.down = new TextureRegionDrawable(buttonSkin.getRegion("buttonDown"));

        // Create the image actors
        imageBackground = new Image(texBackground);
        imageTitle = new Image(texTitle);

        imageBackground.setFillParent(true);

        // Create the button actors
        buttonPlay = new TextButton("Play", buttonStyle);
        buttonInstructions = new TextButton("Instructions", buttonStyle);
        buttonCredits = new TextButton("Credits", buttonStyle);
        buttonExit = new TextButton("Exit", buttonStyle);

        // Create the button event listeners
        buttonPlay.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                buttonPlayClicked();
            }
        });

        buttonInstructions.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                buttonInstructionsClicked();
            }
        });

        buttonCredits.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                buttonCreditsClicked();
            }
        });

        buttonExit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                buttonExitClicked();
            }
        });

        // Position the actors
        resizeStage();

        // Add the actors to the stage
        stage.addActor(imageBackground);

        stage.addActor(imageTitle);

        stage.addActor(buttonPlay);
        stage.addActor(buttonInstructions);
        stage.addActor(buttonCredits);
        stage.addActor(buttonExit);
    }

    /**
     * Resizes the view-port of the stage and re-positions the actors
     */
    private void resizeStage() {
        Viewport viewport = new StretchViewport(width, height);
        stage.setViewport(viewport);

        // Reposition the actors
        imageTitle.setPosition((0.5f * width) - (0.5f * imageTitle.getWidth()), 0.7f * height);

        buttonPlay.setPosition(buttonPositions.BUTTON_X.getValue() * width, buttonPositions.BUTTON_PLAY_Y.getValue() * height);
        buttonInstructions.setPosition(buttonPositions.BUTTON_X.getValue() * width, buttonPositions.BUTTON_CONTROLS_Y.getValue() * height);
        buttonCredits.setPosition(buttonPositions.BUTTON_X.getValue() * width, buttonPositions.BUTTON_CREDITS_Y.getValue() * height);
        buttonExit.setPosition(buttonPositions.BUTTON_X.getValue() * width, buttonPositions.BUTTON_EXIT_Y.getValue() * height);
    }

    /**
     * Callback function for the buttonPlay click event
     */
    private void buttonPlayClicked() {
        game.setScreen(game.gameScreen);
    }

    /**
     * Callback function for the buttonControls click event
     */
    private void buttonInstructionsClicked() {
        game.setScreen(game.instructionsScreen);
    }

    /**
     * Callback function for the buttonCredits click event
     */
    private void buttonCreditsClicked() {
        game.setScreen(game.creditsScreen);
    }

    /**
     * Callback function for the buttonExit click event
     */
    private void buttonExitClicked() {
        Gdx.app.exit();
    }

    /**
     * Screen Method Implementations
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        stage.act(delta);
        stage.draw();

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        resizeStage();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage();

        Gdx.input.setInputProcessor(stage);

        initStage();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
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
        if (stage != null) {
            stage.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }
    }

}
