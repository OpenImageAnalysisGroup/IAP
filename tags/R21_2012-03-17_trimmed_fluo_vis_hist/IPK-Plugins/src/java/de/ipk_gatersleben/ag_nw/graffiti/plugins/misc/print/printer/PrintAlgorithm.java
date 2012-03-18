/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $ID:$
 * Created on 10.07.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print.printer;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.KeyStroke;

import org.SystemInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print.PrintEnvironment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print.PrintTool;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter.PngJpegAlgorithm;

/**
 * The print algorithm.
 * 
 * @author Burkhard Sell
 * @version $Revision: 1.1 $
 */
public class PrintAlgorithm extends AbstractAlgorithm {
	
	private Session activeSession = null;
	
	private boolean printAllOpen = false;
	
	public PrintAlgorithm() {
		super();
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	public String getName() {
		return "Print...";
	}
	
	// public Parameter[] getParameters() {
	// return new Parameter[] {
	// new BooleanParameter(printAllOpen, "Print all loaded documents", "If enabled all open graph windows are printed at once.")
	// };
	// }
	//
	// public void setParameters(Parameter[] params) {
	// int i = 0;
	// printAllOpen = ((BooleanParameter)params[i++]).getBoolean();
	// }
	
	/**
	 * Unused for this plugin.
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
	 */
	@Override
	public void attach(Graph g, Selection s) {
		// simply do nothing
	}
	
	@Override
	public void check() throws PreconditionException {
		EditorSession session = GravistoService.getInstance().getMainFrame()
							.getActiveEditorSession();
		
		session.getActiveView();
		
		// if (!PNGAlgorithm.isViewTypeOK(theView)) {
		// throw new PreconditionException(
		// "The active view may not be processed.<br>" +
		// "Exporting a graphical view of this view type is not yet supported.");
		// }
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_P, SystemInfo.getAccelModifier());
	}
	
	/**
	 * This method is invoked by the plugin environment to start
	 * the action this plugin is for.
	 * <p>
	 * This method starts the printprocess
	 * </p>
	 * *
	 * <p>
	 * This method needs the activeSession set by the {@link #setActiveSession(Session) setActiveSession(Session)} Method. Make shure, that
	 * <code>setActiveSession(Session)</code> is called <strong>BEFORE</strong> <code>execute()</code> method!!!
	 * </p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 * @see #setActiveSession(Session)
	 * @throws IllegalStateException
	 *            if activeSession is null
	 */
	public void execute() {
		activeSession =
							GravistoService.getInstance().getMainFrame().getActiveSession();
		
		if (activeSession == null)
			throw new IllegalStateException(
								"No active session found. Ensure that "
													+ "setActiveSession is called BEFORE execute method!");
		
		if (!printAllOpen) {
			if (AbstractTool.getActiveTool() != null)
				AbstractTool.getActiveTool().preProcessImageCreation();
			
			PrintEnvironment.setZoomToOnePage(true);
			PngJpegAlgorithm.setDoubleBuffered(activeSession.getActiveView().getViewComponent(), false);
			// do the print using PrintTool
			PrintTool.print(activeSession.getActiveView());
			PngJpegAlgorithm.setDoubleBuffered(activeSession.getActiveView().getViewComponent(), true);
			if (AbstractTool.getActiveTool() != null)
				AbstractTool.getActiveTool().postProcessImageCreation();
		} else {
			Collection<View> views = new ArrayList<View>();
			for (Session s : MainFrame.getSessions()) {
				View validView = null;
				for (View v : s.getViews()) {
					if (v instanceof GraffitiView) {
						validView = v;
					}
				}
				if (validView != null) {
					views.add(validView);
				}
			}
			if (AbstractTool.getActiveTool() != null)
				AbstractTool.getActiveTool().preProcessImageCreation();
			
			PrintEnvironment.setZoomToOnePage(true);
			try {
				for (View v : views)
					PngJpegAlgorithm.setDoubleBuffered(v.getViewComponent(), false);
				PrintTool.print(views);
			} finally {
				for (View v : views)
					PngJpegAlgorithm.setDoubleBuffered(v.getViewComponent(), true);
			}
			if (AbstractTool.getActiveTool() != null)
				AbstractTool.getActiveTool().postProcessImageCreation();
		}
	}
	
	/**
	 * Resets the algorithm state.
	 * <p>
	 * Active session will be invalidated.
	 * </p>
	 * <p>
	 * <strong>ATTENTION</strong>: After calling this method {@link #setActiveSession(Session) setActiveSession(Session)} have to be called again BEFORE calling
	 * {@link #execute() execute()}
	 * <p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 * @see #setActiveSession(Session)
	 */
	@Override
	public void reset() {
		// invalidate active session by simply setting it to null
		activeSession = null;
	}
	
	/**
	 * Sets the active session.
	 * <p>
	 * This session is uses by {@link #execute() execute()} Method. Make shure, that this method is called <strong>BEFORE</strong> <code>execute()</code>!!!
	 * </p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#setActiveSession(org.graffiti.session.Session)
	 * @see #execute()
	 */
	public void setActiveSession(Session session) {
		// save the active session
		this.activeSession = session;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#isLayoutAlgorithm()
	 */
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
}
