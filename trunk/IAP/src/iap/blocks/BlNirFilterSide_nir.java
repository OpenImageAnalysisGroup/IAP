package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRmask() {
		boolean debug = getBoolean("debug", false);
		FlexibleImage nirMask = input().masks().nir();
		
		if (!getBoolean("enable", true)) {
			return nirMask;
		}
		
		if (getBoolean("Replace Background with Gray", false)) {
			int gl = getInt("Replace color value", 180);
			nirMask = nirMask.io().replaceColor(options.getBackground(), new Color(gl, gl, gl).getRGB()).getImage().show("Background replace with gray", debug);
		}
		FlexibleImage origNirMask = options.getCameraPosition() == CameraPosition.TOP && nirMask != null ? nirMask.copy() : null;
		int average = 180;
		if (nirMask != null) {
			double f;
			int regionSize;
			f = getDouble("Adaptive_Threshold_F", 0.08);
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
}
