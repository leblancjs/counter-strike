package com.github.leblancjs.counter_strike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.github.leblancjs.counter_strike.model.CounterStrike;

public class GameStarter extends ApplicationAdapter {
//    private static final float CAMERA_WIDTH = 18f;
//    private static final float CAMERA_HEIGHT = 13.5f;

    private CounterStrike counterStrike;

//    private TiledMap map;
//    private TiledMapRenderer mapRenderer;
//    private OrthographicCamera camera;
//    private BitmapFont font;
//    private SpriteBatch batch;

    @Override
    public void create() {
        counterStrike = new CounterStrike();

//        camera = new OrthographicCamera();
//        camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
//        camera.update();
//
//        font = new BitmapFont();
//        batch = new SpriteBatch();
//
//        map = new AtlasTmxMapLoader().load("dust/dust.tmx");
//        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);
    }

    @Override
    public void render() {
        counterStrike.render();

//        Gdx.gl.glClearColor(1, 0, 0, 1);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//
//        camera.update();
//
//        mapRenderer.setView(camera);
//        mapRenderer.render();
//
//        batch.begin();
//        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
//        batch.end();
    }

    @Override
    public void dispose() {
        if (counterStrike != null)
            counterStrike.dispose();

//        batch.dispose();
//        map.dispose();
    }
}
