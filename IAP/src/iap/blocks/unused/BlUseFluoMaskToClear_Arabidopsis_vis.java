package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.CameraType;

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
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
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
											getInt("cut top bottom", 0),
											getInt("shift X", 0),
											getInt("shift Y", 0),
											options.getBackground())
											.blur(getDouble("blur fluo mask on vis", 2)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					double f = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
					processedMasks.setVis(
							processedMasks.vis().io().applyMask(
									processedMasks.fluo().io().copy().resize(f, f)
											.blur(getDouble("blur fluo mask on vis", 1.5d)).getImage(),
									back).show("FILTERED VIS IMAGE", debug).getImage());
					processedMasks.setFluo(
							processedMasks.fluo().io().copy().applyMask(
									processedMasks.vis().io().copy().resize(1d / f, 1d / f)
											.blur(getDouble("blur vis mask on fluo", 1.5d)).getImage(),
									back).show("FILTERED FLUO IMAGE", debug).getImage());
				}
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
	
}
