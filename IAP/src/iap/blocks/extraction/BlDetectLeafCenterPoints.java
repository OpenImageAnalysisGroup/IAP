package iap.blocks.extraction;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.GapList;
import org.SystemAnalysis;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.image_analysis_tools.cvppp_2014.LeafCountCvppp;
import iap.blocks.image_analysis_tools.cvppp_2014.LeafSegmentationCvppp;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.MaximumFinder;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.methods.RegionLabeling;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

public class BlDetectLeafCenterPoints extends AbstractBlock implements CalculatesProperties {
	
	@Override
	protected Image processMask(Image mask) {
		Image workimg;
		Image res = null;
		if (mask == null)
			workimg = null;
		else {
			workimg = mask.copy();
			boolean performLabeling = getBoolean("Perform Leaf Labeling", false);
			int maxTolerance = getInt("Maximum Tolerance", 5);
			// only top images
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
			if (performLabeling) {
				// start CVPPP 2014 challenge code for leaf labeling
				res = performLeafLabeling(workimg, res, maxTolerance, input().images().getImageInfo(mask.getCameraType()));
			} else {
				GapList<Feature> pointList = detectCenterPoints(workimg, maxTolerance);
				res = saveAndMarkResults(workimg, pointList, input().images().getImageInfo(mask.getCameraType()));
			}
			}
		}
		return res;
	}
	
	private Image performLeafLabeling(Image workimg, Image res, int maxTolerance, ImageData imageRef) {
		boolean saveLeafFeatures = getBoolean("Save Center Points Corrdinates and Leaf Features", false);
		// leaf-count
		Image[] segmentedImages = new Image[] { workimg };
		Image[] segmentedAndNotSplitImages = new Image[] { workimg };
		LeafCountCvppp lc = new LeafCountCvppp(segmentedImages);
		try {
			lc.detectLeaves(maxTolerance);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
		segmentedImages = lc.getResultImages();
		segmentedImages[0].show("Split Image", debugValues);
		HashMap<String, ArrayList<Feature>> leafCenterPoints = lc.getLeafCenterPoints();
		
		// leaf segmentation
		LeafSegmentationCvppp ls = new LeafSegmentationCvppp(leafCenterPoints, segmentedImages, segmentedAndNotSplitImages);
		try {
			ls.segmentLeaves();
			res = ls.getResultImage();
			res = res.io().replaceColor(-16777216, ImageOperation.BACKGROUND_COLORint).show("Labeled Leaf Image", debugValues).getImage();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (saveLeafFeatures) {
			// res.show("res");
			ClusterDetection cd = new ClusterDetection(res, -1);
			cd.detectClusters();
			Vector2i[] centers = cd.getClusterCenterPoints();
			int[] sizes = cd.getClusterSize();
			int cdcount = cd.getClusterCount();
			
			RegionLabeling rl = new RegionLabeling(res, false, ImageOperation.BACKGROUND_COLORint, ImageOperation.BACKGROUND_COLORint);
			rl.detectClusters();
			int rlcount = rl.getClusterCount();
			
			CameraPosition pos = optionsAndResults.getCameraPosition();
			
			for (int num = 1; num <= cd.getClusterCount(); num++) {
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, workimg.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "leaf.centerpoint." + num + ".position.x"), centers[num].x,
					"leaves", this, imageRef);
					
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, workimg.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "leaf.centerpoint." + num + ".position.y"), centers[num].y,
					"leaves", this, imageRef);
					
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(pos, workimg.getCameraType(), TraitCategory.ORGAN_GEOMETRY, "leaf.area." + num), sizes[num],
					"leaves", this, imageRef);
			}
		}
		return res;
	}
	
	private Image saveAndMarkResults(Image img, GapList<Feature> pointList, ImageData imageRef) {
		boolean markResults = getBoolean("Mark Center Points in Result Image", false);
		boolean saveCPCoordinates = getBoolean("Save Center Points Corrdinates", false);
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
				new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "centerpoint.count"), pointList.size(), "leaves", this, imageRef);
				
			// save x and y position
			if (saveCPCoordinates) {
			int num = 0;
			for (Feature p : pointList) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "centerpoint." + num + ".position.x"), (int) p.getPosition().getX(),
						"leaves", this, imageRef);
						
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(pos, img.getCameraType(), TraitCategory.GEOMETRY, "centerpoint." + num + ".position.y"), (int) p.getPosition().getY(),
						"leaves", this, imageRef);
				num++;
			}
			}
		}
		
		if (saveResultObject) {
			String name = this.getClass().getSimpleName();
			name = name.toLowerCase();
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist" + "_" + img.getCameraType(), pointList);
		}
		
		return img;
	}
	
	private GapList<Feature> detectCenterPoints(Image img, int maxTolerance) {
		img = img.io().bm().dilate(getInt("Mask Size for Dilate", 5)).getImage();
		FloatProcessor edmfp = img.io().bm().edmFloat();
		
		if (debugValues) {
			img.show("input");
			new Image(edmfp.getBufferedImage()).show("distmap");
		}
		
		MaximumFinder mf = new MaximumFinder();
		
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
		return "Detects leaf center points from top view (for arabidopsis, tobacco). Also the method for leaf labeling of rosette plants which can be found in 'Pape, J.M., Klukas, C.: 3-D histogram-based segmentation and leaf detection for rosette plants. In: Computer Vision - ECCV 2014 Workshops, vol. 8928, pp. 61-74 (2015)' is included and can be performed by enabeling the option 'Perform Leaf Labeling'.";
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
