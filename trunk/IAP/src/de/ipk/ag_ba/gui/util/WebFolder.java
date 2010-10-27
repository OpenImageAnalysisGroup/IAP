/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Apr 28, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.ObjectRef;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.ConfigureViewAction;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.session.EditorSession;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.Other;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.AIPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.TabMetaCrop;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.WebDirectoryFileListAccess;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class WebFolder {
	public static NavigationButton getBrowserNavigationEntity(final HashMap<String, String> folder2url,
			String title, String icon, final String url, final String referenceTitle, final String referenceImage,
			final String referenceURL, final String[] valid, final String introTxt,
			final String optSubFolderForFolderItems, GUIsetting guiSetting) {

		NavigationButton nav = new NavigationButton(new AbstractNavigationAction(
				"Open web-folder content") {
			private NavigationButton src;

			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
					ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
				if (referenceTitle != null) {
					NavigationAction action = new AbstractNavigationAction("Show in browser") {
						@Override
						public void performActionCalculateResults(NavigationButton src) {
							AttributeHelper.showInBrowser(referenceURL);
						}

						@Override
						public ArrayList<NavigationButton> getResultNewNavigationSet(
								ArrayList<NavigationButton> currentSet) {
							return null;
						}

						@Override
						public ArrayList<NavigationButton> getResultNewActionSet() {
							return null;
						}
					};
					NavigationButton website = new NavigationButton(action, referenceTitle,
							referenceImage, src.getGUIsetting());
					website.setToolTipText("Open " + referenceURL);
					actions.add(website);
				}

				if (folder2url != null && folder2url.containsKey("")) {
					for (String fpp : folder2url.get("").split("\\^")) {
						final String fp = fpp;
						NavigationAction action = new AbstractNavigationAction("Open web-ressource") {
							@Override
							public void performActionCalculateResults(NavigationButton src) {
								AttributeHelper.showInBrowser(fp.split(":", 2)[1]);
							}

							@Override
							public ArrayList<NavigationButton> getResultNewNavigationSet(
									ArrayList<NavigationButton> currentSet) {
								return null;
							}

							@Override
							public ArrayList<NavigationButton> getResultNewActionSet() {
								return null;
							}
						};

						NavigationButton website = new NavigationButton(action, fp.split(":")[0],
								"img/dataset.png", src.getGUIsetting());
						website.setToolTipText("Open " + folder2url.get("").split(":", 2)[1]);
						actions.add(website);
					}
				}

				NavigationAction subFolderAction = null;
				if (optSubFolderForFolderItems != null && optSubFolderForFolderItems.length() > 0) {
					subFolderAction = new EmptyNavigationAction(optSubFolderForFolderItems, "Show List of Web-Ressources",
							"img/ext/folder.png", "img/ext/folder-drag-accept.png");
					NavigationButton subFolder = new NavigationButton(subFolderAction, src.getGUIsetting());
					actions.add(subFolder);
				}

				TreeSet<String> folders = new TreeSet<String>();

				final HashMap<String, TreeSet<PathwayWebLinkItem>> folder2file = new HashMap<String, TreeSet<PathwayWebLinkItem>>();

				try {
					Collection<PathwayWebLinkItem> mainList = WebDirectoryFileListAccess.getWebDirectoryFileListItems(url,
							valid, false);
					for (PathwayWebLinkItem i : mainList) {
						if (i.getGroup1() != null && i.getGroup1().length() > 0) {
							folders.add(i.getGroup1());
							if (!folder2file.containsKey(i.getGroup1()))
								folder2file.put(i.getGroup1(), new TreeSet<PathwayWebLinkItem>());
							folder2file.get(i.getGroup1()).add(i);
						}
					}

					if (folders.size() == 0) {
						for (PathwayWebLinkItem mc : mainList) {
							NavigationButton ne = getPathwayViewEntity(mc, src.getGUIsetting());
							actions.add(ne);
						}
					}
				} catch (Exception e) {
					NavigationButton ne = Other.getServerStatusEntity(false, src.getGUIsetting());
					ne.setTitle("- Connection Problem -");
					actions.add(ne);
					ErrorMsg.addErrorMessage(e);
				}

				for (String f : folders) {
					final String ff = f;
					NavigationButton ne = new NavigationButton(new AbstractNavigationAction(
							"Open web-folder") {
						private NavigationButton src2;

						@Override
						public void performActionCalculateResults(NavigationButton src) {
							this.src2 = src;
						}

						@Override
						public ArrayList<NavigationButton> getResultNewNavigationSet(
								ArrayList<NavigationButton> currentSet) {
							ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
							res.add(src2);
							return res;
						}

						@Override
						public ArrayList<NavigationButton> getResultNewActionSet() {
							ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();

							if (folder2url != null && folder2url.containsKey(ff)) {
								String title = folder2url.get(ff).split(":")[0];
								final String url = folder2url.get(ff).split(":", 2)[1];
								NavigationButton website = new NavigationButton(new AbstractNavigationAction(
										"Show web-resource") {
									@Override
									public void performActionCalculateResults(NavigationButton src) {
										AttributeHelper.showInBrowser(url);
									}

									@Override
									public ArrayList<NavigationButton> getResultNewNavigationSet(
											ArrayList<NavigationButton> currentSet) {
										return null;
									}

									@Override
									public ArrayList<NavigationButton> getResultNewActionSet() {
										return null;
									}
								}, title, "img/dataset.png", src.getGUIsetting());
								website.setToolTipText("Open " + url);
								res.add(website);
							}

							// "img/ext/folder-drag-accept.png"

							for (PathwayWebLinkItem mc : folder2file.get(ff)) {
								NavigationButton j = getPathwayViewEntity(mc, src.getGUIsetting());
								res.add(j);
							}

							return res;
						}
					}, f, "img/ext/folder-remote-open.png", "img/ext/folder-remote.png", src.getGUIsetting());

					if (subFolderAction != null)
						subFolderAction.addAdditionalEntity(ne);
					else
						actions.add(ne);
				}
				return actions;
			}

			@Override
			public MainPanelComponent getResultMainPanel() {
				if (introTxt != null)
					return new MainPanelComponent(introTxt);
				else
					return null;
			}
		}, title, icon, guiSetting);

		return nav;
	}

	private static NavigationButton getPathwayViewEntity(final PathwayWebLinkItem mmc, GUIsetting guiSettings) {
		NavigationButton ne = new NavigationButton(new AbstractNavigationAction(
				"Load web-folder content") {
			private NavigationButton src = null;
			private final ObjectRef graphRef = new ObjectRef();
			private final ObjectRef scrollpaneRef = new ObjectRef();

			@Override
			public void performActionCalculateResults(NavigationButton src) {
				this.src = src;

				URL url;
				try {
					url = new URL(mmc.getURL());
					final Graph g = MainFrame.getInstance().getGraph(mmc.getFileName(), url);
					graphRef.setObject(g);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}

			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
					ArrayList<NavigationButton> currentSet) {
				ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
				res.add(src);
				return res;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();

				NavigationAction action = new AbstractNavigationAction("Show Graph in IAP Online-Version of VANTED") {
					Graph g;

					@Override
					public void performActionCalculateResults(NavigationButton src) {
						g = (Graph) graphRef.getObject();
					}

					@Override
					public ArrayList<NavigationButton> getResultNewNavigationSet(
							ArrayList<NavigationButton> currentSet) {
						return null;
					}

					@Override
					public ArrayList<NavigationButton> getResultNewActionSet() {

						AIPmain.showVANTED();
						if (g != null)
							MainFrame.getInstance().showGraph(g, null, LoadSetting.VIEW_CHOOSER_NEVER);

						return null;
					}

					@Override
					public boolean getProvidesActions() {
						return false;
					}

				};

				NavigationButton editInVanted = new NavigationButton(action, "Edit in VANTED",
						"img/vanted1_0.png", src.getGUIsetting());
				result.add(editInVanted);

				JComponent zoomSlider = getZoomSliderForGraph(scrollpaneRef);
				result.add(new NavigationButton(zoomSlider, src.getGUIsetting()));

				return result;
			}

			@Override
			public MainPanelComponent getResultMainPanel() {
				try {
					Graph g = (Graph) graphRef.getObject();
					if (g != null) {
						boolean isMetaCrop = mmc.getURL().contains("http://pgrc-16.ipk-gatersleben.de/wgrp/nwg/metacrop");
						if (isMetaCrop) {
							System.out.println("Adding MetaCrop links");
							addAnnotationsToGraphElements(g);
						}
						EditorSession es = new EditorSession(g);
						final ObjectRef refLastURL = new ObjectRef();
						final ObjectRef refLastDragPoint = new ObjectRef("", new Vector2d(0, 0));
						JScrollPane graphViewScrollPane = MainFrame.getInstance().showViewChooserDialog(es, true, null,
								LoadSetting.VIEW_CHOOSER_NEVER_DONT_ADD_VIEW_TO_EDITORSESSION, new ConfigureViewAction() {
									View newView;

									public void storeView(View v) {
										newView = v;
									}

									public void run() {
										final ObjectRef beingDragged = new ObjectRef("", false);

										final GraffitiView gv = (GraffitiView) newView;
										// gv.setDrawMode(DrawMode.REDUCED); // REDUCED
										gv.threadedRedraw = false;

										final MouseMotionListener mml = new MouseMotionListener() {
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
											public void mouseReleased(MouseEvent e) {
												e.consume();
												if ((Boolean) beingDragged.getObject()) {
													beingDragged.setObject(false);
													((Component) scrollpaneRef.getObject()).setCursor(Cursor
															.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
												}
												mml.mouseMoved(e);
											}

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

											public void mouseExited(MouseEvent e) {
											}

											public void mouseEntered(MouseEvent e) {
											}

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
											public void run() {
												BackgroundTaskHelper.executeLaterOnSwingTask(100, new Runnable() {
													public void run() {
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

	protected static void addAnnotationsToGraphElements(Graph graph) {
		new TabMetaCrop().addAnnotationsToGraphElements(graph);
	}

	static ThreadSafeOptions currentZoom = new ThreadSafeOptions();

	private static JComponent getZoomSliderForGraph(final ObjectRef scrollpaneRef) {

		int FPS_MIN = 0;
		int FPS_MAX = 200;
		int FPS_INIT = 100;

		final JSlider sliderZoom = new JSlider(JSlider.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);

		final JLabel lbl = new JLabel("Zoom (100%)");

		// Turn on labels at major tick marks.
		sliderZoom.setMajorTickSpacing(50);
		sliderZoom.setMinorTickSpacing(10);
		sliderZoom.setPaintTicks(true);
		sliderZoom.setPaintLabels(true);
		sliderZoom.setOpaque(false);
		sliderZoom.setVisible(false);
		BackgroundTaskHelper.executeLaterOnSwingTask(200, new Runnable() {
			public void run() {
				sliderZoom.setVisible(true);
			}
		});

		if (currentZoom.getInt() == 0)
			currentZoom.setInt(100);
		else {
			updateZoom((JScrollPane) scrollpaneRef.getObject(), lbl, sliderZoom, currentZoom.getInt());
			sliderZoom.setValue(currentZoom.getInt());
		}

		sliderZoom.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider s = (JSlider) e.getSource();
				int val = s.getValue() - s.getValue() % 5;
				if (val < 5)
					val = 5;
				if (val != currentZoom.getInt()) {
					currentZoom.setInt(val);
					updateZoom((JScrollPane) scrollpaneRef.getObject(), lbl, s, val);
				}
			}
		});

		return TableLayout.getSplitVertical(lbl, sliderZoom, TableLayout.PREFERRED, TableLayout.PREFERRED);
	}

	// ImageIcon icon = GravistoService.loadIcon(AIPmain.class, img, -48, 48);
	// final JButton n1 = new JButton(title, icon);
	// n1.setOpaque(false);
	//
	// n1.setVerticalTextPosition(SwingConstants.BOTTOM);
	// n1.setHorizontalTextPosition(SwingConstants.CENTER);
	//
	// n1.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// MyUtility.navigate(title);
	// SwingUtilities.invokeLater(r);
	// }
	// });

	private static void updateZoom(final JScrollPane graphViewScrollPane, final JLabel lbl, JSlider s, int val) {
		lbl.setText("Zoom (" + val + "%)");
		AffineTransform at = new AffineTransform();
		at.setToScale(val / 100d, val / 100d);
		try {
			ZoomListener zoomView = (ZoomListener) graphViewScrollPane.getViewport().getView();
			zoomView.zoomChanged(at);
		} catch (Exception err) {
			s.setEnabled(false);
			lbl.setText("Zoom (not supported)");
		}
	}

	public static NavigationButton getURLentity(String title, final String referenceURL, String image,
			GUIsetting guiSetting) {
		NavigationAction action = new AbstractNavigationAction("Show in browser") {
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				AttributeHelper.showInBrowser(referenceURL);
			}

			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(
					ArrayList<NavigationButton> currentSet) {
				return null;
			}

			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
		};
		NavigationButton website = new NavigationButton(action, title, image, guiSetting);
		website.setToolTipText("Open " + referenceURL);

		return website;
	}
}
