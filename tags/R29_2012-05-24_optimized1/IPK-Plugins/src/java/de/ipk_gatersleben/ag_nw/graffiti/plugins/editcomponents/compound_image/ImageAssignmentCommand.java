/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.04.2007 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class ImageAssignmentCommand extends AbstractAlgorithm {
	
	private String imageUrl = "";
	private String imagePos = GraphicAttributeConstants.AUTO_OUTSIDE;
	private JTextField tf;
	private JButton bt;
	
	public void execute() {
		if (imageUrl == null || imageUrl.length() <= 0)
			return;
		try {
			graph.getListenerManager().transactionStarted(this);
			for (Node n : getSelectedOrAllNodes()) {
				AttributeHelper.setAttribute(n, "image", "image_url", imageUrl);
				AttributeHelper.setAttribute(n, "image", "image_position", imagePos);
			}
		} finally {
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command a image file may be assigned to the selected nodes.<br>" +
							"The image files are linked and not included in the graph file. Thus you should specify<br>" +
							"a accessible ressource, as the file is downloaded or loaded from the specified URL.<br>" +
							"The image files will be cached on disk to speed up later loading and processing of the<br>" +
							"graph.<br>" +
							"Use the Node attribute editor to change the positioning of the image.<br>" +
							"Default init position of the images is outside of the nodes.<br>" +
							"In case the image position is set to `centered', the node size will be<br>" +
							"increased in case the image does not fit into the node shape.";
	}
	
	@Override
	public Parameter[] getParameters() {
		String text = "";
		for (Node nd : selection.getNodes())
			if (AttributeHelper.hasAttribute(nd, "image", "image_url")) {
				text = (String) AttributeHelper.getAttributeValue(nd, "image", "image_url", "", "", false);
				break;
			}
		tf = new JTextField(text);
		bt = new JButton("Search");
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File f = org.OpenFileDialogService.getFile(new String[] { "png", "gif", "jpg" }, "Image Files");
				if (f != null)
					tf.setText(f.getAbsolutePath());
			}
		});
		
		return new Parameter[] {
							new org.graffiti.plugin.parameter.JComponentParameter(info.clearthought.layout.TableLayout.getSplit(tf, bt, -1.0d, -2.0), "Image URL",
												"A (web)-URL to an image file"),
							// new StringParameter("", "Image URL",
				// "A URL to a image file"),
				new ObjectListParameter(imagePos, "Initial Image Position",
												"You may change the image position at a later point in time from the Node side panel",
												CompoundImagePositionAttributeEditor.getPosiblePositions(false)) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 1;
		imageUrl = tf.getText();
		imagePos = (String) ((ObjectListParameter) params[i++]).getValue();
	}
	
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Embed Image...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
}
