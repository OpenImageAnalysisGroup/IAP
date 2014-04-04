package tests.JMP.leaf_clustering;

import java.util.HashMap;

import javax.vecmath.Point2d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * @author pape
 */
public class LeafTip {
	private final long time;
	private final int x;
	private final int y;
	private int leafID;
	private double minDist; // distance to next leaftip
	private HashMap<String, Object> featureMap;
	
	public LeafTip(long time, int x, int y) {
		this.time = time;
		this.x = x;
		this.y = y;
		leafID = -1;
	}
	
	public LeafTip(long time, Vector2D pos, HashMap<String, Object> featureMap) {
		this.time = time;
		x = (int) pos.getX();
		y = (int) pos.getY();
		this.featureMap = new HashMap<String, Object>();
		this.featureMap = featureMap;
	}
	
	public int getLeafID() {
		return leafID;
	}
	
	public double getMinDist() {
		return minDist;
	}
	
	public void setLeafID(int leafID) {
		this.leafID = leafID;
	}
	
	public void setDist(double dist) {
		this.minDist = dist;
	}
	
	/**
	 * Distance to last leaftip which was recently added to leaf.
	 * 
	 * @param leaf
	 * @return
	 */
	public double dist(Leaf leaf) {
		LeafTip temp = leaf.getLast();
		return Math.sqrt((getX() - temp.getX()) * (getX() - temp.getX()) + (y - temp.y) * (y - temp.y));
	}
	
	public double dist(LeafTip lt) {
		return Math.sqrt((x - lt.getX()) * (x - lt.getX()) + (y - lt.getY()) * (y - lt.getY()));
	}
	
	public long getTime() {
		return time;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public Object getFeature(String key) {
		return featureMap.get(key);
	}
	
	public void addFeature(Object value, String key) {
		featureMap.put(key, value);
	}
	
	public double dist(Point2d p) {
		return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
	}
}
