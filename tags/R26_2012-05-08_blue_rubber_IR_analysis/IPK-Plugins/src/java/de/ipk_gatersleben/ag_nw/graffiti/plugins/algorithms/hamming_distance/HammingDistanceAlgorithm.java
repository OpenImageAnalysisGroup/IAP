/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFileChooser;

import org.OpenFileDialogService;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * This algorithm computes the hamming distances between directed graphs. The
 * hamming distance is the number of pairwise differences between the
 * graphs, whereas nodes, edges, or both and also edge labels can be
 * considered. It uses the node labels to compare nodes and the labels of
 * nodes connected to the edge to compare edges. Each node may have a
 * distinguishable label.
 * 
 * @author Falk Schreiber
 */

/*
 * TODO: implement in check:
 * - check if the graphs are directed,
 * - check if the graph contains distinguished node labels
 * (and also if they contain a label for each node!)
 * possible extensions:
 * - consider:
 * - undirected graphs,
 * - directed graph as undirected graphs
 * - a given set of nodes and edges for computing
 * the hamming distance
 * (also update check precondition)
 * - set attributes to use the graph colorer algorithm to
 * show the differences in the graphs
 * - sort the graphs depending on the MIN SUM ordering
 */
public class HammingDistanceAlgorithm
					extends AbstractAlgorithm {
	/*************************************************************/
	/* Member variables */
	/*************************************************************/
	
	/**
	 * The list of current graphs
	 */
	// private Collection<Graph> listOfGraphs;
	
	/**
	 * The list of current sessions (for the graph names)
	 */
	// private Collection allSessions;
	
	/**
	 * The number of current graphs (size of listOfGraphs)
	 */
	// private int numberOfGraphs;
	
	/**
	 * Parameter to consider nodes
	 */
	private boolean consNodes = true;
	
	private boolean useLoadedGraphs;
	
	/**
	 * Parameter to consider edges
	 */
	private boolean consEdges = true;
	
	/**
	 * Parameter to consider edge labels
	 */
	private boolean consEdgeLabels;
	
	/**
	 * Parameter for hamming distance for nodes
	 */
	private int nodesDistance;
	
	/**
	 * Parameter for hamming distance for edges
	 */
	private int edgesDistance;
	
	/**
	 * Parameter to compute order of graphs
	 */
	private boolean computeOrder;
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name
	 */
	public String getName() {
		return "Hamming Distance";
	}
	
	/**
	 * Checks the preconditions of the algorithm.
	 * 
	 * @throws PreconditionException
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (useLoadedGraphs) {
			if (graph == null) {
				errors.add("The graph instance may not be null.");
			}
			Collection<Graph> listOfGraphs = GravistoService.getInstance().getMainGraphs();
			
			if (listOfGraphs == null || listOfGraphs.size() < 2) {
				errors.add("The list of graphs has to contain more than one graph.");
			}
			
			Iterator<Graph> it = listOfGraphs.iterator();
			
			while (it.hasNext()) {
				Graph g = (Graph) it.next();
				
				if (g.getNumberOfNodes() == 0) {
					errors.add("A graph may not be empty.");
				}
			}
		}
		if (nodesDistance <= 0) {
			errors.add("The value of the hamming distance between nodes may not <= 0.");
		}
		
		if (edgesDistance <= 0) {
			errors.add("The value of the hamming distance between edges may not <= 0.");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	/**
	 * The main method
	 */
	public void execute() {
		if (useLoadedGraphs) {
			Collection<Graph> listOfGraphs = GravistoService.getInstance().getMainGraphs();
			processGraphs(listOfGraphs, null);
		} else {
			ArrayList<File> files = new ArrayList<File>();
			JFileChooser fc = MainFrame.getInstance().getIoManager().createOpenFileChooser();
			fc.setMultiSelectionEnabled(true);
			// fc.resetChoosableFileFilters();
			
			OpenFileDialogService.setActiveDirectoryFor(fc);
			
			int returnVal = fc.showDialog(MainFrame.getInstance(), "Compute Hamming-Distances");
			
			OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] selfiles = fc.getSelectedFiles();
				// ask if they should be opened into one graph view
				for (File sf : selfiles)
					files.add(sf);
			}
			fc.setMultiSelectionEnabled(false);
			processGraphs(null, files);
		}
	}
	
	public static void main(String[] args) {
		if (args == null || args.length != 2) {
			System.out.println("VANTED COMMAND LINE ANALYSIS (HAMMING DISTANCES)");
			System.out.println("(c) 2006 by Christian Klukas, Falk Schreiber");
			System.out.println("Start:");
			System.out.println("Parameter 1 =  INDEX NUMBER");
			System.out.println("  (-negative values for test-calculation, return values are random values,");
			System.out.println("  0 means calculate everything)");
			System.out.println("Parameter 2 =  INPUT/OUTPUT FOLDER");
		} else {
			int index = 0;
			try {
				index = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				System.err.println("File-Index is no valid number!");
				return;
			}
			File dir = new File(args[1]);
			if (!dir.isDirectory()) {
				System.err.println("Input folder does not specify a directory!");
				return;
			}
			File[] files = dir.listFiles();
			ArrayList<File> filelist = new ArrayList<File>();
			for (File file : files) {
				if (file.getName().toUpperCase().endsWith(".GML"))
					filelist.add(file);
			}
			files = filelist.toArray(new File[] {});
			int fileCount = files.length;
			if (index > files.length) {
				System.err.println("File-Index too high, not enough input files!");
				return;
			}
			System.out.println("Hamming Distance Calculation " + Math.abs(index) + ":1-" + fileCount);
			if (index < 0)
				System.out.println("Calculation SIMULATION");
			System.out.println("Input/Output Folder: " + dir.getAbsolutePath());
			System.out.println("Input Files: " + files.length);
			HammingCalculator hc = new HammingCalculator(
								null, filelist,
								true, true, false, 1, 1, false, index, dir.getAbsolutePath());
			hc.run();
		}
		return;
	}
	
	private void processGraphs(Collection<Graph> listOfGraphs, ArrayList<File> listOfGraphFileNames) {
		HammingCalculator hc = new HammingCalculator(
							listOfGraphs, listOfGraphFileNames,
							consNodes, consEdges, consEdgeLabels, nodesDistance, edgesDistance, computeOrder, 0, null);
		BackgroundTaskHelper.issueSimpleTask("Hamming Distance Calculation", "Compute Hamming-Distances...", hc, null, hc);
	}
	
	/**
	 * Get parameters
	 * 
	 * @return Parameter
	 */
	@Override
	public Parameter[] getParameters() {
		BooleanParameter loadedGraphsParam =
							new BooleanParameter(
												false,
												"Analyze loaded graphs",
												"If selected, the currently loaded graphs are analyzed, if unselected, you may specify the list of graphs by selecting the corresponding graph files.");
		BooleanParameter nodesParam =
							new BooleanParameter(consNodes, "Consider nodes",
												"Consider nodes to compute the hamming distance.");
		BooleanParameter edgesParam =
							new BooleanParameter(consEdges, "Consider edges",
												"Consider edges to compute the hamming distance.");
		BooleanParameter edgeLabelsParam =
							new BooleanParameter(consEdgeLabels, "Consider edge labels",
												"Consider edge labels to compute the hamming distance.");
		BooleanParameter computeOrderParam =
							new BooleanParameter(computeOrder, "Compute order",
												"Compute MIN SUM ordering of graphs.");
		IntegerParameter nodesDistanceParam =
							new IntegerParameter(1, "Nodes distance",
												"Each different node increases the hamming distance by this value.");
		IntegerParameter edgesDistanceParam =
							new IntegerParameter(1, "Edges distance",
												"Each different edge increases the hamming distance by this value.");
		
		return new Parameter[] {
							loadedGraphsParam,
							nodesParam, edgesParam, edgeLabelsParam, computeOrderParam,
							nodesDistanceParam, edgesDistanceParam };
	}
	
	/**
	 * Sets parameters
	 * 
	 * @param params
	 *           DOCUMENT ME!
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.parameters = params;
		useLoadedGraphs = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		consNodes = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		consEdges = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		consEdgeLabels =
							((BooleanParameter) params[i++]).getBoolean().booleanValue();
		computeOrder =
							((BooleanParameter) params[i++]).getBoolean().booleanValue();
		nodesDistance =
							((IntegerParameter) params[i++]).getInteger().intValue();
		edgesDistance =
							((IntegerParameter) params[i++]).getInteger().intValue();
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
}
