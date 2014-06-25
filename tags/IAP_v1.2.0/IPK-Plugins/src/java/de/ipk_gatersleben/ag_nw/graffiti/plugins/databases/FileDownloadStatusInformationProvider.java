/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases;

import javax.swing.JComponent;

public interface FileDownloadStatusInformationProvider {
	JComponent getStatusPane(boolean showEmpty);
	
	String getDescription();
	
	public void finishedNewDownload();
}
