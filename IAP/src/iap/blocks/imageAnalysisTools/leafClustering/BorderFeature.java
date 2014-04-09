package iap.blocks.imageAnalysisTools.leafClustering;

import iap.blocks.imageAnalysisTools.leafClustering.FeatureObject.FeatureObjectType;

import java.util.HashMap;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * @author pape
 */
public class BorderFeature {
	HashMap<String, FeatureObject> featureMap = new HashMap<String, FeatureObject>();
	
	public BorderFeature(Integer X, Integer Y) {
		featureMap.put("x", new FeatureObject(X.intValue(), FeatureObjectType.POSITION));
		featureMap.put("y", new FeatureObject(Y.intValue(), FeatureObjectType.POSITION));
	}
	
	public BorderFeature(int x, int y, double i, String key) {
		featureMap.put("x", new FeatureObject(x, FeatureObjectType.POSITION));
		featureMap.put("y", new FeatureObject(y, FeatureObjectType.POSITION));
		featureMap.put(key, new FeatureObject(i, FeatureObjectType.NUMERIC));
	}
	
	public BorderFeature() {
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
		return featureMap.get(key).feature;
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
	
	// public void addBorderFeature(BorderFeature borderFeature) {
	// Iterator it = borderFeature.featureMap.entrySet().iterator();
	// while (it.hasNext()) {
	// Map.Entry pairs = (Map.Entry) it.next();
	// featureMap.put((String) pairs.getKey(), (FeatureObject) pairs.getValue());
	// it.remove(); // avoids a ConcurrentModificationException
	// }
	// }
}
