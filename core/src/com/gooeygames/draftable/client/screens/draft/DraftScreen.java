package com.gooeygames.draftable.client.screens.draft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.cards.CardColor;
import com.gooeygames.draftable.client.cards.CardWebTextureLoader;
import com.gooeygames.draftable.client.cards.Collection;
import com.gooeygames.draftable.client.jsonobjects.DraftStatus;
import com.gooeygames.draftable.client.utils.ServerProxy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DraftScreen implements Screen {

    protected Draftable game;
    protected String draftId;
    protected String serverUrl;
    protected Stage stage;

    protected Table uiTable;
    protected Collection collection;
    protected Image viewedCard;
    protected Table statusTable;
    protected Table pileTable;
    protected List<TextButton> buttons;
    protected TextButton viewCollectionButton;

    protected Table collectionTable;
    protected Map<CardColor, WidgetGroup> colorGroups;
    protected Table collectionCardTable;
    protected boolean viewingCollection;

    protected boolean isTurn;
    protected boolean gameStarted;
    protected float queryTimer;
    protected DraftStatus status;

    protected int currentPileIndex;

    public DraftScreen(Draftable game, String draftId, boolean rejoin) {
        this.game = game;
        this.draftId = draftId;
        this.collection = new Collection();

        if (rejoin){
            gameStarted = true;
            loadDraftState();
        }
    }

    @Override
    public void show() {
        serverUrl = game.serverUrl;
        initialize();
        preloadCube(draftId);
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

        queryTimer -= delta;

        if (!gameStarted && queryTimer <= 0) {
            try {
                gameStarted = ServerProxy.isStarted(serverUrl, draftId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (gameStarted){
                initialize();
            }
            queryTimer = 1f;
            return;
        }

        if (!isTurn && queryTimer <= 0) {
            try {
                status = ServerProxy.getStatus(serverUrl, draftId);
                isTurn = status.TurnID == game.drafterId;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isTurn){
                startTurn();
            }
            queryTimer = 1f;
            return;
        }
    }

    protected abstract void startTurn();

    protected abstract void resetStatus();

    protected void viewCollection(){
        uiTable.setVisible(false);
        collectionTable.setVisible(true);
        viewingCollection = true;
    }

    protected void viewPiles(){
        uiTable.setVisible(true);
        collectionTable.setVisible(false);
        viewingCollection = false;
    }

    protected void hideCard(){
        viewedCard.setVisible(false);
        viewedCard.clearListeners();
    }

    protected void viewButtons(){
        for (TextButton button : buttons){
            button.setVisible(true);
        }
    }

    protected void hideButtons(){
        for (TextButton button : buttons){
            button.setVisible(false);
        }
    }

    protected void initialize(){
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);

        if (!gameStarted) return;

        buttons = new ArrayList<>();

        collectionTable = new Table().bottom();
        collectionTable.setFillParent(true);
        stage.addActor(collectionTable);

        collectionCardTable = new Table();
        collectionTable.add(collectionCardTable);

        colorGroups = new HashMap<>();
        int xPos = -50;
        for (CardColor color : CardColor.values()){
            WidgetGroup group = new WidgetGroup();
            colorGroups.put(color, group);
            collectionCardTable.add(group);

            group.setPosition(xPos, 10);
        }

        collectionTable.row();
        TextButton viewDraftButton = new TextButton("Draft", game.defaultSkin);
        viewDraftButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                viewPiles();
            }
        });
        collectionTable.add(viewDraftButton).width(300).height(60);

        sortCollection();

        viewedCard = new Image(game.blankCardTexture);
        viewedCard.setPosition((Gdx.graphics.getWidth()/2) - viewedCard.getWidth()/2,
                (Gdx.graphics.getHeight()/2) - viewedCard.getHeight()/2);
        stage.addActor(viewedCard);
        hideCard();

        uiTable.clear();
        statusTable = new Table();
        uiTable.add(statusTable);
        resetStatus();

        uiTable.row().expand();
        pileTable = new Table();
        uiTable.add(pileTable).pad(10);

        uiTable.row();

        viewCollectionButton = new TextButton("Collection", game.defaultSkin);
        viewCollectionButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                viewCollection();
            }
        });

        if (!viewingCollection){
            viewPiles();
        }else{
            viewCollection();
        }

        resetStatus();
    }

    protected List<Card> getPile(int index) {
        try {
            List<Card> pile = ServerProxy.getPile(serverUrl, draftId, index);
            return pile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void takePile(int index) {
        try {
            List<Card> pile = ServerProxy.takePile(serverUrl, draftId, index);
            addToCollection(pile);
            viewCollection();
            isTurn = false;
            hideButtons();
            pileTable.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void addToCollection(List<Card> cards){
        for (Card card : cards){
            collection.add(card);
        }

        if (colorGroups == null) return;
        sortCollection();
    }

    private void populateCollectionTable(WidgetGroup group, List<Card> cards){
        group.clear();

        int offset = Gdx.graphics.getHeight();
        for (Card card : cards){
            Image cardImage = getCardImage(card);
            float scale = (Gdx.graphics.getWidth()/8)/cardImage.getWidth();
            cardImage.setScale(scale);
            cardImage.setPosition(group.getX() - (cardImage.getWidth()/2 * scale), offset - cardImage.getHeight()*scale-60);
            offset -= 70*scale;
            group.addActor(cardImage);
        }
    }

    protected Image getCardImage(Card card){
        Image image = new Image(CardWebTextureLoader.getTexture(card));
        image.setScaling(Scaling.fit);
        image.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                viewCard(image);
            }
        });
        return image;
    }

    protected void viewCard(Image image){
        viewedCard.setVisible(true);
        viewedCard.setDrawable(image.getDrawable());
        viewedCard.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                hideCard();
            }
        });
    }

    protected void sortCollection(){
        List<Vector2> points = getEvenlyDispersedPoints(CardColor.values().length, Gdx.graphics.getWidth()*1.1f, new Vector2(0, 0));
        int index = 0;
        for (CardColor color : CardColor.values()){
            WidgetGroup group = colorGroups.get(color);
            group.setPosition(points.get(index).x, points.get(index).y);
            populateCollectionTable(group, collection.get().get(color));
            index++;
        }
    }

    protected List<Vector2> getEvenlyDispersedPoints(int numberOfPoints, float maxWidth, Vector2 basePos){
        List<Vector2> points = new ArrayList<>();
        float segmentLength = maxWidth / (numberOfPoints + 1);

        Vector2 currentPos = new Vector2(basePos.x, basePos.y).sub(new Vector2(maxWidth/2, 0)).add(new Vector2(segmentLength, 0));
        for (int i = 0; i < numberOfPoints; i++){
            points.add(new Vector2(currentPos));
            currentPos.x += segmentLength;
        }
        return points;
    }

    protected void saveDraftState(){
        String serializedCards = new Gson().toJson(collection.cardList);
        game.preferences.putString("DraftedCards", serializedCards);
        game.preferences.putInteger("DrafterId", game.drafterId);
        game.preferences.putString("DraftType", game.draftType);
        game.preferences.putBoolean("IsTurn", isTurn);
        game.preferences.flush();
    }

    protected void loadDraftState(){
        String serializedCards = game.preferences.getString("DraftedCards");
        List<Card> deserializedCards = new GsonBuilder().create().fromJson(serializedCards, new TypeToken<List<Card>>(){}.getType());
        collection = new Collection();
        addToCollection(deserializedCards);

        game.drafterId = game.preferences.getInteger("DrafterId");
        isTurn = game.preferences.getBoolean("IsTurn");
    }

    @Override
    public void resize(int width, int height) {
        initialize();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        saveDraftState();
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
