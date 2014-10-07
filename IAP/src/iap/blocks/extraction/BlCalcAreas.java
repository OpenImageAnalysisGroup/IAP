package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Calculates the top and side area values for visible and fluorescence images (by default) and if enabled, from NIR and IR images as well.
 * 
 * @author klukas
 */
public class BlCalcAreas extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	private static final String AREA = "area";
	private static final String AREA_NORM = "area.norm";
	
	@Override
	protected Image processVISmask() {
		Image image = input().masks().vis();
		if (getBoolean("process VIS mask", true))
			processImage(CameraType.VIS, image);
		return image;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image image = input().masks().fluo();
		if (getBoolean("process FLUO mask", true))
			processImage(CameraType.FLUO, image);
		return image;
	}
	
	@Override
	protected Image processNIRmask() {
		Image image = input().masks().nir();
		if (getBoolean("process NIR mask", false))
			processImage(CameraType.NIR, image);
		return image;
	}
	
	@Override
	protected Image processIRmask() {
		Image image = input().masks().ir();
		if (getBoolean("process IR mask", false))
			processImage(CameraType.IR, image);
		return image;
	}
	
	private void processImage(CameraType ct, Image image) {
		if (image == null)
			return;
		
		Double distHorizontal = optionsAndResults.getCalculatedBlueMarkerDistance();
		Double realMarkerDist = optionsAndResults.getREAL_MARKER_DISTANCE();
		if (distHorizontal == null)
			realMarkerDist = null;
		
		double normFactorArea = distHorizontal != null && realMarkerDist != null ? (realMarkerDist * realMarkerDist)
				/ (distHorizontal * distHorizontal)
				: 1;
		// double normFactor = distHorizontal != null && realMarkerDist != null ? realMarkerDist / distHorizontal : 1;
		int filledArea = image.io().countFilledPixels();
		if (distHorizontal != null) {
			getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.GEOMETRY, AREA_NORM),
					filledArea * normFactorArea,
					"mm^2", this);
		}
		getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.GEOMETRY, AREA), filledArea,
				"px^2", this);
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) {
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] {
						"RESULT_side.vis.area", "RESULT_top.vis.area", "RESULT_side.vis.area.norm", "RESULT_top.vis.area.norm",
						"RESULT_side.fluo.area", "RESULT_top.fluo.area", "RESULT_side.fluo.area.norm", "RESULT_top.fluo.area.norm",
						"RESULT_side.nir.area", "RESULT_top.nir.area", "RESULT_side.nir.area.norm", "RESULT_top.nir.area.norm",
						"RESULT_side.ir.area", "RESULT_top.ir.area", "RESULT_side.fluo.ir.norm", "RESULT_top.ir.area.norm",
				}, this);
		
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
		return "Calculate Areas";
	}
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	public String getDescription() {
		return "Calculates the top and side area values for visible and florescence images (by default) " +
				"and if enabled, from NIR and IR images as well.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty(AREA, "Number of foreground pixels. Therefore, projected plant area in pixels."),
				new CalculatedProperty(AREA_NORM, "Normalized area of foreground pixels according to real-world coordinates.")
		};
	}
}
