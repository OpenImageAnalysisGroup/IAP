package de.ipk.ag_ba.commands.experiment.process;

import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.UserDefinedImageAnalysisPipelineTask;

public class ActionPerformAnalysisLocally extends AbstractPhenotypeAnalysisAction {
	
	private IniIoProvider iniIO;
	private SystemOptions so;
	
	public ActionPerformAnalysisLocally(String tooltip) {
		super(tooltip);
	}
	
	public ActionPerformAnalysisLocally(IniIoProvider iniIO, ExperimentReferenceInterface experiment, MongoDB m) {
		this("Perform Analysis (local execution)");
		this.iniIO = iniIO;
		this.m = m;
		so = SystemOptions.getInstance(null, iniIO);
		this.experiment = experiment;
	}
	
	@Override
	public String getDefaultTooltip() {
		return so.getString("DESCRIPTION", "pipeline_description", "(no description specified)", true);
	}
	
	@Override
	public String getDefaultTitle() {
		if (so.isIniNull())
			return "Analysis pipeline not available";
		String vv = so.getString("DESCRIPTION", "tuned_for_IAP_version", "(unknown legacy IAP version)");
		String warning = ReleaseInfo.IAP_VERSION_STRING.equals(vv) ? "" :
				"<br><small><font color='red'>Settings not tested with IAP V" + ReleaseInfo.IAP_VERSION_STRING + "!</font>";
		
		return (warning.isEmpty() ? "" : "<html><center>") + "Process " + StringManipulationTools
				.removeHTMLtags(so.getString("DESCRIPTION", "pipeline_name", "(unnamed)", true)) + warning;
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
				so.getString("DESCRIPTION", "pipeline_description", "(no description specified)", true),
				null);
		return new UserDefinedImageAnalysisPipelineTask(pd);
	}
	
	@Override
	public boolean remotingEnabledForThisAction() {
		return false;
	}
}