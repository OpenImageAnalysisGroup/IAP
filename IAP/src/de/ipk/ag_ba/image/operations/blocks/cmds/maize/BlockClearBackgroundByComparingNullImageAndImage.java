/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlockClearBackgroundByComparingNullImageAndImage extends AbstractSnapshotAnalysisBlockFIS {
	
	int back = PhenotypeAnalysisTask.BACKGROUND_COLORint;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage vis = getInput().getImages().getVis();
			vis = vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
			return new ImageOperation(vis).compare()
					.compareImages(getInput().getMasks().getVis(),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.L_Diff_VIS),
							options.getIntSetting(Setting.abDiff_VIS),
							back, false, false).border(2).getImage();
		}
		if (options.getCameraTyp() == CameraPosition.TOP) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage vis = getInput().getImages().getVis();
			vis = vis.resize((int) (scaleFactor * vis.getWidth()), (int) (scaleFactor * vis.getHeight()));
			return new ImageOperation(vis).blur(3).printImage("median", false).compare()
					.compareImages(getInput().getMasks().getVis().getIO().blur(3).printImage("medianb", false).getImage(),
							options.getIntSetting(Setting.L_Diff_VIS) * 0.8d,
							options.getIntSetting(Setting.L_Diff_VIS) * 0.8d,
							options.getIntSetting(Setting.abDiff_VIS) * 0.8d,
							back, false, false, false).border(2).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage fluo = getInput().getImages().getFluo();
			fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
			return new ImageOperation(fluo).compare()
					.compareImages(getInput().getMasks().getFluo(),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.abDiff_FLOU),
							back, false, true).border(2).getImage();
		}
		if (options.getCameraTyp() == CameraPosition.TOP) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			FlexibleImage fluo = getInput().getImages().getFluo();
			fluo = fluo.resize((int) (scaleFactor * fluo.getWidth()), (int) (scaleFactor * fluo.getHeight()));
			return new ImageOperation(fluo).compare()
					.compareImages(getInput().getMasks().getFluo(),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.L_Diff_FLOU),
							options.getIntSetting(Setting.abDiff_FLOU),
							back, false, true).border(2).getImage();
		}
		throw new UnsupportedOperationException("Unknown camera setting.");
	}
}
