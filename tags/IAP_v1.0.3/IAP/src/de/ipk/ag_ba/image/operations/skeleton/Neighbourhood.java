package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Neighbourhood {
	
	public int colorEndpoints = Color.YELLOW.getRGB();
	public int colorBranches = Color.RED.getRGB();
	
	ArrayList<Point> neigbours = new ArrayList<Point>();
	Point branch = null;
	Point endpoint = null;
	
	public Neighbourhood(ArrayList<Point> inp) {
		this.neigbours = inp;
		calcNeigbourhoodProperties();
	}
	
	private void calcNeigbourhoodProperties() {
		for (Point p : neigbours) {
		}
		
	}
}
