package tests.plugins.pipelines.tobacco;

/**
 * Creates an object which stores the position and the intensity value of an image pixel.
 * 
 * @author pape
 */
public class PositionAndColor {
	
	public int x;
	public int y;
	public int colorInt;
	
	public PositionAndColor(int x, int y, int color) {
		this.x = x;
		this.y = y;
		this.colorInt = color;
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
}
