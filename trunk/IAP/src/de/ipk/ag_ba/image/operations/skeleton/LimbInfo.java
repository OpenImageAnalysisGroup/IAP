package de.ipk.ag_ba.image.operations.skeleton;

import java.util.ArrayList;

import org.Vector2i;

public class LimbInfo {
	
	private final ArrayList<Vector2i> edgePoints;
	
	public LimbInfo(ArrayList<Vector2i> edgePoints) {
		this.edgePoints = edgePoints;
	}
	
	public LimbInfo(LimbInfo info1, LimbInfo info2) {
		this.edgePoints = new ArrayList<Vector2i>();
		edgePoints.addAll(info1.getEdgePoints());
		edgePoints.addAll(info2.getEdgePoints());
	}
	
	public ArrayList<Vector2i> getEdgePoints() {
		return edgePoints;
	}
	
	public int getLinearMx() {
		return 0;
	}
}
