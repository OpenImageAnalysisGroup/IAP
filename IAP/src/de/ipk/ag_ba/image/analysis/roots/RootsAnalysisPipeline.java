package de.ipk.ag_ba.image.analysis.roots;

import java.awt.Color;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.image.analysis.maize.AbstractImageProcessor;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveImagesToMasks_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlMoveMasksToImageSet_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis.BlLoadImagesIfNeeded_images;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsAddBorderAroundImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsRemoveBoxAndNoise;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsSharpenImage;
import de.ipk.ag_ba.image.operations.blocks.cmds.roots.BlRootsSkeletonize;

/**
 * Roots / Waterscan Pipeline
 * 
 * @author klukas, entzian
 */
public class RootsAnalysisPipeline extends AbstractImageProcessor {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	private final String pipelineName;
	
	public RootsAnalysisPipeline(String pipelineName) {
		this.pipelineName = pipelineName;
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		
		String[] defaultBlockList = new String[] {
				BlLoadImagesIfNeeded_images.class.getCanonicalName(),
				BlRootsAddBorderAroundImage.class.getCanonicalName(),
				BlMoveImagesToMasks_vis_fluo_nir.class.getCanonicalName(),
				BlRootsSharpenImage.class.getCanonicalName(),
				BlRootsRemoveBoxAndNoise.class.getCanonicalName(),
				BlRootsSkeletonize.class.getCanonicalName(),
				BlMoveMasksToImageSet_vis_fluo_nir.class.getCanonicalName()
		};
		modifySettings(options);
		
		return getPipelineFromBlockList(pipelineName, defaultBlockList);
	}
	
	@Override
	public void setStatus(
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
		return status;
	}
	
	@Override
	public IAP_RELEASE getVersionTag() {
		return IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_ROOTS;
	}
	
	private void modifySettings(ImageProcessorOptions options) {
		if (options == null)
			return;
		
		SystemOptions so = SystemOptions.getInstance(pipelineName + ".pipeline.ini");
		String g = "IMAGE-ANALYSIS-PIPELINE-SETTINGS-" + getClass().getCanonicalName();
		
		options.setSystemOptionStorage(so, g);
		
		// for Block "BlLoadImagesIfNeeded_images"
		// Block 1
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_WIDTH_VIS, 768);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_WIDTH_FLUO, 768);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_WIDTH_NIR, 768);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_WIDTH_IR, 768);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_HEIGHT_VIS, 576);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_HEIGHT_FLUO, 576);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_HEIGHT_NIR, 576);
		options.clearAndAddIntSetting(Setting.TV_TEST_IMAGE_HEIGHT_IR, 576);
		
		// Block 2
		options.clearAndAddIntSetting(Setting.ROOT_BORDER_SIZE_VIS, 100);
		options.clearAndAddIntSetting(Setting.ROOT_BORDER_COLOR, new Color(255, 255, 255).getRGB());
		options.clearAndAddIntSetting(Setting.ROOT_TRANSLATE_X, 50);
		options.clearAndAddIntSetting(Setting.ROOT_TRANSLATE_Y, 50);
		
		// Block 4
		options.clearAndAddIntSetting(Setting.ROOT_BLUR_RADIUS, 2);
		options.clearAndAddIntSetting(Setting.ROOT_NUMBER_OF_RUNS_SHARPEN, 3);
		
		// Block 5
		options.clearAndAddIntSetting(Setting.ROOT_BORDER_WIDTH, 2);
		options.clearAndAddIntSetting(Setting.ROOT_TRESHOLD_BLUE, 3);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA, 50);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM, 50);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA, (800 * 800));
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM, 800);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA, 10);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM, 10);
		options.clearAndAddIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_SIZE_OF_REGION, 5);
		options.clearAndAddIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_ASSUMED_BACKGROUND_COLOR, Color.BLACK.getRGB());
		options.clearAndAddIntSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_NEW_FORGROUND_COLOR, Color.WHITE.getRGB());
		options.clearAndAddDoubleSetting(Setting.ROOT_ADAPTIVE_TRESHOLD_K, 0.02);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_AREA, 10);
		options.clearAndAddIntSetting(Setting.ROOT_REMOVE_SMALL_ELEMENTS_DIM, 10);
		
		options.setSystemOptionStorage(null, null);
		
	}
	
}
