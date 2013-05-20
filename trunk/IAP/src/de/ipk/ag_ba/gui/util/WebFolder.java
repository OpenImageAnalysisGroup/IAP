/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 28, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import info.clearthought.layout.TableLayout;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.view.ZoomListener;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.commands.datasource.AbstractUrlNavigationAction;
import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.TabMetaCrop;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.WebDirectoryFileListAccess;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class WebFolder {
	public static NavigationButton getBrowserNavigationEntity(final Library lib, String title,
			String icon, final String url, final String referenceTitle, final String referenceImage,
			final IOurl referenceURL, final String[] valid, final String introTxt,
			final String optSubFolderForFolderItems, GUIsetting guiSetting) {
		
		NavigationButton nav = new NavigationButton(new AbstractNavigationAction("Open web-folder content") {
			private NavigationButton src;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
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
				ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
				if (referenceTitle != null) {
					NavigationAction action = new AbstractUrlNavigationAction("Show in browser") {
						@Override
						public void performActionCalculateResults(NavigationButton src) {
							AttributeHelper.showInBrowser(referenceURL);
						}
						
						@Override
						public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
							return null;
						}
						
						@Override
						public ArrayList<NavigationButton> getResultNewActionSet() {
							return null;
						}
						
						@Override
						public IOurl getURL() {
							return referenceURL;
						}
					};
					NavigationButton website = new NavigationButton(action, referenceTitle, referenceImage,
							src.getGUIsetting());
					website.setToolTipText("Open " + referenceURL);
					actions.add(website);
				}
				
				if (lib != null) {
					for (Book fpp : lib.getBooksInFolder("")) {
						final Book fp = fpp;
						NavigationAction action = new AbstractUrlNavigationAction("Open web-resource") {
							@Override
							public void performActionCalculateResults(NavigationButton src) {
								AttributeHelper.showInBrowser(fp.getUrl());
							}
							
							@Override
							public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
								return null;
							}
							
							@Override
							public ArrayList<NavigationButton> getResultNewActionSet() {
								return null;
							}
							
							@Override
							public IOurl getURL() {
								return fp.getUrl();
							}
						};
						
						NavigationButton website = new NavigationButton(action, fp.getTitle(), "img/dataset.png",
								src.getGUIsetting());
						website.setToolTipText("Open " + fp.getUrl());
						actions.add(website);
					}
				}
				
				NavigationAction subFolderAction = null;
				if (optSubFolderForFolderItems != null && optSubFolderForFolderItems.length() > 0) {
					subFolderAction = new EmptyNavigationAction(optSubFolderForFolderItems, "Show List of Web-Resources",
							"img/ext/folder.png", "img/ext/folder-drag-accept_t.png");
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
							NavigationButton ne = IAPservice.getPathwayViewEntity(mc, src.getGUIsetting());
							actions.add(ne);
						}
					}
				} catch (Exception e) {
					NavigationButton ne = Other.getServerStatusEntity(src.getGUIsetting());
					ne.setTitle("- Connection Problem -");
					actions.add(ne);
					ErrorMsg.addErrorMessage(e);
				}
				
				for (String f : folders) {
					final String ff = f;
					NavigationButton ne = new NavigationButton(new AbstractNavigationAction("Open web-folder") {
						private NavigationButton src2;
						
						@Override
						public void performActionCalculateResults(NavigationButton src) {
							this.src2 = src;
						}
						
						@Override
						public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
							ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
							res.add(src2);
							return res;
						}
						
						@Override
						public ArrayList<NavigationButton> getResultNewActionSet() {
							ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
							
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
							}, referenceTitle, "img/dataset.png", guiSetting);
							website.setToolTipText("Open " + url);
							res.add(website);
							
							// "img/ext/folder-drag-accept_t.png"
							
							for (PathwayWebLinkItem mc : folder2file.get(ff)) {
								NavigationButton j = IAPservice.getPathwayViewEntity(mc, guiSetting);
								res.add(j);
							}
							
							return res;
						}
					}, f,
							IAPimages.getFolderRemoteOpen(),
							IAPimages.getFolderRemoteClosed(), src.getGUIsetting());
					
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
	
	/**
	 * @deprecated Use {@link IAPservice#getPathwayViewEntity(PathwayWebLinkItem,GUIsetting)} instead
	 */
	@Deprecated
	private static NavigationButton getPathwayViewEntity(final PathwayWebLinkItem mmc, GUIsetting guiSettings) {
		return IAPservice.getPathwayViewEntity(mmc, guiSettings);
	}
	
	public static void addAnnotationsToGraphElements(Graph graph) {
		new TabMetaCrop().addAnnotationsToGraphElements(graph);
	}
	
	static ThreadSafeOptions currentZoom = new ThreadSafeOptions();
	
	public static JComponent getZoomSliderForGraph(final ObjectRef scrollpaneRef) {
		
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
			@Override
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
			@Override
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
	
	public static NavigationButton getURLactionButtton(String title, final IOurl referenceURL, String image,
			GUIsetting guiSetting) {
		NavigationAction action = getURLaction(title, referenceURL, image);
		NavigationButton website = new NavigationButton(action, title, image, guiSetting);
		website.setToolTipText("Open " + referenceURL);
		
		return website;
	}
	
	public static NavigationAction getURLaction(final String title, final IOurl referenceURL, final String img) {
		NavigationAction action = new AbstractUrlNavigationAction("Show in browser") {
			IOurl trueURL = null;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) {
				if (trueURL == null)
					if (referenceURL.endsWith(".webloc"))
						try {
							trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
							AttributeHelper.showInBrowser(referenceURL);
							return;
						}
					else
						trueURL = referenceURL;
				AttributeHelper.showInBrowser(trueURL);
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return null;
			}
			
			@Override
			public String getDefaultTitle() {
				return title;
			}
			
			@Override
			public String getDefaultImage() {
				return img;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
			
			@Override
			public IOurl getURL() {
				if (trueURL == null)
					if (referenceURL.endsWith(".webloc"))
						try {
							trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
							return trueURL;
						}
					else
						trueURL = referenceURL;
				return trueURL;
			}
		};
		return action;
	}
}
