package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
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
			processImage(CameraType.VIS, image, input().images().getVisInfo());
		return image;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image image = input().masks().fluo();
		if (getBoolean("process FLUO mask", true))
			processImage(CameraType.FLUO, image, input().images().getFluoInfo());
		return image;
	}
	
	@Override
	protected Image processNIRmask() {
		Image image = input().masks().nir();
		if (getBoolean("process NIR mask", false))
			processImage(CameraType.NIR, image, input().images().getNirInfo());
		return image;
	}
	
	@Override
	protected Image processIRmask() {
		Image image = input().masks().ir();
		if (getBoolean("process IR mask", false))
			processImage(CameraType.IR, image, input().images().getIrInfo());
		return image;
	}
	
	private void processImage(CameraType ct, Image image, ImageData imageRef) {
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
					"mm^2", this, imageRef);
		}
		getResultSet().setNumericResult(getBlockPosition(), new Trait(optionsAndResults.getCameraPosition(), ct, TraitCategory.GEOMETRY, AREA), filledArea,
				"px^2", this, imageRef);
	}
	
	@Override
	public void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) {
		
		for (CameraPosition cp : new CameraPosition[] { CameraPosition.SIDE, CameraPosition.TOP })
			for (CameraType ct : CameraType.values())
				calculateRelativeValues(time2allResultsForSnapshot, time2summaryResult, getBlockPosition(),
						new String[] {
								new Trait(cp, ct, TraitCategory.GEOMETRY, "area").toString(),
								new Trait(cp, ct, TraitCategory.GEOMETRY, "area.norm").toString()
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
	public boolean isChangingImages() {
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
