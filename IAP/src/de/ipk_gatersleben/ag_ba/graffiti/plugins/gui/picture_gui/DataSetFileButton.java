package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.HomeFolder;
import org.graffiti.editor.MainFrame;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.AIPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ExperimentIOManager;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.FileSystemHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.VolumeData;
import de.ipk_gatersleben.ag_pbi.vanted3d.mapping.MappingResultGraph;
import de.ipk_gatersleben.ag_pbi.vanted3d.mapping.MappingResultGraphNode;
import de.ipk_gatersleben.ag_pbi.vanted3d.views.ThreeDview;

/**
 * @author Christian Klukas
 */
public class DataSetFileButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected static final int ICON_HEIGHT = 128;
	protected static final int ICON_WIDTH = 128;
	private JMenuItem showImageCmd;
	private JMenuItem showVolumeCmd;
	private JMenuItem openFileCmd;
	private JMenuItem saveFileCmd;
	private JMenuItem removeOneFromDatabaseCmd;
	private JMenuItem removeAllFromDatabaseCmd;

	String user, pass;

	JProgressBar progress;
	private final MongoTreeNode targetTreeNode;
	public MyImageIcon myImage;

	ImageResult imageResult = null;

	private boolean readOnly;

	volatile boolean downloadInProgress = false;
	boolean downloadNeeded = false;
	private ActionListener sizeChangedListener;

	public int getIsJavaImage() {
		if (myImage == null)
			return 0;
		return myImage.imageAvailable;
	}

	// public JMyPC2DBEbutton(String user, String pass, DBEtreeNode projectNode,
	// ImageResult imageResult, ActionListener sizeChangedListener) {
	// this(user, pas, projectNode, imageResult.getFileName(),
	// imageResult.getPreviewIcon(), null);
	// this.imageResult=imageResult;
	// this.targetTreeNode=projectNode;
	// this.sizeChangedListener = sizeChangedListener;
	// this.user = user;
	// this.pass = pass;
	// }

	public DataSetFileButton(String user, String pass, MongoTreeNode targetTreeNode, String label, MyImageIcon icon,
			ImageIcon previewImage) {
		super();

		progress = new JProgressBar(0, 100);
		progress.setValue(-1);

		progress.setVisible(false);

		updateLayout(label, icon, previewImage);

		addActionListener(this);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		this.targetTreeNode = targetTreeNode;

		this.user = user;
		this.pass = pass;
	}

	JLabel mmlbl;

	public void updateLayout(String label, MyImageIcon icon, ImageIcon previewImage) {
		double border = 2;
		removeAll();
		JLabel ilbl;
		if (icon != null)
			ilbl = new JLabel(icon);
		else
			ilbl = new JLabel(previewImage);

		double[][] size = {
				{ border, TableLayout.FILL, border }, // Columns
				{ border, DataSetFileButton.ICON_HEIGHT, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED,
						border } }; // Rows

		setLayout(new TableLayout(size));

		ilbl.setHorizontalAlignment(JLabel.CENTER);
		ilbl.setVerticalAlignment(JLabel.CENTER);
		// new IconAdapter(icon)
		add(ilbl, "1,1,c,c");

		add(progress, "1,3,c,c");
		if (label != null) {
			mmlbl = new JLabel("<html><center>" + label);
			mmlbl.setHorizontalAlignment(JLabel.CENTER);
		}
		add(mmlbl, "1,5,c,c");
		myImage = icon;
		validate();
	}

	public DataSetFileButton(String user, String pass, MongoTreeNode projectNode, ImageResult imageResult,
			ImageIcon previewImage, boolean readOnly) {
		this(user, pass, projectNode, "<html><body><b>" + getMaxString(strip(imageResult.getFileName()))
				+ "</b></body></html>", null, previewImage);
		this.imageResult = imageResult;
		if (imageResult == null)
			System.out.println("Error: Image Reference Data is null!");
		this.readOnly = readOnly;
		if (getMaxString(imageResult.getFileName()).endsWith("..."))
			setToolTipText(imageResult.getFileName());
	}

	private static String strip(String fileName) {
		if (fileName.contains(File.separator))
			return fileName.substring(fileName.lastIndexOf(File.separator) + File.separator.length());
		else
			return fileName;
	}

	public static String getMaxString(String fileName) {
		int maxlen = 20;
		if (fileName.length() < maxlen)
			return fileName;
		else {
			return fileName.substring(0, maxlen - 3) + "...";
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension((int) d.getWidth() - 10, (int) d.getHeight());
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent evt) {

		if (evt.getSource() == this) {
			final JPopupMenu myPopup = new JPopupMenu();
			if (downloadNeeded) {
				JMenuItem tempItem = new JMenuItem("Download file...");
				tempItem.setEnabled(false);
				myPopup.add(tempItem);

				final DataSetFileButton thisInstance = this;
				downloadNeeded = false;
				setProgressValue(0);
				showProgressbar();
				Thread download = new Thread(new Runnable() {
					public void run() {
						downloadInProgress = true;
						final File tf;
						try {
							tf = File.createTempFile("dbe_", "." + imageResult.getFileName());
							SupplementaryFilePanelMongoDB.addTempFileToBeDeletedLater(tf);
						} catch (IOException e2) {
							SupplementaryFilePanelMongoDB.showError("Could not create temp file for storing database image!",
									e2);
							downloadNeeded = true;
							return;
						}

						try {
							InputStream is = ExperimentIOManager.getInputStream(imageResult.getBinaryFileInfo().getFileName());
							if (is == null)
								System.out.println("Inputstream = null");
							HomeFolder.copyFile(is, tf);
						} catch (Exception e1) {
							System.out.println("No valid input stream for "
									+ imageResult.getBinaryFileInfo().getFileName().toString());

							MappingDataEntity mde = targetTreeNode.getTargetEntity();
							try {
								VolumeData volume = (VolumeData) mde;
								if (volume != null)
									DataExchangeHelperForExperiments.downloadFile(user, pass, imageResult, tf, thisInstance,
											MongoCollection.VOLUMES);
							} catch (Exception e) {
								DataExchangeHelperForExperiments.downloadFile(user, pass, imageResult, tf, thisInstance,
										MongoCollection.IMAGES);
							}
						}

						if (tf != null) {
							imageResult.downloadedFile = tf;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									try {
										myImage = new MyImageIcon(MainFrame.getInstance(), DataSetFileButton.ICON_WIDTH,
												DataSetFileButton.ICON_HEIGHT, FileSystemHandler.getURL(tf),
												myImage != null ? myImage.getBinaryFileInfo() : null);
									} catch (MalformedURLException e) {
										downloadNeeded = true;
										SupplementaryFilePanelMongoDB.showError("URL Format Error", e);
									}
								}
							});
						}

						downloadInProgress = false;
						hideProgressbar();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (myPopup.isVisible()) {
									myPopup.setVisible(false);
									JPopupMenu myPopup2 = new JPopupMenu();
									addDefaultCommands(myPopup2);
									myPopup2.validate();
									myPopup2.show(thisInstance, 5, 5);
								}
							}
						});
					}
				});
				BackgroundThreadDispatcher.addTask(download, 1);
			} else if (downloadInProgress) {
				JMenuItem tempItem = new JMenuItem("Download in progress...");
				tempItem.setEnabled(false);
				myPopup.add(tempItem);
			} else {
				addDefaultCommands(myPopup);
			}
			myPopup.show(this, 5, 5);
		}
		if (evt.getSource() == saveFileCmd) {
			// imageResult.fileName
			// myImage.fileURL
			SupplementaryFilePanelMongoDB.fileChooser.setSelectedFile(new File(imageResult.getFileName()));
			if (SupplementaryFilePanelMongoDB.fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
				File outputFile = SupplementaryFilePanelMongoDB.fileChooser.getSelectedFile();

				FileInputStream in;
				try {
					in = new FileInputStream(imageResult.downloadedFile);
					FileOutputStream out = new FileOutputStream(outputFile);

					byte[] buffer = new byte[1024];
					int bytes_read;
					while (true) {
						bytes_read = in.read(buffer);
						if (bytes_read == -1)
							break;
						out.write(buffer, 0, bytes_read);
					}

					in.close();
					out.close();
				} catch (FileNotFoundException e) {
					SupplementaryFilePanelMongoDB.showError("File not found.", e);
				} catch (IOException e) {
					SupplementaryFilePanelMongoDB.showError("IO Exception", e);
				}
			}
		}
		if (evt.getSource() == showVolumeCmd) {
			MappingResultGraph mrg = new MappingResultGraph();
			VolumeData volume = (VolumeData) imageResult.getBinaryFileInfo().getEntity();
			MappingResultGraphNode mrgn = mrg.createVolumeNode(volume, null);
			mrgn.getParams().setURL(FileSystemHandler.getURL(imageResult.getDownloadedFile()));

			EditorSession session = MainFrame.getInstance().createNewSession(mrg.getGraph());

			MainFrame.getInstance().createInternalFrame(ThreeDview.class.getCanonicalName(), session, false);

			AIPmain.showVANTED();
			//
			// MainFrame.showMessageWindow("3D-Volume " +
			// imageResult.getDownloadedFile().getAbsolutePath(), view
			// .getViewComponent());
		}
		if (evt.getSource() == showImageCmd) {
			JFrame myImageFrame = new JFrame("Image View");
			JComponent cp = (JComponent) myImageFrame.getContentPane();
			if (myImage != null && this.myImage.imageAvailable != 1) {
				JOptionPane.showMessageDialog(null, "Format of this file is unknown. Image can not be shown.",
						"Unknown Image Format", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

			ImageIcon i;
			if (imageResult.getFileName().contains(File.separator))
				i = new ImageIcon(imageResult.getFileName());
			else
				try {
					i = new ImageIcon(ImageIO.read(ExperimentIOManager.getInputStream(myImage.fileURL)));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					i = null;
				}

			cp.add(new JScrollPane(new JLabel(i)));
			myImageFrame.getContentPane().validate();
			myImageFrame.setSize(new Dimension(i.getImage().getWidth(cp) + 30, i.getImage().getHeight(cp) + 40));
			if (imageResult.getFileName().contains(File.separator))
				myImageFrame.setTitle("Image View - " + imageResult.getFileName());
			else
				myImageFrame.setTitle("Image View - " + myImage.fileURL);
			myImageFrame.setVisible(true);
		}
		if (evt.getSource() == openFileCmd) {
			if (imageResult.getFileName().contains(File.separator))
				AttributeHelper.showInBrowser(imageResult.getFileName());
			else
				AttributeHelper.showInBrowser(myImage.fileURL.toString());

		}
		if (evt.getSource() == removeOneFromDatabaseCmd) {
			// remove image with given (this) imageFileID
			// DataExchangeHelperForExperiments.removeSingleImageFromDataBase(user,
			// pass, imageResult);
			// clean up gui...
			Stack<DataSetFileButton> toBeDeleted = new Stack<DataSetFileButton>();
			JMyFilePanel p = (JMyFilePanel) this.getParent();
			String imageFileIDtoBeDeleted = imageResult.getMd5();
			for (int i = 0; i < p.getComponentCount(); i++) {
				Component o = p.getComponent(i);
				if (o instanceof DataSetFileButton) {
					DataSetFileButton checkButton = (DataSetFileButton) o;
					if (checkButton.imageResult.getMd5() == imageFileIDtoBeDeleted)
						toBeDeleted.add(checkButton);
				}
			}
			while (!toBeDeleted.empty())
				p.remove(toBeDeleted.pop());

			p.invalidate();
			p.validate();
			p.scrollpane.validate();
			p.repaint();
			targetTreeNode.setSizeDirty(true);
			targetTreeNode.updateSizeInfo(user, pass, sizeChangedListener);
		}
		if (evt.getSource() == removeAllFromDatabaseCmd) {
			// DataExchangeHelperForExperiments.removeAllImagesForOneTargetNodeFromDataBase(user,
			// pass, imageResult);
			JMyFilePanel p = (JMyFilePanel) this.getParent();
			p.removeAll();
			p.invalidate();
			p.validate();
			p.scrollpane.validate();
			p.repaint();
			targetTreeNode.setSizeDirty(true);
			targetTreeNode.updateSizeInfo(user, pass, sizeChangedListener);
			// if (user.equals(Consts.ROOTUSERNAME.toString())) { // TODO
			Thread t = new Thread(new Runnable() {
				public void run() {
					// try {
					// CallDBE2WebService.setDeleteUnusedBlobs(user, pass);
					// } catch (Exception e) {
					// ErrorMsg.addErrorMessage(e);
					// }
				}
			});
			t.start();
			// }
		}
	}

	void addDefaultCommands(final JPopupMenu myPopup) {
		showImageCmd = new JMenuItem("Show Image");
		showVolumeCmd = new JMenuItem("Show 3D-Volume");
		saveFileCmd = new JMenuItem("Save File As...");
		openFileCmd = new JMenuItem("View/Open with system default application");
		removeOneFromDatabaseCmd = new JMenuItem("! Delete this file !");
		removeAllFromDatabaseCmd = new JMenuItem("! Delete all files in view !");

		showImageCmd.addActionListener(this);
		showVolumeCmd.addActionListener(this);
		openFileCmd.addActionListener(this);
		saveFileCmd.addActionListener(this);
		removeOneFromDatabaseCmd.addActionListener(this);
		removeAllFromDatabaseCmd.addActionListener(this);

		if (imageResult != null && imageResult.downloadedFile != null) {
			if (getIsJavaImage() > 0 || imageResult.getFileName().contains(File.separator))
				myPopup.add(showImageCmd);
			if (imageResult.getBinaryFileInfo().getEntity() instanceof VolumeData)
				myPopup.add(showVolumeCmd);
			myPopup.add(openFileCmd);
			myPopup.add(saveFileCmd);
			if (!readOnly) {
				// myPopup.add(new JSeparator());
				// myPopup.add(removeOneFromDatabaseCmd);
				// myPopup.add(removeAllFromDatabaseCmd);
			}
		} else {
			final JMenuItem err = new JMenuItem("Upload in progress");
			err.setEnabled(false);
			myPopup.add(err);
		}
	}

	/**
	 * Sets the progress status to the value specified. Uses
	 * SwingUtilities.invokeLater (if needed).
	 * 
	 * @param i
	 *           Progress value (0..100)
	 */
	public void setProgressValue(final int i) {
		if (SwingUtilities.isEventDispatchThread()) {
			if (i < 0)
				progress.setIndeterminate(true);
			else
				progress.setValue(i);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (i < 0)
						progress.setIndeterminate(true);
					else
						progress.setValue(i);
				}
			});
		}
	}

	/**
	 * Hides the progressbar. Runs from a thread by using
	 * SwingUtilities.invokeLater (if needed).
	 */
	public void hideProgressbar() {
		if (SwingUtilities.isEventDispatchThread())
			progress.setVisible(false);
		else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progress.setVisible(false);
				}
			});
	}

	/**
	 * Shows the progressbar. Runs from a thread by using
	 * SwingUtilities.invokeLater (if needed).
	 */
	public void showProgressbar() {
		if (SwingUtilities.isEventDispatchThread()) {
			progress.setValue(0);
			progress.setVisible(true);
			progress.setValue(0);
			progress.repaint();
		} else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progress.setValue(0);
					progress.setVisible(true);
					progress.setValue(0);
					progress.repaint();
				}
			});
	}

	public void setDataBaseInfo(boolean downloadNeeded) {
		this.downloadNeeded = downloadNeeded;
		if (!downloadNeeded)
			imageResult.downloadedFile = new File(imageResult.getFileName());
		hideProgressbar();
	}

	/**
	 * Creates an temporary preview file that can be used for storage in the
	 * database.
	 * 
	 * @return PreviewImage-File
	 */
	public File createTempPreviewImage() {
		Image img = myImage.getImage();
		BufferedImage bi = new BufferedImage(img.getWidth(this), img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(img, 0, 0, this);
		File tf;
		try {
			tf = File.createTempFile("dbe_clipboard_", ".png");
		} catch (IOException e) {
			return null;
		}
		SupplementaryFilePanelMongoDB.addTempFileToBeDeletedLater(tf);
		try {
			ImageIO.write(bi, "PNG", tf);
		} catch (IOException e1) {
			return null;
		}
		return tf;
	}

	// /**
	// * @param imageID
	// */
	// public void setImageResultInfo(String imageMD5,
	// String targetTable,
	// String targetTablePrimaryKeyName,
	// Object targetTablePrimaryKeyValue) {
	// imageResult=DataExchangeHelperForXMLandSOAP.getImageInfo(
	// imageMD5,
	// targetTable,
	// targetTablePrimaryKeyName,
	// targetTablePrimaryKeyValue);
	// downloadNeeded=true;
	// }

	class IconAdapter extends JComponent {
		private static final long serialVersionUID = 1L;

		int width = DataSetFileButton.ICON_WIDTH;
		int height = DataSetFileButton.ICON_HEIGHT;

		public IconAdapter(Icon icon) {
			this.icon = icon;
			height = icon.getIconHeight();
			width = icon.getIconWidth();
		}

		@Override
		public void paintComponent(Graphics g) {
			icon.paintIcon(this, g, (getWidth() - width) / 2, (getHeight() - height) / 2);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(DataSetFileButton.ICON_WIDTH, DataSetFileButton.ICON_HEIGHT); // icon.getIconWidth(),
			// icon.getIconHeight());
		}

		private final Icon icon;
	}

	public void setIsPrimaryDatabaseEntity() {
		this.readOnly = true;
		if (mmlbl != null) {
			mmlbl.setBorder(BorderFactory.createEtchedBorder());
			mmlbl.setOpaque(true);
			mmlbl.setText(mmlbl.getText());
		}
	}

	public void setDownloadNeeded(boolean b) {
		this.downloadNeeded = true;
		myImage = null;
	}
}
