package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClear_Arabidopsis_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.vis() != null) {
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					processedMasks.setVis(
							processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().copy().addBorder(
											getInt("cut left right", 0),
											getInt("cut top bottom", 50),
											getInt("shift X", 0),
											getInt("shift Y", 30),
											options.getBackground())
											.blur(getDouble("blur fluo mask on vis", 2)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					double f = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
					processedMasks.setVis(
							processedMasks.vis().io().applyMask(
									processedMasks.fluo().io().resize(f, f)
											.blur(getDouble("blur fluo mask on vis", 1.5d)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
					processedMasks.setFluo(
							processedMasks.fluo().io().applyMask(
									processedMasks.vis().io().resize(1d / f, 1d / f)
											.blur(getDouble("blur vis mask on fluo", 1.5d)).getImage(),
									back).show("FILTERED FLUO IMAGE", debug).getImage());
				}
			}
		}
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}
