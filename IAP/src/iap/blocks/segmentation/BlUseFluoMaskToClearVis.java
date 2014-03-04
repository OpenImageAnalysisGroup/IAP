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
public class BlUseFluoMaskToClearVis extends AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = optionsAndResults.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.vis() != null) {
				if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE) {
					processedMasks.setVis(
							processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().copy().blur(getDouble("blur fluo mask on vis", 2)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
				}
				double f = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
				if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
					processedMasks.setVis(
							processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().copy().resize(f, f)
											.blur(getDouble("blur fluo mask on vis", 0d)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
				}
				if (getBoolean("Apply Filtered VIS back to FLUO", false))
					processedMasks.setFluo(
							processedMasks.fluo().io().copy().applyMask_ResizeMaskIfNeeded(
									processedMasks.vis().io().copy().resize(1d / f, 1d / f)
											.blur(getDouble("blur vis mask on fluo", 1.5d)).getImage(),
									back).show("FILTERED FLUO IMAGE", debug).getImage());
			}
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Apply FLUO mask to VIS";
	}
	
	@Override
	public String getDescription() {
		return "Clears the VIS image, based on the FLUO mask, and optionally applies filtered VIS back to FLUO.";
	}
}
