package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.ErrorMsg;
import org.JMButton;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public abstract class PathwayWebLinkTab extends InspectorTab {
	private static final long serialVersionUID = -3026855152800218905L;
	private final String title, webAddress, content, contentSingle, infoURL;
	protected PathwayWebLinkTreeNode myRootNode;
	
	private final HashMap<String, PathwayWebLinkTreeNode> group2treeNode = new HashMap<String, PathwayWebLinkTreeNode>();
	
	private final boolean ommitIfEmtpyGroup;
	private boolean showGraphExtensions = false;
	private final String downloadButtonText;
	
	public PathwayWebLinkTab(String title, String url, String content, String contentSingle, String infoURL,
						boolean ommitEmptyGroupItems) {
		this(title, url, content, contentSingle, infoURL, ommitEmptyGroupItems, "<html>Download selected " + content);
	}
	
	public PathwayWebLinkTab(String title, String url, String content, String contentSingle, String infoURL,
						boolean ommitEmptyGroupItems, String downloadButtonText) {
		super();
		this.title = title;
		this.webAddress = url;
		this.content = content;
		this.contentSingle = contentSingle;
		this.infoURL = infoURL;
		this.ommitIfEmtpyGroup = ommitEmptyGroupItems;
		myRootNode = new PathwayWebLinkTreeNode(this.title);
		this.downloadButtonText = downloadButtonText;
		initComponents();
	}
	
	protected void initComponents() {
		double border = 5;
		double[][] size = {
							{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, 3, TableLayoutConstants.PREFERRED, 5,
												TableLayoutConstants.PREFERRED, 3, TableLayoutConstants.FILL, border } }; // Rows
		this.setLayout(new TableLayout(size));
		
		final JTree pathwayList = getPathwayList();
		
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2) {
					e.consume();
					getDownloadFilesActionListener(pathwayList).actionPerformed(null);
				}
			}
		};
		pathwayList.addMouseListener(ml);
		
		add(getDownloadButton(pathwayList, "Get list of " + content), "1,1");
		
		add(getLoadDataButton(pathwayList), "1,3");
		
		add(new JLabel("<html>Select " + content + " from the list and use the download button "
							+ "to download and view " + title.toLowerCase() + " (" + infoURL + ")."), "1,5");
		
		add(new JScrollPane(pathwayList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), "1,7");
		
		validate();
	}
	
	private JButton getLoadDataButton(final JTree pathwayList) {
		JButton result = new JMButton(downloadButtonText);
		
		result.setOpaque(false);
		result.addActionListener(getDownloadFilesActionListener(pathwayList));
		return result;
	}
	
	protected ActionListener getDownloadFilesActionListener(final JTree pathwayList) {
		return new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final Collection<PathwayWebLinkItem> selection = getPathwaySelection(pathwayList);
				if (selection.isEmpty()) {
					MainFrame.showMessageDialog("<html>" + "Please select at least one " + contentSingle
										+ " from the list.<br>" + "If the list is empty, please use the action command to fill the list.",
										"Information");
				}
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
									"Load Data...", "Initialize...");
				final Collection<Graph> graphs = new ArrayList<Graph>();
				BackgroundTaskHelper.issueSimpleTask(title, "Load Data...", new Runnable() {
					public void run() {
						int i = 1;
						int max = selection.size();
						for (PathwayWebLinkItem mc : selection) {
							IOurl url;
							try {
								url = mc.getURL();
								status.setCurrentStatusText1("Load " + contentSingle + " " + i + "/" + max);
								status.setCurrentStatusText2(url.toString());
								System.out.println("Load: " + url.toString());
								if (!MainFrame.getInstance().lookUpAndSwitchToNamedSession(url.toString())) {
									Graph g = MainFrame.getInstance().getGraph(url, mc.getFileName());
									if (g != null) {
										addAnnotationsToGraphElements(g);
										graphs.add(g);
									}
								}
								status.setCurrentStatusValueFine(100d * i / max);
								i++;
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
						status.setCurrentStatusValue(100);
						status.setCurrentStatusText1(content + " are loaded");
						status.setCurrentStatusText2("Create graph view(s)");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// empty
						}
					}
				}, new Runnable() {
					public void run() {
						for (Graph g : graphs)
							MainFrame.getInstance().showGraph(g, arg0);
						status.setCurrentStatusText1("Processing finished");
						status.setCurrentStatusText2("");
					}
				}, status);
			}
		};
	}
	
	protected abstract void addAnnotationsToGraphElements(Graph graph);
	
	private JTree getPathwayList() {
		JTree result = new JTree(new DefaultTreeModel(myRootNode));
		return result;
	}
	
	private JButton getDownloadButton(final JTree pathwayTree, String desc) {
		JButton result = new JMButton(desc);
		
		result.setOpaque(false);
		result.addActionListener(getLoadActionListener(pathwayTree));
		return result;
	}
	
	public ActionListener getLoadActionListener(final JTree pathwayTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pathwayTree.clearSelection();
				myRootNode.removeAllChildren();
				group2treeNode.clear();
				try {
					for (PathwayWebLinkItem i : WebDirectoryFileListAccess.getWebDirectoryFileListItems(webAddress,
										getValidExtensions(), showGraphExtensions))
						addTreeChild(i);
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
					MainFrame.showMessageDialog("I/O-Exception: " + e.toString(), "Error");
				}
				((DefaultTreeModel) pathwayTree.getModel()).reload();
			}
		};
	}
	
	protected abstract String[] getValidExtensions();
	
	protected void addTreeChild(PathwayWebLinkItem i) {
		String group1 = i.getGroup1();
		String group2 = i.getGroup2();
		if (ommitIfEmtpyGroup && (group1 == null || group1.trim().length() <= 0))
			return;
		if (ommitIfEmtpyGroup && group2 != null && group2.trim().length() <= 0)
			return;
		
		PathwayWebLinkTreeNode tn = group2treeNode.get(group1);
		if (tn == null) {
			PathwayWebLinkTreeNode groupNode = new PathwayWebLinkTreeNode(group1);
			myRootNode.add(groupNode);
			tn = groupNode;
			group2treeNode.put(group1, tn);
		}
		PathwayWebLinkTreeNode tn2 = null;
		if (group2 != null) {
			tn2 = group2treeNode.get(group2);
			if (tn2 == null) {
				PathwayWebLinkTreeNode groupNode2 = new PathwayWebLinkTreeNode(group2);
				tn.add(groupNode2);
				tn2 = groupNode2;
				group2treeNode.put(group2, tn2);
			}
		}
		if (tn2 != null) {
			tn.add(tn2);
			tn2.add(new PathwayWebLinkTreeNode(i));
		} else
			tn.add(new PathwayWebLinkTreeNode(i));
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public void postAttributeAdded(AttributeEvent e) {
		//
		
	}
	
	public void postAttributeChanged(AttributeEvent e) {
		//
		
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeAdded(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeChanged(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
		//
		
	}
	
	public void transactionFinished(TransactionEvent e) {
		//
		
	}
	
	public void transactionStarted(TransactionEvent e) {
		//
		
	}
	
	protected Collection<PathwayWebLinkItem> getPathwaySelection(JTree pathwayList) {
		HashSet<PathwayWebLinkItem> resultA = new HashSet<PathwayWebLinkItem>();
		ArrayList<PathwayWebLinkItem> resultB = new ArrayList<PathwayWebLinkItem>();
		TreePath[] tp = pathwayList.getSelectionPaths();
		if (tp != null && tp.length > 0) {
			for (TreePath t : tp) {
				Object o = t.getLastPathComponent();
				if (o instanceof PathwayWebLinkTreeNode) {
					PathwayWebLinkTreeNode mc = (PathwayWebLinkTreeNode) o;
					if (mc.getMetaCropListItem() != null) {
						if (!resultA.contains(mc.getMetaCropListItem())) {
							resultA.add(mc.getMetaCropListItem());
							resultB.add(mc.getMetaCropListItem());
						}
					} else {
						for (int i = 0; i < mc.getChildCount(); i++) {
							TreeNode tn = mc.getChildAt(i);
							if (tn instanceof PathwayWebLinkTreeNode) {
								PathwayWebLinkTreeNode mmc = (PathwayWebLinkTreeNode) tn;
								if (mmc.getMetaCropListItem() != null) {
									if (!resultA.contains(mmc.getMetaCropListItem())) {
										resultA.add(mmc.getMetaCropListItem());
										resultB.add(mmc.getMetaCropListItem());
									}
								} else {
									PathwayWebLinkTreeNode mmmc = mmc;
									for (int ii = 0; ii < mmmc.getChildCount(); ii++) {
										TreeNode ttn = mmmc.getChildAt(ii);
										if (ttn instanceof PathwayWebLinkTreeNode) {
											PathwayWebLinkTreeNode mmmmc = (PathwayWebLinkTreeNode) ttn;
											if (mmmmc.getMetaCropListItem() != null) {
												if (!resultA.contains(mmmmc.getMetaCropListItem())) {
													resultA.add(mmmmc.getMetaCropListItem());
													resultB.add(mmmmc.getMetaCropListItem());
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
		}
		return resultB;
	}
	
	public void setShowGraphExtensions(boolean show) {
		this.showGraphExtensions = show;
	}
	
}