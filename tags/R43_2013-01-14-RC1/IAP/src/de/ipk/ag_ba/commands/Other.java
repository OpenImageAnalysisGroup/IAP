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

import javax.swing.ImageIcon;
import javax.swing.Timer;

import org.ObjectRef;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.commands.experiment.ActionShowDataWithinVANTED;
import de.ipk.ag_ba.gui.calendar.NavigationButtonCalendar2;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;

/**
 * @author klukas
 */
public class Other {
	
	public static ArrayList<NavigationButton> getProcessExperimentDataWithVantedEntities(final MongoDB m, final ExperimentReference experimentName,
			GUIsetting guIsetting) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		
		ArrayList<AbstractExperimentDataProcessor> validProcessors = new ArrayList<AbstractExperimentDataProcessor>();
		ArrayList<AbstractExperimentDataProcessor> optIgnoredProcessors = null;
		for (ExperimentDataProcessor ep : ExperimentDataProcessingManager.getExperimentDataProcessors())
			// check if ep is not ignored
			if (optIgnoredProcessors == null || !optIgnoredProcessors.contains(ep.getClass())) {
				validProcessors.add((AbstractExperimentDataProcessor) ep);
			}
		
		for (Object o : validProcessors) {
			final AbstractExperimentDataProcessor pp = (AbstractExperimentDataProcessor) o;
			NavigationAction action = new ActionShowDataWithinVANTED("Analyze Data", pp, m, experimentName);
			NavigationButton ne = new NavigationButton(action, pp.getShortName(), "img/vanted1_0.png",
					guIsetting);
			
			ImageIcon i = pp.getIcon();
			if (i != null) {
				i = new ImageIcon(GravistoService.getScaledImage(i.getImage(), -48, 48));
				ne.setIcon(i, "img/vanted1_0.png");
			}
			
			result.add(ne);
		}
		
		return result;
	}
	
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
		
		final ObjectRef refCalEnt = new ObjectRef();
		final ObjectRef refCalGui = new ObjectRef();
		
		NavigationAction calendarAction = new ActionCalendar(refCalEnt, refCalGui, group2ei, m);
		NavigationButtonCalendar2 calendarEntity = new NavigationButtonCalendar2("Calendar", "img/ext/calendar48.png", calendarAction, guiSettings);
		calendarEntity.setShowSpecificDay(true);
		refCalEnt.setObject(calendarEntity);
		return calendarEntity;
	}
	
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
