package tests.JMP.leaf_clustering;

import java.util.ArrayList;
import java.util.Iterator;

public class Leaf implements Iterable<LeafTip> {
	ArrayList<LeafTip> tipList;
	public int leafID;
	
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
		float time = Integer.MIN_VALUE;
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
}
