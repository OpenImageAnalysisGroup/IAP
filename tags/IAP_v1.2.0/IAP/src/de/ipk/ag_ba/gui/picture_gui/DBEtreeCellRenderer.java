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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DBEtreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
	
	private static final long serialVersionUID = 1L;
	
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
	
	ImageIcon cameraRendererIcon, groupRendererIcon, timeRendererIcon;
	
	public void setGroupRendererIcon(ImageIcon groupRendererIcon) {
		this.groupRendererIcon = groupRendererIcon;
	}
	
	public void setCameraRendererIcon(ImageIcon cameraRendererIcon) {
		this.cameraRendererIcon = cameraRendererIcon;
	}
	
	public void setTimeRendererIcon(ImageIcon timeRendererIcon) {
		this.timeRendererIcon = timeRendererIcon;
	};
	
	public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		
		Component ret = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		
		JLabel label = (JLabel) ret;
		
		if (value instanceof MongoTreeNode) {
			MongoTreeNode mtn = (MongoTreeNode) value;
			if (timeRendererIcon != null && mtn.isSampleNode())
				label.setIcon(timeRendererIcon);
			else
				if (groupRendererIcon != null && mtn.isGroupNode())
					label.setIcon(groupRendererIcon);
				else
					if (cameraRendererIcon != null) {
						if (mtn.getTargetEntity() != null && (mtn.getTargetEntity() instanceof SubstanceInterface)) {
							String n = ((SubstanceInterface) mtn.getTargetEntity()).getName();
							// new style
							// could be checked statically using LTdataExchange.positionFirst
							// but having both cases handled here, ensures that also old datasets show
							// the custom icon for the camera images
							if (StringManipulationTools.count(n, ".") == 1 &&
									(n.startsWith("top.") || n.startsWith("side.")) &&
									(n.endsWith(".vis") || n.endsWith(".fluo") || n.endsWith(".nir") || n.endsWith(".ir")))
								label.setIcon(cameraRendererIcon);
							else
								// old style
								if ((n.endsWith(".top") || n.endsWith(".side")) &&
										(n.startsWith("vis.") || n.startsWith("fluo.") || n.startsWith("nir.") || n.startsWith("ir.")))
									label.setIcon(cameraRendererIcon);
						}
					}
		}
		
		if (leaf && !isValidDBEtreeNode(value)) {
			setToolTipText(null); // no tool tip
			setIcon(null);
		}
		
		String tt = ((MongoTreeNode) value).getTooltipInfo();
		label.setToolTipText(tt);
		
		return ret;
	}
}
