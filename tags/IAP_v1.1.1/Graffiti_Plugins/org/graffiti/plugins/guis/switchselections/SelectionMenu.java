// ==============================================================================
//
// SelectionMenu.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SelectionMenu.java,v 1.1 2011-01-31 09:03:38 klukas Exp $

package org.graffiti.plugins.guis.switchselections;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.graffiti.core.StringBundle;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.gui.GraffitiMenu;
import org.graffiti.plugin.gui.GraffitiMenuItem;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

/**
 * A menu providing entries to manage selections.
 * 
 * @author $Author: klukas $
 */
public class SelectionMenu
					extends GraffitiMenu
					implements GraffitiComponent, SelectionListener, SessionListener {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The action for the "save as ..." menu item. */
	private final Action saveAction;
	
	/** The menu item for the saveAction. */
	private final GraffitiMenuItem saveItem;
	
	/** Save last active session */
	private EditorSession activeSession;
	
	/** DOCUMENT ME! */
	private Map<EditorSession, Selection> lastSelMap = new HashMap<EditorSession, Selection>();
	
	/** Saves entries for each session. */
	@SuppressWarnings("unchecked")
	private Map<EditorSession, Map> sessionItemsMap = new HashMap<EditorSession, Map>();
	
	/** The <code>StringBundle</code> for the string constants. */
	private StringBundle sBundle = StringBundle.getInstance();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Creates a new SelectionMenu object.
	 */
	public SelectionMenu() {
		super();
		setName(sBundle.getString("menu.selections"));
		setText(sBundle.getString("menu.selections"));
		setEnabled(false);
		
		saveAction = new SelectionSaveAction(sBundle.getString(
							"menu.saveselection"));
		saveAction.setEnabled(true);
		saveAction.putValue(Action.NAME, sBundle.getString("menu.saveselection"));
		saveItem = new GraffitiMenuItem(sBundle.getString("menu.selections"),
							saveAction);
		saveItem.setToolTipText("Adds the current selection to the list " +
							"of loadable selections in this menu.\nOnly available till " +
							"the session is closed.\nDoes not survive undo / redo " +
							"operations on graph elements inside the selection.");
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionChanged(org.graffiti.selection.SelectionEvent)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(SelectionEvent e) {
		activeSession = GravistoService.getInstance().getMainFrame()
							.getActiveEditorSession();
		
		Map<String, ItemPositionPair> nameItemMap = (Map<String, ItemPositionPair>) sessionItemsMap.get(activeSession);
		
		Selection lastSel = (Selection) lastSelMap.get(activeSession);
		assert sBundle.getString("activeSelection").equals(lastSel.getName());
		
		JMenuItem comp = ((ItemPositionPair) nameItemMap.get(lastSel.getName())).getMenuItem();
		
		Selection clonedLastSel = null;
		
		clonedLastSel = (Selection) lastSel.clone();
		clonedLastSel.setName(sBundle.getString("activeSelection"));
		
		// setEnabled(true);
		if (comp != null) {
			// already there => remove old, add new; maybe can replace differently
			remove(comp);
		}
		
		SelectionChangeAction selAction = new SelectionChangeAction(clonedLastSel,
							activeSession);
		selAction.setEnabled(true);
		
		// selAction.putValue(Action.NAME, lastSel.getName());
		selAction.putValue(Action.NAME, sBundle.getString("activeSelection"));
		
		GraffitiMenuItem item = new GraffitiMenuItem(sBundle.getString(
							"menu.selections"), selAction);
		insert(item, 1);
		
		nameItemMap.put(sBundle.getString("activeSelection"),
							new ItemPositionPair(item, 1));
		
		if (!e.getSelection().isEmpty()) {
			lastSel = (Selection) e.getSelection().clone();
			lastSel.setName(sBundle.getString("activeSelection"));
			lastSelMap.put(activeSession, lastSel);
		}
		
		validate();
	}
	
	/**
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	@SuppressWarnings("unchecked")
	public void selectionListChanged(SelectionEvent e) {
		Selection clonedSel = null;
		
		clonedSel = (Selection) e.getSelection().clone();
		clonedSel.setName(e.getSelection().getName());
		
		activeSession = GravistoService.getInstance().getMainFrame()
							.getActiveEditorSession();
		
		Map<String, ItemPositionPair> nameItemMap = (Map<String, ItemPositionPair>) sessionItemsMap.get(activeSession);
		
		if (nameItemMap == null) {
			nameItemMap = new HashMap<String, ItemPositionPair>();
			sessionItemsMap.put(activeSession, nameItemMap);
			
			add(saveItem);
			nameItemMap.put(saveItem.getActionCommand(),
								new ItemPositionPair(saveItem, this.getItemCount() - 1));
		}
		
		ItemPositionPair itp = (ItemPositionPair) nameItemMap.get(e.getSelection()
							.getName());
		Component comp = null;
		
		if (itp != null) {
			comp = itp.getMenuItem();
		}
		
		// setEnabled(true);
		if (e.toBeAdded()) {
			if (comp != null) {
				// already there => remove old, add new
				remove(comp);
			}
			
			Action selAction = new SelectionChangeAction(clonedSel,
								activeSession);
			selAction.setEnabled(true);
			selAction.putValue(Action.NAME, clonedSel.getName());
			
			GraffitiMenuItem item = new GraffitiMenuItem(sBundle.getString(
								"menu.selections"), selAction);
			this.add(item);
			nameItemMap.put(clonedSel.getName(),
								new ItemPositionPair(item, this.getItemCount() - 1));
			
			if (!isEnabled()) {
				setEnabled(true);
			}
		} else {
			remove(comp);
		}
		
		if (sBundle.getString("activeSelection").equals(clonedSel.getName())) {
			lastSelMap.put(activeSession, clonedSel);
		}
		
		validate();
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		if (activeSession != s) {
			// switch to the set of selections associated with the new session
			removeAll();
			
			Map<?, ?> nameItemMap = (Map<?, ?>) sessionItemsMap.get(s);
			
			if (nameItemMap != null) {
				ArrayList<JMenuItem> items = new ArrayList<JMenuItem>(nameItemMap.size());
				
				// ensure that size = nameItemMap.size(); strange isn't it?!
				for (int i = 0; i < nameItemMap.size(); i++) {
					items.add(null);
				}
				
				// add menu items to list in correct order
				for (Iterator<?> it = nameItemMap.values().iterator(); it.hasNext();) {
					ItemPositionPair ipp = (ItemPositionPair) it.next();
					items.set(ipp.getPosition(), ipp.getMenuItem());
				}
				
				// add menu items in correct order to menu
				for (Iterator<JMenuItem> iter = items.iterator(); iter.hasNext();) {
					JMenuItem item = (JMenuItem) iter.next();
					add(item);
				}
				
				if (!nameItemMap.isEmpty()) {
					setEnabled(true);
				}
			}
			
			if (s == null) {
				setEnabled(false);
			}
			
			activeSession = GravistoService.getInstance().getMainFrame()
								.getActiveEditorSession();
		}
		
		validate();
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
	}
	
	// ~ Inner Classes ==========================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @author $Author: klukas $
	 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:38 $
	 */
	class ItemPositionPair {
		/** DOCUMENT ME! */
		private JMenuItem mi;
		
		/** DOCUMENT ME! */
		private int pos;
		
		/**
		 * Creates a new ItemPositionPair object.
		 * 
		 * @param jmi
		 *           DOCUMENT ME!
		 * @param posi
		 *           DOCUMENT ME!
		 */
		public ItemPositionPair(JMenuItem jmi, int posi) {
			mi = jmi;
			pos = posi;
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @param mi
		 */
		public void setMenuItem(JMenuItem mi) {
			this.mi = mi;
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @return mi
		 */
		public JMenuItem getMenuItem() {
			return mi;
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @param pos
		 */
		public void setPosition(int pos) {
			this.pos = pos;
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @return pos
		 */
		public int getPosition() {
			return pos;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
