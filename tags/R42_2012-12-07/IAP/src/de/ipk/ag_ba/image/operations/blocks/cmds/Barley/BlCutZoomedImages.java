package de.ipk.ag_ba.image.operations.blocks.cmds.Barley;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * add border or cut outside
 */
public class BlCutZoomedImages extends AbstractBlock {
	
	private double[][] zoomLevels;
	
	@Override
	protected void prepare() {
		super.prepare();
		String zoomID = getString("zoomID-top", "zoom-top:");
		if (options.getCameraPosition() == CameraPosition.SIDE)
			zoomID = getString("zoomID-side", "zoom-side:");
		zoomLevels = getFillGradeFromOutlierString(zoomID);
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		
		if (image != null) {
			int w = getInt("cut-image-width", 1624);
			int h = getInt("cut-image-height", 1234);
			if (image.getWidth() < image.getHeight()) {
				w = getInt("cut-image-height", 1234);
				h = getInt("cut-image-width", 1624);
			}
			return cut(image.io().adjustWidthHeightRatio(w, h, 10));
		} else {
			return null;
		}
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		
		if (mask != null) {
			int w = getInt("cut-image-width", 1624);
			int h = getInt("cut-image-height", 1234);
			if (mask.getWidth() < mask.getHeight()) {
				w = getInt("cut-image-height", 1234);
				h = getInt("cut-image-width", 1624);
			}
			return cut(mask.io().adjustWidthHeightRatio(w, h, 10));
		} else {
			return null;
		}
	}
	
	private FlexibleImage cut(ImageOperation img) {
		double zoom = Double.NaN;
		double offX = Double.NaN;
		double offY = Double.NaN;
		switch (img.getType()) {
			case VIS:
				zoom = zoomLevels[0][0];
				offX = zoomLevels[1][0];
				offY = zoomLevels[2][0];
				break;
			case FLUO:
				zoom = zoomLevels[0][1];
				offX = zoomLevels[1][1];
				offY = zoomLevels[2][1];
				break;
			case NIR:
				zoom = zoomLevels[0][2];
				offX = zoomLevels[1][2];
				offY = zoomLevels[2][2];
				break;
			case IR:
				zoom = zoomLevels[0][3];
				offX = zoomLevels[1][3];
				offY = zoomLevels[2][3];
				break;
		}
		int verticalTooTooMuch = (int) ((1d - zoom) * img.getHeight());
		int b = -verticalTooTooMuch / 2;
		return img.addBorder(b, (int) (b / 2d + offX), (int) (b / 2d + offY), ImageOperation.BACKGROUND_COLORint).getImage();
	}
	
	/**
	 * Example: zoom-top:75;75;75;75 ==> carrier fills 75 percent of VIS;FLUO;NIR;IR images
	 */
	private double[][] getFillGradeFromOutlierString(String zoomID) {
		ImageData i = input().images().getVisInfo();
		if (i == null)
			i = input().images().getFluoInfo();
		if (i == null)
			i = input().images().getNirInfo();
		String outlierDef = i.getParentSample().getParentCondition().getExperimentGlobalOutlierInfo();
		if (outlierDef != null && outlierDef.contains(zoomID)) {
			for (String s : outlierDef.split(getString("outlier-separator", "//"))) {
				s = s.trim();
				if (s.startsWith(zoomID)) {
					s = s.substring(zoomID.length());
					String[] levels = s.split(getString("zoomID-typ-separator", ";"));
					double[][] res = new double[3][4];
					res[0][0] = Double.parseDouble(levels[0].split(getString("zoomID-value-separator", ":"))[0]) / 100d;
					res[0][1] = Double.parseDouble(levels[1].split(getString("zoomID-value-separator", ":"))[0]) / 100d;
					res[0][2] = Double.parseDouble(levels[2].split(getString("zoomID-value-separator", ":"))[0]) / 100d;
					res[0][3] = Double.parseDouble(levels[3].split(getString("zoomID-value-separator", ":"))[0]) / 100d;
					
					res[1][0] = Double.parseDouble(levels[0].split(getString("zoomID-value-separator", ":"))[1]);
					res[1][1] = Double.parseDouble(levels[1].split(getString("zoomID-value-separator", ":"))[1]);
					res[1][2] = Double.parseDouble(levels[2].split(getString("zoomID-value-separator", ":"))[1]);
					res[1][3] = Double.parseDouble(levels[3].split(getString("zoomID-value-separator", ":"))[1]);
					
					res[2][0] = Double.parseDouble(levels[0].split(getString("zoomID-value-separator", ":"))[2]);
					res[2][1] = Double.parseDouble(levels[1].split(getString("zoomID-value-separator", ":"))[2]);
					res[2][2] = Double.parseDouble(levels[2].split(getString("zoomID-value-separator", ":"))[2]);
					res[2][3] = Double.parseDouble(levels[3].split(getString("zoomID-value-separator", ":"))[2]);
					
					return res;
				}
			}
			return new double[][] { { 1d, 1d, 1d, 1d }, { 0d, 0d, 0d, 0d }, { 0d, 0d, 0d, 0d } };
		} else
			return new double[][] { { 1d, 1d, 1d, 1d }, { 0d, 0d, 0d, 0d }, { 0d, 0d, 0d, 0d } };
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
	}
}
