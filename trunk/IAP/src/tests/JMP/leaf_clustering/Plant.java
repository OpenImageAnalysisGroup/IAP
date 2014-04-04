package tests.JMP.leaf_clustering;

import java.util.LinkedList;

import javax.vecmath.Point2d;

import tests.JMP.methods.HelperMethods;

public class Plant {
	private final LinkedList<Leaf> leafList;
	private int growTime;
	private String iD;
	
	public Plant() {
		leafList = new LinkedList<Leaf>();
		iD = "";
	}
	
	public LinkedList<Leaf> getLeafList() {
		return leafList;
	}
	
	public void addNewLeaf(Leaf leaf) {
		leafList.add(leaf);
	}
	
	public void addLeafTip(LeafTip tempTip) {
		for (Leaf l : leafList) {
			if (l.getID() == tempTip.getLeafID())
				l.addLeafTip(tempTip);
		}
	}
	
	public void addNewLeaf(LeafTip tempTip) {
		leafList.add(new Leaf(tempTip, tempTip.getLeafID()));
	}
	
	public void setGrowTime(int t) {
		growTime = t;
	}
	
	public int getGrowTime() {
		return growTime;
	}
	
	public int getNumberOfLeaves() {
		return leafList.size();
	}
	
	public LinkedList<LeafTip> getLastTips() {
		LinkedList<LeafTip> out = new LinkedList<LeafTip>();
		for (Leaf l : leafList) {
			out.add(l.getLast());
		}
		return out;
	}
	
	public String getID() {
		return iD;
	}
	
	public void setID(String s) {
		iD = s;
	}
	
	public void calcMovementForEachLeaf(Point2d cen) {
		for (Leaf l : leafList) {
			for (LeafTip lt : l) {
				lt.addFeature(lt.dist(cen), "disttocenter");
			}
		}
	}
	
	public void saveLeafCoordinates(String path) {
		String out = "";
		for (Leaf l : leafList) {
			out += "Leaf: " + l.getID() + "\n";
			for (LeafTip lt : l) {
				out += lt.getX() + ",;" + lt.getY() + ",\n";
			}
			out += "\n";
		}
		HelperMethods.write(path, "Leaf Coordinates of " + getID(), out);
	}
	
	public void saveLeafTipFeature(String path, String key) {
		String out = "";
		for (Leaf l : leafList) {
			out += "Leaf: " + l.getID() + "\n";
			for (LeafTip lt : l) {
				out += lt.getFeature(key) + ",\n";
			}
			out += "\n";
		}
		HelperMethods.write(path, "Feature-" + key + getID(), out);
	}
}
