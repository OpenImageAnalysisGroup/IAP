/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.MultipleFileLoader;
import org.Release;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.pathway_kegg_operation.PathwayKeggLoading;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.InterpreteLabelNamesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author $author$
 * @version $Revision: 1.4 $
 */
@SuppressWarnings("deprecation")
public class TabKegg extends InspectorTab
		implements
		ActionListener,
		MultipleFileLoader {
	private static final long serialVersionUID = 1L;
	
	MyDefaultMutableTreeNode myRootNode;
	
	private static JCheckBox prettifyLabels = null;
	
	JTree keggTree;
	JButton getOrganismListFromKegg;
	JButton getPathwayListFromKegg;
	
	HashMap<KeggPathwayEntry, MyDefaultMutableTreeNode> pathwayToTreeNode = new HashMap<KeggPathwayEntry, MyDefaultMutableTreeNode>();
	
	HashMap<MyDefaultMutableTreeNode, Collection<KeggPathwayEntry>> pathwaysOfTreeNode =
			new HashMap<MyDefaultMutableTreeNode, Collection<KeggPathwayEntry>>();
	
	final String noNode = "No node is selected.";
	private boolean showAnalyzeOption;
	
	private void initComponents() {
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
						TableLayout.PREFERRED,
						TableLayout.PREFERRED,
						TableLayoutConstants.FILL,
						TableLayout.PREFERRED,
						border } }; // Rows
		this.setLayout(new TableLayout(size));
		
		getOrganismListFromKegg = new JMButton("<html>Select Organisms");
		
		getOrganismListFromKegg.addActionListener(this);
		getOrganismListFromKegg.setOpaque(false);
		
		JComponent helpButton = FolderPanel.getHelpButton(
				JLabelJavaHelpLink.getHelpActionListener("panel_kegg"), getBackground());
		
		JComponent orgList;
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			orgList =
					TableLayout.getSplit(
							getOrganismListFromKegg,
							TableLayout.getSplit(
									new JLabel(""),
									helpButton,
									5, TableLayout.PREFERRED),
							TableLayout.FILL,
							TableLayout.PREFERRED);
		else
			orgList = getOrganismListFromKegg;
		
		// this.add(TableLayout.getSplit(loadFromFile, saveAll, TableLayout.FILL, TableLayout.FILL), "1,2");
		
		JButton btnCheckMappingCount = new JMButton("Analyze Pathway-Dataset Mapping");
		
		btnCheckMappingCount.setOpaque(false);
		btnCheckMappingCount.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final KeggService ks = new KeggService();
				Runnable workTask = new Runnable() {
					@Override
					public void run() {
						ks.processKeggTree(keggTree, pathwayToTreeNode);
						keggTree.repaint();
					}
				};
				BackgroundTaskHelper bth = new BackgroundTaskHelper(workTask, ks,
						"Pathway-Dataset Analysis",
						"Pathway-Dataset Analysis",
						true, false);
				bth.startWork(MainFrame.getInstance());
			}
		});
		JButton btnResetMappingInfo = new JMButton("Reset");
		
		btnResetMappingInfo.setOpaque(false);
		btnResetMappingInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<KeggPathwayEntry> keggPathways = new ArrayList<KeggPathwayEntry>();
				for (KeggPathwayEntry kpe : pathwayToTreeNode.keySet())
					keggPathways.add(kpe);
				for (Iterator<KeggPathwayEntry> it = keggPathways.iterator(); it.hasNext();) {
					KeggPathwayEntry kwe = it.next();
					kwe.setMappingCount("");
				}
				keggTree.repaint();
			}
		});
		
		myRootNode = new MyDefaultMutableTreeNode("Please select the desired organisms");
		DefaultTreeModel myTreeModel = new DefaultTreeModel(myRootNode);
		keggTree = new JTree(myTreeModel);
		
		JScrollPane pathwayTreeScroll = new JScrollPane(keggTree);
		
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = keggTree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = keggTree.getPathForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					if (e.getClickCount() == 2 && selPath != null) {
						myDoubleClick(selPath);
					}
				}
			}
		};
		keggTree.addMouseListener(ml);
		JButton loadPathway = new JMButton("Load Selected Pathways");
		loadPathway.setOpaque(false);
		
		prettifyLabels = new JCheckBox("auto-prettify", true);
		prettifyLabels.setOpaque(false);
		prettifyLabels.setToolTipText("If selected, upon loading numeric IDs are replaced by database entry names.");
		
		JComponent lowerPane = getLowerPane(loadPathway, prettifyLabels);
		
		loadPathway.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TreePath[] selPaths = keggTree.getSelectionPaths();
				if (selPaths == null) {
					MainFrame.showMessageDialog(
							"Please select a pathway or pathway group from the tree above!",
							"Information");
				} else {
					final SortedSet<KeggPathwayEntry> entries = new TreeSet<KeggPathwayEntry>();
					for (TreePath path : selPaths) {
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
						if ((node.getUserObject() instanceof KeggPathwayEntry)) {
							KeggPathwayEntry myEntry = (KeggPathwayEntry) node.getUserObject();
							entries.add(myEntry);
						} else {
							// enumerate tree
							enumerateTree(node, entries);
						}
					}
					processPathwayLoading2(entries, getAutoPrettifyLabelSetting());
				}
			}
		});
		final JTextField searchBox = new JTextField("");
		searchBox.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String searchText = searchBox.getText();
						// keggPathways
						for (int i = 0; i < myRootNode.getChildCount(); i++) {
							MyDefaultMutableTreeNode myRootNode2 = (MyDefaultMutableTreeNode) keggTree.getModel().getChild(myRootNode, i);
							buildPathwayTree(searchText, myRootNode2, pathwaysOfTreeNode.get(myRootNode2));
						}
					}
				});
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		JLabel keggDesc = new JLabel(
				"<html><small>KEGG Pathway access:");
		keggDesc.setBackground(null);
		keggDesc.setOpaque(false);
		
		JComponent kgmlVersionSelection = KeggHelper.getKGMLversionSelectionCombobox();
		
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			orgList = TableLayout.getSplit(orgList, null/* kgmlVersionSelection */,
					TableLayout.FILL, TableLayout.PREFERRED);
		
		JComponent searchPathway = TableLayout.getSplit(new JLabel("Search"), searchBox, TableLayout.PREFERRED, TableLayout.FILL);
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			this.add(
					TableLayout.getSplitVertical(
							keggDesc,
							orgList,
							TableLayout.PREFERRED, TableLayout.PREFERRED), "1,1");
		else
			this.add(orgList, "1,1");
		if (showAnalyzeOption && ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			this.add(TableLayout.getSplit(btnCheckMappingCount, btnResetMappingInfo, TableLayout.FILL, TableLayout.PREFERRED), "1,2");
		this.add(pathwayTreeScroll, "1,3");
		this.add(
				TableLayout.get3SplitVertical(searchPathway, null, lowerPane, TableLayout.PREFERRED, 3, TableLayout.PREFERRED),
				"1,4");
		
		this.validate();
	}
	
	protected JComponent getLowerPane(JButton loadPathway, JCheckBox prettifyLabels) {
		JButton replaceLabels = new JMButton("<< Modify Labels");
		replaceLabels.setOpaque(false);
		replaceLabels.setToolTipText("Click to modify active graph node labels");
		replaceLabels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new InterpreteLabelNamesAlgorithm(), e);
			}
		});
		
		return TableLayout.get3Split(replaceLabels, loadPathway, prettifyLabels, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED);
	}
	
	private static void processPathwayLoading(final SortedSet<KeggPathwayEntry> entries, final int maxColumns,
			final Boolean processMapLinks) {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Load Pathways", "");
		final Graph superGraph = new AdjListGraph(new ListenerManager());
		BackgroundTaskHelper.issueSimpleTask("Load Pathway Selection", "Please wait...",
				new Runnable() {
					@Override
					public void run() {
						int maxCol = maxColumns - 1;
						int col = 0;
						double offX = 0;
						double offY = 0;
						double space = 50;
						double maxHeightInColumn = 0;
						double workLoad = entries.size();
						double progress = 0;
						for (KeggPathwayEntry myEntry : entries) {
							if (status.wantsToStop()) {
								status.setCurrentStatusText1("Processing incomplete!");
								status.setCurrentStatusText2("Operation aborted.");
								break;
							}
							Graph g = new AdjListGraph(new ListenerManager());
							status.setCurrentStatusText1("Load Pathway:");
							status.setCurrentStatusText2(myEntry.getMapName() + ": " + myEntry.getPathwayName());
							KeggService.loadPathway(myEntry, g, null, false, false, getAutoPrettifyLabelSetting());
							progress = progress + 1;
							status.setCurrentStatusValueFine(100d * progress / workLoad);
							
							if (g != null && g.getNodes().size() > 0) {
								Vector2d d = NodeTools.getMaximumXY(g.getNodes(), 1d, 0, 0, true);
								for (Node n : g.getNodes()) {
									NodeHelper nh = new NodeHelper(n);
									Point2D p = nh.getPosition();
									nh.setPosition(p.getX() + offX, p.getY() + offY);
									nh.setClusterID("path:" + myEntry.getMapName());
								}
								offX += d.x;
								offX += space;
								superGraph.addGraph(g);
								if (d.y > maxHeightInColumn)
									maxHeightInColumn = d.y;
								col++;
								if (col > maxCol) {
									offY += maxHeightInColumn;
									offY += space;
									offX = 0;
									col = 0;
									maxHeightInColumn = 0;
								}
							}
						}
						if (processMapLinks != null && processMapLinks)
							processMapLinks(status, superGraph);
						
						status.setCurrentStatusText1("Processing complete. Initalize graph view.");
						status.setCurrentStatusText2("Please wait. This may take a few moments.");
						status.setCurrentStatusValueFine(-1);
					}
				}, new Runnable() {
					@Override
					public void run() {
						if (!status.wantsToStop())
							MainFrame.getInstance().showGraph(superGraph, null);
					}
				}, status);
	}
	
	protected void processPathwayLoading2(final SortedSet<KeggPathwayEntry> entries, final boolean processLabels) {
		boolean inOneView = false;
		boolean isKGMLed = (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR);
		int maxColumns = 3;
		if (entries.size() < 1) {
			MainFrame.showMessageDialog("Please select a pathway or a pathway group from the tree!", "Error");
			return;
		}
		Boolean processMapLinks = null;
		
		if (entries.size() > 1) {
			Object[] res = MyInputHelper.getInput(
					"<html>" +
							"The selected pathways may be loaded into separate windows,<br>" +
							"or into a single window, where the pathways are layout initially in a grid,<br>" +
							"so that they do not overlap. The number of columns, used for this grid-layout<br>" +
							"may be modified with the corresponding setting.",
					"Load into one view?",
					new Object[] {
							"Place pathways in a single view?", new Boolean(true),
							"Maximum number of columns", new Integer(maxColumns),
							(isKGMLed ? "Process map-link references?" : null), (isKGMLed ? new Boolean(true) : null)
					});
			if (res == null)
				return;
			inOneView = (Boolean) res[0];
			maxColumns = (Integer) res[1];
			processMapLinks = (Boolean) res[2];
			if (maxColumns < 1)
				maxColumns = 1;
		}
		
		if (!inOneView) {
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Load pathways:", "");
			
			Runnable backgroundTask1 = new Runnable() {
				@Override
				public void run() {
					int max = entries.size();
					int i = 0;
					for (KeggPathwayEntry myEntry : entries) {
						status.setCurrentStatusText2("Loading " + myEntry.getMapName() + "...");
						status.setCurrentStatusValueFine(100d * i / max);
						KeggService.loadPathway(myEntry, processLabels);
						i++;
						status.setCurrentStatusValueFine(100d * i / max);
					}
					status.setCurrentStatusText2("Finished. Loaded " + max + " pathways.");
				}
			};
			
			BackgroundTaskHelper.issueSimpleTask("Load KEGG Pathways", "Load pathways...", backgroundTask1, null, status);
		} else {
			processPathwayLoading(entries, maxColumns, processMapLinks);
		}
	}
	
	protected void enumerateTree(DefaultMutableTreeNode node, SortedSet<KeggPathwayEntry> entries) {
		if ((node.getUserObject() instanceof KeggPathwayEntry)) {
			KeggPathwayEntry myEntry = (KeggPathwayEntry) node.getUserObject();
			entries.add(myEntry);
		} else {
			for (int i = 0; i < node.getChildCount(); i++)
				enumerateTree((DefaultMutableTreeNode) node.getChildAt(i), entries);
		}
	}
	
	protected void myDoubleClick(final TreePath path) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		SortedSet<KeggPathwayEntry> entries = new TreeSet<KeggPathwayEntry>();
		enumerateTree(node, entries);
		if (entries.size() <= 0)
			MainFrame.showMessageDialog("<html>" +
					"Please select a valid pathway entry!<br>" +
					"(Do not forget to propagate the tree with the pathway list of at least on organism)",
					"Error");
		else
			processPathwayLoading2(entries, getAutoPrettifyLabelSetting());
	}
	
	protected static boolean getAutoPrettifyLabelSetting() {
		if (prettifyLabels == null)
			return true;
		else
			return prettifyLabels.isSelected();
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry, boolean processLabels) {
		KeggService.loadPathway(myEntry, processLabels);
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabKegg() {
		super();
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			this.title = "Load";
		else
			this.title = "KEGG";
		initComponents();
		Release.setFileLoadHelper(this);
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
	public void actionPerformed(ActionEvent e) {
		// String url = null;
		
		if (e.getSource() == getOrganismListFromKegg) {
			final Collection<OrganismEntry> organisms = new ArrayList<OrganismEntry>();
			BackgroundTaskHelper.issueSimpleTask(
					"Retrieve Organism-List",
					"Please wait (retrieve data)...",
					"",
					new Runnable() {
						@Override
						public void run() {
							try {
								KeggHelper h = new KeggHelper();
								organisms.addAll(h.getOrganisms());
								KoService.getInformationLazy("");
							} catch (Exception er) {
								ErrorMsg.addErrorMessage(er);
							}
						}
						
					},
					new Runnable() {
						@Override
						public void run() {
							// organismList.add(new OrganismEntry("map", "Reference Pathways"));
							if (organisms.size() <= 2) {
								MainFrame
										.showMessageDialog(
												"<html>Error: could not retrieve KEGG organism list!<br>Check the error log with <i>Help/Error Messages</i> for detailed information.",
												"Error");
							} else {
								OrganismEntry[] oe = getKEGGorganismFromUser(organisms);
								if (oe != null)
									updateTreeForOrganism(oe);
							}
							// for (OrganismEntry oe : organismList)
							// selectOrganism.addItem(oe);
							// if (selectOrganism.getItemCount()>0) {
							// getOrganismListFromKegg.setText("<html>Select organism:");
							// }
						}
						
					});
		}
		//
		// if (e.getSource() == selectOrganism) {
		// // final OrganismEntry organism = (OrganismEntry) selectOrganism.getSelectedItem();
		// updateTreeForOrganism(null);
		// }
	}
	
	public static OrganismEntry[] getKEGGorganismFromUser(final Collection<OrganismEntry> organisms) {
		final MutableList organismSelection = new MutableList(new DefaultListModel());
		
		organismSelection.setPrototypeCellValue("<html>ÄÖyz");
		organismSelection.setFixedCellWidth(580);
		organismSelection.setFixedCellHeight(new JLabel("<html>AyÖÄ").getPreferredSize().height);
		
		// Collections.sort((List<OrganismEntry>) organisms,
		// new Comparator<OrganismEntry>() {
		// public int compare(final OrganismEntry arg0, OrganismEntry arg1) {
		// if (arg0.getHierarchy().equals("Reference") && arg1.getHierarchy().equals("Reference"))
		// return arg0.getDefinition().compareTo(arg1.getDefinition());
		// if (arg0.getHierarchy().equals("Reference"))
		// return -1;
		// return arg0.toString().compareTo(arg1.toString());
		// }
		// });
		for (OrganismEntry oe : organisms) {
			organismSelection.getContents().addElement(oe);
		}
		organismSelection.setSelectedIndex(0);
		
		final JLabel searchResult = new JLabel("<html><small><font color='gray'>" + organisms.size() + " entries");
		
		JScrollPane organismSelectionScrollPane = new JScrollPane(organismSelection);
		
		organismSelectionScrollPane.setPreferredSize(new Dimension(600, 300));
		
		final JTextField filter = new JTextField("");
		
		filter.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				//
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				//
				
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String filterText = filter.getText().toUpperCase();
						
						organismSelection.getContents().clear();
						for (OrganismEntry oe : organisms) {
							if (oe.toString().toUpperCase().contains(filterText) || oe.getShortName().equals("map") || oe.getShortName().equals("ko"))
								organismSelection.getContents().addElement(oe);
						}
						searchResult.setText("<html><small><font color='gray'>" + organismSelection.getContents().size() + "/" + organisms.size() + " entries shown");
					};
				});
			}
		});
		
		// MyOrganismSelectionDialog osd = new MyOrganismSelectionDialog();
		Object[] result = MyInputHelper.getInput(
				"Please select the desired organisms.<br>" +
						"<small>You may use the Search-Field to locate the " +
						"desired organism.",
				"Select Organisms",
				new Object[] {
						"Select Organisms", organismSelectionScrollPane,
						"Search", filter,
						"", searchResult
				});
		if (result != null && organismSelection.getSelectedValue() != null) {
			Object[] ooo = organismSelection.getSelectedValues();
			ArrayList<OrganismEntry> res = new ArrayList<OrganismEntry>();
			for (Object o : ooo)
				res.add((OrganismEntry) o);
			OrganismEntry[] oe = res.toArray(new OrganismEntry[] {});
			return oe;
		}
		return null;
	}
	
	public void updateTreeForOrganism(final OrganismEntry[] organismlist) {
		keggTree.clearSelection();
		ToolTipManager.sharedInstance().registerComponent(keggTree);
		keggTree.setCellRenderer(new TreeCellRenderer() {
			DefaultTreeCellRenderer tcr = new DefaultTreeCellRenderer();
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component cmp = tcr.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				DefaultMutableTreeNode tn = (DefaultMutableTreeNode) value;
				if (tn.getUserObject() != null && tn.getUserObject() instanceof KeggPathwayEntry) {
					KeggPathwayEntry kpe = (KeggPathwayEntry) tn.getUserObject();
					((JComponent) cmp).setToolTipText(
							"<html><b>" + kpe.getMapName() + "</b>" +
									" (" + kpe.getPathwayName() + ")" +
									// "<br>"+
									// "URL: "+kpe.getPathwayURLstring()+""+
									kpe.getMappingCountDescription("<br>Data Mapping (<br>matching substances</b>/enzymes/compounds/nodes): ") +
									"<br><br><center><b>Double-click to load pathway in a new network view</b></center>"
							);
				} else
					((JComponent) cmp).setToolTipText("<html><b>Double-click to load a group of pathways<br>in separate network views or into a single one</b>");
				return cmp;
			}
		});
		pathwaysOfTreeNode.clear();
		myRootNode.removeAllChildren();
		myRootNode.setUserObject("Selected Organisms");
		if (organismlist != null) {
			for (OrganismEntry organism2 : organismlist) {
				final OrganismEntry organism = organism2;
				final MyDefaultMutableTreeNode myRootNode2 = new MyDefaultMutableTreeNode(organism.toString());
				myRootNode.add(myRootNode2);
				myRootNode2.setUserObject(organism);
				myRootNode2.removeAllChildren();
				((DefaultTreeModel) keggTree.getModel()).reload();
				final Collection<KeggPathwayEntry> pathways = new ArrayList<KeggPathwayEntry>();
				final BackgroundTaskStatusProviderSupportingExternalCall status =
						new BackgroundTaskStatusProviderSupportingExternalCallImpl("Refresh Pathway-List",
								"");
				BackgroundTaskHelper.issueSimpleTask(
						"Refresh Pathway-List",
						"Please wait (SOAP/FTP call and pathway group lookup is performed)...",
						new Runnable() {
							@Override
							public void run() {
								try {
									KeggHelper h = new KeggHelper();
									pathways.addAll(h.getXMLpathways(organism, true, status));
								} catch (Exception er) {
									ErrorMsg.addErrorMessage(er.getLocalizedMessage());
								}
							}
						},
						new Runnable() {
							@Override
							public void run() {
								pathwaysOfTreeNode.put(myRootNode2, pathways);
								buildPathwayTree(null, myRootNode2, pathways);
							}
						}, status);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		// empty
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		//
		
	}
	
	@SuppressWarnings("unchecked")
	private void buildPathwayTree(String searchText, MyDefaultMutableTreeNode myRootNode,
			Collection<KeggPathwayEntry> keggPathways) {
		myRootNode.removeAllChildren();
		Collection<MyDefaultMutableTreeNode> addLater = new ArrayList<MyDefaultMutableTreeNode>();
		if (keggPathways == null)
			return;
		for (KeggPathwayEntry kpe : keggPathways) {
			if (searchText != null && searchText.length() > 0) {
				boolean matchName = kpe.toString().toUpperCase().indexOf(searchText.toUpperCase()) >= 0;
				boolean matchNumber = kpe.getMapName().toUpperCase().indexOf(searchText.toUpperCase()) >= 0;
				if (!matchName && !matchNumber)
					continue;
			}
			MyDefaultMutableTreeNode myNewNode = new MyDefaultMutableTreeNode(kpe);
			
			pathwayToTreeNode.put(kpe, myNewNode);
			
			MyDefaultMutableTreeNode level1 = null;
			MyDefaultMutableTreeNode level2 = null;
			
			String[] group = kpe.getGroupName();
			boolean addAll = true;
			if (addAll) {
				// search level 1
				for (MyDefaultMutableTreeNode tn : addLater) {
					String label = tn.toDefaultString();
					if (label.equalsIgnoreCase(group[0])) {
						level1 = tn;
						break;
					}
				}
				if (level1 == null) {
					// create new group node
					level1 = new MyDefaultMutableTreeNode(group[0]);
					addLater.add(level1);
				}
				// search level 2
				for (int i = 0; i < level1.getChildCount(); i++) {
					MyDefaultMutableTreeNode tn = (MyDefaultMutableTreeNode) level1.getChildAt(i);
					String label = tn.toDefaultString();
					if (label.equalsIgnoreCase(group[1])) {
						level2 = tn;
						break;
					}
				}
				if (level2 == null) {
					// create new group node
					level2 = new MyDefaultMutableTreeNode(group[1]);
					level1.add(level2);
				}
				// end: addAll
			}
			if (level2 != null)
				level2.add(myNewNode);
		}
		Collections.sort((List<MyDefaultMutableTreeNode>) addLater, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				MyDefaultMutableTreeNode n1 = (MyDefaultMutableTreeNode) o1;
				MyDefaultMutableTreeNode n2 = (MyDefaultMutableTreeNode) o2;
				return n1.toDefaultString().compareTo(n2.toDefaultString());
			}
		});
		for (MyDefaultMutableTreeNode level1 : addLater) {
			myRootNode.add(level1);
		}
		((DefaultTreeModel) keggTree.getModel()).reload();
	}
	
	@Override
	public void loadGraphInBackground(File[] files, ActionEvent ae) {
		boolean inOneView = false;
		Boolean processMapLinks = null;
		int maxColumns = 3;
		if (files == null || files.length < 1) {
			return;
		}
		boolean isKGMLed = (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR);
		if (files.length > 1) {
			Object[] res = MyInputHelper.getInput(
					"<html>" +
							"The selected files may be loaded into separate windows,<br>" +
							"or into a single window, where the graphs are layout initially in a grid,<br>" +
							"so that they do not overlap. The number of columns, used for this grid-layout<br>" +
							"may be modified with the corresponding setting." +
							(isKGMLed ? "<br>" +
									"In case multiple files (pathways) are loaded into one view, you may optionally<br>" +
									"resolve defined map links pointing from one loaded pathway to anthother." +
									"" : ""),
					"Load into one view?",
					new Object[] {
							"Place graphs in a single view?", new Boolean(true),
							"Maximum number of columns", new Integer(maxColumns),
							(isKGMLed ? "Process map-link references?" : null), (isKGMLed ? new Boolean(true) : null)
					});
			if (res == null)
				return;
			inOneView = (Boolean) res[0];
			maxColumns = (Integer) res[1];
			processMapLinks = (Boolean) res[2];
			if (maxColumns < 1)
				maxColumns = 1;
		}
		
		if (!inOneView) {
			try {
				MainFrame.getInstance().loadGraphInBackground(files, ae, false);
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else {
			loadGraphGraphsInSingleView(files, maxColumns, processMapLinks);
		}
	}
	
	public void loadGraphGraphsInSingleView(final File[] files, final int maxColumns, final Boolean processMapLinks) {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Load Pathways...", "");
		final Graph superGraph = new AdjListGraph(new ListenerManager());
		BackgroundTaskHelper.issueSimpleTask("Load Files", "Please wait...",
				new Runnable() {
					@Override
					public void run() {
						int maxCol = maxColumns - 1;
						int col = 0;
						double offX = 0;
						double offY = 0;
						double space = 50;
						double maxHeightInColumn = 0;
						double workLoad = files.length;
						double progress = 0;
						for (File file : files) {
							if (status.wantsToStop()) {
								status.setCurrentStatusText1("Processing incomplete!");
								status.setCurrentStatusText2("Operation aborted.");
								break;
							}
							status.setCurrentStatusText1("Load File:");
							status.setCurrentStatusText2(file.getName());
							Graph g = null;
							try {
								g = MainFrame.getInstance().getGraph(file);
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
								continue;
							}
							progress = progress + 1;
							status.setCurrentStatusValueFine(100d * progress / workLoad);
							
							if (g != null && g.getNodes().size() > 0) {
								Vector2d d = NodeTools.getMaximumXY(g.getNodes(), 1d, 0, 0, true);
								for (Node n : g.getNodes()) {
									NodeHelper nh = new NodeHelper(n);
									Point2D p = nh.getPosition();
									nh.setPosition(p.getX() + offX, p.getY() + offY);
									if (nh.getClusterID("").equals("")) {
										String id22 = KeggGmlHelper.getKeggId(g);
										if (id22 != null && id22.length() > 0)
											nh.setClusterID(id22);
										else
											nh.setClusterID("file:" + file.getName());
									}
								}
								for (Edge e : g.getEdges()) {
									EdgeHelper.moveBends(e, offX, offY);
								}
								
								offX += d.x;
								offX += space;
								superGraph.addGraph(g);
								if (d.y > maxHeightInColumn)
									maxHeightInColumn = d.y;
								col++;
								if (col > maxCol) {
									offY += maxHeightInColumn;
									offY += space;
									offX = 0;
									col = 0;
									maxHeightInColumn = 0;
								}
							}
						}
						if (processMapLinks != null && processMapLinks)
							processMapLinks(status, superGraph);
						
						status.setCurrentStatusText1("Processing complete. Initalize graph view.");
						status.setCurrentStatusText2("Please wait. This may take a few moments.");
						status.setCurrentStatusValueFine(-1);
					}
				}, new Runnable() {
					@Override
					public void run() {
						if (!status.wantsToStop())
							MainFrame.getInstance().showGraph(superGraph, null);
					}
				}, status);
		
	}
	
	private static void processMapLinks(final BackgroundTaskStatusProviderSupportingExternalCallImpl status, final Graph superGraph) {
		// processInternalMapLinks
		status.setCurrentStatusText1("Process map-link references between pathways");
		status.setCurrentStatusText2("Step (1/3). Please wait...");
		Pathway pathway = PathwayKeggLoading.processInternalMapLinks(superGraph, null, null);
		status.setCurrentStatusText1("Generate graph from combined pathway");
		status.setCurrentStatusText2("Step (2/3). Please wait...");
		Graph ng = pathway.getGraph();
		status.setCurrentStatusText1("Update graph information");
		status.setCurrentStatusText2("Step (3/3). Please wait...");
		superGraph.clear();
		superGraph.addGraph(ng);
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	public void setShowAnalyzeOption(boolean showAnalyzeOption) {
		this.showAnalyzeOption = showAnalyzeOption;
	}
	
}
