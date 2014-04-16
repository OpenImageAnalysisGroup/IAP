package iap.blocks.extraction.postprocessors;

import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.Color;
import java.awt.Point;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

final class LeafTipHighlighter implements RunnableOnImageSet {
	/**
	 * 
	 */
	private final int xPosition;
	private final int yPosition;
	private final Color regionColorRGBAvg;
	private final int radiusfin;
	private final int[] dim;
	private final int iF;
	private final Color regionColorHsvAvg;
	private final int off;
	private final Point centerOfGravity;
	private final int directionF;
	private final CameraType camType;
	
	LeafTipHighlighter(int xPosition, Color regionColorRGBAvg, int radiusfin, int[] dim, int yPosition, int iF,
			Color regionColorHsvAvg, int off,
			Point centerOfGravity, int directionF, CameraType camType) {
		this.xPosition = xPosition;
		this.regionColorRGBAvg = regionColorRGBAvg;
		this.radiusfin = radiusfin;
		this.dim = dim;
		this.yPosition = yPosition;
		this.iF = iF;
		this.regionColorHsvAvg = regionColorHsvAvg;
		this.off = off;
		this.centerOfGravity = centerOfGravity;
		this.directionF = directionF;
		this.camType = camType;
	}
	
	@Override
	public Image postProcessMask(Image img) {
		if (camType == CameraType.FLUO)
			img = highlightOnFluo(img);
		if (camType == CameraType.VIS)
			img = highlightOnVis(img);
		if (camType == CameraType.NIR)
			img = highlightOnNir(img);
		
		return img;
	}
	
	private Image highlightOnVis(Image imgVisLTHighlighted) {
		float ratio = (float) ((regionColorHsvAvg.getRed() / 255.) / (regionColorRGBAvg.getRed() / 255.));
		float hue, sat;
		int fontsizeVis = 24;
		if (ratio > 1) {
			hue = 0.7f;
			sat = -1f + ratio * 2f - 1;
			if (sat < 0)
				sat = 0;
			if (sat > 1)
				sat = 1;
		} else {
			hue = 0.0f;
			sat = 1 - (-1f + ratio * 2f);
			if (sat < 0)
				sat = 1;
		}
		int rot = 315;
		de.ipk.ag_ba.image.operations.complex_hull.Point p1 = new de.ipk.ag_ba.image.operations.complex_hull.Point((int) (radiusfin * 4
				* Math.cos(rot * (Math.PI / 180))
				+ xPosition), (int) (radiusfin * 3 * Math.sin(rot
				* (Math.PI / 180)) + yPosition));
		de.ipk.ag_ba.image.operations.complex_hull.Point p2 = new de.ipk.ag_ba.image.operations.complex_hull.Point((int) (radiusfin * 8
				* Math.cos(rot * (Math.PI / 180))
				+ xPosition), (int) (radiusfin * 6 * Math.sin(rot * (Math.PI / 180)) + yPosition));
		imgVisLTHighlighted = imgVisLTHighlighted
				.io()
				.canvas()
				.fillCircle(xPosition, yPosition, radiusfin * 4, radiusfin * 4 + 8, Color.HSBtoRGB(hue, sat, (float) 1.0),
						0.8)
				.fillCircle(xPosition, yPosition, radiusfin * 2, radiusfin * 4,
						Color.HSBtoRGB((float) (regionColorRGBAvg.getRed() / 255.), (float) 1.0, (float) 1.0),
						0.8)
				.fillCircle(xPosition, yPosition, radiusfin * 2, Color.HSBtoRGB((float) (regionColorHsvAvg.getRed() / 255.), (float) 1.0, (float) 1.0), 0.5)
				.drawLine(p1, p2, Color.BLACK.getRGB(), 0.5, 2)
				.drawLine(p2, p2.moved(300.0, 0.0), Color.BLACK.getRGB(), 0.5, 2)
				.text((int) p2.x, (int) p2.y - 3,
						"HSV in: " + regionColorRGBAvg.getRed() + " | " + regionColorRGBAvg.getGreen() + " | " + regionColorRGBAvg.getBlue(), Color.BLACK,
						fontsizeVis)
				.text((int) p2.x, (int) p2.y - fontsizeVis - 8,
						"HSV out: " + regionColorHsvAvg.getRed() + " | " + regionColorHsvAvg.getGreen() + " | " + regionColorHsvAvg.getBlue(), Color.BLACK,
						fontsizeVis)
				.getImage();
		
		return imgVisLTHighlighted;
	}
	
	private Image highlightOnFluo(Image imgGamma) {
		imgGamma = imgGamma
				.io()
				.canvas()
				.drawLine(xPosition, yPosition, centerOfGravity.x + dim[0], centerOfGravity.y + dim[2], Color.ORANGE.getRGB(), 0.2, 1)
				.drawCircle(xPosition, yPosition, radiusfin + 4, Color.BLUE.getRGB(), 0.5, 1)
				.text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition - 20 + off, "LEAF: " + iF,
						Color.BLACK)
				// .text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition + 0 + off,
				// "DIRECTION: " + (directionF > 0 ? "DOWN" : "UP"),
				// Color.BLACK)
				.text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition + 0 + off,
						"CHLO: " + regionColorRGBAvg.getRed() + ", PHEN: " + regionColorRGBAvg.getGreen() + ", CLAS: " + regionColorRGBAvg.getBlue()
						, Color.BLACK)
				.getImage();
		return imgGamma;
	}
	
	private Image highlightOnNir(Image img) {
		img = img
				.io()
				.canvas()
				.drawCircle(xPosition, yPosition, radiusfin,
						new Color(regionColorRGBAvg.getBlue() - 50, regionColorRGBAvg.getBlue() - 50, regionColorRGBAvg.getBlue() - 50).getRGB(), 0.5, 2)
				.text(xPosition + 6, yPosition - 6, "Avg Nir: " + (regionColorRGBAvg.getBlue() - 30), Color.BLACK, 7)
				.getImage();
		return img;
	}
	
	@Override
	public Image postProcessImage(Image image) {
		return image;
	}
	
	@Override
	public CameraType getConfig() {
		CameraType cT = null;
		if (camType == CameraType.FLUO)
			cT = CameraType.FLUO;
		if (camType == CameraType.VIS)
			cT = CameraType.VIS;
		if (camType == CameraType.NIR)
			cT = CameraType.NIR;
		
		return cT;
	}
}