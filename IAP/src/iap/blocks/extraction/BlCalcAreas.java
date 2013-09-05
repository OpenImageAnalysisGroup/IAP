package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

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
 * Calculates the top and side area values for visible and florescence images (by default) and if enabled, from NIR and IR images as well.
 * 
 * @author klukas
 */
public class BlCalcAreas extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image image = input().masks().vis();
		if (getBoolean("process VIS mask", true))
			processImage("vis.", image);
		return image;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image image = input().masks().fluo();
		if (getBoolean("process FLUO mask", true))
			processImage("fluo.", image);
		return image;
	}
	
	@Override
	protected Image processNIRmask() {
		Image image = input().masks().nir();
		if (getBoolean("process NIR mask", false))
			processImage("nir.", image);
		return image;
	}
	
	@Override
	protected Image processIRmask() {
		Image image = input().masks().ir();
		if (getBoolean("process IR mask", false))
			processImage("ir.", image);
		return image;
	}
	
	private void processImage(String prefix, Image image) {
		if (image == null)
			return;
		
		Double distHorizontal = options.getCalculatedBlueMarkerDistance();
		Double realMarkerDist = options.getREAL_MARKER_DISTANCE();
		if (distHorizontal == null)
			realMarkerDist = null;
		
		double normFactorArea = distHorizontal != null && realMarkerDist != null ? (realMarkerDist * realMarkerDist)
				/ (distHorizontal * distHorizontal)
				: 1;
		// double normFactor = distHorizontal != null && realMarkerDist != null ? realMarkerDist / distHorizontal : 1;
		String pos = options.getCameraPosition() == CameraPosition.SIDE ? "RESULT_side." : "RESULT_top.";
		int filledArea = image.io().countFilledPixels();
		if (distHorizontal != null) {
			getProperties().setNumericProperty(getBlockPosition(), pos + prefix + "area.norm", filledArea * normFactorArea, "mm^2");
		}
		getProperties().setNumericProperty(getBlockPosition(), pos + prefix + "area", filledArea, "px^2");
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, Sample3D> time2inSamples,
			TreeMap<Long, TreeMap<String, ImageData>> time2inImages,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		calculateRelativeValues(time2inSamples, time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
				new String[] {
						"RESULT_side.vis.area", "RESULT_top.vis.area", "RESULT_side.vis.area.norm", "RESULT_top.vis.area.norm",
						"RESULT_side.fluo.area", "RESULT_top.fluo.area", "RESULT_side.fluo.area.norm", "RESULT_top.fluo.area.norm",
						"RESULT_side.nir.area", "RESULT_top.nir.area", "RESULT_side.nir.area.norm", "RESULT_top.nir.area.norm",
						"RESULT_side.ir.area", "RESULT_top.ir.area", "RESULT_side.fluo.ir.norm", "RESULT_top.ir.area.norm",
				});
		
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
}
