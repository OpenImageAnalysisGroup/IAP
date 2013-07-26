/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 23, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.Timer;

import org.ObjectRef;

import de.ipk.ag_ba.gui.calendar.NavigationButtonCalendar2;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class Other {
	
	public static NavigationButton getServerStatusEntity(GUIsetting guIsetting) {
		return getServerStatusEntity("System Status", guIsetting);
	}
	
	public static Timer globalScreenshotTimer = null;
	
	public static NavigationButton getServerStatusEntity(String title,
			GUIsetting guIsetting) {
		NavigationAction serverStatusAction = new ActionSystemStatus("Check service availability");
		NavigationButton serverStatusEntity = new NavigationButton(serverStatusAction, title,
				IAPimages.getCheckstatus(),
				guIsetting);
		return serverStatusEntity;
	}
	
	public static NavigationButton getCalendarEntity(
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, GUIsetting guiSettings) {
		return getCalendarEntity(group2ei, m, guiSettings, true);
	}
	
	public static NavigationButton getCalendarEntity(
			final TreeMap<String, TreeMap<String, ArrayList<ExperimentHeaderInterface>>> group2ei, final MongoDB m, GUIsetting guiSettings,
			boolean showOnlySpecificDay) {
		
		final ObjectRef refCalEnt = new ObjectRef();
		final ObjectRef refCalGui = new ObjectRef();
		
		NavigationAction calendarAction = new ActionCalendar(refCalEnt, refCalGui, group2ei, m);
		NavigationButtonCalendar2 calendarEntity = new NavigationButtonCalendar2("Calendar", "img/ext/calendar48.png", calendarAction, guiSettings);
		calendarEntity.setShowSpecificDay(showOnlySpecificDay);
		refCalEnt.setObject(calendarEntity);
		return calendarEntity;
	}
	
	@SuppressWarnings("unused")
	private static void checkServerAvailabilityByPing(HashMap<String, ArrayList<String>> infoset, String name,
			String role, String host) {
		infoset.put(name, new ArrayList<String>());
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			boolean reachable = address.isReachable(1000);
			if (!reachable)
				throw new Exception("Host is not reachable within time limit of one second.");
			infoset.get(name).add("<h2>" + role + "</h2><hr><br>" + "" + "The " + role + " is powered on.");
			infoset.get(name).add("<br><b>Status result check: OK</b>");
			
		} catch (Exception e1) {
			infoset.get(name).add(
					"<h2>" + role + "</h2><hr><br>" + "" + "The " + role + " was not reachable within time limits.<br>"
							+ "The cause may be internet connectivity problems or server side<br>"
							+ "problems which may take some time to be corrected.<br><br>"
							+ "The availability of this server is monitored automatically.<br>"
							+ "Effort will be put on improving reliability of the service.<br>");
			infoset.get(name).add("Error-Details: " + e1.toString());
			infoset.get(name).add("<br><b>Status result check: ERROR</b>");
			
		}
	}
}
