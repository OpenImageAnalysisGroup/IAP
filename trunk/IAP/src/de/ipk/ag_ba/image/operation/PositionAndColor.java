package de.ipk.ag_ba.image.operation;

import java.awt.Color;

/**
 * Creates an object which stores the position and the intensity int-value of an image pixel.
 * 
 * @author pape
 */
public class PositionAndColor {
	
	public int x;
	public int y;
	public int intensityInt;
	
	public PositionAndColor(int x, int y, int colorInt) {
		this.x = x;
		this.y = y;
		this.intensityInt = colorInt;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (((PositionAndColor) obj) != null && this != null) {
			if (this.x == ((PositionAndColor) obj).x && this.y == ((PositionAndColor) obj).y)
				return true;
			else
				return false;
		} else
			return false;
	}
	
	public int distQ(PositionAndColor a) {
		return (x - a.x) * (x - a.x) + (y - a.y) * (y - a.y);
	}
	
	public void setIntensityInt(int val) {
		this.intensityInt = val;
	}
	
	@Override
	public String toString() {
		Color c = new Color(intensityInt);
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		return x + ":" + y + "=" + intensityInt + "/" + r + "," + g + "," + b;
	}
}
