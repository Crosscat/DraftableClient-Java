package com.gooeygames.draftable.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.gooeygames.draftable.client.screens.draft.DraftScreen;
import com.gooeygames.draftable.client.screens.draft.winston.WinstonDraftScreen;
import com.gooeygames.draftable.client.screens.settings.SettingsScreen;

public class Draftable extends Game {

    public String serverUrl = "http://localhost:3000";
    public int drafterId = -1;
    public String draftType;

	public Skin defaultSkin;
	public Texture blankCardTexture;
	public Texture redPixel;

	public Preferences preferences;

	@Override
	public void create () {
	    defaultSkin = new Skin(Gdx.files.internal("uiskin.json"));
	    blankCardTexture = new Texture("blankcard.jpg");
	    Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGB565);
		pm.setColor(Color.RED);
		pm.fill();
		redPixel = new Texture(pm);

		setScreen(new SettingsScreen(this));
//         setScreen(new WinstonDraftScreen(this, "0"));
	}

	public void enterDraft(String draftType, String draftId, boolean rejoin){
		switch (draftType.toLowerCase()){
			case "winston":
				setScreen(new WinstonDraftScreen(this, draftId, rejoin));
				break;
		}
	}
}
