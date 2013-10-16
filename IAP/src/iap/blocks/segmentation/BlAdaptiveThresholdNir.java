package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlAdaptiveThresholdNir extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processNIRmask() {
		boolean debug = getBoolean("debug", false);
		Image nirMask = input().masks().nir();
		
		if (!getBoolean("enabled", true)) {
			return nirMask;
		}
		
		Image origNirMask = options.getCameraPosition() == CameraPosition.TOP && nirMask != null ? nirMask.copy() : null;
		int average = 180;
		if (nirMask != null) {
			double f;
			int regionSize;
			f = getDouble("Adaptive_Threshold_F", 0.08);
			if (getBoolean("Replace Background with Gray", true)) {
				int gl = getInt("Replace color value", 180);
				nirMask = nirMask.io().replaceColor(options.getBackground(), new Color(gl, gl, gl).getRGB()).getImage().show("Background replace with gray", debug);
			}
			regionSize = getInt("Adaptive_Threshold_Region_Size", 50);
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
		return "Adaptive NIR Segmentation";
	}
	
	@Override
	public String getDescription() {
		return "Separates the plant pixels from the background, by using a adaptive thresholding algorithm.";
	}
}
