package de.ipk.ag_ba.image.operation;

/**
 * @author pape
 *         1 - hsv, saturation to zero l = [h, 0, v]
 *         2 - use this formula l = 0.3 * r + 0.59 * g + 0.11 * b
 *         3 - l = max(r, g, b)
 */
public enum GrayscaleMode {
	MODE_1,
	MODE_2,
	MODE_3;
}
