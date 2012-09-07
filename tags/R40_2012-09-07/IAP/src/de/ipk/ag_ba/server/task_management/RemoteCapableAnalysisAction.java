/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;

/**
 * @author klukas
 */
public interface RemoteCapableAnalysisAction extends NavigationAction {
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver);
	
	public void setParams(ExperimentReference inputExperiment, MongoDB m, String params);
	
	public String getDatasetID();
	
	public MongoDB getMongoDB();
	
	/**
	 * @return 1, if this task utilizes 1 CPU, 2, if this tasks creates two concurrent subtasks.
	 */
	public int getCpuTargetUtilization();
	
	public int getNumberOfJobs();
	
	/**
	 * @return A version tag, used to ensure that the remote action is executed only with the current version of the code.
	 */
	public IAP_RELEASE getVersionTag();
}
