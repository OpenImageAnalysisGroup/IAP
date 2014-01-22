package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Apply the FLUO mask to other camera types, and the VIS mask back to the FLUO mask.
 * 
 * @author klukas
 */
public class BlAdaptiveUseFluoMaskToClearOther extends AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	private boolean autoTune;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		this.autoTune = getBoolean("Auto-tune", true);
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		int back = options.getBackground();
		double averageLeafWidthEstimation = processedMasks.fluo() == null ? Double.NaN :
				processedMasks.fluo().io().countFilledPixels() /
						(double) processedMasks.fluo().copy().io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels();
		if (getBoolean("process VIS and FLUO", true) && processedMasks.fluo() != null && processedMasks.vis() != null) {
			
			// apply enlarged FLUO mask to VIS
			if (processedMasks.vis() != null) {
				double fW = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
				double fH = (double) processedMasks.vis().getHeight() / (double) processedMasks.fluo().getHeight();
				double fluoBlur = autoTune ? getDouble("Blur Leaf-width Factor Fluo on Vis", 2) * averageLeafWidthEstimation : getDouble(
						"blur fluo mask on vis",
						10);
				// System.out.println("DEBUG: FOR MASKING VIS BLUR FLUO: " + fluoBlur);
				processedMasks.setVis(
						processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(fluoBlur).getImage(),
								back).getImage());
				
				if (getDouble("Blur Leaf-width Factor Vis on Fluo", 3) > 0) {
					double corr = processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
					double visBlur = autoTune ? getDouble("Blur Leaf-width Factor Vis on Fluo", 3) * averageLeafWidthEstimation * corr : getDouble(
							"blur vis mask on fluo",
							40d);
					// System.out.println("DEBUG: FOR MASKING FLUO BLUR VIS: " + visBlur);
					processedMasks.setFluo(
							processedMasks.fluo().io().copy().applyMask(
									processedMasks.vis().io().copy().resize(1d / fW, 1d / fH).blur(visBlur).getImage(),
									back).getImage());
				}
			}
		}
		if (getBoolean("process NIR", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.nir() != null) {
				double fluoBlur = autoTune ? averageLeafWidthEstimation : getDouble("blur fluo mask on nir", 10);
				// System.out.println("DEBUG: FOR MASKING NIR BLUR FLUO: " + fluoBlur);
				processedMasks.setNir(
						processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(fluoBlur).getImage(),
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
		return "Auto-tuning FLUO-derived masking of other images";
	}
	
	@Override
	public String getDescription() {
		return "Use FLUO mask to clear VIS/NIR/IR. "
				+
				"The blur-factor is derived from the average width of the plant elements in the FLUO image. "
				+
				"The parameters &quot;Blur Leaf-width Factor Fluo on Vis&quot; and &quot;Blur Leaf-width Factor Vis on Fluo&quot; are used to modify the mask size if auto-tune is enabled";
	}
}
