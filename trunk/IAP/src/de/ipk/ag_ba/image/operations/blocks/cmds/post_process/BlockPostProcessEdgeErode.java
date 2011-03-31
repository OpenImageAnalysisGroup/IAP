package de.ipk.ag_ba.image.operations.blocks.cmds.post_process;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.ImageTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class BlockPostProcessEdgeErode extends AbstractSnapshotAnalysisBlockFIS {
	
	private final boolean enlarge;
	
	/**
	 * Erode/delate/erode several times, depending on image type.
	 */
	public BlockPostProcessEdgeErode(boolean enlarge) {
		this.enlarge = enlarge;
		
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return postProcessResultImage(getInput().getImages().getVis(), getInput().getMasks().getVis(), options, options.getCameraTyp(), ImageTyp.RGB, enlarge);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return postProcessResultImage(getInput().getImages().getFluo(), getInput().getMasks().getFluo(), options, options.getCameraTyp(), ImageTyp.FLUO, enlarge);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return postProcessResultImage(getInput().getImages().getNir(), getInput().getMasks().getNir(), options, options.getCameraTyp(), ImageTyp.NIR, enlarge);
	}
	
	static FlexibleImage postProcessResultImage(FlexibleImage srcImage, FlexibleImage finalImage, ImageProcessorOptions options, CameraTyp cameraTyp,
			ImageTyp imageTyp, boolean enlarge) {
		
		ImageOperation maskIo = new ImageOperation(finalImage);
		
		switch (cameraTyp) {
			case SIDE:

				switch (imageTyp) {
					case RGB:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_RGB_SIDE); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_RGB_SIDE); ii++)
								maskIo.erode();
						break;
					
					case FLUO:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_FLUO_SIDE); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_FLUO_SIDE); ii++)
								maskIo.erode();
						break;
					
					case NIR:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_NIR_SIDE); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_NIR_SIDE); ii++)
								maskIo.erode();
						break;
				}
				
			case TOP:

				switch (imageTyp) {
					case RGB:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_RGB_TOP); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_RGB_TOP); ii++)
								maskIo.erode();
						break;
					
					case FLUO:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_FLUO_TOP); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_FLUO_TOP); ii++)
								maskIo.erode();
						break;
					
					case NIR:
						if (enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_DILATE_NIR_TOP); ii++)
								maskIo.dilate();
						if (!enlarge)
							for (int ii = 0; ii < options.getIntSetting(Setting.POST_PROCESS_ERODE_NIR_TOP); ii++)
								maskIo.erode();
						break;
				}
		}
		
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
	}
}
