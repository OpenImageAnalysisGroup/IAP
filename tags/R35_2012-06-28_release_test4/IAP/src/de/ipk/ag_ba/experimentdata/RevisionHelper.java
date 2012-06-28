package de.ipk.ag_ba.experimentdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class RevisionHelper {
	public static Revision getFirstRevision(Revision nm) {
		HashMap<Long, Revision> time2rev = new HashMap<Long, Revision>();
		for (Revision o : nm.getRevisionSet()) {
			time2rev.put(o.getSaveTime(), o);
		}
		while (time2rev.get(nm.getReplaces()) != null) {
			nm = time2rev.get(nm.getReplaces());
		}
		return nm;
	}
	
	public static ArrayList<ArrayList<Revision>> getAllRevisions(Revision nm) {
		HashMap<Long, Revision> time2rev = new HashMap<Long, Revision>();
		for (Revision o : nm.getRevisionSet()) {
			time2rev.put(o.getSaveTime(), o);
		}
		HashSet<Revision> known = new HashSet<Revision>();
		ArrayList<ArrayList<Revision>> resultSet = new ArrayList<ArrayList<Revision>>();
		for (Revision startRevision : time2rev.values()) {
			if (known.contains(startRevision))
				continue;
			known.add(startRevision);
			ArrayList<Revision> result = new ArrayList<Revision>();
			result.add(nm);
			while (time2rev.get(nm.getReplaces()) != null) {
				nm = time2rev.get(nm.getReplaces());
				known.add(nm);
				result.add(nm);
			}
			Collections.reverse(result);
			resultSet.add(result);
		}
		return resultSet;
	}
	
	public static ArrayList<Revision> getRevisionHistory(Revision nm) {
		HashMap<Long, Revision> time2rev = new HashMap<Long, Revision>();
		for (Revision o : nm.getRevisionSet()) {
			time2rev.put(o.getSaveTime(), o);
		}
		ArrayList<Revision> result = new ArrayList<Revision>();
		result.add(nm);
		while (time2rev.get(nm.getReplaces()) != null) {
			nm = time2rev.get(nm.getReplaces());
			result.add(nm);
		}
		Collections.reverse(result);
		return result;
	}
}
