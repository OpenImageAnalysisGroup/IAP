/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

import org.FeatureSet;
import org.JMButton;
import org.ReleaseInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.dialog.ParameterEditPanel;
import org.graffiti.graph.Graph;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.managers.pluginmgr.Dependency;
import org.graffiti.managers.pluginmgr.PluginDependency;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.options.OptionPane;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeAlgorithm;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;

import scenario.ScenarioService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.BSHscriptMenuEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.DefaultContextMenuManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.RubyScriptMenuEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.HandlesAlgorithmData;

public class PreferencesDialog extends JDialog
					implements PluginManagerListener {
	
	private static final long serialVersionUID = 1L;
	
	public static JButton activeStartLayoutButton;
	
	JMenuBar myMenu;
	
	JTree myTree;
	
	DefaultMutableTreeNode rootNode;
	DefaultMutableTreeNode rootNodeByPlugin;
	DefaultMutableTreeNode rootNodeAlgorithms;
	DefaultMutableTreeNode rootNodeThreadSafeAlgorithms;
	DefaultMutableTreeNode rootNodeSettings;
	DefaultMutableTreeNode rootNodeScripts;
	
	HashMap<String, MyPluginTreeNode> knownNodes;
	
	JPanel settingsPanel;
	
	private boolean showOnlyLayoutAlgorithms;
	private boolean showThreadSafeLayoutAlgorithms;
	
	public ThreadSafeOptions optionsForPlugin = null;
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JDialog#dialogInit()
	 */
	@Override
	protected void dialogInit() {
		super.dialogInit();
		
		JMenuItem fileOpenAction = new JMenuItem("Import Settings...");
		JMenuItem fileSaveAction = new JMenuItem("Export Settings...");
		
		JMenu fileItem = new JMenu("File");
		fileItem.add(fileOpenAction);
		fileItem.add(fileSaveAction);
		
		myMenu = new JMenuBar();
		myMenu.add(fileItem);
		
		this.setJMenuBar(myMenu);
		
		this.setSize(640, 480);
		
		Container cp = this.getContentPane();
		
		initializeGUIforGivenContainer(cp, this, false, true, true, true, true, true, false,
							null,
							null,
							null,
							false);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e) {
			}
			
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent e) {
				if (optionsForPlugin != null) {
					optionsForPlugin.setAbortWanted(true);
				}
				hide();
				dispose();
			}
			
			public void windowClosed(WindowEvent e) {
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowActivated(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
		});
	}
	
	/**
	 * @param cp
	 * @param selection
	 * @param graph
	 * @param setAlgorithmDataObject
	 */
	public void initializeGUIforGivenContainer(Container cp, final JDialog thisDialog,
						boolean vertical, boolean showCloseButton,
						boolean showAlgorithms,
						boolean showSettings,
						boolean showInteractiveAlgorithms,
						boolean showScripts,
						boolean showOnlyLayoutAlgorithms,
						final Graph graph, final Selection selection,
						final HandlesAlgorithmData setAlgorithmDataObject,
						final boolean executeMoveToTopAfterwards) {
		
		if (showScripts && !ReleaseInfo.getIsAllowedFeature(FeatureSet.SCRIPT_ACCESS))
			showScripts = false;
		
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
		
		// cp.add(new JLabel("Select a Plugin"), "1,1");
		// cp.add(new JLabel("Modify Settings"), "3,1");
		
		settingsPanel = new JPanel();
		settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
		rootNode = new DefaultMutableTreeNode("System Commands");
		myTree = new JTree(rootNode);
		// setBackgroundNonSelectionColor()
		
		DefaultTreeCellRenderer tcr = (DefaultTreeCellRenderer) myTree.getCellRenderer();
		tcr.setOpaque(true);
		tcr.setBackgroundNonSelectionColor(Color.YELLOW);
		
		knownNodes = new HashMap<String, MyPluginTreeNode>();
		this.showOnlyLayoutAlgorithms = showOnlyLayoutAlgorithms;
		this.showThreadSafeLayoutAlgorithms = showInteractiveAlgorithms;
		myTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				processTreeSelectionEvent(e);
			}
			
			/**
			 * @param e
			 */
			private void processTreeSelectionEvent(TreeSelectionEvent e) {
				
				if (optionsForPlugin != null) {
					optionsForPlugin.setAbortWanted(true);
				}
				
				if (e.getNewLeadSelectionPath() == null || e.getNewLeadSelectionPath().getLastPathComponent() == null)
					return;
				
				PreferencesDialog.activeStartLayoutButton = null;
				
				MutableTreeNode mt = (MutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
				if (mt instanceof MyPluginTreeNode) {
					MyPluginTreeNode mpt = (MyPluginTreeNode) mt;
					Object data = mpt.getUserObject();
					settingsPanel.removeAll();
					settingsPanel.repaint();
					if (mpt.getClassType() == BSHscriptMenuEntry.class) {
						final BSHscriptMenuEntry sm = (BSHscriptMenuEntry) mpt.getUserObject();
						settingsPanel.add(new JLabel("Script file: " + sm.getCmdFile()));
						JButton startButton = new JButton(sm.getText());
						startButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								ActionListener al = sm.getActionListeners()[0];
								al.actionPerformed(new ActionEvent(sm, 0, ""));
							}
						});
						settingsPanel.add(startButton);
						settingsPanel.validate();
					} else
						if (mpt.getClassType() == Dependency.class) {
							List<?> deps = (List<?>) mpt.getUserObject();
							for (Iterator<?> it = deps.iterator(); it.hasNext();) {
								settingsPanel.add(getDependencyPanel((Dependency) it.next()));
							}
							settingsPanel.validate();
						} else
							if (mpt.getClassType() == PluginEntry.class) {
								PluginEntry de = (PluginEntry) data;
								settingsPanel.add(new JLabel("Plugin selection: " + de.getDescription().getName()));
								settingsPanel.validate();
							} else
								if (mpt.getClassType() == ThreadSafeAlgorithm.class) {
									ThreadSafeAlgorithm alg = (ThreadSafeAlgorithm) data;
									JPanel pluginContent = new JPanel();
									optionsForPlugin =
														new ThreadSafeOptions();
									
									if (alg.setControlInterface(optionsForPlugin,
														pluginContent)) {
										JScrollPane sp = new JScrollPane(pluginContent);
										sp.setBorder(null);
										settingsPanel.add(sp);
										settingsPanel.validate();
									}
								} else
									if (mpt.getClassType() == Algorithm.class) {
										Algorithm alg = (Algorithm) data;
										initAlgorithmPreferencesPanel(alg, graph, selection, setAlgorithmDataObject, executeMoveToTopAfterwards);
									} else
										if (mpt.getClassType() == OptionPane.class) {
											if (data instanceof OptionPane) {
												OptionPane op = (OptionPane) data;
												settingsPanel.add(new OptionSetter(op));
											}
											settingsPanel.validate();
										}
				}
			}
			
			private JComponent getDependencyPanel(Dependency dep) {
				return new JLabel(dep.getName() + " (" + dep.getMain() + ")");
			}
			
			private void initAlgorithmPreferencesPanel(final Algorithm alg,
								final Graph graph, Selection sele,
								final HandlesAlgorithmData setAlgorithmDataObject,
								final boolean executeMoveToTopAfterwards) {
				// settingsPanel.add(new JLabel("Algorithm selection: "+alg.getName()));
				
				JPanel progressAndStatus = new JPanel();
				double border = 5;
				double[][] size =
				{
									{ border, TableLayoutConstants.FILL, border }, // Columns
						{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border }
				}; // Rows
				
				progressAndStatus.setLayout(new TableLayout(size));
				
				String desc = alg.getDescription();
				JLabel info = new JLabel(desc);
				info.setBorder(BorderFactory.createLoweredBevelBorder());
				info.setOpaque(false);
				if (desc != null && desc.length() > 0)
					progressAndStatus.add(info, "1,3");
				EditComponentManager editComponentManager = MainFrame.getInstance().getEditComponentManager();
				
				ParameterEditPanel paramPanel = null;
				alg.attach(graph, selection);
				boolean canNotStart = false;
				try {
					Graph workgraph = graph;
					try {
						if (workgraph == null) {
							workgraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
							sele = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
							if (sele == null)
								sele = new Selection("");
						}
					} catch (NullPointerException npe) {
						// empty
					}
					alg.attach(workgraph, sele);
					alg.check();
				} catch (PreconditionException e1) {
					canNotStart = true;
					JLabel hint = new JLabel("<html>Algorithm can not be used at the moment:<br>" + e1.getLocalizedMessage());
					progressAndStatus.add(hint, "1,2");
					paramPanel = null;
				}
				if (!canNotStart)
					if (alg.getParameters() != null) {
						paramPanel = new ParameterEditPanel(alg.getParameters(),
											editComponentManager.getEditComponents(), sele, alg.getName(), true, alg.getName());
						if (paramPanel != null) {
							JScrollPane sp = new JScrollPane(paramPanel);
							sp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
							sp.setOpaque(false);
							sp.setBackground(null);
							progressAndStatus.add(sp, "1,2");
						}
					}
				final ParameterEditPanel finalParamPanel = paramPanel;
				JButton runButton = new JMButton("Layout Network");
				PreferencesDialog.activeStartLayoutButton = runButton;
				if (setAlgorithmDataObject != null)
					runButton.setText("Select Layouter");
				if (canNotStart)
					runButton.setEnabled(false);
				final Selection selectionF = sele;
				runButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Graph workgraph = graph;
						if (workgraph == null) {
							try {
								workgraph = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
							} catch (Exception err) {
								MainFrame.showMessageDialog("No active graph!", "Error");
								return;
							}
						}
						if (finalParamPanel != null)
							alg.setParameters(finalParamPanel.getUpdatedParameters());
						if (setAlgorithmDataObject != null) {
							setAlgorithmDataObject.setAlgorithm(alg);
							if (thisDialog != null)
								thisDialog.setVisible(false);
						} else {
							Selection selection = selectionF;
							try {
								workgraph = MainFrame.getInstance().getActiveEditorSession().getGraph();
								selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
								if (selection == null)
									selection = new Selection("");
							} catch (NullPointerException npe) {
								// empty
							}
							runAlgorithm(alg, workgraph, selection);
							if (executeMoveToTopAfterwards) {
								CenterLayouterAlgorithm ca = new CenterLayouterAlgorithm();
								ca.attach(workgraph, selection);
								runAlgorithm(ca, workgraph, selection);
							}
							if (thisDialog != null)
								thisDialog.setVisible(false);
						}
					}
				});
				runButton.setMinimumSize(new Dimension(10, 10));
				progressAndStatus.add(runButton, "1,1");
				
				progressAndStatus.validate();
				settingsPanel.add(progressAndStatus);
				settingsPanel.validate();
			}
		});
		
		JSplitPane mainComp;
		// myTree.setOpaque(false);
		JScrollPane sp = new JScrollPane(myTree);
		sp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		if (!vertical)
			mainComp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, settingsPanel);
		else
			mainComp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, settingsPanel);
		mainComp.setDividerLocation(200); // 175
		mainComp.setDividerSize(7);
		mainComp.setOneTouchExpandable(true);
		
		mainComp.setBorder(null);
		
		cp.add(mainComp);
		
		rootNodeSettings = new DefaultMutableTreeNode("Settings");
		rootNodeByPlugin = new DefaultMutableTreeNode("Dependencies");
		if (showOnlyLayoutAlgorithms) {
			rootNodeAlgorithms = rootNode;
			rootNode.setUserObject("Layout Commands");
			// rootNodeAlgorithms=new DefaultMutableTreeNode("Layout Algorithms");
			rootNodeThreadSafeAlgorithms = rootNodeAlgorithms;
		} else {
			rootNodeAlgorithms = new DefaultMutableTreeNode("Algorithms");
			rootNodeThreadSafeAlgorithms = new DefaultMutableTreeNode("Interactive Algorithms");
		}
		rootNodeScripts = new DefaultMutableTreeNode("Script Commands");
		
		// rootNode.add(rootNodeByPlugin);
		
		if (showScripts)
			rootNode.add(rootNodeScripts);
		if (showAlgorithms && !showOnlyLayoutAlgorithms)
			rootNode.add(rootNodeAlgorithms);
		if (showInteractiveAlgorithms && !showOnlyLayoutAlgorithms)
			rootNode.add(rootNodeThreadSafeAlgorithms);
		if (showSettings)
			rootNode.add(rootNodeSettings);
		
		final JMenu scriptContextMenu = new JMenu();
		Timer scanForScripts = new Timer(20000, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				// update settings
				addSettings();
				scriptContextMenu.removeAll();
				// Scan for scripts and add them to the tree
				DefaultContextMenuManager.returnScriptMenu(scriptContextMenu);
				rootNodeScripts.removeAllChildren();
				Component[] comps = scriptContextMenu.getMenuComponents();
				for (int i = 0; i < comps.length; i++) {
					if (comps[i] != null) {
						if (comps[i] instanceof BSHscriptMenuEntry) {
							BSHscriptMenuEntry sme = (BSHscriptMenuEntry) comps[i];
							rootNodeScripts.add(
												new MyPluginTreeNode(sme.getText(),
																	comps[i], BSHscriptMenuEntry.class));
						}
						if (comps[i] instanceof RubyScriptMenuEntry) {
							RubyScriptMenuEntry sme = (RubyScriptMenuEntry) comps[i];
							rootNodeScripts.add(
												new MyPluginTreeNode(sme.getText(),
																	comps[i], RubyScriptMenuEntry.class));
						}
					}
				}
				try {
					myTree.updateUI();
				} catch (Exception e) {
					
				}
			}
		});
		if (showScripts) {
			scanForScripts.setDelay(10000);
			scanForScripts.setRepeats(true);
			scanForScripts.start();
		}
		if (showCloseButton) {
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					thisDialog.setVisible(false);
				}
			});
			cp.add(closeButton);
		}
		cp.validate();
		
		Collection<?> loadedPlugins = GravistoService.getInstance().getMainFrame().getPluginManager().getPluginEntries();
		for (Iterator<?> it = loadedPlugins.iterator(); it.hasNext();) {
			PluginEntry p = (PluginEntry) it.next();
			if (p.getPlugin() != null) {
				pluginAdded(p.getPlugin(), p.getDescription());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Dialog#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Plugin Manager";
	}
	
	void runAlgorithm(final Algorithm alg, Graph graph, Selection selection) {
		ScenarioService.postWorkflowStep(alg, alg.getParameters());
		alg.attach(graph, selection);
		alg.execute();
		alg.reset();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		
		final String todoReplaceString = "DEPENDENCY NOT SOLVED:";
		
		Collection<?> pl = GravistoService.getInstance().getMainFrame().getPluginManager().getPluginEntries();
		
		PluginEntry pluginEntry = null;
		
		for (Iterator<?> it = pl.iterator(); it.hasNext();) {
			PluginEntry entry = (PluginEntry) it.next();
			if (entry.getPlugin() == plugin) {
				pluginEntry = entry;
				break;
			}
		}
		if (pluginEntry == null)
			return;
		MyPluginTreeNode newNode;
		if (pluginEntry.getDescription() == null) {
			newNode = new MyPluginTreeNode(pluginEntry.getPlugin().getClass().getName() + " (no description)", pluginEntry, PluginEntry.class);
		} else {
			String pN = pluginEntry.getDescription().getName();
			newNode = new MyPluginTreeNode(pN, pluginEntry, PluginEntry.class);
		}
		rootNodeByPlugin.add(newNode);
		knownNodes.put(pluginEntry.getDescription().getMain(), newNode);
		findNotResolvedDependencies(todoReplaceString);
		
		addDependencies(todoReplaceString, pluginEntry, newNode);
		
		addAlgorithms(pluginEntry, newNode, showOnlyLayoutAlgorithms, showThreadSafeLayoutAlgorithms);
		
		addSettings();
		
		SortJTree.sortJTree(rootNode, true);
		myTree.expandRow(0);
		try {
			myTree.updateUI();
		} catch (Exception e) {
			// ignore for now
		}
	}
	
	/**
	 * @param todoReplaceString
	 * @param pluginEntry
	 * @param newNode
	 */
	private void addDependencies(final String todoReplaceString, PluginEntry pluginEntry, MyPluginTreeNode newNode) {
		MyPluginTreeNode depsNode = new MyPluginTreeNode("Dependencies",
							pluginEntry.getDescription().getDependencies(), Dependency.class);
		
		List<?> deps = pluginEntry.getDescription().getDependencies();
		if (!deps.isEmpty()) {
			newNode.add(depsNode);
			for (Iterator<?> it = deps.iterator(); it.hasNext();) {
				PluginDependency d = (PluginDependency) it.next();
				String main = d.getMain();
				MutableTreeNode knownPlugin = (MutableTreeNode) knownNodes.get(main);
				if (knownPlugin != null)
					depsNode.add(knownPlugin);
				else {
					depsNode.add(new DefaultMutableTreeNode(todoReplaceString + d.getMain()));
					// System.out.println("Not resolved dep.: "+todoReplaceString+d.getMain());
				}
			}
		}
	}
	
	/**
	 * @param pluginEntry
	 * @param newNode
	 */
	private void addSettings() {
		ArrayList<?> options = GravistoService.getInstance().getKnownOptionPanes();
		if (options != null && options.size() > rootNodeSettings.getChildCount()) {
			rootNodeSettings.removeAllChildren();
			for (Iterator<?> it = options.iterator(); it.hasNext();) {
				OptionPane op = (OptionPane) it.next();
				rootNodeSettings.add(
									new MyPluginTreeNode(
														op.getCategory() + "/" + op.getOptionName(), op, OptionPane.class));
			}
		}
	}
	
	/**
	 * @param pluginEntry
	 * @param newNode
	 */
	private void addAlgorithms(PluginEntry pluginEntry, MyPluginTreeNode newNode, boolean showOnlyLayoutAlgorithms, boolean includeThreadSafeAlgorithms) {
		Algorithm[] algs = pluginEntry.getPlugin().getAlgorithms();
		if (algs.length > 0) {
			DefaultMutableTreeNode algNode = new DefaultMutableTreeNode("Algorithms");
			newNode.add(algNode);
			for (int i = 0; i < algs.length; i++) {
				if (algs[i] == null)
					continue;
				if (showOnlyLayoutAlgorithms && !algs[i].isLayoutAlgorithm())
					continue;
				String algName = algs[i].getName();
				String name;
				if (algName == null)
					continue;
				else
					name = algName;
				// System.out.println("Neuer Algorithmus: "+algName);
				algNode.add(new MyPluginTreeNode(name, algs[i], Algorithm.class));
				if (algs[i] instanceof ThreadSafeAlgorithm && includeThreadSafeAlgorithms) {
					rootNodeThreadSafeAlgorithms.add(new MyPluginTreeNode(name, algs[i], ThreadSafeAlgorithm.class));
				} else
					rootNodeAlgorithms.add(new MyPluginTreeNode(name, algs[i], Algorithm.class));
			}
		}
	}
	
	/**
	 * @param todoReplaceString
	 */
	private void findNotResolvedDependencies(final String todoReplaceString) {
		for (Enumeration<?> enum2 = rootNodeByPlugin.depthFirstEnumeration(); enum2.hasMoreElements();) {
			Object e = enum2.nextElement();
			if (e instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode dn = (DefaultMutableTreeNode) e;
				String title = dn.toString();
				if (title.indexOf(todoReplaceString) >= 0) {
					String main = title.substring(todoReplaceString.length());
					for (Enumeration<?> searchEnum = rootNodeByPlugin.depthFirstEnumeration(); searchEnum.hasMoreElements();) {
						Object e2 = searchEnum.nextElement();
						if (e2 instanceof MyPluginTreeNode) {
							MyPluginTreeNode pt = (MyPluginTreeNode) e2;
							if (pt.getClassType() == PluginEntry.class) {
								PluginEntry pe = (PluginEntry) pt.getUserObject();
								if (pe.getDescription().getMain().equals(main)) {
									DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dn.getParent();
									if (parent != null) {
										// System.out.println("Solved dependency: "+pe.getDescription().getMain());
										dn.removeFromParent();
										parent.add((MutableTreeNode) pt.clone());
									}
								}
							}
						}
					}
				}
			}
		}
	}
}