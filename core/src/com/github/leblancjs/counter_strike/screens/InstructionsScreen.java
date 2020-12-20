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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.leblancjs.counter_strike.model.CounterStrike;
import com.github.leblancjs.counter_strike.screens.MenuScreen.buttonStates;

public class InstructionsScreen implements Screen {

    private static final float PADDING = 0.05f;

    private static final float BUTTON_BACK_POSITION = 0.1f;
    private static final float LABEL_INSTRUCTIONS_POSITION = 0.3f;

    private CounterStrike game;

    private SpriteBatch batch;
    private Stage stage;

    private TextureRegion texBackground;
    private TextureRegion[] texButton;

    private Image imageBackground;

    private Label labelInstructions;

    private TextButton buttonBack;

    private String instructionsText = "The game is very straightforward. You are a Counter Terrorist who has been deployed to rescue " +
            "hostages that were captured by the evil terrorist forces. Find them and bring them back to the " +
            "beginning, or kill all the terrorists to win.\n\n" +
            "CONTROLS\n" +
            "- Use the keys WASD to move around and the mouse to aim\n" +
            "- Click with the mouse to fire\n" +
            "- Press E to rescue a hostage\n\n" +
            "If you are debugging the game, or you want to cheat and know where the terrorists are going and if " +
            "they can see you, press P to toggle the debugging mode.\n\n" +
            "Thanks for playing!";


    private int width, height;

    /**
     * Constructor class for the instructions screen. It has a reference to the game class to
     * be able to switch between screens.
     *
     * @param game   : the game instance
     * @param width  : the width of the screen
     * @param height : the height of the screen
     */
    public InstructionsScreen(CounterStrike game, int width, int height) {
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

        // Initialize the label style
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = new BitmapFont();
        labelStyle.fontColor = Color.WHITE;

        // Create the image actors
        imageBackground = new Image(texBackground);

        imageBackground.setFillParent(true);

        // Create the label actors
        labelInstructions = new Label(instructionsText, labelStyle);
        labelInstructions.setWrap(true);

        // Create the button actors
        buttonBack = new TextButton("Back", buttonStyle);

        // Create the button event listeners
        buttonBack.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                buttonBackClicked();
            }
        });

        // Position the actors
        resizeStage();

        // Add the actors to the stage
        stage.addActor(imageBackground);

        stage.addActor(labelInstructions);

        stage.addActor(buttonBack);
    }

    /**
     * Resizes the view-port of the stage and re-positions the actors
     */
    private void resizeStage() {
        stage.getViewport().update(width, height);

        // Resize the label
        labelInstructions.setWidth(width - PADDING * width);
        labelInstructions.setHeight(height - (BUTTON_BACK_POSITION * height) - PADDING * height);

        // Reposition the actors
        labelInstructions.setPosition((0.5f * width) - (0.5f * labelInstructions.getWidth()), LABEL_INSTRUCTIONS_POSITION * height);
        buttonBack.setPosition((0.5f * width) - (0.5f * buttonBack.getWidth()), BUTTON_BACK_POSITION * height);
    }

    /**
     * Callback function for the buttonBack click event
     */
    public void buttonBackClicked() {
        game.setScreen(game.menuScreen);
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
