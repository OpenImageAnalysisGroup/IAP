package iap.blocks.segmentation;

import iap.blocks.auto.BlAdaptiveSegmentationFluo;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.fluoop.FluoAnalysis;
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author klukas
 */
public class BlIntensityCalculationFluo extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected synchronized Image processFLUOmask() {
		
		if (input().masks().fluo() == null) {
			return null;
		}
		
		if (getBoolean("Store Unchanged Fluo for Color Analysis", true))
			getResultSet().setImage(getBlockPosition(), "inp_fluo",
					new ImageAndImageData(
							input().masks().fluo(),
							input().masks().getFluoInfo()), true);
		
		boolean debug = getBoolean("debug", false);
		ImageOperation io = new ImageOperation(input().masks().fluo()).applyMask_ResizeSourceIfNeeded(input().images().fluo(),
				optionsAndResults.getBackground());
		ImageStack fis = debug ? new ImageStack() : null;
		if (debug)
			fis.addImage("FLUO", io.copy().getImage(), null);
		Image resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, getDouble("minimum-intensity-classic", 220)).getImage();
		Image resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, getDouble("minimum-intensity-chloro", 220)).getImage();
		Image resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, getDouble("minimum-intensity-phenol", 240)).getImage();
		
		Image r = new Image(resClassic, resChlorophyll, resPhenol);
		
		if (debug) {
			fis.addImage("ClChPh", r, null);
			fis.addImage("CLASSIC", resClassic, null);
			fis.addImage("CHLORO", resChlorophyll, null);
			fis.addImage("PHENO", resPhenol, null);
			/**
			 * @see IntensityAnalysis: r_intensityClassic, g_.., b_...
			 */
			fis.show("Fluorescence Segmentation Results");
		}
		
		getResultSet().setImage(getBlockPosition(), BlAdaptiveSegmentationFluo.RESULT_OF_FLUO_INTENSITY,
				new ImageAndImageData(r, input().masks().getFluoInfo()), true);
		
		return input().masks().fluo().io().applyMask(r).getImage();
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.fluo());
		if (processedMasks.fluo() != null)
			processedMasks.setFluo(processedMasks.fluo());
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Calculate Fluo Intensity";
	}
	
	@Override
	public String getDescription() {
		return "Fluorescence intensity conversion and threshold-based filtering of background.";
	}
}
