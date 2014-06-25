/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.iharder.dnd.FileDrop;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.HelperClass;
import org.HomeFolder;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.SettingsHelperDefaultIsTrue;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.inspector.ContainsTabbedPane;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.modes.defaults.MegaTools;

import scenario.Scenario;
import scenario.ScenarioGui;
import scenario.ScenarioService;
import bsh.Interpreter;
import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info.PluginInfoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class WorkflowHelper extends InspectorTab implements ScenarioGui, ContainsTabbedPane, HelperClass {
	private static final long serialVersionUID = 1L;
	
	private static WorkflowHelper instance = null;
	
	public JTabbedPane hc = new JTabbedPane();
	
	private FolderPanel library;
	
	NewsHelper nh;
	
	private JLabel recordStatus;
	
	private JCheckBox keggEnabler;
	
	public WorkflowHelper() {
		super();
		instance = this;
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			this.title = "News";
		else
			this.title = "Help";
		nh = new NewsHelper(this);
		
		if (!SystemAnalysis.isHeadless())
			initComponents();
	}
	
	private void initComponents() {
		double[][] sizeM = { { TableLayoutConstants.FILL }, // Columns
				{ TableLayoutConstants.FILL } }; // Rows
		
		setLayout(new TableLayout(sizeM));
		setBackground(null);
		setOpaque(false);
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			add(nh.getNews((JTabbedPane) getParent()), "0,0");
		} else {
			try {
				hc.addTab("News", nh.getNews(hc));
				if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
					hc.addTab("Workflow", getWorkFlowHelp());
				hc.addTab("Examples", new TabExampleFiles());
				hc.addTab("Settings", getSettings());
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.MacroRecorder))
				hc.addTab("Macros", getScenarioControl());
			hc.validate();
			add(hc, "0,0");
		}
		validate();
	}
	
	private JPanel getSettings() {
		final JPanel res = new JPanel();
		res.setBackground(null);
		res.setOpaque(false);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
						TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 2 * border,
						TableLayoutConstants.PREFERRED, 2 * border, TableLayoutConstants.PREFERRED, 5 * border,
						TableLayoutConstants.PREFERRED, border } }; // Rows
		res.setLayout(new TableLayout(size));
		
		JCheckBox helpEnabler = new JCheckBox("<html><font color='gray'>Help Functions (not yet available)");
		
		final JComboBox lookSelection = new JComboBox();
		lookSelection.setOpaque(false);
		final JMButton saveLook = new JMButton("Save");
		saveLook.setEnabled(false);
		
		// // windows styles
		// if (AttributeHelper.windowsRunning()) {
		// String[] val = new String[] {
		// "org.fife.plaf.Office2003.Office2003LookAndFeel",
		// "org.fife.plaf.OfficeXP.OfficeXPLookAndFeel",
		// "org.fife.plaf.VisualStudio2005.VisualStudio2005LookAndFeel" };
		// String[] desc = new String[] { "Office 2003 Style", "Office XP Style",
		// "VisualStudio 2005 Style" };
		// int i = 0;
		// for (String v : val)
		// lookSelection.addItem(new LookAndFeelWrapper(new
		// LookAndFeelInfo(desc[i], v)));
		// i++;
		// }
		
		ThemedLookAndFeelInfo info = new ThemedLookAndFeelInfo("VANTED", "de.muntjak.tinylookandfeel.TinyLookAndFeel",
				"VANTED");
		lookSelection.addItem(new LookAndFeelWrapper(info));
		
		try {
			LookAndFeelWrapper avtiveLaF = null;
			String sel = UIManager.getLookAndFeel().getClass().getCanonicalName();
			for (LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
				LookAndFeelWrapper d = new LookAndFeelWrapper(lafi);
				if (d.getClassName().equals(sel))
					avtiveLaF = d;
				if (d.isValid() && !d.getName().equals("TinyLookAndFeel"))
					lookSelection.addItem(d);
			}
			if (avtiveLaF != null)
				lookSelection.setSelectedItem(avtiveLaF);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		lookSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						LookAndFeelWrapper po = (LookAndFeelWrapper) lookSelection.getSelectedItem();
						if (po == null)
							return;
						
						po.activateTheme();
						
						try {
							UIManager.setLookAndFeel(po.getClassName());
							if (ReleaseInfo.isRunningAsApplet())
								SwingUtilities.updateComponentTreeUI(ReleaseInfo.getApplet());
							else
								SwingUtilities.updateComponentTreeUI(MainFrame.getInstance());
							MainFrame.getInstance().repaint();
							saveLook.setEnabled(true);
							saveLook.setText("Save");
							saveLook.requestFocus();
						} catch (Exception err) {
							saveLook.setEnabled(false);
							saveLook.setText("Error");
							ErrorMsg.addErrorMessage(err);
						} catch (Error e) {
							saveLook.setEnabled(false);
							saveLook.setText("N/A");
						}
					}
				});
			}
		});
		
		saveLook.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LookAndFeelWrapper op = (LookAndFeelWrapper) lookSelection.getSelectedItem();
				if (op == null)
					return;
				
				try {
					SystemOptions.getInstance().setString("VANTED", "LnF", op.getClassName());
					saveLook.setText("saved");
					saveLook.setEnabled(false);
					lookSelection.requestFocus();
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		
		helpEnabler.addActionListener(getHelpEnabledSettingActionListener(helpEnabler));
		helpEnabler.setOpaque(false);
		boolean auto = ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp);
		helpEnabler.setSelected(auto);
		res.add(TableLayout.get3Split(new JLabel("Look and feel: "), lookSelection, saveLook, TableLayout.PREFERRED,
				TableLayout.FILL, TableLayout.PREFERRED), "1,1");
		
		keggEnabler = new JCheckBox("KEGG access");
		keggEnabler.setOpaque(false);
		try {
			if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
				keggEnabler.setSelected(true);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		keggEnabler.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (keggEnabler.isSelected()) {
					keggEnabler.setSelected(false);
					if (Main.doEnableKEGGaskUser()) {
						try {
							new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").createNewFile();
						} catch (IOException e) {
							ErrorMsg.addErrorMessage(e);
						}
						try {
							if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").exists())
								new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").delete();
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					} else {
						try {
							new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
						try {
							if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
								new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").delete();
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
				} else {
					try {
						new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_rejected").createNewFile();
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					try {
						if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
							new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").delete();
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				try {
					if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "license_kegg_accepted").exists())
						keggEnabler.setSelected(true);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		});
		
		// helpEnabler
		res.add(getPluginConfigurationPanel(null, keggEnabler), "1,3");
		res.add(TableLayout.get3Split(getAddOnManagerButton(), null, getPreferencesFolderButton(), TableLayout.FILL, 4,
				TableLayout.FILL, 0, 0), "1,5");
		
		res.add(TableLayout.getSplit(getGridSettingEditor(), null, TableLayout.FILL, TableLayout.PREFERRED), "1,7");
		
		res.add(TableLayout.getSplit(null, null, TableLayout.PREFERRED, TableLayout.FILL), "1,9");
		
		res.add(new JLabel("<html>"
				+ "<font color='#BB22222'>After restarting the program the changed settings will be fully active."), "1,11");
		
		// final JLabel memLabel = GravistoService.getMemoryInfoLabel(false);
		// res.add(memLabel, "1,13");
		return res;
	}
	
	private JComponent getGridSettingEditor() {
		FolderPanel settings = new FolderPanel("Graph-View Settings", false, false, false, null);
		boolean enabled = new SettingsHelperDefaultIsTrue().isEnabled("graph_view_grid");
		MegaMoveTool.gridEnabled = enabled;
		
		Runnable enableGrid = new Runnable() {
			@Override
			public void run() {
				MegaMoveTool.gridEnabled = true;
			}
			
		};
		Runnable disableGrid = new Runnable() {
			@Override
			public void run() {
				MegaMoveTool.gridEnabled = false;
			}
		};
		Runnable enableZoom = new Runnable() {
			@Override
			public void run() {
				MegaTools.MouseWheelZoomEnabled = true;
			}
			
		};
		Runnable disableZoom = new Runnable() {
			@Override
			public void run() {
				MegaTools.MouseWheelZoomEnabled = false;
			}
		};
		JComponent gridCheckBox = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor("Enable Grid",
				"graph_view_grid", enableGrid, disableGrid);
		settings.addGuiComponentRow(null, gridCheckBox, false);
		
		JComponent databaseCheckBox = new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(
				"Database-based node statusbar-infos", "grav_view_database_node_status", enableGrid, disableGrid);
		settings.addGuiComponentRow(null, databaseCheckBox, false);
		
		// Database-based node statusbar-infos
		
		JComponent zoomCheckBox;
		if (AttributeHelper.macOSrunning()) {
			zoomCheckBox = new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(
					"<html>Mouse Wheel Zoom<br>(disable to scroll instead)", "graph_view_wheel_zoom", enableZoom, disableZoom);
		} else {
			zoomCheckBox = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor(
					"<html>Mouse Wheel Zoom<br>(disable to scroll instead)", "graph_view_wheel_zoom", enableZoom, disableZoom);
		}
		settings.addGuiComponentRow(null, zoomCheckBox, false);
		// settings.addGuiComponentRow(new JLabel("Move nodes/bends"), gridSize,
		// false);
		// settings.addGuiComponentRow(new JLabel("Resize small nodes"),
		// gridSizeSmall, false);
		// settings.addGuiComponentRow(new JLabel("Resize normal nodes"),
		// gridSizeNormal, false);
		// settings.addGuiComponentRow(new JLabel("Resize large nodes"),
		// gridSizeLarge, false);
		settings.layoutRows();
		return settings;
	}
	
	public static JButton getAddOnManagerButton() {
		final JButton result = new JMButton("<html>Install / Configure Add-ons");
		result.setIcon(GenericPluginAdapter.getAddonIcon());
		if (AddonManagerPlugin.getInstance() == null) {
			result.setEnabled(false);
			result.setText("<html>" + result.getText() + "<br>(Add-on manager disabled)<br>&nbsp;");
		}
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddonManagerPlugin p = AddonManagerPlugin.getInstance();
				if (p == null)
					MainFrame.showMessageDialog("Addon-Manager Plugin not loaded on startup. Please restart application.",
							"Internal Error");
				else
					p.showManageAddonDialog();
			}
		});
		
		final String oldText = result.getText();
		
		FileDrop.Listener fdl = new FileDrop.Listener() {
			@Override
			public void filesDropped(File[] files) {
				if (files != null && files.length > 0) {
					for (File f : files)
						if (!f.getName().toLowerCase().endsWith(".jar")) {
							result.setText("<html>Some Files are not a valid Add-on!");
							Timer t = new Timer(5000, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									result.setText(oldText);
								}
							});
							t.start();
							break;
						}
					if (AddonManagerPlugin.getInstance() == null) {
						MainFrame.showMessageDialog("Addon-Manager Plugin not loaded.", "Could not install Add-on(s)");
					} else
						AddonManagerPlugin.getInstance().process(Arrays.asList(files));
				}
			}
		};
		
		Runnable dragdetected = new Runnable() {
			@Override
			public void run() {
				result.setText("<html><br><b>Drop file to install Add-on<br><br>");
			}
		};
		
		Runnable dragenddetected = new Runnable() {
			@Override
			public void run() {
				if (!result.getText().contains("!"))
					result.setText(oldText);
			}
		};
		new FileDrop(null, result, null, false, fdl, dragdetected, dragenddetected);
		
		return result;
	}
	
	private JButton getPreferencesFolderButton() {
		JButton result = new JMButton("<html>Show Preferences Folder");
		
		result.setOpaque(false);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPreferencesFolder();
			}
		});
		return result;
	}
	
	private JComponent getPluginConfigurationPanel(final JComponent additionalSetting1,
			final JComponent additionalSetting2) {
		
		JLabel button = new JLabel("Loading of optional program features (");
		JLabel bt3 = new JLabel("):");
		JLabelHTMLlink bt2 = new JLabelHTMLlink("Reset", "Resets all settings to their default state. Restart needed!",
				new Runnable() {
					@Override
					public void run() {
						if (SettingsHelperDefaultIsTrue.oldStyle) {
							FilenameFilter filter = new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) {
									return name.startsWith("feature_disabled") || name.startsWith("feature_enabled");
								}
							};
							for (String settingsFile : new File(HomeFolder.getHomeFolder()).list(filter))
								new File(HomeFolder.getHomeFolder() + "/" + settingsFile).delete();
						} else {
							SettingsHelperDefaultIsTrue.resetAllKnown();
							SettingsHelperDefaultIsFalse.resetAllKnown();
						}
					}
				});
		
		final FolderPanel features = new FolderPanel(null, false, false, false, null);
		features.setBackground(null);
		features.setFrameColor(null, null, 0, 5);
		ErrorMsg.addOnAppLoadingFinishedAction(new Runnable() {
			@Override
			public void run() {
				features.clearGuiComponentList();
				if (additionalSetting1 != null)
					features.addGuiComponentRow(null, additionalSetting1, false);
				if (additionalSetting2 != null)
					features.addGuiComponentRow(null, additionalSetting2, false);
				for (JComponent jc : getOptionalSettingsPanels()) {
					features.addGuiComponentRow(null, jc, false);
				}
				features.layoutRows();
			}
		});
		return TableLayout.getSplitVertical(TableLayout.get3Split(button, bt2, bt3, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.PREFERRED), features, TableLayout.PREFERRED, TableLayout.PREFERRED);
	}
	
	private Collection<JComponent> getOptionalSettingsPanels() {
		Collection<JComponent> result = new ArrayList<JComponent>();
		for (PluginEntry pe : MainFrame.getInstance().getPluginManager().getPluginEntries()) {
			if (pe.getDescription().isOptional()) {
				result.add(TableLayout.getSplit(getEnableDisableOption(pe), new JLabel(""), TableLayout.PREFERRED,
						TableLayout.FILL));
			}
		}
		return result;
	}
	
	private JComponent getEnableDisableOption(final PluginEntry pe) {
		JComponent setting;
		if (pe.getDescription().isOptionalDefaultTrue())
			setting = new SettingsHelperDefaultIsTrue().getBooleanSettingsEditor(pe.getDescription().getName(), pe
					.getDescription().getName(), null, null);
		else
			setting = new SettingsHelperDefaultIsFalse().getBooleanSettingsEditor(pe.getDescription().getName(), pe
					.getDescription().getName(), null, null);
		try {
			String desc = PluginInfoHelper.getSummaryInfo(false, pe.getDescription(), pe.getPlugin());
			setting.setToolTipText("<html>" + desc);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return setting;
	}
	
	public static void setSettings(boolean enable) {
		for (PluginEntry pe : MainFrame.getInstance().getPluginManager().getPluginEntries())
			if (pe.getDescription().isOptional()) {
				if (pe.getDescription().isOptionalDefaultTrue())
					new SettingsHelperDefaultIsTrue().setEnabled(pe.getDescription().getName(), enable);
				else
					new SettingsHelperDefaultIsFalse().setEnabled(pe.getDescription().getName(), enable);
			}
		
		if (enable != instance.keggEnabler.isSelected())
			instance.keggEnabler.doClick();
	}
	
	private ActionListener getHelpEnabledSettingActionListener(final JCheckBox helpEnabler) {
		ActionListener res = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (helpEnabler.isSelected())
						new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_help_enabled").createNewFile();
					else {
						new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_help_enabled").delete();
					}
				} catch (IOException err) {
					ErrorMsg.addErrorMessage(err);
				}
				
			}
		};
		return res;
	}
	
	private JPanel getWorkFlowHelp() {
		JPanel help1 = new JPanel();
		help1.setOpaque(false);
		help1.setBackground(null);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
						TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
						TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border } }; // Rows
		help1.setLayout(new TableLayout(size));
		
		FolderPanel intro = new FolderPanel("Introduction", false, true, false, null);
		intro.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		intro
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small>"
										+ "In this side panel you find an outline of a possible workflow for data analysis and visualization with this application.<br>"
										+ "Please click onto the heading of a workflow-step or this introduction to show or hide details. "
										+ "In case the desired worfklow step is still not fully visible, move the slider next to this side panel (left side) more to the left, or increase the window size.<br><br>"
										+ "<i>I hope you will enjoy the work with this program!</i> - C. Klukas")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("main"), Color.WHITE), false, 5);
		intro.layoutRows();
		
		/* new JLabel() */
		
		FolderPanel step1 = new FolderPanel("1. Create / Load Input Form", true, true, false, null);
		step1.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		step1
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small><ol>"
										+ "<li>Save a new input template using the &quot;Experiments&quot; side-panel and open it in MS Excel or another compatible spreadsheet application."
										+ "<li>Fill the input template as described in the help. Certain input fields need to contain data in a specific format, so be carefully and consult the linked documentation (? button). Later with your own existing templates this task will be much easier as when done for the first time."
										+ "<li>Load the template in the &quot;Experiments&quot side panel. In case of error, carefully check the input template."
										+ "<li>Compare the numer of datapoints with the number of datapoints shown in the appearing experiment tab. If not all data is loaded, check the input format."
										+ "</ol>Hint: For mapping data onto graph edges instead of graph nodes, specify target and end node of an edge in your input template as follows &quot;A^B&quot;. "
										+ "The mapping will then be performed onto the edge pointing from A to B. You could also add edge labels to the graph and then use these "
										+ "labels in the input forms.")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("inputformats"), Color.WHITE), false, 5);
		
		// JLabelJavaHelpLink.getHelpActionListener("inputformats"),
		// "inputformats"
		
		step1.layoutRows();
		
		FolderPanel step2 = new FolderPanel("2. Create / Load Pathway", true, true, false, null
				// JLabelJavaHelpLink.getHelpActionListener("inputformats"),
				// "inputformats"
				);
		step2.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		step2.addGuiComponentRow(
				getCustomizedLabel(new JLabel(
						"<html>Either create a graph from scratch"
								+ "<small><ul><li>Create a new file: Menu File/New"
								+ "<li>Add network nodes and edges, representing the desired entities like enzymes, compounds and reactions"
								+ "<li>Name the nodes with the same substance names like the names you use for your experimental data"
								+ "</ul>")),
				FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("editcommands"), Color.WHITE), false, 5);
		step2.addGuiComponentRow(getCustomizedLabel(new JLabel("<html>Or use a existing network" + "<small><ul>"
				+ "<li>Load a existing network file: Menu File/Open: "
				+ "Supported file formats are GML (VANTED's native file format), SBML, Pajek and KGML files."
				+ "<li>Use the side panel &quot;Pathways/KEGG&quot; to load (organism specific) pathways. "
				+ "Hint: Create the network with the new system KGML-ED (http://kgml-ed.ipk-gatersleben.de), "
				+ "if you would like to perform advanced KEGG Pathway editing operations." + "</ul>")),
				FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("networkloading"), Color.WHITE), false,
				5);
		step2
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html>Then modify the network"
										+ "<small><ul>"
										+ "<li>Change node titles, to fit your data naming conventions"
										+ "<li>Remove not needed nodes / edges, or add new ones"
										+ "<li>Change the drawing style (use the Network, Node and Edge side panels, to change the style of the selected graph elements)"
										+ "</ul>"
										+ "Hint: You may also ommit this step and just perform the data mapping, without opening a graph window. All measurement data "
										+ "will be shown in a new graph window, using different newly created graph nodes or graph edges, displaying data for the measured substances. "
										+ "You could then use this as the basis for the manual creation of a network. You could also perform a n:n correlation analysis to "
										+ "create a correlation network. If your data input form specifies a mapping onto graph edges, the corresponding network will be automatically "
										+ "derived from the data.")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("editcommands"), Color.WHITE), false, 5);
		
		step2.layoutRows();
		
		FolderPanel step3 = new FolderPanel("3. Perform Data Mapping", true, true, false, null);
		step3.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		step3
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small>Use the data mapping command from a experiment-tab created for a loaded experimental dataset (&quot;Experiments&quot; side-panel)")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("combine"), Color.WHITE), false, 5);
		step3.layoutRows();
		
		FolderPanel step4 = new FolderPanel("4. Modify Charts and Graph-Layout", true, true, false, null);
		step4.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		step4
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small>You may change the charting display for all nodes or edges showing mapped experimental data "
										+ "from the &quot;Network&quot; "
										+ "side panel. For the currently selected graph elements additional specific settings may be changed from the &quot;Node&quot; "
										+ "and &quot;Edge&quot; side panels. "
										+ "The sub category &quot;Charting&quot; contains many options for changing bar or line colors, line widths, "
										+ "the type of diagram (bar/line/pie), the node size, label position and font and much more.<br><br>Certain options "
										+ "for these diagrams are not visible, in case graph elements are part of the selection, which do not contain "
										+ "mapping data. You may use the menu command Edit/Select Nodes and Edit/Select Edges, to select the "
										+ "specifically only those elements, which have experimental data assigned.")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("sidepanels"), Color.WHITE), false, 6);
		step4.layoutRows();
		
		FolderPanel step5 = new FolderPanel("5. Statistic Analysis", true, true, false, null);
		step5.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		
		step5
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html>Identify outliers in the dataset"
										+ "<small><ul><li>The identification of outliers is a difficult task, which may only in some cases be performed automatically. "
										+ "The more replikate measurements are done, the more easy it is to identify outliers. "
										+ "In case of more than about 10-30 replikates, the grubbs test from the Analysis menu may be used to "
										+ "automatically identify or remove outliers in the experimental data." + "</ul>")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("analysismenu_david"), Color.WHITE),
						false, 6);
		
		step5
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html>Recognize the sample data distribution"
										+ "<small><ul><li>The assumption of normal distribution of data samples is important for several analysis tasks. "
										+ "The so called &quot;David Quicktest&quot; may be used to identify not normally distributed data samples. "
										+ "But with few replikates, a sample data distribution is difficult to recognize." + "</ul>")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("analysismenu_david"), Color.WHITE),
						false, 6);
		
		step5.addGuiComponentRow(getCustomizedLabel(new JLabel(
				"<html>Use the &quot;Statistics&quot; side-panel to perform statistical analysis" + "<small><ul>"
						+ "<li>For comparing different plant lines, a t-Test or U-Test may be performed"
						+ "<li>Correlations between different substances may be found by using the "
						+ "correlation analysis commands. It is possible to either correlate a single "
						+ "substance with the remaining ones (&quot;Correlate 1:n&quot;), or you may "
						+ "correlate a number of selected substances against each other with a &quot;"
						+ "Scatter Matrix&quot; or with the &quot;Correlate n:n&quot; command." + "</ul>")),
				FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("panel_statistics"), Color.WHITE),
				false, 6);
		step5.layoutRows();
		
		FolderPanel step6 = new FolderPanel("6. Save / Export Work", true, true, false, null);
		step6.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		step6
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small>During work with the program, and before closing it, it is recommended to save your work to disk (menu File). "
										+ "The preferred and native data format for VANTED is GML, a standard graph format. You may also export the data to other graph formats, but "
										+ "in this case not all network specifics like mapped data or node colors will be saved. You may create a image file of the graph view, for usage in presentations or papers by "
										+ "using the according commands from the File menu (create JPEG, PNG, ...).")),
						FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("filemenu"), Color.WHITE), false, 6);
		step6.layoutRows();
		int b = 3;
		
		intro.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		step6.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		help1.add(intro.getBorderedComponent(0, 0, 2 * b, 0), "1,1");
		help1.add(step1.getBorderedComponent(b, 0, b, 0), "1,2");
		help1.add(step2.getBorderedComponent(b, 0, b, 0), "1,3");
		help1.add(step3.getBorderedComponent(b, 0, b, 0), "1,4");
		help1.add(step4.getBorderedComponent(b, 0, b, 0), "1,5");
		help1.add(step5.getBorderedComponent(b, 0, b, 0), "1,6");
		help1.add(step6.getBorderedComponent(b, 0, b, 0), "1,7");
		help1.validate();
		return help1;
	}
	
	private static FolderPanel workflowFolderPanel;
	
	@Override
	public void postWorkflowStep(String title, final String[] imports, final String[] commands) {
		final String name = title;
		JButton jb = new JMButton(name);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Interpreter interpr = new Interpreter(); // Construct an
				// interpreter
				StringBuilder s = new StringBuilder();
				s.append("boolean useStoredParameters=true;");
				for (String i : imports)
					s.append(i);
				for (String c : commands)
					s.append(c);
				try {
					interpr.eval(s.toString());
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		workflowFolderPanel.addFirstGuiComponentRow(jb, new JLabel("<html>"), true, 2);
		updateStatus();
	}
	
	@Override
	public void postWorkflowStep(Action action) {
		final String name = (String) action.getValue("name");
		JButton jb = new JMButton(name);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GraffitiAction.performAction(name);
			}
		});
		workflowFolderPanel.addFirstGuiComponentRow(jb, new JLabel("<html>"), true, 2);
		updateStatus();
	}
	
	@Override
	public void postWorkflowStep(final Algorithm algorithm, final Parameter[] params) {
		if (workflowFolderPanel != null) {
			JButton jb = new JMButton(algorithm.getName());
			
			// StringBuilder sb = new StringBuilder("<html><pre>");
			// sb.append("// Start algorithm: "+algorithm.getName()+"<br>");
			// sb.append("// 	category: "+algorithm.getCategory()+"<br>");
			// sb.append("// 	description: "+ErrorMsg.removeHTMLtags(algorithm.getDescription())+"<br><br>");
			String paramList = getObjectList(params, ", ");
			// sb.append(algorithm.getClass().getCanonicalName()+".execute(new Object[] {"+paramList+"});");
			//
			// final String title = algorithm.getName();
			// final String help = sb.toString();
			
			// final String algClass = algorithm.getClass().getCanonicalName();
			final String algName = algorithm.getName();
			
			jb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						GravistoService.getInstance().runPlugin(algName,
								MainFrame.getInstance().getActiveEditorSession().getGraph(), e);
						//
						// Algorithm algo = (Algorithm)
						// Class.forName(algClass).newInstance();
						// algo.attach(
						// MainFrame.getInstance().getActiveEditorSession().getGraph(),
						// MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection());
						// algo.setParameters(params);
						// algo.check();
						// algo.execute();
						// algo.reset();
					} catch (Exception e1) {
						ErrorMsg.addErrorMessage(e1);
					}
				}
			});
			paramList = "";
			workflowFolderPanel.addFirstGuiComponentRow(jb, new JLabel("<html>" + paramList), true, 2);
			updateStatus();
		}
	}
	
	private void updateStatus() {
		if (workflowFolderPanel.getRowCount() != 1)
			recordStatus.setText("Recording (" + workflowFolderPanel.getRowCount() + " actions)");
		else
			recordStatus.setText("Recording (" + workflowFolderPanel.getRowCount() + " action)");
	}
	
	private String getObjectList(Parameter[] params, String div) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < params.length; i++) {
			Parameter p = params[i];
			if (p != null && p.getValue() != null) {
				Object o = p.getValue();
				if (o instanceof String) {
					sb.append("\"" + (String) o + "\"");
				} else
					if (o instanceof Integer) {
						sb.append("new Integer(" + ((Integer) o).intValue() + ")");
					} else {
						if (o instanceof Boolean) {
							sb.append("new Boolean(" + ((Boolean) o).booleanValue() + ")");
						} else {
							//
						}
					}
			} else {
				sb.append("null");
			}
			if (i + 1 < params.length)
				sb.append(div);
		}
		return sb.toString();
	}
	
	private JPanel getScenarioControl() {
		JPanel help1 = new JPanel();
		help1.setOpaque(false);
		help1.setBackground(null);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED,
						border } }; // Rows
		help1.setLayout(new TableLayout(size));
		
		recordStatus = new JLabel("No active recording");
		
		FolderPanel control = new FolderPanel("Control", false, true, false, null);
		control.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		control
				.addGuiComponentRow(
						getCustomizedLabel(new JLabel(
								"<html><small>"
										+ "Click <b>Record</b> to start recording a workflow.<br>"
										+ "As the workflow is completed, click <b>Pause</b> and then <b>Save</b> "
										+ "to save the workflow steps for later reuse. You may use the created application menu entries (or context menu items) to duplicate a workflow at a later time. Use the Library section below to inspect/modify the created source code.")),
						// FolderPanel.getHelpButton(JLabelJavaHelpLink.getHelpActionListener("main"),
						// Color.WHITE
						null, false, 5);
		control.addGuiComponentRow(TableLayout.get4Split(getRecordButton(), getWorkflowPauseButton(), getSaveButton(),
				recordStatus, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, 5, 0),
				null, false, 5);
		control.layoutRows();
		
		/* new JLabel() */
		
		FolderPanel active = new FolderPanel("Active Recording", false, true, false, null);
		workflowFolderPanel = active;
		ScenarioService.setGUI(this);
		
		workflowFolderPanel.setMaximumRowCount(5);
		
		active.setColumnStyle(TableLayout.PREFERRED, TableLayout.FILL);
		// active.addGuiComponentRow(new JButton("<html>Grid Layout"), null,
		// false, 2);
		// active.addGuiComponentRow(new JButton("<html>Search"), null, false, 2);
		// active.addGuiComponentRow(new JButton("<html>Delete"), null, false, 2);
		// active.addGuiComponentRow(new JButton("<html>Colorize Nodes"), null,
		// false, 2);
		// active.addGuiComponentRow(new JButton("<html>Colorize Nodes"), null,
		// false, 2);
		
		// JLabelJavaHelpLink.getHelpActionListener("inputformats"),
		// "inputformats"
		
		active.layoutRows();
		
		library = new FolderPanel("Library", false, true, false, null
				// JLabelJavaHelpLink.getHelpActionListener("inputformats"),
				// "inputformats"
				);
		library.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		for (Scenario s : ScenarioService.getAvailableScnenarios())
			library.addGuiComponentRow(new MyScenarioEditor(s), null, false, 2);
		
		library.setMaximumRowCount(5);
		
		library.layoutRows();
		
		int b = 3;
		help1.add(control.getBorderedComponent(0, 0, 2 * b, 0), "1,1");
		help1.add(active.getBorderedComponent(b, 0, b, 0), "1,2");
		help1.add(library.getBorderedComponent(b, 0, b, 0), "1,3");
		help1.validate();
		return help1;
	}
	
	private JButton getSaveButton() {
		JButton res = new JMButton("");
		res.setToolTipText("Save workflow recording (create menu command)");
		res.setIcon(myGetIcon("images/save.png"));
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ScenarioService.getCurrentScenario() == null)
					MainFrame.showMessageDialog("No active recording", "Error");
				else {
					Scenario ss = ScenarioService.getCurrentScenario();
					Object[] res = MyInputHelper
							.getInput("<html>" + "Please specify menu group and<br>" + "menu command title:", "Save Macro",
									"Menu Group", "Workflow", "Menu Title", "Recorded Workflow");
					if (res != null) {
						int i = 0;
						String group = (String) res[i++];
						String cmd = (String) res[i++];
						ss.setName(cmd);
						ss.setMenuGroup(group);
						TextFile tf = new TextFile();
						tf.add(ss.toString());
						try {
							String fileName = ss.getFileName();
							if (new File(fileName).exists()) {
								String warning = "<b>Warning: Exisiting file " + fileName + "<br>"
										+ "will be overwritten. If desired, click Cancel to abort.<b></br><br>";
								Object[] resss = MyInputHelper.getInput(warning, "Information", new Object[] {});
								if (resss == null)
									return;
							}
							tf.write(fileName);
							
							library.clearGuiComponentList();
							for (Scenario s : ScenarioService.getAvailableScnenarios())
								library.addGuiComponentRow(new MyScenarioEditor(s), null, false, 2);
							library.layoutRows();
							ScenarioService.recordStop();
							recordStatus.setText("Workflow saved");
						} catch (IOException e1) {
							ErrorMsg.addErrorMessage(e1);
						}
					}
				}
			}
		});
		return res;
	}
	
	private JButton getRecordButton() {
		JButton result = new JMButton("");
		result.setIcon(myGetIcon("images/record.png"));
		result.setToolTipText("Start new workflow recording");
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Scenario s = new Scenario("", "");
				ScenarioService.setCurrentScenario(s);
				workflowFolderPanel.clearGuiComponentList();
				workflowFolderPanel.layoutRows();
				
				ScenarioService.recordStart();
				recordStatus.setText("Recording");
			}
		});
		return result;
	}
	
	public Icon myGetIcon(String name) {
		ClassLoader cl = WorkflowHelper.class.getClassLoader();
		String path = WorkflowHelper.class.getPackage().getName().replace('.', '/');
		try {
			ImageIcon icon = new ImageIcon(cl.getResource(path + "/" + name));
			return icon;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("File not found: " + name);
			return null;
		}
	}
	
	private JButton getWorkflowPauseButton() {
		final JButton result = new JMButton("");
		result.setToolTipText("Pause/continue workflow recording");
		result.setIcon(myGetIcon("images/pause.png"));
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ScenarioService.isRecording()) {
					ScenarioService.recordStop();
					recordStatus.setText("Recording paused");
				} else {
					ScenarioService.recordStart();
					recordStatus.setText("Recording");
				}
			}
		});
		return result;
	}
	
	private JLabel getCustomizedLabel(JLabel label) {
		label.setBackground(Color.WHITE);
		return label;
	}
	
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	public void transactionFinished(TransactionEvent e) {
	}
	
	public void transactionStarted(TransactionEvent e) {
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	public static WorkflowHelper getInstance() {
		return instance;
	}
	
	@Override
	public JTabbedPane getTabbedPane() {
		return hc;
	}
	
	public static void showPreferencesFolder() {
		MainFrame.showMessageDialog("<html>" + "The application preferences folder will be opened in a moment.<br>"
				+ "This folder contains downloaded database files, stored quick-searches,<br>"
				+ "network download cache files, and program settings files.<br>"
				+ "Quick searches (created with 'Edit/Search...': 'Create new menu command')<br>"
				+ "are stored as files the file name extension '.bsh'. Such a file may be<br>"
				+ "deleted, in case the custom search command is not needed any more.", "Information");
		BackgroundTaskHelper.executeLaterOnSwingTask(2000, new Runnable() {
			@Override
			public void run() {
				AttributeHelper.showInBrowser(ReleaseInfo.getAppFolder());
			}
		});
	}
}
