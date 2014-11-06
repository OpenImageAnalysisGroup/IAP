/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.util;

import ij.ImageJ;
import ij.WindowManager;
import ij.gui.ImageWindow;
import info.StopWatch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MeasurementFilter;
import org.ObjectRef;
import org.ReleaseInfo;
import org.Screenshot;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.Vector2d;
import org.graffiti.editor.ConfigureViewAction;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.session.EditorSession;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.media.jai.codec.SeekableStream;

import de.ipk.ag_ba.commands.AbstractGraphUrlNavigationAction;
import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ExportSetting;
import de.ipk.ag_ba.commands.experiment.process.report.MySnapshotFilter;
import de.ipk.ag_ba.commands.experiment.process.report.SnapshotFilter;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportZIP;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk.ag_ba.server.task_management.MassCopySupport;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentCalculationService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

/**
 * @author klukas
 */
public class IAPservice {
	
	public static boolean isReachable(String host) {
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			int time;
			if (SystemAnalysis.isWindowsRunning())
				time = 2000;
			else
				time = 200;
			boolean reachable = address.isReachable(time);
			if (!reachable)
				return false;
			else
				return true;
		} catch (Exception e1) {
			return false;
		}
	}
	
	public static NavigationButton getPathwayViewEntity(final PathwayWebLinkItem mmc, GUIsetting guiSettings) {
		NavigationButton ne = new NavigationButton(getPathwayViewAction(mmc), guiSettings);
		return ne;
	}
	
	public static AbstractGraphUrlNavigationAction getPathwayViewAction(final PathwayWebLinkItem mmc) {
		return new AbstractGraphUrlNavigationAction("Load web-folder content") {
			private NavigationButton src = null;
			private final ObjectRef graphRef = new ObjectRef();
			private final ObjectRef scrollpaneRef = new ObjectRef();
			
			@Override
			public String getDefaultTitle() {
				return mmc.toString();
			}
			
			@Override
			public String getDefaultImage() {
				return "img/graphfile_t.png";
			}
			
			@Override
			public IOurl getURL() {
				return mmc.getURL();
			}
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;
				
				try {
					final IOurl url = mmc.getURL();
					if (loadDirect) {
						BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
							@Override
							public void run() {
								MainFrame.getInstance().loadGraph(url.getFileName(), url);
							}
						});
					} else {
						final Graph g = MainFrame.getGraph(url, url.getFileName());
						graphRef.setObject(g);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				if (!loadDirect)
					res.add(src);
				return res;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				if (loadDirect)
					return null;
				ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
				
				NavigationAction editInVantedAction = new AbstractNavigationAction("Show Graph in IAP Online-Version of VANTED") {
					Graph g;
					private NavigationButton src;
					
					@Override
					public void performActionCalculateResults(NavigationButton src) {
						g = (Graph) graphRef.getObject();
						this.src = src;
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
						ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
						// res.add(src);
						return res;
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						return null;// new ArrayList<NavigationButton>();
					}
					
					@Override
					public MainPanelComponent getResultMainPanel() {
						boolean showInline = SystemOptions.getInstance().getBoolean("VANTED", "debug-show-inline-iap", false);
						JComponent gui = IAPmain.showVANTED(showInline);
						// if (gui != null)
						// gui.setBorder(BorderFactory.createLoweredBevelBorder());
						if (gui != null)
							gui.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
						if (g != null) {
							MainFrame i = MainFrame.getInstance();
							if (i != null)
								i.showGraph(g, null, LoadSetting.VIEW_CHOOSER_NEVER);
						}
						return gui != null ? new MainPanelComponent(gui) : null;
					}
				};
				
				NavigationButton editInVanted = new NavigationButton(editInVantedAction, "Edit", "img/vanted1_0.png",
						src.getGUIsetting());
				result.add(editInVanted);
				
				JComponent zoomSlider = WebFolder.getZoomSliderForGraph(scrollpaneRef);
				result.add(new NavigationButton(zoomSlider, src.getGUIsetting()));
				
				return result;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				if (loadDirect)
					return null;
				try {
					Graph g = (Graph) graphRef.getObject();
					if (g != null) {
						boolean isMetaCrop = mmc.getURL().toString()
								.contains("http://vanted.ipk-gatersleben.de/addons/metacrop");
						if (isMetaCrop) {
							System.out.println("Adding MetaCrop links");
							WebFolder.addAnnotationsToGraphElements(g);
						}
						EditorSession es = new EditorSession(g);
						final ObjectRef refLastURL = new ObjectRef();
						final ObjectRef refLastDragPoint = new ObjectRef("", new Vector2d(0, 0));
						MainFrame mf = MainFrame.getInstance();
						if (mf == null)
							return null;
						JScrollPane graphViewScrollPane = mf.showViewChooserDialog(es, true, null,
								LoadSetting.VIEW_CHOOSER_NEVER_SHOW_DONT_ADD_VIEW_TO_EDITORSESSION, new ConfigureViewAction() {
									View newView;
									
									@Override
									public void storeView(View v) {
										newView = v;
									}
									
									@Override
									public void run() {
										final ObjectRef beingDragged = new ObjectRef("", false);
										
										final GraffitiView gv = (GraffitiView) newView;
										// gv.setDrawMode(DrawMode.REDUCED); // REDUCED
										gv.threadedRedraw = false;
										
										final MouseMotionListener mml = new MouseMotionListener() {
											@Override
											public void mouseMoved(MouseEvent e) {
												boolean urlFound = false;
												try {
													Component c = gv.findComponentAt(e.getX(), e.getY());
													if (c != null) {
														if (c instanceof GraphElementComponent) {
															GraphElementComponent gc = (GraphElementComponent) c;
															// String lbl =
															// AttributeHelper.getLabel(gc.getGraphElement(),
															// "no label");
															String url = AttributeHelper.getReferenceURL(gc.getGraphElement());
															urlFound = url != null && url.length() > 0;
															if (urlFound) {
																((Component) scrollpaneRef.getObject()).setCursor(Cursor
																		.getPredefinedCursor(Cursor.HAND_CURSOR));
																refLastURL.setObject(url);
															}
														}
														if (c instanceof AttributeComponent) {
															AttributeComponent ac = (AttributeComponent) c;
															GraphElement ge = (GraphElement) ac.getAttribute().getAttributable();
															String url = AttributeHelper.getReferenceURL(ge);
															urlFound = url != null && url.length() > 0;
															if (urlFound) {
																((Component) scrollpaneRef.getObject()).setCursor(Cursor
																		.getPredefinedCursor(Cursor.HAND_CURSOR));
																refLastURL.setObject(url);
															}
														}
													}
												} catch (Exception err) {
													System.out.println("e");
												}
												if (!urlFound) {
													((Component) scrollpaneRef.getObject()).setCursor(Cursor
															.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
													refLastURL.setObject(null);
												} else {
													
												}
											}
											
											@Override
											public void mouseDragged(MouseEvent e) {
												JViewport viewPort = ((JScrollPane) scrollpaneRef.getObject()).getViewport();
												if (!((Boolean) beingDragged.getObject())) {
													((Component) scrollpaneRef.getObject()).setCursor(Cursor
															.getPredefinedCursor(Cursor.MOVE_CURSOR));
													beingDragged.setObject(true);
													((Vector2d) refLastDragPoint.getObject()).x = e.getX();
													((Vector2d) refLastDragPoint.getObject()).y = e.getY();
												}
												refLastURL.setObject(null);
												
												Point scrollPosition = viewPort.getViewPosition();
												
												double dx = e.getX() - ((Vector2d) refLastDragPoint.getObject()).x;
												double dy = e.getY() - ((Vector2d) refLastDragPoint.getObject()).y;
												
												scrollPosition.x -= dx;
												scrollPosition.y -= dy;
												if (scrollPosition.x < 0)
													scrollPosition.x = 0;
												if (scrollPosition.y < 0)
													scrollPosition.y = 0;
												
												viewPort.setViewPosition(scrollPosition);
											}
										};
										
										gv.addMouseListener(new MouseListener() {
											@Override
											public void mouseReleased(MouseEvent e) {
												e.consume();
												if ((Boolean) beingDragged.getObject()) {
													beingDragged.setObject(false);
													((Component) scrollpaneRef.getObject()).setCursor(Cursor
															.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
												}
												mml.mouseMoved(e);
											}
											
											@Override
											public void mousePressed(MouseEvent e) {
												e.consume();
												if (!((Boolean) beingDragged.getObject())) {
													((Component) scrollpaneRef.getObject()).setCursor(Cursor
															.getPredefinedCursor(Cursor.MOVE_CURSOR));
													beingDragged.setObject(true);
													((Vector2d) refLastDragPoint.getObject()).x = e.getX();
													((Vector2d) refLastDragPoint.getObject()).y = e.getY();
												}
											}
											
											@Override
											public void mouseExited(MouseEvent e) {
											}
											
											@Override
											public void mouseEntered(MouseEvent e) {
											}
											
											@Override
											public void mouseClicked(MouseEvent e) {
												e.consume();
												String url = (String) refLastURL.getObject();
												if (url != null && url.length() > 0) {
													AttributeHelper.showInBrowser(url);
												}
											}
										});
										gv.addMouseMotionListener(mml);
										
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												BackgroundTaskHelper.executeLaterOnSwingTask(100, new Runnable() {
													@Override
													public void run() {
														if (ReleaseInfo.getApplet() != null)
															ReleaseInfo.getApplet().repaint();
													}
												});
											}
										});
									}
									
								});
						graphViewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						graphViewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						scrollpaneRef.setObject(graphViewScrollPane);
						return new MainPanelComponent(graphViewScrollPane);
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				return null;
			}
			
		};
	}
	
	public static ArrayList<String> portScan(String hostname, BackgroundTaskStatusProviderSupportingExternalCall status) {
		ArrayList<String> res = new ArrayList<String>();
		
		int port = 0;
		
		for (port = 0; port < 65536; port++) {
			try {
				Socket s = new Socket();
				s.connect(new InetSocketAddress(hostname, port), 100);
				res.add("Open port: " + port);
				s.close();
			} catch (Exception ex) {
				// not listening on this port
			}
			if (status != null) {
				status.setCurrentStatusValueFine(100d * port / 65536d);
				status.setCurrentStatusText1("Port " + port);
			}
		}
		return res;
	}
	
	public static String getCurrentTimeAsNiceString() {
		return new SimpleDateFormat().format(new Date());
	}
	
	public static ConditionInterface[] sort(ConditionInterface[] array) {
		Arrays.sort(array, new Comparator<ConditionInterface>() {
			@Override
			public int compare(ConditionInterface o1, ConditionInterface o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		return array;
	}
	
	public static ArrayList<NumericMeasurementInterface> getMatchFor(IOurl url, ExperimentInterface experiment, boolean ignoreTime) {
		
		ArrayList<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		
		String searchKey = null;
		Collection<NumericMeasurementInterface> ml = Substance3D.getAllMeasurements(experiment);
		for (NumericMeasurementInterface md : ml) {
			if (md instanceof ImageData) {
				ImageData id = (ImageData) md;
				if (url.equals(id.getURL())) {
					String key = id.getParentSample().getFullId(!ignoreTime) + ";" + id.getReplicateID() + ";" + id.getPosition();
					String name = id.getParentSample().getParentCondition().getParentSubstance().getName();
					if (name.contains("."))
						if (LTdataExchange.positionFirst)
							key += name.substring(0, name.indexOf("."));
						else
							key += name.substring(name.lastIndexOf("."));
					searchKey = key + "";
					break;
				}
			}
		}
		if (searchKey != null) {
			for (NumericMeasurementInterface md : ml) {
				if (md instanceof ImageData) {
					ImageData id = (ImageData) md;
					String key = id.getParentSample().getFullId(!ignoreTime) + ";" + id.getReplicateID() + ";" + id.getPosition();
					String name = id.getParentSample().getParentCondition().getParentSubstance().getName();
					if (name.contains("."))
						if (LTdataExchange.positionFirst)
							key += name.substring(0, name.indexOf("."));
						else
							key += name.substring(name.lastIndexOf("."));
					if (searchKey.equalsIgnoreCase(key)) {
						result.add(id);
					}
				}
			}
		}
		
		if (ignoreTime) {
			// sort snapshots, as data from different time points will be returned
			Collections.sort(result, new Comparator<NumericMeasurementInterface>() {
				@Override
				public int compare(NumericMeasurementInterface a, NumericMeasurementInterface b) {
					long tA = a.getParentSample().getSampleFineTimeOrRowId() != null ? a.getParentSample().getSampleFineTimeOrRowId() : a.getParentSample()
							.getTime();
					long tB = b.getParentSample().getSampleFineTimeOrRowId() != null ? b.getParentSample().getSampleFineTimeOrRowId() : b.getParentSample()
							.getTime();
					return tA < tB ? -1 : (tA == tB ? 0 : 1);
				}
			});
		}
		
		return result;
	}
	
	public static Collection<NumericMeasurementInterface> getMatchForReference(IOurl fileNameMain,
			ExperimentInterface experiment) {
		Collection<NumericMeasurementInterface> pairs = getMatchFor(fileNameMain, experiment, false);
		
		Collection<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		
		for (NumericMeasurementInterface nmi : pairs) {
			ImageData id = (ImageData) nmi;
			if (id.getLabelURL() != null) {
				ImageData idMod = (ImageData) id.clone(id.getParentSample());
				idMod.setURL(idMod.getLabelURL());
				String annotation = idMod.getAnnotation();
				if (annotation != null && annotation.length() > 0 && id.getAnnotationField("oldreference") != null) {
					idMod.setLabelURL(new IOurl(idMod.getAnnotationField("oldreference")));
				} else
					idMod.setLabelURL(null);
				result.add(idMod);
			}
		}
		
		return result;
	}
	
	public static Match matchHelper(ExperimentInterface experiment) {
		return new Match(experiment);
	}
	
	public final static float[] cubeRoots = getCubeRoots(0f, 1.1f, 1100);
	private static HashMap<String, String> niceNames = initNiceNames();
	
	public static float[] getCubeRoots(float lo, float up, int n) {
		StopWatch s = new StopWatch("INFO: cube_roots", false);
		float[] res = new float[n + 1];
		float sq = 1f / 3f;
		for (int i = 0; i <= n; i++) {
			float x = lo + i * (up - lo) / n;
			res[i] = (float) Math.pow(x, sq);
		}
		s.printTime(1000);
		return res;
	}
	
	private static HashMap<String, String> initNiceNames() {
		HashMap<String, String> res = new HashMap<String, String>();
		res.put("weight_before", "Weight before watering");
		res.put("water_weight", "Water weight");
		res.put("side.vis.height.norm", "Height (normalized)");
		res.put("side.vis.height", "Height");
		res.put("side.vis.area.norm", "Side Vis Area (normalized)");
		res.put("side.vis.area", "Side Vis Area");
		res.put("side.fluo.intensity.mean", "Fluo intensity (side)");
		res.put("side.nir.intensity.mean", "NIR intensity (side)");
		res.put("top.fluo.intensity.mean", "Fluo intensity (top)");
		res.put("top.nir.intensity.mean", "NIR intensity (top)");
		res.put("side.vis.hsv.h.mean", "Average hue (side)");
		res.put("top.vis.hsv.h.mean", "Average hue (top)");
		res.put("side.vis.width.norm", "Width (normalized)");
		res.put("side.vis.width", "Width");
		res.put("top.vis.area.norm", "Top Vis Area (normalized)");
		res.put("top.vis.area", "Top Vis Area");
		res.put("combined.fluo.volume.iap", "Volume estimation (fluo)");
		return res;
	}
	
	public static LinkedList<SnapshotDataIAP> getSnapshotsFromExperiment(
			UrlCacheManager urlManager,
			ExperimentInterface experiment,
			HashMap<String, Integer> optSubstanceIds,
			boolean prepareTransportToBrowser,
			boolean exportIndividualAngles,
			boolean storeAllReplicates,
			SnapshotFilter optSnapshotFilter,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, ExportSetting optCustomSubsetDef, boolean useZIPexportFileNames)
			throws InterruptedException {
		return getSnapshotsFromExperiment(urlManager,
				experiment,
				optSubstanceIds,
				prepareTransportToBrowser,
				exportIndividualAngles,
				storeAllReplicates,
				optSnapshotFilter,
				optStatus, optCustomSubsetDef, useZIPexportFileNames, null, null, null, null);
	}
	
	public static LinkedList<SnapshotDataIAP> getSnapshotsFromExperiment(
			final UrlCacheManager urlManager,
			ExperimentInterface experimentNF,
			final HashMap<String, Integer> optSubstanceIds,
			final boolean prepareTransportToBrowser,
			final boolean exportIndividualAngles,
			final boolean storeAllReplicates,
			final SnapshotFilter optSnapshotFilter,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			final ExportSetting optCustomSubsetDef,
			final boolean useZIPexportFileNames,
			HashSet<NumericMeasurement> lowerSingleNF,
			HashSet<NumericMeasurement> upperSingleNF,
			HashSet<SampleInterface> lowerCombinedNF,
			HashSet<SampleInterface> upperCombinedNF) throws InterruptedException {
		if (lowerSingleNF != null && lowerSingleNF.isEmpty())
			lowerSingleNF = null;
		if (upperSingleNF != null && upperSingleNF.isEmpty())
			upperSingleNF = null;
		if (lowerCombinedNF != null && lowerCombinedNF.isEmpty())
			lowerCombinedNF = null;
		if (upperCombinedNF != null && upperCombinedNF.isEmpty())
			upperCombinedNF = null;
		
		final HashSet<NumericMeasurement> lowerSingle = lowerSingleNF;
		final HashSet<NumericMeasurement> upperSingle = upperSingleNF;
		final HashSet<SampleInterface> lowerCombined = lowerCombinedNF;
		final HashSet<SampleInterface> upperCombined = upperCombinedNF;
		
		System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set...");
		System.out.println("Transport to browser? " + prepareTransportToBrowser);
		System.out.println("Store all angles? " + exportIndividualAngles);
		System.out.println("Store all replicates? " + storeAllReplicates);
		
		StopWatch sw = new StopWatch("Create Snapshots");
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Rename substances");
		final HashMap<String, SnapshotDataIAP> timestampAndQuality2snapshot = new HashMap<String, SnapshotDataIAP>();
		
		final LinkedList<SnapshotDataIAP> result = new LinkedList<SnapshotDataIAP>();
		
		if (experimentNF != null) {
			for (SubstanceInterface substance : experimentNF) {
				if (optCustomSubsetDef != null && optCustomSubsetDef.ignoreSubstance(substance))
					continue;
				if (substance.getName() != null && substance.getName().contains(".bin.")) {
					String oldName = substance.getName();
					oldName = StringManipulationTools.stringReplace(oldName, ".0.", ".00.");
					oldName = StringManipulationTools.stringReplace(oldName, ".1.", ".01.");
					oldName = StringManipulationTools.stringReplace(oldName, ".2.", ".02.");
					oldName = StringManipulationTools.stringReplace(oldName, ".3.", ".03.");
					oldName = StringManipulationTools.stringReplace(oldName, ".4.", ".04.");
					oldName = StringManipulationTools.stringReplace(oldName, ".5.", ".05.");
					oldName = StringManipulationTools.stringReplace(oldName, ".6.", ".06.");
					oldName = StringManipulationTools.stringReplace(oldName, ".7.", ".07.");
					oldName = StringManipulationTools.stringReplace(oldName, ".8.", ".08.");
					oldName = StringManipulationTools.stringReplace(oldName, ".9.", ".09.");
					substance.setName(oldName);
				}
			}
			Experiment e = (Experiment) experimentNF;
			e.sortSubstances();
		}
		
		boolean hasTemperatureData = false;
		TreeMap<Long, Double> timeDay2averageTemp = new TreeMap<Long, Double>();
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process Climate Data");
		
		if (experimentNF != null) {
			String type = experimentNF.getHeader().getExperimentType();
			if (type == null)
				type = "";
			double ggd_baseline;
			if (type.equals("Barley")) {
				ggd_baseline = SystemOptions.getInstance().getDouble("Growing-Degree-Days", "Barley-Baseline", 5.5);
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Growing-degree days, using baseline for Barley, "
						+ ggd_baseline + " °C");
			} else
				if (type.equals("Maize")) {
					ggd_baseline = SystemOptions.getInstance().getDouble("Growing-Degree-Days", "Maize-Baseline", 10);
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Growing-degree days, using baseline for Maize, "
							+ ggd_baseline + " °C");
				} else {
					ggd_baseline = SystemOptions.getInstance().getDouble("Growing-Degree-Days", "Default-Baseline", 10);
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Growing-degree days, using default baseline (type is neither Maize nor Barley), "
							+ ggd_baseline + " °C");
				}
			GregorianCalendar gc = new GregorianCalendar();
			for (SubstanceInterface substance : experimentNF) {
				if (optCustomSubsetDef != null && optCustomSubsetDef.ignoreSubstance(substance))
					continue;
				if (substance.getName() != null && substance.getName().equals("temp.air.mean")) {
					for (ConditionInterface ci : substance) {
						for (SampleInterface sa : ci) {
							Long time = sa.getSampleFineTimeOrRowId();
							if (time != null) {
								double temp = sa.getSampleAverage().getValue();
								timeDay2averageTemp.put(SystemAnalysis.getUnixDay(time, gc), temp - ggd_baseline);
							}
						}
					}
					hasTemperatureData = !timeDay2averageTemp.isEmpty();
				}
			}
			
			if (hasTemperatureData) {
				experimentNF = experimentNF.clone();
				for (SubstanceInterface substance : experimentNF) {
					if (optCustomSubsetDef != null && optCustomSubsetDef.ignoreSubstance(substance))
						continue;
					for (ConditionInterface c : sort(substance.toArray(new ConditionInterface[] {}))) {
						for (SampleInterface s : c) {
							Long time = s.getSampleFineTimeOrRowId();
							if (time == null)
								continue;
							// replace DAY X or DAS X with GDD Y, based on temperature data
							if (s.getTimeUnit().equalsIgnoreCase("day") || s.getTimeUnit().equalsIgnoreCase("das")) {
								long unixDay = SystemAnalysis.getUnixDay(time, gc);
								double ggd = getGGD(unixDay - s.getTime(), unixDay, timeDay2averageTemp);
								s.setTime((int) Math.round(ggd));
								s.setTimeUnit("GDD");
							}
							if (s.getTimeUnit().equalsIgnoreCase("unix day")) {
								long unixDay = s.getTime();
								double ggd = getGGD(unixDay, unixDay, timeDay2averageTemp);
								s.setTime((int) Math.round(ggd));
								s.setTimeUnit("GDD");
							}
						}
					}
				}
			}
		}
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create Snapshots");
		final ThreadSafeOptions sidx = new ThreadSafeOptions();
		if (experimentNF != null) {
			final ExperimentInterface experiment = experimentNF;
			final int scnt = experiment.size();
			ArrayList<LocalComputeJob> threads = new ArrayList<>();
			for (SubstanceInterface substance : experiment) {
				sidx.addInt(1);
				if (optCustomSubsetDef != null && optCustomSubsetDef.ignoreSubstance(substance))
					continue;
				final SubstanceInterface substanceF = substance;
				threads.add(BackgroundThreadDispatcher.addTask(new Runnable() {
					@Override
					public void run() {
						if (optStatus != null)
							optStatus.setCurrentStatusText1("Process subset " + sidx.getInt() + "/" + scnt);
						processConditions(urlManager, optSubstanceIds, exportIndividualAngles,
								storeAllReplicates, optSnapshotFilter, optStatus,
								timestampAndQuality2snapshot,
								result, substanceF, useZIPexportFileNames, lowerSingle, upperSingle, lowerCombined, upperCombined);
						if (optStatus != null)
							optStatus.setCurrentStatusValueFine(100d * sidx.getInt() / scnt);
					}
				}, "Process substance " + substance.getName()));
			}
			BackgroundThreadDispatcher.waitFor(threads);
		}
		sw.printTime(100);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Sorting Snapshots (" + result.size() + ")");
		
		sw = new StopWatch("Sort Snapshots");
		Collections.sort(result, new Comparator<SnapshotDataIAP>() {
			@Override
			public int compare(SnapshotDataIAP a, SnapshotDataIAP b) {
				int r;
				r = a.getDay().compareTo(b.getDay());
				if (r != 0)
					return r;
				// r = a.getCondition().compareTo(b.getCondition());
				// if (r != 0)
				// return r;
				if (a.getSnapshotTime() != null && b.getSnapshotTime() != null)
					r = a.getSnapshotTime().compareTo(b.getSnapshotTime());
				if (r != 0)
					return r;
				r = a.getCondition().compareTo(b.getCondition());
				if (r != 0)
					return r;
				return r;
			}
		});
		sw.printTime(50);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process Fields");
		
		for (SnapshotDataIAP sd : result)
			if (prepareTransportToBrowser)
				sd.prepareFieldsForDataTransport();
			else
				sd.prepareStore();
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Snapshot Set Created");
		
		return result;
	}
	
	private static void processConditions(UrlCacheManager urlManager, HashMap<String, Integer> optSubstanceIds, boolean exportIndividualAngles,
			boolean storeAllReplicates, SnapshotFilter optSnapshotFilter, BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			HashMap<String, SnapshotDataIAP> timestampAndQuality2snapshot, Collection<SnapshotDataIAP> result, SubstanceInterface substance,
			boolean useZIPexportFileNames,
			HashSet<NumericMeasurement> lowerSingle, HashSet<NumericMeasurement> upperSingle,
			HashSet<SampleInterface> lowerCombined, HashSet<SampleInterface> upperCombined) {
		for (ConditionInterface c : sort(substance.toArray(new ConditionInterface[] {}))) {
			for (SampleInterface sample : c) {
				TreeSet<String> qualities = new TreeSet<String>();
				if (storeAllReplicates) {
					for (NumericMeasurementInterface nmi : sample) {
						String q = getQuality(nmi);
						qualities.add(q);
					}
				} else
					qualities.add("");
				
				for (String qualityFilter : qualities) {
					Long snapshotTimeIndex = sample.getSampleFineTimeOrRowId();
					
					if (snapshotTimeIndex == null)
						snapshotTimeIndex = (long) sample.getTime();
					
					SnapshotDataIAP sn;
					boolean addSN = false;
					synchronized (timestampAndQuality2snapshot) {
						if (!timestampAndQuality2snapshot.containsKey(snapshotTimeIndex + "//" + qualityFilter)) {
							SnapshotDataIAP ns = new SnapshotDataIAP();
							timestampAndQuality2snapshot.put(snapshotTimeIndex + "//" + qualityFilter, ns);
							addSN = true;
						}
						sn = timestampAndQuality2snapshot.get(snapshotTimeIndex + "//" + qualityFilter);
					}
					synchronized (sn) {
						// set fields
						if (sn.getCondition() == null)
							sn.setCondition(c.getConditionName());
						// species, genotype, variety, growthCondition, treatment, sequence;
						if (sn.getSpecies() == null)
							sn.setSpecies(c.getSpecies());
						if (sn.getGenotype() == null)
							sn.setGenotype(c.getGenotype());
						if (sn.getVariety() == null)
							sn.setVariety(c.getVariety());
						if (sn.getGrowthCondition() == null)
							sn.setGrowthCondition(c.getGrowthconditions());
						if (sn.getTreatment() == null)
							sn.setTreatment(c.getTreatment());
						if (sn.getSequence() == null)
							sn.setSequence(c.getSequence());
						
						if (sn.getTimePoint() == null)
							sn.setTimePoint(sample.getSampleTime());
						if (sn.getSnapshotTime() == null)
							sn.setSnapshotTime(sample.getSampleFineTimeOrRowId());
						sn.setDay(sample.getTime());
						
						if (sample.size() > 0) {
							for (NumericMeasurementInterface mm : sample) {
								if (!isOKquality(qualityFilter, mm))
									continue;
								if (sn.getPlantId() == null) {
									if (mm.getQualityAnnotation() != null && mm.getQualityAnnotation().length() > 0)
										sn.setPlantId("" + mm.getQualityAnnotation());
									else
										sn.setPlantId("" + mm.getReplicateID());
								}
								// if (storeAllReplicates) {
								// System.out.println("Plant ID: " + sn.getPlantId());
								// if (mm.getQualityAnnotation() != null && sn.getPlantId() != null && !sn.getPlantId().contains(mm.getQualityAnnotation())) {
								// sn.setPlantId(sn.getPlantId() + "//" + mm.getQualityAnnotation());
								// System.out.println("-> Plant ID: " + sn.getPlantId());
								// }
								// sn.setPlantId(sn.getPlantId() + "//" + mm.getReplicateID());
								// System.out.println("--> Plant ID: " + sn.getPlantId());
								// }
								
								break;
							}
							String sub = sample.getParentCondition().getParentSubstance().getName();
							boolean isTop = sub != null && sub.startsWith("top.");
							if (optSubstanceIds != null &&
									!sub.equals("water_sum") && !sub.equals("weight_before") && !sub.equals("water_weight")) {
								sub = sample.getSubstanceNameWithUnit();
								int idx;
								synchronized (optSubstanceIds) {
									if (!optSubstanceIds.containsKey(sub)) {
										optSubstanceIds.put(sub, optSubstanceIds.size());
									}
									idx = optSubstanceIds.get(sub);
									// sample.recalculateSampleAverage();
								}
								if (sample.getSampleAverage() != null && qualityFilter == null) {
									double vvv = sample.calcMean();
									sn.storeValue(idx, vvv, lowerCombined.contains(sample), upperCombined.contains(sample));
								} else {
									// find all values with OK quality and calculate average
									double sum = 0;
									int n = 0;
									for (NumericMeasurementInterface nmi : sample) {
										if (!isOKquality(qualityFilter, nmi))
											continue;
										double v = nmi.getValue();
										if (!Double.isNaN(v)) {
											sum += v;
											n++;
										}
									}
									sn.storeValue(idx, sum / n,
											lowerCombined != null && lowerCombined.contains(sample),
											upperCombined != null && upperCombined.contains(sample));
								}
								if (exportIndividualAngles) {
									for (NumericMeasurementInterface nmi : sample) {
										if (qualityFilter != null && !isOKquality(qualityFilter, nmi))
											continue;
										NumericMeasurement3D nmi3d = (NumericMeasurement3D) nmi;
										Double p = nmi3d.getPosition();
										sn.storeAngleValue(idx, p, nmi3d.getValue(), isTop,
												lowerSingle != null && lowerSingle.contains(nmi),
												upperSingle != null && upperSingle.contains(nmi));
									}
								}
							} else {
								double mmSum = 0;
								double mmLowest = Double.MAX_VALUE;
								for (NumericMeasurementInterface mmm : sample) {
									if (qualityFilter != null && !isOKquality(qualityFilter, mmm))
										continue;
									double mmmValue = mmm.getValue();
									if (!Double.isNaN(mmmValue) && !Double.isInfinite(mmmValue)) {
										if (mmmValue > 0) {
											mmSum += mmmValue;
											if (mmmValue < mmLowest)
												mmLowest = mmmValue;
										}
									}
								}
								if (sub.equals("water_sum")) {
									if (mmSum > 0)
										sn.setWholeDayWaterAmount((int) mmSum);
								} else
									if (sub.equals("weight_before")) {
										if (mmLowest < Double.MAX_VALUE)
											sn.setWeightBefore(mmLowest);
									} else
										if (sub.equals("water_weight")) {
											if (mmSum > 0)
												sn.setWeightAfter(mmSum);
										}
							}
						}
						
						if (sample instanceof Sample3D && exportIndividualAngles) {
							Sample3D s3d = (Sample3D) sample;
							
							Collection<NumericMeasurementInterface> sl = sortImages(s3d.getMeasurements(MeasurementNodeType.IMAGE,
									MeasurementNodeType.VOLUME));
							int imageCount = 0;
							GregorianCalendar gc = new GregorianCalendar();
							for (NumericMeasurementInterface ii : sl) {
								if (qualityFilter != null && !isOKquality(qualityFilter, ii))
									continue;
								imageCount++;
								if (ii instanceof ImageData) {
									ImageData i = (ImageData) ii;
									String subn = ii.getParentSample().getParentCondition().getParentSubstance().getName();
									ImageConfiguration ic = ImageConfiguration.get(subn);
									String info = "+++" + substance.getInfo();
									long urlId = urlManager != null ? urlManager.getId(i, useZIPexportFileNames ?
											ActionDataExportZIP.getImageFileExportNameForZIPexport(gc, i) + info : i.getURL().toString() + info) : -1;
									if (ic == ImageConfiguration.Unknown) {
										ic = ImageConfiguration.get(i.getURL().getFileName());
									}
									Integer p = i.getPosition() != null ? i.getPosition().intValue() : null;
									if (p == null)
										p = 0;
									if (ic == ImageConfiguration.Unknown) {
										sn.addUnknown(urlId, p);
									} else {
										if (ic == ImageConfiguration.VisSide)
											sn.addRgb(urlId, p);
										if (ic == ImageConfiguration.FluoSide)
											sn.addFluo(urlId, p);
										if (ic == ImageConfiguration.NirSide)
											sn.addNir(urlId, p);
										if (ic == ImageConfiguration.IrSide)
											sn.addIr(urlId, p);
										
										if (ic == ImageConfiguration.VisTop)
											sn.addRgb(urlId, p < 1 ? -1 : -p);
										if (ic == ImageConfiguration.FluoTop)
											sn.addFluo(urlId, p < 1 ? -1 : -p);
										if (ic == ImageConfiguration.NirTop)
											sn.addNir(urlId, p < 1 ? -1 : -p);
										if (ic == ImageConfiguration.IrTop)
											sn.addIr(urlId, p < 1 ? -1 : -p);
									}
								}
								if (ii instanceof VolumeData) {
									VolumeData i = (VolumeData) ii;
									String subn = ii.getParentSample().getParentCondition().getParentSubstance().getName();
									ImageConfiguration ic = ImageConfiguration.get(subn);
									long urlId = urlManager.getId(i, i.getURL().toString());
									if (ic == ImageConfiguration.Unknown) {
										ic = ImageConfiguration.get(i.getURL().getFileName());
									}
									Integer p = i.getPosition() != null ? i.getPosition().intValue() : null;
									if (p == null)
										p = 0;
									if (ic == ImageConfiguration.Unknown) {
										sn.addUnknown(urlId, p);
									} else {
										if (ic == ImageConfiguration.VisSide)
											sn.addRgb(urlId, p);
										if (ic == ImageConfiguration.FluoSide)
											sn.addFluo(urlId, p);
										if (ic == ImageConfiguration.NirSide)
											sn.addNir(urlId, p);
										
										if (ic == ImageConfiguration.VisTop)
											sn.addRgb(urlId, p < 1 ? -1 : -p);
										if (ic == ImageConfiguration.FluoTop)
											sn.addFluo(urlId, p < 1 ? -1 : -p);
										if (ic == ImageConfiguration.NirTop)
											sn.addNir(urlId, p < 1 ? -1 : -p);
									}
								}
							}
							if (imageCount > 0 && optSubstanceIds != null) {
								String sub = sample.getSubstanceNameWithUnit();
								if (sub != null) {
									int idx = optSubstanceIds.get(sub);
									sn.storeValue(idx, (double) imageCount, false, false);
								}
							}
						}
					}
					
					if (addSN) {
						synchronized (result) {
							if (optSnapshotFilter == null)
								result.add(sn);
							else
								if (!optSnapshotFilter.filterOut(sn))
									result.add(sn);
								else {
									// System.out.println("About to filter out a snapshot: " + sn);
									// System.out.println("RES=" + optSnapshotFilter.filterOut(sn));
								}
							if (optStatus != null)
								optStatus.setCurrentStatusText2("Create Snapshots (" + result.size() + ")");
						}
					}
				}
			}
		}
	}
	
	private static String getQuality(NumericMeasurementInterface nmi) {
		String rs;
		if (Math.abs(nmi.getReplicateID()) < 10)
			rs = "00" + nmi.getReplicateID();
		else
			if (Math.abs(nmi.getReplicateID()) < 100)
				rs = "0" + nmi.getReplicateID();
			else
				rs = "" + nmi.getReplicateID();
		
		return rs + "//" + nmi.getQualityAnnotation();
	}
	
	private static boolean isOKquality(String qualityFilter, NumericMeasurementInterface nmi) {
		return qualityFilter.isEmpty() || qualityFilter.equals(getQuality(nmi));
	}
	
	/**
	 * If a day is missing in the temperature data, it will be looked up from the nearest timepoint with data.
	 * If the day before and the day after the day with missing data has data available, the day before will be used.
	 * 
	 * @return the sum of temperatures within the specified duration (including first and last day).
	 */
	private static double getGGD(long startUnixDay, long currentUnixDay, TreeMap<Long, Double> timeDay2averageTemp) {
		double sum = 0;
		if (!timeDay2averageTemp.isEmpty())
			for (long d = startUnixDay; d <= currentUnixDay; d++) {
				Double gdd = timeDay2averageTemp.get(d);
				if (gdd != null)
					sum += gdd;
				else {
					boolean found = false;
					int distance = 1;
					do {
						Double gdd_A = timeDay2averageTemp.get(d - distance);
						Double gdd_B = timeDay2averageTemp.get(d + distance);
						if (gdd_A != null) {
							sum += gdd_A;
							found = true;
						} else
							if (gdd_B != null) {
								sum += gdd_B;
								found = true;
							}
						distance++;
					} while (!found);
				}
			}
		return sum;
	}
	
	public static Collection<NumericMeasurementInterface> sortImages(Collection<NumericMeasurementInterface> measurements) {
		ArrayList<NumericMeasurementInterface> ml = new ArrayList<NumericMeasurementInterface>(measurements);
		Collections.sort(ml, new Comparator<NumericMeasurementInterface>() {
			@Override
			public int compare(NumericMeasurementInterface o1, NumericMeasurementInterface o2) {
				if (o1 instanceof ImageData && o2 instanceof ImageData) {
					ImageData id1 = (ImageData) o1;
					ImageData id2 = (ImageData) o2;
					int r1 = id1.getSubstanceName().compareTo(id2.getSubstanceName());
					if (r1 != 0)
						return r1;
					Double p1 = 0d;
					if (id1.getPosition() != null)
						p1 = id1.getPosition();
					Double p2 = 0d;
					if (id2.getPosition() != null)
						p2 = id2.getPosition();
					return p1.compareTo(p2);
				} else {
					String a = o1.getParentSample().getParentCondition().getParentSubstance().getName();
					String b = o2.getParentSample().getParentCondition().getParentSubstance().getName();
					int r1 = a.compareTo(b);
					return r1;
				}
			}
		});
		return ml;
	}
	
	public static void getExperimentData(
			ExperimentHeaderInterface header,
			MongoDB m,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			RunnableWithMappingData resultReceiver) throws Exception {
		ExperimentInterface experiment = null;
		if (header.getDatabaseId() != null
				&& header.getDatabaseId().startsWith("lt:"))
			experiment = new de.ipk.ag_ba.postgresql.LTdataExchange().getExperiment(header,
					false,
					status);
		else
			if (header.getDatabaseId() != null
					&& header.getDatabaseId().startsWith("hsm:"))
				experiment = HSMfolderTargetDataManager.getExperiment(header,
						status);
			else
				experiment = m.getExperiment(header, true, status);
		if (experiment != null)
			experiment.setHeader(header);
		
		resultReceiver.setExperimenData(experiment);
	}
	
	public static void monitorExperimentDataProgress() throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		ResourceIOManager.registerIOHandler(new LTftpHandler());
		System.out.println(SystemAnalysis.getCurrentTime() + ">START OBSERVING EXPERIMENT PROGRESS...");
		IAPmail m = new IAPmail();
		String host = SystemAnalysisExt.getHostName();
		host = host.substring(0, host.indexOf("_"));
		String experimentListFileName = ReleaseInfo.getAppSubdirFolderWithFinalSep("watch") + "experiments.txt";
		if (!(new File(experimentListFileName).exists())) {
			TextFile c = new TextFile();
			c.add("# config format: experiment measurement-label, start weighting (h:mm), end weighting (h:mm),");
			c.add("# start weighting 2 (h:mm, or 0:00), end weighting 2 (h:mm, or 0:00), delay in minutes,email1:email2:email3:...");
			c.add("# example config: 1116BA, 8:00, 12:00, 0:00, 0:00, 30,klukas@ipk-gatersleben.de  -- check 1116BA every 30 minutes from 8 to 12 for watering data within the last 30 minutes");
			c.add("# Remark: If the email-address klukas@ipk-gatersleben.de is missing, it is automatically included in the send-command.");
			// c.add("# example config: 1116BA, auto, 10,30, klukas@ipk-gatersleben.de   -- check 1116BA every 30 minutes for watering data within the last 30 minutes, ignoring known start and stop times (with up to 10 minutes difference) from previous day");
			// add all experiments from today or yesterday as default entries to file
			LTdataExchange lde = new LTdataExchange();
			ArrayList<ExperimentHeaderInterface> el = new ArrayList<ExperimentHeaderInterface>();
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SCAN DB CONTENT...");
			for (String database : new LTdataExchange().getDatabases()) {
				try {
					el.addAll(lde.getExperimentsInDatabase(null, database));
				} catch (Exception e) {
					if (!e.getMessage().contains("relation \"snapshot\" does not exist"))
						System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Can't process DB " + database + ": " + e.getMessage());
				}
			}
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">CHECK PROGRESS...");
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Hint 1: To input multiple addresses, split them with ':', don't add spaces.");
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Hint 2: The email-address of the developer (klukas@...) is automatically included.");
			Object[] inp = MyInputHelper.getInput("Please enter the desired mail-addresses:", "Target Mail", new Object[] {
					"Mail 1:Mail 2", "klukas@ipk-gatersleben.de"
			});
			if (inp == null)
				return;
			el = ExperimentHeaderService.filterNewest(el, true);
			for (ExperimentHeaderInterface ehi : el)
				if (ehi.getImportdate() != null)
					if (System.currentTimeMillis() - ehi.getImportdate().getTime() < 24 * 60 * 60 * 1000)
						if (ehi.getExperimentName() != null) {
							String
							// s = ehi.getDatabase() + "," + ehi.getExperimentName() + ",auto,10,30" + (String) inp[0];
							s = ehi.getDatabase() + "," + ehi.getExperimentName() + ",0:00,23:59,0:00,23:59,30," + (String) inp[0];
							c.add(s);
							System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">ADD WATCH ENTRY: " + s);
						}
			c.write(experimentListFileName);
		} else
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">READ CONFIG FILE " + experimentListFileName + "...");
		HashSet<String> outOfDateExperiments = new HashSet<String>();
		
		{
			// init some settings, will be retrieved later once an mail is sent
			@SuppressWarnings("unused")
			boolean takeScreenShot = SystemOptions.getInstance().getBoolean("Watch-Service", "Include Screenshot in e-Mail", true);
			@SuppressWarnings("unused")
			int prefixLength = SystemOptions.getInstance().getInteger("Watch-Service", "DB2WebCam//LT-DB Prefix Length", 3);
		}
		
		long startTime = System.currentTimeMillis();
		
		boolean containsAutoTimingSetting = false;
		HashMap<IOurl, Long> cam2lastSnapshot = new HashMap<IOurl, Long>();
		HashMap<IOurl, String> cam2mimeType = new HashMap<IOurl, String>();
		HashMap<String, BitSet> experimentId2minutesWithDataFromLastDay = new HashMap<String, BitSet>();
		int autoTimeingLastInfoDay = -1;
		while (true) {
			cam2lastSnapshot = initWebCamURLlist(cam2lastSnapshot, cam2mimeType);
			
			if (SystemOptions.getInstance().getBoolean("Watch-Service", "IP cameras//Store Webcam Images", false)) {
				storeImages(cam2lastSnapshot, cam2mimeType);
			}
			boolean saveDesktopSnapshot = SystemOptions.getInstance().getBoolean("Watch-Service", "Screenshot//Publish Desktop", true);
			if (saveDesktopSnapshot)
				storeDesktopImage(false);
			
			ArrayList<WatchConfig> configList = new ArrayList<WatchConfig>();
			TextFile config = new TextFile(experimentListFileName);
			for (String c : config)
				if (!c.isEmpty() && !c.startsWith("#"))
					configList.add(new WatchConfig(c));
			
			int smallestTimeFrame = Integer.MAX_VALUE;
			for (WatchConfig wc : configList)
				if (wc.getLastMinutes() < smallestTimeFrame)
					smallestTimeFrame = wc.getLastMinutes();
			boolean foundSomeError = false;
			ArrayList<String> errorMessages = new ArrayList<String>();
			if (smallestTimeFrame == Integer.MAX_VALUE) {
				try {
					AttributeHelper.showInFileBrowser(ReleaseInfo.getAppSubdirFolder("watch"), "watch.txt");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				LTdataExchange lde = new LTdataExchange();
				ArrayList<ExperimentHeaderInterface> el = new ArrayList<ExperimentHeaderInterface>();
				TreeSet<String> validDatabases = new TreeSet<String>();
				boolean checkAll = false;
				for (WatchConfig wc : configList) {
					if (wc.getDatabase().length() > 0)
						validDatabases.add(wc.getDatabase());
					else
						checkAll = true;
				}
				ArrayList<String> dbs_toBeChecked = new ArrayList<String>();
				if (checkAll)
					dbs_toBeChecked.addAll(lde.getDatabases());
				else
					dbs_toBeChecked.addAll(validDatabases);
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SCAN DB CONTENT...");
				for (String database : dbs_toBeChecked) {
					try {
						System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SCAN DB " + database + "...");
						el.addAll(lde.getExperimentsInDatabase(null, database));
					} catch (Exception e) {
						if (!e.getMessage().contains("relation \"snapshot\" does not exist"))
							System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Cant process DB " + database + ": " + e.getMessage());
					}
				}
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">CHECK PROGRESS...");
				el = ExperimentHeaderService.filterNewest(el, true);
				for (ExperimentHeaderInterface ehi : el) {
					for (WatchConfig wc : configList) {
						if (ehi.getExperimentName() != null && ehi.getExperimentName().equals(wc.getExperimentName())) {
							if (wc.grace > 0) {
								GregorianCalendar gc = new GregorianCalendar();
								int currentDay = gc.get(GregorianCalendar.DAY_OF_YEAR);
								if (containsAutoTimingSetting) {
									if (autoTimeingLastInfoDay != currentDay) {
										try {
											experimentId2minutesWithDataFromLastDay.put(ehi.getDatabaseId(), new BitSet(24 * 3600));
											ExperimentInterface experimentData = new ExperimentReference(ehi).getData();
											BitSet lastDaysMinutes = experimentId2minutesWithDataFromLastDay.get(ehi.getDatabaseId());
											for (SampleInterface sample : new ExperimentCalculationService(experimentData).getSamplesFromYesterDay()) {
												Sample3D s3d = (Sample3D) sample;
												int sampleMinuteOfDay = ExperimentCalculationService.getMinuteOfDayFromSampleTime(s3d.getTime(),
														s3d.getSampleFineTimeOrRowId());
												for (int offset = -wc.grace; offset <= wc.grace; offset++) {
													lastDaysMinutes.set(sampleMinuteOfDay + offset);
												}
											}
											autoTimeingLastInfoDay = currentDay;
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
							
							String lastUpdateText = (ehi.getImportdate() != null ?
									" (last update " + SystemAnalysis.getCurrentTime(ehi.getImportdate().getTime()) + ")" : " (NO UPDATE TIME)");
							System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">CHECK " + ehi.getExperimentName() +
									lastUpdateText + " (expect data every day from " + wc.h1_st + ":" + ff(wc.minute1_st)
									+ " to " + wc.h1_end + ":" + ff(wc.minute1_end) + " AND "
									+ wc.h2_st + ":" + ff(wc.minute2_st) + " to " + wc.h2_end + ":" + ff(wc.minute2_end) + ")");
							long startTime1 = wc.getStartTimeForToday1();
							long startTime2 = wc.getStartTimeForToday2();
							long endTime1 = wc.getEndTimeForToday1();
							long endTime2 = wc.getEndTimeForToday2();
							Date ddd = ehi.getImportdate();
							if (ddd != null) {
								Date dddt = fixSetToday(ddd);
								boolean m1 = (dddt.getTime() > startTime1 && dddt.getTime() <= endTime1);
								boolean m2 = (dddt.getTime() > startTime2 && dddt.getTime() <= endTime2);
								boolean m3 = (new Date().getTime() - ddd.getTime()) > wc.getLastMinutes() * 60 * 1000;
								String imageSrc1 = null;
								String fileName1 = null;
								String contentType1 = "image/jpeg";
								String imageSrc2 = null;
								String fileName2 = null;
								String contentType2 = "image/jpeg";
								int prefixLength = SystemOptions.getInstance().getInteger("Watch-Service",
										"DB2WebCam//LT-DB Prefix Length", 3);
								if (ehi.getDatabase() != null && ehi.getDatabase().length() >= prefixLength) {
									String prefix = ehi.getDatabase().substring(0, prefixLength);
									ArrayList<String> possibleCameraSources = new ArrayList<String>();
									possibleCameraSources.add("[No Source]");
									for (WebCamInfo webcam : IAPservice.getActiveWebCamURLs()) {
										possibleCameraSources.add(webcam.getName());
									}
									if (possibleCameraSources.size() > 1) {
										String cameraSource = SystemOptions.getInstance().getStringRadioSelection("Watch-Service",
												"DB2WebCam//DB " + prefix + " Camera 1",
												possibleCameraSources, possibleCameraSources.get(0), true);
										if (cameraSource != null) {
											for (WebCamInfo webcam : IAPservice.getActiveWebCamURLs()) {
												if (webcam.getName() != null && webcam.getName().equals(cameraSource)) {
													// match
													contentType1 = webcam.getContentType("image/jpeg");
													fileName1 = webcam.getName()
															+ SystemOptions.getInstance().getString("Watch-Service",
																	"DB2WebCam//DB " + prefix + " Camera 1 Attachment Name Postfix",
																	"_[time].jpg");
													if (fileName1 != null && fileName1.indexOf("[time]") >= 0)
														fileName1 = StringManipulationTools.stringReplace(fileName1, "[time]", SystemAnalysis.getCurrentTimeInclSec());
													imageSrc1 = webcam.getUrl();
													break;
												}
											}
										}
									}
									if (possibleCameraSources.size() > 2) {
										String cameraSource = SystemOptions.getInstance().getStringRadioSelection("Watch-Service",
												"DB2WebCam//DB " + prefix + " Camera 2",
												possibleCameraSources, possibleCameraSources.get(0), true);
										if (cameraSource != null) {
											for (WebCamInfo webcam : IAPservice.getActiveWebCamURLs()) {
												if (webcam.getName() != null && webcam.getName().equals(cameraSource)) {
													// match
													contentType2 = webcam.getContentType("image/jpeg");
													fileName2 = webcam.getName()
															+ SystemOptions.getInstance().getString("Watch-Service",
																	"DB2WebCam//DB " + prefix + " Camera 2 Attachment Name Postfix",
																	"_[time].jpg");
													if (fileName2 != null && fileName2.indexOf("[time]") >= 0) {
														Thread.sleep(1000);
														fileName2 = StringManipulationTools.stringReplace(fileName2, "[time]", SystemAnalysis.getCurrentTimeInclSec());
													}
													imageSrc2 = webcam.getUrl();
													break;
												}
											}
										}
									}
								}
								
								if ((m1 || m2) && m3) {
									// WARN
									foundSomeError = true;
									errorMessages.add(SystemAnalysis.getCurrentTimeInclSec() + ">NO CURRENT DATA FOR EXPERIMENT " + ehi.getExperimentName()
											+ ", LAST DATA FROM " + SystemAnalysis.getCurrentTime(ehi.getImportdate().getTime()));
									if (!outOfDateExperiments.contains(ehi.getExperimentName())) {
										System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SEND WARNING MAIL FOR EXPERIMENT "
												+ ehi.getExperimentName()
												+ " TO " + wc.getMails());
										int minMin = SystemOptions.getInstance().getInteger("Watch-Service", "Startup-Email-Delay-min", 15);
										if (System.currentTimeMillis() - startTime < minMin * 60 * 1000)
											System.out.println(SystemAnalysis.getCurrentTime() + ">WITHIN THE FIRST " + minMin + " MINUTES OF START NO MAIL WILL BE SEND");
										else
											m.sendEmail(
													wc.getMails(),
													"INFO: " +
															ehi.getExperimentName()
															+ " STATUS CHANGE",
													"WARNING: " + ehi.getExperimentName()
															+ " shows no further progress " + lastUpdateText + " // " + SystemAnalysis.getUserName() + "@"
															+ host + "\n\n" +
															"No new data found for experiment " + ehi.getExperimentName()
															+ ".\n\n\nExperiment details:\n\n" +
															StringManipulationTools.stringReplace(ehi.toStringLines(), "<br>", "\n"),
													imageSrc1, fileName1, contentType1, imageSrc2, fileName2, contentType2, ehi);
									}
									outOfDateExperiments.add(ehi.getExperimentName());
								} else {
									// all OK
									if (outOfDateExperiments.contains(ehi.getExperimentName())) {
										int minMin = SystemOptions.getInstance().getInteger("Watch-Service", "Startup-Email-Delay-min", 15);
										System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SEND INFO MAIL FOR EXPERIMENT " + ehi.getExperimentName()
												+ " TO " + wc.getMails());
										if (System.currentTimeMillis() - startTime < minMin * 60 * 1000)
											System.out.println(SystemAnalysis.getCurrentTime() + ">WITHIN THE FIRST " + minMin + " MINUTES OF START NO MAIL WILL BE SEND");
										else
											m.sendEmail(
													wc.getMails(),
													"INFO: " + ehi.getExperimentName()
															+ " STATUS CHANGE",
													"INFO: " + ehi.getExperimentName()
															+ " shows progress again " + lastUpdateText + " // " + SystemAnalysis.getUserName() + "@"
															+ host + "\n\n" +
															"New data has been found for experiment " + ehi.getExperimentName()
															+ ". Status is now OK!\n\n\nExperiment details:\n\n" +
															StringManipulationTools.stringReplace(ehi.toStringLines(), "<br>", "\n"),
													imageSrc1, fileName1, contentType1, imageSrc2, fileName2, contentType2, ehi);
										
									}
									outOfDateExperiments.remove(ehi.getExperimentName());
								}
							}
						}
					}
				}
			}
			smallestTimeFrame = 10;
			
			if (smallestTimeFrame > 24 * 60) {
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">UPDATE TIME UNDEFINED, QUITTING");
				return;
			}
			
			if (!foundSomeError) {
				System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SLEEP " + smallestTimeFrame + " minutes... (ALL OK)");
				Thread.sleep(smallestTimeFrame * 60 * 1000);
			} else {
				int wait = smallestTimeFrame;
				if (wait < 1)
					wait = 1;
				for (String e : errorMessages)
					System.out.println(e);
				System.out.println(SystemAnalysis.getCurrentTimeInclSec()
						+ ">SLEEP " + wait + " minutes... (WILL CHECK FOR RECOVERY)");
				Thread.sleep(wait * 60 * 1000);
			}
			System.out.println(SystemAnalysis.getCurrentTime() + ">SLEEP FINISHED");
			@SuppressWarnings("deprecation")
			int seconds = new Date().getSeconds();
			int sec = 60 - seconds % 60;
			if (sec > 0) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">NEED TO WAIT ANOTHER " + sec + " SECONDS, TO BE ON THE WHOLE MINUTE");
				Thread.sleep(sec * 1000);
			}
			@SuppressWarnings("deprecation")
			int minutes = new Date().getMinutes();
			int wait = 15 - minutes % 15;
			if (wait > 0) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">NEED TO WAIT ANOTHER " + wait + " MINUTES, TO BE ON PROPER TIME SCHEDULE (0,15,30,45)");
				Thread.sleep(wait * 60 * 1000);
			}
		}
	}
	
	private static String[] templateWebCamTitles = new String[] {
			"Barley Cam",
			"Maize Cam",
			"Phytochamber Cam 1",
			"Phytochamber Cam 2"
	};
	
	private static String[] templateWebCamtURLs = new String[] {
			"user:pass@http://lemnacam.ipk-gatersleben.de/jpg/image.jpg?timestamp=[time]",
			"http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity",
			"http://ba-16.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity",
			"http://ba-17.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity"
	};
	
	protected static HashMap<IOurl, Long> initWebCamURLlist(HashMap<IOurl, Long> cam2lastSnapshot, HashMap<IOurl, String> cam2fileExt) {
		ArrayList<WebCamInfo> urls = getActiveWebCamURLs();
		HashMap<IOurl, Long> newCam2lastSnapshot = new HashMap<IOurl, Long>(urls.size());
		for (WebCamInfo wi : urls) {
			String u = wi.getUrl();
			IOurl ur = new IOurl(u);
			if (cam2lastSnapshot.containsKey(new IOurl(u)))
				newCam2lastSnapshot.put(ur, cam2lastSnapshot.get(new IOurl(u)));
			else
				newCam2lastSnapshot.put(ur, 0l);
			cam2fileExt.put(ur, wi.getContentType("image/jpeg"));
		}
		cam2lastSnapshot = newCam2lastSnapshot;
		return cam2lastSnapshot;
	}
	
	public static ArrayList<WebCamInfo> getActiveWebCamURLs() {
		ArrayList<WebCamInfo> urls = new ArrayList<WebCamInfo>();
		for (int idx = 0; idx < SystemOptions.getInstance().getInteger("Watch-Service", "IP cameras//N", templateWebCamtURLs.length); idx++) {
			String n = SystemOptions.getInstance().getString("Watch-Service",
					"IP cameras//Title-" + (idx + 1), templateWebCamTitles[idx]);
			String u = SystemOptions.getInstance().getString("Watch-Service",
					"IP cameras//URL-" + (idx + 1), templateWebCamtURLs[idx]);
			String ct = SystemOptions.getInstance().getString("Watch-Service",
					"IP cameras//Content Type-" + (idx + 1), "image/jpg");
			boolean e = SystemOptions.getInstance().getBoolean("Watch-Service",
					"IP cameras//Webcam " + (idx + 1) + " enabled", false);
			if (e && n != null && !n.isEmpty() && u != null && !u.isEmpty())
				urls.add(new WebCamInfo(u, n, ct));
		}
		return urls;
	}
	
	private synchronized static void storeImages(final HashMap<IOurl, Long> cam2lastSnapshot, final HashMap<IOurl, String> cam2mimeType) {
		long t = System.currentTimeMillis();
		for (final IOurl cam : cam2lastSnapshot.keySet()) {
			try {
				// at most every 5 minutes
				if (t - cam2lastSnapshot.get(cam) >= 1000 * 60 * 5) {
					final InputStream inp = cam.getInputStream();
					MongoDB mm = MongoDB.getDefaultCloud();
					mm.processDB(new RunnableOnDB() {
						private DB db;
						
						@Override
						public void run() {
							String mime = cam2mimeType.get(cam);
							String ext = ".jpg";
							if (mime.toUpperCase().contains("PNG"))
								ext = ".png";
							if (mime.toUpperCase().contains("TIF"))
								ext = ".tif";
							long ms = System.currentTimeMillis();
							GridFS gridfs_webcam_files = new GridFS(db, "fs_webcam_" + cam.toString());
							GridFSInputFile inputFile = gridfs_webcam_files.createFile(inp, StringManipulationTools.formatNumberAddZeroInFront(ms, 19) + ext);
							inputFile.setMetaData(new BasicDBObject("time", ms));
							inputFile.save();
							System.out.println(SystemAnalysis.getCurrentTime() + ">SAVED WEBCAM SNAPSHOT FROM " + cam + " IN DB " + db.getName());
							cam2lastSnapshot.put(cam, System.currentTimeMillis());
						}
						
						@Override
						public void setDB(DB db) {
							this.db = db;
						}
					});
				}
			} catch (Exception e) {
				System.err.println(SystemAnalysis.getCurrentTime() + ">COULD NOT SAVE VIDEO SNAPSHOT: " + e.getMessage());
			}
		}
	}
	
	private static long lastScreenshotSaving = 0;
	
	public synchronized static void storeDesktopImage(boolean immediately) {
		long t = System.currentTimeMillis();
		
		try {
			int interval =
					SystemOptions.getInstance().getInteger("Watch-Service", "Screenshot//Screenshot-Intervall_sec", 60);
			if (interval < 1) {
				SystemOptions.getInstance().setInteger("Watch-Service", "Screenshot//Screenshot-Intervall_sec", 1);
				interval = 1;
			}
			if (!GraphicsEnvironment.isHeadless() && (t - lastScreenshotSaving >= interval * 1000 || immediately)) {
				final Screenshot screenshot = SystemAnalysis.getScreenshot();
				MongoDB mm = MongoDB.getDefaultCloud();
				mm.processDB(new RunnableOnDB() {
					private DB db;
					
					@Override
					public void run() {
						GridFS gridfs_webcam_files = new GridFS(db, "fs_screenshots");
						List<GridFSDBFile> oldFiles = gridfs_webcam_files.find(screenshot.getScreenshotStaticFileName());
						GridFSInputFile inputFile = gridfs_webcam_files.createFile(screenshot.getScreenshotImage(), screenshot.getScreenshotStaticFileName());
						inputFile.setMetaData(new BasicDBObject("time", screenshot.getTime()));
						inputFile.setMetaData(new BasicDBObject("host", SystemAnalysisExt.getHostNameNoError()));
						inputFile.save();
						for (GridFSDBFile oldFile : oldFiles)
							gridfs_webcam_files.remove(oldFile);
						System.out.println(SystemAnalysis.getCurrentTime() + ">SAVED DESKTOP SNAPSHOT IN DB " + db.getName());
						lastScreenshotSaving = System.currentTimeMillis();
					}
					
					@Override
					public void setDB(DB db) {
						this.db = db;
					}
				});
			}
		} catch (Exception e) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">COULD NOT SAVE CURRENT DESKTOP SCREENSHOT: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	private static Date fixSetToday(Date ddd) {
		int h = ddd.getHours();
		int m = ddd.getMinutes();
		ddd = new Date();
		ddd.setHours(h);
		ddd.setMinutes(m);
		return ddd;
	}
	
	private static String ff(int t) {
		return StringManipulationTools.formatNumber(t, "00");
	}
	
	/**
	 * @param r
	 *           0..255
	 * @param g
	 *           0..255
	 * @param b
	 *           0..255
	 * @return Intensity (temperature) in the range of 0..1
	 */
	public static double getIRintenstityFromRGB(int r, int g, int b) {
		int i = getIntIRintensity(r, g, b);
		return i / (8d * 255d);
	}
	
	private static int getIntIRintensity(int r, int g, int b) {
		if (r == 0 && g == 0)
			return b;
		if (r == 0 && b == 255)
			return 255 + g;
		if (r == 0 && g == 255)
			return 3 * 255 - b;
		if (g == 255 && b == 0)
			return 3 * 255 + r;
		if (r == 255 && b == 0)
			return 5 * 255 - g;
		if (r == 255 && g == 0)
			return 5 * 255 + b;
		if (r == 255 && b == 255)
			return 7 * 255 - g;
		if (r == 255 && g == 255)
			return 7 * 255 + b;
		throw new UnsupportedOperationException("Invalid RGB values for intensity conversion (" + r + "/" + g + "/" + b + ")");
	}
	
	public static double getIRintenstityFromRGB(int c, int back) {
		if (c == back)
			return Double.NaN;
		else {
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);
			return getIRintenstityFromRGB(r, g, b);
		}
	}
	
	public static int getIRintensityDifferenceColor(double d, int back, double scale) {
		if (Double.isNaN(d))
			return back;
		else {
			if (d < 0) {
				int gray = (int) Math.round(255 - (-d * 255) * scale);
				if (gray < 0) {
					// System.err.println("Too high temp difference (multiplier for visualization is too high): " + gray);
					return back;
					// gray = 0;
				}
				return new Color(gray, gray, gray).getRGB();
			} else {
				return back;
				// int red = (int) Math.round(d * 255);
				// red = 255 - red;
				// return new Color(255, 255, 255).getRGB();
			}
		}
	}
	
	public static String getNiceNameForPhenotypicProperty(String substanceName) {
		return niceNames.get(substanceName);
	}
	
	public static double MathPow(double v, double ot) {
		// if (v < 0 || v > 1.1) {
		// return Math.pow(v, ot);
		// } else
		return cubeRoots[(int) (1000 * v)];
	}
	
	public static boolean isAnalyzedWithCurrentRelease(ExperimentHeaderInterface exp) {
		// for (IAP_RELEASE ir : IAP_RELEASE.values())
		// if (exp.getRemark().contains(ir.toString()))
		// return true;
		// return false;
		return true;
	}
	
	public static void showImageJ() {
		if (SystemAnalysis.isHeadless())
			return;
		if (IAPservice.ij != null && !IAPservice.ij.isShowing())
			closeAllImageJimageWindows();
		if (IAPservice.ij != null && IAPservice.ij.isShowing())
			IAPservice.ij.toFront();
		if (IAPservice.ij == null || !IAPservice.ij.isShowing())
			IAPservice.ij = new ImageJ();
	}
	
	private static ImageJ ij = null;
	
	public static void autoCloseAt(final int hour) {
		Thread autoClose = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// wait 5 minutes, this wait prevents that the app is closed immediately upon restart
					Thread.sleep(5 * 60 * 1000);
					// start monitoring
					int timeHour, timeMinute;
					do {
						Thread.sleep(5 * 1000); // wait 5 seconds
						timeHour = SystemAnalysis.getCurrentTimeHour();
						timeMinute = SystemAnalysis.getCurrentTimeMinute();
					} while (!(timeHour == hour && timeMinute == 0));
					MongoDB.saveSystemMessage("TIMED AUTO-CLOSE (host " + SystemAnalysisExt.getHostNameNiceNoError() +
							"): SYSTEM.EXIT(0) // current time: " + SystemAnalysis.getCurrentTimeInclSec());
					System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">TIMED AUTO-CLOSE: SYSTEM.EXIT(0)");
					System.exit(0);
				} catch (InterruptedException e) {
					// empty
				}
			}
		});
		autoClose.setName("Auto close at " + hour + ":00");
		autoClose.start();
	}
	
	public MassCopySupport getMassCopySupport() {
		return MassCopySupport.getInstance();
	}
	
	public static java.awt.Image getImage(Object ref, String name) throws Exception {
		final MyByteArrayInputStream in = ResourceIOManager.getInputStreamMemoryCached(
				GravistoService.getIOurl(ref.getClass(), name, null).getInputStream());
		SeekableStream ss = SeekableStream.wrapInputStream(in, true);
		RenderedOp ro = JAI.create("stream", ss);
		ColorModel cm = ro.getColorModel();
		if (true || (cm.getNumColorComponents() == 1 && cm.getPixelSize() == 8)) {
			// convert 8-bit gray scale image to RGB
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Image " + name + " is interpreted as a gray-scale image and specially converted.");
			BufferedImage bi = ro.getAsBufferedImage();
			BufferedImage rgbImg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = rgbImg.createGraphics();
			g2d.drawImage(bi, 0, 0, null);
			g2d.dispose();
			return rgbImg;
			// de.ipk.ag_ba.image.structures.Image gray = new de.ipk.ag_ba.image.structures.Image(ro.getAsBufferedImage());
		}
		return ro.getAsBufferedImage();
	}
	
	@SuppressWarnings("rawtypes")
	public static java.awt.Image getImage(Class ref, String name) {
		URL url = GravistoService.getResource(ref, name);
		if (url == null)
			return null;
		else {
			return new ImageIcon(url).getImage();
		}
	}
	
	public static IOurl getURLfromWeblocFile(IOurl referenceURL) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(referenceURL.getInputStream()));
		String result = null;
		String str;
		while ((str = in.readLine()) != null) {
			System.out.println(str);
			if (str.startsWith("URL=")) {
				result = str.substring("URL=".length());
				break;
			}
			if (str.contains("<string>")) {
				result = StringManipulationTools.removeHTMLtags(str).trim();
				break;
			}
		}
		in.close();
		return new IOurl(result);
	}
	
	/**
	 * @return for example: 'png' or 'jpg'
	 */
	public static String getTargetFileExtension(boolean isIconStorage, String sourceFileExtension) {
		if (sourceFileExtension != null && sourceFileExtension.startsWith("."))
			sourceFileExtension = sourceFileExtension.substring(".".length());
		String targetFileExtension = isIconStorage ?
				SystemOptions.getInstance().getString("IAP", "Preview File Type", "png") :
				SystemOptions.getInstance().getString("IAP", "Result File Type", "png");
		if (sourceFileExtension != null && (sourceFileExtension.endsWith("jpg") || sourceFileExtension.endsWith("jpeg")))
			if (targetFileExtension != null && (targetFileExtension.endsWith("png") || targetFileExtension.endsWith("png")))
				targetFileExtension = sourceFileExtension; // don't convert source JPG to PNG, makes not really sense
		return targetFileExtension;
	}
	
	public static MeasurementFilter getMeasurementFilter(ExperimentHeaderInterface header) {
		MeasurementFilter mf = new MySnapshotFilter(new ArrayList<ThreadSafeOptions>(), header.getGlobalOutlierInfo());
		return mf;
	}
	
	/**
	 * @param experiment
	 *           Experiment is modified.
	 * @return number of removed measurements (numeric or binary)
	 */
	public static int removeOutliers(ExperimentInterface experiment) {
		ArrayList<NumericMeasurementInterface> outlierList = new ArrayList<NumericMeasurementInterface>();
		HashMap<SampleInterface, Integer> samplesWithOutliers2valueCnt = new HashMap<SampleInterface, Integer>();
		
		MeasurementFilter mf = IAPservice.getMeasurementFilter(experiment.getHeader());
		for (SubstanceInterface s : experiment) {
			for (ConditionInterface c : s) {
				for (SampleInterface si : c) {
					boolean isOutlier = false;
					for (NumericMeasurementInterface n : si) {
						if (mf.isGlobalOutlierOrSpecificOutlier(n)) {
							isOutlier = true;
							outlierList.add(n);
						}
					}
					if (isOutlier)
						samplesWithOutliers2valueCnt.put(si, si.size());
				}
			}
		}
		for (NumericMeasurementInterface o : outlierList) {
			o.getParentSample().remove(o);
		}
		for (SampleInterface s : samplesWithOutliers2valueCnt.keySet()) {
			s.recalculateSampleAverage(false);
			if (s.size() == 0 && samplesWithOutliers2valueCnt.get(s) > 0)
				s.getParentCondition().remove(s);
		}
		
		int removed = outlierList.size();
		if (removed > 0) {
			String remark = experiment.getHeader().getRemark();
			if (remark == null)
				remark = "";
			
			if (!remark.trim().isEmpty())
				remark = remark + " // ";
			
			remark = remark + "removed " + removed + " outlier measurements (" + SystemAnalysis.getUserName() + ", " + SystemAnalysis.getCurrentTime() + ")";
			
			experiment.getHeader().setRemark(remark);
		}
		return removed;
	}
	
	public static void showImages(final ArrayList<ImageData> toBeLoaded) {
		if (toBeLoaded == null || toBeLoaded.size() == 0)
			return;
		final ImageStack is = new ImageStack();
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		Runnable backgroundTask1 = new Runnable() {
			
			@Override
			public void run() {
				int err = 0;
				for (int i = 0; i < toBeLoaded.size(); i++) {
					status.setCurrentStatusText1("Load image " + (i + 1) + "/" + toBeLoaded.size());
					ImageData id = toBeLoaded.get(i);
					Image fi;
					try {
						fi = new Image(id.getURL());
						is.addImage(id.getQualityAnnotation() + " / " + id.getSubstanceName() + " / " + id.getParentSample().getTimeUnit() + " "
								+ id.getParentSample().getTime(), fi);
					} catch (Exception e) {
						e.printStackTrace();
						err++;
					}
					if (err > 0)
						status.setCurrentStatusText2(err + " errors");
					status.setCurrentStatusValueFine(100d * (i + 1) / toBeLoaded.size());
				}
				status.setCurrentStatusText1("Processing completed, loaded " + toBeLoaded.size() + " images");
				status.setCurrentStatusValue(100);
			}
		};
		Runnable finishSwingTask = new Runnable() {
			@Override
			public void run() {
				is.show("Image Stack");
			}
		};
		
		BackgroundTaskHelper.issueSimpleTaskInWindow("Load Image List", "Initialize", backgroundTask1, finishSwingTask, status, false, true);
	}
	
	public static boolean getIsAnnotationSavePossible(ExperimentReference experiment) {
		if (experiment != null && experiment.m != null)
			return true;
		String dbId = experiment != null && experiment.getHeader() != null ? experiment.getHeader().getDatabaseId() : null;
		if (dbId != null) {
			String id = dbId.contains(":") ? dbId.substring(0, dbId.indexOf(":")) : null;
			if (id != null && !id.isEmpty()) {
				ResourceIOHandler vfs = ResourceIOManager.getHandlerFromPrefix(id);
				if (vfs instanceof VirtualFileSystemHandler) {
					VirtualFileSystemHandler vv = (VirtualFileSystemHandler) vfs;
					if (vv.getVFS() instanceof VirtualFileSystemVFS2) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isImageJvisible() {
		return IAPservice.ij != null && IAPservice.ij.isShowing();
	}
	
	public static int getIAPimageWindowCount() {
		if (isImageJvisible())
			return WindowManager.getImageCount();
		else {
			int n = 0;
			if (IAPservice.ij != null && !IAPservice.ij.isShowing())
				if (IAPservice.ij.getWindows() != null)
					for (Window w : IAPservice.ij.getWindows())
						if (w instanceof ImageWindow)
							// if (!((((ImageWindow) w).getState() & Frame.ICONIFIED) == Frame.ICONIFIED))
							if (!((ImageWindow) w).isClosed())
								n++;
			return n;
		}
	}
	
	public static void closeAllImageJimageWindows() {
		if (IAPservice.isImageJvisible()) {
			WindowManager.closeAllWindows();
		} else
			if (IAPservice.ij != null && !IAPservice.ij.isShowing())
				if (IAPservice.ij.getWindows() != null)
					for (Window w : IAPservice.ij.getWindows())
						if (w instanceof ImageWindow)
							// if (!((((ImageWindow) w).getState() & Frame.ICONIFIED) == Frame.ICONIFIED))
							w.dispose();
	}
	
	public static int[] add(int[] arr, int add) {
		int[] res = new int[arr.length];
		for (int i = 0; i < res.length; i++)
			res[i] = arr[i] + add;
		return res;
	}
}