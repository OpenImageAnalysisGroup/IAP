package de.ipk.ag_ba.image.analysis.maize;

import ij.measure.ResultsTable;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockBarleyResults extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage input = getInput().getMasks().getVis();
		ResultsTable rt = new ResultsTable();
		int filledPixels = new ImageOperation(input).countFilledPixels();
		
		rt.incrementCounter();
		rt.addValue("filledPixels", filledPixels);
		
		if (options.getCameraPosition() == CameraPosition.SIDE && rt != null)
			getProperties().storeResults(
					"RESULT_side.", rt,
							getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && rt != null)
			getProperties().storeResults(
					"RESULT_top.", rt, getBlockPosition());
		
		return input;
	}
	
}
