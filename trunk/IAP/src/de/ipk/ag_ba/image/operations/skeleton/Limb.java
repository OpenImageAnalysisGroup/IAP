package de.ipk.ag_ba.image.operations.skeleton;

import java.awt.Point;
import java.util.ArrayList;

public class Limb {
	
	Point endpoint;
	Point initialpoint;
	ArrayList<Point> points = new ArrayList<Point>();
	boolean isCut = false;
	
	public Limb(Point endpoint, Point initialpoint) {
		this.endpoint = endpoint;
		this.initialpoint = initialpoint;
	}
	
	public Limb(Point endpoint) {
		this.endpoint = endpoint;
	}
	
	public Point getInitialpoint() {
		return initialpoint;
	}
	
	public void setInitialpoint(Point initialpoint) {
		this.initialpoint = initialpoint;
	}
	
	public int length() {
		return points.size() + 2;
	}
	
	public void addPoint(Point inp) {
		this.points.add(inp);
	}
}
