package de.ipk.ag_ba.commands.experiment.process;

import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.ErrorMsg;
import org.ReleaseInfo;

import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.maize.AbstractPhenotypeAnalysisAction;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.UserDefinedImageAnalysisPipelineTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionPerformGridAnalysis extends AbstractPhenotypeAnalysisAction {
	private final PipelineDesc pd;
	private int numberOfJobs;
	
	public ActionPerformGridAnalysis() {
		super(null);
		pd = null;
	}
	
	public ActionPerformGridAnalysis(PipelineDesc pd, MongoDB m, ExperimentReferenceInterface experimentReference) {
		super(pd.getTooltip());
		this.pd = pd;
		this.m = m;
		this.experiment = experimentReference;
		this.experimentResult = null;
		if (experimentReference != null && experimentReference.getHeader() != null)
			this.mongoDatasetID = experimentReference.getHeader().getDatabaseId();
		int snapshotsPerJob = 100;
		this.numberOfJobs = experimentReference.getHeader().getNumberOfFiles() / 3 / snapshotsPerJob;
		try {
			ExperimentInterface e = experimentReference.getData();
			List<NumericMeasurementInterface> images = Substance3D.getAllFiles(e, MeasurementNodeType.IMAGE);
			TreeSet<String> ids = new TreeSet<String>();
			HashSet<Integer> days = new HashSet<Integer>();
			if (images != null) {
				for (NumericMeasurementInterface nmi : images) {
					if (nmi.getQualityAnnotation() != null && !nmi.getQualityAnnotation().isEmpty())
						ids.add(nmi.getQualityAnnotation());
					days.add(nmi.getParentSample().getTime());
				}
			}
			
			if (ids.size() > 1 && (days.size() / 3) > 0)
				this.numberOfJobs = ids.size() / (50 / (days.size() / 3));
			
			if (numberOfJobs > ids.size())
				numberOfJobs = ids.size();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@Override
	public ParameterOptions getParameters() {
		return new ParameterOptions("You may modify the number of submitted jobs:", new Object[] {
				"Job Count", numberOfJobs
		});
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		if (parameters != null && parameters.length > 0)
			numberOfJobs = (Integer) parameters[0];
	}
	
	@Override
	protected ImageAnalysisTask getImageAnalysisTask() {
		if (pd == null)
			return new UserDefinedImageAnalysisPipelineTask(
					new PipelineDesc(null, experiment.getIniIoProvider(), null, null, null));
		else
			return new UserDefinedImageAnalysisPipelineTask(pd);
	}
	
	@Override
	public String getDefaultImage() {
		if (m == null)
			return "img/ext/gpl2/Gnome-Text-X-Script-64.png";
		else
			return "img/ext/network-workgroup-power.png";
	}
	
	@Override
	public String getDefaultTitle() {
		if (m == null)
			return "Analysis Job";
		else {
			String warning = ReleaseInfo.IAP_VERSION_STRING.equals("" + pd.getTestedIAPversion()) ? "" :
					"<br><small><font color='red'>Settings not tested with IAP V" + ReleaseInfo.IAP_VERSION_STRING + "!";
			return "<html><center>Submit " + numberOfJobs + " analysis jobs to " + (m != null ? m.getDatabaseName() : "(database instance is null)") + warning;
		}
	}
	
	@Override
	public int getCpuTargetUtilization() {
		return 1;
	}
	
	@Override
	public int getNumberOfJobs() {
		return numberOfJobs;
	}
	
}