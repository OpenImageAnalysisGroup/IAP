/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggRelationSubTypeAttributeEditor
					extends AbstractValueEditComponent {
	
	protected ArrayList<JLabel> keggRelationSubTypeSelection = new ArrayList<JLabel>();
	
	public KeggRelationSubTypeAttributeEditor(final Displayable disp) {
		super(disp);
		String curValuesString = ((KeggRelationSubTypeAttribute) getDisplayable()).getString();
		String[] curValues = curValuesString.split(";");
		for (String curVal : curValues) {
			JLabel rsts = new JLabel();
			rsts.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
			rsts.setOpaque(false);
			rsts.setText(curVal);
			rsts.setPreferredSize(new Dimension(30, (int) rsts.getPreferredSize().getHeight()));
			keggRelationSubTypeSelection.add(rsts);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JComponent getComponent() {
		JComponent res = TableLayout.getMultiSplit((ArrayList) keggRelationSubTypeSelection);
		res.setOpaque(false);
		return res;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			for (JLabel jcb : keggRelationSubTypeSelection) {
				jcb.setText(EMPTY_STRING);
			}
		} else {
			keggRelationSubTypeSelection.clear();
			String curValuesString = ((KeggRelationSubTypeAttribute) getDisplayable()).getString();
			String[] curValues = curValuesString.split(";");
			for (String curVal : curValues) {
				JLabel rsts = new JLabel();
				rsts.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
				rsts.setText(curVal);
				rsts.setPreferredSize(new Dimension(20, (int) rsts.getPreferredSize().getHeight()));
				keggRelationSubTypeSelection.add(rsts);
			}
		}
	}
	
	public void setValue() {
		boolean isOneEmpty = false;
		for (JLabel jcb : keggRelationSubTypeSelection) {
			if (jcb.getText().equals(EMPTY_STRING))
				isOneEmpty = true;
		}
		if (!isOneEmpty) {
			String rval = "";
			for (JLabel jcb : keggRelationSubTypeSelection) {
				rval = rval + jcb.getText() + ";";
			}
			if (rval.endsWith(";"))
				rval = rval.substring(0, rval.length() - 1);
			((KeggRelationSubTypeAttribute) displayable).setString(rval);
		}
	}
}
