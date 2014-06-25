package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculates the convex hull for the visible light and fluorescence images and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the mask). If enabled from the settings also the NIR and IR masks are processed.
 * 
 * @author klukas
 */
public class BlCalcConvexHull extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return mask;
		if (getBoolean("process " + mask.getCameraType().name() + " mask", mask.getCameraType() == CameraType.VIS || mask.getCameraType() == CameraType.FLUO))
			return processImage(mask.getCameraType().name().toLowerCase() + ".", mask);
		else
			return mask;
	}
	
	private Image processImage(String prefix, Image image) {
		boolean debug = getBoolean("debug", false);
		
		ResultsTableWithUnits numericResults;
		Double distHorizontal = optionsAndResults.getCalculatedBlueMarkerDistance();
		Double realDist = optionsAndResults.getREAL_MARKER_DISTANCE();
		if (distHorizontal == null)
			realDist = null;
		boolean drawHull = getBoolean("draw_convex_hull", true);
		boolean drawPCLine = getBoolean("draw_caliper_length", true);
		boolean drawMinRect = getBoolean("draw_retangle", true);
		boolean drawCircle = getBoolean("draw_circle", true);
		
		ImageOperation res = new ImageOperation(image).show(prefix + " input image", debug).hull()
				.find(getResultSet(), true, false,
						drawHull, drawPCLine, drawHull, drawMinRect, drawCircle,
						Color.RED.getRGB(),
						Color.CYAN.getRGB(),
						Color.RED.getRGB(),
						Color.YELLOW.darker().getRGB(),
						Color.PINK.getRGB(),
						distHorizontal, realDist);
		
		numericResults = res.getResultsTable();
		if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE && numericResults != null)
			getResultSet().storeResults(
					"RESULT_side." + prefix, numericResults,
					getBlockPosition());
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getResultSet().storeResults(
					"RESULT_top." + prefix, numericResults, getBlockPosition());
		
		res.getImage().show("output image", debug);
		
		return res.getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Calculate Convex Hull";
	}
	
	@Override
	public String getDescription() {
		return "Calculates the convex hull for the visible light and fluorescence images and stores according data " +
				"results as numeric values (size of hull, centroid). The complex hull, the image borders and the " +
				"centroid are drawn on the result (input and result is the mask). If enabled from the settings " +
				"also the NIR and IR masks are processed.";
	}
}
