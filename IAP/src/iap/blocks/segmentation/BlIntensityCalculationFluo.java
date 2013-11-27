package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.FluoAnalysis;
import de.ipk.ag_ba.image.operation.ImageOperation;
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
		
		// getInput().getMasks().getFluo().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeFLUOMask2.png");
		if (input().masks().fluo() == null) {
			return null;
		}
		boolean debug = getBoolean("debug", false);
		ImageOperation io = new ImageOperation(input().masks().fluo()).applyMask_ResizeSourceIfNeeded(input().images().fluo(),
				options.getBackground());
		ImageStack fis = debug ? new ImageStack() : null;
		if (debug)
			fis.addImage("FLUO", io.copy().getImage(), null);
		double min = 220;
		Image resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, getDouble("minimum-intensity-classic", min)).getImage();
		Image resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, getDouble("minimum-intensity-chloro", min)).getImage();
		min = getDouble("minimum-intensity-phenol", 240);
		
		Image resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, min).getImage();
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
			// r.getIO().saveImageOnDesktop("FLUO_C_P_C.png");
		}
		
		// the proper 3 channel image is required by BlIntensityAnalysis
		// if (!getBoolean("show conversion", false)) {
		// r = io.copy().applyMask(r, options.getBackground()).getImage();
		// }
		
		return r;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.fluo());
		if (processedMasks.fluo() != null)
			processedMasks.setFluo(processedMasks.fluo().io().medianFilter32Bit().getImage());
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
