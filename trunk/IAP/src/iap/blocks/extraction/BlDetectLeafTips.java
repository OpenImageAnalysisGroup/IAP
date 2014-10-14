package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.image_analysis_tools.leafClustering.BorderAnalysis;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.StringManipulationTools;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.ImageConvolution;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */
public class BlDetectLeafTips extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	boolean ignore = false;
	boolean debug_borderDetection;
	double borderSize;
	
	@Override
	protected void prepare() {
		super.prepare();
		
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
		boolean isBestAngle = isBestAngle(CameraType.VIS);
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
		if (getBoolean("Calculate on Visible Image", true) && !ignore) {
			Image workimg = input().masks().vis().copy();
			
			int searchRadius = getInt("Search-radius (Vis)", 30);
			double fillGradeInPercent = getDouble("Fillgrade (Vis)", 0.3);
			int blurSize = getInt("Size for Bluring (Vis)", 0);
			int erodeSize = getInt("Masksize Erode (Vis)", 2);
			int dilateSize = getInt("Masksize Dilate (Vis)", 5);
			int minHeightPercent = getInt("Minimum Leaf Height Percent", -1);
			
			// test sr and fg
			// int i1 = (int) (optionsAndResults.getUnitTestIdx() / 6);
			// int i2 = (int) (optionsAndResults.getUnitTestIdx() % 6);
			//
			// searchRadius = searchRadius + i1 * 2;
			// fillGradeInPercent = fillGradeInPercent + (i2 - 2) * 0.025;
			
			// searchRadius = searchRadius + (i1 - 2) * 10;
			// fillGradeInPercent = fillGradeInPercent + (i2 - 2) * 0.05;
			
			borderSize = searchRadius / 2;
			workimg.setCameraType(input().masks().vis().getCameraType());
			workimg = preprocessImage(workimg, searchRadius, blurSize, erodeSize, dilateSize);
			Roi bb = workimg.io().getBoundingBox();
			int maxValidY = (int) (bb.getBounds().y + bb.getBounds().height - minHeightPercent / 100d * bb.getBounds().height);
			workimg.setCameraType(CameraType.VIS);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, input().masks().vis(), searchRadius, fillGradeInPercent), CameraType.VIS,
					optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().vis();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		boolean isBestAngle = isBestAngle(CameraType.FLUO);
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
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
			workimg.setCameraType(CameraType.FLUO);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, input().masks().fluo(), searchRadius, fillGradeInPercent), CameraType.FLUO,
					optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().fluo();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		boolean isBestAngle = isBestAngle(CameraType.NIR);
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
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
			savePeaksAndFeatures(getPeaksFromBorder(workimg, input().masks().nir(), searchRadius, fillGradeInPercent), CameraType.NIR,
					optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY);
		}
		return input().masks().nir();
	}
	
	private void savePeaksAndFeatures(LinkedList<Feature> peakList, CameraType cameraType, CameraPosition cameraPosition, int searchRadius, int maxValidY) {
		boolean saveListObject = true;
		boolean saveLeafCount = true;
		boolean saveFeaturesInResultSet = getBoolean("Save individual leaf features", true);
		boolean saveColorFeaturesInResultSet = getBoolean("Save individual leaf color features", true);
		
		if (saveListObject) {
			saveLeafTipList(peakList, cameraType, maxValidY);
		}
		
		if (saveLeafCount) {
			saveLeafCount(cameraType, cameraPosition, peakList.size());
		}
		
		boolean saveAddF = true;
		if (saveAddF) {
			int n = 0, nup = 0, ndown = 0;
			double angleSum = 0;
			DescriptiveStatistics statsLeafDirection = new DescriptiveStatistics();
			for (Feature bf : peakList) {
				final Double angle = (Double) bf.getFeature("angle");
				
				if (saveColorFeaturesInResultSet) {
					ArrayList<PositionAndColor> pixels = (ArrayList<PositionAndColor>) bf.getFeature("pixels");
					int[] regionArray = BorderAnalysis.copyRegiontoArray(pixels);
					Image leafTipImage = new Image(regionArray.length, 1, regionArray);
					double r_mean = leafTipImage.io().channels().getR().getImageAsImagePlus().getStatistics().mean;
					double g_mean = leafTipImage.io().channels().getG().getImageAsImagePlus().getStatistics().mean;
					double b_mean = leafTipImage.io().channels().getB().getImageAsImagePlus().getStatistics().mean;
					
					double h_mean = leafTipImage.io().channels().getH().getImageAsImagePlus().getStatistics().mean;
					double s_mean = leafTipImage.io().channels().getS().getImageAsImagePlus().getStatistics().mean;
					double v_mean = leafTipImage.io().channels().getV().getImageAsImagePlus().getStatistics().mean;
					
					double labL_mean = leafTipImage.io().channels().getLabL().getImageAsImagePlus().getStatistics().mean;
					double laba_mean = leafTipImage.io().channels().getLabA().getImageAsImagePlus().getStatistics().mean;
					double labb_mean = leafTipImage.io().channels().getLabB().getImageAsImagePlus().getStatistics().mean;
					
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.rgb.r.mean"), r_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.rgb.g.mean"), g_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.rgb.b.mean"), b_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.hsv.h.mean"), h_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.hsv.h.mean"), s_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.hsv.h.mean"), v_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.lab.l.mean"), labL_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.lab.a.mean"), laba_mean, null, this);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip.color.lab.b.mean"), labb_mean, null, this);
				}
				
				if (angle != null) {
					if (angle.doubleValue() > 90.0)
						nup++;
					else
						ndown++;
					n++;
					angleSum += angle.doubleValue();
					statsLeafDirection.addValue(angle.doubleValue());
				}
			}
			if (n > 0) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.up.count"), nup, "leaftips", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.down.count"), ndown, "leaftips", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.mean"), angleSum / n, "leaftips", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.stdev"), statsLeafDirection.getStandardDeviation(), "leaftips",
						this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.skewness"), statsLeafDirection.getSkewness(), "leaftips", this);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.kurtosis"), statsLeafDirection.getKurtosis(), "leaftips", this);
			}
		}
		
		if (saveFeaturesInResultSet) {
			int index = 1;
			for (Feature bf : peakList) {
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
				
				getResultSet().setNumericResult(
						0,
						new Trait(cameraPosition, cameraType_fin, TraitCategory.GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(index, 2)
								+ ".position.x"),
						pos_fin.getX(), "px", this);
				getResultSet().setNumericResult(
						0,
						new Trait(cameraPosition, cameraType_fin, TraitCategory.GEOMETRY, "leaftip." + StringManipulationTools.formatNumberAddZeroInFront(index, 2)
								+ ".position.y"),
						pos_fin.getY(), "px", this);
				
				if (angle != null)
					getResultSet()
							.setNumericResult(
									0,
									new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip."
											+ StringManipulationTools.formatNumberAddZeroInFront(index, 2)
											+ ".angle"),
									angle, "degree", this);
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
	
	private void saveLeafCount(CameraType cameraType, CameraPosition cameraPosition, int count) {
		// save leaf count
		getResultSet().setNumericResult(getBlockPosition(),
				new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.count"), count, "leaftips", this);
		
		boolean isBestAngle = isBestAngle(cameraType);
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
		// save leaf count for best angle
		if (isBestAngle)
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.count.best_angle"), count, "leaftips", this);
	}
	
	private void saveLeafTipList(LinkedList<Feature> peakList, CameraType cameraType, int maxValidY) {
		ArrayList<Feature> toRemove = new ArrayList<Feature>();
		// remove bordersize from all position-features
		for (Feature bf : peakList) {
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
	
	private LinkedList<Feature> getPeaksFromBorder(Image img, Image orig, int searchRadius, double fillGradeInPercent) {
		BorderAnalysis ba = null;
		LinkedList<Feature> res = null;
		
		ba = new BorderAnalysis(img, orig);
		int geometricThresh = (int) (fillGradeInPercent * (Math.PI * searchRadius * searchRadius));
		ba.setCheckSplit(true);
		ba.calcSUSAN(searchRadius, geometricThresh);
		ba.getPeaksFromBorder(1, (int) (searchRadius * 1.5), "susan");
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
			// if (ct != CameraType.NIR)
			// System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No " + ct.toString()
			// + " skeleton image available, can't process it within leaf-tip detection!");
		}
		
		// morphological operations
		img = img.io().bm().dilate(dilate).erode(erode).getImage().show("Erode and Dilate on " + ct.toString(), debugValues);
		
		// blur
		img = img.io().blurImageJ(blurSize).getImage().show("Blured Image " + ct.toString(), debugValues);
		
		// enlarge 1 px lines
		ImageConvolution ic = new ImageConvolution(img);
		img = ic.enlargeLines().getImage().show("Enlarged Lines " + ct.toString(), debugValues);
		
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
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("leaftip.*.x", "X-coordinate of a certain leaf-tip."),
				new CalculatedProperty("leaftip.*.y", "Y-coordinate of a certain leaf-tip."),
				new CalculatedProperty("leaftip.*.angle", "Leaf-tip angle of a certain leaf."),
				new CalculatedProperty("leaftip.count", "Number of leaves."),
				new CalculatedProperty("leaftip.count.best_angle", "Number of leaves for the 'best' side view angle "
						+ "(as determined from the main growth orientation observed from top-view.")
		};
	}
}