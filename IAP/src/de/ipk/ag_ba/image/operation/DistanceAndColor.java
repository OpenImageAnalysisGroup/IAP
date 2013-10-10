package de.ipk.ag_ba.image.operation;

/**
 * Stores a distance value and a connected color. May be used to create a sorted list of colors, with increasing order
 * of similarity.
 * 
 * @author klukas
 */
public class DistanceAndColor implements Comparable<DistanceAndColor> {
	
	final int x;
	final int y;
	final int r;
	final int g;
	final int b;
	private final float distance;
	
	public DistanceAndColor(int x, int y, int r, int g, int b, float dist) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.g = g;
		this.b = b;
		distance = dist;
	}
	
	@Override
	public int compareTo(DistanceAndColor o) {
		return distance < o.distance ? -1 : 1;
	}
	
}
