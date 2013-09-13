package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlAutoAdaptiveThresholdNir extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processNIRmask() {
		boolean debug = getBoolean("debug", false);
		Image nirMask = input().masks().nir();
		
		if (!getBoolean("enabled", true)) {
			return nirMask;
		}
		boolean autoTune = getBoolean("Auto-tune", true);
		Image origNirMask = options.getCameraPosition() == CameraPosition.TOP && nirMask != null ? nirMask.copy() : null;
		int average = autoTune ? 180 : getInt("Replace color value", 180);
		if (nirMask != null) {
			double f;
			int regionSize;
			f = getDouble("Adaptive_Threshold_F", 0.08);
			if (getBoolean("Replace Background with Gray", true)) {
				nirMask = nirMask.io().replaceColor(options.getBackground(), new Color(average, average, average).getRGB()).getImage()
						.show("Background replace with gray", debug);
			}
			if (!autoTune)
				regionSize = getInt("Adaptive_Threshold_Region_Size", 50);
			else {
				Image ref = input().masks().vis();
				if (ref == null)
					ref = input().masks().fluo();
				if (ref == null)
					regionSize = getInt("Adaptive_Threshold_Region_Size", 50);
				else {
					ref = ref.copy();
					double averageLeafWidthEstimation = ref.io().countFilledPixels() /
							(double) ref.io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels();
					regionSize = (int) (averageLeafWidthEstimation * 15);
					if (regionSize < 10)
						regionSize = 10;
				}
			}
			nirMask = nirMask.io().show("ADAPT IN", debug).
					adaptiveThresholdForGrayscaleImage(regionSize, average,
							options.getBackground(), f).getImage().show("ADAPT OUT", debug);
			input().masks().setNir(nirMask);
			if (origNirMask != null) {
				nirMask = nirMask.io().and(origNirMask).getImage();
			}
		}
		return nirMask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto-tuning NIR Segmentation";
	}
	
	@Override
	public String getDescription() {
		return "Separates the plant pixels from the background, by using a adaptive thresholding algorithm. " +
				"The region size for the auto-tuning thresholding block is varies, depending on the estimated " +
				"average leaf width.";
	}
}
