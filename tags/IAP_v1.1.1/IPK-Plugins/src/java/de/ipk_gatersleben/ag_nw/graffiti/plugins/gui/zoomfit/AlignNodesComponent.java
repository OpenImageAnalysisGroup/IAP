/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.graffiti.undo.Undoable;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

/**
 * DOCUMENT ME!
 */
public class AlignNodesComponent extends JToolBar implements GraffitiComponent,
					ActionListener, Undoable, SessionListener, ViewListener {
	
	// ~ Instance fields
	// ========================================================
	
	private static final long serialVersionUID = 1L;
	
	// JToolBar myContent;
	
	/** The alignment buttons */
	private JButton jbHorB, jbHorC, jbHorT, jbVertC, jbVertL, jbVertR;
	
	/** active session */
	private static Session activeSession;
	
	private UndoableEditSupport undoSupport;
	
	private String prefComp;
	
	// ~ Constructors
	// ===========================================================
	
	/**
	 * Constructor for ZoomChangeComponent.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	public AlignNodesComponent(String prefComp) {
		super("Node-Alignment");
		
		this.prefComp = prefComp;
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		
		ImageIcon iconVL = new ImageIcon(cl.getResource(path
							+ "/images/vert_l.gif"));
		ImageIcon iconVR = new ImageIcon(cl.getResource(path
							+ "/images/vert_r.gif"));
		ImageIcon iconVC = new ImageIcon(cl.getResource(path
							+ "/images/vert_c.gif"));
		ImageIcon iconHT = new ImageIcon(cl.getResource(path
							+ "/images/hor_t.gif"));
		ImageIcon iconHB = new ImageIcon(cl.getResource(path
							+ "/images/hor_b.gif"));
		ImageIcon iconHC = new ImageIcon(cl.getResource(path
							+ "/images/hor_c.gif"));
		
		// jbHorR, jbHorC, jbHorL, jbVertT, jbVertC, jbVertB
		jbHorB = addButton(this, iconHB);
		jbHorB.putClientProperty("cmd", AlignNodesCommand.Command.jbHorB);
		jbHorB.setToolTipText("Align nodes at bottom");
		
		jbHorC = addButton(this, iconHC);
		jbHorC.putClientProperty("cmd", AlignNodesCommand.Command.jbHorC);
		jbHorC.setToolTipText("Align nodes horizontally centered");
		
		jbHorT = addButton(this, iconHT);
		jbHorT.putClientProperty("cmd", AlignNodesCommand.Command.jbHorT);
		jbHorT.setToolTipText("Align nodes at top");
		
		jbVertL = addButton(this, iconVL);
		jbVertL.putClientProperty("cmd", AlignNodesCommand.Command.jbVertL);
		jbVertL.setToolTipText("Align nodes at left");
		
		jbVertC = addButton(this, iconVC);
		jbVertC.putClientProperty("cmd", AlignNodesCommand.Command.jbVertC);
		jbVertC.setToolTipText("Align nodes vertically centered");
		
		jbVertR = addButton(this, iconVR);
		jbVertR.putClientProperty("cmd", AlignNodesCommand.Command.jbVertR);
		jbVertR.setToolTipText("Align nodes at right");
		
		// setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		validate();
	}
	
	// ~ Methods
	// ================================================================
	
	private JButton addButton(JComponent myContent, ImageIcon icon) {
		JButton newButton = new JButton(icon);
		int s = 6;
		newButton.setMargin(new Insets(s, s, s, s));
		newButton.setRolloverEnabled(true);
		newButton.addActionListener(this);
		myContent.add(newButton);
		return newButton;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (activeSession != null) {
			UndoableEdit cmd = new AlignNodesCommand(
								(AlignNodesCommand.Command) ((JButton) e.getSource())
													.getClientProperty("cmd"), (EditorSession) activeSession);
			cmd.redo();
			undoSupport.beginUpdate();
			undoSupport.postEdit(cmd);
			undoSupport.endUpdate();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.undo.Undoable#setUndoSupport(javax.swing.undo.UndoableEditSupport)
	 */
	public void setUndoSupport(UndoableEditSupport us) {
		undoSupport = us;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		activeSession = s;
		jbHorB.setVisible(s != null);
		jbHorC.setVisible(s != null);
		jbHorT.setVisible(s != null);
		jbVertC.setVisible(s != null);
		jbVertL.setVisible(s != null);
		jbVertR.setVisible(s != null);
		
		if (s != null) {
			viewChanged(s.getActiveView());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		activeSession = s;
		viewChanged(s.getActiveView());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return prefComp;
	}
	
	public void viewChanged(View newView) {
		View view = newView;
		if (view == null || !(view.getClass() == IPKGraffitiView.class)) {
			jbHorB.setVisible(false);
			jbHorC.setVisible(false);
			jbHorT.setVisible(false);
			jbVertC.setVisible(false);
			jbVertL.setVisible(false);
			jbVertR.setVisible(false);
		} else {
			jbHorB.setVisible(true);
			jbHorC.setVisible(true);
			jbHorT.setVisible(true);
			jbVertC.setVisible(true);
			jbVertL.setVisible(true);
			jbVertR.setVisible(true);
		}
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
