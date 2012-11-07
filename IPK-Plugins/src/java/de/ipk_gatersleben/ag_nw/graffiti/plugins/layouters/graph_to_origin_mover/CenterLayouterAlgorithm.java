/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.AttributeHelper;
import org.ErrorMsg;
import org.SystemInfo;
import org.Vector2d;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;

import de.ipk_gatersleben.ag_nw.graffiti.AttributeConstants;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;

/**
 * DOCTODO: Include class header
 */
public class CenterLayouterAlgorithm
					extends AbstractAlgorithm
					implements ProvidesGeneralContextMenu, ActionListener {
	
	/**
	 * DOCTODO: Include method header
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getName() {
		return "Move Network to Upper-Left";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('1', SystemInfo.getAccelModifier());
	}
	
	/**
	 * DOCTODO: Include method header
	 * 
	 * @throws PreconditionException
	 *            DOCUMENT ME!
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	private static double min2(double a, double b) {
		if (a < b)
			return a;
		else
			return b;
	}
	
	/**
	 * DOCTODO: Include method header
	 */
	public void execute() {
		
		moveGraph(graph, getName(), true, 50, 50);
	}
	
	@SuppressWarnings("unchecked")
	public static void moveGraph(Graph graph, String nameOfOperation, boolean moveToTop, double offX, double offY) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		HashMap<CoordinateAttribute, Vector2d> bends2newPositions = new HashMap<CoordinateAttribute, Vector2d>();
		
		ArrayList<CoordinateAttribute> transformThese = new ArrayList<CoordinateAttribute>();
		
		for (Node n : graph.getNodes()) {
			if (AttributeHelper.isHiddenGraphElement(n))
				continue;
			CoordinateAttribute coA = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Vector2d size = AttributeHelper.getSize(n);
			CoordinateAttribute can = new CoordinateAttribute("id", coA.getX() - size.x / 2, coA.getY() - size.y / 2);
			transformThese.add(can);
		}
		for (Edge e : graph.getEdges()) {
			if (AttributeHelper.isHiddenGraphElement(e))
				continue;
			LinkedHashMapAttribute ha = null;
			try {
				ha = ((LinkedHashMapAttribute) e.getAttribute(AttributeConstants.BENDS));
			} catch (Exception err) {
				// empty
			}
			if (ha == null)
				continue;
			Map<?, ?> m = ha.getCollection();
			if (m == null)
				continue;
			for (Iterator<?> bi = m.entrySet().iterator(); bi.hasNext();) {
				// transform bends
				Map.Entry en = (java.util.Map.Entry) bi.next();
				CoordinateAttribute co = (CoordinateAttribute) en.getValue();
				transformThese.add(co);
			}
		}
		
		for (CoordinateAttribute ca : transformThese) {
			minX = min2(minX, ca.getX());
			minY = min2(minY, ca.getY());
		}
		
		for (Node n : graph.getNodes()) {
			CoordinateAttribute cn = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			if (moveToTop)
				nodes2newPositions.put(n, new Vector2d(cn.getX() - minX + offX, cn.getY() - minY + offY));
			else
				nodes2newPositions.put(n, new Vector2d(cn.getX() + offX, cn.getY() + offY));
		}
		
		for (Edge e : graph.getEdges()) {
			if (moveToTop)
				EdgeHelper.moveBends(e, -minX + offX, -minY + offY, bends2newPositions);
			else
				EdgeHelper.moveBends(e, offX, offY, bends2newPositions);
		}
		
		GraphHelper.applyUndoableNodeAndBendPositionUpdate(nodes2newPositions, bends2newPositions, nameOfOperation);
	}
	
	/**
	 * @deprecated Use {@link EdgeHelper#moveBends(Edge,double,double,HashMap<CoordinateAttribute, Vector2d>)} instead
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static void moveBends(Edge e, double moveX, double moveY, HashMap<CoordinateAttribute, Vector2d> bends2newPositions) {
		EdgeHelper.moveBends(e, moveX, moveY, bends2newPositions);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.ContextMenuPlugin#getCurrentContextMenuItem()
	 */
	public JMenuItem[] getCurrentContextMenuItem() {
		JMenuItem myMenuItem = new JMenuItem("Move Graph to Top-Left");
		myMenuItem.addActionListener(this);
		return new JMenuItem[] { myMenuItem };
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		GravistoService.getInstance().getMainFrame();
		GravistoService.getInstance().algorithmAttachData(this);
		try {
			this.check();
			this.execute();
		} catch (PreconditionException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		this.reset();
	}
}
