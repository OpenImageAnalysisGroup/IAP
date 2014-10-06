package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.MaximumFinder;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlDetectLeafCenterPoints extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processMask(Image mask) {
		Image res;
		if (mask == null)
			res = null;
		else {
			res = mask;
			// only top images
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				LinkedList<Feature> pointList = detectCenterPoints(res);
				res = saveAndMarkResults(res, pointList);
			}
		}
		return res;
	}
	
	private Image saveAndMarkResults(Image img, LinkedList<Feature> pointList) {
		boolean markResults = getBoolean("Mark Center Points in Result Image", false);
		boolean saveResults = true;
		boolean saveResultObject = true;
		
		if (markResults) {
			ImageCanvas ic = new ImageCanvas(img);
			for (Feature p : pointList) {
				ic.drawRectangle((int) p.getPosition().getX() - 10, (int) p.getPosition().getY() - 10, 21, 21, Color.RED, 3);
			}
			img = ic.getImage();
		}
		
		if (saveResults) {
			String pos = optionsAndResults.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					pos + img.getCameraType() + ".leaf.count", pointList.size(), "leaves|CENTERPOINTS", this);
			
			// save x and y position
			int num = 0;
			for (Feature p : pointList) {
				getResultSet().setNumericResult(getBlockPosition(),
						pos + img.getCameraType() + ".leaf.x." + num, (int) p.getPosition().getX(), "leaves|CENTERPOINTS", this);
				
				getResultSet().setNumericResult(getBlockPosition(),
						pos + img.getCameraType() + ".leaf.y." + num, (int) p.getPosition().getY(), "leaves|CENTERPOINTS", this);
				num++;
			}
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist" + "_" + img.getCameraType(), pointList);
		}
		
		return img;
	}
	
	private LinkedList<Feature> detectCenterPoints(Image img) {
		img = img.io().bm().dilate(getInt("Mask Size for Dilate", 5)).getImage();
		FloatProcessor edmfp = img.io().bm().edmFloat();
		
		if (debugValues) {
			img.show("input");
			new Image(edmfp.getBufferedImage()).show("distmap");
		}
		
		MaximumFinder mf = new MaximumFinder();
		int maxTolerance = getInt("Maximum Tolerance", 5);
		ByteProcessor bp = mf.findMaxima(edmfp, maxTolerance, 1, mf.LIST, true, true);
		
		if (debugValues && bp != null)
			new Image(bp.getBufferedImage()).show("Maximas");
		
		ResultsTable rt = mf.getRt();
		
		if (debugValues)
			rt.show("results");
		
		LinkedList<Feature> centerPoints = new LinkedList<Feature>();
		
		for (int i = 0; i < rt.getCounter(); i++) {
			int x = (int) rt.getValue("X", i);
			int y = (int) rt.getValue("Y", i);
			centerPoints.add(new Feature(x, y));
		}
		
		return centerPoints;
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
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Detect Leaf Center Points";
	}
	
	@Override
	public String getDescription() {
		return "Detects leaf center points from top view (for arabidopsis, tobacco)";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("leaf.count", "!todo"),
				new CalculatedProperty("leaf.x", "!todo"),
				new CalculatedProperty("leaf.y", "!todo"),
		};
	}
	
}
