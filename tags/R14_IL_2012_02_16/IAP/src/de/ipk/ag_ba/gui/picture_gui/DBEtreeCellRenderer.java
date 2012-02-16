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
 * Created on 15.07.2004 by Christian Klukas
 */
package de.ipk.ag_ba.gui.picture_gui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DBEtreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
						int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (leaf && !isValidDBEtreeNode(value)) {
			setToolTipText(null); // no tool tip
			setIcon(null);
		}
		return this;
	}
	
	protected boolean isValidDBEtreeNode(Object value) {
		if (value instanceof MongoTreeNode) {
			MongoTreeNode dtn = (MongoTreeNode) value;
			if (dtn.getTargetEntity() == null)
				return false;
			else
				return true;
		}
		return false;
	}
}
