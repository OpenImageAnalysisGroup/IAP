/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * @author pape, klukas
 */
public class BlockClearBackgroundByComparingNullImageAndImage extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = PhenotypeAnalysisTask.BACKGROUND_COLORint;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			return new ImageOperation(getInput().getImages().getVis()).compare()
					.compareImages(getInput().getMasks().getVis(),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.abDiff_VIS),
							back, true, false).getImage();
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			return new ImageOperation(getInput().getImages().getVis()).compare()
					.compareImages(getInput().getMasks().getVis(),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.abDiff_VIS),
							back, true, false).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			return new ImageOperation(getInput().getImages().getFluo()).compare()
					.compareImages(getInput().getMasks().getFluo(),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.abDiff_FLOU),
							back, false, true).getImage();
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			return new ImageOperation(getInput().getImages().getFluo()).compare()
					.compareImages(getInput().getMasks().getFluo(),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.abDiff_FLOU),
							back, false, true).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			return new ImageOperation(getInput().getImages().getNir()).compare()
					.compareImages(getInput().getMasks().getNir(),
							options.getIntSetting(Setting.L_Diff_NIR),
							options.getIntSetting(Setting.L_Diff_NIR),
							options.getIntSetting(Setting.abDiff_NIR),
							back, false, false).getImage();
		}
		if (options.getCameraTyp() == CameraTyp.TOP) {
			return new ImageOperation(getInput().getImages().getNir()).compare()
					.compareImages(getInput().getMasks().getNir(),
							options.getIntSetting(Setting.L_Diff_NIR),
							options.getIntSetting(Setting.L_Diff_NIR),
							options.getIntSetting(Setting.abDiff_NIR),
							back, false, false).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
}
