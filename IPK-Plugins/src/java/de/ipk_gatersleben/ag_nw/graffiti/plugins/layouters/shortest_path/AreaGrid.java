package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.shortest_path;

import java.util.Collection;

import org.Vector2d;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

public class AreaGrid {
	
	private Graph graph;
	private int gridX, gridY;
	
	public AreaGrid(Graph graph) {
		this.graph = graph;
		createGrid(5000, 5000);
	}
	
	private void createGrid(int maxGridSizeX, int maxGridSizeY) {
		// make grid: smallest width of any node divided by 4 -> grid field size width
		// make grid: smallest height of any node divided by 4 -> grid field size height
		// graph dimensions divided by grid width, grid height --> grid size X, Y
		gridX = 10;
		gridY = 10;
	}
	
	public int getGridDiameter() {
		return (int) Math.sqrt(gridX * gridX + gridY * gridY);
	}
	
	public void setEdgeCrossingTravelCost(int i) {
	}
	
	public void setNodeCrossingTravelCost(int i) {
	}
	
	public void claimNodeSpace() {
		for (@SuppressWarnings("unused")
		Node n : graph.getNodes()) {
			
		}
	}
	
	public Collection<Vector2d> routeEdge(Node source, Node target) {
		return null;
	}
	
}
