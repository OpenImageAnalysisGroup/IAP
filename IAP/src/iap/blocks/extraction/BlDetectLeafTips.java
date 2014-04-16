package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.imageAnalysisTools.leafClustering.BorderAnalysis;
import iap.blocks.imageAnalysisTools.leafClustering.BorderFeature;
import iap.blocks.imageAnalysisTools.leafClustering.FeatureObject;
import iap.blocks.imageAnalysisTools.leafClustering.FeatureObject.FeatureObjectType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.StringManipulationTools;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.ImageConvolution;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author pape, klukas
 */
public class BlDetectLeafTips extends AbstractSnapshotAnalysisBlock {
	
	boolean ignore = false;
	boolean debug_borderDetection;
	double borderSize;
	private boolean isBestAngle;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.isBestAngle = isBestAngle();
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
		
		// calculation for side
		if (!getBoolean("Process Side", true) && optionsAndResults.getCameraPosition() == CameraPosition.SIDE)
			ignore = true;
		
		// calculation for top
		if (!getBoolean("Process Top", false) && optionsAndResults.getCameraPosition() == CameraPosition.TOP)
			ignore = true;
		
		debug_borderDetection = getBoolean("Debug Border Detection", false);
	}
	
	@Override
	public boolean isChangingImages() {
		return true; // post-processor highlights leaf-tips
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		if (getBoolean("Calculate on Visible Image", true) && !ignore) {
			Image workimg = input().masks().vis().copy();
			
			int searchRadius = getInt("Search-radius (Vis)", 50);
			double fillGradeInPercent = getDouble("Fillgrade (Vis)", 0.3);
			
			int i1 = (int) (optionsAndResults.getUnitTestIdx() / 6);
			int i2 = (int) (optionsAndResults.getUnitTestIdx() % 6);
			
			searchRadius = searchRadius + (i1 - 2) * 10;
			fillGradeInPercent = fillGradeInPercent + (i2 - 2) * 0.05;
			
			borderSize = searchRadius / 2;
			workimg.setCameraType(input().masks().vis().getCameraType());
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Vis)", 0), getInt("Masksize Erode (Vis)", 2),
					getInt("Masksize Dilate (Vis)", 5));
			Roi bb = workimg.io().getBoundingBox();
			int maxValidY = (int) (bb.getBounds().y + bb.getBounds().height - getInt("Minimum Leaf Height Percent", -1) / 100d * bb.getBounds().height);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.VIS, optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().vis();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		if (getBoolean("Calculate on Fluorescence Image", false) && !ignore) {
			Image workimg = input().masks().fluo().copy();
			int searchRadius = getInt("Search-radius (Fluo)", 40);
			double fillGradeInPercent = getDouble("Fillgrade (Fluo)", 0.3);
			borderSize = searchRadius / 2;
			workimg.setCameraType(CameraType.FLUO);
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Fluo)", 0), getInt("Masksize Erode (Fluo)", 2),
					getInt("Masksize Dilate (Fluo)", 5));
			Roi bb = workimg.io().getBoundingBox();
			int maxValidY = (int) (bb.getBounds().y + bb.getBounds().height - getInt("Minimum Leaf Height Percent", -1) / 100d * bb.getBounds().height);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.FLUO, optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().fluo();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		if (getBoolean("Calculate on Near-infrared Image", false) && !ignore) {
			Image workimg = input().masks().nir().copy();
			int searchRadius = getInt("Search-radius (Nir)", 15);
			double fillGradeInPercent = getDouble("Fillgrade (Nir)", 0.35);
			borderSize = searchRadius / 2;
			workimg.setCameraType(CameraType.NIR);
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Nir)", 0), getInt("Masksize Erode (Nir)", 2),
					getInt("Masksize Dilate (Nir)", 5));
			Roi bb = workimg.io().getBoundingBox();
			int maxValidY = (int) (bb.getBounds().y + bb.getBounds().height - getInt("Minimum Leaf Height Percent", -1) / 100d * bb.getBounds().height);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.NIR, optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().nir();
	}
	
	private void savePeaksAndFeatures(LinkedList<BorderFeature> peakList, CameraType cameraType, CameraPosition cameraPosition, int searchRadius, int maxValidY) {
		boolean saveListObject = true;
		boolean saveFeaturesInResultSet = false;
		
		if (saveListObject) {
			ArrayList<BorderFeature> toRemove = new ArrayList<BorderFeature>();
			// remove bordersize from all positions
			for (BorderFeature bf : peakList) {
				HashMap<String, FeatureObject> fm = bf.getFeatureMap();
				if (bf.getPosition().getY() > maxValidY)
					toRemove.add(bf);
				for (FeatureObject fo : fm.values()) {
					if (fo.featureObjectType == FeatureObjectType.POSITION) {
						fo.feature = (int) ((Integer) (fo.feature) - borderSize);
					}
					if (fo.featureObjectType == FeatureObjectType.VECTOR) {
						fo.feature = ((Vector2D) fo.feature).add(new Vector2D(-borderSize, -borderSize));
					}
				}
			}
			peakList.removeAll(toRemove);
			getResultSet().setObjectResult(getBlockPosition(), "leaftiplist_" + cameraType, peakList);
		}
		
		int count = 0;
		for (BorderFeature bf : peakList) {
			count++;
		}
		
		// save leaf count
		getResultSet().setNumericResult(getBlockPosition(),
				"RESULT_" + cameraPosition + "." + cameraType + ".leaftip.count", count, "leaftips|SUSAN");
		
		// save leaf count for best angle
		if (isBestAngle)
			getResultSet().setNumericResult(getBlockPosition(),
					"RESULT_" + cameraPosition + "." + cameraType + ".leaftip.count.best_angle", count, "leaftips|SUSAN");
		
		if (saveFeaturesInResultSet) {
			int index = 1;
			for (BorderFeature bf : peakList) {
				Vector2D pos = bf.getPosition();
				final Double angle = (Double) bf.getFeature("angle");
				Vector2D direction = (Vector2D) bf.getFeature("direction");
				final CameraType cameraType_fin = cameraType;
				
				if (pos == null || cameraPosition == null || cameraType == null) {
					continue;
				}
				
				// correct positions
				Vector2D sub = new Vector2D(-borderSize, -borderSize);
				final Vector2D pos_fin = pos.add(sub);
				final Vector2D direction_fin = direction.add(sub);
				
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index) + ".x",
						pos_fin.getX(), "px");
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index) + ".y",
						pos_fin.getY(), "px");
				
				if (angle != null)
					getResultSet()
							.setNumericResult(
									0,
									"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index)
											+ ".angle",
									angle, "degree");
				index++;
				
				if (searchRadius > 0) {
					final int searchRadius_fin = searchRadius;
					getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
						
						@Override
						public Image postProcessMask(Image mask) {
							return mask
									.io()
									.canvas()
									.drawCircle((int) pos_fin.getX(), (int) pos_fin.getY(), searchRadius_fin, Color.RED.getRGB(), 0.5, 3)
									.drawLine((int) pos_fin.getX(), (int) pos_fin.getY(), (int) direction_fin.getX(), (int) direction_fin.getY(), Color.BLUE.getRGB(),
											0.8,
											1)
									.text((int) direction_fin.getX() + 10, (int) direction_fin.getY(),
											"x: " + ((int) pos_fin.getX() + borderSize) + " y: " + ((int) pos_fin.getY() + borderSize),
											Color.BLACK)
									.text((int) direction_fin.getX() + 10, (int) direction_fin.getY() + 15, "angle: " + angle.intValue(), Color.BLACK)
									.getImage();
						}
						
						@Override
						public Image postProcessImage(Image image) {
							return image;
						}
						
						@Override
						public CameraType getConfig() {
							return cameraType_fin;
						}
					});
				}
			}
		}
	}
	
	private LinkedList<BorderFeature> getPeaksFromBorder(Image img, int searchRadius, double fillGradeInPercent) {
		BorderAnalysis ba = null;
		LinkedList<BorderFeature> res = null;
		
		ba = new BorderAnalysis(img);
		int geometricThresh = (int) (fillGradeInPercent * (Math.PI * searchRadius * searchRadius));
		ba.setCheckSplit(true);
		ba.calcSUSAN(searchRadius, geometricThresh);
		ba.getPeaksFromBorder(1, searchRadius * 2, "susan");
		ba.approxDirection(searchRadius * 2);
		
		if (debug_borderDetection)
			ba.plot(0, searchRadius);
		
		res = ba.getPeakList();
		
		return res;
	}
	
	private Image preprocessImage(Image img, int searchRadius, int blurSize, int erode, int dilate) {
		int background = ImageOperation.BACKGROUND_COLORint;
		CameraType ct = img.getCameraType();
		
		// get skeleton-image and workimage to connect lose leaves and for optimization
		Image skel = getResultSet().getImage("skeleton_" + ct.toString());
		Image skel_workimge = getResultSet().getImage("skeleton_workimage_" + ct.toString());
		if (skel != null && skel_workimge != null) {
			img = img.io().or(skel.copy().io().bm().dilate(15).getImage()).or(skel_workimge).getImage()
					.show("skel images on mask" + ct.toString(), debugValues);
		} else {
			if (ct != CameraType.NIR)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No " + ct.toString()
						+ " skeleton image available, can't process it within leaf-tip detection!");
		}
		
		// morphological operations
		img = img.io().bm().dilate(dilate).erode(erode).getImage().show("Erode and Dilate on " + ct.toString(), debugValues);
		
		// blur
		img = img.io().blurImageJ(blurSize).getImage().show("Blured Image " + ct.toString(), debugValues);
		
		// enlarge 1 px lines TODO this works, but the border tracking returns errors even if 1 px lines are enlarged.
		ImageConvolution ic = new ImageConvolution(img);
		// img.show("before");
		img = ic.enlargeLines().getImage().show("Enlarged Lines " + ct.toString(), debugValues);
		// img.show("after");
		
		// add border around image
		img = img.io().addBorder((int) borderSize, 0, 0, background).getImage();
		
		return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
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
		return "Detect Leaf-Tips";
	}
	
	@Override
	public String getDescription() {
		return "Detect leaf-tips of a plant. (e.g. could be used for calculation of leaf number)";
	}
}