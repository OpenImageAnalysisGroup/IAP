package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Point;
import java.util.ArrayList;

public class Limb {
	
	public Point endpoint;
	Point initialpoint;
	ArrayList<Point> points = new ArrayList<Point>();
	private boolean isCut = false;
	
	public Limb(Point endpoint, Point initialpoint) {
		this.endpoint = endpoint;
		this.initialpoint = initialpoint;
	}
	
	public Limb(Point endpoint) {
		this.endpoint = endpoint;
	}
	
	public Limb() {
		// empty
	}
	
	public void setInitialpoint(Point initialpoint) {
		this.initialpoint = initialpoint;
	}
	
	public int length() {
		return points.size() + 2;
	}
	
	public void addPoint(Point inp) {
		points.add(inp);
	}
	
	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public boolean initialOrEndpoint(Point p) {
		if (initialpoint.distance(p) < 0.1 || endpoint.distance(p) < 0.1)
			return true;
		else
			return false;
	}
	
	boolean isCut() {
		return isCut;
	}
	
	void setCut(boolean isCut) {
		this.isCut = isCut;
	}
}
