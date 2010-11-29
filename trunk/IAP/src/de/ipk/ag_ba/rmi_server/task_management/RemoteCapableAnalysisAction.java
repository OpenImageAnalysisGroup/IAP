/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;

/**
 * @author klukas
 */
public interface RemoteCapableAnalysisAction extends NavigationAction {
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver);

	public void setParams(ExperimentReference inputExperiment, String login, String pass, String params);

	public String getMongoDatasetID();
}
