package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.FluoAnalysis;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author klukas
 */
public class BlAdaptiveSegmentationFluo extends AbstractSnapshotAnalysisBlock {
	
	private boolean auto_tune, auto_tune_process_red_by_green;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.auto_tune = getBoolean("Auto-Tune", true);
		this.auto_tune_process_red_by_green = getBoolean("Auto-Tune - Process Mostly Red", false);
	}
	
	@Override
	protected synchronized Image processFLUOmask() {
		if (input().masks().fluo() == null) {
			return null;
		}
		
		if (debugValues) {
			input().masks().fluo().show("inp fluo");
		}
		ImageOperation io = new ImageOperation(input().masks().fluo()).applyMask_ResizeSourceIfNeeded(input().images().fluo(), options.getBackground());
		
		Image resClassic, resChlorophyll, resPhenol;
		if (!auto_tune) {
			double min = 220;
			double p1 = getDouble("minimum-intensity-classic", min);
			double p2 = getDouble("minimum-intensity-chloro", min);
			double p3 = getDouble("minimum-intensity-phenol", 170);
			resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, p1).getImage();
			resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, p2).getImage();
			resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, p3).getImage();
		} else {
			ImageOperation filterInp = io.copy();
			if (auto_tune_process_red_by_green)
				filterInp = filterInp.getR();
			else
				filterInp = filterInp.convertFluo2intensity(FluoAnalysis.CLASSIC, 255);
			ImageOperation filter = filterInp.show("Input For Auto-Threshold", false);
			filter = filter.autoThresholdingColorImageByUsingBrightnessMaxEntropy(auto_tune_process_red_by_green, debugValues).getImage()
					.show("Result Filter", debugValues).io();
			
			io = io.applyMask(filter.getImage()).show("USED FOR CALC", debugValues);
			resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, 255).getImage();
			resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, 255).getImage();
			resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, 255).getImage();
			Image res = new Image(resClassic, resChlorophyll, resPhenol);
			res.show("res", debugValues);
		}
		Image r = new Image(resClassic, resChlorophyll, resPhenol);
		if (auto_tune && auto_tune_process_red_by_green) {
			r = r.io().show("BEFORE HUE", false).filterByHSV_hue(getDouble("Auto-Tune - Minimum Result Hue", 0.4), options.getBackground())
					.show("AFTER HUE", false).getImage();
		}
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
		return "Auto-tuning Fluo Segmentation";
	}
	
	@Override
	public String getDescription() {
		return "Detects the noise level, background and foreground levels to segment the plant from the background (FLUO).";
	}
}
