package de.ipk.ag_ba.image.operation.channels;

import java.awt.Color;

import org.color.ColorUtil;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class ChannelCalculation {
	
	private final ImageOperation imageOperation;
	
	public ChannelCalculation(ImageOperation imageOperation) {
		this.imageOperation = imageOperation;
	}
	
	/**
	 * @return (r,g,b) = (r-g, r-g, r-g)
	 */
	public ChannelCalculation redMinusGreen() {
		int[] p = imageOperation.getAs1D();
		int r, g, b;
		for (int i = 0; i < p.length; i++) {
			int c = p[i];
			if (c == ImageOperation.BACKGROUND_COLORint)
				continue;
			r = ((c & 0xff0000) >> 16); // R 0..1
			g = ((c & 0x00ff00) >> 8); // G 0..1
			b = (c & 0x0000ff); // B 0..1
			
			r = r - g;
			if (r < 0)
				r = 0;
			g = r;
			b = r;
			
			if (r > 255)
				r = 255;
			if (g > 255)
				g = 255;
			if (b > 255)
				b = 255;
			p[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		// getImage().copy().show("FUUUU");
		return this;
	}
	
	/**
	 * @return (r,g,b) = (r/g*255, r/g*255, r/g*255)
	 */
	public ChannelCalculation redDividedByGreen() {
		int[] p = imageOperation.getAs1D();
		int r, g, b;
		imageOperation.copy().show("FFF");
		for (int i = 0; i < p.length; i++) {
			int c = p[i];
			if (c == ImageOperation.BACKGROUND_COLORint)
				continue;
			r = ((c & 0xff0000) >> 16); // R
			g = ((c & 0x00ff00) >> 8); // G
			b = (c & 0x0000ff); // B
			
			if (g > 0) {
				r = (int) (r / (double) g);
			}
			if (r >= 255)
				r = 254;
			g = 0;
			b = 0;
			
			if (r > 255)
				r = 255;
			if (g > 255)
				g = 255;
			if (b > 255)
				b = 255;
			p[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		// imageOperation.copy().show("UUU");
		return this;
	}
	
	public ChannelCalculation labA() {
		int[] p = imageOperation.getAs1D();
		int r, g, b;
		float[][][] lab = ImageOperation.getLabCubeInstance();
		for (int i = 0; i < p.length; i++) {
			int c = p[i];
			if (c == ImageOperation.BACKGROUND_COLORint)
				continue;
			r = ((c & 0xff0000) >> 16); // R 0..1
			g = ((c & 0x00ff00) >> 8); // G 0..1
			b = (c & 0x0000ff); // B 0..1
			
			// int Li = (int) lab[r][g][b];
			int ai = (int) lab[r][g][b + 256];
			// int bi = (int) ImageOperation.labCube[r][g][b + 512];
			
			// imageOperation.getR();
			
			p[i] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		// getImage().copy().show("FUUUU");
		return this;
	}
	
	public Image getImage() {
		return imageOperation.getImage();
	}
	
	public ImageOperation io() {
		return imageOperation;
	}
	
	public Image colorDifferenceDeltaE2000(Image image, double wl, double wa, double wb) {
		int[] a = getImage().getAs1A();
		int[] b = image.getAs1A();
		double[] res = new double[Math.min(a.length, b.length)];
		for (int i = 0; i < Math.min(a.length, b.length); i++) {
			res[i] = ColorUtil.deltaE2000(new Color(a[i]), new Color(b[i]), wl, wa, wb);
		}
		if (a.length < b.length)
			return new Image(getImage().getWidth(), getImage().getHeight(), res);
		else
			return new Image(image.getWidth(), image.getHeight(), res);
	}
}
