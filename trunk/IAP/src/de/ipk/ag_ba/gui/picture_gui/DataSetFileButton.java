package de.ipk.ag_ba.gui.picture_gui;

import ij.ImagePlus;
import ij.io.FileInfoXYZ;
import ij.io.Opener;
import ij.io.TiffDecoder;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
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
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HomeFolder;
import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.BarleyAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.Maize3DanalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;
import de.ipk.ag_ba.vanted.LoadedVolumeExtension;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
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
	
	MongoDB m;
	
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
	
	public DataSetFileButton(final MongoDB m,
			final MongoTreeNode targetTreeNode, String label, MyImageIcon icon,
			ImageIcon previewImage) {
		super();
		
		progress = new JProgressBar(0, 100);
		progress.setValue(-1);
		
		progress.setVisible(false);
		
		updateLayout(label, icon, previewImage);
		
		addActionListener(this);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		this.targetTreeNode = targetTreeNode;
		
		this.m = m;
		
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
				if (e.getButton() == MouseEvent.BUTTON3 && primary) {
					JPopupMenu jp = new JPopupMenu("Debug");
					
					JMenuItem debugPipelineTestShowMainImage = new JMenuItem(
							"Show Image");
					debugPipelineTestShowMainImage
							.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									try {
										IOurl s = imageResult
												.getBinaryFileInfo()
												.getFileNameMain();
										Collection<NumericMeasurementInterface> match = IAPservice
												.getMatchFor(s, targetTreeNode
														.getExperiment());
										
										for (NumericMeasurementInterface nmi : match) {
											ImageData id = (ImageData) nmi;
											if (id.getURL().getDetail()
													.equals(s.getDetail())) {
												String oldRef = id.getURL()
														.toString();
												IOurl u = new IOurl(oldRef);
												FlexibleImage fi = new FlexibleImage(
														u);
												fi.print("Main Image");
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
										FlexibleImage fi = new FlexibleImage(s);
										fi.print("Reference Image");
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
														.getExperiment());
										
										for (NumericMeasurementInterface nmi : match) {
											ImageData id = (ImageData) nmi;
											if (id.getURL().getDetail()
													.equals(s.getDetail())) {
												String oldRef = id
														.getAnnotationField("oldreference");
												
												IOurl u = new IOurl(oldRef);
												FlexibleImage fi = new FlexibleImage(
														u);
												fi.print("Annotation Image");
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
												targetTreeNode.getExperiment());
								if (match.size() > 0) {
									FlexibleImageStack snapshot = new FlexibleImageStack();
									for (NumericMeasurementInterface nmi : match) {
										if (nmi instanceof ImageData) {
											ImageData id = (ImageData) nmi;
											if (id.getURL() != null) {
												FlexibleImage fi = new FlexibleImage(
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
												FlexibleImage fi = new FlexibleImage(
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
												FlexibleImage fi = new FlexibleImage(
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
									snapshot.print("Snapshot "
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
					
					JMenuItem debugPipelineTest0 = new JMenuItem(
							"Arabidopsis Analysis Pipeline (Image+Reference)");
					debugPipelineTest0.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchFor(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment());
								
								BlockPipeline.debugTryAnalysis(
										targetTreeNode.getExperiment(),
										match, m,
										new ArabidopsisAnalysisTask());
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
					
					JMenuItem debugPipelineTest00 = new JMenuItem(
							"Arabidopsis Analysis Pipeline (Reference+Old Reference)");
					debugPipelineTest00.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchForReference(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment(),
												m);
								
								BlockPipeline.debugTryAnalysis(
										targetTreeNode.getExperiment(),
										match, m,
										new ArabidopsisAnalysisTask());
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
					
					JMenuItem debugPipelineTest1 = new JMenuItem(
							"Maize Analysis Pipeline (Image+Reference)");
					debugPipelineTest1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchFor(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment());
								
								BlockPipeline.debugTryAnalysis(
										targetTreeNode.getExperiment(),
										match, m,
										new MaizeAnalysisTask());
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
					
					JMenuItem debugPipelineTest2 = new JMenuItem(
							"Maize Analysis Pipeline (Reference+Old Reference)");
					debugPipelineTest2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchForReference(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment(),
												m);
								
								BlockPipeline.debugTryAnalysis(
										targetTreeNode.getExperiment(),
										match, m,
										new MaizeAnalysisTask());
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
					
					JMenuItem debugPipelineTest3 = new JMenuItem(
							"Barley Analysis Pipeline (Image+Reference)");
					debugPipelineTest3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchFor(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment());
								BlockPipeline.debugTryAnalysis(targetTreeNode.getExperiment(), match, m,
										new BarleyAnalysisTask());
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
					
					JMenuItem debugPipelineTest4 = new JMenuItem(
							"Barley Analysis Pipeline (Reference+Old Reference)");
					debugPipelineTest4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchForReference(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(),
												targetTreeNode.getExperiment(),
												m);
								
								BlockPipeline.debugTryAnalysis(
										targetTreeNode.getExperiment(),
										match, m,
										new BarleyAnalysisTask());
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
					
					JMenuItem debugPipelineTest5 = new JMenuItem(
							"Maize 3-D Analysis (Snapshot Images+References)");
					debugPipelineTest5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								MappingDataEntity img = imageResult
										.getBinaryFileInfo().getEntity();
								ImageData i = (ImageData) img;
								
								ExperimentInterface experiment = targetTreeNode
										.getExperiment();
								
								Collection<NumericMeasurementInterface> match = IAPservice
										.getMatchFor(imageResult
												.getBinaryFileInfo()
												.getFileNameMain(), experiment);
								
								ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
								
								for (SubstanceInterface m : experiment) {
									Substance3D m3 = (Substance3D) m;
									for (ConditionInterface s : m3) {
										Condition3D s3 = (Condition3D) s;
										for (SampleInterface sd : s3) {
											Sample3D sd3 = (Sample3D) sd;
											boolean found = false;
											search: for (NumericMeasurementInterface nmi : sd3) {
												for (NumericMeasurementInterface nmiHIT : match) {
													if (nmi == nmiHIT) {
														found = true;
														break search;
													}
												}
											}
											if (found)
												workload.add(sd3);
										}
									}
								}
								
								final ThreadSafeOptions tso = new ThreadSafeOptions();
								tso.setInt(1);
								
								final Maize3DanalysisTask task = new Maize3DanalysisTask();
								task.setInput(AbstractPhenotypingTask.getWateringInfo(experiment), workload, null, null, 0, 1);
								
								final BackgroundTaskStatusProviderSupportingExternalCall sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
										"Maize 3-D Reconstruction", "");
								Runnable backgroundTask = new Runnable() {
									@Override
									public void run() {
										try {
											task.performAnalysis(SystemAnalysis
													.getNumberOfCPUs(), 1, sp);
											Collection<NumericMeasurementInterface> out = task
													.getOutput();
											FlexibleImageStack fis = new FlexibleImageStack();
											for (NumericMeasurementInterface nmi : out) {
												if (nmi instanceof LoadedImage) {
													LoadedImage li = (LoadedImage) nmi;
													fis.addImage(
															((LoadedImage) nmi)
																	.getPositionIn3D(),
															new FlexibleImage(
																	li.getLoadedImage()));
												}
												if (nmi instanceof LoadedVolumeExtension) {
													LoadedVolumeExtension lve = (LoadedVolumeExtension) nmi;
													lve.getSideViewGif(512,
															512, sp);
												}
											}
											fis.print("Foreground images");
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								};
								BackgroundTaskHelper.issueSimpleTaskInWindow(
										"Maize 3-D Debug Test",
										"Initialize...", backgroundTask, null,
										sp);
								
							} catch (Exception err) {
								JOptionPane.showMessageDialog(null, "Error: "
										+ err.getMessage()
										+ ". Command execution error.",
										"Error",
										JOptionPane.INFORMATION_MESSAGE);
								ErrorMsg.addErrorMessage(err);
								return;
							}
						}
					});
					
					jp.add(debugPipelineTestShowMainImage);
					jp.add(debugPipelineTestShowReferenceImage);
					jp.add(debugPipelineTestShowImage);
					
					JMenu sn = new JMenu("Snapshot");
					JMenuItem a = new JMenuItem("Main");
					a.addActionListener(getListener(targetTreeNode, true,
							false, false));
					sn.add(a);
					JMenuItem b = new JMenuItem("Reference");
					b.addActionListener(getListener(targetTreeNode, false,
							true, false));
					sn.add(b);
					JMenuItem c = new JMenuItem("Annotation");
					c.addActionListener(getListener(targetTreeNode, false,
							false, true));
					sn.add(c);
					
					JMenuItem debugShowSnapshotNoStack = new JMenuItem(
							"Main, Reference, Annotation");
					debugShowSnapshotNoStack.addActionListener(getListener(
							targetTreeNode, true, true, true));
					sn.add(debugShowSnapshotNoStack);
					sn.add(debugShowSnapshot);
					jp.add(sn);
					
					jp.add(debugPipelineTest0);
					jp.add(debugPipelineTest00);
					jp.add(debugPipelineTest1);
					jp.add(debugPipelineTest2);
					jp.add(debugPipelineTest3);
					jp.add(debugPipelineTest4);
					jp.add(debugPipelineTest5);
					jp.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
	
	JLabel mmlbl;
	private boolean primary;
	
	public void updateLayout(String label, MyImageIcon icon,
			ImageIcon previewImage) {
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
	
	public DataSetFileButton(MongoDB m, MongoTreeNode projectNode,
			ImageResult imageResult, ImageIcon previewImage, boolean readOnly) {
		this(
				m,
				projectNode,
				"<html><body><b>"
						+ getMaxString(strip(
								imageResult.getFileNameMain(),
								((NumericMeasurement3D) imageResult
										.getBinaryFileInfo().entity)
										.getQualityAnnotation()
										+ "<br>("
										+ (((NumericMeasurement3D) imageResult
												.getBinaryFileInfo().entity)
												.getPosition() != null ? ((NumericMeasurement3D) imageResult
												.getBinaryFileInfo().entity)
												.getPosition().intValue()
												+ ")" : "0)")))
						+ "</b></body></html>", null, previewImage);
		this.imageResult = imageResult;
		this.readOnly = readOnly;
		if (getMaxString(imageResult.getFileNameMain()).endsWith("..."))
			setToolTipText(imageResult.getFileNameMain());
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
	public void actionPerformed(final ActionEvent evt) {
		
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
				tempItem.setEnabled(false);
				myPopup.add(tempItem);
				myPopup.show(this, 5, 5);
				
				downloadNeeded = false;
				setProgressValue(-1);
				showProgressbar();
				MyThread download;
				try {
					download = new MyThread(new Runnable() {
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
								System.out.println(urlMain);
								InputStream isMain = urlMain != null ? urlMain
										.getInputStream() : null;
								if (isMain == null)
									System.out.println("Inputstream = null");
								HomeFolder.copyFile(isMain, tfMain);
								
								IOurl urlLabel = imageResult
										.getBinaryFileInfo().getFileNameLabel();
								System.out.println(urlLabel);
								InputStream isLabel = urlLabel != null ? urlLabel
										.getInputStream() : null;
								if (isLabel == null)
									System.out.println("Inputstream = null");
								HomeFolder.copyFile(isLabel, tfLabel);
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
												.downloadFile(m, imageResult
														.getHashMain(), tfMain,
														DataSetFileButton.this,
														MongoCollection.VOLUMES);
								} catch (Exception e) {
									DataExchangeHelperForExperiments
											.downloadFile(m,
													imageResult.getHashMain(),
													tfMain,
													DataSetFileButton.this,
													MongoCollection.IMAGES);
									DataExchangeHelperForExperiments
											.downloadFile(m, imageResult
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
					BackgroundThreadDispatcher.addTask(download, 1 + 1000, 0, true);
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
				FlexibleImage fi = new FlexibleImage(myImage.fileURLmain);
				fi.print("Image View - "
						+ myImage.fileURLmain.getFileNameDecoded());
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
				FlexibleImage fi = null;
				try {
					fi = new FlexibleImage(myImage.fileURLlabel);
				} catch (Exception err) {
					// try to load as TIFF..
				}
				if (fi == null || fi.getWidth() == 0) {
					try {
						TiffDecoder tid = new TiffDecoder(
								myImage.fileURLlabel.getInputStream(),
								myImage.fileURLlabel.getFileName());
						FileInfoXYZ[] info = tid.getTiffInfo();
						Opener o = new Opener();
						ImagePlus imp = o.openTiffStack(info);
						imp.show("Image Label View - "
								+ myImage.fileURLlabel.getFileNameDecoded());
						IAPmain.showImageJ();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,
								"Error: " + e.getLocalizedMessage()
										+ ". Image can not be shown.",
								"Unknown Image Format",
								JOptionPane.INFORMATION_MESSAGE);
					}
				} else
					if (fi != null)
						fi.print("Image Label View - "
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
			if (imageResult.getFileNameMain().contains(File.separator))
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
				targetTreeNode.updateSizeInfo(m, sizeChangedListener);
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
				targetTreeNode.updateSizeInfo(m, sizeChangedListener);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// if (user.equals(Consts.ROOTUSERNAME.toString())) { // TODO
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
		showImageCmdLabel = new JMenuItem("Show Image (Reference)");
		showVolumeCmd = new JMenuItem("Show 3D-Volume");
		saveFileCmdMain = new JMenuItem("Save File As...");
		saveFileCmdLabel = new JMenuItem("Save Reference File As...");
		openFileCmdMain = new JMenuItem(
				"View/Open with system default application");
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
				|| imageResult.getFileNameMain().contains(File.separator))
			myPopup.add(showImageCmdMain);
		if ((getIsJavaImage() > 0 && imageResult.getFileNameLabel() != null)
				|| (imageResult.getFileNameLabel() != null && imageResult
						.getFileNameLabel().contains(File.separator)))
			myPopup.add(showImageCmdLabel);
		if (imageResult.getBinaryFileInfo().getEntity() instanceof VolumeData)
			myPopup.add(showVolumeCmd);
		myPopup.add(openFileCmdMain);
		if (imageResult.getBinaryFileInfo().getHashLabel() != null)
			myPopup.add(openFileCmdLabel);
		myPopup.add(saveFileCmdMain);
		if (imageResult.getBinaryFileInfo().getHashLabel() != null)
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
		Image img = myImage.getImage();
		BufferedImage bi = new BufferedImage(img.getWidth(this),
				img.getHeight(this), BufferedImage.TYPE_INT_ARGB);
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
	}
	
	public void setDownloadNeeded(boolean b) {
		this.downloadNeeded = true;
		myImage = null;
	}
	
	private ActionListener getListener(final MongoTreeNode targetTreeNode,
			final boolean main, final boolean ref, final boolean anno) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<NumericMeasurementInterface> match = IAPservice
							.getMatchFor(imageResult.getBinaryFileInfo()
									.getFileNameMain(), targetTreeNode
									.getExperiment());
					if (match.size() > 0) {
						NumericMeasurementInterface a = match.iterator().next();
						String pre = "snapshot "
								+ a.getQualityAnnotation()
								+ " "
								+ a.getParentSample().getSampleTime()
								+ " "
								+ a.getParentSample().getParentCondition()
										.getConditionName();
						
						if (main)
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getURL() != null) {
										FlexibleImage fi = new FlexibleImage(
												id.getURL());
										fi.print(id.getSubstanceName() + " // "
												+ pre);
									}
								}
							}
						if (ref)
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getLabelURL() != null) {
										FlexibleImage fi = new FlexibleImage(
												id.getLabelURL());
										fi.print("Reference "
												+ id.getSubstanceName()
												+ " // " + pre);
									}
								}
							}
						if (anno)
							for (NumericMeasurementInterface nmi : match) {
								if (nmi instanceof ImageData) {
									ImageData id = (ImageData) nmi;
									if (id.getAnnotationField("oldreference") != null) {
										FlexibleImage fi = new FlexibleImage(
												new IOurl(
														id.getAnnotationField("oldreference")));
										fi.print("Annotation "
												+ id.getSubstanceName()
												+ " // " + pre);
									}
								}
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
}
