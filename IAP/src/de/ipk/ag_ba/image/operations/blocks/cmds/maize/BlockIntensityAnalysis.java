package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockIntensityAnalysis extends AbstractSnapshotAnalysisBlockFIS {
	
	private int plantArea;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.plantArea = getInput().getImages().getVis().getIO().countFilledPixels();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
		ResultsTable rt = io.intensity(4).calcualteHistorgram(plantArea);
		getProperties().storeResults("RESULT_fluo.", rt, getBlockPosition());
		return io.getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		ImageOperation io = new ImageOperation(getInput().getMasks().getNir());
		ResultsTable rt = io.intensity(4).calcualteHistorgram(plantArea);
		getProperties().storeResults("RESULT_nir.", rt, getBlockPosition());
		return io.getImage();
	}
}
