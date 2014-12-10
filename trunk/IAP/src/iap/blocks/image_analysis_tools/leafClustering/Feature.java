package iap.blocks.image_analysis_tools.leafClustering;

import iap.blocks.extraction.Numeric;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;

import java.util.HashMap;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * @author pape
 */
public class Feature implements Numeric {
	HashMap<String, FeatureObject> featureMap = new HashMap<String, FeatureObject>();
	
	public Feature(Integer X, Integer Y) {
		featureMap.put("x", new FeatureObject(X.intValue(), FeatureObjectType.POSITION));
		featureMap.put("y", new FeatureObject(Y.intValue(), FeatureObjectType.POSITION));
	}
	
	public Feature(int x, int y, double i, String key) {
		featureMap.put("x", new FeatureObject(x, FeatureObjectType.POSITION));
		featureMap.put("y", new FeatureObject(y, FeatureObjectType.POSITION));
		featureMap.put(key, new FeatureObject(i, FeatureObjectType.NUMERIC));
	}
	
	public Feature(double i, String key) {
		featureMap.put(key, new FeatureObject(i, FeatureObjectType.NUMERIC));
	}
	
	public Feature() {
	}
	
	public void addFeatureOverwrite(String key, Object value) {
		featureMap.put(key, new FeatureObject(value, FeatureObjectType.NUMERIC));
	}
	
	public void addFeature(String key, Object value, FeatureObjectType type) {
		if (featureMap.get(key) != null)
			return;
		else
			featureMap.put(key, new FeatureObject(value, type));
	}
	
	public Object getFeature(String key) {
		FeatureObject v = featureMap.get(key);
		if (v != null)
			return v.feature;
		else
			return null;
	}
	
	public Point3d getFeaturePoint(String key) {
		return new Point3d(((Integer) featureMap.get("x").feature).doubleValue(), ((Integer) featureMap.get("y").feature).doubleValue(),
				((Double) featureMap.get(key).feature).doubleValue());
	}
	
	public Vector2D getPosition() {
		return new Vector2D(((Integer) featureMap.get("x").feature).doubleValue(), ((Integer) featureMap.get("y").feature).doubleValue());
	}
	
	public HashMap<String, FeatureObject> getFeatureMap() {
		return featureMap;
	}
	
	public void setPosition(int x, int y) {
		featureMap.get("x").feature = x;
		featureMap.get("y").feature = y;
	}
	
	@Override
	public Double getValue() {
		return (Double) getFeature("angle");
	}
}
