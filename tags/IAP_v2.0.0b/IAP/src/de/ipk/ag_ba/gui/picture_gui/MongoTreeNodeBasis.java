package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author klukas
 */
public abstract class MongoTreeNodeBasis implements DBEtreeNodeModelHelper {
	
	DBEtreeNodeModelHelper[] children;
	int index;
	
	public boolean readOnly = true;
	boolean isLeaf;
	
	Runnable getChildsMethod = null;
	
	public MongoTreeNodeBasis(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #getChild(int)
	 */
	public DBEtreeNodeModelHelper getChild(int index) {
		if (children == null)
			getChildsMethod.run();
		return children[index];
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #getChildCount()
	 */
	public int getChildCount() {
		if (children == null && getChildsMethod != null)
			getChildsMethod.run();
		return children != null ? children.length : 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #setIndex(int)
	 */
	public void setIndex(int index) {
		this.index = index;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #getIndex()
	 */
	public int getIndex() {
		return index;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #isLeaf()
	 */
	public boolean isLeaf() {
		return isLeaf;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #setGetChildrenMethod(java.lang.Runnable)
	 */
	public void setGetChildrenMethod(Runnable r) {
		getChildsMethod = r;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #setChildren(de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.
	 * DBEtreeNodeModelHelper[])
	 */
	public void setChildren(DBEtreeNodeModelHelper[] children) {
		this.children = children;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.expdb.application.picture_gui.DBEtreeNodeModelHelper
	 * #setIsLeaf(boolean)
	 */
	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
}
