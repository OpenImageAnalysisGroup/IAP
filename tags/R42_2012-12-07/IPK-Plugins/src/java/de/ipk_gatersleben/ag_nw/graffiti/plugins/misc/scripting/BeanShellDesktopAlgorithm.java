/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.scripting;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Provides a Bean Shell Desktop for the execution of Bean Shell Scripts.
 * 
 * @author Dirk Koschützki
 * @author Christian Klukas
 */
public class BeanShellDesktopAlgorithm
					extends AbstractAlgorithm {
	
	/**
	 * Creates a new BeanShellDesktopAlgorithm object.
	 */
	public BeanShellDesktopAlgorithm() {
	}
	
	/**
	 * Returns the algorithms name.
	 * 
	 * @return the algorithms name
	 */
	public String getName() {
		return "Show BeanShell";
	}
	
	@Override
	public String getCategory() {
		return "menu.window";
	}
	
	/**
	 * No special checks implemented.
	 */
	@Override
	public void check()
						throws PreconditionException {
		super.check();
	}
	
	/**
	 * Starts the Bean Shell Desktop.
	 */
	public void execute() {
		EditorSession session =
							GravistoService.getInstance().getMainFrame()
												.getActiveEditorSession();
		
		Selection selection =
							session.getSelectionModel().getActiveSelection();
		
		Interpreter interpreter = new Interpreter();
		
		try {
			interpreter.set("graph", graph);
			interpreter.set("selection", selection);
			interpreter.set("session", session);
			interpreter.eval("importCommands(\"commands\")");
			interpreter.eval("graffitiDesktop();");
			
		} catch (EvalError e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/**
	 * No special reset implemented.
	 */
	@Override
	public void reset() {
		super.reset();
	}
}
