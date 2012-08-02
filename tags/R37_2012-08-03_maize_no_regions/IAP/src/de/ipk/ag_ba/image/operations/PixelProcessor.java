package de.ipk.ag_ba.image.operations;

public interface PixelProcessor {
	public int processPixelForegroundValue(int x, int y, int rgb, int w, int h);
}
