package de.ipk.ag_ba;

import org.graffiti.editor.SplashScreenInterface;

public class SplashScreenDontPrintProgress implements SplashScreenInterface {
	
	@Override
	public int getMaximum() {
		return 0;
	}
	
	@Override
	public void setMaximum(int maximum) {
	}
	
	@Override
	public void setText(String text) {
	}
	
	@Override
	public void setValue(int value) {
	}
	
	@Override
	public int getValue() {
		return 0;
	}
	
	@Override
	public void setVisible(boolean b) {
	}
	
	@Override
	public void setInitialisationFinished() {
	}
	
}
