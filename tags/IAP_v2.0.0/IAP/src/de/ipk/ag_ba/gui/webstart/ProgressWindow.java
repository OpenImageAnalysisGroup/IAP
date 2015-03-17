package de.ipk.ag_ba.gui.webstart;

import org.graffiti.editor.SplashScreenInterface;

public interface ProgressWindow extends SplashScreenInterface {
	
	void show(boolean undecorated);
	
	void hide();
	
}
