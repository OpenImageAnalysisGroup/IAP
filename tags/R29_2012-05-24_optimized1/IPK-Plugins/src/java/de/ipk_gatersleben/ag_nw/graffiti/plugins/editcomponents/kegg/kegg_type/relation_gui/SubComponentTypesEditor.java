/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.relation_gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.FolderPanel;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeName;

public class SubComponentTypesEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	private JLabel subComponentTypesHelp;
	private ArrayList<SubComponentNameCheckBox> checkBoxes = new ArrayList<SubComponentNameCheckBox>();
	
	public SubComponentTypesEditor(Relation initialRelation, JLabel subComponentTypesHelp) {
		setLayout(TableLayout.getLayout(TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
		this.subComponentTypesHelp = subComponentTypesHelp;
		updateRelationSelection(initialRelation);
		FolderPanel fp = new FolderPanel("Sub-Components", true, true, false, null);
		fp.setBackground(null);
		// fp.setMaximumRowCount(5);
		fp.addCollapseListenerDialogSizeUpdate();
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 2);
		
		for (SubtypeName stn : SubtypeName.values()) {
			if (stn == SubtypeName.compound)
				continue;
			if (stn == SubtypeName.hiddenCompound)
				continue;
			SubComponentNameCheckBox sccb = new SubComponentNameCheckBox(initialRelation, stn, subComponentTypesHelp);
			checkBoxes.add(sccb);
		}
		for (int i = 0; i < checkBoxes.size(); i++) {
			SubComponentNameCheckBox sccb1 = null;
			SubComponentNameCheckBox sccb2 = null;
			sccb1 = checkBoxes.get(i);
			i++;
			if (i < checkBoxes.size())
				sccb2 = checkBoxes.get(i);
			fp.addGuiComponentRow(sccb1, sccb2, false);
		}
		fp.layoutRows();
		add(fp, "0,0");
		validate();
	}
	
	public void updateRelationSelection(Relation r) {
		subComponentTypesHelp.setText("");
		for (SubComponentNameCheckBox sccp : checkBoxes) {
			sccp.updateRelationSelection(r);
		}
	}
	
	public void setCallBack(MyRelationList list) {
		for (SubComponentNameCheckBox sccp : checkBoxes) {
			sccp.setCallBack(list);
		}
	}
}
