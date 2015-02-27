package de.ipk.ag_ba.image.operation.channels;

public class ColorFloatRGB {
	
	/**
	 * @param r
	 *           0..255
	 * @param g
	 *           0..255
	 * @param b
	 *           0..255
	 * @param hsbvals
	 *           HSB
	 * @return
	 */
	public static float[] RGBtoHSB(float r, float g, float b, float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}
		float cmax = (r > g) ? r : g;
		if (b > cmax)
			cmax = b;
		float cmin = (r < g) ? r : g;
		if (b < cmin)
			cmin = b;
		
		brightness = (cmax) / 255.0f;
		if (cmax >= 0.1)
			saturation = (cmax - cmin) / (cmax);
		else
			saturation = 0;
		if (saturation < 0.1)
			hue = 0;
		else {
			float redc = (cmax - r) / (cmax - cmin);
			float greenc = (cmax - g) / (cmax - cmin);
			float bluec = (cmax - b) / (cmax - cmin);
			if (r == cmax)
				hue = bluec - greenc;
			else
				if (g == cmax)
					hue = 2.0f + redc - bluec;
				else
					hue = 4.0f + greenc - redc;
			hue = hue / 6.0f;
			if (hue < 0)
				hue = hue + 1.0f;
		}
		hsbvals[0] = hue;
		if (saturation > 2)
			System.out.println(saturation);
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}
	
}
