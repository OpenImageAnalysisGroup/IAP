package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashSet;

public class ObjectStat {
	
	private final String property;
	
	private final ArrayList<Object> objectArray = new ArrayList<Object>();
	private final HashSet<Object> objectSet = new HashSet<Object>();
	
	private int compareCnt = 0;
	private int equalCnt = 0;
	
	private int simpleCounter = 0;
	
	public ObjectStat(String property) {
		this.property = property;
	}
	
	public void add(Object o, boolean useArrayAndSet) {
		if (useArrayAndSet)
			add(o);
		else
			simpleCounter++;
	}
	
	public void add(Object o) {
		boolean foundEqual = false, foundCompare = false;
		boolean oc = o instanceof Comparable;
		for (Object known : objectArray) {
			if (!foundEqual && known == o) {
				foundEqual = true;
			}
			if (oc) {
				if (!foundCompare && ((Comparable) known).compareTo(o) == 0) {
					foundCompare = true;
				}
			}
			if (foundCompare && foundEqual)
				break;
		}
		if (!foundCompare)
			compareCnt++;
		if (!foundEqual)
			equalCnt++;
		objectArray.add(o);
		objectSet.add(o);
	}
	
	public static String getTableHeader(boolean sh) {
		if (sh)
			return "<tr><th></th><th>Ref</th><th>Ins</th><th>Set</th><th>Com</th></tr>";
		else
			return "<tr><th></th><th>References</th><th>Instances</th><th>Set Size</th><th>Compare-To-Count</th></tr>";
	}
	
	@Override
	public String toString() {
		int objCnt = objectArray.size();
		int setCnt = objectSet.size();
		if (simpleCounter > 0)
			return "<tr><td>" + property + "</td><td>" + simpleCounter + "</td><td colspan='3'>not computed</td></tr>";
		else
			return "<tr><td>" + property + "</td><td>" + objCnt + "</td><td>" + equalCnt + "</td><td>" + setCnt + "</td><td>" + compareCnt + "</td></tr>";
	}
	
}
