/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 27, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.webstart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import de.ipk.ag_ba.commands.ActionHome;
import de.ipk.ag_ba.commands.JLabelUpdateReady;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.plugins.vanted_vfs.NavigationButtonFilter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.network.TabAglet;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import info.clearthought.layout.TableLayout;

/**
 * @author klukas
 */
public class IAPgui {
	
	public static String optCustomIntroText = null;
	
	public static JComponent getMainGUIcontent(
			final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow,
			NavigationAction optCustomHomeAction) {
		return getMainGUIcontent(myStatus, secondWindow, optCustomHomeAction, null, true);
	}
	
	public static JComponent getMainGUIcontent(
			final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus,
			boolean secondWindow,
			NavigationAction optCustomHomeAction,
			NavigationButtonFilter optNavigationButtonFilter, boolean executeAction) {
			
		final JPanel graphPanel = new JPanel();
		
		graphPanel.setBackground(IAPnavigationPanel.getTabColor());
		graphPanel.setOpaque(true);
		graphPanel.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		final IAPnavigationPanel navigationPanel = new IAPnavigationPanel(PanelTarget.NAVIGATION, graphPanel, null);
		navigationPanel.setOpaque(false);
		
		navigationPanel.setDisallowBookmarkCreation(optCustomHomeAction != null);
		
		JPanel actionPanelRight = new JPanel();
		final IAPnavigationPanel actionPanel = new IAPnavigationPanel(PanelTarget.ACTION, graphPanel, actionPanelRight);
		actionPanel.setNavigationButtonFilter(optNavigationButtonFilter);
		actionPanel.setOpaque(false);
		
		navigationPanel.setTheOther(actionPanel);
		actionPanel.setTheOther(navigationPanel);
		
		GUIsetting guiSetting = new GUIsetting(navigationPanel, actionPanel, graphPanel);
		final NavigationAction home = optCustomHomeAction != null ? optCustomHomeAction : new ActionHome(myStatus, guiSetting);
		
		navigationPanel.setGuiSetting(guiSetting);
		actionPanel.setGuiSetting(guiSetting);
		
		final NavigationButton overView = new NavigationButton(home, guiSetting);
		
		final ArrayList<NavigationButton> homeNavigation = new ArrayList<NavigationButton>();
		try {
			if (executeAction) {
			home.performActionCalculateResults(overView);
			actionPanel.setEntitySet(home.getResultNewActionSet());
			} else
			actionPanel.setEntitySet(new ArrayList<>());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ArrayList<NavigationButton> set = home.getResultNewNavigationSet(homeNavigation);
		if (set != null)
			while (!set.isEmpty() && set.get(0) == null)
			set.remove(0);
		if (optCustomHomeAction != null && (set == null || set.isEmpty())) {
			set = new ArrayList<>();
			set.add(new NavigationButton(optCustomHomeAction, guiSetting));
		}
		navigationPanel.setEntitySet(set);
		
		JLabel lbl;
		if (!executeAction)
			lbl = new JLabel(optCustomIntroText);
		else
			if (!secondWindow && IAPmain.myClassKnown) {
			lbl = new JLabel("<html><h2><font color='red'>IAP Reloading Not Supported!</font></h2>"
					+ "<b>It is recommended to close any browser window and re-open this website,<br>"
					+ "otherwise this information system may not work reliable.</b><br><br>"
					+ "Technical background: reloading this applet is not yet supported");
			} else {
			lbl = new JLabel(getIntroTxt());
			}
		lbl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		graphPanel.add(new MainPanelComponent(lbl.getText()).getGUI(), "0,0");
		graphPanel.revalidate();
		
		TabAglet.getInstance();
		
		JLabelUpdateReady statusMessage = new JLabelUpdateReady() {
			@Override
			public void update() {
			
			}
		};
		statusMessage.setText("");
		
		statusMessage.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
		
		MainFrame.addMessageReceiver((msg) -> {
			if (!statusMessage.isDisplayable())
			throw new UnsupportedOperationException("Receiver is not valid any more");
			if (msg != null && msg.contains(":")) {
			String[] m = msg.split(":", 2);
			MessageType mt = MessageType.valueOf(m[0]);
			String n = m[1];
			SwingUtilities.invokeLater(() -> {
				switch (mt) {
					case ERROR:
						statusMessage.setText("<html><b>Error:</b> " + n);
						break;
					case INFO:
						statusMessage.setText("<html><b>Notification:</b> " + n);
						break;
					case PERMANENT_INFO:
						statusMessage.setText("<html><b>Info:</b> " + n);
						break;
					default:
						break;
				}
				statusMessage.validate();
			});
			}
		});
		
		JComponent res = TableLayout.get3SplitVertical(navigationPanel, TableLayout.getSplit(actionPanel,
			actionPanelRight, TableLayout.FILL, TableLayout.PREFERRED),
			TableLayout.getSplitVertical(graphPanel, statusMessage, TableLayout.FILL, TableLayout.PREFERRED),
			TableLayout.PREFERRED,
			TableLayout.PREFERRED, TableLayout.FILL);
			
		res.revalidate();
		if (!executeAction) {
			NavigationButton ab = set.iterator().next();
			BackgroundTaskHelper.executeLaterOnSwingTask(1, () -> {
			try {
				ab.performAction();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			});
		}
		return res;
	}
	
	public static String getIntroTxt() {
		if (optCustomIntroText != null)
			return optCustomIntroText;
		try {
			return "<html><h2><font face='Arial'>Welcome to IAP - The Integrated Analysis Platform! <small>V" + ReleaseInfo.IAP_VERSION_STRING
				+ "</small></font></h2>"
				+ "<font face='Arial'>The Integrated Analysis Platform IAP is a systems biology cloud storage, analysis and visualization system. "
				+ "It is focused on high-throughput plant phenotyping.<br>"
				+ "<br>"
				+ "You find information on how to use this software and additional reference information by clicking the command button 'About'.<br>"
				+ "Use the first row of buttons to go back to any previously selected command or to return to this 'Start'-screen." +
				new LogService().getLatestNews(SystemOptions.getInstance().getInteger("NEWS", "show_n_items", 0),
						"<br>" +
								"<p>Latest user-posted news:<br><br><ul>",
						"<br>" +
								"<p>Latest system-messages:<br><br><ul>",
						"<li>", "", "</ul>")
				+ "";
		} catch (Exception e) {
			e.printStackTrace();
			return "<html><h2>Exception while getting intro-text: " + e.getMessage() + "</h2>";
		}
	}
	
	public static void navigateTo(final String target, NavigationButton src, NavigationAction optHomeAction) {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: NAVIGATE TO: " + target);
		assert src != null;
		assert src.getGUIsetting() != null;
		assert src.getGUIsetting().getNavigationPanel() != null;
		
		NavigationButton button = null;
		try {
			button = src.getGUIsetting().getNavigationPanel().getEntitySet(false).iterator().next();
		} catch (NoSuchElementException nse) {
			button = new NavigationButton(optHomeAction, src.getGUIsetting());
		}
		final IAPnavigationPanel navigationPanel = src.getGUIsetting().getNavigationPanel();
		final IAPnavigationPanel actionPanel = src.getGUIsetting().getActionPanel();
		final JComponent graphPanel = src.getGUIsetting().getGraphPanel();
		
		Runnable rrr = new Runnable() {
			@Override
			public void run() {
			navigateTo(target, navigationPanel, actionPanel, graphPanel);
			}
		};
		button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr, null);
	}
	
	private static void navigateTo(String target, final IAPnavigationPanel navigationPanel,
			final IAPnavigationPanel actionPanel, final JComponent graphPanel) {
			
		if (target == null || target.length() == 0)
			return;
		if (target.startsWith("#"))
			target = target.substring("#".length());
			
		if (target.startsWith("Overview"))
			target = target.substring("Overview".length());
		if (target.startsWith("IAP"))
			target = target.substring("IAP".length());
		if (target.startsWith("Start"))
			target = target.substring("Start".length());
		if (target.startsWith("."))
			target = target.substring(".".length());
			
		HashMap<String, NavigationButton> knownEntities = new HashMap<String, NavigationButton>();
		
		for (NavigationButton ne : actionPanel.getEntitySet(target.length() > 0)) {
			knownEntities.put(IAPnavigationPanel.replaceBadChars(ne.getTitle()), ne);
			if (ne.getTitle().contains("(")) {
			String t = IAPnavigationPanel.replaceBadChars(ne.getTitle().substring(0, ne.getTitle().lastIndexOf("(")).trim());
			System.out.println(t);
			knownEntities.put(t, ne);
			}
		}
		String thisTarget = target.split("\\.", 2)[0];
		if (thisTarget != null && thisTarget.length() > 0) {
			String nextTarget = target.length() - thisTarget.length() > 1 ? target.substring(thisTarget.length()
				+ ".".length()) : "";
			// nextTarget = IAPnavigationPanel.replaceBadChars(nextTarget);
			NavigationButton button = knownEntities.get(thisTarget);
			if (button == null && (thisTarget.contains("_") || thisTarget.contains("("))) {
			String tt = thisTarget.contains("(") ? thisTarget.substring(0, thisTarget.indexOf("(")).trim() : thisTarget.trim();
			button = knownEntities.get(tt);
			tt = StringManipulationTools.stringReplace(tt, "_", ".");
			if (button == null)
				button = knownEntities.get(tt);
			}
			
			if (button == null)
			System.out.println("Upcoming problem...");
			if (button != null) {
			final String nt = nextTarget;
			Runnable rrr = new Runnable() {
				@Override
				public void run() {
					if (nt.length() > 0) {
						navigateTo(nt, navigationPanel, actionPanel, graphPanel);
					}
				}
			};
			button.executeNavigation(PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, rrr, null);
			} else {
			System.out.println("WARNING: Could not find target action: " + thisTarget);
			// navigationPanel.getEntitySet(false).iterator().next().executeNavigation(
			// PanelTarget.ACTION, navigationPanel, actionPanel, graphPanel, null, null);
			}
		}
	}
}
