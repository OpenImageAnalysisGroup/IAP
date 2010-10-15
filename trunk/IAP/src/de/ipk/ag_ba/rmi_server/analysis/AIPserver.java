/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

/**
 * @author klukas
 *
 */
public class AIPserver {
	private IOmodule io = new IOmodule();
	private IAmodule ia = new IAmodule();
	
	
	public ServerInfo getServerInfo() {
		ServerInfo info = new ServerInfo();
		info.getInfoFrom(io, ia);
		return info;
	}
}
