package de.ipk.ag_ba.commands.experiment.charting;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ExperimentTransformationPipeline {
	
	private ExperimentTransformation[] pipelineSteps;
	private final ExperimentInterface startDataset;
	
	public ExperimentTransformationPipeline(ExperimentInterface startDataset, ExperimentTransformation... pipelineSteps) {
		this.startDataset = startDataset;
		this.pipelineSteps = pipelineSteps;
	}
	
	public ExperimentInterface getInput(ExperimentTransformation target) {
		ExperimentInterface result = startDataset;
		for (ExperimentTransformation step : pipelineSteps) {
			if (step == target)
				return result;
			result = step.transform(result);
		}
		throw new RuntimeException("Invalid target!");
		
	}
	
	public ExperimentInterface transformAllSteps() {
		ExperimentInterface result = startDataset;
		for (ExperimentTransformation step : pipelineSteps) {
			result = step.transform(result);
		}
		return result;
	}
	
	public void setSteps(ExperimentTransformation... steps) {
		pipelineSteps = steps;
	}
}
