/*******************************************************************************
 * 
 *    Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.enums.ButtonDrawStyle;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.StyleAware;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ModelToGui;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.PopupListener;

/**
 * @author klukas
 */
public class MyNavigationPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ArrayList<NavigationGraphicalEntity> set;
	private final JComponent graphPanel;
	private MyNavigationPanel theOther;
	private final PanelTarget target;
	private final JPanel actionPanelRight;
	private ButtonDrawStyle buttonStyle = ButtonDrawStyle.FLAT;

	public MyNavigationPanel(PanelTarget target, JComponent graphPanel, JPanel actionPanelRight) {
		this.target = target;
		this.graphPanel = graphPanel;
		this.actionPanelRight = actionPanelRight;

		JPopupMenu popup = new JPopupMenu("Button Style");

		JMenuItem menuItem = new JMenuItem("Flat");
		menuItem.putClientProperty("style", ButtonDrawStyle.FLAT);
		menuItem.addActionListener(this);
		popup.add(menuItem);
		menuItem = new JMenuItem("Buttons");
		menuItem.putClientProperty("style", ButtonDrawStyle.BUTTONS);
		menuItem.addActionListener(this);
		popup.add(menuItem);
		// menuItem = new JMenuItem("Text Only");
		// menuItem.putClientProperty("style", ButtonDrawStyle.TEXT);
		// menuItem.addActionListener(this);
		// popup.add(menuItem);
		menuItem = new JMenuItem("List");
		menuItem.putClientProperty("style", ButtonDrawStyle.COMPACT_LIST);
		menuItem.addActionListener(this);
		popup.add(menuItem);

		addMouseListener(new PopupListener(popup));
	}

	public ArrayList<NavigationGraphicalEntity> getEntitySet() {
		return set;
	}

	public void setEntitySet(ArrayList<NavigationGraphicalEntity> set) {
		if (set == null)
			return;
		this.set = set;
		updateGUI();
	}

	private void updateGUI() {
		removeAll();
		if (set != null) {
			ArrayList<JComponent> right = new ArrayList<JComponent>();
			boolean first = true;
			for (NavigationGraphicalEntity ne : set) {
				if (ne instanceof StyleAware) {
					((StyleAware) ne).setButtonStyle(buttonStyle);
				}
				if (target == PanelTarget.NAVIGATION) {
					if (!first) {
						JLabel lbl = new JLabel("<html><small>&#9654;");
						lbl.setForeground(Color.GRAY);
						add(lbl);
					}
					add(ModelToGui.getNavigationButton(buttonStyle, ne, target, this, getTheOther(), graphPanel));
					first = false;
				} else {
					if (actionPanelRight != null && ne.isRightAligned())
						right.add(ModelToGui.getNavigationButton(buttonStyle, ne, target, getTheOther(), this, graphPanel));
					else
						add(ModelToGui.getNavigationButton(buttonStyle, ne, target, getTheOther(), this, graphPanel));
				}
			}
			if (actionPanelRight != null) {
				if (right.size() > 0) {
					actionPanelRight.setBackground(new Color(255, 220, 220));
					actionPanelRight.setLayout(TableLayout.getLayout(TableLayout.PREFERRED, TableLayout.PREFERRED));
					actionPanelRight.removeAll();
					actionPanelRight.add(TableLayout.getMultiSplit(right, TableLayout.PREFERRED, 6, 6, 5, 5), "0,0");
				} else {
					actionPanelRight.setLayout(TableLayout.getLayout(TableLayout.PREFERRED, TableLayout.PREFERRED));
					actionPanelRight.removeAll();
				}
			}
		}
		if (getParent() != null) {
			getParent().validate();
			getParent().repaint();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					getParent().validate();
					getParent().repaint();
				}
			});
		} else {
			validate();
			if (actionPanelRight != null)
				actionPanelRight.validate();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		int w = getWidth();
		int h = getHeight();

		// Paint a gradient from top to bottom
		GradientPaint gp;
		if (target == PanelTarget.NAVIGATION)
			gp = new GradientPaint(0, 0, new Color(240, 240, 240), 0, h, new Color(210, 230, 210));
		else {
			Color c2;
			c2 = getTabColor();

			gp = new GradientPaint(0, 0, new Color(250, 250, 250), 0, h, c2);
		}
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, w, h);
	}

	public static Color getTabColor() {
		Color c2;
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
	 * 
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
			updateGUI();
			theOther.buttonStyle = bds;
			theOther.updateGUI();
		}
	}

}
