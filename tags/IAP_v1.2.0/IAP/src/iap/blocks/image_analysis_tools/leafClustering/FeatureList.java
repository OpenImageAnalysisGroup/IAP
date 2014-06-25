package iap.blocks.image_analysis_tools.leafClustering;

import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.vecmath.Point3d;

/**
 * @author pape
 */
public class FeatureList {
	LinkedList<Feature> borderFeatureList;
	boolean onlyBiggest;
	
	public FeatureList(ArrayList<ArrayList<Integer>> borderLists, boolean val) {
		borderFeatureList = new LinkedList<Feature>();
		onlyBiggest = val;
		for (ArrayList<Integer> list : borderLists) {
			for (int i = 0; i < list.size(); i += 2) {
				Integer x = list.get(i);
				Integer y = list.get(i + 1);
				borderFeatureList.add(new Feature(x, y));
			}
			if (onlyBiggest)
				break;
		}
	}
	
	public LinkedList<Feature> getFeatureList() {
		return borderFeatureList;
	}
	
	/**
	 * @param key
	 *           - key which used for normalization.
	 * @return
	 */
	public LinkedList<Point3d> normalizeBorderFeatureList(String key) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		// get min and max val
		for (Feature p : borderFeatureList) {
			double val = (Double) p.getFeature(key);
			if (val < min)
				min = val;
			if (val > max)
				max = val;
		}
		
		LinkedList<Point3d> norm = new LinkedList<Point3d>();
		
		// calc normalized value
		for (Feature p : borderFeatureList) {
			double val = (Double) p.getFeature(key);
			double fac = (int) (255 * ((val - min) / (max - min)));
			Integer x = (Integer) p.getFeature("x");
			Integer y = (Integer) p.getFeature("y");
			norm.add(new Point3d(x.doubleValue(), y.doubleValue(), fac));
		}
		return norm;
	}
	
	public int size() {
		return borderFeatureList.size();
	}
	
	/**
	 * @param idxFeature
	 *           - index of borderlist
	 * @param filterKey
	 *           - key of searched feature
	 * @return
	 */
	public Object getFeature(int idxFeature, String filterKey) {
		return borderFeatureList.get(idxFeature).getFeature(filterKey);
	}
	
	public Feature getFeatureMap(int idxFeature) {
		return borderFeatureList.get(idxFeature);
	}
	
	public Point3d getFeatureAsPoint3d(int idxFeature, String filterKey) {
		return borderFeatureList.get(idxFeature).getFeaturePoint(filterKey);
	}
	
	public void addFeature(int index, Double val, String key, FeatureObjectType type) {
		borderFeatureList.get(index).addFeature(key, val, type);
	}
	
	public Feature get(int index) {
		return borderFeatureList.get(index);
	}
}
