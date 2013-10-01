package org;

import java.util.ArrayList;

public class StringAnnotationProcessor {
	
	private String value;
	
	public StringAnnotationProcessor(String value) {
		this.value = value;
	}
	
	public String getAnnotationField(String key) {
		String a = getAnnotation();
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				String[] nn = f.split("#", 2);
				if (nn[0].equals(key))
					return nn[1];
			}
		}
		return null;
	}
	
	public ArrayList<String> getAnnotationKeys(String search) {
		ArrayList<String> result = new ArrayList<String>();
		String a = getAnnotation();
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				String[] nn = f.split("#", 2);
				if (nn[0].contains(search))
					result.add(nn[0]);
			}
		}
		return result;
	}
	
	public synchronized void addAnnotationField(String key, String value) {
		if (key == null || value == null)
			return;
		if (value.contains(";"))
			throw new UnsupportedOperationException(
					"annotation field value must not contain the ;-character");
		String a = getAnnotation();
		if (a == null)
			a = key + "#" + value;
		else
			a += ";" + key + "#" + value;
		setAnnotation(a);
	}
	
	private void setAnnotation(String a) {
		this.value = a;
	}
	
	private String getAnnotation() {
		return value;
	}
	
	public boolean replaceAnnotationField(String key, String value) {
		if (key == null || value == null)
			return false;
		boolean found = false;
		StringBuilder res = new StringBuilder();
		String a = getAnnotation();
		if (value.contains(";"))
			throw new UnsupportedOperationException(
					"annotation field value must not contain a commata character");
		if (a != null) {
			String anno = a;
			String[] fields = anno.split(";");
			for (String f : fields) {
				if (f.length() == 0)
					continue;
				String[] nn = f.split("#", 2);
				if (res.length() > 0)
					res.append(";");
				res.append(nn[0]);
				if (nn[0].equals(key)) {
					res.append("#" + value);
					found = true;
				} else
					res.append("#" + nn[1]);
			}
		}
		setAnnotation(res.toString());
		return found;
	}
}
