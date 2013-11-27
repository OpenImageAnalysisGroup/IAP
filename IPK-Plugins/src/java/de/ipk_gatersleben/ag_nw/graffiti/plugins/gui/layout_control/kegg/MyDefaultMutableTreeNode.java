/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import javax.swing.tree.DefaultMutableTreeNode;

import org.FeatureSet;
import org.ReleaseInfo;

public class MyDefaultMutableTreeNode extends DefaultMutableTreeNode {
	
	private static final long serialVersionUID = 1L;
	boolean isKeggPathway = false;
	boolean trim = false;
	
	public MyDefaultMutableTreeNode(KeggPathwayEntry kpe) {
		super(kpe);
		isKeggPathway = true;
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			trim = true;
	}
	
	public MyDefaultMutableTreeNode(String string) {
		super(string);
		isKeggPathway = false;
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			trim = true;
	}
	
	@Override
	public String toString() {
		if (isKeggPathway) {
			if (trim) {
				String sv = super.toString().trim();
				KeggPathwayEntry kpe = (KeggPathwayEntry) getUserObject();
				String mapN = kpe.getMapName();
				return "<html>" + sv + " <font color='gray'><small>(" + mapN + ")";
			} else
				return super.toString();
		} else {
			int cnt = getPathwayCount();
			String sv = super.toString();
			if (trim)
				sv = sv.trim();
			if (cnt > 0)
				return "<html>" + sv + " <font color='gray'><small>(" + cnt + " p.)";
			else
				return sv;
		}
	}
	
	private int getPathwayCount() {
		if (isKeggPathway)
			return 1;
		int result = 0;
		for (int i = 0; i < getChildCount(); i++) {
			Object o = getChildAt(i);
			if (o instanceof MyDefaultMutableTreeNode) {
				MyDefaultMutableTreeNode child = (MyDefaultMutableTreeNode) o;
				result += child.getPathwayCount();
			}
		}
		return result;
	}
	
	public String toDefaultString() {
		return super.toString();
	}
	
}
