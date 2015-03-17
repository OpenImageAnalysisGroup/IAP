package de.ipk.ag_ba.image.operation;

public interface PixelProcessor {
	public int processPixelForegroundValue(int x, int y, int rgb, int w, int h);
}
