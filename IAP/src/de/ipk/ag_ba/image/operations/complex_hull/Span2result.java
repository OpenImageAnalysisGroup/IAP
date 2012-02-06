package de.ipk.ag_ba.image.operations.complex_hull;

public class Span2result {
	
	private final double length;
	private final Point p1;
	private final Point p2;
	private final double p1len;
	private final double p2len;
	private final Point p1l;
	private final Point p2l;
	
	public Span2result(double len, Point p1, Point p2, Point p1l, Point p2l, double p1len, double p2len) {
		this.length = len;
		this.p1 = p1;
		this.p2 = p2;
		this.p1l = p1l;
		this.p2l = p2l;
		this.p1len = p1len;
		this.p2len = p2len;
	}
	
	public double getLengthPC1() {
		return length;
	}
	
	public Point getP1() {
		return p1;
	}
	
	public Point getP2() {
		return p2;
	}
	
	public double getLengthPC2() {
		double res = p1len + p2len;
		return res;
	}
	
	public double getP1len() {
		return p1len;
	}
	
	public double getP2len() {
		return p2len;
	}
	
	public Point getP1l() {
		return p1l;
	}
	
	public Point getP2l() {
		return p2l;
	}
	
}
