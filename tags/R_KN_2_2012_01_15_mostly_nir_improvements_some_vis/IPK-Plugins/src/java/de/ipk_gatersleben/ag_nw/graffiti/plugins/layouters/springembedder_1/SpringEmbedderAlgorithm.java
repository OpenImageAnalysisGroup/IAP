/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.06.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.springembedder_1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.graffiti.attributes.Attribute;
import org.graffiti.graph.AdjListEdge;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

/**
 * @author Christian Klukas
 *         Spring Embedder Algorithm. Example #1.
 */
public class SpringEmbedderAlgorithm extends AbstractAlgorithm {
	
	/**
	 * @author Christian Klukas
	 *         To change the template for this generated type comment go to
	 *         Window>Preferences>Java>Code Generation>Code and Comments
	 */
	
	class Vector2d {
		double x, y;
		
		public Vector2d(double initX, double initY) {
			x = initX;
			y = initY;
		}
	}
	
	/**
	 * stiffness of spring between two connected nodes,
	 * see Graph Drawing p. 308
	 */
	public double k1 = 1;
	
	/**
	 * strength of the electrical repulsion between all nodes,
	 * see Graph Drawing p. 308
	 */
	public double k2 = -90000;
	
	/**
	 * natural (zero energy) length of spring between two connected nodes,
	 * see Graph Drawing p. 308
	 */
	public double nat_l = 100;
	
	/**
	 * Rand-Absto�ung links und oben
	 */
	public double borderWidth = 150;
	
	/**
	 * Absto�ungs-Kraft direkt am Rand
	 */
	public double maxBorderForce = 100;
	
	/**
	 * Rand-Absto�ung verwenden, ja/nein
	 */
	public boolean borderForce = true;
	
	public boolean redraw = true;
	
	public boolean moveAll = true;
	
	private final String COORDSTR =
						GraphicAttributeConstants.GRAPHICS
											+ Attribute.SEPARATOR
											+ GraphicAttributeConstants.COORDINATE;
	
	/**
	 * Sets Menu Command Title
	 */
	public String getName() {
		// return "Springembedder 1";
		return null; // avoids listing in the plugin menu
	}
	
