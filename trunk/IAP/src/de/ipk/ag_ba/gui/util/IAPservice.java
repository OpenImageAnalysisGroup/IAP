/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.util;

import ij.ImageJ;
import info.StopWatch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.Vector2d;
import org.graffiti.editor.ConfigureViewAction;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.session.EditorSession;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

import de.ipk.ag_ba.commands.AbstractGraphUrlNavigationAction;
import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.commands.SnapshotFilter;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentCalculationService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
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
		NavigationButton ne = new NavigationButton(new AbstractGraphUrlNavigationAction("Load web-folder content") {
			private NavigationButton src = null;
			private final ObjectRef graphRef = new ObjectRef();
			private final ObjectRef scrollpaneRef = new ObjectRef();
			
			@Override
			public String getURL() {
				return mmc.getURL().toString();
			}
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;
				
				IOurl url;
				try {
					url = mmc.getURL();
					final Graph g = MainFrame.getGraph(url, url.getFileName());
					graphRef.setObject(g);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				res.add(src);
				return res;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
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
						res.add(src);
						return res;
					}
					
					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {
						return new ArrayList<NavigationButton>();
					}
					
					@Override
					public MainPanelComponent getResultMainPanel() {
						JComponent gui = IAPmain.showVANTED(true);
						if (gui != null)
							gui.setBorder(BorderFactory.createLoweredBevelBorder());
						if (g != null) {
							MainFrame i = MainFrame.getInstance();
							if (i != null)
								i.showGraph(g, null, LoadSetting.VIEW_CHOOSER_NEVER);
						}
						return gui != null ? new MainPanelComponent(gui) : null;
					}
					
					@Override
					public boolean getProvidesActions() {
						return false;
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
			
		}, mmc.toString(), "img/graphfile.png", guiSettings);
		
		return ne;
	}
	
	public static ArrayList<String> portScan(String hostname, BackgroundTaskStatusProviderSupportingExternalCall status) {
		ArrayList<String> res = new ArrayList<String>();
		
		int port = 0;
		
		for (port = 0; port < 65536; port++) {
			try {
				Socket s = new Socket();
				s.connect(new InetSocketAddress(hostname, port), 100);
				res.add("Open port: " + port);
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
	
	public static boolean getSetting(IAPoptions groupByCoordinator) {
		switch (groupByCoordinator) {
			case GROUP_BY_COORDINATOR:
				return true;
			case GROUP_BY_EXPERIMENT_TYPE:
				return true;
		}
		return false;
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
	
	public static ArrayList<NumericMeasurementInterface> getMatchFor(IOurl url, ExperimentInterface experiment) {
		
		ArrayList<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		
		String search = url.toString();
		String searchKey = null;
		Collection<NumericMeasurementInterface> ml = Substance3D.getAllMeasurements(experiment);
		for (NumericMeasurementInterface md : ml) {
			if (md instanceof ImageData) {
				ImageData id = (ImageData) md;
				String u = id.getURL().getDetail() + "";
				if (search.contains(u)) {
					String key = id.getParentSample().getFullId() + ";" + id.getReplicateID() + ";" + id.getPosition();
					String name = id.getParentSample().getParentCondition().getParentSubstance().getName();
					if (name.contains("."))
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
					String key = id.getParentSample().getFullId() + ";" + id.getReplicateID() + ";" + id.getPosition();
					String name = id.getParentSample().getParentCondition().getParentSubstance().getName();
					if (name.contains("."))
						key += name.substring(name.lastIndexOf("."));
					if (searchKey.equalsIgnoreCase(key)) {
						result.add(id);
					}
				}
			}
		}
		
		return result;
	}
	
	public static Collection<NumericMeasurementInterface> getMatchForReference(IOurl fileNameMain, ExperimentInterface experiment, MongoDB m) {
		Collection<NumericMeasurementInterface> pairs = getMatchFor(fileNameMain, experiment);
		
		Collection<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		
		for (NumericMeasurementInterface nmi : pairs) {
			ImageData id = (ImageData) nmi;
			if (id.getLabelURL() != null) {
				ImageData idMod = (ImageData) id.clone(id.getParentSample());
				idMod.setURL(idMod.getLabelURL());
				String annotation = idMod.getAnnotation();
				if (annotation != null && annotation.length() > 0 && id.getAnnotationField("oldreference") != null) {
					idMod.setLabelURL(new IOurl(idMod.getAnnotationField("oldreference")));
					result.add(idMod);
				}
			}
		}
		
		return result;
	}
	
	public final static float[] cubeRoots = getCubeRoots(0f, 1.1f, 1100);
	private static Boolean mainMongoDBreachable = null;
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
		res.put("weight_before (g)", "Weight before watering");
		res.put("water_weight (g)", "Water weight");
		res.put("side.height.norm (mm)", "Height (normalized)");
		res.put("side.height (px)", "Height");
		res.put("side.area.norm (mm^2)", "Side Area (normalized)");
		res.put("side.area (px)", "Side Area");
		res.put("side.fluo.intensity.average (relative)", "Fluo intensity (side)");
		res.put("side.nir.intensity.average (relative)", "NIR intensity (side)");
		res.put("top.fluo.intensity.average (relative)", "Fluo intensity (top)");
		res.put("top.nir.intensity.average (relative)", "NIR intensity (top)");
		res.put("side.vis.hue.average (relative)", "Average hue (side)");
		res.put("top.vis.hue.average (relative)", "Average hue (top)");
		res.put("side.width.norm (mm)", "Width (normalized)");
		res.put("side.width (px)", "Width");
		res.put("top.area.norm", "Top area (normalized)");
		res.put("top.area (px)", "Top area");
		res.put("volume.fluo.iap (px^3)", "Digital biomass (fluo)");
		return res;
	}
	
	public static boolean isMongoReachable() {
		if (mainMongoDBreachable == null)
			mainMongoDBreachable = isReachable(MongoDB.getDefaultCloudHostName());
		return mainMongoDBreachable;
	}
	
	public static ArrayList<SnapshotDataIAP> getSnapshotsFromExperiment(
			UrlCacheManager urlManager,
			ExperimentInterface experiment,
			HashMap<String, Integer> optSubstanceIds,
			boolean prepareTransportToBrowser,
			boolean storeAllAngleValues,
			boolean storeAllReplicates,
			SnapshotFilter optSnapshotFilter) {
		
		System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set...");
		System.out.println("Transport to browser? " + prepareTransportToBrowser);
		System.out.println("Store all angles? " + prepareTransportToBrowser);
		System.out.println("Store all replicates? " + prepareTransportToBrowser);
		
		StopWatch sw = new StopWatch("Create Snapshots");
		
		HashMap<String, SnapshotDataIAP> timestampAndQuality2snapshot = new HashMap<String, SnapshotDataIAP>();
		
		ArrayList<SnapshotDataIAP> result = new ArrayList<SnapshotDataIAP>();
		
		if (experiment != null) {
			for (SubstanceInterface substance : experiment)
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
			Experiment e = (Experiment) experiment;
			e.sortSubstances();
		}
		
		boolean hasTemperatureData = false;
		TreeMap<Long, Double> timeDay2averageTemp = new TreeMap<Long, Double>();
		
		if (experiment != null) {
			double ggd_baseline = 10;
			String type = experiment.getHeader().getExperimentType();
			if (type==null)
				type = "";
			if (type.equals("Barley")) {
				ggd_baseline = 5.5;
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Growing-degree days, using baseline for Barley, 5.5°C");
			}
			if (type.equals("Maize")) {
				ggd_baseline = 10;
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Growing-degree days, using baseline for Maize, 10°C");
			}
			GregorianCalendar gc = new GregorianCalendar();
			for (SubstanceInterface substance : experiment) {
				if (substance.getName() != null && substance.getName().equals("temp.air.avg")) {
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
				experiment = experiment.clone();
				for (SubstanceInterface substance : experiment) {
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
		if (experiment != null)
			for (SubstanceInterface substance : experiment) {
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
							
							boolean addSN = false;
							if (!timestampAndQuality2snapshot.containsKey(snapshotTimeIndex + "//" + qualityFilter)) {
								SnapshotDataIAP ns = new SnapshotDataIAP();
								timestampAndQuality2snapshot.put(snapshotTimeIndex + "//" + qualityFilter, ns);
								addSN = true;
							}
							
							SnapshotDataIAP sn = timestampAndQuality2snapshot.get(snapshotTimeIndex + "//" + qualityFilter);
							
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
								if (optSubstanceIds != null &&
										!sub.equals("water_sum") && !sub.equals("weight_before") && !sub.equals("water_weight")) {
									sub = sample.getSubstanceNameWithUnit();
									synchronized (optSubstanceIds) {
										if (!optSubstanceIds.containsKey(sub)) {
											optSubstanceIds.put(sub, optSubstanceIds.size());
										}
										int idx = optSubstanceIds.get(sub);
										sample.recalculateSampleAverage();
										if (sample.getSampleAverage() != null && qualityFilter == null) {
											double vvv = sample.calcMean();
											sn.storeValue(idx, vvv);
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
											sn.storeValue(idx, sum / n);
										}
										if (storeAllAngleValues) {
											for (NumericMeasurementInterface nmi : sample) {
												NumericMeasurement3D nmi3d = (NumericMeasurement3D) nmi;
												sn.storeAngleValue(idx, nmi3d.getPosition(), nmi3d.getValue());
											}
										}
									}
								} else {
									double mmSum = 0;
									double mmLowest = Double.MAX_VALUE;
									for (NumericMeasurementInterface mmm : sample) {
										if (!isOKquality(qualityFilter, mmm))
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
							
							if (sample instanceof Sample3D) {
								Sample3D s3d = (Sample3D) sample;
								
								Collection<NumericMeasurementInterface> sl = sortImages(s3d.getMeasurements(MeasurementNodeType.IMAGE,
										MeasurementNodeType.VOLUME));
								int imageCount = 0;
								for (NumericMeasurementInterface ii : sl) {
									if (!isOKquality(qualityFilter, ii))
										continue;
									imageCount++;
									if (ii instanceof ImageData) {
										ImageData i = (ImageData) ii;
										String subn = ii.getParentSample().getParentCondition().getParentSubstance().getName();
										ImageConfiguration ic = ImageConfiguration.get(subn);
										long urlId = urlManager != null ? urlManager.getId(i.getURL().toString()) : -1;
										if (ic == ImageConfiguration.Unknown) {
											ic = ImageConfiguration.get(i.getURL().getFileName());
										}
										Integer p = i.getPosition() != null ? i.getPosition().intValue() : null;
										if (p == null)
											p = 0;
										if (ic == ImageConfiguration.Unknown) {
											sn.addUnknown(urlId, p);
										} else {
											if (ic == ImageConfiguration.RgbSide)
												sn.addRgb(urlId, p);
											if (ic == ImageConfiguration.FluoSide)
												sn.addFluo(urlId, p);
											if (ic == ImageConfiguration.NirSide)
												sn.addNir(urlId, p);
											if (ic == ImageConfiguration.IrSide)
												sn.addIr(urlId, p);
											
											if (ic == ImageConfiguration.RgbTop)
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
										long urlId = urlManager.getId(i.getURL().toString());
										if (ic == ImageConfiguration.Unknown) {
											ic = ImageConfiguration.get(i.getURL().getFileName());
										}
										Integer p = i.getPosition() != null ? i.getPosition().intValue() : null;
										if (p == null)
											p = 0;
										if (ic == ImageConfiguration.Unknown) {
											sn.addUnknown(urlId, p);
										} else {
											if (ic == ImageConfiguration.RgbSide)
												sn.addRgb(urlId, p);
											if (ic == ImageConfiguration.FluoSide)
												sn.addFluo(urlId, p);
											if (ic == ImageConfiguration.NirSide)
												sn.addNir(urlId, p);
											
											if (ic == ImageConfiguration.RgbTop)
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
										sn.storeValue(idx, (double) imageCount);
									}
								}
							}
							
							if (addSN) {
								if (optSnapshotFilter == null)
									result.add(sn);
								else
									if (!optSnapshotFilter.filterOut(sn))
										result.add(sn);
									else {
										System.out.println("About to filter out a snapshot: " + sn);
										System.out.println("RES=" + optSnapshotFilter.filterOut(sn));
									}
							}
						}
					}
				}
			}
		
		sw.printTime(100);
		
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
		
		for (SnapshotDataIAP sd : result)
			if (prepareTransportToBrowser)
				sd.prepareFieldsForDataTransport();
			else
				sd.prepareStore();
		
		return result;
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
		ArrayList<NumericMeasurementInterface> ml = (ArrayList<NumericMeasurementInterface>) measurements;
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
				&& header.getDatabaseId().startsWith("lemnatec:"))
			experiment = new de.ipk.ag_ba.postgresql.LemnaTecDataExchange().getExperiment(header,
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
			c.add("# example config: 1116BA, auto, 10,30, klukas@ipk-gatersleben.de   -- check 1116BA every 30 minutes for watering data within the last 30 minutes, ignoring known start and stop times (with up to 10 minutes difference) from previous day");
			// add all experiments from today or yesterday as default entries to file
			LemnaTecDataExchange lde = new LemnaTecDataExchange();
			ArrayList<ExperimentHeaderInterface> el = new ArrayList<ExperimentHeaderInterface>();
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SCAN DB CONTENT...");
			for (String database : new LemnaTecDataExchange().getDatabases()) {
				try {
					el.addAll(lde.getExperimentsInDatabase(null, database));
				} catch (Exception e) {
					if (!e.getMessage().contains("relation \"snapshot\" does not exist"))
						System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">Can't process DB " + database + ": " + e.getMessage());
				}
			}
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">CHECK PROGRESS...");
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
							String s = ehi.getDatabase() + "," + ehi.getExperimentName() + ",auto,10,30" + (String) inp[0];
							// s = ehi.getDatabase() + "," + ehi.getExperimentName() + ",0:00,23:59,0:00,23:59,30," + (String) inp[0];
							c.add(s);
							System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">ADD WATCH ENTRY: " + s);
						}
			c.write(experimentListFileName);
		} else
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">READ CONFIG FILE " + experimentListFileName + "...");
		HashSet<String> outOfDateExperiments = new HashSet<String>();
		HashMap<IAPwebcam, Long> cam2lastSnapshot = new HashMap<IAPwebcam, Long>();
		long startTime = System.currentTimeMillis();
		
		boolean containsAutoTimingSetting = true;
		HashMap<String, BitSet> experimentId2minutesWithDataFromLastDay = new HashMap<String, BitSet>();
		int autoTimeingLastInfoDay = -1;
		while (true) {
			boolean createVideo = true;
			if (createVideo) {
				storeImages(cam2lastSnapshot);
			}
			
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
				LemnaTecDataExchange lde = new LemnaTecDataExchange();
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
											ExperimentInterface experimentData = new ExperimentReference(ehi).getData((MongoDB) null);
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
								String imageSrc = null;
								String fileName = null;
								if (ehi.getDatabase().startsWith("CGH")) {
									fileName = "maize " + SystemAnalysis.getCurrentTimeInclSec() + ".jpg";
									imageSrc = "http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=1280x960&Quality=Clarity";
								} else
									if (ehi.getDatabase().startsWith("BGH")) {
										fileName = "barley " + SystemAnalysis.getCurrentTimeInclSec() + ".jpg";
										imageSrc = "root:lemnatec@http://lemnacam.ipk-gatersleben.de/jpg/image.jpg?timestamp=" +
												System.currentTimeMillis();
									}
								if (fileName != null)
									Thread.sleep(1000); // to ensure that each email has a different file name
									
								if ((m1 || m2) && m3) {
									// WARN
									foundSomeError = true;
									errorMessages.add(SystemAnalysis.getCurrentTimeInclSec() + ">NO CURRENT DATA FOR EXPERIMENT " + ehi.getExperimentName()
											+ ", LAST DATA FROM " + SystemAnalysis.getCurrentTime(ehi.getImportdate().getTime()));
									if (!outOfDateExperiments.contains(ehi.getExperimentName())) {
										System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SEND WARNING MAIL FOR EXPERIMENT "
												+ ehi.getExperimentName()
												+ " TO " + wc.getMails());
										if (System.currentTimeMillis() - startTime < 15 * 60 * 1000)
											System.out.println(SystemAnalysis.getCurrentTime() + ">WITHIN THE FIRST 15 MINUTES OF START NO MAIL WILL BE SEND");
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
													imageSrc, fileName);
									}
									outOfDateExperiments.add(ehi.getExperimentName());
								} else {
									// all OK
									if (outOfDateExperiments.contains(ehi.getExperimentName())) {
										System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SEND INFO MAIL FOR EXPERIMENT " + ehi.getExperimentName()
												+ " TO " + wc.getMails());
										if (System.currentTimeMillis() - startTime < 15 * 60 * 1000)
											System.out.println(SystemAnalysis.getCurrentTime() + ">WITHIN THE FIRST 15 MINUTES OF START NO MAIL WILL BE SEND");
										else
											m.sendEmail(
													wc.getMails(),
													"INFO: " + ehi.getExperimentName()
															+ " STATUS CHANGE",
													"INFO: " + ehi.getExperimentName()
															+ " shows progress again " + lastUpdateText + " // " + SystemAnalysis.getUserName() + "@"
															+ host + "\n\n" +
															"After error condition new data has been found for experiment " + ehi.getExperimentName()
															+ ". Status is now OK!\n\n\nExperiment details:\n\n" +
															StringManipulationTools.stringReplace(ehi.toStringLines(), "<br>", "\n"),
													imageSrc, fileName);
										
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
	
	private static void storeImages(final HashMap<IAPwebcam, Long> cam2lastSnapshot) {
		if (!cam2lastSnapshot.containsKey(IAPwebcam.BARLEY))
			cam2lastSnapshot.put(IAPwebcam.BARLEY, 0l);
		if (!cam2lastSnapshot.containsKey(IAPwebcam.MAIZE))
			cam2lastSnapshot.put(IAPwebcam.MAIZE, 0l);
		long t = System.currentTimeMillis();
		for (final IAPwebcam cam : cam2lastSnapshot.keySet()) {
			try {
				// at most every 5 minutes
				if (t - cam2lastSnapshot.get(cam) >= 1000 * 60 * 5) {
					final InputStream inp = cam.getSnapshotJPGdata();
					MongoDB mm = MongoDB.getDefaultCloud();
					mm.processDB(new RunnableOnDB() {
						private DB db;
						
						@Override
						public void run() {
							GridFS gridfs_webcam_files = new GridFS(db, "fs_webcam_" + cam.toString());
							GridFSInputFile inputFile = gridfs_webcam_files.createFile(inp, cam.getFileName());
							inputFile.setMetaData(new BasicDBObject("time", System.currentTimeMillis()));
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
	
	public static int getIRintensityDifferenceColor(double d, int back) {
		if (Double.isNaN(d))
			return back;
		else {
			if (d < 0) {
				int gray = (int) Math.round(255 - (-d * 255) * 17);
				if (gray < 0) {
					System.err.println("Too high temp differnence (multiplier for visualization is too high): " + gray);
					gray = 0;
				}
				return new Color(gray, gray, gray).getRGB();
			} else {
				int red = (int) Math.round(d * 255);
				red = 255 - red;
				return new Color(255, 255, 255).getRGB();
			}
		}
	}
	
	public static String getNiceNameForPhenotypicProperty(String substanceName) {
		return niceNames.get(substanceName);
	}
	
	public static double MathPow(double v, double ot) {
		// if (v < 0 || v > 1.1) {
		// System.out.println("TODO: " + v);
		// return Math.pow(v, ot);
		// } else
		return cubeRoots[(int) (1000 * v)];
	}
	
	public static int getMaxTrayCount(ExperimentInterface experiment) throws Exception {
		int max = 1;
		for (SubstanceInterface si : experiment)
			for (ConditionInterface ci : si) {
				int tc = getTrayCountFromCondition(ci);
				if (tc > max)
					max = tc;
			}
		return max;
	}
	
	public static boolean isAnalyzedWithCurrentRelease(ExperimentHeaderInterface exp) {
		for (IAP_RELEASE ir : IAP_RELEASE.values())
			if (exp.getRemark().contains(ir.toString()))
				return true;
		return false;
	}
	
	public static void showImageJ() {
		if (SystemAnalysis.isHeadless())
			return;
		if (IAPservice.ij == null || !IAPservice.ij.isShowing())
			IAPservice.ij = new ImageJ();
	}
	
	private static ImageJ ij = null;
	
	public static int getTrayCountFromCondition(ConditionInterface con) {
		String t = con.getTreatment();
		int executionTrayCount = 1;
		if (t != null && (t.contains("OAC_2x3") || t.contains("6-tray"))) {
			executionTrayCount = 6; // 2x3
		} else
			if (t != null && (t.contains("OAC_4x3") || t.contains("12-tray"))) {
				executionTrayCount = 12; // 3x4
			} else {
				t = con.getGrowthconditions();
				if (t != null && (t.contains("OAC_2x3") || t.contains("6-tray"))) {
					executionTrayCount = 6; // 2x3
				} else
					if (t != null && (t.contains("OAC_4x3") || t.contains("12-tray"))) {
						executionTrayCount = 12; // 3x4
					}
			}
		return executionTrayCount;
	}
}