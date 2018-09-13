package com.gooeygames.draftable.client.screens.draft.winston;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.cards.CardColor;
import com.gooeygames.draftable.client.cards.CardWebTextureLoader;
import com.gooeygames.draftable.client.jsonobjects.DraftStatus;
import com.gooeygames.draftable.client.screens.draft.DraftScreen;
import com.gooeygames.draftable.client.utils.ServerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WinstonDraftScreen extends DraftScreen {
    protected Table collectionTable;
    protected Map<CardColor, WidgetGroup> colorGroups;
    protected Table collectionCardTable;
    protected Table statusTable;
    protected Table pileTable;
    protected TextButton takeButton;
    protected TextButton skipButton;
    protected Label[] pileLabels;

    protected int currentPileIndex;
    protected Label currentPileLabel;
    protected Image viewedCard;
    protected boolean viewingCollection;
    protected DraftStatus status;

    protected boolean isTurn;
    protected boolean gameStarted;
    protected float queryTimer;

    public WinstonDraftScreen(Draftable game, String draftId) {
        super(game, draftId);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

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
                currentPileIndex = nextAvailablePile(-1);
                if (currentPileIndex >= 0){
                    viewPiles();
                    displayPile(currentPileIndex);
                    viewButtons();
                }else{
                    hideButtons();
                }
            }
            queryTimer = 1f;
            return;
        }
    }

    @Override
    protected void initialize(){
        if (!gameStarted) return;

        uiTable.clear();
        statusTable = new Table();
        uiTable.add(statusTable);
        resetStatus();
        //uiTable.row();
        //Table currentPileTable = new Table();
        //currentPileLabel = new Label("Current Pile: 1", game.defaultSkin);
        //currentPileTable.add(currentPileLabel);
        //uiTable.add(currentPileTable);

        uiTable.row().expand();
        pileTable = new Table();
        uiTable.add(pileTable).pad(10);

        uiTable.row();
        takeButton = new TextButton("Take", game.defaultSkin);
        takeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                takePile();
            }
        });
        skipButton = new TextButton("Skip", game.defaultSkin);
        skipButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                skipPile();
            }
        });
        TextButton viewCollectionButton = new TextButton("Collection", game.defaultSkin);
        viewCollectionButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                viewCollection();
            }
        });
        Table buttonTable = new Table();
        buttonTable.add(takeButton).left().width(300).height(60);
        buttonTable.add().width(100);
        buttonTable.add(viewCollectionButton).center().align(Align.center).width(300).height(60);
        buttonTable.add().width(100);
        buttonTable.add(skipButton).right().width(300).height(60);
        uiTable.add(buttonTable);

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

        viewedCard = new Image(game.blankCardTexture);
        viewedCard.setPosition((Gdx.graphics.getWidth()/2) - viewedCard.getWidth()/2,
                (Gdx.graphics.getHeight()/2) - viewedCard.getHeight()/2);
        stage.addActor(viewedCard);
        hideCard();

        hideButtons();

        if (!viewingCollection){
            viewPiles();
            if (isTurn){
                displayPile(currentPileIndex);
                viewButtons();
            }
        }else{
            viewCollection();
            sortCollection();
        }
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

    protected void takePile() {
        try {
            List<Card> pile = ServerProxy.takePile(serverUrl, draftId, currentPileIndex);
            addToCollection(pile);
            viewCollection();
            isTurn = false;
            hideButtons();
            pileTable.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void skipPile(){
        try {
            if (currentPileIndex >= 3) return;
            ServerProxy.skipPile(serverUrl, draftId, currentPileIndex);
            currentPileIndex = nextAvailablePile(currentPileIndex);
            displayPile(currentPileIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void displayPile(int index){
        pileTable.clear();
        List<Card> pile = getPile(index);

        int cardIndex = 0;
        for (Card card : pile){
            pileTable.add(getCardImage(card));
            cardIndex++;
            if (cardIndex % 5 == 0){
                pileTable.row();
            }
        }

        resetStatus();
        Label.LabelStyle style = new Label.LabelStyle(pileLabels[currentPileIndex].getStyle());
        style.background = new Image(game.redPixel).getDrawable();
        pileLabels[currentPileIndex].setStyle(style);

        if (nextAvailablePile(currentPileIndex) == -1){
            skipButton.setVisible(false);
        }
    }

    protected int nextAvailablePile(int index){
        for (int i = index+1; i < 4; i++){
            if (status.PileSizes[i] != 0){
                return i;
            }
        }
        return -1;
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

    protected void hideCard(){
        viewedCard.setVisible(false);
        viewedCard.clearListeners();
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

    protected void resetStatus(){
        statusTable.clear();
        try {
            status = ServerProxy.getStatus(serverUrl, draftId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pileLabels = new Label[4];
        pileLabels[0] = new Label("Pile 1: " + status.PileSizes[0], game.defaultSkin);
        pileLabels[1] = new Label("Pile 2: " + status.PileSizes[1], game.defaultSkin);
        pileLabels[2] = new Label("Pile 3: " + status.PileSizes[2], game.defaultSkin);
        pileLabels[3] = new Label("Cube: " + status.PileSizes[3], game.defaultSkin);

        statusTable.add(pileLabels[0]);
        statusTable.add().width(100);
        statusTable.add(pileLabels[1]);
        statusTable.add().width(100);
        statusTable.add(pileLabels[2]);
        statusTable.add().width(100);
        statusTable.add(pileLabels[3]);
    }

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

    protected void viewButtons(){
        skipButton.setVisible(true);
        takeButton.setVisible(true);
    }

    protected void hideButtons(){
        skipButton.setVisible(false);
        takeButton.setVisible(false);
    }

    protected void addToCollection(List<Card> cards){
        for (Card card : cards){
            collection.add(card);
        }

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
}
