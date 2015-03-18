package iap.blocks.image_analysis_tools.leafClustering;

import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;

import java.util.LinkedList;

import javax.vecmath.Point2d;

import org.GapList;

/**
 * @author pape
 */
public class Plant {
	private final LinkedList<Leaf> leafList;
	private final LinkedList<Long> timePoints; // sample points for measurements
	private String iD;
	private String settingFolder;
	
	public Plant() {
		leafList = new LinkedList<Leaf>();
		iD = "";
		timePoints = new LinkedList<Long>();
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
	
	public void addTime(long t) {
		timePoints.add(t);
	}
	
	public int getGrowTime() {
		return (int) (timePoints.getLast() - timePoints.getFirst());
	}
	
	public int getNumberOfLeaves() {
		return leafList.size();
	}
	
	/**
	 * Returns last matched leaves (considered the ignored flag, this leaves will not be included in the returned list).
	 */
	public GapList<LeafTip> getLastTips() {
		GapList<LeafTip> out = new GapList<LeafTip>();
		for (Leaf l : leafList) {
			if (l.ignore == true)
				continue;
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
	
	/**
	 * Set ignore for leaf n.
	 */
	public void setIgnoreForLeaf(int n) {
		for (Leaf l : leafList) {
			if (l.leafID == n)
				l.ignore = true;
		}
	}
}
