package com.gooeygames.draftable.client.screens.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.jsonobjects.Drafter;
import com.gooeygames.draftable.client.utils.CubeLoader;
import com.gooeygames.draftable.client.utils.ServerProxy;

public class SettingsScreen implements Screen {

    private Draftable game;
    private Stage stage;
    private Table uiTable;

    private TextField serverUrl;
    private TextField cubePath;
    private TextField cardsPerPlayer;
    private TextField draftType;
    private TextField draftId;

    private TextButton createDraft;
    private TextButton joinDraft;
    private TextButton startDraft;
    private TextButton rejoinDraft;

    public SettingsScreen(Draftable game){
        this.game = game;
    }

    @Override
    public void show() {
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        loadUI();
        loadPreferences();
        populateFields();
    }

    private void loadUI(){
        uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);
        //uiTable.setDebug(true);
        uiTable.defaults().width(200);

        Label titleLabel = new Label("Draftable", game.defaultSkin);
        titleLabel.setAlignment(Align.center);
        uiTable.add(titleLabel).colspan(2).padBottom(30);

        uiTable.row();
        uiTable.add(new Label("Server Url", game.defaultSkin)).left();
        serverUrl = new TextField("", game.defaultSkin);
        uiTable.add(serverUrl).left().width(300);

        uiTable.row();
        uiTable.add(new Label("Cube Path", game.defaultSkin)).left();
        cubePath = new TextField("", game.defaultSkin);
        uiTable.add(cubePath).left().width(300);

        uiTable.row();
        uiTable.add(new Label("Cards Per Player", game.defaultSkin)).left();
        cardsPerPlayer = new TextField("", game.defaultSkin);
        uiTable.add(cardsPerPlayer).left().width(300);

        uiTable.row();
        uiTable.add(new Label("Draft Type", game.defaultSkin)).left();
        draftType = new TextField("", game.defaultSkin);
        uiTable.add(draftType).left().width(300);

        uiTable.row();
        uiTable.add(new Label("Draft ID", game.defaultSkin)).left();
        draftId = new TextField("", game.defaultSkin);
        uiTable.add(draftId).left().width(300);


        uiTable.row();
        createDraft = new TextButton("Create Draft", game.defaultSkin);
        createDraft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (serverUrl.getText() == null) return;
                if (cubePath.getText() == null) return;
                if (draftType.getText() == null) return;
                if (cardsPerPlayer.getText() == null) return;

                game.serverUrl = serverUrl.getText();
                savePreferences();
                try {
                    int id = ServerProxy.createDraft(serverUrl.getText(), CubeLoader.getCubeDataFromFile(cubePath.getText()), draftType.getText(), cardsPerPlayer.getText());
                    Drafter drafter = ServerProxy.joinDraft(serverUrl.getText(), String.valueOf(id));
                    draftId.setText(String.valueOf(id));
                    game.drafterId = drafter.ID;
                    game.draftType = drafter.DraftType;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uiTable.add(createDraft).colspan(2).width(150).padTop(15);

        uiTable.row();
        joinDraft = new TextButton("Join Draft", game.defaultSkin);
        joinDraft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (serverUrl.getText() == null) return;
                if (draftId.getText() == null) return;

                game.serverUrl = serverUrl.getText();
                savePreferences();
                try{
                    Drafter drafter = ServerProxy.joinDraft(serverUrl.getText(), draftId.getText());
                    game.drafterId = drafter.ID;
                    game.draftType = drafter.DraftType;
                    game.enterDraft(game.draftType, draftId.getText(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uiTable.add(joinDraft).colspan(2).width(150);

        uiTable.row();
        startDraft = new TextButton("Start Draft", game.defaultSkin);
        startDraft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.drafterId < 0) return;

                game.serverUrl = serverUrl.getText();
                savePreferences();
                try {
                    ServerProxy.startDraft(serverUrl.getText(), draftId.getText());
                    game.enterDraft(draftType.getText(), draftId.getText(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uiTable.add(startDraft).colspan(2).width(150);

        uiTable.row();
        rejoinDraft = new TextButton("Rejoin Draft", game.defaultSkin);
        rejoinDraft.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (serverUrl.getText() == null) return;
                if (draftId.getText() == null) return;

                game.serverUrl = serverUrl.getText();
                savePreferences();
                try{
                    game.draftType = game.preferences.getString("DraftType");
                    game.enterDraft(game.draftType, draftId.getText(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        uiTable.add(rejoinDraft).colspan(2).width(150);
    }

    private void loadPreferences(){
        game.preferences = Gdx.app.getPreferences("Settings");
    }

    private void populateFields(){
        serverUrl.setText(game.preferences.getString("ServerUrl"));
        cubePath.setText(game.preferences.getString("CubePath"));
        cardsPerPlayer.setText(game.preferences.getString("CardsPerPlayer"));
        draftType.setText(game.preferences.getString("DraftType"));
    }

    private void savePreferences(){
        game.preferences.putString("ServerUrl", serverUrl.getText());
        game.preferences.putString("CubePath", cubePath.getText());
        game.preferences.putString("CardsPerPlayer", cardsPerPlayer.getText());
        game.preferences.putString("DraftType", draftType.getText());
        game.preferences.flush();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
        savePreferences();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
