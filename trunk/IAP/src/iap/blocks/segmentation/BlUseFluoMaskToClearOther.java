package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Apply the FLUO mask to other camera types, and the VIS mask back to the FLUO mask.
 * 
 * @author klukas
 */
public class BlUseFluoMaskToClearOther extends AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		int back = optionsAndResults.getBackground();
		if (getBoolean("process VIS and FLUO", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			double fluoOnVis = getDouble("blur fluo mask on vis", 6);
			double visOnFluo = getDouble("blur vis mask on fluo", 40d);
			// boolean unitTune = true;
			// if (unitTune)
			// if (optionsAndResults.getUnitTestSteps() > 0) {
			// fluoOnVis += (int) ((optionsAndResults.getUnitTestIdx() / 4) - 2) * 2;
			// visOnFluo += ((int) ((optionsAndResults.getUnitTestIdx()) % 4) - 2) * 2;
			// }
			
			if (processedMasks.vis() != null) {
				double fW = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
				double fH = (double) processedMasks.vis().getHeight() / (double) processedMasks.fluo().getHeight();
				processedMasks.setVis(
						processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blurImageJ(fluoOnVis).getImage(),
								back).getImage());
				processedMasks.setFluo(
						processedMasks.fluo().io().copy().applyMask(
								processedMasks.vis().io().copy().resize(1d / fW, 1d / fH).blurImageJ(visOnFluo).getImage(),
								back).getImage());
			}
		}
		if (getBoolean("process NIR", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.nir() != null) {
				processedMasks.setNir(
						processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blurImageJ(getDouble("blur fluo mask on nir", 10)).getImage(),
								back).getImage());
			}
		}
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
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Apply FLUO mask to other images";
	}
	
	@Override
	public String getDescription() {
		return "Use FLUO mask to clear VIS/NIR/IR.";
	}
}
