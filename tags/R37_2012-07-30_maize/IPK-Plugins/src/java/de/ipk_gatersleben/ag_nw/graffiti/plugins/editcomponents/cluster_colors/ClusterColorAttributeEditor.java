/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ClusterColorAttributeEditor
					extends AbstractValueEditComponent {
	private JPanel jpanel;
	
	private int barCount = -1;
	
	public ClusterColorAttributeEditor(Displayable disp) {
		super(disp);
		if (disp instanceof ClusterColorParameter)
			setGUI((ClusterColorAttribute) disp.getValue(), false);
		else
			setGUI((ClusterColorAttribute) disp, false);
	}
	
	private void setGUI(ClusterColorAttribute cca, boolean showEmpty) {
		jpanel = new JPanel();
		
		double[][] size = new double[2][];
		barCount = cca.getDefinedClusterColorCount();
		
		if (barCount <= 0) {
			jpanel.setLayout(new GridLayout(1, 1));
			jpanel.add(new JLabel("no cluster-info"));
		} else {
			size[0] = new double[barCount];
			size[1] = new double[2];
			for (int x = 0; x < barCount; x++)
				size[0][x] = TableLayoutConstants.FILL;
			size[1][0] = TableLayoutConstants.PREFERRED; // TableLayoutConstants.FILL;
			size[1][1] = TableLayoutConstants.PREFERRED; // TableLayoutConstants.FILL;
			
			jpanel.setLayout(new TableLayout(size));
			ArrayList<Color> barCols = null;
			ArrayList<Color> barOutlineCols = null;
			try {
				barCols = cca.getClusterColors();
				barOutlineCols = cca.getClusterOutlineColors();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			}
			for (int i = 0; i < barCount; i++) {
				JLabel colButton = new JLabel(":");
				JLabel colButtonOutline = new JLabel(":");
				colButton.setMinimumSize(new Dimension(2, 8));
				colButton.setPreferredSize(new Dimension(10, colButton.getPreferredSize().height));
				colButtonOutline.setMinimumSize(new Dimension(2, 8));
				colButtonOutline.setPreferredSize(new Dimension(10, colButtonOutline.getPreferredSize().height));
				if (barCols != null && barCols.get(i) != null && !showEmpty) {
					colButton.setBackground(barCols.get(i));
					colButton.setForeground(barCols.get(i));
					colButton.setText("#");
				} else
					colButton.setText(EMPTY_STRING);
				addDefaultColorActionListenerAndAddBarInfo(colButton, true, i);
				colButton.putClientProperty("isBar", new Boolean(true));
				colButton.putClientProperty("barIndex", new Integer(i));
				
				if (barOutlineCols != null && barOutlineCols.get(i) != null && !showEmpty) {
					colButtonOutline.setBackground(barOutlineCols.get(i));
					colButtonOutline.setForeground(barOutlineCols.get(i));
					colButtonOutline.setText("#");
				} else
					colButtonOutline.setText(EMPTY_STRING);
				addDefaultColorActionListenerAndAddBarInfo(colButtonOutline, false, i);
				colButtonOutline.putClientProperty("isBar", new Boolean(false));
				colButtonOutline.putClientProperty("barIndex", new Integer(i));
				
				jpanel.add(colButton, i + ",0");
				
				jpanel.add(colButtonOutline, i + ",1");
			}
		}
		jpanel.setMaximumSize(new Dimension(2000, 40));
		jpanel.revalidate();
	}
	
	private void addDefaultColorActionListenerAndAddBarInfo(final JLabel colorButton, final boolean barColor, final int bar) {
		colorButton.setOpaque(true);
		colorButton.setBorder(BorderFactory.createRaisedBevelBorder());
		colorButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				JLabel src = (JLabel) e.getSource();
				Color c;
				if (barColor)
					c = JColorChooser.showDialog(MainFrame.getInstance(), "Select the (bar/line) color of dataset " + bar, src.getBackground());
				else
					c = JColorChooser.showDialog(MainFrame.getInstance(), "Select the outline-color (bar charts) of dataset " + bar, src.getBackground());
				if (c != null) {
					src.setBackground(c);
					src.setForeground(c);
					src.setText("#");
				}
			}
			
			public void mousePressed(MouseEvent arg0) {
			}
			
			public void mouseReleased(MouseEvent arg0) {
			}
			
			public void mouseEntered(MouseEvent arg0) {
				colorButton.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
			}
			
			public void mouseExited(MouseEvent arg0) {
				colorButton.setBorder(BorderFactory.createRaisedBevelBorder());
			}
		});
	}
	
	public JComponent getComponent() {
		return jpanel;
	}
	
	public void setEditFieldValue() {
		ClusterColorAttribute cca;
		if (getDisplayable() instanceof ClusterColorParameter)
			cca = (ClusterColorAttribute) getDisplayable().getValue();
		else
			cca = (ClusterColorAttribute) getDisplayable();
		for (int i = 0; i < jpanel.getComponentCount(); i++) {
			Object o = jpanel.getComponent(i);
			if (o instanceof JLabel) {
				JLabel jb = (JLabel) o;
				if (jb.getText().equals(EMPTY_STRING))
					continue;
				Boolean bar = (Boolean) jb.getClientProperty("isBar");
				Integer barIndex = (Integer) jb.getClientProperty("barIndex");
				if (bar.booleanValue())
					cca.setClusterColor(barIndex.intValue(), jb.getBackground());
				else
					cca.setClusterOutlineColor(barIndex.intValue(), jb.getBackground());
			}
		}
	}
	
	public void setValue() {
		ClusterColorAttribute cca;
		if (getDisplayable() instanceof ClusterColorParameter)
			cca = (ClusterColorAttribute) getDisplayable().getValue();
		else
			cca = (ClusterColorAttribute) getDisplayable();
		for (int i = 0; i < jpanel.getComponentCount(); i++) {
			Object o = jpanel.getComponent(i);
			if (o instanceof JLabel) {
				JLabel jb = (JLabel) o;
				if (jb.getText().equals(EMPTY_STRING))
					continue;
				Boolean bar = (Boolean) jb.getClientProperty("isBar");
				Integer barIndex = (Integer) jb.getClientProperty("barIndex");
				if (bar.booleanValue())
					cca.setClusterColor(barIndex.intValue(), jb.getBackground());
				else
					cca.setClusterOutlineColor(barIndex.intValue(), jb.getBackground());
			}
		}
		Graph g = (Graph) cca.getAttributable();
		if (g != null) {
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
				PajekClusterColor.executeClusterColoringOnGraph(g, cca);
			}
			Graph emptyGraph = new AdjListGraph();
			Graph clusterGraph = (Graph) AttributeHelper.getAttributeValue(g, "cluster", "clustergraph", emptyGraph, new AdjListGraph(), false);
			if (clusterGraph != emptyGraph) {
				PajekClusterColor.executeClusterColoringOnGraph(clusterGraph, cca);
			}
		}
	}
}
