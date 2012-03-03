package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import java.util.ArrayList;

import org.PositionGridGenerator;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

public class BarabasiAlbertGraphGenerator extends AbstractAlgorithm {
	
	@Override
	public String getCategory() {
		return "Elements";
	}
	
	private int numberOfNodes = 5;
	private boolean label = true;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new IntegerParameter(numberOfNodes, "Number of nodes", "Number of nodes"),
							new BooleanParameter(label, "Add node label", "If enabled, each node will be labeld (1,2,3,...)"), };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		numberOfNodes = ((IntegerParameter) params[i++]).getInteger();
		label = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	@Override
	public void check() throws PreconditionException {
		if (numberOfNodes < 1)
			throw new PreconditionException("Number of nodes needs to be at least 1");
	}
	
	public String getName() {
		return "Generate Barabási–Albert random graph";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Create a random scale-free network using a preferential<br>" +
							"attachment mechanism (considering node degree).";
	}
	
	public void execute() {
		if (graph.getNumberOfNodes() >= numberOfNodes)
			return;
		
		graph.getListenerManager().transactionStarted(this);
		try {
			new ArrayList<Node>();
			new PositionGridGenerator(50, 50, 800);
			
			// for (int i = 0; i<numberOfNodes; i++) {
			// Node n = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pgg.getNextPositionVec2d()));
			// nodes.add(n);
			//
			// if (label)
			// AttributeHelper.setLabel(n, ""+(i+1));
			// }
			
			// for (int i = 0; i<numberOfNodes; i++) {
			// Node a = nodes.get(i);
			// for (int j = 0; j<numberOfNodes; j++) {
			// if (!selfLoops && (i==j))
			// continue;
			// if (!directed && i>j)
			// continue;
			// Node b = nodes.get(j);
			// double r = Math.random();
			// if (r<=p) {
			// graph.addEdge(a, b, directed,
			// AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, directed));
			// }
			// }
			// }
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
}
