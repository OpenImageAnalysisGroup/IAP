package de.ipk.ag_ba.commands.experiment.process;

import org.IniIoProvider;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.UserDefinedImageAnalysisPipelineTask;

public class ActionPerformAnalysisLocally extends AbstractPhenotypeAnalysisAction {
	
	private IniIoProvider iniIO;
	private SystemOptions so;
	
	public ActionPerformAnalysisLocally(String tooltip) {
		super(tooltip);
	}
	
	public ActionPerformAnalysisLocally(IniIoProvider iniIO, ExperimentReference experiment) {
		this("Perform Analysis (local execution)");
		this.iniIO = iniIO;
		so = SystemOptions.getInstance(null, iniIO);
		this.experiment = experiment;
	}
	
	@Override
	public String getDefaultTooltip() {
		return so.getString("DESCRIPTION", "pipeline_description", "(no description specified)", true);
	}
	
	@Override
	public String getDefaultTitle() {
		return "Perform " + StringManipulationTools
				.removeHTMLtags(so.getString("DESCRIPTION", "pipeline_name", "(unnamed)", true));
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Applications-Engineering-64.png";
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return SystemAnalysis.getNumberOfCPUs();
	}
	
	@Override
	public int getNumberOfJobs() {
		return 1;
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		PipelineDesc pd = new PipelineDesc(
				null, iniIO,
				so.getString("DESCRIPTION", "pipeline_name", "(unnamed)", true),
				so.getString("DESCRIPTION", "pipeline_description", "(no description specified)", true));
		return new UserDefinedImageAnalysisPipelineTask(pd);
	}
}