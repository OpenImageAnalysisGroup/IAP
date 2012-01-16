package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn;

import java.awt.Color;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.HelperClass;
import org.LabelFrameSetting;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugins.attributecomponents.simplelabel.LabelComponent;
import org.graffiti.plugins.editcomponents.defaults.EdgeArrowShapeEditComponent;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.views.defaults.EllipseNodeShape;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;
import org.graffiti.plugins.views.defaults.RectangularNodeShape;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ComplexShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.DoubleEllipseShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiEllipseShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiNucleicAcidFeatureShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiRectangleShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.NucleicAcidFeatureShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ObservableShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.PertubationShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.SourceSinkShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagDownShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagLeftShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagRightShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagUpShape;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TransitionShape;

public class SBGNgraphHelper implements HelperClass {
	
	public static void addItemOrSetStyle(Node n, SBGNitem item,
						String label, String info1, String info2, String state1, String state2,
						boolean cloneMarker, String cloneMarkerText,
						boolean setStyle) {
		int defWidth = 120;
		int defHeight = 60;
		double defLineWidth = 2d;
		
		if (!cloneMarker)
			cloneMarkerText = null;
		
		Node nn;
		
		Class<?> shape;
		switch (item) {
			case UnspecifiedEntityNode:
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, EllipseNodeShape.class.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, true);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case SimpleChemical:
				if (!setStyle)
					AttributeHelper.setSize(n, defHeight, defHeight);
				AttributeHelper.setShape(n, EllipseNodeShape.class.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, true);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case MacroMolecule:
				if (!setStyle)
					AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, RectangleNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 15);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case GeneticEntity:
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, NucleicAcidFeatureShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 15);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case MultimerMacrolecule:
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, MultiRectangleShape.class.getCanonicalName());
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offX", 10);
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offY", 10);
				AttributeHelper.setRoundedEdges(n, 15);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case MultimerGeneticEntity:
				AttributeHelper.setSize(n, defWidth, defHeight - 10);
				AttributeHelper.setShape(n, MultiNucleicAcidFeatureShape.class.getCanonicalName());
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offX", 10);
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offY", 10);
				AttributeHelper.setRoundedEdges(n, 15);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case MultimerSimpleChemical:
				AttributeHelper.setSize(n, defHeight, defHeight);
				AttributeHelper.setShape(n, MultiEllipseShape.class.getCanonicalName());
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offX", 10);
				AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, "offY", 10);
				AttributeHelper.setRoundedEdges(n, 15);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, true);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case SourceSink:
				AttributeHelper.setSize(n, defHeight, defHeight);
				AttributeHelper.setShape(n, SourceSinkShape.class.getCanonicalName());
				break;
			case TagRight:
				shape = TagRightShape.class;
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, shape.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case TagLeft:
				shape = TagLeftShape.class;
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, shape.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case TagUp:
				shape = TagUpShape.class;
				AttributeHelper.setSize(n, defHeight, defWidth);
				AttributeHelper.setShape(n, shape.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, true);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case TagDown:
				shape = TagDownShape.class;
				AttributeHelper.setSize(n, defHeight, defWidth);
				AttributeHelper.setShape(n, shape.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, true);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case Observable:
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, ObservableShape.class.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case Pertubation:
				AttributeHelper.setSize(n, defWidth, defHeight);
				AttributeHelper.setShape(n, PertubationShape.class.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, cloneMarkerText, false);
				setClone(n, cloneMarker, cloneMarkerText);
				break;
			case Transition:
				if (setStyle) {
					nn = checkEmbedTransitionNode(n);
					if (nn != null)
						n = nn;
				}
				AttributeHelper.setSize(n, defHeight / 3, defHeight / 3);
				AttributeHelper.setShape(n, TransitionShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 0);
				AttributeHelper.setLabel(n, "");
				break;
			case Omitted:
				if (setStyle) {
					nn = checkEmbedTransitionNode(n);
					if (nn != null)
						n = nn;
				}
				AttributeHelper.setSize(n, defHeight / 3, defHeight / 3);
				AttributeHelper.setShape(n, RectangularNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 0);
				AttributeHelper.setLabel(n, "<html><b>&#92;&#92;");
				break;
			case Uncertain:
				if (setStyle) {
					nn = checkEmbedTransitionNode(n);
					if (nn != null)
						n = nn;
				}
				AttributeHelper.setSize(n, defHeight / 3, defHeight / 3);
				AttributeHelper.setShape(n, RectangularNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 0);
				AttributeHelper.setLabel(n, "<html><b>?");
				break;
			case Associaction:
				AttributeHelper.setSize(n, defHeight / 3, defHeight / 3);
				AttributeHelper.setShape(n, EllipseNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 0);
				AttributeHelper.setFillColor(n, Color.BLACK);
				break;
			case Dissociation:
				AttributeHelper.setSize(n, defHeight / 3, defHeight / 3);
				AttributeHelper.setShape(n, DoubleEllipseShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 3);
				break;
			case AND:
				AttributeHelper.setSize(n, defHeight * 2 / 3, defHeight * 2 / 3);
				AttributeHelper.setShape(n, "oval");
				AttributeHelper.setLabel(n, "AND");
				AttributeHelper.setRoundedEdges(n, 0);
				break;
			case OR:
				AttributeHelper.setSize(n, defHeight * 2 / 3, defHeight * 2 / 3);
				AttributeHelper.setShape(n, "oval");
				AttributeHelper.setLabel(n, "OR");
				AttributeHelper.setRoundedEdges(n, 0);
				break;
			case NOT:
				AttributeHelper.setSize(n, defHeight * 2 / 3, defHeight * 2 / 3);
				AttributeHelper.setShape(n, "oval");
				AttributeHelper.setLabel(n, "NOT");
				AttributeHelper.setRoundedEdges(n, 0);
				break;
			case Complex:
				AttributeHelper.setSize(n, defWidth * 2, defHeight * 2);
				AttributeHelper.setShape(n, ComplexShape.class.getCanonicalName());
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, null, false);
				AttributeHelper.setBorderWidth(n, 2);
				break;
			case Compartment:
				AttributeHelper.setSize(n, defWidth * 2, defHeight * 2);
				AttributeHelper.setShape(n, RectangularNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 55);
				AttributeHelper.setBorderWidth(n, 8); // AttributeHelper.setBorderWidth(n, 15);
				// AttributeHelper.setOutlineColor(n, new Color(230, 230, 255));
				AttributeHelper.setOutlineColor(n, Color.BLACK);
				// AttributeHelper.setOutlineColor(n, Color.LIGHT_GRAY);
				if (!setStyle)
					setLabels(label, info1, info2, state1, state2, n, null, false);
				AttributeHelper.setLabelAlignment(-1, n, AlignmentSetting.INSIDETOP);
				break;
			case Submap:
				if (!setStyle)
					AttributeHelper.setSize(n, defWidth * 2, defHeight * 2);
				AttributeHelper.setShape(n, RectangularNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 2);
				if (!setStyle)
					AttributeHelper.setLabel(n, label);
				AttributeHelper.setBorderWidth(n, 2);
				break;
		}
		if (n != null) {
			if (item != SBGNitem.Complex && item != SBGNitem.Compartment && item != SBGNitem.Submap)
				AttributeHelper.setFrameThickNess(n, defLineWidth);
			if (item != SBGNitem.Compartment)
				AttributeHelper.setOutlineColor(n, Color.BLACK);
			if (item != SBGNitem.Associaction)
				AttributeHelper.setFillColor(n, Color.WHITE);
			AttributeHelper.setAttribute(n, "sbgn", "role", item.name());
			final Node fn = n;
			if (!setStyle)
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						GraphHelper.clearSelection();
						GraphHelper.selectGraphElement(fn);
					}
				});
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Node checkEmbedTransitionNode(Node n) {
		Node result = null;
		String role = (String) AttributeHelper.getAttributeValue(n, "sbgn", "role", "", "");
		if (role.equals(SBGNitem.MacroMolecule.name()) || role.equals(SBGNitem.MultimerMacrolecule.name())) {
			// embed transition node
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			pos.x -= 20;
			pos.x -= AttributeHelper.getWidth(n) / 2;
			Graph g = n.getGraph();
			Node transition = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x, pos.y));
			for (Edge e : n.getEdges()) {
				if (e.getSource() == n && e.getTarget() != n)
					g.addEdgeCopy(e, transition, e.getTarget());
				else
					if (e.getSource() != n && e.getTarget() == n)
						g.addEdgeCopy(e, e.getSource(), transition);
					else
						if (e.getSource() == n && e.getTarget() == n)
							g.addEdgeCopy(e, transition, transition);
			}
			g.deleteAll((Collection) n.getEdges());
			Edge ne = g.addEdge(n, transition, true);
			setEdgeStyle(ne, SBGNarc.Catalysis, null, null, false);
			result = transition;
		}
		return result;
	}
	
	private static void setClone(Node n, boolean cloneMarker, String cloneText) {
		if (cloneMarker) {
			AttributeHelper.setLabel(1, n, cloneText, null, null);
			if (cloneText != null) {
				AttributeHelper.setLabelColor(1, n, Color.WHITE);
				AttributeHelper.setLabelAlignment(1, n, AlignmentSetting.INSIDEBOTTOM);
			}
			AttributeHelper.setAttribute(n, GraphicAttributeConstants.GRAPHICS, GraphicAttributeConstants.GRADIENT, -0.35d);
		}
	}
	
	private static void setLabels(String label, String info1, String info2,
						String state1, String state2, Node n, String cloneMarkerText, boolean isRound) {
		AttributeHelper.setLabel(n, label);
		if (info1 != null) {
			AttributeHelper.setLabel(2, n, info1, null, null);
			AttributeHelper.setLabelFrameStyle(2, n, LabelFrameSetting.RECTANGLE);
		}
		if (info2 != null) {
			AttributeHelper.setLabel(3, n, info2, null, null);
			AttributeHelper.setLabelFrameStyle(3, n, LabelFrameSetting.RECTANGLE);
		}
		if (state1 != null) {
			AttributeHelper.setLabel(4, n, state1, null, null);
			AttributeHelper.setLabelFrameStyle(4, n, LabelFrameSetting.ELLIPSE);
		}
		if (state2 != null) {
			AttributeHelper.setLabel(5, n, state2, null, null);
			AttributeHelper.setLabelFrameStyle(5, n, LabelFrameSetting.ELLIPSE);
		}
		if (info1 != null && info1.length() > 0 && info2 != null && info2.length() > 0) {
			if (info1 != null)
				AttributeHelper.setLabelAlignment(2, n, AlignmentSetting.BORDER_TOP_LEFT);
			if (info2 != null)
				AttributeHelper.setLabelAlignment(3, n, AlignmentSetting.BORDER_TOP_RIGHT);
		} else {
			if (isRound) {
				if (info1 != null)
					AttributeHelper.setLabelAlignment(2, n, AlignmentSetting.BORDER_TOP_CENTER);
				if (info2 != null)
					AttributeHelper.setLabelAlignment(3, n, AlignmentSetting.BORDER_TOP_CENTER);
			} else {
				if (info1 != null)
					AttributeHelper.setLabelAlignment(2, n, AlignmentSetting.BORDER_TOP_LEFT);
				if (info2 != null)
					AttributeHelper.setLabelAlignment(3, n, AlignmentSetting.BORDER_TOP_LEFT);
			}
		}
		if (state1 != null && state1.length() > 0 && state2 != null && state2.length() > 0) {
			if (isRound) {
				if (state1 != null)
					AttributeHelper.setLabelAlignment(4, n, AlignmentSetting.BORDER_LEFT_CENTER);
				if (state2 != null)
					AttributeHelper.setLabelAlignment(5, n, AlignmentSetting.BORDER_RIGHT_CENTER);
			} else {
				if (state1 != null)
					AttributeHelper.setLabelAlignment(4, n, AlignmentSetting.BORDER_BOTTOM_LEFT);
				if (state2 != null)
					AttributeHelper.setLabelAlignment(5, n, AlignmentSetting.BORDER_BOTTOM_RIGHT);
			}
		} else {
			if (cloneMarkerText == null) {
				if (state1 != null)
					AttributeHelper.setLabelAlignment(4, n, AlignmentSetting.BORDER_BOTTOM_CENTER);
				if (state2 != null)
					AttributeHelper.setLabelAlignment(5, n, AlignmentSetting.BORDER_BOTTOM_CENTER);
			} else {
				if (state1 != null)
					AttributeHelper.setLabelAlignment(4, n, AlignmentSetting.BORDER_RIGHT_CENTER);
				if (state2 != null)
					AttributeHelper.setLabelAlignment(5, n, AlignmentSetting.BORDER_RIGHT_CENTER);
			}
		}
	}
	
	static Vector2d getTargetPositionForNewElement(Graph g) {
		Vector2d lastP = MegaMoveTool.getLastMovementPosition();
		return new Vector2d(lastP.x + 60, lastP.y + 60);
	}
	
	public static void setEdgeStyle(Object src, Collection<Edge> edges, SBGNarc item,
						String consumption, String production, boolean reversible) {
		if (edges == null || edges.size() <= 0)
			return;
		try {
			edges.iterator().next().getGraph().getListenerManager().transactionStarted(src);
			for (Edge e : edges) {
				setEdgeStyle(e, item, consumption, production, reversible);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} finally {
			edges.iterator().next().getGraph().getListenerManager().transactionFinished(src);
		}
	}
	
	public static void setAutoEdgeStyle(Collection<Edge> edges,
						String consumption, String production, boolean reversible) {
		if (edges == null)
			return;
		for (Edge e : edges) {
			setAutoEdgeStyle(e, consumption, production, reversible);
		}
	}
	
	private static void setAutoEdgeStyle(Edge edge, String consumption,
						String production, boolean reversible) {
		// String sourceRole = (String)AttributeHelper.getAttributeValue(edge.getSource(), "sbgn", "role", "", "", false);
		String targetRole = (String) AttributeHelper.getAttributeValue(edge.getTarget(), "sbgn", "role", "", "", false);
		if (targetRole.equals(SBGNitem.TagDown.name()) || targetRole.equals(SBGNitem.TagUp.name())
							|| targetRole.equals(SBGNitem.TagLeft.name()) || targetRole.equals(SBGNitem.TagRight.name())) {
			setEdgeStyle(edge, SBGNarc.Equivalence, consumption, production, reversible);
		}
		if (targetRole.equals(SBGNitem.Submap)) {
			setEdgeStyle(edge, SBGNarc.Equivalence, consumption, production, reversible);
		}
	}
	
	private static void setEdgeStyle(Edge edge, SBGNarc item,
						String consumption, String production, boolean reversible) {
		double bendFaktor = 1d;
		int arrowSize = 15;
		switch (item) {
			case ConsumptionProductionHorOrVert:
				Node relevantNode = edge.getSource();
				String targetRole = (String) AttributeHelper.getAttributeValue(edge.getTarget(), "sbgn", "role", "", "", false);
				if (targetRole.equals(SBGNitem.Transition.name()) || targetRole.equals(SBGNitem.Omitted.name())
									|| targetRole.equals(SBGNitem.Uncertain.name())) {
					relevantNode = edge.getTarget();
				}
				String srcRole = (String) AttributeHelper.getAttributeValue(edge.getSource(), "sbgn", "role", "", "", false);
				if (srcRole.equals(SBGNitem.Transition.name()) || srcRole.equals(SBGNitem.Omitted.name())
									|| srcRole.equals(SBGNitem.Uncertain.name())) {
					relevantNode = edge.getSource();
				}
				String setting = LabelComponent.getBestAutoOutsideSetting(relevantNode);
				if (setting != null && (setting.equals(GraphicAttributeConstants.LEFT) || setting.equals(GraphicAttributeConstants.RIGHT))) {
					SBGNgraphHelper.setEdgeStyle(edge, SBGNarc.ConsumptionProductionVert, "", "", reversible);
				} else {
					SBGNgraphHelper.setEdgeStyle(edge, SBGNarc.ConsumptionProductionHor, "", "", reversible);
				}
				break;
			case ConsumptionProductionHor:
				arrowSize = 10;
				Vector2d targetPos = AttributeHelper.getPositionVec2d(edge.getTarget());
				Vector2d srcPos = AttributeHelper.getPositionVec2d(edge.getSource());
				if (targetPos.x < srcPos.x)
					bendFaktor = -bendFaktor;
				
				targetRole = (String) AttributeHelper.getAttributeValue(edge.getTarget(), "sbgn", "role", "", "", false);
				if (targetRole.equals(SBGNitem.Transition.name()) || targetRole.equals(SBGNitem.Omitted.name())
									|| targetRole.equals(SBGNitem.Uncertain.name())) {
					if (reversible) {
						AttributeHelper.setArrowhead(edge, false);
						AttributeHelper.setArrowtail(edge, EdgeArrowShapeEditComponent.standardArrow);
						AttributeHelper.setArrowSize(edge, arrowSize);
					} else {
						AttributeHelper.setArrowtail(edge, false);
						AttributeHelper.setArrowhead(edge, false);
					}
					AttributeHelper.setLabelConsumption(edge, consumption);
					AttributeHelper.setLabelProduction(edge, null);
					
					Vector2d targetSize = AttributeHelper.getSize(edge.getTarget());
					AttributeHelper.removeEdgeBends(edge);
					AttributeHelper.addEdgeBend(edge, targetPos.x - targetSize.x * bendFaktor, targetPos.y);
					AttributeHelper.setEdgeBendStyle(edge, "Poly");
				} else {
					AttributeHelper.setArrowtail(edge, false);
					AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.standardArrow);
					AttributeHelper.setArrowSize(edge, arrowSize);
					AttributeHelper.setLabelProduction(edge, production);
					AttributeHelper.setLabelConsumption(edge, null);
				}
				srcRole = (String) AttributeHelper.getAttributeValue(edge.getSource(), "sbgn", "role", "", "", false);
				if (srcRole.equals(SBGNitem.Transition.name()) || srcRole.equals(SBGNitem.Omitted.name())
									|| srcRole.equals(SBGNitem.Uncertain.name())) {
					Vector2d srcSize = AttributeHelper.getSize(edge.getSource());
					AttributeHelper.removeEdgeBends(edge);
					AttributeHelper.addEdgeBend(edge, srcPos.x + srcSize.x * bendFaktor, srcPos.y);
					AttributeHelper.setEdgeBendStyle(edge, "Poly");
				}
				break;
			case ConsumptionProductionVert:
				arrowSize = 10;
				targetRole = (String) AttributeHelper.getAttributeValue(edge.getTarget(), "sbgn", "role", "", "", false);
				targetPos = AttributeHelper.getPositionVec2d(edge.getTarget());
				srcPos = AttributeHelper.getPositionVec2d(edge.getSource());
				if (targetPos.y < srcPos.y)
					bendFaktor = -bendFaktor;
				
				if (targetRole.equals(SBGNitem.Transition.name()) || targetRole.equals(SBGNitem.Omitted.name())
									|| targetRole.equals(SBGNitem.Uncertain.name())) {
					if (reversible) {
						AttributeHelper.setArrowhead(edge, false);
						AttributeHelper.setArrowtail(edge, EdgeArrowShapeEditComponent.standardArrow);
						AttributeHelper.setArrowSize(edge, arrowSize);
					} else {
						AttributeHelper.setArrowtail(edge, false);
						AttributeHelper.setArrowhead(edge, false);
					}
					AttributeHelper.setLabelConsumption(edge, consumption);
					AttributeHelper.setLabelProduction(edge, null);
					
					Vector2d targetSize = AttributeHelper.getSize(edge.getTarget());
					AttributeHelper.removeEdgeBends(edge);
					AttributeHelper.addEdgeBend(edge, targetPos.x, targetPos.y - targetSize.y * bendFaktor);
					AttributeHelper.setEdgeBendStyle(edge, "Poly");
				} else {
					AttributeHelper.setArrowtail(edge, false);
					AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.standardArrow);
					AttributeHelper.setArrowSize(edge, arrowSize);
					AttributeHelper.setLabelProduction(edge, production);
					AttributeHelper.setLabelConsumption(edge, null);
				}
				srcRole = (String) AttributeHelper.getAttributeValue(edge.getSource(), "sbgn", "role", "", "", false);
				if (srcRole.equals(SBGNitem.Transition.name()) || srcRole.equals(SBGNitem.Omitted.name())
									|| srcRole.equals(SBGNitem.Uncertain.name())) {
					Vector2d srcSize = AttributeHelper.getSize(edge.getSource());
					AttributeHelper.removeEdgeBends(edge);
					AttributeHelper.addEdgeBend(edge, srcPos.x, srcPos.y + srcSize.y * bendFaktor);
					AttributeHelper.setEdgeBendStyle(edge, "Poly");
				}
				break;
			case Modulation:
				AttributeHelper.setArrowtail(edge, false);
				AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.thinDiamondArrow);
				AttributeHelper.setArrowSize(edge, arrowSize);
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				break;
			case Stimulation:
				AttributeHelper.setArrowtail(edge, false);
				AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.thinStandardArrow);
				AttributeHelper.setArrowSize(edge, arrowSize);
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				break;
			case Catalysis:
				AttributeHelper.setArrowtail(edge, false);
				AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.thinCircleArrow);
				AttributeHelper.setArrowSize(edge, arrowSize);
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				break;
			case Inhibition:
				AttributeHelper.setArrowtail(edge, false);
				AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.inhibitorArrow);
				AttributeHelper.setArrowSize(edge, arrowSize);
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				break;
			case Trigger:
				AttributeHelper.setArrowtail(edge, false);
				AttributeHelper.setArrowhead(edge, EdgeArrowShapeEditComponent.triggerArrow);
				AttributeHelper.setArrowSize(edge, arrowSize);
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				break;
			case Logic:
			case Equivalence:
				AttributeHelper.setLabelConsumption(edge, null);
				AttributeHelper.setLabelProduction(edge, null);
				srcRole = (String) AttributeHelper.getAttributeValue(edge.getSource(), "sbgn", "role", "", "", false);
				if (srcRole.equals(SBGNitem.Associaction.name()) || srcRole.equals(SBGNitem.Dissociation.name())
									|| srcRole.equals(SBGNitem.AND.name()) || srcRole.equals(SBGNitem.OR.name())
									|| srcRole.equals(SBGNitem.NOT.name())) {
					AttributeHelper.setArrowtail(edge, false);
					AttributeHelper.setArrowhead(edge, true);
					AttributeHelper.setArrowSize(edge, arrowSize);
				} else {
					AttributeHelper.setArrowtail(edge, false);
					AttributeHelper.setArrowhead(edge, false);
				}
				break;
		}
		AttributeHelper.setOutlineColor(edge, Color.BLACK);
		AttributeHelper.setBorderWidth(edge, 2);
		AttributeHelper.setAttribute(edge, "sbgn", "role", item.name());
		String targetRole = (String) AttributeHelper.getAttributeValue(edge.getTarget(), "sbgn", "role", "", "", false);
		if (targetRole.equals(SBGNitem.Associaction) || targetRole.equals(SBGNitem.Dissociation)
							|| targetRole.equals(SBGNitem.AND) || targetRole.equals(SBGNitem.OR)
							|| targetRole.equals(SBGNitem.NOT)) {
			AttributeHelper.setArrowtail(edge, false);
			AttributeHelper.setArrowhead(edge, false);
		}
	}
	
	public static String getRole(GraphElement ge) {
		return (String) AttributeHelper.getAttributeValue(ge, "sbgn", "role", "", "", false);
	}
}
