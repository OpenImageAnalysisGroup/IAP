/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.util.Date;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;

/**
 * @author klukas
 */
public interface RemoteCapableAnalysisAction extends NavigationAction {
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver,
			Date optProcessOnlySampleDataNewerThanThisDate);
	
	public void setParams(ExperimentReferenceInterface inputExperiment, MongoDB m, String params);
	
	public String getDatasetID();
	
	public MongoDB getMongoDB();
	
	/**
	 * @return 1, if this task utilizes 1 CPU, 2, if this tasks creates two concurrent subtasks.
	 */
	public int getCpuTargetUtilization();
	
	public int getNumberOfJobs();
	
	boolean remotingEnabledForThisAction();
	
}
