package de.ipk.ag_ba.image.analysis.maize;

import java.util.HashMap;
import java.util.HashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlCrop_images_vis_fluo_nir_ir;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlReplaceEmptyOriginalImages_vis_fluo_nir;
import de.ipk.ag_ba.image.operations.blocks.cmds.threeD.BlockThreeDgeneration;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Comprehensive corn image analysis pipeline, processing VIS, FLUO and NIR images. Depends on reference images for initial comparison
 * and foreground / background separation.
 * 
 * @author klukas, pape
 */
public class MaizeAnalysisPipelineWith3D extends MaizeAnalysisPipeline {
	
	private BackgroundTaskStatusProviderSupportingExternalCall status;
	
	@Override
	protected BlockPipeline getPipeline(ImageProcessorOptions options) {
		BlockPipeline p = super.getPipeline(options);
		if (options != null)
			options.clearAndAddBooleanSetting(Setting.DRAW_CONVEX_HULL, false);
		if (options != null)
			options.clearAndAddBooleanSetting(Setting.DRAW_SKELETON, false);
		p.remove(BlCrop_images_vis_fluo_nir_ir.class);
		p.remove(BlReplaceEmptyOriginalImages_vis_fluo_nir.class);
		p.add(BlockThreeDgeneration.class);
		return p;
	}
	
	@Override
	public HashMap<Integer, FlexibleMaskAndImageSet> pipeline(ImageProcessorOptions options, FlexibleImageSet input, FlexibleImageSet optInputMasks,
			int maxThreadsPerImage,
			HashMap<Integer, FlexibleImageStack> debugStack) throws Exception {
		HashSet<Integer> rotationAngles = new HashSet<Integer>();
		if (input != null && input.getVisInfo() != null && input.getVisInfo().getParentSample() != null) {
			SampleInterface inSample = input.getVisInfo().getParentSample();
			for (Measurement m : inSample) {
				if (m instanceof ImageData) {
					ImageData id = (ImageData) m;
					if (id.getPosition() == null || id.getPosition() > 0)
						rotationAngles.add(id.getPosition() != null ? id.getPosition().intValue() : 0);
				}
			}
			if (rotationAngles.size() > 3) {
				// System.out.print(SystemAnalysisExt.getCurrentTime() + ">INFO: START 3D-PROCESSING OF A SAMPLE WITH " + rotationAngles.size()
				// + " RELATED SIDE ANGLE IMAGES...");
				HashMap<Integer, FlexibleMaskAndImageSet> res = super.pipeline(options, input, optInputMasks, maxThreadsPerImage, debugStack);
				// System.out.println("OK");
				return res;
			}
		}
		return null;
	}
	
	@Override
	public void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.status = status;
	}
	
	@Override
	public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
		return status;
	}
	
}
