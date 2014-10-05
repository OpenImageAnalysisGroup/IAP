package de.ipk.ag_ba.gui.picture_gui;

import iap.blocks.data_structures.CalculatedProperty;
import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.JMButton;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class SupplementaryFilePanelMongoDB extends JPanel implements ActionListener, StatusDisplay {
	private static final long serialVersionUID = 2171413300210427409L;
	
	private static Stack<File> tempFiles;
	
	static JFileChooser fileChooser = new JFileChooser();
	
	final JTree expTree;
	
	private MyDropTarget currentDropTarget;
	
	// private static List<WeakReference<JTree>> myTrees;
	
	private void processEditAddFile() {
		final MyDropTarget targetDropTarget = currentDropTarget;
		if (targetDropTarget == null) {
			JOptionPane.showMessageDialog(this, "Images can not be added to current database node.\n"
					+ "Select an valid experiment-data node and try again.", "Image upload not possible",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// in case it is no DBE-TreeNode or if it is one and it is Read-Only
		// then reject drop
		if (targetDropTarget.isTargetReadOnly()) {
			JOptionPane.showMessageDialog(this, "Image can not be added to current database node.",
					"No write-access to this entity of the experiment",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Add file(s) or directory content");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		if (fc.showDialog(this, "Add file(s) or directory content") == JFileChooser.APPROVE_OPTION) {
			final File files[] = fc.getSelectedFiles();
			
			try {
				LocalComputeJob writeThread = new LocalComputeJob(new Runnable() {
					@Override
					public void run() {
						
						for (int i = 0; i < files.length; i++) {
							File file = files[i];
							if (file.isDirectory())
								try {
									targetDropTarget.processDirectory(file);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							else
								try {
									targetDropTarget.addImageOrFileToDatabase(file, false);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
						
					}
				}, "file strage thread");
				BackgroundThreadDispatcher.addTask(writeThread);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	DataSetFilePanel currentFilePanel = null;
	
	private final DataSetFilePanel buttonView;
	
	public SupplementaryFilePanelMongoDB(final ExperimentReference doc,
			String experimentName) {
		final SupplementaryFilePanelMongoDB thisPanel = this;
		
		BackgroundThreadDispatcher.setFrameInstance(this);
		
		JButton addButton = new JMButton("Add Files...");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processEditAddFile();
			}
		});
		
		final DataSetFilePanel filePanel = new DataSetFilePanel();
		
		buttonView = filePanel;
		ArrayList<JButton> knownButtons = new ArrayList<JButton>();
		JButton[] actions = buttonView == null ? null : new JButton[] {
				new JButton(getAction("<html><u>Small", 128, knownButtons)),
				new JButton(getAction("<html>Middle", 256, knownButtons)),
				new JButton(getAction("<html>Large", 512, knownButtons))
		};
		for (JButton jb : actions)
			knownButtons.add(jb);
		final FilePanelHeader filePanelHeader = new FilePanelHeader(addButton, actions, "Icon Size >");
		filePanel.init(filePanelHeader, knownButtons);
		
		currentFilePanel = filePanel;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		
		// todo if mongo knows this ID as an experiment ID
		boolean readOnly = !(doc != null && doc.getHeader() != null
				&& doc.getHeader().getDatabaseId() != null);
		
		readOnly = !IAPservice.getIsAnnotationSavePossible(doc);
		
		expTree = new JTree(new ExperimentTreeModel(this, doc, readOnly));
		
		DBEtreeCellRenderer cir = new DBEtreeCellRenderer();
		cir.setCameraRendererIcon(new ImageIcon(IAPimages.getImage(IAPimages.getCamera(), 16)));
		cir.setGroupRendererIcon(new ImageIcon(IAPimages.getImage(IAPimages.getSystemWheel(), 16)));
		cir.setTimeRendererIcon(new ImageIcon(IAPimages.getImage(IAPimages.getClock(), 16)));
		
		ToolTipManager.sharedInstance().registerComponent(expTree);
		
		expTree.setCellRenderer(cir);
		// myTrees.add(new WeakReference<JTree>(expTree));
		expTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() == null || e.getNewLeadSelectionPath().getLastPathComponent() == null)
					return;
				
				final Object mt = e.getNewLeadSelectionPath().getLastPathComponent();
				if (mt instanceof MongoTreeNode && ((MongoTreeNode) mt).getTargetEntity() != null) {
					final MongoTreeNode mtdbe = (MongoTreeNode) mt;
					MongoTreeNode projectNode = mtdbe.getProjectNode();
					try {
						projectNode.updateSizeInfo(thisPanel);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					MyDropTarget myDropTarget = new MyDropTarget(filePanel, mtdbe, expTree);
					currentDropTarget = myDropTarget;
					filePanel.setDropTarget(myDropTarget);
					
					filePanel.setFiller(new Runnable() {
						@Override
						public void run() {
							String desc = "";
							String dd = CalculatedProperty.getDescriptionFor(mtdbe.getTargetEntity());
							if (dd != null) {
								SubstanceInterface si = null;
								if (mtdbe.getTargetEntity() instanceof SubstanceInterface)
									si = (SubstanceInterface) mtdbe.getTargetEntity();
								if (mtdbe.getTargetEntity() instanceof ConditionInterface)
									si = ((ConditionInterface) mtdbe.getTargetEntity()).getParentSubstance();
								desc = "<table cellspacing=0 cellpadding=2 border=0><tr><td bgcolor='FFDDDD'>Reference Information for <b>" + si.getName()
										+ "</b></td></tr><tr><td bgcolor='#FFEEEE '>"
										+ dd + "</td></tr></table>";
							}
							filePanel.removeAll();
							filePanel.setLayout(new FlowLayout(filePanel.getWidth(), 10, 10));
							if (!((MongoTreeNodeBasis) mt).readOnly) {
								String msg = "<font color='black'>"
										+ (desc.isEmpty() ? "You may also use drag+drop to add files to the currently selected entity of the experiment." : "") + desc;
								filePanel.setHeader(true, msg, false, true);
							} else {
								filePanel
										.setHeader(
												false,
												"<font color='black'>"
														+ (desc.isEmpty() ? "Additional files can't be assigned to this entity." : "") + desc,
												true, true);
							}
							
							filePanel.validate();
							filePanel.repaint();
							
							removeTempFiles();
							try {
								DataExchangeHelperForExperiments.fillFilePanel(filePanel, mtdbe, expTree, IAPservice.getIsAnnotationSavePossible(doc));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});
					
					filePanel.fill();
				} else {
					currentDropTarget = null;
					filePanel.setDropTarget(null);
					filePanel.removeAll();
					
					filePanel.setFiller(new Runnable() {
						@Override
						public void run() {
							filePanel.setLayout(new FlowLayout(filePanel.getWidth(), 10, 10));
							if (!((MongoTreeNodeBasis) mt).readOnly)
								filePanel
										.setHeader(
												false,
												"<font color='black'>To assign data, please select the experiment-node, a condition, timepoint or measurement value.",
												true, true);
							else
								filePanel
										.setHeader(
												false,
												"<font color='black'>Additional files can't be assigned to this entity.",
												true, true);
							
							filePanel.validate();
							filePanel.repaint();
						}
					});
					
					filePanel.fill();
				}
			}
		});
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					expTree.setSelectionPath(new TreePath(expTree.getModel().getRoot()));
				} catch (Exception e) {
					// empty
				}
			}
		});
		
		JScrollPane fileScroller = new JScrollPane(filePanel);
		fileScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		filePanel.setScrollpane(fileScroller);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(expTree), TableLayout
				.getSplitVertical(filePanelHeader, fileScroller, TableLayout.PREFERRED, TableLayout.FILL));
		splitPane.setOneTouchExpandable(true);
		splitPane.setLastDividerLocation(200);
		splitPane.setDividerLocation(200);
		
		add(splitPane);
		
		validate();
	}
	
	private Action getAction(String string, final int size, final ArrayList<JButton> knownButtons) {
		return new AbstractAction(string) {
			
			@Override
			public boolean isEnabled() {
				return DataSetFileButton.ICON_WIDTH != size;
			}
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				buttonView.getModifyButtonSize(size).actionPerformed(arg0);
				for (JButton jb : knownButtons) {
					jb.setEnabled(jb.getAction().isEnabled());
				}
			}
		};
	}
	
	public static void addTempFileToBeDeletedLater(File tempFile) {
		if (tempFiles == null)
			tempFiles = new Stack<File>();
		tempFiles.add(tempFile);
	}
	
	void removeTempFiles() {
		if (tempFiles == null)
			return;
		if (BackgroundThreadDispatcher.getWorkLoad() > 0)
			return;
		while (tempFiles.size() > 0) {
			File tempFile = tempFiles.pop();
			tempFile.delete();
		}
	}
	
	public void repaintTree() {
		if (SwingUtilities.isEventDispatchThread()) {
			if (expTree != null) {
				expTree.invalidate();
				expTree.repaint();
			}
		} else
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (expTree != null) {
						expTree.invalidate();
						expTree.repaint();
					}
				}
			});
	}
	
	public static void showError(String string, Exception e) {
		String s = e != null ? e.getMessage() : "";
		MainFrame.showMessageDialogWithScrollBars2("Message: " + string + "\n\nError: " + s, "Error");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		repaintTree();
	}
	
	@Override
	public String getTitle() {
		return "";
	}
	
	@Override
	public void setTitle(String message) {
		if (currentFilePanel != null)
			currentFilePanel.setHeader(currentFilePanel.getIsButtonEnabled(), "<code>"
					+ StringManipulationTools.UnicodeToHtml(message), currentFilePanel.getIsWarningDisplayed(), false);
	}
}
