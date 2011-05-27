package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Calculates the convex hull for the fluorescence image and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the fluo mask).
 * 
 * @author klukas
 */
public class BlockConvexHullOnFLuo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() throws InterruptedException {
		
		if (getInput().getMasks().getFluo() == null) {
			System.err.println("ERROR: BlockConvexHullOnFLuo: Input Fluo Mask is NULL!");
			return null;
		}
		ImageOperation res = new ImageOperation(getInput().getMasks().getFluo()).hull().find(true, false, true, true, Color.RED.getRGB(), Color.BLUE.getRGB(),
				Color.ORANGE.getRGB());
		
		ResultsTable numericResults = res.getResultsTable();
		
		if (options.getCameraTyp() == CameraTyp.SIDE)
			getProperties().storeResults("RESULT_side.", numericResults, getBlockPosition());
		if (options.getCameraTyp() == CameraTyp.TOP)
			getProperties().storeResults("RESULT_top.", numericResults, getBlockPosition());
		
		return res.getImage();
	}
	
}
