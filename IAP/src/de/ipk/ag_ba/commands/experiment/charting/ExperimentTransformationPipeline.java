package de.ipk.ag_ba.commands.experiment.charting;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ExperimentTransformationPipeline {
	
	private ExperimentTransformation[] pipelineSteps;
	private final ExperimentInterface startDataset;
	private ExperimentInterface[] stepResults;
	
	public ExperimentTransformationPipeline(ExperimentInterface startDataset, ExperimentTransformation... pipelineSteps) {
		this.startDataset = startDataset;
		this.pipelineSteps = pipelineSteps;
		this.stepResults = new ExperimentInterface[this.pipelineSteps.length];
	}
	
	public ExperimentInterface getInput(ExperimentTransformation target) {
		ExperimentInterface result = startDataset.clone();
		System.out.println("INPUT: N=" + result.getNumberOfMeasurementValues() + ", C=" + result.iterator().next().size());
		int idx = 0;
		for (ExperimentTransformation step : pipelineSteps) {
			if (step == target) {
				System.out.println();
				return result;
			}
			if (stepResults[idx] == null) {
				for (int i = idx; i < stepResults.length; i++)
					stepResults[i] = null;
			}
			result = stepResults[idx] != null ? stepResults[idx] : step.transform(result);
			stepResults[idx] = result;
			System.out.print(" >[" + step.getClass().getSimpleName() + "] N=" + result.getNumberOfMeasurementValues() + ", C="
					+ (result.iterator().hasNext() ? result.iterator().next().size() : "n/a"));
			idx++;
		}
		result.setHeader(startDataset.getHeader().clone());
		result.getHeader().setDatabaseId(null);
		return result;
	}
	
	public void setSteps(ExperimentTransformation... steps) {
		pipelineSteps = steps;
		this.stepResults = new ExperimentInterface[this.pipelineSteps.length];
	}
	
	public void setDirty(ExperimentTransformation step) throws Exception {
		boolean inform = false;
		int idx = 0;
		for (ExperimentTransformation s : pipelineSteps) {
			if (inform) {
				stepResults[idx] = null;
				s.updateStatus();
			}
			if (s == step) {
				stepResults[idx] = null;
				inform = true;
			}
			idx++;
		}
	}
}
