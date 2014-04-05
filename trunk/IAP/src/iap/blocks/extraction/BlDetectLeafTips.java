package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import org.StringManipulationTools;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import tests.JMP.leaf_clustering.BorderAnalysis;
import tests.JMP.leaf_clustering.BorderFeature;
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
	
	@Override
	protected void prepare() {
		super.prepare();
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", false)) {
			boolean isBestAngle = isBestAngle();
			if (!isBestAngle)
				ignore = true;
		}
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
		if (getBoolean("Calculate on Visible Image", false) && !ignore) {
			Image workimg = input().masks().vis().copy();
			int searchRadius = getInt("Search-radius (Vis)", 33);
			double fillGradeInPercent = getDouble("Fillgrade (Vis)", 0.3);
			borderSize = searchRadius / 2;
			workimg.setCameraType(input().masks().vis().getCameraType());
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Vis)", 3), getInt("Masksize Erode (Vis)", 8),
					getInt("Masksize Dilate (Vis)", 12));
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.VIS, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().vis();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		if (getBoolean("Calculate on Fluorescence Image", false) && !ignore) {
			Image workimg = input().masks().fluo().copy();
			int searchRadius = getInt("Search-radius (Fluo)", 30);
			double fillGradeInPercent = getDouble("Fillgrade (Fluo)", 0.3);
			borderSize = searchRadius / 2;
			workimg.setCameraType(CameraType.FLUO);
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Fluo)", 2), getInt("Masksize Erode (Fluo)", 8),
					getInt("Masksize Dilate (Fluo)", 12));
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.FLUO, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().fluo();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		if (getBoolean("Calculate on Near-infrared Image", false) && !ignore) {
			Image workimg = input().masks().nir().copy();
			int searchRadius = getInt("Search-radius (Nir)", 20);
			double fillGradeInPercent = getDouble("Fillgrade (Nir)", 0.35);
			borderSize = searchRadius / 2;
			workimg.setCameraType(CameraType.NIR);
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Nir)", 2), getInt("Masksize Erode (Nir)", 2),
					getInt("Masksize Dilate (Nir)", 4));
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.NIR, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().nir();
	}
	
	private void savePeaksAndFeatures(LinkedList<BorderFeature> peakList, CameraType cameraType, CameraPosition cameraPosition, int searchRadius) {
		
		getResultSet().setObjectResult(getBlockPosition(), "leaftiplist_" + cameraType, peakList);
		
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
				getResultSet().setNumericResult(0,
						"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index) + ".angle",
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
								.drawLine((int) pos_fin.getX(), (int) pos_fin.getY(), (int) direction_fin.getX(), (int) direction_fin.getY(), Color.BLUE.getRGB(), 0.5,
										1)
								.text((int) direction_fin.getX() + 10, (int) direction_fin.getY(),
										"x: " + (int) direction_fin.getX() + " y: " + (int) direction_fin.getY(),
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
		// save leaf count
		getResultSet().setNumericResult(getBlockPosition(),
				"RESULT_" + cameraPosition + "." + cameraType + ".leaftip.count", index - 1, "leaftips");
	}
	
	private LinkedList<BorderFeature> getPeaksFromBorder(Image img, int searchRadius, double fillGradeInPercent) {
		BorderAnalysis ba = null;
		LinkedList<BorderFeature> res = null;
		
		ba = new BorderAnalysis(img);
		int geometricThresh = (int) (fillGradeInPercent * (Math.PI * searchRadius * searchRadius));
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
		
		// get skeleton-image to connect lose leaves and for optimization
		Image skel = getResultSet().getImage("skeleton_" + ct.toString());
		if (skel != null)
			img = img.io().or(skel.copy().io().replaceColor(-16777216, background).getImage()).getImage()
					.show("skel on mask" + ct.toString(), debugValues);
		else {
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No " + ct.toString()
					+ " skeleton image available, can't process it within leaf-tip detection!");
		}
		
		// morphological operations
		img = img.io().bm().dilate(dilate).erode(erode).getImage().show("Erode and Dilate on " + ct.toString(), debugValues);
		
		// blur
		img = img.io().blur(blurSize).getImage().show("Blured Image " + ct.toString(), debugValues);
		
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