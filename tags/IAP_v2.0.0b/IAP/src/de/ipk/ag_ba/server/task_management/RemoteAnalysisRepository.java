/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import org.graffiti.util.InstanceLoader;

/**
 * @author klukas
 */
public class RemoteAnalysisRepository {
	
	private static RemoteAnalysisRepository instance;
	
	private RemoteAnalysisRepository() {
		
	}
	
	public static RemoteAnalysisRepository getInstance() {
		if (instance == null)
			instance = new RemoteAnalysisRepository();
		return instance;
	}
	
	public RemoteCapableAnalysisAction getNewAnalysisAction(String analysisActionClassName)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
		if (analysisActionClassName != null) {
			Object o = Class.forName(analysisActionClassName, true, InstanceLoader.getCurrentLoader()).newInstance();
			RemoteCapableAnalysisAction res = (RemoteCapableAnalysisAction) o;
			return res;
		} else
			return null;
	}
	
}
