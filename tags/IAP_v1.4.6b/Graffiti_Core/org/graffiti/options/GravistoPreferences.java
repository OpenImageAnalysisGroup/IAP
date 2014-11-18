/*
 * Created on 03.08.2004 by Christian Klukas
 */
package org.graffiti.options;

import java.util.HashMap;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class GravistoPreferences {
	
	@SuppressWarnings("unchecked")
	HashMap<String, Comparable> optionsAndValues;
	String preferencesNodeName;
	
	@SuppressWarnings("unchecked")
	public GravistoPreferences(String preferencesNodeName) {
		this.preferencesNodeName = preferencesNodeName;
		optionsAndValues = new HashMap<String, Comparable>();
	}
	
	public double getDouble(String optionName, double defaultValue) {
		if (optionsAndValues.containsKey(optionName)) {
			Object value = optionsAndValues.get(optionName);
			return ((Double) value).doubleValue();
		} else
			return defaultValue;
	}
	
	public GravistoPreferences node(String subNodeName) {
		return new GravistoPreferences(preferencesNodeName + "/" + subNodeName);
	}
	
	public String get(String optionName, String defaultValue) {
		if (optionsAndValues.containsKey(optionName)) {
			Object value = optionsAndValues.get(optionName);
			return (String) value;
		} else
			return defaultValue;
	}
	
	public float getFloat(String optionName, float defaultValue) {
		if (optionsAndValues.containsKey(optionName)) {
			Object value = optionsAndValues.get(optionName);
			return ((Float) value).floatValue();
		} else
			return defaultValue;
	}
	
	public void put(String optionName, String value) {
		optionsAndValues.put(optionName, value);
	}
	
	public int getInt(String optionName, int defaultValue) {
		if (optionsAndValues.containsKey(optionName)) {
			Object value = optionsAndValues.get(optionName);
			return ((Integer) value).intValue();
		} else
			return defaultValue;
	}
	
	public void clear() {
		optionsAndValues.clear();
	}
	
	public void putInt(String optionName, int value) {
		optionsAndValues.put(optionName, new Integer(value));
	}
	
	/**
	 * @param class1
	 * @return
	 */
	public static GravistoPreferences userNodeForPackage(Class<?> classValue) {
		try {
			return new GravistoPreferences(classValue.getPackage().getName() + "/" + classValue.getName());
		} catch (NullPointerException npe) {
			return new GravistoPreferences("/" + classValue.getName());
		}
	}
	
	/**
	 * @return
	 */
	public String[] keys() {
		return (String[]) optionsAndValues.keySet().toArray(new String[] {});
	}
	
}
