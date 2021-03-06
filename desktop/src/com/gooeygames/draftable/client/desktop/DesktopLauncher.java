package com.gooeygames.draftable.client.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gooeygames.draftable.client.Draftable;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1700;
		config.height = 1000;
		new LwjglApplication(new Draftable(), config);
	}
}
