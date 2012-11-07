package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.ErrorMsg;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.LVL3ModelConverter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.PathWayHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.gui.CheckTreeManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.LVL2ModelConverter;

/**
 * Imports Level 2 and 3 OWL Files to Vanted Graph. Uses two packages, one for
 * each level.
 * 
 * @author ricardo
 */
public class BioPAX_OWL_Reader extends AbstractInputSerializer
{
	/**
	 * opens a dialog for asking whether to import the whole OWL File or to
	 * import just a subset of pathways
	 * 
	 * @return
	 */
	private String askForPathWays()
	{
		int answer = JOptionPane.showOptionDialog(JOptionPane.getRootFrame().getFocusOwner(), "Which BioPax-PathWays do you want to import?",
				"Please choose an option!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] {
						"I want to import the whole model at once.", "I want to select some pathways." }, null);
		if (answer == 0)
			return "all";
		else
			return "some";
	}
	
	/**
	 * opens a Dialog for choosing Pathways out of level 2 OWL Files
	 * 
	 * @param arrayList
	 * @return
	 */
	private ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> askWhichPathWaysLvl2(
			ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> arrayList)
	{
		// creates a tree structure view of the pathways
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("All");
		HashMap<String, String> myMap = new HashMap<String, String>();
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay myPath : arrayList)
		{
			String pathRDFId = myPath.getRDFId();
			DefaultMutableTreeNode Sub;
			if (myPath.getDisplayName().length() > 0)
			{
				Sub = new DefaultMutableTreeNode(myPath.getDisplayName());
				myMap.put(myPath.getDisplayName(), pathRDFId);
			} else
			{
				Sub = new DefaultMutableTreeNode(pathRDFId);
				myMap.put(pathRDFId, pathRDFId);
			}
			
			top.add(Sub);
			findSubPathsLvL2(Sub, myPath);
		}
		// makes your tree as CheckTree
		
		JTree myTree = new JTree(top);
		CheckTreeManager checkTreeManager = new CheckTreeManager(myTree);
		JScrollPane myScroll = new JScrollPane(myTree);
		myScroll.setPreferredSize(new Dimension(600, 400));
		myScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		myScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), myScroll, "Which BioPax-Pathways do you want?", JOptionPane.OK_OPTION);
		
		// to get the paths that were checked
		TreePath checkedPaths[] = checkTreeManager.getSelectionModel().getSelectionPaths();
		ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> selectedPaths = new ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay>();
		for (TreePath P : checkedPaths)
		{
			String RDFId = myMap.get(P.getLastPathComponent().toString());
			selectedPaths.add(findMyPathWaylvl2(RDFId, arrayList));
		}
		if (checkedPaths.length == 1 && checkedPaths[0].getLastPathComponent().toString().matches("All"))
			return arrayList;
		return selectedPaths;
		
	}
	
	/**
	 * opens a Dialog for choosing Pathways out of level 3 OWL Files
	 * 
	 * @param arrayList
	 * @return
	 */
	private ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> askWhichPathWaysLvl3(
			ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> arrayList)
	{
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("All");
		HashMap<String, String> myMap = new HashMap<String, String>();
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay myPath : arrayList)
		{
			String pathRDFId = myPath.getRDFId();
			DefaultMutableTreeNode Sub;
			if (myPath.getDisplayName().length() > 0)
			{
				Sub = new DefaultMutableTreeNode(myPath.getDisplayName());
				myMap.put(myPath.getDisplayName(), pathRDFId);
			} else
			{
				Sub = new DefaultMutableTreeNode(pathRDFId);
				myMap.put(pathRDFId, pathRDFId);
			}
			
			top.add(Sub);
			findSubPathsLvL3(Sub, myPath);
		}
		// makes your tree as CheckTree
		
		JTree myTree = new JTree(top);
		CheckTreeManager checkTreeManager = new CheckTreeManager(myTree);
		JScrollPane myScroll = new JScrollPane(myTree);
		myScroll.setPreferredSize(new Dimension(600, 400));
		myScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		myScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), myScroll, "Which BioPax-Pathways do you want?", JOptionPane.OK_OPTION);
		
		// to get the paths that were checked
		TreePath checkedPaths[] = checkTreeManager.getSelectionModel().getSelectionPaths();
		ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> selectedPaths = new ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay>();
		
		if (checkedPaths == null || checkedPaths.length == 0)
			return selectedPaths;
		
		for (TreePath P : checkedPaths)
		{
			String RDFId = myMap.get(P.getLastPathComponent().toString());
			
			selectedPaths.add(findMyPathWaylvl3(RDFId, arrayList));
		}
		if (checkedPaths.length == 1 && checkedPaths[0].getLastPathComponent().toString().matches("All"))
			return arrayList;
		return selectedPaths;
	}
	
	/**
	 * finds level2 to pathways and all of its children and adds them to the OWL
	 * file
	 * 
	 * @param RDFId
	 * @param arrayList
	 * @return
	 */
	private de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay findMyPathWaylvl2(String RDFId,
			ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> arrayList)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay P : arrayList)
		{
			if (P.getRDFId().matches(RDFId))
				return P;
			de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay temp = findMyPathWaylvl2(RDFId, P.getSubPathWays());
			if (temp != null)
				return temp;
		}
		return null;
	}
	
	/**
	 * finds level2 to pathways and all of its children and adds them to the OWL
	 * file
	 * 
	 * @param RDFId
	 * @param arrayList
	 * @return
	 */
	private de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay findMyPathWaylvl2(String RDFId,
			Set<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> subPathWays)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay P : subPathWays)
		{
			if (P.getRDFId().matches(RDFId))
				return P;
			de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay temp = findMyPathWaylvl2(RDFId, P.getSubPathWays());
			if (temp != null)
				return temp;
		}
		return null;
	}
	
	/**
	 * finds level3 to pathways and all of its children and adds them to the OWL
	 * file
	 * 
	 * @param RDFId
	 * @param arrayList
	 * @return
	 */
	private MyPathWay findMyPathWaylvl3(String RDFId, ArrayList<MyPathWay> arrayList)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay P : arrayList)
		{
			if (P.getRDFId().matches(RDFId))
				return P;
			de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay temp = findMyPathWaylvl3(RDFId, P.getSubPathWays());
			if (temp != null)
				return temp;
		}
		return null;
	}
	
	/**
	 * finds level3 to pathways and all of its children and adds them to the OWL
	 * file
	 * 
	 * @param RDFId
	 * @param arrayList
	 * @return
	 */
	private de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay findMyPathWaylvl3(String RDFId,
			Set<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> subPathWays)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay P : subPathWays)
		{
			if (P.getRDFId().matches(RDFId))
				return P;
			de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay temp = findMyPathWaylvl3(RDFId, P.getSubPathWays());
			if (temp != null)
				return temp;
		}
		return null;
	}
	
	/**
	 * function for filling the view of pathways
	 * 
	 * @param top
	 * @param myPath
	 */
	private void findSubPathsLvL2(DefaultMutableTreeNode top, de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay myPath)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay comps : myPath.getSubPathWays())
		{
			String showName = myPath.getRDFId();
			DefaultMutableTreeNode Sub;
			if (myPath.getDisplayName().length() > 0)
			{
				Sub = new DefaultMutableTreeNode(showName + "-:-" + myPath.getDisplayName());
			} else
			{
				Sub = new DefaultMutableTreeNode(showName);
			}
			top.add(Sub);
			findSubPathsLvL2(Sub, comps);
		}
		
	}
	
	/**
	 * function for filling the view of pathways
	 * 
	 * @param top
	 * @param myPath
	 */
	private void findSubPathsLvL3(DefaultMutableTreeNode top, de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay myPath)
	{
		
		for (MyPathWay comps : myPath.getSubPathWays())
		{
			String showName = myPath.getRDFId();
			DefaultMutableTreeNode Sub;
			if (myPath.getDisplayName().length() > 0)
			{
				Sub = new DefaultMutableTreeNode(showName + "-:-" + myPath.getDisplayName());
			} else
			{
				Sub = new DefaultMutableTreeNode(showName);
			}
			top.add(Sub);
			findSubPathsLvL3(Sub, comps);
		}
		
	}
	
	@Override
	public String[] getExtensions()
	{
		return new String[] { ".owl" }; //$NON-NLS-1$
	}
	
	@Override
	public String[] getFileTypeDescriptions()
	{
		return new String[] { "BioPax" }; //$NON-NLS-1$
	}
	
	/**
	 * main read method
	 */
	@Override
	public void read(InputStream in, Graph g) throws IOException
	{
		Model model = null;
		try
		{
			// TODO: Validator doesn't work right
			
			/*
			 * TODO: FileName as attribute on the graph
			 * System.out.println(g.getName());
			 */
			
			// convert model into graph
			BioPAXIOHandler handler = new SimpleIOHandler();
			
			// This will auto-detect the level.
			handler.fixReusedPEPs(false);
			
			model = handler.convertFromOWL(in);
			
		} catch (Exception e)
		{
			ErrorMsg.addErrorMessage(e);
		}
		// from now on own code part
		String answer = askForPathWays();
		if (answer.matches("all"))
		{
			if (model.getLevel() == BioPAXLevel.L3)
			{
				// initialize hand-written ModelConverter
				LVL3ModelConverter mc = new LVL3ModelConverter();
				try
				{
					// try to convert the model from the stream to a graph
					mc.convertLVL3Model(model, g, true);
					PathWayLoaderLvL3 pwl = new PathWayLoaderLvL3(model, g);
				} catch (Exception e)
				{
					ErrorMsg.addErrorMessage(e);
				}
				// return graph to vanted
				g = mc.getGraph();
				g.setString(Messages.getString("UtilitySuperClassToGraph.114"), BioPAXLevel.L3.name()); //$NON-NLS-1$
			}
			if (model.getLevel() == BioPAXLevel.L2)
			{
				
				// initialize hand-written ModelConverter
				LVL2ModelConverter mc = new LVL2ModelConverter();
				try
				{
					// try to convert the model from the stream to a graph
					mc.convertLVL2Model(model, g, true);
					PathWayLoaderLvL2 pwl = new PathWayLoaderLvL2(model, g);
				} catch (Exception e)
				{
					ErrorMsg.addErrorMessage(e);
				}
				// return graph to vanted
				g = mc.getGraph();
				g.setString(Messages.getString("UtilitySuperClassToGraph.114"), BioPAXLevel.L2.name()); //$NON-NLS-1$
			}
		} else
		{
			if (model.getLevel() == BioPAXLevel.L3)
			{
				// initialize hand-written ModelConverter
				LVL3ModelConverter mc = new LVL3ModelConverter();
				try
				{
					// try to convert the model from the stream to a graph
					mc.convertLVL3Model(model, g, false);
					PathWayHandler handler = new PathWayHandler(model, g);
					handler.getPathWaysfromModel();
					if (handler.getPathWays().isEmpty())
					{
						// keine Pathways im Model
						// lade gesamten Graphen
						JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "No PathWays in the model.");
						mc.convertLVL3Model(model, g, true);
					} else
						if (handler.getPathWays().size() == 1)
						{
							// nur ein Pathways im Model, es macht keinen Sinn zu
							// fragen welcher geladen werden soll
							// lade gesamten Graphen
							JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "There was only one Pathway, so this one will be loaded");
							mc.convertLVL3Model(model, g, true);
							PathWayLoaderLvL3 pwl = new PathWayLoaderLvL3(model, g);
						} else
						{
							// lade nur selektierten Graphen
							ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> selectedPaths = askWhichPathWaysLvl3(handler
									.getPathWays());
							mc.readInteractionsFromPathway(model, selectedPaths);
							PathWayLoaderLvL3 pwl = new PathWayLoaderLvL3(model, g);
						}
					
					// return graph to vanted
					g = mc.getGraph();
					g.setString(Messages.getString("UtilitySuperClassToGraph.114"), BioPAXLevel.L3.name()); //$NON-NLS-1$
				} catch (Exception e)
				{
					ErrorMsg.addErrorMessage(e);
				}
			}
			if (model.getLevel() == BioPAXLevel.L2)
			{
				// initialize hand-written ModelConverter
				LVL2ModelConverter mc = new LVL2ModelConverter();
				try
				{
					// try to convert the model from the stream to a graph
					mc.convertLVL2Model(model, g, false);
					de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.PathWayHandler handler = new de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.PathWayHandler(
							model, g);
					handler.getPathWaysfromModel();
					if (handler.getPathWays().isEmpty())
					{
						// keine Pathways im Model
						// lade gesamten Graphen
						JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "No PathWays in the model.");
						mc.convertLVL2Model(model, g, true);
					} else
						if (handler.getPathWays().size() == 1)
						{
							// nur ein Pathways im Model, es macht keinen Sinn zu
							// fragen welcher geladen werden soll
							// lade gesamten Graphen
							JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), "There was only one Pathway, so this one will be loaded");
							mc.convertLVL2Model(model, g, true);
							PathWayLoaderLvL2 pwl = new PathWayLoaderLvL2(model, g);
						} else
						{
							ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions.MyPathWay> selectedPaths = askWhichPathWaysLvl2(handler
									.getPathWays());
							mc.readInteractionsFromPathway(model, selectedPaths);
							PathWayLoaderLvL2 pwl = new PathWayLoaderLvL2(model, g);
						}
					
					// return graph to vanted
					g = mc.getGraph();
					g.setString(Messages.getString("UtilitySuperClassToGraph.114"), BioPAXLevel.L3.name()); //$NON-NLS-1$
				} catch (Exception e)
				{
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
	}
	
	@Override
	public void read(Reader reader, Graph newGraph) throws Exception
	{
	}
	
}
