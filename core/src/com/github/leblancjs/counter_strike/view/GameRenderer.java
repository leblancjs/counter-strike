package com.github.leblancjs.counter_strike.view;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.github.leblancjs.counter_strike.model.Actor;
import com.github.leblancjs.counter_strike.model.Actor.ActorType;
import com.github.leblancjs.counter_strike.model.Actor.State;
import com.github.leblancjs.counter_strike.model.Blood;
import com.github.leblancjs.counter_strike.model.Bullet;
import com.github.leblancjs.counter_strike.model.Path;
import com.github.leblancjs.counter_strike.model.PathNode;
import com.github.leblancjs.counter_strike.model.Weapon.WeaponType;
import com.github.leblancjs.counter_strike.model.World;

public class GameRenderer {

    private static final float CAMERA_WIDTH = 18f;
    private static final float CAMERA_HEIGHT = 13.5f;

    private static final int BODY_CT_COUNT = 4;
    private static final int BODY_T_COUNT = 4;
    private static final int BODY_H_COUNT = 8;

    private static final int WALKING_FRAME_COUNT = 8;
    private static final float WALKING_FRAME_DURATION = (1f / WALKING_FRAME_COUNT);

    private static final int POINTER_SIZE = 24;

    private static final float BLAST_WIDTH = 2f;
    private static final float BLAST_HEIGHT = 1f;
    private static final float BLAST_OFFSET = 1f;

    private static final float FOG_DISTANCE = 7f;
    private static final float FOG_ALPHA = 0.9f;

    private static final float HEALTH_BAR_HEIGHT = 0.2f;
    private static final float HEALTH_BAR_OFFSET = 0.3f;
    private static final float HEALTH_BAR_ALPHA = 0.8f;

    private static final float WEAPON_OFFSET = 0.5f;

    private World world;
    private OrthographicCamera camera;

    private TextureRegion[] counterTerroristBodies;
    private TextureRegion[] terroristBodies;
    private TextureRegion[] hostageBodies;
    private TextureRegion[] legs;
    private TextureRegion legsFrame;
    private TextureRegion blast;
    private TextureRegion blood;
    private TextureRegion pointer;

    private Texture fog;
    private Texture healthBar;
    private Texture healthBarBack;

    private HashMap<WeaponType, TextureRegion> weapons;

    private Animation legsAnimation;

    private SpriteBatch batch;
    private ShapeRenderer debugRenderer;

    private TiledMap map;
    private TiledMapRenderer mapRenderer;

    private BitmapFont font;
    private GlyphLayout glyphLayout;

    public static float ppuX, ppuY;

    private String messageLost = "Game Over: You are dead.";
    private String messageWonTerroristsEliminated = "Victory: All terrorists have been eliminated.";
    private String messageWonHostagesRescued = "Victory: All hostages have been rescued.";

    private int width, height;

    /**
     * Constructor for the game renderer. It receives a world to render and the dimensions of the screen.
     *
     * @param world  : the world to render
     * @param width  : the width of the screen
     * @param height : the height of the screen
     */
    public GameRenderer(World world, int width, int height) {
        this.world = world;

        this.width = width;
        this.height = height;

        ppuX = (float) this.width / CAMERA_WIDTH;
        ppuY = (float) this.height / CAMERA_HEIGHT;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, CAMERA_WIDTH * ppuX, CAMERA_HEIGHT * ppuY);
        camera.position.set(0.5f * width, 0.5f * height, 0);
        camera.update();

        batch = new SpriteBatch();

