package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClearIr extends AbstractSnapshotAnalysisBlock {
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		debug = getBoolean("debug", false);
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = optionsAndResults.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to IR
			if (processedMasks.ir() != null) {
				if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE && getBoolean("enabled", true)) {
					processedMasks.setIr(
							processedMasks.ir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().addBorder(0, 0, 0, 0, optionsAndResults.getBackground())
											.blur(getDouble("mask blur", 2)).getImage(),
									back).show("FILTERED IR IMAGE", debug).getImage());
				}
				if (optionsAndResults.getCameraPosition() == CameraPosition.TOP && getBoolean("enabled", true)) {
					processedMasks.setIr(
							processedMasks.ir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().copy()
											// .addBorder(0, 0, 0, 0, options.getBackground())
											.blur(getDouble("mask blur", 2)).getImage(),
									back).show("FILTERED IR IMAGE", debug).getImage());
				}
			}
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Apply FLUO mask to IR";
	}
	
	@Override
	public String getDescription() {
		return "Clears the IR image, based on the FLUO mask.";
	}
	
}
