package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;

/**
 * @author klukas
 */
public class UserDefinedImageAnalysisPipelineTask extends AbstractPhenotypingTask {
	public UserDefinedImageAnalysisPipelineTask(PipelineDesc pd) {
		super(pd);
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return true;
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return true;
	}
	
	@Override
	public ImageProcessor getImageProcessor() {
		return new AbstractImageProcessor() {
			private BackgroundTaskStatusProviderSupportingExternalCall status;
			
			@Override
			public void setStatus(BackgroundTaskStatusProviderSupportingExternalCall status) {
				this.status = status;
			}
			
			@Override
			public BackgroundTaskStatusProviderSupportingExternalCall getStatus() {
				return status;
			}
			
			@Override
			public BlockPipeline getPipeline(ImageProcessorOptions options) {
				return getPipelineFromBlockList(getSystemOptions(), null);
			}
		};
	}
}
