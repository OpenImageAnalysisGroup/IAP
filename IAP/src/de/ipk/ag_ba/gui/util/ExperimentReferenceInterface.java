package de.ipk.ag_ba.gui.util;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.IniIoProvider;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

public interface ExperimentReferenceInterface {
	
	public abstract ExperimentInterface getData() throws Exception;
	
	public abstract ExperimentInterface getExperiment();
	
	public abstract ExperimentInterface getData(
			boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception;
	
	public abstract String getExperimentName();
	
	public abstract void setExperimentData(ExperimentInterface data);
	
	public abstract ExperimentHeaderInterface getHeader();
	
	public abstract ExperimentInterface getData(BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception;
	
	public abstract void loadDataInBackground(BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception;
	
	public abstract void runAsDataBecomesAvailable(Runnable r);
	
	public abstract ExperimentInterface getExperimentPeek();
	
	public abstract IniIoProvider getIniIoProvider();
	
	public abstract void setIniIoProvider(IniIoProvider iniProvider);
	
	public abstract ExperimentIoHelper getIoHelper();
	
	public abstract void resetStoredHeader();
	
	public abstract void setHeader(ExperimentHeaderInterface header);
	
	public abstract MongoDB getM();
	
	public abstract void setM(MongoDB m);

	public abstract String getDatabaseName();
	
}