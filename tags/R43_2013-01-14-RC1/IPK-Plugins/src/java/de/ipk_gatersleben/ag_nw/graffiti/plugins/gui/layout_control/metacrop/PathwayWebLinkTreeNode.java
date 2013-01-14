package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import javax.swing.tree.DefaultMutableTreeNode;

public class PathwayWebLinkTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private String label;
	private PathwayWebLinkItem metaCropListItem;
	
	public PathwayWebLinkTreeNode(String label) {
		this.label = label;
		this.metaCropListItem = null;
	}
	
	public PathwayWebLinkTreeNode(PathwayWebLinkItem i) {
		this.label = i.toString();
		this.metaCropListItem = i;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public PathwayWebLinkItem getMetaCropListItem() {
		return metaCropListItem;
	}
}
