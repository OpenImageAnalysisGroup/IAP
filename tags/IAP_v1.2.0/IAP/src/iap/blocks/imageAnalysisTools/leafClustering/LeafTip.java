package iap.blocks.imageAnalysisTools.leafClustering;

import iap.blocks.extraction.Normalisation;
import iap.blocks.imageAnalysisTools.leafClustering.FeatureObject.FeatureObjectType;

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
	private HashMap<String, FeatureObject> featureMap;
	private Normalisation normalisationFactor;
	
	public LeafTip(long time, int x, int y) {
		this.time = time;
		if (normalisationFactor == null || !normalisationFactor.isRealWorldCoordinateValid()) {
			this.x = x;
			this.y = y;
		} else {
			this.x = (normalisationFactor.convertImgXtoRealWorldX(x));
			this.y = (normalisationFactor.convertImgYtoRealWorldY(y));
		}
		leafID = -1;
	}
	
	public LeafTip(long time, Vector2D pos, HashMap<String, FeatureObject> featureMap, Normalisation normalisationFactor) {
		this.time = time;
		this.normalisationFactor = normalisationFactor;
		
		if (normalisationFactor == null || !normalisationFactor.isRealWorldCoordinateValid()) {
			x = (int) pos.getX();
			y = (int) pos.getY();
		} else {
			x = (normalisationFactor.convertImgXtoRealWorldX(pos.getX()));
			y = (normalisationFactor.convertImgYtoRealWorldY(pos.getY()));
			// System.out.println("x: " + x + " : y: " + y + " posx/y: " + pos.getX() + " : " + pos.getY());
		}
		
		this.featureMap = new HashMap<String, FeatureObject>();
		this.featureMap = featureMap;
	}
	
	public LeafTip(long time, double x, double y, HashMap<String, FeatureObject> featureMap, Normalisation normalisationFactor) {
		this.time = time;
		this.normalisationFactor = normalisationFactor;
		
		if (normalisationFactor == null || !normalisationFactor.isRealWorldCoordinateValid()) {
			this.x = (int) x;
			this.y = (int) y;
		} else {
			this.x = (normalisationFactor.convertImgXtoRealWorldX(x));
			this.y = (normalisationFactor.convertImgYtoRealWorldY(y));
			// System.out.println("x: " + x + " : y: " + y + " posx/y: " + pos.getX() + " : " + pos.getY());
		}
		
		this.featureMap = new HashMap<String, FeatureObject>();
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
		return Math.sqrt((getRealWorldX() - temp.getRealWorldX()) * (getRealWorldX() - temp.getRealWorldX()) + (y - temp.y) * (y - temp.y));
	}
	
	public double dist(LeafTip lt) {
		return Math.sqrt((x - lt.getRealWorldX()) * (x - lt.getRealWorldX()) + (y - lt.getRealWorldY()) * (y - lt.getRealWorldY()));
	}
	
	public long getTime() {
		return time;
	}
	
	public int getRealWorldX() {
		return x;
	}
	
	public int getRealWorldY() {
		return y;
	}
	
	public Object getFeature(String key) {
		return featureMap.get(key).feature;
	}
	
	public void addFeature(Object value, String key, FeatureObjectType type) {
		featureMap.put(key, new FeatureObject(value, type));
	}
	
	public double dist(Point2d p) {
		return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
	}
	
	public int getImageX(Normalisation norm) {
		return norm == null ? x : norm.getImageXfromRealWorldX(x);
	}
	
	public int getImageY(Normalisation norm) {
		return norm == null ? y : norm.getImageYfromRealWorldY(y);
	}
}