        debugRenderer = new ShapeRenderer();

        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        loadTextures();
        initAnimations();
        initMap();
    }

    /**
     * Renders the game.
     */
    public void render() {
        updateCamera();

        drawMap();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawBlood();
        drawActors();
        drawGunBlasts();
        drawHealth();
        drawFog();
        drawPointer();
        drawMessage();

        batch.end();


        if (world.isDebug()) {
            drawDebug();
        }

        world.getCollisions().clear();
    }

    /**
     * Disposes of all the graphics.
     */
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }

        if (debugRenderer != null) {
            debugRenderer.dispose();
        }

        if (font != null) {
            font.dispose();
        }
    }

    /**
     * Updates the camera's position.
     */
    private void updateCamera() {
        Actor player = world.getPrimaryActor();

        float cameraX = (player.getPosition().x + Actor.SIZE / 2) * ppuX;
        float cameraY = (player.getPosition().y + Actor.SIZE / 2) * ppuY;

        camera.position.set(cameraX, cameraY, 0f);
        //scamera.zoom = 5f;
        camera.update();
    }

    /**
     * Resizes the screen and updates the pixel per unit values.
     *
     * @param width  : the new width of the screen
     * @param height : the new height of the screen
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        ppuX = (float) this.width / CAMERA_WIDTH;
        ppuY = (float) this.height / CAMERA_HEIGHT;

        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        mapRenderer.setView(camera);
    }

    /**
     * Loads the textures needed to render the game.
     */
    private void loadTextures() {
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("textures/game/game.pack"));

        // Counter Terrorists
        counterTerroristBodies = new TextureRegion[BODY_CT_COUNT];

        for (int i = 0; i < BODY_CT_COUNT; i++) {
            counterTerroristBodies[i] = atlas.findRegion("ct" + (i + 1));
        }

        // Terrorists
        terroristBodies = new TextureRegion[BODY_T_COUNT];

        for (int i = 0; i < BODY_T_COUNT; i++) {
            terroristBodies[i] = atlas.findRegion("t" + (i + 1));
        }

        // Hostages
        hostageBodies = new TextureRegion[BODY_H_COUNT];

        for (int i = 0; i < BODY_H_COUNT; i++) {
            hostageBodies[i] = atlas.findRegion("h" + (i + 1));
        }

        // Legs
        legs = new TextureRegion[WALKING_FRAME_COUNT];

        for (int i = 0; i < WALKING_FRAME_COUNT; i++) {
            legs[i] = atlas.findRegion("legs", i + 1);
        }

        // Weapons
        weapons = new HashMap<WeaponType, TextureRegion>();

        weapons.put(WeaponType.M4A1, atlas.findRegion("m4a1"));
        weapons.put(WeaponType.AK47, atlas.findRegion("ak47"));

        // Blast
        blast = atlas.findRegion("blast");

        // Blood
        blood = atlas.findRegion("blood");

        // Pointer
        pointer = atlas.findRegion("pointer");

        // Fog
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, FOG_ALPHA));
        pixmap.fill();

        fog = new Texture(pixmap);

        // Health Bar
        pixmap.setColor(new Color(0, 0.75f, 0, HEALTH_BAR_ALPHA));
        pixmap.fill();

        healthBar = new Texture(pixmap);

        pixmap.setColor(new Color(0.75f, 0, 0, HEALTH_BAR_ALPHA));
        pixmap.fill();

        healthBarBack = new Texture(pixmap);

        pixmap.dispose();
    }

    /**
     * Initializes the animations.
     */
    private void initAnimations() {
        // Legs
        legsAnimation = new Animation(WALKING_FRAME_DURATION, legs);
    }

    /**
     * Loads the map's tile atlas and initializes the map.
     */
    private void initMap() {
        map = world.getMap();
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        mapRenderer.setView(camera);
    }

    /**
     * Draws the map's tile layers.
     */
    private void drawMap() {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    /**
     * Draws the world's actors.
     */
    private void drawActors() {
        for (Actor actor : world.getActors()) {
            Vector2 position = actor.getPosition();
            float x = position.x * ppuX;
            float y = position.y * ppuY;
            float width = Actor.SIZE * ppuX;
            float height = Actor.SIZE * ppuY;
            float angle = 0f;

            if (actor.getPlayable()) {
                // Get the mouse cursor's position
                Vector3 pointerPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(pointerPos);

                pointerPos.x /= ppuX;
                pointerPos.y /= ppuY;

                // Get the direction the actor is facing
                float dx = pointerPos.x - (actor.getPosition().x + Actor.SIZE / 2);
                float dy = pointerPos.y - (actor.getPosition().y + Actor.SIZE / 2);

                angle = Math.abs((float) (Math.atan2(dy, dx) * (180 / Math.PI)));

                if (pointerPos.y < (actor.getPosition().y + Actor.SIZE / 2)) {
                    angle = 360 - angle;
                }

                //System.out.println("Pointer: (" + pointerPos.x + ", " + pointerPos.y + ") | Player: (" + position.x + ", " + position.y + ") | Angle: " + angle);

                actor.setRotation(angle);
            } else {
                angle = actor.getRotation();
            }

            // Legs
            if (actor.getState().equals(State.WALKING)) {
                legsFrame = (TextureRegion) legsAnimation.getKeyFrame(actor.getStateTime(), true);

                batch.draw(legsFrame, x, y, 0.5f * width, 0.5f * height, width, height, 1f, 1f, angle);
            }

            // Body
            TextureRegion body = null;

            switch (actor.getType()) {
                case COUNTER_TERRORIST:
                    body = counterTerroristBodies[actor.getBody()];
                    break;
                case TERRORIST:
                    body = terroristBodies[actor.getBody()];
                    break;
                case HOSTAGE:
                    body = hostageBodies[actor.getBody()];
                    break;
            }

            batch.draw(body, x, y, 0.5f * width, 0.5f * height, width, height, 1f, 1f, angle);

            // Weapon
            if (actor.getWeapon().getType() != WeaponType.NONE) {
                batch.draw(weapons.get(actor.getWeapon().getType()), x + (WEAPON_OFFSET * ppuX), y, 0.5f * width - (WEAPON_OFFSET * ppuX), 0.5f * height, width, height, 1f, 1f, angle);
            }
        }
    }

    /**
     * Draws the gun blasts.
     */
    private void drawGunBlasts() {
        for (Bullet bullet : world.getBullets()) {
            Actor shooter = bullet.getShooter();
            Vector2 position = shooter.getPosition();

            batch.draw(blast, (position.x + BLAST_OFFSET) * ppuX, position.y * ppuY, (0.5f * Actor.SIZE - BLAST_OFFSET) * ppuX, 0.5f * Actor.SIZE * ppuY, BLAST_WIDTH * ppuX, BLAST_HEIGHT * ppuY, 1f, 1f, shooter.getRotation());
        }
    }

    /**
     * Draws the blood stains on the map.
     */
    private void drawBlood() {
        for (Blood stain : world.getBlood()) {
            batch.draw(blood, (stain.getPosition().x + Blood.SIZE / 2) * ppuX, (stain.getPosition().y + Blood.SIZE / 2) * ppuY, Blood.SIZE / 2, Blood.SIZE / 2, Blood.SIZE * ppuX, Blood.SIZE * ppuY, stain.getScale(), stain.getScale(), stain.getRotation());
        }
    }

    /**
     * Draws the actors' health bars.
     */
    private void drawHealth() {
        for (Actor actor : world.getActors()) {
            float healthPc = actor.getHealth() / Actor.HEALTH_MAX;

            batch.draw(healthBar, actor.getPosition().x * ppuX, (actor.getPosition().y - HEALTH_BAR_OFFSET) * ppuY, Actor.SIZE * ppuX * healthPc, HEALTH_BAR_HEIGHT * ppuY);
            batch.draw(healthBarBack, (actor.getPosition().x + Actor.SIZE * healthPc) * ppuX, (actor.getPosition().y - HEALTH_BAR_OFFSET) * ppuY, (Actor.SIZE * (1f - healthPc)) * ppuX, HEALTH_BAR_HEIGHT * ppuY);
        }
    }

    /**
     * Draws the fog of war.
     */
    private void drawFog() {
        Vector2 position = world.getPrimaryActor().getPosition();

        int startX = (int) Math.floor(position.x - CAMERA_WIDTH / 2);
        int startY = (int) Math.floor(position.y - CAMERA_HEIGHT / 2);
        int endX = (int) Math.ceil(position.x + CAMERA_WIDTH / 2 + 1f);
        int endY = (int) Math.ceil(position.y + CAMERA_HEIGHT / 2 + 1f);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Vector2 tile = new Vector2(x, y);
                Vector2 delta = tile.cpy().sub(position);

                float distance = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y);

                if (distance >= FOG_DISTANCE) {
                    batch.draw(fog, x * ppuX, y * ppuY, World.WALL_SIZE * ppuX, World.WALL_SIZE * ppuY);
                }
            }
        }
    }

    /**
     * Draws the mouse pointer.
     */
    private void drawPointer() {
        Vector3 position = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(position);

        batch.draw(pointer, position.x - (POINTER_SIZE / 2), position.y - (POINTER_SIZE / 2));
    }

    /**
     * Draws messages concerning the game's state
     */
    private void drawMessage() {
        String message;

        // Determine what message to display
        if (world.getPlayer().getState() == State.DYING) {
            message = messageLost;
        } else if (world.getActorCount(ActorType.TERRORIST) < 1) {
            message = messageWonTerroristsEliminated;
        } else if (world.getActorCount(ActorType.HOSTAGE) == world.getRescueCount() && world.getRescueCount() != 0) {
            message = messageWonHostagesRescued;
        } else {
            message = null;
        }

        if (message == null) {
            return;
        }

        // Render the message
        batch.draw(fog, 0f, 0f, world.getMapWidth() * ppuX, world.getMapHeight() * ppuY);

        glyphLayout.setText(font, message);

        font.draw(batch, message, camera.position.x - glyphLayout.width / 2,
                camera.position.y - glyphLayout.height / 2);
    }

    /**
     * Draws the bullet bounding boxes and bullet trajectories.
     */
    private void drawDebug() {
        Rectangle rect;

        debugRenderer.setProjectionMatrix(camera.combined);

        // Rescue Zone
        debugRenderer.begin(ShapeType.Filled);

        rect = world.getRescueZone();

        debugRenderer.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        debugRenderer.rect(rect.x * ppuX, rect.y * ppuY, rect.width * ppuX, rect.height * ppuY);

        debugRenderer.end();

        // Paths
        debugRenderer.begin(ShapeType.Filled);

        for (Path path : world.getPaths()) {
            for (PathNode node : path.getNodes()) {
                rect = new Rectangle(node.getPosition().x, node.getPosition().y, World.WALL_SIZE, World.WALL_SIZE);

                debugRenderer.setColor(new Color(1, 1, 1, 1));
                debugRenderer.rect(rect.x * ppuX, rect.y * ppuY, rect.width * ppuX, rect.height * ppuY);
            }
        }

        debugRenderer.end();

        // Map
        debugRenderer.begin(ShapeType.Line);

        for (Rectangle wall : world.getBlocks()) {
            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(wall.x * ppuX, wall.y * ppuY, wall.width * ppuX, wall.height * ppuY);
        }

        for (Rectangle wall : world.getCollisions()) {
            debugRenderer.setColor(Color.WHITE);
            debugRenderer.rect(wall.x * ppuX, wall.y * ppuY, wall.width * ppuX, wall.height * ppuY);
        }

        debugRenderer.end();

        // Dead Actors
        debugRenderer.begin(ShapeType.Filled);

        for (Actor actor : world.getDeadActors()) {
            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(actor.getPosition().x * ppuX, actor.getPosition().y * ppuY, Actor.BOUNDS_SIZE * ppuX, Actor.BOUNDS_SIZE * ppuY);
        }

        debugRenderer.end();

        // Actors
        debugRenderer.begin(ShapeType.Filled);

        for (Actor actor : world.getActors()) {
            debugRenderer.setColor(Color.BLUE);
            debugRenderer.rect(actor.getPosition().x * ppuX, actor.getPosition().y * ppuY, Actor.BOUNDS_SIZE * ppuX, Actor.BOUNDS_SIZE * ppuY);
        }

        debugRenderer.end();

        // Bullets
        debugRenderer.begin(ShapeType.Line);

        for (Bullet bullet : world.getBullets()) {
            debugRenderer.setColor(Color.YELLOW);
            debugRenderer.line(bullet.getStart().x * ppuX, bullet.getStart().y * ppuX, bullet.getEnd().x * ppuX, bullet.getStart().y + bullet.getEnd().y * ppuY);
        }

        debugRenderer.end();

        // Target Tracers
        debugRenderer.begin(ShapeType.Line);

        for (Actor actor : world.getActors()) {
            if (actor.getHead() != null) {
                debugRenderer.setColor(Color.ORANGE);
                debugRenderer.line((actor.getPosition().x + Actor.SIZE / 2) * ppuX, (actor.getPosition().y + Actor.SIZE / 2) * ppuY, (actor.getHead().getPosition().x + Actor.SIZE / 2) * ppuX, (actor.getHead().getPosition().y + Actor.SIZE / 2) * ppuY);
            }
        }

        debugRenderer.end();
    }

}
