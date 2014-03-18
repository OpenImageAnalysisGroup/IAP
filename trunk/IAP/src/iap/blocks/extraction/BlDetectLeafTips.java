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

import de.ipk.ag_ba.image.operation.BorderAnalysis;
import de.ipk.ag_ba.image.operation.BorderFeature;
import de.ipk.ag_ba.image.operation.ImageConvolution;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

public class BlDetectLeafTips extends AbstractSnapshotAnalysisBlock {
	
	boolean ignore = false;
	boolean debug_borderDetection;
	
	@Override
	protected void prepare() {
		super.prepare();
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", false)) {
			boolean isBestAngle = isBestAngle();
			if (!isBestAngle)
				ignore = false;
		}
		debug_borderDetection = getBoolean("Debug Border Detection", false);
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		Image workimg = input().masks().vis().copy();
		if (getBoolean("Calculate on Visible Image", false) && !ignore) {
			int searchRadius = getInt("Search-radius (Vis)", 33);
			double fillGradeInPercent = getDouble("Fillgrade (Vis)", 0.3);
			workimg.setCameraType(input().masks().vis().getCameraType());
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Vis)", 2), getInt("Masksize Erode (Vis)", 4),
					getInt("Masksize Dilate (Vis)", 8));
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.VIS, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().vis();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		Image workimg = input().masks().fluo().copy();
		if (getBoolean("Calculate on Fluorescence Image", false) && !ignore) {
			int searchRadius = getInt("Search-radius (Fluo)", 30);
			double fillGradeInPercent = getDouble("Fillgrade", 0.3);
			workimg.setCameraType(CameraType.FLUO);
			workimg = preprocessImage(workimg, searchRadius, getInt("Size for Bluring (Fluo)", 2), getInt("Masksize Erode (Fluo)", 2),
					getInt("Masksize Dilate (Fluo)", 4));
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.FLUO, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().fluo();
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		Image workimg = input().masks().nir().copy();
		if (getBoolean("Calculate on Near-infrared Image", false) && !ignore) {
			int searchRadius = getInt("Search-radius (Nir)", 10);
			double fillGradeInPercent = getDouble("Fillgrade", 0.35);
			savePeaksAndFeatures(getPeaksFromBorder(workimg, searchRadius, fillGradeInPercent), CameraType.NIR, optionsAndResults.getCameraPosition(),
					searchRadius);
		}
		return input().masks().nir();
	}
	
	private void savePeaksAndFeatures(LinkedList<BorderFeature> peakList, CameraType cameraType, CameraPosition cameraPosition, int searchRadius) {
		int index = 1;
		
		for (BorderFeature bf : peakList) {
			final Vector2D pos = bf.getPosition();
			final CameraType cameraType_fin = cameraType;
			if (pos == null || cameraPosition == null || cameraType == null) {
				continue;
			}
			getResultSet().setNumericResult(0,
					"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index) + ".x",
					pos.getX(), "px");
			getResultSet().setNumericResult(0,
					"RESULT_" + cameraPosition.toString() + "." + cameraType.toString() + ".leaftip." + StringManipulationTools.formatNumber(index) + ".y",
					pos.getY(), "px");
			index++;
			
			if (searchRadius > 0) {
				final int searchRadius_fin = searchRadius;
				getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
					
					@Override
					public Image postProcessMask(Image mask) {
						return mask.io().canvas().drawCircle((int) pos.getX(), (int) pos.getY(), searchRadius_fin, Color.RED.getRGB(), 0.5, 3).getImage();
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
		getResultSet().setNumericResult(0,
				"RESULT_" + cameraPosition + "." + cameraType + ".leaftip.count", index, "leaftips");
	}
	
	private LinkedList<BorderFeature> getPeaksFromBorder(Image img, int searchRadius, double fillGradeInPercent) {
		BorderAnalysis ba = null;
		LinkedList<BorderFeature> res = null;
		try {
			ba = new BorderAnalysis(img);
			int geometricThresh = (int) (fillGradeInPercent * (Math.PI * searchRadius * searchRadius));
			ba.calcSUSAN(searchRadius, geometricThresh);
			ba.getPeaksFromBorder(2, 10, "susan");
			ba.approxDirection(searchRadius * 2);
			
			if (debug_borderDetection)
				ba.plot(0);
			
			res = ba.getPeakList();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		img = img.io().bm().erode(erode).dilate(dilate).getImage().show("Erode and Dilate on " + ct.toString(), debugValues);
		
		// blur
		img = img.io().blur(blurSize).getImage().show("Blured Image " + ct.toString(), debugValues);
		
		// enlarge 1 px lines
		ImageConvolution ic = new ImageConvolution(img);
		img = ic.enlargeLines().getImage().show("Enlarged Lines " + ct.toString(), debugValues);
		
		// add border around image
		img = img.io().addBorder(searchRadius / 2, 0, 0, background).getImage();
		
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
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
		return "Detect leaf-tips of a plant. (number of leaves) <br><br>If skeleton (fluo) is calculated within the pipeline in a previous step, all plant objects are connected.";
	}
}