/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.ObjectRef;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.commands.BookmarkAction;
import de.ipk.ag_ba.gui.enums.ButtonDrawStyle;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.interfaces.StyleAware;
import de.ipk.ag_ba.gui.navigation_actions.ActionClearClipboard;
import de.ipk.ag_ba.gui.navigation_actions.ActionMergeClipboard;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.PopupListener;
import de.ipk.ag_ba.gui.webstart.Bookmark;
import de.ipk.ag_ba.gui.webstart.IAPgui;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class MyNavigationPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ArrayList<NavigationButton> set;
	private final JComponent graphPanel;
	private MyNavigationPanel theOther;
	private final PanelTarget target;
	private final JPanel actionPanelRight;
	private ButtonDrawStyle buttonStyle = ButtonDrawStyle.FLAT;
	private final JCheckBoxMenuItem menuItemCompact, menuItemCompact2;
	private final JCheckBoxMenuItem menuItemFlat;
	private final JCheckBoxMenuItem menuItemButtons;
	private JScrollPane scrollpane;
	private int maxYY;
	private GUIsetting guiSettting;
	
	public MyNavigationPanel(PanelTarget target, JComponent graphPanel, JPanel actionPanelRight) {
		this.target = target;
		this.graphPanel = graphPanel;
		this.actionPanelRight = actionPanelRight;
		
		JPopupMenu popup = new JPopupMenu("Button Style");
		
		JMenuItem menuItem = new JMenuItem("New Window");
		menuItem.setIcon(GravistoService.loadIcon(IAPmain.class, "img/new_frame.png"));
		menuItem.addActionListener(getNewWindowListener());
		popup.add(menuItem);
		
		popup.addSeparator();
		
		menuItemCompact = new JCheckBoxMenuItem("Compact", buttonStyle == ButtonDrawStyle.COMPACT_LIST);
		menuItemCompact.putClientProperty("style", ButtonDrawStyle.COMPACT_LIST);
		menuItemCompact.addActionListener(this);
		
		popup.add(menuItemCompact);
		
		menuItemCompact2 = new JCheckBoxMenuItem("Compact (2)", buttonStyle == ButtonDrawStyle.COMPACT_LIST_2);
		menuItemCompact2.putClientProperty("style", ButtonDrawStyle.COMPACT_LIST_2);
		menuItemCompact2.addActionListener(this);
		
		popup.add(menuItemCompact2);
		
		menuItemFlat = new JCheckBoxMenuItem("Flat", buttonStyle == ButtonDrawStyle.FLAT);
		menuItemFlat.putClientProperty("style", ButtonDrawStyle.FLAT);
		menuItemFlat.addActionListener(this);
		popup.add(menuItemFlat);
		
		menuItemButtons = new JCheckBoxMenuItem("Buttons", buttonStyle == ButtonDrawStyle.BUTTONS);
		menuItemButtons.putClientProperty("style", ButtonDrawStyle.BUTTONS);
		menuItemButtons.addActionListener(this);
		popup.add(menuItemButtons);
		// menuItem = new JMenuItem("Text Only");
		// menuItem.putClientProperty("style", ButtonDrawStyle.TEXT);
		// menuItem.addActionListener(this);
		// popup.add(menuItem);
		
		addMouseListener(new PopupListener(popup));
	}
	
	private ActionListener getNewWindowListener() {
		ActionListener res = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame jff = new JFrame("IAP Cloud Storage, Analysis and Visualization System");
				jff.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
				BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
						"", "");
				jff.add(IAPgui.getMainGUIcontent(myStatus, true), "0,0");
				jff.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				jff.setLocationByPlatform(true);
				jff.setSize(800, 600);
				jff.setVisible(true);
				jff.revalidate();
				jff.doLayout();
			}
		};
		return res;
	}
	
	public ArrayList<NavigationButton> getEntitySet(boolean includeBookmarks) {
		if (includeBookmarks)
			return set;
		else {
			ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
			for (NavigationButton n : set) {
				if (n == null)
					continue;
				if (!(n.getAction() instanceof BookmarkAction))
					res.add(n);
			}
			return res;
		}
	}
	
	public void setEntitySet(ArrayList<NavigationButton> set) {
		if (set == null)
			return;
		this.set = new ArrayList<NavigationButton>(set);
		if (target == PanelTarget.ACTION) {
			if (guiSettting.getClipboardItems().size() > 0) {
				NavigationAction na = new ActionClearClipboard("Remove all entries from the clipboard");
				NavigationButton nb = new NavigationButton(na, guiSettting);
				nb.setRightAligned(true);
				this.set.add(nb);
			}
			if (guiSettting.getClipboardItems().size() > 1) {
				NavigationAction na = new ActionMergeClipboard("Merge clipboard data set");
				NavigationButton nb = new NavigationButton(na, guiSettting);
				nb.setRightAligned(true);
				this.set.add(nb);
			}
			for (ExperimentReference clipboardItem : guiSettting.getClipboardItems()) {
				NavigationAction na = new ActionMongoOrLemnaTecExperimentNavigation(clipboardItem);
				NavigationButton nb = new NavigationButton("Clipboard item " + clipboardItem.getExperimentName() + "", na, guiSettting);
				nb.setRightAligned(true);
				this.set.add(nb);
			}
		}
		updateGUI();
	}
	
	private void updateGUI() {
		transferFocusUpCycle();
		removeAll();
		if (set != null) {
			ButtonDrawStyle buttonStyleToUse = buttonStyle;
			if (target == PanelTarget.ACTION) {
				if (theOther != null && getEntitySet(true).size() == 0 && theOther.buttonStyle != ButtonDrawStyle.COMPACT_LIST_2) {
					theOther.buttonStyle = ButtonDrawStyle.COMPACT_LIST;
					theOther.updateGUI();
					theOther.disableContextMenu();
				} else {
					if (theOther.buttonStyle != buttonStyle) {
						theOther.buttonStyle = buttonStyle;
						theOther.updateGUI();
						theOther.enableContextMenu();
					}
				}
			}
			ArrayList<JComponent> right = new ArrayList<JComponent>();
			boolean first = true;
			ObjectRef next = new ObjectRef();
			boolean firstStar = true;
			for (NavigationButton ne : set) {
				if (ne == null)
					continue;
				if (ne instanceof StyleAware) {
					((StyleAware) ne).setButtonStyle(buttonStyleToUse);
				}
				if (getTarget() == PanelTarget.NAVIGATION) {
					if (!first) {
						JLabel lbl = new JLabel("<html><small>" + Unicode.ARROW_RIGHT);
						if (next != null) {
							next.setObject(lbl);
							next = new ObjectRef();
						}
						if (ne.getAction() instanceof BookmarkAction) {
							if (firstStar)
								lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
							else
								lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
							
							lbl.setText("<html><font size='5'>" + Unicode.STAR);
							firstStar = false;
							lbl.setToolTipText("Remove " + ne.getTitle() + " bookmark");
							lbl.addMouseListener(getDeleteBookmarkActionListener(lbl, next, ne.getAction()));
						} else {
							lbl.addMouseListener(getAddBookmarkActionListener(lbl, next, ne));
							lbl.setToolTipText("Add bookmark");
						}
						lbl.setForeground(Color.GRAY);
						add(lbl);
					}
					add(NavigationButton.getNavigationButton(buttonStyleToUse, ne, getTarget(), this, getTheOther(), graphPanel));
					first = false;
				} else {
					if (actionPanelRight != null && ne.isRightAligned())
						right.add(NavigationButton.getNavigationButton(buttonStyleToUse, ne, getTarget(), getTheOther(), this,
								graphPanel));
					else
						add(NavigationButton.getNavigationButton(buttonStyleToUse, ne, getTarget(), getTheOther(), this,
								graphPanel));
				}
			}
			if (!firstStar) {
				JLabel lbl = new JLabel("<html><small>" + Unicode.ARROW_RIGHT);
				if (next != null) {
					next.setObject(lbl);
				}
				lbl.setText("<html><font size='5'>" + Unicode.STAR);
				lbl.setForeground(Color.GRAY);
				add(lbl);
			}
			if (actionPanelRight != null) {
				if (right.size() > 0) {
					actionPanelRight.setBackground(new Color(255, 220, 220));
					actionPanelRight.setLayout(TableLayout.getLayout(TableLayout.PREFERRED, TableLayout.PREFERRED));
					actionPanelRight.removeAll();
					actionPanelRight.add(TableLayout.getMultiSplit(right, 100, 6, 6, 5, 5), "0,0");
				} else {
					actionPanelRight.setLayout(TableLayout.getLayout(TableLayout.PREFERRED, TableLayout.PREFERRED));
					actionPanelRight.removeAll();
				}
			}
		}
		if (getParent() != null) {
			getParent().revalidate();
			getParent().repaint();
			// revalidate();
			if (getParent() != null) {
				// getParent().getParent().revalidate();
				// getParent().getParent().repaint();
			}
			
		} else {
			revalidate();
			repaint();
			if (actionPanelRight != null) {
				actionPanelRight.revalidate();
				actionPanelRight.repaint();
			}
		}
	}
	
	private void enableContextMenu() {
		menuItemCompact.setEnabled(true);
		menuItemCompact2.setEnabled(true);
		menuItemFlat.setEnabled(true);
		menuItemButtons.setEnabled(true);
	}
	
	private void disableContextMenu() {
		menuItemCompact.setEnabled(false);
		menuItemCompact2.setEnabled(false);
		menuItemFlat.setEnabled(false);
		menuItemButtons.setEnabled(false);
	}
	
	private MouseListener getAddBookmarkActionListener(final JLabel lbl, final ObjectRef right, final NavigationButton ne) {
		MouseListener res = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 1) {
					if (lbl.contains(e.getX(), e.getY())) {
						System.out.println(ne.getTitle());
						BufferedImage i;
						if (ne.getIconInactive() != null)
							i = GravistoService.getBufferedImage(ne.getIconInactive().getImage());
						else
							i = GravistoService.getBufferedImage(GravistoService.loadIcon(IAPmain.class,
									ne.getNavigationImage()).getImage());
						// add bookmark
						String target = getTargetPath(ne);
						if (target != null) {
							if (Bookmark.add(ne.getTitle(), target, i)) {
								if (!set.isEmpty())
									set.iterator().next().executeNavigation(PanelTarget.NAVIGATION, MyNavigationPanel.this,
											theOther, graphPanel, null, null);
							} else
								MainFrame.getInstance().showMessageDialog("Could not add bookmark.");
						} else
							MainFrame.getInstance().showMessageDialog("Could not determine target path (tasks are running).");
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (right.getObject() != null)
					((JLabel) right.getObject()).setText("<html><small>" + Unicode.ARROW_RIGHT);
				if (lbl != null)
					lbl.setText("<html><small>" + Unicode.ARROW_RIGHT);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (right.getObject() != null)
					((JLabel) right.getObject()).setText("<html><font size='5'><b>" + Unicode.ARROW_LEFT_EMPTY);
				if (lbl != null)
					lbl.setText("<html><font size='4'>" + Unicode.PEN);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//
				
			}
		};
		return res;
	}
	
	private String getTargetPath(NavigationButton finalTarget) {
		ArrayList<String> path = new ArrayList<String>();
		for (NavigationButton ne : set) {
			if (ne.isProcessing())
				return null;
			path.add(replaceBadChars(ne.getTitle()));
			if (ne == finalTarget)
				break;
		}
		return StringManipulationTools.getStringList(path, ".");
	}
	
	public static String replaceBadChars(String title) {
		title = StringManipulationTools.removeHTMLtags(title).trim();
		return StringManipulationTools.stringReplace(title, ".", "_");
	}
	
	public static String getTargetPath(Collection<NavigationButton> buttons) {
		ArrayList<String> path = new ArrayList<String>();
		for (NavigationButton ne : buttons) {
			if (ne == null)
				break;
			if (ne.isProcessing())
				return null;
			path.add(replaceBadChars(ne.getTitle()));
		}
		return StringManipulationTools.getStringList(path, ".");
	}
	
	private MouseListener getDeleteBookmarkActionListener(final JLabel lbl, final ObjectRef right,
			final NavigationAction action) {
		MouseListener res = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 1) {
					if (lbl.contains(e.getX(), e.getY())) {
						BookmarkAction ba = (BookmarkAction) action;
						if (!ba.getBookmark().delete()) {
							MainFrame.getInstance().showMessageDialog("Could not delete bookmark.");
						} else {
							if (!set.isEmpty())
								set.iterator().next().executeNavigation(PanelTarget.NAVIGATION, MyNavigationPanel.this,
										theOther, graphPanel, null, null);
						}
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (right.getObject() != null)
					((JLabel) right.getObject()).setText("<html><font size='5'>" + Unicode.STAR);
				if (lbl != null)
					lbl.setText("<html><font size='5'>" + Unicode.STAR);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (right.getObject() != null)
					((JLabel) right.getObject()).setText("<html><font size='5'><b>" + Unicode.ARROW_LEFT_EMPTY);
				if (lbl != null)
					lbl.setText("<html><font size='4'>" + Unicode.RECYCLE);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//
				
			}
		};
		return res;
	}
	
	public static Color getTabColor() {
		Color c2;
		c2 = UIManager.getColor("MenuBar.background");
		if (c2 == null)
			if (UIManager.getBoolean("TabbedPane.contentOpaque")) {
				c2 = UIManager.getColor("TabbedPane.contentAreaColor");
				if (c2 == null)
					c2 = UIManager.getColor("TabbedPane.background");
			} else
				c2 = UIManager.getColor("TabbedPane.background"); // UIManager.getColor("TabbedPane.contentAreaColor");
		if (c2 == null)
			c2 = new Color(200, 200, 255);
		return c2;
	}
	
	public void setTheOther(MyNavigationPanel theOther) {
		this.theOther = theOther;
	}
	
	public MyNavigationPanel getTheOther() {
		return theOther;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent jc = (JComponent) e.getSource();
		Object o = jc.getClientProperty("style");
		if (o != null && o instanceof ButtonDrawStyle) {
			ButtonDrawStyle bds = (ButtonDrawStyle) o;
			buttonStyle = bds;
			theOther.buttonStyle = bds;
			
			menuItemCompact.setSelected(buttonStyle == ButtonDrawStyle.COMPACT_LIST);
			menuItemCompact2.setSelected(buttonStyle == ButtonDrawStyle.COMPACT_LIST_2);
			menuItemFlat.setSelected(buttonStyle == ButtonDrawStyle.FLAT);
			menuItemButtons.setSelected(buttonStyle == ButtonDrawStyle.BUTTONS);
			
			theOther.menuItemCompact.setSelected(buttonStyle == ButtonDrawStyle.COMPACT_LIST);
			theOther.menuItemCompact2.setSelected(buttonStyle == ButtonDrawStyle.COMPACT_LIST_2);
			theOther.menuItemFlat.setSelected(buttonStyle == ButtonDrawStyle.FLAT);
			theOther.menuItemButtons.setSelected(buttonStyle == ButtonDrawStyle.BUTTONS);
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setEntitySet(set);
					theOther.setEntitySet(theOther.set);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JComponent jc = MyNavigationPanel.this;
							while (jc.getParent() != null && jc.getParent() instanceof JComponent)
								jc = (JComponent) jc.getParent();
							jc.revalidate();
							jc.repaint();
						}
					});
				}
			});
		}
	}
	
	public void setScrollpane(JScrollPane scrollpane) {
		this.scrollpane = scrollpane;
	}
	
	public JScrollPane getScrollpane() {
		return scrollpane;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (getScrollpane() == null) {
			Dimension d = super.getPreferredSize();
			if (set.isEmpty())
				d.height = 0;
			return d;
		} else {
			if (target == PanelTarget.NAVIGATION)
				return super.getPreferredSize();
			Component[] comps = getComponents();
			int maxY = 0, lines = 0;
			for (int i = 0; i < comps.length; i++) {
				Component c = comps[i];
				if (c.getY() + c.getHeight() > maxY)
					maxY = c.getY() + c.getHeight();
				else {
					if (lines == 0)
						setMaxYY(maxY);
					lines++;
				}
			}
			if (lines == 0)
				setMaxYY(maxY);
			if (maxY < 8)
				return new Dimension(getScrollpane().getWidth() - 15, maxY + 8);
			else
				return new Dimension(getScrollpane().getWidth() - 15, maxY);
		}
	}
	
	public PanelTarget getTarget() {
		return target;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		int w = getWidth();
		int h = getHeight();
		
		// Paint a gradient from top to bottom
		GradientPaint gp;
		if (getTarget() == PanelTarget.NAVIGATION && !theOther.set.isEmpty())
			gp = new GradientPaint(0, 0, new Color(240, 240, 240), 0, h, new Color(210, 230, 210));
		else {
			Color c2;
			c2 = MyNavigationPanel.getTabColor();
			
			gp = new GradientPaint(0, 0, new Color(250, 250, 250), 0, h, c2);
		}
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}
	
	public void setMaxYY(int maxYY) {
		this.maxYY = maxYY;
	}
	
	public int getMaxYY() {
		return maxYY;
	}
	
	public void setGuiSetting(GUIsetting guiSetting) {
		this.guiSettting = guiSetting;
	}
	
}
