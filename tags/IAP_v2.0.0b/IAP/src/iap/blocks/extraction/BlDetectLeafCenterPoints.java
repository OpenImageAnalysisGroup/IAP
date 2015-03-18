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

import org.GapList;
import org.SystemAnalysis;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

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
				GapList<Feature> pointList = detectCenterPoints(res);
				res = saveAndMarkResults(res, pointList, input().images().getImageInfo(mask.getCameraType()));
			}
		}
		return res;
	}
	
	private Image saveAndMarkResults(Image img, GapList<Feature> pointList, ImageData imageRef) {
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
			CameraPosition pos = optionsAndResults.getCameraPosition();
			// save leaf count
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "leaf.count"), pointList.size(), "leaves", this, imageRef);
			
			// save x and y position
			int num = 0;
			for (Feature p : pointList) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "leaf." + num + ".position.x"), (int) p.getPosition().getX(),
						"leaves", this, imageRef);
				
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "leaf." + num + ".position.y"), (int) p.getPosition().getY(),
						"leaves", this, imageRef);
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
	
	private GapList<Feature> detectCenterPoints(Image img) {
		img = img.io().bm().dilate(getInt("Mask Size for Dilate", 5)).getImage();
		FloatProcessor edmfp = img.io().bm().edmFloat();
		
		if (debugValues) {
			img.show("input");
			new Image(edmfp.getBufferedImage()).show("distmap");
		}
		
		MaximumFinder mf = new MaximumFinder();
		int maxTolerance = getInt("Maximum Tolerance", 5);
		
		GapList<Feature> centerPoints = new GapList<Feature>();
		try {
			ByteProcessor bp = mf.findMaxima(edmfp, maxTolerance, 1, mf.LIST, true, true);
			
			if (debugValues && bp != null)
				new Image(bp.getBufferedImage()).show("Maximas");
			
			ResultsTable rt = mf.getRt();
			
			if (debugValues)
				rt.show("results");
			
			for (int i = 0; i < rt.getCounter(); i++) {
				int x = (int) rt.getValue("X", i);
				int y = (int) rt.getValue("Y", i);
				centerPoints.add(new Feature(x, y));
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Could not calculate leaf center points for image (forground pixels: "
					+ img.io().countFilledPixels() + ")!");
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
				new CalculatedProperty("leaf.count", "Number of detected leaves."),
				new CalculatedProperty("leaf.*.position.x", "Center position (X-axis) of a leaf."),
				new CalculatedProperty("leaf.*.position.y", "Center position (Y-axis) of a leaf."),
		};
	}
	
}
