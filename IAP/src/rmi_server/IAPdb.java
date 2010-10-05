/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jul 8, 2010 by Christian Klukas
 */
package rmi_server;

import java.util.Collection;

import com.healthmarketscience.rmiio.RemoteInputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;

/**
 * @author klukas
 */
public interface IAPdb extends java.rmi.Remote {
	public Collection<ExperimentInfo> getExperiments(
			String login, String pass, boolean fromOwnerTruefromOthersFalse)
		throws java.rmi.RemoteException;
	
	public Collection<FileInfo> getExperimentFiles(
			String login, String pass, String experimentName)
		throws java.rmi.RemoteException;
	
	public Experiment getExperiment(
			String login, String pass, String experimentName)
		throws java.rmi.RemoteException;
	
	public void storeExperiment(
			String login, String pass, String userGroup, Experiment experiment)
		throws java.rmi.RemoteException;
	
	public RemoteInputStream getImageFile(
			String login, String pass, String md5, boolean returnPreview) 
		throws java.rmi.RemoteException;
}