	/**
	 * Error Checking
	 */
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		if (graph == null) {
			errors.add("The graph instance may not be null.");
		}
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	private double getX(Node a) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(COORDSTR);
		return coA.getX();
	}
	
	private double getY(Node a) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(COORDSTR);
		return coA.getY();
	}
	
	private double getDistance(Node a, Node b) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(COORDSTR);
		CoordinateAttribute coB = (CoordinateAttribute) b.getAttribute(COORDSTR);
		
		return Math.sqrt(
							Math.pow(coA.getX() - coB.getX(), 2)
												+ Math.pow(coA.getY() - coB.getY(), 2));
	}
	
	private double getDiffX(Node a, Node b) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(COORDSTR);
		CoordinateAttribute coB = (CoordinateAttribute) b.getAttribute(COORDSTR);
		
		return coB.getX() - coA.getX();
	}
	
	private double getDiffY(Node a, Node b) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(COORDSTR);
		CoordinateAttribute coB = (CoordinateAttribute) b.getAttribute(COORDSTR);
		
		return coB.getY() - coA.getY();
	}
	
	private double borderForceX(double x) {
		if (x < borderWidth) {
			return Math.max(-maxBorderForce / borderWidth * x + maxBorderForce, 0);
		} else
			return 0;
	}
	
	private double borderForceY(double y) {
		if (y < borderWidth) {
			return Math.max(-maxBorderForce / borderWidth * y + maxBorderForce, 0);
		} else
			return 0;
	}
	
	private void doSpringEmbedder(
						int n,
						Vector<Node> graphNodes, // <Node>
			ArrayList<Vector2d> energyForNodes) { // <Vector2d>
	
		energyForNodes.clear();
		
		int i; // Index current node
		
		for (i = 0; i < n; i++) {
			Node myNode = (Node) graphNodes.get(i);
			
			double distance, distanceX, distanceY;
			
			Collection<Edge> tempCol = myNode.getEdges();
			Vector<Node> connectedNodes = new Vector<Node>(); // <Node>
			
			Iterator<Edge> tempIt = tempCol.iterator();
			AdjListEdge tempEdge;
			while (tempIt.hasNext()) {
				tempEdge = (AdjListEdge) tempIt.next();
				Node tempNode;
				if (myNode != tempEdge.getSource()) {
					tempNode = tempEdge.getSource();
				} else {
					tempNode = tempEdge.getTarget();
				}
				connectedNodes.add(tempNode);
			}
			
			double forceX = 0, forceY = 0;
			
			// Abstoßungskräfte zu restlichen Knoten
			
			int i2; // Index relative node
			if (graphNodes.size() > 0) {
				for (i2 = 0; i2 < graphNodes.size(); i2++) {
					if (i2 != i) {
						distance =
											getDistance(
																(Node) graphNodes.get(i),
																(Node) graphNodes.get(i2));
						distanceX =
											getDiffX(
																(Node) graphNodes.get(i),
																(Node) graphNodes.get(i2));
						distanceY =
											getDiffY(
																(Node) graphNodes.get(i),
																(Node) graphNodes.get(i2));
						if (distance > 0) {
							forceX += k2 / distance / distance * distanceX / distance;
							forceY += k2 / distance / distance * distanceY / distance;
						}
					}
				}
			}
			
			// Anziehungskräfte zwischen verbundenen Knoten
			if (!connectedNodes.isEmpty()) {
				for (i2 = 0; i2 < connectedNodes.size(); i2++) {
					distance =
										getDistance(
															(Node) graphNodes.get(i),
															(Node) connectedNodes.get(i2));
					distanceX =
										getDiffX(
															(Node) graphNodes.get(i),
															(Node) connectedNodes.get(i2));
					distanceY =
										getDiffY(
															(Node) graphNodes.get(i),
															(Node) connectedNodes.get(i2));
					
					if (distance > 0) {
						forceX += k1 * (distance - nat_l) * distanceX / distance;
						forceY += k1 * (distance - nat_l) * distanceY / distance;
					}
				}
			}
			
			if (borderForce) {
				forceX += borderForceX(getX((Node) graphNodes.get(i)));
				forceY += borderForceY(getY((Node) graphNodes.get(i)));
			}
			
			forceX /= 4; // n/3;
			forceY /= 4; // n/3;
			
			if (!moveAll) {
				// moveNode(temperature_max_move, energyForNodes, myNodes, i);
			}
			
			Vector2d forceVec = new Vector2d(forceX, forceY);
			energyForNodes.add(forceVec);
		}
	}
	
	/**
	 * Layout Algorithm
	 */
	public void execute() {
		// View theView = session.getActiveView();
		
		double temperature_max_move = 100;
		// Math.sqrt(viewHeight * viewHeight + viewWidth * viewWidth);
		double temp_alpha = 0.97; // 0.95 ... 0.995
		
		int n = graph.getNumberOfNodes();
		
		ArrayList<Vector2d> energyForNodes = new ArrayList<Vector2d>(); // <Vector2d>
		energyForNodes.ensureCapacity(n);
		
		Vector<Node> myNodes = new Vector<Node>();
		
		for (Iterator<Node> it = graph.getNodesIterator(); it.hasNext();) {
			Node tempN = (Node) it.next();
			myNodes.add(tempN);
		}
		
		double moveRun = 0; // Summe Bewegungsvektoren, wenn
		// keine Bewegung erfolgt, kann Abbruch erfolgen
		
		int runCount = 0;
		
		do {
			
			runCount += 1;
			
			moveRun = 0;
			
			doSpringEmbedder(n, myNodes, energyForNodes);
			
			int i;
			for (i = 0; i < n; i++) {
				if (moveAll) {
					moveNode(temperature_max_move, energyForNodes, myNodes, i);
				}
				moveRun += 1;
			}
			// if (redraw)
			// theView.completeRedraw();
			temperature_max_move *= temp_alpha;
		} while ((temperature_max_move > 1) && (moveRun > 1));
	}
	
	private void moveNode(
						double temperature_max_move,
						ArrayList<Vector2d> energyForNodes,
						Vector<Node> myNodes,
						int i) {
		Vector2d moveVec = (Vector2d) energyForNodes.get(i);
		
		CoordinateAttribute cn =
							(CoordinateAttribute) ((Node) myNodes.get(i)).getAttribute(
												COORDSTR);
		
		double l = Math.sqrt(moveVec.x * moveVec.x + moveVec.y * moveVec.y);
		
		if (l > temperature_max_move) {
			moveVec.x = moveVec.x / l * temperature_max_move;
			moveVec.y = moveVec.y / l * temperature_max_move;
		}
		
		cn.setCoordinate(cn.getX() + moveVec.x, cn.getY() + moveVec.y);
	}
	
	@Override
	public Parameter[] getParameters() {
		
		// UserPrefs mySettings=new UserPrefs("SpringEmbedderPlugin");
		DoubleParameter k1Param =
							new DoubleParameter(
												"k1_1",
												"Stiffness of spring between two connected nodes");
		// k1=mySettings.getDouble(Sk1);
		k1Param.setDouble(k1);
		
		DoubleParameter k2Param =
							new DoubleParameter(
												"k2_-90.000",
												"Strength of the electrical repulsion between all nodes");
		
		// k2=mySettings.getDouble(Sk2);
		k2Param.setDouble(k2);
		
		DoubleParameter nat_l_Param =
							new DoubleParameter(
												"Ziel-Kantenl�nge_100",
												"Natural (zero energy) length of spring between two connected nodes");
		// nat_l=mySettings.getDouble(Snat_l);
		nat_l_Param.setDouble(nat_l);
		
		BooleanParameter borderForce_Param =
							new BooleanParameter(borderForce, "Rand-Absto�ung_true", "");
		
		DoubleParameter borderWidth_Param =
							new DoubleParameter("Rand-Abstand_150", "Einflu�bereich des Randes");
		// borderWidth=mySettings.getDouble(SborderWidth);
		borderWidth_Param.setDouble(borderWidth);
		
		DoubleParameter randForce_Param =
							new DoubleParameter("Rand-Kraft_100", "Abso�ungskraft direkt am Rand");
		// maxBorderForce=mySettings.getDouble(SmaxBorderForce);
		randForce_Param.setDouble(maxBorderForce);
		
		BooleanParameter redraw_Param =
							new BooleanParameter(redraw, "Redraw_true", "");
		// redraw=mySettings.getBool(Sredraw);
		
		return new Parameter[] {
							k1Param,
							k2Param,
							nat_l_Param,
							borderForce_Param,
							borderWidth_Param,
							randForce_Param,
							redraw_Param };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		
		// UserPrefs mySettings=new UserPrefs(SappName);
		
		k1 = ((DoubleParameter) params[0]).getDouble().doubleValue();
		// mySettings.setPref(Sk1, new Double(k1).toString());
		k2 = ((DoubleParameter) params[1]).getDouble().doubleValue();
		// mySettings.setPref(Sk2, new Double(k2).toString());
		nat_l = ((DoubleParameter) params[2]).getDouble().doubleValue();
		// mySettings.setPref(Snat_l, new Double(nat_l).toString());
		borderForce =
							new Boolean(((BooleanParameter) params[3]).getValue().toString())
												.booleanValue();
		// mySettings.setPref(SborderForce, new Boolean(borderForce).toString());
		
		borderWidth = ((DoubleParameter) params[4]).getDouble().doubleValue();
		// mySettings.setPref(SborderWidth, new Double(borderWidth).toString());
		
		maxBorderForce = ((DoubleParameter) params[5]).getDouble().doubleValue();
		// mySettings.setPref(SmaxBorderForce,
		// new Double(maxBorderForce).toString());
		
		redraw =
							new Boolean(((BooleanParameter) params[6]).getValue().toString())
												.booleanValue();
		// mySettings.setPref(Sredraw,
		// new Boolean(redraw).toString());
	}
}
