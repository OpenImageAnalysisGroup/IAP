package de.ipk.ag_ba.image.operation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class BorderFeature {
	HashMap<String, Object> featureMap = new HashMap<String, Object>();
	
	public BorderFeature(Integer X, Integer Y) {
		featureMap.put("x", X.intValue());
		featureMap.put("y", Y.intValue());
	}
	
	public BorderFeature(int x, int y, double i, String key) {
		featureMap.put("x", x);
		featureMap.put("y", y);
		featureMap.put(key, i);
	}
	
	public BorderFeature() {
	}
	
	public void addFeatureOverwrite(String key, Object value) {
		featureMap.put(key, value);
	}
	
	public void addFeature(String key, Object value) {
		if (featureMap.get(key) != null)
			return;
		else
			featureMap.put(key, value);
	}
	
	public Object getFeature(String key) {
		return featureMap.get(key);
	}
	
	public Point3d getFeaturePoint(String key) {
		return new Point3d(((Integer) featureMap.get("x")).doubleValue(), ((Integer) featureMap.get("y")).doubleValue(),
				((Double) featureMap.get(key)).doubleValue());
	}
	
	public Vector2D getPosition() {
		return new Vector2D(((Integer) featureMap.get("x")).doubleValue(), ((Integer) featureMap.get("y")).doubleValue());
	}
	
	public HashMap<String, Object> getFeatureMap() {
		return featureMap;
	}
	
	public void addBorderFeature(BorderFeature borderFeature) {
		Iterator it = borderFeature.featureMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			featureMap.put((String) pairs.getKey(), pairs.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
	}
}
