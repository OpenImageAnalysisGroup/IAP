package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Calculates the convex hull for the fluroescence image and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the fluo mask).
 * 
 * @author klukas
 */
public class BlockConvexHullOnFLuo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() throws InterruptedException {
		
		ImageOperation res = new ImageOperation(getInput().getMasks().getFluo()).hull().find(false, true, true, true, Color.RED.getRGB(), Color.BLUE.getRGB(),
				Color.ORANGE.getRGB());
		
		ResultsTable numericResults = res.getResultsTable();
		
		getProperties().storeResults("RESULT_", numericResults, getBlockPosition());
		
		return res.getImage();
	}
	
}
