/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import info.StopWatch;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TreeMap;

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

import de.ipk.ag_ba.commands.AbstractGraphUrlNavigationAction;
import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
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
	
	public static Collection<NumericMeasurementInterface> getMatchFor(IOurl url, ExperimentInterface experiment) {
		
		Collection<NumericMeasurementInterface> result = new ArrayList<NumericMeasurementInterface>();
		
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
	private static boolean autoClose;
	
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
	
	public static void setCloudExecutionMode(boolean autoClose) {
		IAPservice.autoClose = autoClose;
	}
	
	public static boolean isCloudExecutionModeActive() {
		return autoClose;
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
			boolean storeAllAngleValues) {
		
		StopWatch sw = new StopWatch("Create Snapshots");
		
		HashMap<Long, SnapshotDataIAP> timestampe2snapshot = new HashMap<Long, SnapshotDataIAP>();
		
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
				if (substance.getName() != null && substance.getName().equals("temp.air")) {
					for (ConditionInterface ci : substance) {
						if (ci.getSpecies() != null && ci.getSpecies().equals("avg")) {
							for (SampleInterface sa : ci) {
								Long time = sa.getRowId();
								if (time != null) {
									double temp = sa.getSampleAverage().getValue();
									timeDay2averageTemp.put(SystemAnalysis.getUnixDay(time, gc), temp - ggd_baseline);
								}
							}
						}
					}
					hasTemperatureData = !timeDay2averageTemp.isEmpty();
				}
			}
			hasTemperatureData = false;
			if (hasTemperatureData) {
				experiment = experiment.clone();
				for (SubstanceInterface substance : experiment) {
					for (ConditionInterface c : sort(substance.toArray(new ConditionInterface[] {}))) {
						for (SampleInterface s : c) {
							Long time = s.getRowId();
							if (time == null)
								continue;
							// replace DAY X or DAS X with GDD Y, based on temperature data
							if (s.getTimeUnit().equalsIgnoreCase("day") || s.getTimeUnit().equalsIgnoreCase("das")) {
								long unixDay = SystemAnalysis.getUnixDay(time, gc);
								double ggd = getGGD(unixDay - s.getTime(), unixDay, timeDay2averageTemp);
								s.setTime((int) ggd);
								s.setTimeUnit("GDD");
							}
							if (s.getTimeUnit().equalsIgnoreCase("unix day")) {
								long unixDay = s.getTime();
								double ggd = getGGD(unixDay, unixDay, timeDay2averageTemp);
								s.setTime((int) ggd);
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
					for (SampleInterface s : c) {
						Long time = s.getRowId();
						
						if (time == null)
							continue;
						
						if (!timestampe2snapshot.containsKey(time)) {
							SnapshotDataIAP ns = new SnapshotDataIAP();
							timestampe2snapshot.put(time, ns);
							result.add(ns);
						}
						
						SnapshotDataIAP sn = timestampe2snapshot.get(time);
						
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
							sn.setTimePoint(s.getSampleTime());
						if (sn.getSnapshotTime() == null)
							sn.setSnapshotTime(s.getRowId());
						sn.setDay(s.getTime());
						
						if (s.size() > 0) {
							NumericMeasurementInterface mm = s.iterator().next();
							if (sn.getPlantId() == null)
								if (mm.getQualityAnnotation() != null && mm.getQualityAnnotation().length() > 0)
									sn.setPlantId("" + mm.getQualityAnnotation());
								else
									sn.setPlantId("" + mm.getReplicateID());
							String sub = s.getParentCondition().getParentSubstance().getName();
							if (optSubstanceIds != null &&
									!sub.equals("water_sum") && !sub.equals("weight_before") && !sub.equals("water_weight")) {
								sub = s.getSubstanceNameWithUnit();
								synchronized (optSubstanceIds) {
									if (!optSubstanceIds.containsKey(sub)) {
										optSubstanceIds.put(sub, optSubstanceIds.size());
									}
									int idx = optSubstanceIds.get(sub);
									s.recalculateSampleAverage();
									if (s.getSampleAverage() != null) {
										double vvv = s.calcMean();
										sn.storeValue(idx, vvv);
									} else
										sn.storeValue(idx, mm.getValue());
									if (storeAllAngleValues) {
										for (NumericMeasurementInterface nmi : s) {
											NumericMeasurement3D nmi3d = (NumericMeasurement3D) nmi;
											sn.storeAngleValue(idx, nmi3d.getPosition(), nmi3d.getValue());
										}
									}
								}
							} else {
								double mmSum = 0;
								double mmLowest = Double.MAX_VALUE;
								for (NumericMeasurementInterface mmm : s) {
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
										sn.setTargetWaterAmount((int) mmSum);
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
						
						if (s instanceof Sample3D) {
							Sample3D s3d = (Sample3D) s;
							Collection<NumericMeasurementInterface> sl = sortImages(s3d.getMeasurements(MeasurementNodeType.IMAGE,
									MeasurementNodeType.VOLUME));
							int imageCount = 0;
							for (NumericMeasurementInterface ii : sl) {
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
								String sub = s.getSubstanceNameWithUnit();
								if (sub != null) {
									int idx = optSubstanceIds.get(sub);
									sn.storeValue(idx, (double) imageCount);
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
		
		// if (prepareTransportToBrowser)
		// currently this needs to be performed, independently on if the data needs to be transported
		for (SnapshotDataIAP sd : result)
			sd.prepareFieldsForDataTransport();
		
		return result;
	}
	
	private static double getGGD(long startUnixDay, long currentUnixDay, TreeMap<Long, Double> timeDay2averageTemp) {
		double sum = 0;
		for (long d = startUnixDay; d <= currentUnixDay; d++) {
			Double gdd = timeDay2averageTemp.get(d);
			if (gdd != null)
				sum += gdd;
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
	
}