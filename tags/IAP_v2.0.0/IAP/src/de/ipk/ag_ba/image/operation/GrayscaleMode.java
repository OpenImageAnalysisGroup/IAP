package de.ipk.ag_ba.image.operation;

/**
 * @author pape
 *         1 - hsv, saturation to zero l = [h, 0, v]
 *         2 - use this formula l = 0.3 * r + 0.59 * g + 0.11 * b
 *         3 - l = max(r, g, b)
 *         4 - lightness = (Math.max(r, Math.max(g, b)) + Math.min(r, Math.min(g, b))) / 2
 */
public enum GrayscaleMode {
	ZERO_SATURATION,
	LUMINOSITY,
	MAX,
	LIGHTNESS;
}
