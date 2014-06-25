/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image.CompoundAttribute;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class ShowOrHideImageAttributesAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return null;
		// return "Process Compound Images";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		String optionAdd = "Add Compound-Images";
		String optionRemove = "Remove Compound-Images";
		String response = (String) JOptionPane.showInputDialog(
							MainFrame.getInstance(), // parent
				"Select an command:", // message
				"Add/Remove Compound Images", // dialog title
				JOptionPane.QUESTION_MESSAGE, // icon type
				null, // no explicit icon
				new String[] { // choices
				optionAdd, optionRemove
				},
							"Add"); // default choice
		if (response != null && response.equals(optionAdd)) {
			Collection<Node> workNodes = getSelectedOrAllNodes();
			int cnt = 0;
			graph.getListenerManager().transactionStarted(this);
			for (Node n : workNodes) {
				String substanceName = AttributeHelper.getLabel(n, null);
				if (substanceName != null) {
					CompoundAttribute imgAtt = new CompoundAttribute("image_url");
					imgAtt.setString(substanceName);
					AttributeHelper.setAttribute(n, "image", "image_url", imgAtt);
					cnt++;
				}
			}
			graph.getListenerManager().transactionFinished(this);
			GraphHelper.issueCompleteRedrawForActiveView();
			MainFrame.showMessage(cnt + " image-attributes created or updated", MessageType.INFO, 3000);
		} else
			if (response != null && response.equals(optionRemove)) {
				Collection<Node> workNodes = getSelectedOrAllNodes();
				int cnt = 0;
				graph.getListenerManager().transactionStarted(this);
				for (Node n : workNodes) {
					try {
						n.removeAttribute("kegg" + Attribute.SEPARATOR + "kegg_image");
						cnt++;
					} catch (AttributeNotFoundException e) {
						// ignore
					}
				}
				graph.getListenerManager().transactionFinished(this);
				GraphHelper.issueCompleteRedrawForActiveView();
				MainFrame.showMessage(cnt + " image-attributes removed", MessageType.INFO, 3000);
			} else
				MainFrame.showMessage("Image-attributes not processed", MessageType.INFO, 3000);
	}
}
