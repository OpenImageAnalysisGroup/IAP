package iap.pipelines.maize;

import iap.blocks.postprocessing.BlHighlightNullResults;
import iap.blocks.threeD.BlThreeDreconstruction;
import iap.blocks.unused.BlCrop;
import iap.pipelines.ImageProcessorOptions;
import iap.pipelines.StringAndFlexibleMaskAndImageSet;

import java.util.HashMap;
import java.util.HashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
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
	
	public MaizeAnalysisPipelineWith3D(SystemOptions so) {
		super(so);
	}
	
	@Override
	public BlockPipeline getPipeline(ImageProcessorOptions options) {
		BlockPipeline p = super.getPipeline(options);
		// if (options != null)
		// options.clearAndAddBooleanSetting(Setting.DRAW_CONVEX_HULL, false);
		// if (options != null)
		// options.clearAndAddBooleanSetting(Setting.DRAW_SKELETON, false);
		p.remove(BlCrop.class);
		p.remove(BlHighlightNullResults.class);
		p.add(BlThreeDreconstruction.class);
		return p;
	}
	
	@Override
	public HashMap<Integer, StringAndFlexibleMaskAndImageSet> pipeline(ImageProcessorOptions options, ImageSet input, ImageSet optInputMasks,
			int maxThreadsPerImage,
			HashMap<Integer, ImageStack> debugStack) throws Exception {
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
				HashMap<Integer, StringAndFlexibleMaskAndImageSet> res = super.pipeline(options, input, optInputMasks, maxThreadsPerImage, debugStack);
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
