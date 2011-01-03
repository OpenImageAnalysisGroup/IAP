/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

/**
 * @author klukas
 */
public abstract class AbstractImageAnalysisTask implements ImageAnalysisTask {
	
	@Override
	public void performAnalysis(int maximumThreadCount, BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(1, 1, status);
	}
}
