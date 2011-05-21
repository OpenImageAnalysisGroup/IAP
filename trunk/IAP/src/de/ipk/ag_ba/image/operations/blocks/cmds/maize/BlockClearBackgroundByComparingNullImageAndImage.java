/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.CompareImageGenerator;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author pape, klukas
 */
public class BlockClearBackgroundByComparingNullImageAndImage extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = Color.WHITE.getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getVis(), getInput().getMasks().getVis());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_VIS), options.getIntSetting(Setting.L_Diff_VIS),
					options.getIntSetting(Setting.abDiff_VIS), back, true, false));
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getVis(), getInput().getMasks().getVis());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_VIS), options.getIntSetting(Setting.L_Diff_VIS),
					options.getIntSetting(Setting.abDiff_VIS), back, true, false));
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getFluo(), getInput().getMasks().getFluo());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_FLOU), options.getIntSetting(Setting.L_Diff_FLOU),
					options.getIntSetting(Setting.abDiff_FLOU), back, false, true));
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getFluo(), getInput().getMasks().getFluo());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_FLOU), options.getIntSetting(Setting.L_Diff_FLOU),
					options.getIntSetting(Setting.abDiff_FLOU), back, false, true));
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getNir(), getInput().getMasks().getNir());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_NIR), options.getIntSetting(Setting.L_Diff_NIR),
					options.getIntSetting(Setting.abDiff_NIR), back, false, false));
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			CompareImageGenerator ci = new CompareImageGenerator(getInput().getImages().getNir(), getInput().getMasks().getNir());
			return (ci.compareImages(options.getIntSetting(Setting.L_Diff_NIR), options.getIntSetting(Setting.L_Diff_NIR),
					options.getIntSetting(Setting.abDiff_NIR), back, false, false));
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
}
