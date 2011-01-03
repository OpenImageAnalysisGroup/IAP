/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author entzian
 */
public class ValueSortedTreeMap implements Comparator {
	
	private final Map<Double, Double> map;
	
	public ValueSortedTreeMap() {
		map = new TreeMap<Double, Double>();
	}
	
	public void put(double value1, double value2) {
		map.put(new Double(value1), new Double(value2));
	}
	
	// public Set<Map.Entry<K,V>> entrySet
	
	@Override
	public int compare(Object o1, Object o2) {
		return map.get(o2).compareTo(map.get(o1));
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
}
