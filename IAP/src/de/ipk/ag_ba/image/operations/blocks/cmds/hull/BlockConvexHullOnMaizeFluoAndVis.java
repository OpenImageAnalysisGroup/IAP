package de.ipk.ag_ba.image.operations.blocks.cmds.hull;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the convex hull for the fluorescence image and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the fluo mask).
 * 
 * @author klukas
 */
public class BlockConvexHullOnMaizeFluoAndVis extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage image = getInput().getMasks().getVis();
		if (options.isMaize()) {
			ImageData info = getInput().getImages().getVisInfo();
			ImageOperation res = processImage(image, info);
			return res != null ? res.getImage() : null;
		} else
			return image;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage image = getInput().getMasks().getFluo();
		if (options.isMaize()) {
			ImageData info = getInput().getImages().getFluoInfo();
			ImageOperation res = processImage(image, info);
			return res != null ? res.getImage() : null;
		} else
			return image;
	}
	
	private ImageOperation processImage(FlexibleImage image, ImageData info) {
		ResultsTable numericResults;
		ImageOperation res;
		if (image == null) {
			return null;
		}
		BlockProperty distHorizontal = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
		if (distHorizontal != null || options.getCameraPosition() == CameraPosition.TOP) {
			int realDist = options.getIntSetting(Setting.REAL_MARKER_DISTANCE);
			boolean drawHull = options.getBooleanSetting(Setting.DRAW_CONVEX_HULL);
			res = new ImageOperation(image).hull().find(true, false, drawHull, drawHull, Color.RED.getRGB(),
					Color.BLUE.getRGB(),
					Color.ORANGE.getRGB(), distHorizontal, realDist);
			
			numericResults = res.getResultsTable();
		} else {
			numericResults = null;
			res = new ImageOperation(image);
		}
		if (options.getCameraPosition() == CameraPosition.SIDE && numericResults != null)
			getProperties().storeResults(
					"RESULT_side.", numericResults,
							getBlockPosition());
		if (options.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getProperties().storeResults(
					"RESULT_top.", numericResults, getBlockPosition());
		return res;
	}
	
}
