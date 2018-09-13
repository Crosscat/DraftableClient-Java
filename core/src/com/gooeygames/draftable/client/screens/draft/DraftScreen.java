package com.gooeygames.draftable.client.screens.draft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.cards.CardWebTextureLoader;
import com.gooeygames.draftable.client.cards.Collection;
import com.gooeygames.draftable.client.utils.ServerProxy;

import java.util.List;

public class DraftScreen implements Screen {

    protected Draftable game;
    protected String draftId;
    protected String serverUrl;
    protected Stage stage;

    protected Table uiTable;
    protected Collection collection;


    public DraftScreen(Draftable game, String draftId) {
        this.game = game;
        this.draftId = draftId;
        this.collection = new Collection();
    }

    @Override
    public void show() {
        serverUrl = game.serverUrl;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        preloadCube(draftId);

        uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
    }

    protected void preloadCube(String draftId) {
        try {
            List<Card> cube = ServerProxy.getCube(serverUrl, draftId);
            for (Card card : cube){
                CardWebTextureLoader.getTexture(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    protected void initialize(){

    }

    @Override
    public void resize(int width, int height) {
        this.initialize();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
