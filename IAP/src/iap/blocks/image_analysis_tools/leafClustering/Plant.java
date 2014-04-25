package iap.blocks.image_analysis_tools.leafClustering;

import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;

import java.util.LinkedList;

import javax.vecmath.Point2d;

/**
 * @author pape
 */
public class Plant {
	private final LinkedList<Leaf> leafList;
	private int growTime;
	private String iD;
	private String settingFolder;
	
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
				lt.addFeature(lt.dist(cen), "disttocenter", FeatureObjectType.NUMERIC);
			}
		}
	}
	
	public String getSettingFolder() {
		return settingFolder;
	}
	
	public void setSettingFolder(String s) {
		this.settingFolder = s;
	}
}
