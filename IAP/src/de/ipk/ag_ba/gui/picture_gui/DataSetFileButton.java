package de.ipk.ag_ba.gui.picture_gui;

import iap.pipelines.ImageProcessorOptionsAndResults;
// import ij.io.FileInfoXYZ;
import ij.io.Opener;
// import ij.io.TiffDecoderExtended;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.HomeFolder;
import org.IniIoProvider;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.ActionSettings;
import de.ipk.ag_ba.gui.IAPnavigationPanel;
import de.ipk.ag_ba.gui.PanelTarget;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.ImageAnalysisTasks;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.UserDefinedImageAnalysisPipelineTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author Christian Klukas
 */
public class DataSetFileButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	protected static int ICON_HEIGHT = 128;
	protected static int ICON_WIDTH = 128;
	private JMenuItem showImageCmdMain;
	private JMenuItem showImageCmdLabel;
	private JMenuItem showVolumeCmd;
	private JMenuItem openFileCmdMain;
	private JMenuItem openFileCmdLabel;
	private JMenuItem saveFileCmdMain;
	private JMenuItem saveFileCmdLabel;
	private JMenuItem removeOneFromDatabaseCmd;
	private JMenuItem removeAllFromDatabaseCmd;
	
	JProgressBar progress;
	private final MongoTreeNode targetTreeNode;
	public MyImageIcon myImage;
	
	ImageResult imageResult = null;
	
	private boolean readOnly;
	
	volatile boolean downloadInProgress = false;
	boolean downloadNeeded = false;
	private ActionListener sizeChangedListener;
	private final Collection<DataSetFileButton> buttonsInThisView;
	
	public int getIsJavaImage() {
		if (myImage == null)
			return 0;
		return myImage.imageAvailable;
	}
	
	public DataSetFileButton(
			final MongoTreeNode targetTreeNode, String label, MyImageIcon icon,
			ImageIcon previewImage, boolean isNoImageButton, Collection<DataSetFileButton> buttonsInThisView) {
		super();
		this.buttonsInThisView = buttonsInThisView;
		
		progress = new JProgressBar(0, 100);
		progress.setValue(-1);
		
		progress.setVisible(false);
		
		updateLayout(label, icon, previewImage, isNoImageButton);
		
		addActionListener(this);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		this.targetTreeNode = targetTreeNode;
		
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3 || !primary) {
					ActionEvent ae = new ActionEvent(e.getSource(), 0, null);
					processMouseClick(ae);
				}
			}
		});
	}
	
	JLabel mmlbl;
	private boolean primary;
	private String additionalFileNameInfo;
	private boolean attachment;
	private ActionListener additionalActionListener;
	private AnnotationInfoPanel aip;
	
	public void updateLayout(String label, MyImageIcon icon,
			ImageIcon previewImage, boolean isNoImageButton) {
		double border = 2;
		removeAll();
		JLabel ilbl;
		if (icon != null)
			ilbl = new JLabel(icon);
		else
			ilbl = new JLabel(previewImage);
		
		double[][] size = {
				{ border, DataSetFileButton.ICON_WIDTH, border }, // Columns
				{ border, DataSetFileButton.ICON_HEIGHT, border,
						TableLayout.PREFERRED, border, TableLayout.PREFERRED,
						border } }; // Rows
		
		if (isNoImageButton)
			size = new double[][] {
					{ border, 128, border }, // Columns
					{ 0, 48, 0,
							0, 0, TableLayout.PREFERRED,
							border } };
		
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
	
	public DataSetFileButton(MongoTreeNode projectNode,
			ImageResult imageResult, ImageIcon previewImage, boolean readOnly, boolean isNoImageButton, String customNonImageTitle,
			Collection<DataSetFileButton> buttonsInThisView) {
		this(projectNode,
				"<html><body><b>" +
						(imageResult == null ? "<center>" + customNonImageTitle :
								getMaxString(strip(
										imageResult.getFileNameMain(),
										((!(imageResult.getBinaryFileInfo().entity instanceof NumericMeasurement3D)) ?
												"(Attachment)" :
												((NumericMeasurement3D) imageResult
														.getBinaryFileInfo().entity)
														.getQualityAnnotation()
														+ "<br>("
														+ (((NumericMeasurement3D) imageResult
																.getBinaryFileInfo().entity)
																.getPosition() != null ? ((NumericMeasurement3D) imageResult
																.getBinaryFileInfo().entity)
																.getPosition().intValue()
																+ ")" : "0)")))))
						+ "</b></body></html>", null, previewImage, isNoImageButton, buttonsInThisView);
		this.imageResult = imageResult;
		this.readOnly = readOnly;
		if (imageResult != null && getMaxString(imageResult.getFileNameMain()).endsWith("..."))
			setToolTipText(imageResult.getFileNameMain());
		else
			if (imageResult == null) {
				if (projectNode.getTargetEntity() instanceof Substance3D)
					setToolTipText(((Substance3D) projectNode.getTargetEntity()).getName());
				else
					if (projectNode.getTargetEntity() instanceof Condition3D)
						setToolTipText(((Condition3D) projectNode.getTargetEntity()).getName());
			}
	}
	
	private static String strip(String fileName, String opt) {
		if (fileName.equals("null"))
			fileName = opt;
		if (fileName.contains(File.separator))
			return fileName.substring(fileName.lastIndexOf(File.separator)
					+ File.separator.length());
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
		return new Dimension((int) d.getWidth(), (int) d.getHeight());
	}
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (primary) {
			JPopupMenu jp = new JPopupMenu("Debug");
			
			JMenuItem debugPipelineTestShowMainImage = new JMenuItem(
					"Show Image");
			
			debugPipelineTestShowMainImage.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Zoom-Fit-Best-64.png").getScaledInstance(16, 16,
					java.awt.Image.SCALE_SMOOTH)));
			
			debugPipelineTestShowMainImage
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								IOurl s = imageResult
										.getBinaryFileInfo()
										.getFileNameMain();
								Image fi = new Image(s);
								fi.show("Main Image");
							} catch (Exception err) {
								JOptionPane
										.showMessageDialog(
												null,
												"Error: "
														+ err.getLocalizedMessage()
														+ ". Command execution error.",
												"Error",
												JOptionPane.INFORMATION_MESSAGE);
								return;
							}
						}
					});
			
			JMenuItem debugPipelineTestShowReferenceImage = new JMenuItem(
					"Show Reference");
			debugPipelineTestShowReferenceImage
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								IOurl s = imageResult
										.getBinaryFileInfo()
										.getFileNameLabel();
								Image fi = new Image(s);
								fi.show("Reference Image");
							} catch (Exception err) {
								MainFrame.getInstance().showMessageDialog("Reference image could not be loaded: " + err.getMessage());
								return;
							}
						}
					});
			
			JMenuItem debugPipelineTestShowImage = new JMenuItem(
					"Show Annotation");
			debugPipelineTestShowImage
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								IOurl s = imageResult
										.getBinaryFileInfo()
										.getFileNameMain();
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchFor(s, targetTreeNode
												.getExperiment().getExperiment(), false);
								
								for (NumericMeasurementInterface nmi : match) {
									ImageData id = (ImageData) nmi;
									if (id.getURL().getDetail()
											.equals(s.getDetail())) {
										String oldRef = id
												.getAnnotationField("oldreference");
										
										if (oldRef != null) {
											IOurl u = new IOurl(oldRef);
											Image fi = new Image(
													u);
											fi.show("Annotation Image");
										} else
											MainFrame.getInstance().showMessageDialog("Annotation image is undefined");
									}
								}
							} catch (Exception err) {
								JOptionPane
										.showMessageDialog(
												null,
												"Error: "
														+ err.getLocalizedMessage()
														+ ". Command execution error.",
												"Error",
												JOptionPane.INFORMATION_MESSAGE);
								ErrorMsg.addErrorMessage(err);
								return;
							}
						}
					});
			
			JMenuItem debugShowSnapshot = new JMenuItem(
					"Main, Reference, Annotation (Stack)");
			
			debugShowSnapshot.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Collection<NumericMeasurementInterface> match = IAPservice
								.getMatchFor(imageResult
										.getBinaryFileInfo()
										.getFileNameMain(),
										targetTreeNode.getExperiment().getExperiment(), false);
						if (match.size() > 0) {
							ImageStack snapshot = new ImageStack();
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getURL() != null) {
										Image fi = new Image(
												id.getURL());
										snapshot.addImage(
												id.getSubstanceName(),
												fi);
									}
								}
							}
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getLabelURL() != null) {
										Image fi = new Image(
												id.getLabelURL());
										snapshot.addImage(
												"Reference "
														+ id.getSubstanceName(),
												fi);
									}
								}
							}
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getAnnotationField("oldreference") != null) {
										Image fi = new Image(
												new IOurl(
														id.getAnnotationField("oldreference")));
										snapshot.addImage(
												"Annotation "
														+ id.getSubstanceName(),
												fi);
									}
								}
							}
							
							NumericMeasurementInterface a = match
									.iterator().next();
							snapshot.show("Snapshot "
									+ a.getQualityAnnotation()
									+ " "
									+ a.getParentSample()
											.getSampleTime()
									+ " "
									+ a.getParentSample()
											.getParentCondition()
											.getConditionName());
						}
					} catch (Exception err) {
						JOptionPane.showMessageDialog(null, "Error: "
								+ err.getLocalizedMessage()
								+ ". Command execution error.",
								"Error",
								JOptionPane.INFORMATION_MESSAGE);
						ErrorMsg.addErrorMessage(err);
						return;
					}
				}
			});
			
			jp.add(debugPipelineTestShowMainImage);
			if (imageResult.getBinaryFileInfo().getFileNameLabel() != null)
				jp.add(debugPipelineTestShowReferenceImage);
			jp.add(debugPipelineTestShowImage);
			
			JMenuItem stl = new JMenuItem("Show Image Timeline");
			stl.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Appointment-Soon-64.png").getScaledInstance(16, 16,
					java.awt.Image.SCALE_SMOOTH)));
			stl.addActionListener(getListener(targetTreeNode, true,
					false, false, true));
			jp.add(stl);
			
			JMenu sn = new JMenu("Show Complete Snapshot Set");
			sn.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Emblem-Photos-64.png").getScaledInstance(16, 16,
					java.awt.Image.SCALE_SMOOTH)));
			
			JMenuItem a = new JMenuItem("Main");
			a.addActionListener(getListener(targetTreeNode, true,
					false, false, false));
			sn.add(a);
			JMenuItem b = new JMenuItem("Reference");
			b.addActionListener(getListener(targetTreeNode, false,
					true, false, false));
			sn.add(b);
			JMenuItem c = new JMenuItem("Annotation");
			c.addActionListener(getListener(targetTreeNode, false,
					false, true, false));
			sn.add(c);
			
			JMenuItem debugShowSnapshotNoStack = new JMenuItem(
					"Main, Reference, Annotation");
			debugShowSnapshotNoStack.addActionListener(getListener(
					targetTreeNode, true, true, true, false));
			sn.add(debugShowSnapshotNoStack);
			sn.add(debugShowSnapshot);
			jp.add(sn);
			
			if (targetTreeNode.getExperiment().getIniIoProvider() != null) {
				try {
					final IniIoProvider iop = targetTreeNode.isReadOnly() ? null : targetTreeNode.getExperiment().getIniIoProvider();
					if (iop != null
							&& targetTreeNode != null
							&& targetTreeNode.getExperiment() != null
							&& targetTreeNode.getExperiment().getHeader() != null
							&& targetTreeNode.getExperiment().getHeader().getSettings() != null
							&& !targetTreeNode.getExperiment().getHeader().getSettings().isEmpty()) {
						Action action = new AbstractAction("Change Analysis Settings") {
							@Override
							public void actionPerformed(ActionEvent e) {
								IAPnavigationPanel mnp = new IAPnavigationPanel(PanelTarget.NAVIGATION, null, null);
								NavigationAction ac = new ActionSettings(null, iop, "Change analysis settings", "Modify settings");
								mnp.getNewWindowListener(ac).actionPerformed(null);
							}
						};
						jp.add(new JSeparator());
						JMenuItem mi = new JMenuItem(action);
						mi.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Science-64.png").getScaledInstance(16, 16,
								java.awt.Image.SCALE_SMOOTH)));
						jp.add(mi);
						PipelineDesc pd = new PipelineDesc(null, iop, null, null, null);
						UserDefinedImageAnalysisPipelineTask iat =
								new UserDefinedImageAnalysisPipelineTask(pd);
						if (!targetTreeNode.getExperiment().getHeader().getExperimentType().equalsIgnoreCase("Analysis Results")) {
							JMenuItem debugPipelineTest0a = getMenuItemAnalyseFromMainImage(targetTreeNode, iat);
							debugPipelineTest0a.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Engineering-64.png")
									.getScaledInstance(16, 16,
											java.awt.Image.SCALE_SMOOTH)));
							jp.add(debugPipelineTest0a);
						} else {
							JMenuItem debugPipelineTest00a = getMenuItemAnalyseFromLabelImage(targetTreeNode, iat);
							debugPipelineTest00a.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Engineering-64.png")
									.getScaledInstance(16, 16,
											java.awt.Image.SCALE_SMOOTH)));
							jp.add(debugPipelineTest00a);
						}
						jp.add(new JSeparator());
						Action action2 = new AbstractAction("Close All Image Windows") {
							@Override
							public void actionPerformed(ActionEvent e) {
								IAPservice.closeAllImageJimageWindows();
							}
							
							@Override
							public boolean isEnabled() {
								return IAPservice.getIAPimageWindowCount() > 0;
							}
							
						};
						JMenuItem jmc = new JMenuItem(action2);
						jmc.setIcon(new ImageIcon(IAPimages.getImage("img/close_frame.png")));
						jp.add(jmc);
						
						jp.add(new JSeparator());
					}
				} catch (Exception err) {
					if (err.getCause() != null && err.getCause() instanceof NullPointerException)
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">INFO: No analysis pipeline assigned. Debug menu items are not added to menu list.");
					else
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: Could not analyze assigned pipeline info. Debug menu items are not added to menu list. Error: " + err.getMessage());
				}
			}
			
			if (!targetTreeNode.isReadOnly()) {
				JMenu ta = new JMenu("Analysis Templates");
				ta.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/book_object2.png")
						.getScaledInstance(16, 16,
								java.awt.Image.SCALE_SMOOTH)));
				
				ArrayList<AbstractPhenotypingTask> pl = new ArrayList<AbstractPhenotypingTask>();
				try {
					pl = new ImageAnalysisTasks().getKnownImageAnalysisTasks();
				} catch (Exception e1) {
					e1.printStackTrace();
					ErrorMsg.addErrorMessage(e1);
				}
				
				boolean added = false;
				
				for (final AbstractPhenotypingTask iat : pl) {
					JMenuItem debugPipelineTest0a = getMenuItemAnalyseFromMainImage(targetTreeNode, iat);
					JMenuItem debugPipelineTest00a = getMenuItemAnalyseFromLabelImage(targetTreeNode, iat);
					
					ta.add(debugPipelineTest0a);
					ta.add(debugPipelineTest00a);
					added = true;
				}
				
				if (added)
					jp.add(ta);
				jp.addSeparator();
				
				JMenu fm = getAnnotationChangerSubmenu();
				
				jp.add(fm);
			}
			jp.show(this, getX() + 5, getY() + 5);
		}
	}
	
	private void processMouseClick(final ActionEvent evt) {
		if (imageResult == null && getAdditionalActionListener() != null) {
			getAdditionalActionListener().actionPerformed(evt);
			return;
		}
		if (!downloadNeeded && evt.getSource() instanceof JButton) {
			
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			JPopupMenu myPopup2 = new JPopupMenu();
			addDefaultCommands(myPopup2);
			myPopup2.validate();
			myPopup2.show(DataSetFileButton.this, 5, 5);
			// }
			// });
		}
		
		if (downloadNeeded || evt.getSource() == openFileCmdMain
				|| evt.getSource() == openFileCmdLabel
				|| evt.getSource() == saveFileCmdMain
				|| evt.getSource() == saveFileCmdLabel) {
			final JPopupMenu myPopup = new JPopupMenu();
			
			if (downloadNeeded) {
				JMenuItem tempItem = new JMenuItem("Download file...");
				
				tempItem.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Emblem-Downloads-64.png").getScaledInstance(16, 16,
						java.awt.Image.SCALE_SMOOTH)));
				
				tempItem.setEnabled(false);
				myPopup.add(tempItem);
				myPopup.show(this, 5, 5);
				
				downloadNeeded = false;
				setProgressValue(-1);
				showProgressbar();
				LocalComputeJob download;
				try {
					download = new LocalComputeJob(new Runnable() {
						@Override
						public void run() {
							downloadInProgress = true;
							final File tfMain;
							try {
								tfMain = File.createTempFile("dbe_", "."
										+ imageResult.getFileNameMain());
								SupplementaryFilePanelMongoDB.addTempFileToBeDeletedLater(tfMain);
							} catch (IOException e2) {
								SupplementaryFilePanelMongoDB
										.showError(
												"Could not create temp file for storing database image!",
												e2);
								downloadNeeded = true;
								return;
							}
							final File tfLabel;
							try {
								tfLabel = File.createTempFile("dbe_", "."
										+ imageResult.getFileNameLabel());
								SupplementaryFilePanelMongoDB
										.addTempFileToBeDeletedLater(tfLabel);
							} catch (IOException e2) {
								SupplementaryFilePanelMongoDB
										.showError(
												"Could not create temp file for storing database image!",
												e2);
								downloadNeeded = true;
								return;
							}
							
							try {
								IOurl urlMain = imageResult.getBinaryFileInfo()
										.getFileNameMain();
								System.out.println("Main URL: " + urlMain);
								InputStream isMain = urlMain != null ? urlMain
										.getInputStream() : null;
								if (isMain == null)
									System.out.println("Main URL: Inputstream = null");
								HomeFolder.copyFile(isMain, tfMain);
								IOurl urlLabel = imageResult
										.getBinaryFileInfo().getFileNameLabel();
								if (urlLabel != null) {
									try {
										System.out.println("Label URL: " + urlLabel);
										InputStream isLabel = urlLabel != null ? urlLabel
												.getInputStream() : null;
										if (isLabel == null)
											System.out.println("Inputstream = null");
										HomeFolder.copyFile(isLabel, tfLabel);
									} catch (Exception e) {
										if (urlLabel != null)
											e.printStackTrace();
										System.out.println("ERROR: could not process label URL: " + e.getMessage());
									}
								}
							} catch (Exception e1) {
								System.out.println("No valid input stream for "
										+ imageResult.getBinaryFileInfo()
												.getFileNameMain().toString()
										+ " and/or label.");
								
								MappingDataEntity mde = targetTreeNode
										.getTargetEntity();
								try {
									VolumeData volume = (VolumeData) mde;
									if (volume != null)
										DataExchangeHelperForExperiments
												.downloadFile(targetTreeNode.getExperiment().m, imageResult.getHashMain(), tfMain,
														DataSetFileButton.this,
														MongoCollection.VOLUMES);
								} catch (Exception e) {
									DataExchangeHelperForExperiments
											.downloadFile(targetTreeNode.getExperiment().m,
													imageResult.getHashMain(),
													tfMain,
													DataSetFileButton.this,
													MongoCollection.IMAGES);
									DataExchangeHelperForExperiments
											.downloadFile(targetTreeNode.getExperiment().m, imageResult
													.getFileNameLabel(),
													tfLabel,
													DataSetFileButton.this,
													MongoCollection.IMAGES);
								}
							}
							
							if (tfMain != null || tfLabel != null) {
								imageResult.downloadedFileMain = tfMain;
								imageResult.downloadedFileLabel = tfLabel;
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										try {
											myImage = new MyImageIcon(
													MainFrame.getInstance(),
													DataSetFileButton.ICON_WIDTH,
													DataSetFileButton.ICON_HEIGHT,
													FileSystemHandler
															.getURL(tfMain),
													FileSystemHandler
															.getURL(tfLabel),
													myImage != null ? myImage
															.getBinaryFileInfo()
															: null);
										} catch (MalformedURLException e) {
											downloadNeeded = true;
											SupplementaryFilePanelMongoDB
													.showError(
															"URL Format Error",
															e);
										}
									}
								});
							}
							
							downloadInProgress = false;
							hideProgressbar();
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									if (myPopup.isVisible()) {
										myPopup.setVisible(false);
										JPopupMenu myPopup2 = new JPopupMenu();
										addDefaultCommands(myPopup2);
										myPopup2.validate();
										myPopup2.show(DataSetFileButton.this,
												5, 5);
									}
								}
							});
						}
					}, "database download");
					BackgroundThreadDispatcher.addTask(download);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else
				if (downloadInProgress) {
					JMenuItem tempItem = new JMenuItem("Download in progress...");
					tempItem.setEnabled(false);
					myPopup.add(tempItem);
					myPopup.show(this, 5, 5);
				} else {
					if (evt.getSource() != saveFileCmdMain
							&& evt.getSource() != saveFileCmdLabel
							&& evt.getSource() != openFileCmdMain
							&& evt.getSource() != openFileCmdLabel) {
						addDefaultCommands(myPopup);
						myPopup.show(this, 5, 5);
					}
				}
			
		}
		if (evt.getSource() == saveFileCmdMain) {
			// imageResult.fileName
			// myImage.fileURL
			SupplementaryFilePanelMongoDB.fileChooser.setSelectedFile(new File(
					imageResult.getFileNameMain()));
			if (SupplementaryFilePanelMongoDB.fileChooser
					.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
				File outputFile = SupplementaryFilePanelMongoDB.fileChooser
						.getSelectedFile();
				
				FileInputStream in;
				try {
					in = new FileInputStream(imageResult.downloadedFileMain);
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
					SupplementaryFilePanelMongoDB.showError("File not found.",
							e);
				} catch (IOException e) {
					SupplementaryFilePanelMongoDB.showError("IO Exception", e);
				}
			}
		}
		if (evt.getSource() == saveFileCmdLabel) {
			// imageResult.fileName
			// myImage.fileURL
			SupplementaryFilePanelMongoDB.fileChooser.setSelectedFile(new File(
					imageResult.getFileNameLabel()));
			if (SupplementaryFilePanelMongoDB.fileChooser
					.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
				File outputFile = SupplementaryFilePanelMongoDB.fileChooser
						.getSelectedFile();
				
				FileInputStream in;
				try {
					in = new FileInputStream(imageResult.downloadedFileLabel);
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
					SupplementaryFilePanelMongoDB.showError("File not found.",
							e);
				} catch (IOException e) {
					SupplementaryFilePanelMongoDB.showError("IO Exception", e);
				}
			}
		}
		if (evt.getSource() == showVolumeCmd) {
			// MappingResultGraph mrg = new MappingResultGraph();
			// VolumeData volume = (VolumeData)
			// imageResult.getBinaryFileInfo().getEntity();
			// MappingResultGraphNode mrgn = mrg.createVolumeNode(volume, null);
			// mrgn.getParams().setURL(FileSystemHandler.getURL(imageResult.getDownloadedFile()));
			//
			// EditorSession session =
			// MainFrame.getInstance().createNewSession(mrg.getGraph());
			//
			// MainFrame.getInstance().createInternalFrame(ThreeDview.class.getCanonicalName(),
			// session, false);
			
			IAPmain.showVANTED(false);
		}
		if (evt.getSource() == showImageCmdMain) {
			try {
				if (myImage.fileURLmain == null)
					JOptionPane.showMessageDialog(null,
							"Error: Main URL is undefined. Image can not be shown.",
							"Unknown Image Format",
							JOptionPane.INFORMATION_MESSAGE);
				else {
					Image fi = new Image(myImage.fileURLmain);
					fi.show("Image View - " + myImage.fileURLmain.getFileNameDecoded());
				}
			} catch (Exception e) {
				JOptionPane
						.showMessageDialog(null,
								"Error: " + e.getLocalizedMessage()
										+ ". Image can not be shown.",
								"Unknown Image Format",
								JOptionPane.INFORMATION_MESSAGE);
				ErrorMsg.addErrorMessage(e);
				return;
			}
		}
		if (evt.getSource() == showImageCmdLabel) {
			try {
				Image fi = null;
				try {
					fi = new Image(myImage.fileURLlabel);
				} catch (Exception err) {
					// try to load as TIFF..
				}
				if (fi == null || fi.getWidth() == 0) {
					try {
						// TiffDecoderExtended tid = new TiffDecoderExtended(
						// myImage.fileURLlabel.getInputStream(),
						// myImage.fileURLlabel.getFileName());
						// FileInfoXYZ[] info = tid.getTiffInfo();
						Opener o = new Opener();
						// ImagePlus imp = o.openTiffStack(info);
						// imp.show("Image Label View - "
						// + myImage.fileURLlabel.getFileNameDecoded());
						IAPservice.showImageJ();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,
								"Error: " + e.getLocalizedMessage()
										+ ". Image can not be shown.",
								"Unknown Image Format",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else
					if (fi != null)
						fi.show("Image Label View - "
								+ myImage.fileURLlabel.getFileNameDecoded());
			} catch (Exception e) {
				JOptionPane
						.showMessageDialog(null,
								"Error: " + e.getLocalizedMessage()
										+ ". Image can not be shown.",
								"Unknown Image Format",
								JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		if (evt.getSource() == openFileCmdMain) {
			if (imageResult != null && imageResult.getFileNameMain().contains(File.separator))
				AttributeHelper.showInBrowser(imageResult.getFileNameMain());
			else
				AttributeHelper.showInBrowser(myImage.fileURLmain.toString());
			
		}
		if (evt.getSource() == openFileCmdLabel) {
			if (imageResult.getFileNameLabel().contains(File.separator))
				AttributeHelper.showInBrowser(imageResult.getFileNameLabel());
			else
				AttributeHelper.showInBrowser(myImage.fileURLlabel.toString());
			
		}
		if (evt.getSource() == removeOneFromDatabaseCmd) {
			// remove image with given (this) imageFileID
			// DataExchangeHelperForExperiments.removeSingleImageFromDataBase(user,
			// pass, imageResult);
			// clean up gui...
			Stack<DataSetFileButton> toBeDeleted = new Stack<DataSetFileButton>();
			DataSetFilePanel p = (DataSetFilePanel) this.getParent();
			String imageFileIDtoBeDeleted = imageResult.getHashMain();
			for (int i = 0; i < p.getComponentCount(); i++) {
				Component o = p.getComponent(i);
				if (o instanceof DataSetFileButton) {
					DataSetFileButton checkButton = (DataSetFileButton) o;
					if (checkButton.imageResult.getHashMain() == imageFileIDtoBeDeleted)
						toBeDeleted.add(checkButton);
				}
			}
			while (!toBeDeleted.empty())
				p.remove(toBeDeleted.pop());
			
			p.invalidate();
			p.validate();
			p.getScrollpane().validate();
			p.repaint();
			targetTreeNode.setSizeDirty(true);
			try {
				targetTreeNode.updateSizeInfo(sizeChangedListener);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (evt.getSource() == removeAllFromDatabaseCmd) {
			// DataExchangeHelperForExperiments.removeAllImagesForOneTargetNodeFromDataBase(user,
			// pass, imageResult);
			DataSetFilePanel p = (DataSetFilePanel) this.getParent();
			p.removeAll();
			p.invalidate();
			p.validate();
			p.getScrollpane().validate();
			p.repaint();
			targetTreeNode.setSizeDirty(true);
			try {
				targetTreeNode.updateSizeInfo(sizeChangedListener);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if (user.equals(Consts.ROOTUSERNAME.toString())) {
			Thread t = new Thread(new Runnable() {
				@Override
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
		showImageCmdMain = new JMenuItem("Show Image");
		showImageCmdMain.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Zoom-Fit-Best-64.png").getScaledInstance(16, 16,
				java.awt.Image.SCALE_SMOOTH)));
		
		showImageCmdLabel = new JMenuItem("Show Reference Image");
		showVolumeCmd = new JMenuItem("Show 3D-Volume");
		saveFileCmdMain = new JMenuItem("Save File As...");
		saveFileCmdMain.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Document-Save-64.png").getScaledInstance(16, 16,
				java.awt.Image.SCALE_SMOOTH)));
		
		saveFileCmdLabel = new JMenuItem("Save Reference File As...");
		openFileCmdMain = new JMenuItem(
				"View/Open with system default application");
		openFileCmdMain.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-System-Run-64.png").getScaledInstance(16, 16,
				java.awt.Image.SCALE_SMOOTH)));
		
		openFileCmdLabel = new JMenuItem(
				"View/Open Reference file with system default application");
		removeOneFromDatabaseCmd = new JMenuItem("! Delete this item !");
		removeAllFromDatabaseCmd = new JMenuItem("! Delete all items in view !");
		
		showImageCmdMain.addActionListener(this);
		showImageCmdLabel.addActionListener(this);
		showVolumeCmd.addActionListener(this);
		openFileCmdMain.addActionListener(this);
		saveFileCmdMain.addActionListener(this);
		
		openFileCmdLabel.addActionListener(this);
		saveFileCmdLabel.addActionListener(this);
		removeOneFromDatabaseCmd.addActionListener(this);
		removeAllFromDatabaseCmd.addActionListener(this);
		
		// if (imageResult != null && imageResult.downloadedFileMain != null) {
		if (getIsJavaImage() > 0
				|| (imageResult != null &&
						imageResult.getFileNameMain() != null &&
				imageResult.getFileNameMain().contains(File.separator)))
			myPopup.add(showImageCmdMain);
		if (imageResult != null && imageResult.getBinaryFileInfo().getEntity() instanceof VolumeData)
			myPopup.add(showVolumeCmd);
		myPopup.add(openFileCmdMain);
		myPopup.add(saveFileCmdMain);
		if ((getIsJavaImage() > 0 && imageResult != null && imageResult.getFileNameLabel() != null)
				|| (imageResult != null && imageResult.getFileNameLabel() != null && imageResult
						.getFileNameLabel().contains(File.separator)))
			myPopup.add(showImageCmdLabel);
		if (imageResult != null && imageResult.getBinaryFileInfo().getHashLabel() != null)
			myPopup.add(openFileCmdLabel);
		if (imageResult != null && imageResult.getBinaryFileInfo().getHashLabel() != null)
			myPopup.add(saveFileCmdLabel);
		if (!readOnly) {
			// myPopup.add(new JSeparator());
			// myPopup.add(removeOneFromDatabaseCmd);
			// myPopup.add(removeAllFromDatabaseCmd);
		}
		
		// myPopup.add(debugPipelineTest);
		
		// }
		// else {
		// final JMenuItem err = new JMenuItem("Upload in progress");
		// err.setEnabled(false);
		// myPopup.add(err);
		// }
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
				@Override
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
				@Override
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
				@Override
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
		if (!downloadNeeded) {
			imageResult.downloadedFileMain = new File(
					imageResult.getFileNameMain());
			imageResult.downloadedFileLabel = new File(
					imageResult.getFileNameLabel());
		}
		hideProgressbar();
	}
	
	/**
	 * Creates an temporary preview file that can be used for storage in the
	 * database.
	 * 
	 * @return PreviewImage-File
	 */
	public File createTempPreviewImage() {
		java.awt.Image img = myImage.getImage();
		BufferedImage bi = new BufferedImage(img.getWidth(this),
				img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(img, 0, 0, this);
		File tf;
		try {
			tf = File.createTempFile("dbe_clipboard_", "." + SystemOptions.getInstance().getString("IAP", "Preview File Type", "png"));
		} catch (IOException e) {
			return null;
		}
		SupplementaryFilePanelMongoDB.addTempFileToBeDeletedLater(tf);
		try {
			ImageIO.write(bi, SystemOptions.getInstance().getString("IAP", "Preview File Type", "png"), tf);
		} catch (IOException e1) {
			return null;
		}
		return tf;
	}
	
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
			icon.paintIcon(this, g, (getWidth() - width) / 2,
					(getHeight() - height) / 2);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(DataSetFileButton.ICON_WIDTH,
					DataSetFileButton.ICON_HEIGHT); // icon.getIconWidth(),
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
		this.primary = true;
		validate();
	}
	
	public void setIsAttachment() {
		this.readOnly = false;
		if (mmlbl != null) {
			mmlbl.setBorder(BorderFactory.createEtchedBorder());
			mmlbl.setText(mmlbl.getText());
			mmlbl.setOpaque(true);
			mmlbl.setBackground(new Color(220, 220, 250));
		}
		this.attachment = true;
		validate();
	}
	
	public void setDownloadNeeded(boolean b) {
		this.downloadNeeded = true;
		myImage = null;
	}
	
	private ActionListener getListener(final MongoTreeNode targetTreeNode,
			final boolean main, final boolean ref, final boolean anno, final boolean showTimeLine) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<NumericMeasurementInterface> match = IAPservice
							.getMatchFor(imageResult.getBinaryFileInfo()
									.getFileNameMain(), targetTreeNode
									.getExperiment().getExperiment(), showTimeLine);
					if (match.size() > 0) {
						ArrayList<ImageData> toBeLoaded = new ArrayList<ImageData>();
						NumericMeasurementInterface a = match.iterator().next();
						String pre = "snapshot "
								+ a.getQualityAnnotation()
								+ " "
								+ a.getParentSample().getSampleTime()
								+ " "
								+ a.getParentSample().getParentCondition()
										.getConditionName();
						String desiredCamera = ((ImageData) imageResult.getBinaryFileInfo().getEntity()).getParentSample().getParentCondition().getParentSubstance()
								.getName();
						if (main) {
							ImageStack is = new ImageStack();
							for (NumericMeasurementInterface nmi : match) {
								if (showTimeLine && !desiredCamera.equals(nmi.getParentSample().getParentCondition().getParentSubstance().getName()))
									continue;
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getURL() != null) {
										if (showTimeLine)
											toBeLoaded.add(id);
										else {
											Image fi = new Image(
													id.getURL());
											fi.show(id.getSubstanceName() + " // " + pre);
										}
									}
								}
							}
						}
						if (ref) {
							ImageStack is = new ImageStack();
							for (NumericMeasurementInterface nmi : match) {
								if (showTimeLine && !desiredCamera.equals(nmi.getParentSample().getParentCondition().getParentSubstance().getName()))
									continue;
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getLabelURL() != null) {
										Image fi = new Image(
												id.getLabelURL());
										if (showTimeLine)
											is.addImage(id.getQualityAnnotation() + " / " + id.getSubstanceName() + " / " + id.getParentSample().getTimeUnit() + " "
													+ id.getParentSample().getTime(), fi);
										else
											fi.show("Reference " + id.getSubstanceName() + " // " + pre);
									}
								}
							}
							if (showTimeLine)
								is.show("Reference " + pre);
						}
						if (anno) {
							ImageStack is = new ImageStack();
							for (NumericMeasurementInterface nmi : match) {
								if (showTimeLine && !desiredCamera.equals(nmi.getParentSample().getParentCondition().getParentSubstance().getName()))
									continue;
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getAnnotationField("oldreference") != null) {
										Image fi = new Image(
												new IOurl(
														id.getAnnotationField("oldreference")));
										if (showTimeLine)
											is.addImage(id.getQualityAnnotation() + " / " + id.getSubstanceName() + " / " + id.getParentSample().getTimeUnit() + " "
													+ id.getParentSample().getTime(), fi);
										else
											fi.show("Annotation " + id.getSubstanceName() + " // " + pre);
									}
								}
							}
							if (showTimeLine)
								is.show("Annotation " + pre);
						}
						if (toBeLoaded.size() > 0) {
							IAPservice.showImages(toBeLoaded);
						}
					}
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null,
							"Error: " + err.getLocalizedMessage()
									+ ". Command execution error.", "Error",
							JOptionPane.INFORMATION_MESSAGE);
					ErrorMsg.addErrorMessage(err);
					return;
				}
			}
		};
	}
	
	@Override
	public String getText() {
		if (additionalFileNameInfo == null)
			return super.getText();
		else
			return super.getText() + " (" + additionalFileNameInfo + ")";
	}
	
	public void setAdditionalFileNameInfo(String additionalFileNameInfo) {
		this.additionalFileNameInfo = additionalFileNameInfo;
	}
	
	private JMenuItem getMenuItemAnalyseFromMainImage(final MongoTreeNode targetTreeNode, final AbstractPhenotypingTask iat) {
		JMenuItem debugPipelineTest0a = new JMenuItem(
				StringManipulationTools.removeHTMLtags(iat.getName()) + " (Image+Reference)");
		debugPipelineTest0a.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<NumericMeasurementInterface> match = IAPservice
							.getMatchFor(imageResult
									.getBinaryFileInfo()
									.getFileNameMain(),
									targetTreeNode.getExperiment().getExperiment(), false);
					System.out.println("BLOCKS: " + iat.getImageProcessor().getPipeline(
							new ImageProcessorOptionsAndResults(iat.getSystemOptions(), null, null)).getSize() + ", Image Set Hits: " + match.size());
					for (NumericMeasurementInterface nmi : match)
						System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Image Set Match: " + nmi + " // Subtance: "
								+ nmi.getParentSample().getParentCondition().getParentSubstance().getName());
					BlockPipeline.debugTryAnalysis(
							targetTreeNode.getExperiment(),
							match,
							iat);
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, "Error: "
							+ err.getLocalizedMessage()
							+ ". Command execution error.",
							"Error",
							JOptionPane.INFORMATION_MESSAGE);
					ErrorMsg.addErrorMessage(err);
					return;
				}
			}
		});
		return debugPipelineTest0a;
	}
	
	private JMenuItem getMenuItemAnalyseFromLabelImage(final MongoTreeNode targetTreeNode, final AbstractPhenotypingTask iat) {
		JMenuItem debugPipelineTest00a = new JMenuItem(
				StringManipulationTools.removeHTMLtags(iat.getName()) + " (Reference+Old Reference)");
		debugPipelineTest00a.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<NumericMeasurementInterface> match = IAPservice
							.getMatchForReference(imageResult
									.getBinaryFileInfo()
									.getFileNameMain(),
									targetTreeNode.getExperiment().getExperiment());
					
					BlockPipeline.debugTryAnalysis(
							targetTreeNode.getExperiment(),
							match,
							iat);
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, "Error: "
							+ err.getLocalizedMessage()
							+ ". Command execution error.",
							"Error",
							JOptionPane.INFORMATION_MESSAGE);
					ErrorMsg.addErrorMessage(err);
					return;
				}
			}
		});
		return debugPipelineTest00a;
	}
	
	public void setAdditionalActionListener(ActionListener additionalActionListener) {
		this.additionalActionListener = additionalActionListener;
	}
	
	public ActionListener getAdditionalActionListener() {
		return additionalActionListener;
	}
	
	private JMenu getAnnotationChangerSubmenu() {
		JMenu fm = new JMenu("Modify Annotation (Outlier/Flag)");
		fm.setIcon(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Bookmark-New-64.png").getScaledInstance(16, 16,
				java.awt.Image.SCALE_SMOOTH)));
		fm.add(getCameraSelectionMenu(CameraSelection.THIS_CAMERA));
		fm.add(getCameraSelectionMenu(CameraSelection.ALL_CAMERAS_AND_MEASURMENT_TYPES));
		
		return fm;
	}
	
	private JMenu getCameraSelectionMenu(CameraSelection cs) {
		JMenu cm = new JMenu(cs + "");
		cm.add(getDaySelectionMenu(cs, DaySelection.THIS_SNAPSHOT));
		cm.add(getDaySelectionMenu(cs, DaySelection.THIS_DAY));
		cm.add(getDaySelectionMenu(cs, DaySelection.ALL_DAYS));
		cm.add(getDaySelectionMenu(cs, DaySelection.FROM_THIS_DAY));
		cm.add(getDaySelectionMenu(cs, DaySelection.UNTIL_THIS_DAY));
		
		return cm;
	}
	
	private JMenu getDaySelectionMenu(CameraSelection cs, DaySelection ds) {
		JMenu dm = new JMenu(ds + "");
		
		dm.add(getPlantSelectionMenu(cs, ds, PlantSelection.THIS_PLANT));
		dm.add(getPlantSelectionMenu(cs, ds, PlantSelection.ALL_PLANTS));
		
		return dm;
	}
	
	private JMenu getPlantSelectionMenu(CameraSelection cs, DaySelection ds, PlantSelection ps) {
		JMenu pm = new JMenu(ps + "");
		
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.MARK_AS_OUTLIER));
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.REMOVE_OUTLIER_MARK));
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.TOGGLE_OUTLIER_MARK));
		pm.addSeparator();
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.FLAG));
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.UNFLAG));
		pm.add(getAnnotationChangeMenuItem(cs, ds, ps, AnnotionChangeCommand.TOGGLE_FLAG));
		
		return pm;
	}
	
	private JMenuItem getAnnotationChangeMenuItem(CameraSelection cs, DaySelection ds, PlantSelection ps, AnnotionChangeCommand mode) {
		JMenuItem cmd = new JMenuItem("" + mode);
		cmd.setAction(getAnnotionChangeAction(cs, ds, ps, mode));
		return cmd;
	}
	
	private Action getAnnotionChangeAction(final CameraSelection cs, final DaySelection ds, final PlantSelection ps, final AnnotionChangeCommand mode) {
		Action res = new AbstractAction("" + mode) {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String annotation = mode.getAnnotation();
					SetMode sm = mode.getSetMode();
					Collection<NumericMeasurementInterface> match = IAPservice.matchHelper(targetTreeNode.getExperiment().getExperiment())
							.getMatchForReference(cs, ds, ps, (NumericMeasurementInterface) imageResult.getBinaryFileInfo().getEntity());
					for (NumericMeasurementInterface nm : match) {
						NumericMeasurement3D nmi = (NumericMeasurement3D) nm;
						if (sm == SetMode.SET_VALUE)
							nmi.setAnnotationField(annotation, "1");
						else
							if (sm == SetMode.REMOVE_VALUE)
								nmi.removeAnnotationField(annotation);
							else
								if (sm == SetMode.TOGGLE) {
									String v = nmi.getAnnotationField(annotation);
									if (v == null || v.equals("0"))
										nmi.setAnnotationField(annotation, "1");
									else
										nmi.removeAnnotationField(annotation);
								}
					}
					if (buttonsInThisView != null) {
						synchronized (buttonsInThisView) {
							for (DataSetFileButton dsfb : buttonsInThisView)
								dsfb.updateAnnotationCheckboxValueView();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					MainFrame.showMessageDialog("Error: " + e.getMessage(), "Error");
				}
			}
		};
		return res;
	}
	
	protected void updateAnnotationCheckboxValueView() {
		if (aip != null) {
			aip.removeGui();
			aip.addGui(true);
		}
	}
	
	public void setAnnotationInfoPanel(AnnotationInfoPanel aip) {
		this.aip = aip;
	}
}
