/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.KeyStroke;

import org.Release;
import org.ReleaseInfo;
import org.SystemInfo;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.jdom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

public class EvaluateKGML extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Evaluate KGML/Update View";
		else
			return null;
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke('K', SystemInfo.getAccelModifier());
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		evaluateAndUpdateGraph(graph);
	}
	
	public static void evaluateAndUpdateGraph(Graph graph) {
		try {
			graph.getListenerManager().transactionStarted(graph);
			Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
			Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
			HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
			Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.clear();
			p2.getGraph(graph);
		} finally {
			graph.getListenerManager().transactionFinished(graph);
		}
	}
	
}