/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.OpenFileDialogService;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateGOtreeAlgorithm extends AbstractAlgorithm {
	
	private GoProcessing gp = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create Complete Gene Ontology Network";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("No active graph window");
		super.check();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		if (gp == null) {
			File obofile = OpenFileDialogService.getFile(new String[] { ".obo-xml" }, "Gene Ontology File (*.obo-xml)");
			if (obofile == null)
				return;
			gp = new GoProcessing(obofile);
			if (!gp.isValid()) {
				gp = null;
				MainFrame.showMessageDialog("The input file could not be loaded. It may not be a valid gene-ontology obo-xml file!", "Error");
				return;
			}
		}
		final Graph workGraph = graph;
		workGraph.getListenerManager().transactionStarted(workGraph);
		final BackgroundTaskStatusProviderSupportingExternalCallImpl sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Create GO Tree...",
							"Please wait");
		BackgroundTaskHelper.issueSimpleTask("Create GO Tree", "Create GO Tree...", new Runnable() {
			public void run() {
				int startNodeCnt = workGraph.getNumberOfNodes();
				
				PositionGridGenerator pgg = new PositionGridGenerator(250, 30, 250);
				
				HashMap<String, Node> goTerm2goNode = new HashMap<String, Node>();
				Collection<String> goTerms = gp.getAllGoTerms();
				int i = 0;
				int workLoad = goTerms.size();
				for (String goTerm : goTerms) {
					InterpreteGOtermsAlgorithm.processGoHierarchy(pgg, goTerm2goNode, gp, goTerm, workGraph);
					sp.setCurrentStatusValueFine(100d * (workGraph.getNumberOfNodes() - startNodeCnt) / workLoad);
					sp.setCurrentStatusText2("Evaluate " + goTerm + " (" + (++i) + "/" + workLoad + ")");
					if (sp.wantsToStop())
						break;
				}
				sp.setCurrentStatusText1("Finish transaction (update view, please wait)...");
				if (sp.wantsToStop())
					sp.setCurrentStatusText2("Processing aborted");
				else
					sp.setCurrentStatusText2("Processing completed");
				sp.setCurrentStatusValue(100);
			}
		}, new Runnable() {
			
			public void run() {
				workGraph.getListenerManager().transactionFinished(workGraph);
			}
		}, sp);
	}
}
