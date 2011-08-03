/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 13.10.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.session.EditorSession;

import bsh.EvalError;
import bsh.Interpreter;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * This class provides the menu entries for the BeanShell-Script Menu Items.
 * This modified Menu Items provide the additional methods for storing the
 * beanshell commando file name.
 * 
 * @author klukas To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BSHscriptMenuEntry extends JMenuItem {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The beanshell commando file-name
	 */
	private final String cmdFile;
	
	private boolean individualNodeCommand = false;
	
	/**
	 * The menu title
	 */
	private final String menuTitle;
	
	/**
	 * Creates a new ScriptMenuEntry object.
	 * 
	 * @param title
	 *           Label text for the menu item
	 * @param commandFile
	 *           The name of the beanshell command file
	 */
	public BSHscriptMenuEntry(String title, String commandFile, boolean nodeCommand) {
		cmdFile = commandFile;
		menuTitle = title;
		
		individualNodeCommand = nodeCommand;
	}
	
	/**
	 * Returns the name of the beanshell commando file for this menuitem.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getCmdFile() {
		return cmdFile;
	}
	
	public String getCmdFileSrc() {
		return DefaultContextMenuManager.getContent(getCmdFile());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.AbstractButton#getText()
	 */
	@Override
	public String getText() {
		return menuTitle;
	}
	
	public boolean isIndividualNodeCommand() {
		return individualNodeCommand;
	}
	
	public static void executeScript(final BSHinfo info, String title) {
		final Interpreter interpr = new Interpreter(); // Construct an interpreter
		
		try {
			EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
			interpr.eval("import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.*;");
			interpr.set("session", session);
			interpr.set("graph", GravistoService.getInstance().getMainFrame().getActiveSession().getGraph());
			interpr.set("worknodes", GraphHelper.getSelectedOrAllHelperNodes(session));
		} catch (EvalError e) {
			ErrorMsg.addErrorMessage(e);
		} catch (NullPointerException e) {
			// missing information may be checked or produce error inside the
			// script
			// some scripts might not use the current graph -> ignore error
		}
		if (info.nodeCommand) {
			final Graph g = MainFrame.getInstance().getActiveEditorSession().getGraph();
			final BackgroundTaskStatusProviderSupportingExternalCall myStatusProvider = new BackgroundTaskStatusProviderSupportingExternalCall() {
				double progress = -1;
				boolean stop = false;
				String status1 = "Perform Script Command...";
				String status2 = "";
				
				public int getCurrentStatusValue() {
					return (int) progress;
				}
				
				public void setCurrentStatusValue(int value) {
					progress = value;
				}
				
				public double getCurrentStatusValueFine() {
					return progress;
				}
				
				public String getCurrentStatusMessage1() {
					return status1;
				}
				
				public String getCurrentStatusMessage2() {
					return status2;
				}
				
				public void pleaseStop() {
					stop = true;
				}
				
				public boolean pluginWaitsForUser() {
					return false;
				}
				
				public void pleaseContinueRun() {
				}
				
				public void setCurrentStatusValueFine(double value) {
					progress = value;
				}
				
				public boolean wantsToStop() {
					return stop;
				}
				
				public void setCurrentStatusText1(String status) {
					status1 = status;
				}
				
				public void setCurrentStatusText2(String status) {
					status2 = status;
				}
				
				public void setCurrentStatusValueFineAdd(double smallProgressStep) {
					progress += smallProgressStep;
				}
			};
			BackgroundTaskHelper.issueSimpleTask(title, "Perform Script Command...", new Runnable() {
				public void run() {
					List<Node> nl = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
					g.numberGraphElements();
					g.getListenerManager().transactionStarted(this);
					int i = 0;
					int max = nl.size();
					for (Iterator<Node> nit = nl.iterator(); nit.hasNext();) {
						Node n = nit.next();
						myStatusProvider.setCurrentStatusText2("Process node " + (i + 1) + "/" + max + "...");
						try {
							interpr.set("node", new NodeHelper(n, nit.hasNext()));
							interpr.eval(info.cmdsrc);
						} catch (EvalError e) {
							ErrorMsg.addErrorMessage(e);
						}
						i++;
						myStatusProvider.setCurrentStatusValueFine(100d * i / max);
						if (myStatusProvider.wantsToStop())
							break;
					}
					g.getListenerManager().transactionFinished(this);
					if (myStatusProvider.wantsToStop())
						myStatusProvider.setCurrentStatusText2("Processing aborted (node " + i + "/" + max + ")");
					else
						myStatusProvider.setCurrentStatusText2("Processing finished (node " + i + ")");
					myStatusProvider.setCurrentStatusValueFine(100d);
				}
			}, null, myStatusProvider);
		} else {
			try {
				interpr.eval(info.cmdsrc);
			} catch (EvalError e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	public void execute() {
		BSHinfo info = new BSHinfo(FileSystemHandler.getURL(new File(cmdFile)));
		executeScript(info, getName());
	}
	
	/**
	 * @param firstLine
	 * @return Modified firstLine (if it is a node command), otherwise return
	 *         NULL
	 */
	public static String isNodeCommand(String firstLine) {
		if (firstLine.indexOf("§") > 0 || firstLine.indexOf("^") > 0) {
			if (firstLine.indexOf("§") > 0)
				firstLine = firstLine.substring(0, firstLine.indexOf("§"));
			if (firstLine.indexOf("^") > 0)
				firstLine = firstLine.substring(0, firstLine.indexOf("^"));
			return firstLine;
		}
		return null;
	}
}
