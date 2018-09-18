package com.gooeygames.draftable.client.screens.draft.winston;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.screens.draft.DraftScreen;
import com.gooeygames.draftable.client.utils.ServerProxy;

import java.util.List;

public class WinstonDraftScreen extends DraftScreen {
    protected TextButton takeButton;
    protected TextButton skipButton;
    protected Label[] pileLabels;

    public WinstonDraftScreen(Draftable game, String draftId, boolean rejoin) {
        super(game, draftId, rejoin);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void startTurn() {
        currentPileIndex = nextAvailablePile(-1);
        if (currentPileIndex >= 0){
            viewPiles();
            displayPile(currentPileIndex);
            viewButtons();
        }else{
            hideButtons();
        }
    }

    @Override
    protected void initialize(){
        super.initialize();

        if (!gameStarted) return;

        takeButton = new TextButton("Take", game.defaultSkin);
        takeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                takePile();
            }
        });
        buttons.add(takeButton);
        skipButton = new TextButton("Skip", game.defaultSkin);
        skipButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                skipPile();
            }
        });
        buttons.add(skipButton);
        Table buttonTable = new Table();
        buttonTable.add(takeButton).left().width(300).height(60);
        buttonTable.add().width(100);
        buttonTable.add(viewCollectionButton).center().align(Align.center).width(300).height(60);
        buttonTable.add().width(100);
        buttonTable.add(skipButton).right().width(300).height(60);
        uiTable.add(buttonTable);

        hideButtons();

        if (isTurn){
            displayPile(currentPileIndex);
            viewButtons();
        }
        if (!viewingCollection){
            viewPiles();
        }else{
            viewCollection();
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

    protected void skipPile(){
        try {
            ServerProxy.skipPile(serverUrl, draftId, currentPileIndex);
            currentPileIndex = nextAvailablePile(currentPileIndex);
            displayPile(currentPileIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    protected void saveDraftState(){
        super.saveDraftState();
        game.preferences.putInteger("CurrentPileIndex", currentPileIndex);
        game.preferences.flush();
    }

    @Override
    protected void loadDraftState(){
        super.loadDraftState();
        currentPileIndex = game.preferences.getInteger("CurrentPileIndex");
    }
}
