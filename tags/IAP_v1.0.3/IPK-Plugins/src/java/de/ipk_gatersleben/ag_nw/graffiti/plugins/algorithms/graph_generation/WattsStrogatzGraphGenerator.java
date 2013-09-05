package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.CenterLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class WattsStrogatzGraphGenerator extends AbstractAlgorithm {
	
	@Override
	public String getCategory() {
		return "Elements";
	}
	
	private int numberOfNodes = 5;
	private int initDegree = 4;
	private double p = 0.5;
	private boolean label = true;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new IntegerParameter(numberOfNodes, "Number of nodes", "Number of nodes"),
				new IntegerParameter(initDegree, "Mean degree K", "Initial node degree"),
				new DoubleParameter(p, "Swap probability", "Edge swap probability"),
				new BooleanParameter(label, "Add node label", "If enabled, each node will be labeld (1,2,3,...)"), };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		numberOfNodes = ((IntegerParameter) params[i++]).getInteger();
		initDegree = ((IntegerParameter) params[i++]).getInteger();
		p = ((DoubleParameter) params[i++]).getDouble();
		label = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (numberOfNodes < 1)
			throw new PreconditionException("Number of nodes needs to be at least 1");
		if (initDegree < 2)
			throw new PreconditionException("Initial node degree should at least be 2");
		if (initDegree > numberOfNodes - 1)
			throw new PreconditionException("Initial node degree needs to be smaller than number of nodes minus one");
		if (p < 0)
			throw new PreconditionException("Negative edge-swap-probability is not supported");
		if (p > 1)
			throw new PreconditionException("Edge-swap-probability greater than 1 (100%) is not supported");
	}
	
	@Override
	public String getName() {
		return "Generate Watts and Strogatz random graph";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
				"Create small-world random graph according to Watts and Strogatz model.";
	}
	
	@Override
	public void execute() {
		
		BackgroundTaskHelper.issueSimpleTask("Generating random graph", "Generating random graph", new Runnable() {
			@Override
			public void run() {
				try {
					final Graph rdg = createGraph(numberOfNodes, label, initDegree, p);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainFrame.getInstance().showGraph(rdg, new ActionEvent(this, 1, getName()));
							GraphHelper.issueCompleteRedrawForActiveView();
						}
					});
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				} catch (OutOfMemoryError e) {
					ErrorMsg.addErrorMessage("Out of memory! Please choose to create a smaller graph or increase memory of Java VM!");
				}
			}
		}, null);
		
	}
	
	public static Graph createGraph(int numberOfNodes, boolean label, int initDegree, double p) {
		Graph rdg = new AdjListGraph();
		
		rdg.getListenerManager().transactionStarted(rdg);
		try {
			ArrayList<Node> nodes = new ArrayList<Node>();
			PositionGridGenerator pgg = new PositionGridGenerator(50, 50, 800);
			for (int i = 0; i < numberOfNodes; i++) {
				Node n = rdg.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pgg.getNextPositionVec2d()));
				AttributeHelper.setShapeEllipse(n);
				nodes.add(n);
				
				if (label)
					AttributeHelper.setLabel(n, "" + (i + 1));
			}
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for (int i = 0; i < numberOfNodes; i++) {
				Node a = nodes.get(i);
				int created = 0;
				int j = i - initDegree / 2;
				while (created < initDegree) {
					int jj = (j++) % numberOfNodes;
					if (jj < 0)
						jj += numberOfNodes;
					Node b = nodes.get(jj);
					if (!a.getNeighbors().contains(b) && a != b) {
						edges.add(rdg.addEdge(a, b, false,
								AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, false)));
					}
					created++;
				}
			}
			for (Edge e : edges) {
				double r = Math.random();
				if (r <= p) {
					Node a = e.getSource();
					ArrayList<Node> freeNodes = new ArrayList<Node>(nodes);
					freeNodes.removeAll(a.getNeighbors());
					freeNodes.remove(a);
					if (freeNodes.size() > 0) {
						int tgt = (int) (Math.random() * freeNodes.size());
						Node b = freeNodes.get(tgt);
						e.setTarget(b);
					}
				}
			}
			CircleLayouterAlgorithm ca = new CircleLayouterAlgorithm(400);
			ca.attach(rdg, new Selection("empty"));
			ca.execute();
			CenterLayouterAlgorithm ctr = new CenterLayouterAlgorithm();
			ctr.attach(rdg, new Selection("empty"));
			ctr.execute();
		} finally {
			rdg.getListenerManager().transactionFinished(rdg);
		}
		return rdg;
	}
}
