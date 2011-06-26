package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas, pape
 */
public class BlockIntensityAnalysis extends AbstractSnapshotAnalysisBlockFIS {
	
	protected boolean isChangingImages() {
		return false;
	}
	
	private int visibleFilledPixels, visibleImageSizeInPixel, nirFilledPixels, nirImageSizeInPixel;
	BlockProperty markerDistanceHorizontally;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.visibleFilledPixels = getInput().getImages().getVis().getIO().countFilledPixels();
		this.visibleImageSizeInPixel = getInput().getImages().getVis().getWidth() * getInput().getImages().getVis().getHeight();
		
		if (getInput().getImages().getNir() != null) {
			this.nirFilledPixels = getInput().getImages().getNir().getIO().countFilledPixels();
			this.nirImageSizeInPixel = getInput().getImages().getNir().getWidth() * getInput().getImages().getNir().getHeight();
		}
		if (getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT) != null)
			markerDistanceHorizontally = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	/**
	 * ndvi [-1-1]
	 */
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() != null) {
			
			ImageOperation io = new ImageOperation(getInput().getMasks().getVis());
			double visibleIntensity = io.intensityOfChannelRed(true);
			double averageVis = visibleIntensity / (double) visibleFilledPixels;
			
			ResultsTable rt = new ResultsTable();
			rt.incrementCounter();
			rt.addValue("ndvi.vis.intensity.average", averageVis);
			
			if (getInput().getMasks().getNir() != null) {
				double nirIntensity = getInput().getMasks().getNir().getIO().intensityOfChannelRed(false);
				
				double averageNir = nirIntensity / (double) nirFilledPixels;
				rt.addValue("ndvi.nir.intensity.average", averageNir);
				
				double ndvi = (averageNir - averageVis) / (averageNir + averageVis);
				rt.addValue("ndvi", ndvi);
			}
			getProperties().storeResults("RESULT_", rt, getBlockPosition());
			return getInput().getMasks().getVis();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() != null) {
			ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
			
			ResultsTable rt = io.intensity(5).calculateHistorgram(); // markerDistanceHorizontally
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
				ResultsTable rt = io.intensity(5).calculateHistorgram(); // markerDistanceHorizontally
				if (rt != null)
					getProperties().storeResults("RESULT_nir.", rt, getBlockPosition());
			}
			return io.getImage();
		} else
			return null;
	}
}
