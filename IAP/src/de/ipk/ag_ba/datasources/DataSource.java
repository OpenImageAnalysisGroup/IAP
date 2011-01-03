/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources;

import org.graffiti.plugin.io.resources.ResourceIOHandler;

/**
 * @author klukas
 */
public interface DataSource extends ResourceIOHandler, DataSourceLevel {
	
	public void setLogin(String login, String password);
	
	/**
	 * Used to prepare calls to any methods that need to return data from the
	 * data source.
	 * 
	 * @throws Exception
	 */
	public void readDataSource() throws Exception;
}
