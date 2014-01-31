package iap.blocks.extraction;

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
			img = highlightOnVIS(img);
		
		return img;
	}
	
	private Image highlightOnVIS(Image imgVisLTHighlighted) {
		float ratio = (float) ((regionColorHsvAvg.getRed() / 255.) / (regionColorRGBAvg.getRed() / 255.));
		if (ratio > 1) {
			float sat = -1f + ratio * 2f - 1;
			if (sat < 0)
				sat = 0;
			if (sat > 1)
				sat = 1;
			
			imgVisLTHighlighted = imgVisLTHighlighted
					.io()
					.canvas()
					.fillCircle(xPosition, yPosition, radiusfin * 2, radiusfin * 2 + 5, Color.HSBtoRGB((float) (0.7), sat, (float) 1.0),
							0.8)
					.fillCircle(xPosition, yPosition, radiusfin, radiusfin * 2,
							Color.HSBtoRGB((float) (regionColorRGBAvg.getRed() / 255.), (float) 1.0, (float) 1.0),
							0.8)
					.fillCircle(xPosition, yPosition, radiusfin, Color.HSBtoRGB((float) (regionColorHsvAvg.getRed() / 255.), (float) 1.0, (float) 1.0), 0.5)
					.getImage();
		} else {
			float sat = -1f + ratio * 2f;
			if (sat < 0)
				sat = 0;
			imgVisLTHighlighted = imgVisLTHighlighted
					.io()
					.canvas()
					.fillCircle(xPosition, yPosition, radiusfin * 2, radiusfin * 2 + 5, Color.HSBtoRGB((float) (0.0), 1 - sat, (float) 1.0),
							0.8)
					.fillCircle(xPosition, yPosition, radiusfin, radiusfin * 2,
							Color.HSBtoRGB((float) (regionColorRGBAvg.getRed() / 255.), (float) 1.0, (float) 1.0),
							0.8)
					.fillCircle(xPosition, yPosition, radiusfin, Color.HSBtoRGB((float) (regionColorHsvAvg.getRed() / 255.), (float) 1.0, (float) 1.0), 0.5)
					.getImage();
		}
		return imgVisLTHighlighted;
	}
	
	private Image highlightOnFluo(Image imgGamma) {
		imgGamma = imgGamma
				.io()
				.canvas()
				.drawLine(xPosition, yPosition, centerOfGravity.x + dim[0], centerOfGravity.y + dim[2], Color.ORANGE.getRGB(), 0.2, 1)
				.drawCircle(xPosition, yPosition, radiusfin + 4, Color.BLUE.getRGB(), 0.5, 1)
				.text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition - 20 + off, "LEAF: " + (iF + 1),
						Color.BLACK)
				.text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition + 0 + off,
						"DIRECTION: " + (directionF > 0 ? "DOWN" : "UP"),
						Color.BLACK)
				.text(centerOfGravity.x + xPosition + 20, centerOfGravity.y + yPosition + 20 + off,
						"CHLO: " + regionColorRGBAvg.getRed() + ", PHEN: " + regionColorRGBAvg.getGreen() + ", CLAS: " + regionColorRGBAvg.getBlue()
						, Color.BLACK)
				.getImage();
		return imgGamma;
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