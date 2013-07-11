/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyScatterBlock {
	
	private int maxX = 0;
	private int maxY = 0;
	private boolean returned = false;
	
	private HashMap<String, JComponent> chartPanels = new HashMap<String, JComponent>();
	private HashMap<Integer, String> descriptionX = new HashMap<Integer, String>();
	private HashMap<Integer, String> descriptionY = new HashMap<Integer, String>();
	
	private boolean symetricScatterPanel;
	private int fontSize;
	
	public MyScatterBlock(boolean symetricScatterPanel, int fontSize) {
		this.symetricScatterPanel = symetricScatterPanel;
		this.fontSize = fontSize;
	}
	
	/**
	 * @param chartPanel
	 *           The chart panel (probably a scatter plot)
	 * @param x
	 *           The position in the scatter block - X
	 * @param y
	 *           The position in the scatter block - Y
	 * @param descX
	 *           The description of the X axis.
	 * @param descY
	 *           The description of the Y axis.
	 */
	public void addChartPanel(JComponent chartPanel, int x, int y, String descX, String descY) {
		assert x > 0;
		assert y > 0;
		descriptionX.put(new Integer(x), descX);
		descriptionY.put(new Integer(y), descY);
		chartPanels.put(x + "§" + y, chartPanel);
		if (x > maxX)
			maxX = x;
		if (y > maxY)
			maxY = y;
	}
	
	/**
	 * Returns the scatter block as a component.
	 * This method can only be called once, as components are not allowed
	 * to be used twice in Java GUI programming (IMHO).
	 * 
	 * @return The scatter block with the charts that where added with <code>addChartPanel</code>.
	 */
	public JComponent getChartPanel() {
		if (returned) {
			return new JLabel("Internal Error: Data already returned.");
		} else {
			JPanel result = new JPanel();
			double[][] size = new double[2][];
			size[0] = new double[maxX];
			size[1] = new double[maxY];
			for (int x = 0; x < maxX; x++)
				size[0][x] = TableLayoutConstants.FILL;
			for (int y = 0; y < maxY; y++)
				size[1][y] = TableLayoutConstants.FILL;
			result.setLayout(new TableLayout(size));
			for (int x = 0; x < maxX; x++)
				for (int y = 0; y < maxY; y++) {
					JComponent c = chartPanels.get((x + 1) + "§" + (y + 1));
					if (x == y && symetricScatterPanel) { // && c==null
						String lbl = descriptionX.get(new Integer(x + 1));
						c = new JLabel(lbl);
						((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
						((JLabel) c).setOpaque(true);
						((JLabel) c).setBackground(getBackCol());
						c.setToolTipText(lbl);
						c.setFont(new Font(c.getFont().getName(), 0, fontSize));
					}
					if (c == null)
						c = new JLabel("n/a");
					// ((JComponent)c).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
					result.add(c, x + "," + y); // new JLabel(x+","+y)
				}
			returned = true;
			result.revalidate();
			if (maxX == maxY && maxX == 2 && symetricScatterPanel) {
				result.removeAll();
				int x = 0;
				int y = 1;
				return chartPanels.get((x + 1) + "§" + (y + 1));
			}
			result.setBorder(BorderFactory.createRaisedBevelBorder());
			return result;
		}
	}
	
	public static Color getBackCol() {
		return Color.WHITE;
		// new Color(250, 250, 250);
	}
}
