package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.actions.URLattributeAction;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.MyNonInteractiveSpringEmb;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.myOp;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references.PathwayReferenceAutoCreationAlgorithm;

public class LoadGraphFileAttributeAction implements URLattributeAction {
	
	public ActionListener getActionListener(final Attribute displayable,
						final Graph graph, final GraphElement ge, final boolean performAltCommand) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String val = (String) displayable.getValue();
				Node n = null;
				if (ge != null && ge instanceof Node) {
					n = (Node) ge;
				}
				if (!performAltCommand)
					loadFile(val, graph, n, e);
				else
					addMapLink(val, graph, n, e);
				
			}
		};
	}
	
	public String getPreIdentifyer() {
		return AttributeHelper.preFilePath;
	}
	
	private void addMapLink(String fileName, Graph g, Node initialMapNode, ActionEvent ae) {
		if (fileName != null && fileName.startsWith(getPreIdentifyer()))
			fileName = fileName.substring(getPreIdentifyer().length());
		if (fileName == null || fileName.length() <= 0) {
			MainFrame.showMessageDialog("No file name given! Can't load referenced network.", "Error");
		}
		Object[] res = MyInputHelper.getInput(
							"<html>" +
												"With this command, a new map link node will be created and linked<br>" +
												"to the node you used to open the context menu.<br>" +
												"If the first option is enabled, a existing map-link node will be used,<br>" +
												"if available. The second option determines, if all nodes with the same<br>" +
												"ID or label will be linked to the map-link node, or if only the<br>" +
												"selected noded will be processed.",
							"Add Map-Link Node",
							new Object[] {
												"Re-use existing map link nodes", true,
												"Link all nodes with same label", true,
												"Link all nodes with same target", false
				});
		if (res == null)
			return;
		int i = 0;
		boolean searchExistingMapLinkNode = (Boolean) res[i++];
		boolean searchAndLinkSimilarNodes = (Boolean) res[i++];
		boolean searchAndLinkSameTarget = (Boolean) res[i++];
		HashSet<Node> knownNodes = new HashSet<Node>(g.getNodes());
		Node pathwayLinkNode = PathwayReferenceAutoCreationAlgorithm.addMapLinkNode(fileName, g, initialMapNode, ae,
							searchExistingMapLinkNode, searchAndLinkSimilarNodes, searchAndLinkSameTarget);
		if (pathwayLinkNode != null && !knownNodes.contains(pathwayLinkNode)) {
			Selection selection = new Selection("newMapLinkNode");
			selection.add(pathwayLinkNode);
			ThreadSafeOptions tso = MyNonInteractiveSpringEmb.getNewThreadSafeOptionsWithDefaultSettings();
			tso.setDval(myOp.DvalIndexSliderZeroLength, 200);
			MyNonInteractiveSpringEmb se = new MyNonInteractiveSpringEmb(g, selection, tso);
			se.run();
		}
	}
	
	private void loadFile(String fileName, Graph g, Node initialMapNode, ActionEvent ae) {
		if (fileName != null && fileName.startsWith(getPreIdentifyer()))
			fileName = fileName.substring(getPreIdentifyer().length());
		if (fileName == null || fileName.length() <= 0) {
			MainFrame.showMessageDialog("No file name given! Can't load referenced network.", "Error");
		}
		if (MainFrame.getInstance().lookUpAndSwitchToNamedSession(fileName))
			return;
		File file = checkFile(g, fileName);
		// check for graphs in the same directory
		file = checkFile(g, new File(g.getName(true)).getParent() + "/" + fileName);
		
		if (!file.exists())
			MainFrame.showMessageDialog("File " + file.getAbsolutePath() + " could not be found!", "Error");
		else {
			if (!file.canRead())
				MainFrame.showMessageDialog("File " + fileName + " can not be read!", "Error");
			else {
				try {
					MainFrame.getInstance().loadGraphInBackground(file, ae, true);
				} catch (IllegalAccessException e) {
					ErrorMsg.addErrorMessage(e);
				} catch (InstantiationException e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
	}
	
	private File checkFile(Graph g, String fileName) {
		java.io.File file = new File(fileName);
		
		if (!file.exists() || !file.canRead())
			for (org.graffiti.session.Session s : MainFrame.getSessions()) {
				if (s instanceof EditorSession) {
					EditorSession es = (EditorSession) s;
					if (es.getGraph() == g) {
						String path = "";
						if (es.getFileNameFull() != null)
							path = es.getFileNameFull();
						if (path.lastIndexOf('/') > 0)
							path = path.substring(0, path.lastIndexOf('/')) + '/';
						else
							if (path.lastIndexOf('\\') > 0)
								path = path.substring(0, path.lastIndexOf('\\')) + '\\';
						File filet = new File(path + fileName);
						if (filet.exists()) {
							return filet;
						} else
							if (fileName.indexOf("/") < 0 && fileName.indexOf("\\") < 0)
								return filet;
					}
				}
			}
		return file;
	}
	
	public String getCommandDescription(boolean shortDesc, boolean altDesc) {
		if (shortDesc) {
			if (altDesc)
				return "Add Map-Link: ";
			else
				return "Load File: ";
		} else {
			if (altDesc)
				return "Add Map-Link";
			else
				return "Load File";
		}
	}
	
	public boolean supportsModifyCommand() {
		return true;
	}
}
