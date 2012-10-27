package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class BlCutZoomedImages extends AbstractBlock {
	
	private double[][] zoomLevels;
	
	@Override
	protected void prepare() {
		super.prepare();
		String zoomID = "zoom-top:";
		if (options.getCameraPosition() == CameraPosition.SIDE)
			zoomID = "zoom-side:";
		
		zoomLevels = getFillGradeFromOutlierString(zoomID);
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		int w = 1624;
		int h = 1234;
		if (image.getWidth() < image.getHeight()) {
			w = 1234;
			h = 1624;
		}
		return cut(image.io().adjustWidthHeightRatio(w, h, 10));
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		return cut(mask.io().adjustWidthHeightRatio(1624, 1234, 10));
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
		// add border or cut outside
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
			for (String s : outlierDef.split("//")) {
				s = s.trim();
				if (s.startsWith(zoomID)) {
					s = s.substring(zoomID.length());
					String[] levels = s.split(";");
					double[][] res = new double[3][4];
					res[0][0] = Double.parseDouble(levels[0].split(":")[0]) / 100d;
					res[0][1] = Double.parseDouble(levels[1].split(":")[0]) / 100d;
					res[0][2] = Double.parseDouble(levels[2].split(":")[0]) / 100d;
					res[0][3] = Double.parseDouble(levels[3].split(":")[0]) / 100d;
					
					res[1][0] = Double.parseDouble(levels[0].split(":")[1]);
					res[1][1] = Double.parseDouble(levels[1].split(":")[1]);
					res[1][2] = Double.parseDouble(levels[2].split(":")[1]);
					res[1][3] = Double.parseDouble(levels[3].split(":")[1]);
					
					res[2][0] = Double.parseDouble(levels[0].split(":")[2]);
					res[2][1] = Double.parseDouble(levels[1].split(":")[2]);
					res[2][2] = Double.parseDouble(levels[2].split(":")[2]);
					res[2][3] = Double.parseDouble(levels[3].split(":")[2]);
					
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
		// processedImages.fluo().io().crossfade(processedImages.nir(), 0.5d).print("overlay");
		// processedImages.copy().equalize().print("debug");
	}
}
