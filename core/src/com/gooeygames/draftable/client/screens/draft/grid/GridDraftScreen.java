package com.gooeygames.draftable.client.screens.draft.grid;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.gooeygames.draftable.client.Draftable;
import com.gooeygames.draftable.client.cards.Card;
import com.gooeygames.draftable.client.screens.draft.DraftScreen;
import com.gooeygames.draftable.client.utils.ServerProxy;

import java.util.ArrayList;
import java.util.List;

public class GridDraftScreen extends DraftScreen {

    public GridDraftScreen(Draftable game, String draftId, boolean rejoin) {
        super(game, draftId, rejoin);
    }

    @Override
    protected void initialize() {
        super.initialize();

        if (!gameStarted) return;

        Table buttonTable = new Table();
        buttonTable.add(viewCollectionButton).center().align(Align.center).width(300).height(60);

        uiTable.add(buttonTable);
        if (isTurn){
            displayGrid();
        }
    }

    protected void displayGrid(){
        pileTable.clear();
        Card[] grid = getGrid();
        pileTable.add();

        List<TextButton> pickButtons = new ArrayList<>();

        for (int i = 0; i < 3; i++){
            TextButton pick = new TextButton("Pick", game.defaultSkin);
            pickButtons.add(pick);
            pileTable.add(pick);
        }
        pileTable.row();
        for (int i = 0; i < grid.length; i++){
            if (i % 3 == 0){
                TextButton pick = new TextButton("Pick", game.defaultSkin);
                pickButtons.add(pick);
                pileTable.add(pick);
            }
            Image image = grid[i] == null ? null : getCardImage(grid[i]);
            pileTable.add(image);
            if (i % 3 == 2) pileTable.row();
        }

        pickButtons.get(0).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(3);
            }
        });
        pickButtons.get(1).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(4);
            }
        });
        pickButtons.get(2).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(5);
            }
        });
        pickButtons.get(3).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(0);
            }
        });
        pickButtons.get(4).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(1);
            }
        });
        pickButtons.get(5).addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                takePile(2);
            }
        });
    }

    protected Card[] getGrid(){
        Card[] grid = new Card[9];
        for (int i = 0; i < grid.length; i++){
            List<Card> pile = getPile(i);
            if (pile.isEmpty()) continue;
            grid[i] = pile.get(0);
        }
        return grid;
    }

    @Override
    protected void startTurn() {
        viewPiles();
        displayGrid();
        resetStatus();
    }

    @Override
    protected void resetStatus() {
        statusTable.clear();
        try {
            status = ServerProxy.getStatus(serverUrl, draftId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Label remainingCardLabel = new Label("Cube: " + status.PileSizes[9], game.defaultSkin);

        statusTable.add(remainingCardLabel);
    }
}
