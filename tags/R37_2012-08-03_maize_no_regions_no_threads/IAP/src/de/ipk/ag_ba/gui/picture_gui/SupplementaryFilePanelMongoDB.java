package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Stack;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.JMButton;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

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
			JOptionPane.showMessageDialog(this, "Image can not be added to current database node.\n"
					+ "This experiment is loaded in Read-Only Mode for your Accout!", "No write-access to experiment",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Add File(s) to Database");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		if (fc.showDialog(this, "Add File(s) to Database") == JFileChooser.APPROVE_OPTION) {
			final File files[] = fc.getSelectedFiles();
			
			try {
				MyThread writeThread = new MyThread(new Runnable() {
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
									targetDropTarget.addImageToDatabase(file, false);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
						
					}
				}, "database write thread");
				// writeThread.setPriority(Thread.MIN_PRIORITY);
				BackgroundThreadDispatcher.addTask(writeThread, -1, 0, true);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	DataSetFilePanel currentFilePanel = null;
	
	public SupplementaryFilePanelMongoDB(final MongoDB m, ExperimentInterface doc,
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
		
		final FilePanelHeader filePanelHeader = new FilePanelHeader(addButton);
		final DataSetFilePanel filePanel = new DataSetFilePanel(filePanelHeader);
		
		currentFilePanel = filePanel;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		ToolTipManager.sharedInstance().setDismissDelay(30000);
		
		// todo if mongo knows this ID as an experiment ID
		boolean readOnly = doc.getHeader().getDatabaseId() != null;
		
		expTree = new JTree(new ExperimentTreeModel(this, m, doc, readOnly));
		
		ToolTipManager.sharedInstance().registerComponent(expTree);
		
		expTree.setCellRenderer(new DBEtreeCellRenderer());
		// myTrees.add(new WeakReference<JTree>(expTree));
		expTree.setCellRenderer(new DefaultTreeCellRenderer() {
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				JLabel ccc = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				String tt = ((MongoTreeNode) value).getTooltipInfo();
				ccc.setToolTipText(tt);
				return ccc;
			}
		});
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
						projectNode.updateSizeInfo(m, thisPanel);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					MyDropTarget myDropTarget = new MyDropTarget(m, filePanel, mtdbe, expTree);
					currentDropTarget = myDropTarget;
					filePanel.setDropTarget(myDropTarget);
					
					filePanel.setFiller(new Runnable() {
						@Override
						public void run() {
							filePanel.removeAll();
							filePanel.setLayout(new FlowLayout(filePanel.getWidth(), 10, 10));
							MongoTreeNode mtn = (MongoTreeNode) mt;
							if (!((MongoTreeNodeBasis) mt).readOnly) {
								String msg = "<font color='black'>You may also use drag+drop to upload files to the database and to assign them to this entry";
								filePanel.setHeader(true, msg, false, true);
							} else {
								filePanel.setHeader(false,
										"<font color='black'>You don't have write access to this experiment", true, true);
							}
							
							filePanel.validate();
							filePanel.repaint();
							
							removeTempFiles();
							try {
								DataExchangeHelperForExperiments.fillFilePanel(filePanel, mtdbe, expTree, m);
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
												"<font color='black'>To assign data, please select the experiment-node, a condition, timepoint or measurement value",
												true, true);
							else
								filePanel.setHeader(false,
										"<font color='black'>You don't have write access to this experiment", true, true);
							
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
		MainFrame.showMessageDialogWithScrollBars2(s, "Error: " + string);
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
