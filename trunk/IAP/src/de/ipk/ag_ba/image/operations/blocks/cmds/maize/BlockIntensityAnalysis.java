package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockIntensityAnalysis extends AbstractSnapshotAnalysisBlockFIS {
	
	private int plantAreaVis, plantImagePixelCountVis, plantAreaNir, plantImagePixelCountNir;
	BlockProperty MarkerDistHorizontal;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.plantAreaVis = getInput().getImages().getVis().getIO().countFilledPixels();
		this.plantImagePixelCountVis = getInput().getImages().getVis().getWidth() * getInput().getImages().getVis().getHeight();
		
		if (getInput().getImages().getNir() != null) {
			this.plantAreaNir = getInput().getImages().getNir().getIO().countFilledPixels();
			this.plantImagePixelCountNir = getInput().getImages().getNir().getWidth() * getInput().getImages().getNir().getHeight();
		}
		if (getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT) != null)
			MarkerDistHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	/**
	 * ndiv [-1-1]
	 */
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null) {
			System.err.println("ERROR: BlockIntensityAnalysis: Vis Mask is NULL!");
			return null;
		}
		
		ImageOperation io = new ImageOperation(getInput().getMasks().getVis());
		int pixelsum = io.convert2Grayscale().countFilledPixels();
		
		double averageVis = pixelsum / (double) plantImagePixelCountVis;
		double averageNir = plantAreaNir / (double) plantImagePixelCountNir;
		
		double ndvi = (averageNir - averageVis) / (averageNir + averageVis);
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("ndvi", ndvi);
		getProperties().storeResults("RESULT_", rt, getBlockPosition());
		
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() != null) {
			ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
			
			ResultsTable rt = io.intensity(5).calcualteHistorgram(plantAreaVis, plantImagePixelCountVis, MarkerDistHorizontal);
			getProperties().storeResults("RESULT_fluo.", rt, getBlockPosition());
			return io.getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() != null) {
			ImageOperation io = new ImageOperation(getInput().getMasks().getNir());
			if (getInput().getMasks().getNir().getHeight() > 1) {
				ResultsTable rt = io.intensity(5).calcualteHistorgram(plantAreaVis, plantImagePixelCountVis, MarkerDistHorizontal);
				if (rt != null)
					getProperties().storeResults("RESULT_nir.", rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
}
