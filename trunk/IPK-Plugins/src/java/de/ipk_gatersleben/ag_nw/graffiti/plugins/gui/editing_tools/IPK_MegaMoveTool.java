/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.Component;
import java.awt.Event;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKnodeComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.rotate.RotateAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit.ZoomFitChangeComponent;

/**
 * A modified editing tool
 * 
 * @author Christian Klukas
 * @version $Revision: 1.2 $
 */
public class IPK_MegaMoveTool
					extends MegaMoveTool
					implements MouseWheelListener {
	
	RotateAlgorithm ra = new RotateAlgorithm();
	
	public IPK_MegaMoveTool() {
		super();
		final Tool thisTool = this;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				thisTool.deactivateAll();
				thisTool.activate();
				AbstractTool.lastActiveTool = thisTool;
			}
		});
	}
	
	@Override
	protected void postProcessVisibilityChange(
						GraphElement sourceElementGUIinteraction) {
		super.postProcessVisibilityChange(sourceElementGUIinteraction);
		if (sourceElementGUIinteraction == null)
			return;
		if (PreferencesDialog.activeStartLayoutButton != null &&
							PreferencesDialog.activeStartLayoutButton.isEnabled()) {
			
			Vector2d oldPosition = null;
			if (sourceElementGUIinteraction instanceof Node)
				oldPosition = AttributeHelper.getPositionVec2d((Node) sourceElementGUIinteraction);
			
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(new Selection("empty"));
			PreferencesDialog.activeStartLayoutButton.doClick(100);
			
			Vector2d newPosition = null;
			if (sourceElementGUIinteraction instanceof Node) {
				newPosition = AttributeHelper.getPositionVec2d((Node) sourceElementGUIinteraction);
				GraphHelper.moveGraph(sourceElementGUIinteraction.getGraph(),
									oldPosition.x - newPosition.x, oldPosition.y - newPosition.y);
			}
			
			Selection ss = new Selection("selection");
			ss.add(sourceElementGUIinteraction);
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(ss);
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
			MainFrame.showMessage("Layout has been updated, select the Null-Layout to disable automatic re-layout", MessageType.INFO);
		}
		
	}
	
	private long lastClick_ipk = Long.MIN_VALUE;
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getWhen() <= lastClick_ipk)
			return;
		lastClick_ipk = e.getWhen();
		synchronized (GravistoService.getInstance().selectionSyncObject) {
			// cmm.ensureActiveSession(e);
			GravistoService.getInstance().pluginSetMoveAllowed(false);
			super.mousePressed(e);
		}
	}
	
	JComponent mouseWheelComponent = null;
	
	@Override
	public void activate() {
		if (session == null || session.getActiveView() == null || session.getActiveView().getViewComponent() == null
							|| (!(session.getActiveView() instanceof GraffitiView))) {
			return;
		}
		super.activate();
		// gif.addMouseWheelListener(this);
		if (MainFrame.getInstance().getActiveSession() != null &&
							(MainFrame.getInstance().getActiveSession().getActiveView() instanceof GraffitiView)) {
			GraffitiView gv = (GraffitiView) MainFrame.getInstance().getActiveSession().getActiveView();
			if (gv != null) {
				mouseWheelComponent = gv.getViewComponent();
				mouseWheelComponent.addMouseWheelListener(this);
				for (GraffitiFrame gf : gv.getDetachedFrames()) {
					// jf.addMouseWheelListener(this);
					gf.getComponent(0).addMouseWheelListener(this);
				}
			}
		}
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		if (mouseWheelComponent != null)
			mouseWheelComponent.removeMouseWheelListener(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		synchronized (GravistoService.getInstance().selectionSyncObject) {
			GravistoService.getInstance().pluginSetMoveAllowed(true);
			super.mouseReleased(e);
		}
	}
	
	public long lastMove = Integer.MIN_VALUE;
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		if (e.getWhen() <= lastMove)
			return;
		
		lastMove = e.getWhen();
		if (!MegaTools.MouseWheelZoomEnabled) {
			processMouseWheelScrolling(e);
		} else {
			try {
				if (e.getModifiersEx() != 64 && e.getModifiersEx() != 128) {
					if (e.getWheelRotation() < 0){
//						ZoomFitChangeComponent.zoomIn();
						ZoomFitChangeComponent.zoomToPoint(MainFrame.getInstance().getActiveEditorSession().getActiveView(), e.getPoint(), 0.1);
					} else {
//						ZoomFitChangeComponent.zoomOut();
						ZoomFitChangeComponent.zoomToPoint(MainFrame.getInstance().getActiveEditorSession().getActiveView(), e.getPoint(), -0.1);
					}
					e.consume();
					return;
				}
				e.consume();
			} catch (NullPointerException npe) {
				// ignore
			}
		}
	}
	
	public static void processMouseWheelScrolling(MouseWheelEvent e) {
		Object o = e.getSource();
		if (o != null && o instanceof JComponent) {
			JComponent jc = (JComponent) o;
			JScrollPane jsp = (JScrollPane) ErrorMsg.findParentComponent(jc, JScrollPane.class);
			JScrollBar jsb = null;
			if ((e.getModifiers() & Event.SHIFT_MASK) == 1)
				jsb = jsp.getHorizontalScrollBar();
			if (e.getModifiers() == 0)
				jsb = jsp.getVerticalScrollBar();
			if (jsb != null) {
				int v = jsb.getValue();
				v += e.getUnitsToScroll() * 15;
				if (v < jsb.getMinimum())
					v = jsb.getMinimum();
				if (v > jsb.getMaximum())
					v = jsb.getMaximum();
				jsb.setValue(v);
			}
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		Component src = findComponentAt(e, e.getX(), e.getY());
		if (src != null) {
			if (src instanceof IPKnodeComponent) {
				IPKnodeComponent nci = (IPKnodeComponent) src;
				int mx = e.getX();
				int my = e.getY();
				nodeCursor = myMoveCursor;
				if (resizeHit_TL(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_TL_Cursor;
				if (resizeHit_TR(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_TR_Cursor;
				if (resizeHit_BR(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_BR_Cursor;
				if (resizeHit_BL(mx, my, nci.getX(), nci.getY(), nci.getWidth(), nci.getHeight()))
					nodeCursor = myResize_BL_Cursor;
			}
		}
		super.mouseMoved(e);
	}
	
	@Override
	public String getToolName() {
		return "IPK_MegaMoveTool";
	}
	// /* (non-Javadoc)
	// * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	// */
	// public void mouseEntered(MouseEvent arg0) {
	// // cmm.ensureActiveSession(arg0);
	// super.mouseEntered(arg0);
	// }
}
