package iap.blocks.image_analysis_tools.leafClustering;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author pape
 */
public class Leaf implements Iterable<LeafTip> {
	ArrayList<LeafTip> tipList;
	public int leafID;
	public long oldestLeafTip;
	public boolean ignore; // if leaf has no match for long time -> set ignore
	
	public Leaf(LeafTip l, int id) {
		tipList = new ArrayList<LeafTip>();
		tipList.add(l);
		this.leafID = id;
	}
	
	public Leaf(LeafTip l) {
		tipList = new ArrayList<LeafTip>();
		tipList.add(l);
		this.leafID = -1;
	}
	
	public LeafTip getLast() {
		long time = Long.MIN_VALUE;
		int tipidx = -1;
		int idx = 0;
		for (LeafTip lt : tipList) {
			if (lt.getTime() > time) {
				time = lt.getTime();
				tipidx = idx;
			}
			idx++;
		}
		return tipList.get(tipidx);
	}
	
	public void addLeafTip(LeafTip tempTip) {
		tipList.add(tempTip);
	}
	
	public int getID() {
		return leafID;
	}
	
	public int getNumberOfLeafTips() {
		return tipList.size();
	}
	
	@Override
	public Iterator<LeafTip> iterator() {
		return tipList.iterator();
	}
	
	public LeafTip getFirst() {
		long time = Long.MAX_VALUE;
		int tipidx = -1;
		int idx = 0;
		for (LeafTip lt : tipList) {
			if (lt.getTime() < time) {
				time = lt.getTime();
				tipidx = idx;
			}
			idx++;
		}
		return tipList.get(tipidx);
	}
	
	public long getTimeForLastMatch() {
		long t = 0;
		for (LeafTip lt : tipList) {
			if (lt.getTime() > t)
				t = lt.getTime();
		}
		return t;
	}
}
