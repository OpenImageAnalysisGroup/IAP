package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
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
		ResultsTable numericResults;
		ImageOperation res;
		
		if (getInput().getMasks().getFluo() == null) {
			System.err.println("ERROR: BlockConvexHullOnFLuo: Input Fluo Mask is NULL!");
			return null;
		}
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		if (distHorizontal != null || options.getCameraPosition() == CameraPosition.TOP) {
			res = new ImageOperation(getInput().getMasks().getFluo()).hull().find(true, false, false, false, Color.RED.getRGB(),
					Color.BLUE.getRGB(),
					Color.ORANGE.getRGB(), distHorizontal);
			
			numericResults = res.getResultsTable();
		} else {
			numericResults = null;
			res = new ImageOperation(getInput().getMasks().getFluo());
		}
		if (options.getCameraPosition() == CameraPosition.SIDE && numericResults != null)
			getProperties().storeResults(
					"RESULT_side.deg" + (getInput().getImages().getFluoInfo() != null && getInput().getImages().getFluoInfo().getPosition() != null ? getInput()
											.getImages().getFluoInfo().getPosition().intValue() : "0")
									+ ".", numericResults,
							getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getProperties().storeResults(
					"RESULT_top" + (getInput().getImages().getFluoInfo() != null && getInput().getImages().getFluoInfo().getPosition() != null ? getInput()
							.getImages().getFluoInfo().getPosition().intValue() : "0")
							+ ".", numericResults, getBlockPosition());
		
		return res.getImage();
	}
	
}
