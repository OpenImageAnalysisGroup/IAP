package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the convex hull for the visible light and fluorescence images and stores according data results as numeric
 * values (size of hull, centroid). The complex hull, the image borders and the centroid are drawn
 * on the result (input and result is the mask). If enabled from the settings also the NIR and IR masks are processed.
 * 
 * @author klukas
 */
public class BlCalcConvexHull extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return mask;
		if (getBoolean("process " + mask.getCameraType().name() + " mask", mask.getCameraType() == CameraType.VIS || mask.getCameraType() == CameraType.FLUO))
			return processImage(optionsAndResults.getCameraPosition(), mask.getCameraType(), mask, input().images().getImageInfo(mask.getCameraType()));
		else
			return mask;
	}
	
	private Image processImage(CameraPosition cp, CameraType ct, Image image, ImageData imageRef) {
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
		
		ImageOperation res = new ImageOperation(image).show(ct + " input image", debug).hull()
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
					cp, ct, TraitCategory.GEOMETRY, numericResults,
					getBlockPosition(), this, imageRef);
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP && numericResults != null)
			getResultSet().storeResults(
					cp, ct, TraitCategory.GEOMETRY, numericResults, getBlockPosition(), this, imageRef);
		
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
	public boolean isChangingImages() {
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
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("hull.points", "Number of edge points of the convex hull around the plant."),
				new CalculatedProperty("hull.length", "Length of the convex hull."),
				new CalculatedProperty("hull.length.norm", "Length of the convex hull, normalized to real-world coordinates."),
				new CalculatedProperty("compactness.01", "4 * Math.PI / (borderPixels * borderPixels / filledArea)"),
				new CalculatedProperty("compactness.16", "borderPixels * borderPixels / filledArea"),
				new CalculatedProperty("hull.compactness.01", "4 * Math.PI / (borderPixels * borderPixels / filledArea) (all of convex hull)"),
				new CalculatedProperty("hull.compactness.16", "borderPixels * borderPixels / filledArea (all of convex hull)"),
				new CalculatedProperty("hull.area", "Area (in pixels) of the convex hull, which is the shortest convex line drawing around the plant."),
				new CalculatedProperty("hull.area.norm",
						"Normalized area (in real-world coordinates) of the convex hull, which is the shortest convex line drawing around the plant."),
				new CalculatedProperty("border.length",
						"Number of pixels of the plant, connected by at least one side of the pixel to the background (4-neighbourhood)."),
				new CalculatedProperty("border.length.norm",
						"Plant outline length, normalized to real-world coordinates."),
				new CalculatedProperty("hull.circularity",
						"Indicates similarity of the convex hull to a circle, ranges between 0 and 1. A circular object has value 1."),
				new CalculatedProperty("circumcircle.d", "Diameter of the smallest circle drawn around the plant."),
				new CalculatedProperty("circumcircle.d.norm", "Diameter of the smallest circle drawn around the plant, normalized to real-world coordinates."),
				new CalculatedProperty("minrectangle.area", "Area of the smallest enclosing rectangle, which fits around the plant."),
				new CalculatedProperty("minrectangle.area.norm",
						"Area of the smallest enclosing rectangle, which fits around the plant, normalized to real-world coordinates."),
				new CalculatedProperty("minrectangle.length.a", "Length of one side of the smallest enclosing rectangle, which fits around the plant."),
				new CalculatedProperty("minrectangle.length.a.norm",
						"Length of one side of the smallest enclosing rectangle, which fits around the plant, normalized to real-world coordinates."),
				new CalculatedProperty("minrectangle.length.b",
						"Length of the second side of the smallest enclosing rectangle, which fits around the plant."),
				new CalculatedProperty("minrectangle.length.b.norm",
						"Length of the second side of the smallest enclosing rectangle, which fits around the plant, normalized to real-world coordinates."),
				new CalculatedProperty("hull.fillgrade",
						"Number of pixels of the plant relative to the area of the convex hull. May be formatted as percentage values in Excel (e.g. 20%), "
								+ "CSV exported data is displayed unformatted, e.g. 0.2."),
				new CalculatedProperty("hull.pc1", "Largest distance (in pixels) of any two pixels of the plant."),
				new CalculatedProperty("hull.pc1.norm", "Largest distance of any two pixels of the plant, normalized to real-world coordinates."),
				new CalculatedProperty("hull.pc2",
						"If a line connects the two most far from each other situated plant pixels is drawn, this number indicates the sum of "
								+ "the maximum distances of other plant pixels from the left and right of this line."),
				new CalculatedProperty("hull.pc2.norm",
						"If a line connects the two most far from each other situated plant pixels is drawn, this number indicates the sum of "
								+ "the maximum distances of other plant pixels from the left and right of this line. This value is normalized "
								+ "to real-world coordniates."),
				new CalculatedProperty("hull.centroid.x", "Center of mass coordinate (X-axis) of the convex hull area."),
				new CalculatedProperty("hull.centroid.y", "Center of mass coordinate (Y-axis) of the convex hull area."),
		};
	}
}
