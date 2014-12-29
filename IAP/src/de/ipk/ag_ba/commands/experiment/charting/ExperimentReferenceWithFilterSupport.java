package de.ipk.ag_ba.commands.experiment.charting;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.data_transformation.DataTable;
import de.ipk.ag_ba.data_transformation.loader.DataTableLoader;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ExperimentReferenceWithFilterSupport extends ExperimentReference {
	
	private DataTable data_table;
	
	ExperimentInterface eWithoutData = null;
	
	private boolean removeOutliers;
	
	private String groupFilter;
	
	public ExperimentReferenceWithFilterSupport(ExperimentHeaderInterface ehi, MongoDB m) {
		super(ehi, m);
	}
	
	public ExperimentReferenceWithFilterSupport(ExperimentInterface experiment, BackgroundTaskStatusProviderSupportingExternalCall sp,
			String groupFilter) throws Exception {
		super(experiment);
		this.groupFilter = groupFilter;
		
		if (this.experiment != null)
			this.data_table = new DataTableLoader().loadFromExperiment(this.experiment);
	}
	
	private ExperimentInterface filterExpSub(ExperimentInterface e) {
		if (groupFilter == null)
			return e;
		else {
			if (experiment != null)
				for (SubstanceInterface si : experiment) {
					if (groupFilter.equals(si.getName())) {
						return new Experiment(si.clone());
					}
				}
			return experiment;
		}
	}
	
	public DataTable getDataTable() throws Exception {
		if (data_table == null) {
			ExperimentInterface ee = getData();
			if (ee != null)
				data_table = new DataTableLoader().loadFromExperiment(ee);
		}
		return data_table;
	}
	
	public void setRemoveDefinedOutliers(boolean removeOutliers) {
		if (removeOutliers) {
			ExperimentInterface eclone = experiment.clone();
			IAPservice.removeOutliers(eclone);
			eWithoutData = eclone;
		} else
			eWithoutData = null;
		this.removeOutliers = removeOutliers;
	}
	
	@Override
	public ExperimentInterface getData() throws Exception {
		if (removeOutliers)
			return eWithoutData;
		else
			return super.getData();
	}
	
	@Override
	public ExperimentInterface getData(boolean interactiveGetExperimentSize, BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (removeOutliers)
			return eWithoutData;
		else
			return super.getData(interactiveGetExperimentSize, status);
	}
	
	@Override
	public ExperimentInterface getData(BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (removeOutliers)
			return eWithoutData;
		else
			return super.getData(status);
	}
	
	public static ExperimentReferenceWithFilterSupport tryGetFilteredDataset(ExperimentReferenceInterface experiment, String groupFilter2,
			BackgroundTaskStatusProviderSupportingExternalCall sp) throws Exception {
		if (experiment != null && groupFilter2 != null)
			for (SubstanceInterface si : experiment.getData()) {
				if (groupFilter2.equals(si.getName())) {
					ExperimentInterface ne = Experiment.copyAndExtractSubtanceInclusiveData(si);
					return new ExperimentReferenceWithFilterSupport(ne, sp, groupFilter2);
				}
			}
		return null;
	}
}
