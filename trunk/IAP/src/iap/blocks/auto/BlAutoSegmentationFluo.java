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
public class BlAutoSegmentationFluo extends AbstractSnapshotAnalysisBlock {
	
	private boolean auto_tune;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.auto_tune = getBoolean("Auto-Tune", true);
	}
	
	@Override
	protected synchronized Image processFLUOmask() {
		if (input().masks().fluo() == null) {
			return null;
		}
		ImageOperation io = new ImageOperation(input().masks().fluo()).applyMask_ResizeSourceIfNeeded(input().images().fluo(), options.getBackground());
		double min = 220;
		double p1 = getDouble("minimum-intensity-classic", min);
		double p2 = getDouble("minimum-intensity-chloro", min);
		double p3 = getDouble("minimum-intensity-phenol", 240);
		Image resClassic = io.copy().convertFluo2intensity(FluoAnalysis.CLASSIC, p1).getImage();
		Image resChlorophyll = io.copy().convertFluo2intensity(FluoAnalysis.CHLOROPHYL, p2).getImage();
		Image resPhenol = io.copy().convertFluo2intensity(FluoAnalysis.PHENOL, p3).getImage();
		return new Image(resClassic, resChlorophyll, resPhenol);
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
		return "Detects the noise level, background and foreground levels for segmentation.";
	}
}
