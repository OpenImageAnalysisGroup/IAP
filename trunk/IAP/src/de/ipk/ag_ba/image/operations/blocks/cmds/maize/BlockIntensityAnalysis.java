package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockIntensityAnalysis extends AbstractSnapshotAnalysisBlockFIS {
	
	private int plantAreaVis, plantImagePixelCountVis, plantAreaNir, plantImagePixelCountNir;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.plantAreaVis = getInput().getImages().getVis().getIO().countFilledPixels();
		this.plantImagePixelCountVis = getInput().getImages().getVis().getWidth()*getInput().getImages().getVis().getHeight();
		
		this.plantAreaNir = getInput().getImages().getNir().getIO().countFilledPixels();
		this.plantImagePixelCountNir = getInput().getImages().getNir().getWidth()*getInput().getImages().getNir().getHeight();
	}
	
	/**
	 * ndiv [-1-1]
	 */
	@Override
	protected FlexibleImage processVISmask() {
		ImageOperation io = new ImageOperation(getInput().getMasks().getVis());
		int pixelsum = io.convert2Grayscale().countFilledPixels();
		
		double averageVis = pixelsum/(double)plantImagePixelCountVis;
		double averageNir = plantAreaNir/(double)plantImagePixelCountNir;
		
		double ndvi = (averageNir - averageVis) / (averageNir + averageVis);
		ResultsTable rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("ndvi", ndvi);
		getProperties().storeResults("RESULT_", rt, getBlockPosition());
			
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
		ResultsTable rt = io.intensity(4).calcualteHistorgram(plantAreaVis, plantImagePixelCountVis);
		getProperties().storeResults("RESULT_fluo.", rt, getBlockPosition());
		return io.getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		ImageOperation io = new ImageOperation(getInput().getMasks().getNir());
		ResultsTable rt = io.intensity(4).calcualteHistorgram(plantAreaVis, plantImagePixelCountVis);
		getProperties().storeResults("RESULT_nir.", rt, getBlockPosition());
		return io.getImage();
	}
}
