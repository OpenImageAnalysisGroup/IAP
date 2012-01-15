/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import javax.swing.JComponent;

import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ProjectEntity {
	
	private String experimentName;
	// private Document document;
	
	// private Element measurementCountElement;
	private JComponent gui;
	private String expType = "Loaded Experiment";
	private ExperimentInterface md;
	
	@Deprecated
	public ProjectEntity(String expTitle, Document document) {
		this.experimentName = expTitle;
		this.setDocument(document);
	}
	
	public ProjectEntity(String expTitle, ExperimentInterface md) {
		this.experimentName = expTitle;
		this.md = md;
	}
	
	public ProjectEntity(String expTitle, ExperimentInterface md, JComponent gui) {
		this.experimentName = expTitle;
		this.md = md;
		this.gui = gui;
	}
	
	@Deprecated
	public ProjectEntity(String expTitle, Document document, JComponent gui) {
		this.experimentName = expTitle;
		this.setDocument(document);
		this.gui = gui;
	}
	
	@Deprecated
	public ProjectEntity(String expTitle, String expType, Document document, JComponent gui) {
		this.experimentName = expTitle;
		this.setDocument(document);
		this.gui = gui;
		this.expType = expType;
	}
	
	public ProjectEntity(String targetExperiment) {
		this.experimentName = targetExperiment;
		setDocument(null);
	}
	
	@Override
	public String toString() {
		return "[" + expType + "] " + experimentName;
	}
	
	/**
	 * Use getDocumentData instead, if possible.
	 * 
	 * @return
	 */
	@Deprecated
	public Document getDocument() {
		return Experiment.getDocuments(md).iterator().next();
		// return document;
	}
	
	public ExperimentInterface getDocumentData() {
		return md;
	}
	
	public String getExperimentName() {
		return experimentName;
	}
	
	public JComponent getGUI() {
		return gui;
	}
	
	private void setDocument(Document document) {
		Experiment md = Experiment.getExperimentFromDOM(document);
		this.md = md;
		// this.document = document;
	}
}
