/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import info.clearthought.layout.SingleFiledLayout;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;
import org.graffiti.undo.Undoable;

/**
 * DOCUMENT ME!
 */
public class ClusterHelperToolbarComponent extends JToolBar implements GraffitiComponent,
					ActionListener, Undoable, SessionListener {
	
	// ~ Instance fields
	// ========================================================
	
	private static final long serialVersionUID = 1L;
	
	// JToolBar myContent;
	
	/** The alignment buttons */
	private JButton jbUpdateClusterNodes, jbCondense, jbHideClusterNodes;
	
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
	public ClusterHelperToolbarComponent(String prefComp) {
		super("Cluster/Nodes");
		this.prefComp = prefComp;
		
		// myContent = new JToolBar();
		// // myContent.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
		//
		// myContent.setFloatable(false);
		setLayout(new SingleFiledLayout(SingleFiledLayout.ROW,
							SingleFiledLayout.FULL, 0));
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		
		ImageIcon iconShowCluster = new ImageIcon(cl.getResource(path
							+ "/images/show_cluster_nodes.gif"));
		ImageIcon iconHideCluster = new ImageIcon(cl.getResource(path
							+ "/images/hide_cluster_nodes.gif"));
		ImageIcon iconCondense = new ImageIcon(cl.getResource(path
							+ "/images/condense_nodes.gif"));
		
		jbCondense = addButton(this, iconCondense);
		jbCondense.putClientProperty("cmd", KeggNavigationToolbarCommand.Command.CONDENSE_ENTITIES);
		jbCondense.setToolTipText("Condense multiple entities");
		
		jbUpdateClusterNodes = addButton(this, iconShowCluster);
		jbUpdateClusterNodes.putClientProperty("cmd", KeggNavigationToolbarCommand.Command.UPDATE_CLUSTER_NODES);
		jbUpdateClusterNodes.setToolTipText("Create or update cluster background-nodes");
		
		jbHideClusterNodes = addButton(this, iconHideCluster);
		jbHideClusterNodes.putClientProperty("cmd", KeggNavigationToolbarCommand.Command.HIDE_CLUSTER_NODES);
		jbHideClusterNodes.setToolTipText("Hide cluster background-nodes");
		
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		validate();
	}
	
	// ~ Methods
	// ================================================================
	
	private JButton addButton(JComponent myContent, ImageIcon icon) {
		JButton newButton = new JButton(icon);
		int s = 4;
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
			UndoableEdit cmd = new KeggNavigationToolbarCommand(
								(KeggNavigationToolbarCommand.Command) ((JButton) e.getSource())
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
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return prefComp;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
