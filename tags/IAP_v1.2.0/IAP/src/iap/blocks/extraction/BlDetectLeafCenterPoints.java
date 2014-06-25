package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
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

public class BlDetectLeafCenterPoints extends AbstractBlock {
	
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
		boolean markResults = false;
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
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					"RESULT_" + getBlockPosition() + "." + img.getCameraType() + ".leaf.count", pointList.size(), "leaves|CENTERPOINTS");
			
			// save x and y position
			for (Feature p : pointList) {
				getResultSet().setNumericResult(getBlockPosition(),
						"RESULT_" + getBlockPosition() + "." + img.getCameraType() + ".leaf.x", (int) p.getPosition().getX(), "leaves|CENTERPOINTS");
				
				getResultSet().setNumericResult(getBlockPosition(),
						"RESULT_" + getBlockPosition() + "." + img.getCameraType() + ".leaf.x", (int) p.getPosition().getY(), "leaves|CENTERPOINTS");
			}
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "name" + "_" + img.getCameraType(), pointList);
		}
		
		return img;
	}
	
	private LinkedList<Feature> detectCenterPoints(Image img) {
		FloatProcessor edmfp = img.io().bm().edmFloat();
		
		if (debugValues)
			new Image(edmfp.getBufferedImage()).show("distmap");
		
		MaximumFinder mf = new MaximumFinder();
		int maxTolerance = getInt("Maximum Tolerance", 5);
		ByteProcessor bp = mf.findMaxima(edmfp, maxTolerance, 1, mf.LIST, true, true);
		
		if (debugValues)
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
	
}
