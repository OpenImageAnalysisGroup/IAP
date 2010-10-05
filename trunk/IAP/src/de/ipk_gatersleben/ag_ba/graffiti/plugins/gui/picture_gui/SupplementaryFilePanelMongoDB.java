package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Stack;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.JMButton;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;

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
				Thread writeThread = new Thread(new Runnable() {
					public void run() {

						for (int i = 0; i < files.length; i++) {
							File file = files[i];
							if (file.isDirectory())
								targetDropTarget.processDirectory(file);
							else
								targetDropTarget.addImageToDatabase(file, false);
						}

					}
				});
				writeThread.setPriority(Thread.MIN_PRIORITY);
				BackgroundThreadDispatcher.addTask(writeThread, -1);
			} catch (Exception err) {
				JOptionPane.showMessageDialog(null, "Error: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	JMyFilePanel currentFilePanel = null;

	public SupplementaryFilePanelMongoDB(final String login, final String password, ExperimentInterface doc,
			String experimentName) {

		final SupplementaryFilePanelMongoDB thisPanel = this;

		BackgroundThreadDispatcher.setFrameInstance(this);

		JButton addButton = new JMButton("Add Files...");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processEditAddFile();
			}
		});

		final FilePanelHeader filePanelHeader = new FilePanelHeader(addButton);
		final JMyFilePanel filePanel = new JMyFilePanel(filePanelHeader);

		currentFilePanel = filePanel;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// todo if mongo knows this ID as an experiment ID
		boolean readOnly = doc.getHeader().getExcelfileid() != null;

		expTree = new JTree(new DBEtreeModel(this, login, password, doc, readOnly));
		expTree.setCellRenderer(new DBEtreeCellRenderer());
		// myTrees.add(new WeakReference<JTree>(expTree));

		expTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getNewLeadSelectionPath() == null || e.getNewLeadSelectionPath().getLastPathComponent() == null)
					return;

				Object mt = e.getNewLeadSelectionPath().getLastPathComponent();
				if (mt instanceof MongoTreeNode && ((MongoTreeNode) mt).getTargetEntity() != null) {
					MongoTreeNode mtdbe = (MongoTreeNode) mt;
					MongoTreeNode projectNode = mtdbe.getProjectNode();
					projectNode.updateSizeInfo(login, password, thisPanel);
					filePanel.removeAll();
					filePanel.setLayout(new FlowLayout(filePanel.getWidth(), 10, 10));
					if (!((MongoTreeNodeBasis) mt).readOnly) {
						String msg = "<font color='black'>You may also use drag+drop to upload files to the database and to assign them to this entry";
						filePanel.setHeader(true, msg, false, true);
					} else {
						filePanel.setHeader(false, "<font color='black'>You don't have write access to this experiment",
								true, true);
					}

					MyDropTarget myDropTarget = new MyDropTarget(login, password, filePanel, mtdbe, expTree);
					filePanel.setDropTarget(myDropTarget);

					currentDropTarget = myDropTarget;

					filePanel.validate();
					filePanel.repaint();

					removeTempFiles();

					DataExchangeHelperForExperiments.fillFilePanel(filePanel, mtdbe, expTree, login, password);
				} else {
					filePanel.removeAll();
					filePanel.setLayout(new FlowLayout(filePanel.getWidth(), 10, 10));
					if (!((MongoTreeNodeBasis) mt).readOnly)
						filePanel
								.setHeader(
										false,
										"<font color='black'>To assign data, please select the experiment-node, a condition, timepoint or measurement value",
										true, true);
					else
						filePanel.setHeader(false, "<font color='black'>You don't have write access to this experiment",
								true, true);

					filePanel.setDropTarget(null);

					currentDropTarget = null;

					filePanel.validate();
					filePanel.repaint();
				}
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
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
		filePanel.scrollpane = fileScroller;
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

	public void actionPerformed(ActionEvent e) {
		repaintTree();
	}

	public String getTitle() {
		return "";
	}

	public void setTitle(String message) {
		currentFilePanel.setHeader(currentFilePanel.getIsButtonEnabled(), "<code>"
				+ StringManipulationTools.UnicodeToHtml(message), currentFilePanel.getIsWarningDisplayed(), false);
	}
}
