/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_model;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.BackgroundTaskStatusProvider;

import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.StyleAware;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.ProgressStatusService;

/**
 * @author klukas
 * 
 */
public class NavigationGraphicalEntity implements StyleAware {

	public String title;
	private String navigationImage, actionImage;
	private NavigationAction action;
	private String tooltipHint;
	private JComponent gui;
	private boolean processing;

	private long processingStart = 0;
	private JComponent sideGui;
	private double sideGuiSpace;
	private double sideGuiWidth;
	private ImageIcon icon;
	private boolean providesActions = true;

	protected Runnable optFinishAction = null;
	private Runnable execution;
	private boolean rightAligned;
	private ProgressStatusService statusServer;

	public NavigationGraphicalEntity(NavigationAction navigationAction) {
		this.title = navigationAction.getDefaultTitle();
		this.navigationImage = navigationAction.getDefaultNavigationImage();
		this.actionImage = navigationAction.getDefaultImage();
		this.action = navigationAction;
	}

	public NavigationGraphicalEntity(NavigationAction navigationAction, String title, String image) {
		this.title = title;
		this.navigationImage = image;
		this.actionImage = image;
		this.action = navigationAction;
	}

	public NavigationGraphicalEntity(NavigationAction navigationAction, String title, String navigationImage,
			String actionImage) {
		this.title = title;
		this.navigationImage = navigationImage;
		this.actionImage = actionImage;
		this.action = navigationAction;
	}

	public NavigationGraphicalEntity(JComponent gui) {
		this.gui = gui;
	}

	public JComponent getGUI() {
		return gui;
	}

	public void setToolTipText(String hint) {
		this.tooltipHint = hint;
	}

	public String getNavigationImage() {
		return navigationImage;
	}

	public String getActionImage() {
		return actionImage;
	}

	public String getTitle() {
		long compTime = System.currentTimeMillis() - processingStart;
		if (!processing || compTime < 1000)
			return title;
		else {
			String dots = "";
			int ndots = (int) ((compTime % 1000) / 250);
			String cc = "";
			if (ndots == 0)
				cc = "\\";
			if (ndots == 1)
				cc = "|";
			if (ndots == 2)
				cc = "/";
			if (ndots == 3)
				cc = "-";

			while (dots.length() < 1)
				dots += cc;

			String progress = "";
			String s = "";
			if (action.getStatusProvider().getCurrentStatusValueFine() > 0) {
				if (action.getStatusProvider().getCurrentStatusValueFine() > 0)
					progress = "" + action.getStatusProvider().getCurrentStatusValue() + "%";
				while (progress.length() < 5)
					progress = "_" + progress;
				progress = "";

				s = "<br>";
				int len = (dots + " " + title + progress).length();
				s += getProgress("-", ".", len + 5, action.getStatusProvider().getCurrentStatusValueFine());
			}
			String line2 = "";
			if (action.getStatusProvider() != null && action.getStatusProvider().getCurrentStatusMessage1() != null
					&& action.getStatusProvider().getCurrentStatusMessage1().length() > 0)
				line2 = action.getStatusProvider().getCurrentStatusMessage1();
			if (statusServer != null) {
				BackgroundTaskStatusProvider status = action.getStatusProvider();
				String eta = statusServer.getRemainTime(status.getCurrentStatusValue() == -1, status
						.getCurrentStatusValueFine());
				if (eta.length() > 0) {
					if (line2.length() > 0)
						line2 += ", ";
					line2 += "ETA " + eta;
				}
			}
			if (line2.length() > 0)
				line2 = "<br>" + line2;
			return "<html><b><code><center>" + dots + " " + title + progress + " " + dots + s + line2;
		}
	}

	private String getProgress(String c, String o, int len, double currentStatusValue) {
		StringBuilder s = new StringBuilder();
		while (s.length() < len) {
			boolean inProgress = s.length() * 100d / len <= currentStatusValue;
			if (inProgress)
				s.append(c);
			else
				s.append(o);
		}
		return s.toString();
	}

	public NavigationAction getAction() {
		return action;
	}

	public String getToolTip() {
		if (tooltipHint != null)
			return tooltipHint;
		else
			return action.getDefaultTooltip();
	}

	public void setProcessing(boolean b) {
		this.processing = b;
		this.processingStart = System.currentTimeMillis();
		statusServer = new ProgressStatusService();
	}

	public boolean isProcessing() {
		return processing;
	}

	public void setSideGUI(JComponent sideGui, double sideGuiSpace, double sideGuiWidth) {
		this.sideGui = sideGui;
		this.sideGuiSpace = sideGuiSpace;
		this.sideGuiWidth = sideGuiWidth;
	}

	public JComponent getSideGui() {
		return sideGui;
	}

	public double getSideGuiSpace() {
		return sideGuiSpace;
	}

	public double getSideGuiWidth() {
		return sideGuiWidth;
	}

	public void setIcon(ImageIcon i) {
		this.icon = i;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public void setProvidesActions(boolean b) {
		this.providesActions = b;
	}

	public boolean willProvideActions() {
		return providesActions;
	}

	public void performAction() {
		if (execution != null)
			execution.run();
	}

	public Runnable getExecution() {
		return execution;
	}

	public void setExecution(Runnable execution) {
		this.execution = execution;
	}

	public void setRightAligned(boolean b) {
		this.rightAligned = b;
	}

	public boolean isRightAligned() {
		return rightAligned;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setButtonStyle(ButtonDrawStyle style) {
		// empty, override if needed
	}
}
