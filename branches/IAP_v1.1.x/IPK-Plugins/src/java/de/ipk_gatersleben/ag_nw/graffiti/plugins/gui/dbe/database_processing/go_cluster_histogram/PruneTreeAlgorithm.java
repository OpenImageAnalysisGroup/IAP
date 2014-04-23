package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.util.Queue;

public class PruneTreeAlgorithm extends AbstractAlgorithm {
	
	public void execute() {
		prune(graph, selection.getNodes());
	}
	
	private static void prune(Graph graph, Collection<Node> nodes) {
		graph.getListenerManager().transactionStarted(PruneTreeAlgorithm.class);
		for (GraphElement ge : graph.getGraphElements())
			AttributeHelper.setHidden(false, ge);
		if (nodes.size() > 0) {
			HashSet<Node> protectedNodes = new HashSet<Node>();
			protectedNodes.addAll(nodes);
			scanUpwards(protectedNodes);
			
			for (Node n : graph.getNodes()) {
				if (!protectedNodes.contains(n))
					AttributeHelper.setHidden(true, n, true, true, true);
			}
		}
		graph.getListenerManager().transactionFinished(PruneTreeAlgorithm.class);
	}
	
	private static void scanUpwards(HashSet<Node> protectedNodes) {
		Queue todo = new Queue();
		for (Node n : protectedNodes)
			todo.addLast(n);
		HashSet<Node> processed = new HashSet<Node>();
		while (!todo.isEmpty()) {
			Node n = (Node) todo.removeFirst();
			if (processed.contains(n))
				continue;
			protectedNodes.add(n);
			for (Node up : n.getInNeighbors())
				todo.addLast(up);
			for (Node up : n.getUndirectedNeighbors())
				todo.addLast(up);
			processed.add(n);
		}
	}
	
	public String getName() {
		return "Prune Hierarchy";
	}
	
	@Override
	public String getCategory() {
		return null;// "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Hides lower parts of a hierarchy.<br>" +
							"Upstream parts of the hierarchy connected to one of the<br>" +
							"selected nodes will not be hidden, the remaing part of the<br>" +
							"network will be made invisible.<br><br>" +
							"If the current node selection is empty, all hidden elements<br>" +
							"will be made visible again.<br><br>";
	}
	
	public static void pruneFromTheseNodes(HashSet<Node> significantNodes) {
		if (significantNodes == null || significantNodes.size() <= 0)
			return;
		Graph g = significantNodes.iterator().next().getGraph();
		prune(g, significantNodes);
	}
	
}
