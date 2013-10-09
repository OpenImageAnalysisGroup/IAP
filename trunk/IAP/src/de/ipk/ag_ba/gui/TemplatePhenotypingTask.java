package de.ipk.ag_ba.gui;

import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.AbstractImageProcessor;
import iap.pipelines.ImageProcessor;
import iap.pipelines.ImageProcessorOptions;

import java.util.ArrayList;

import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;

public class TemplatePhenotypingTask extends AbstractPhenotypingTask {
	private final AnalysisPipelineTemplate template;
	
	public TemplatePhenotypingTask(PipelineDesc pd, AnalysisPipelineTemplate template) {
		super(pd);
		this.template = template;
	}
	
	@Override
	protected boolean analyzeTopImages() {
		return template.analyzeTopImages();
	}
	
	@Override
	protected boolean analyzeSideImages() {
		return template.analyzeSideImages();
	}
	
	@Override
	public ImageProcessor getImageProcessor() throws Exception {
		return new AbstractImageProcessor() {
			@Override
			public BlockPipeline getPipeline(ImageProcessorOptions options) {
				ArrayList<String> defaultBlockList = new ArrayList<String>();
				for (ImageAnalysisBlock b : template.getBlockList(options))
					defaultBlockList.add(b.getClass().getCanonicalName());
				
				return getPipelineFromBlockList(options.getOptSystemOptions(), defaultBlockList.toArray(new String[] {}));
			}
		};
	}
}