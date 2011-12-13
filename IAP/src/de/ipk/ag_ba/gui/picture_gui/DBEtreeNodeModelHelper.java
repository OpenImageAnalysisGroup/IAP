/*******************************************************************************
 * The DBE2 Add-on is (c) 2009-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0 (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions of
 * this license, the below stated disclaimer of warranties and the licenses of the used
 * libraries. For further details see license.txt in the root folder of this project.
 ******************************************************************************/
/*
 * Created on 20.04.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author klukas
 */
public interface DBEtreeNodeModelHelper {
	
	/**
	 * @param index
	 * @return Returns a children with given index (0..getChildCount()-1)
	 */
	public DBEtreeNodeModelHelper getChild(int index);
	
	/**
	 * Memorize given index (index of child in parent node).
	 * 
	 * @param index
	 */
	public void setIndex(int index);
	
	/**
	 * Set the information if this node is a leaf
	 * 
	 * @param isLeaf
	 */
	public void setIsLeaf(boolean isLeaf);
	
	/**
	 * The given method will be run in order to enumerate the children. The
	 * Runnable should get a reference to this interface-object and store the
	 * information gathered with setChildren
	 * 
	 * @param r
	 */
	public void setGetChildrenMethod(Runnable r);
	
	/**
	 * Memorize the children.
	 * 
	 * @param children
	 */
	public void setChildren(DBEtreeNodeModelHelper[] children);
	
	/**
	 * @return Index of child in parent node, needs to be saved before
	 *         (setIndex).
	 */
	public int getIndex();
	
	/**
	 * @return Number of children
	 */
	public int getChildCount();
	
	/**
	 * @return True, if no children are possible
	 */
	public boolean isLeaf();
}
