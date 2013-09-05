/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.03.2007 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

public interface PaintStatusSupport {
	
	void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall optStatus);
	
	boolean statusDrawInProgress();
	
}
