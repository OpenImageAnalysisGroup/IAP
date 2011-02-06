/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 11, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

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
import java.util.Date;

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

import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.AbstractGraphUrlNavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

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
					final Graph g = MainFrame.getInstance().getGraph(url, url.getFileName());
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
						gui.setBorder(BorderFactory.createLoweredBevelBorder());
						if (g != null)
							MainFrame.getInstance().showGraph(g, null, LoadSetting.VIEW_CHOOSER_NEVER);
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
						JScrollPane graphViewScrollPane = MainFrame.getInstance().showViewChooserDialog(es, true, null,
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
		}
		return false;
	}
}
