/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ---------------------
 * AbstractTabbedUI.java
 * ---------------------
 * (C)opyright 2004, by Thomas Morgner and Contributors.
 * Original Author: Thomas Morgner;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: AbstractTabbedUI.java,v 1.1 2011-01-31 09:02:57 klukas Exp $
 * Changes
 * -------------------------
 * 16-Feb-2004 : Initial version
 * 07-Jun-2004 : Added standard header (DG);
 */

package org.jfree.ui.tabbedui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.util.Log;

/**
 * A tabbed GUI.
 */
public abstract class AbstractTabbedUI extends JComponent {

	/** The menu bar property key. */
	public static final String JMENUBAR_PROPERTY = "jMenuBar";

	/**
	 * An exit action.
	 */
	protected class ExitAction extends AbstractAction {

		/**
		 * Defines an <code>Action</code> object with a default
		 * description string and default icon.
		 */
		public ExitAction() {
			putValue(NAME, "Exit");
		}

		/**
		 * Invoked when an action occurs.
		 * 
		 * @param e
		 *           the event.
		 */
		public void actionPerformed(final ActionEvent e) {
			attempExit();
		}

	}

	/**
	 * A tab change handler.
	 */
	private class TabChangeHandler implements ChangeListener {

		private final JTabbedPane pane;

		/**
		 * Creates a new handler.
		 * 
		 * @param pane
		 *           the pane.
		 */
		public TabChangeHandler(final JTabbedPane pane) {
			this.pane = pane;
		}

		/**
		 * Invoked when the target of the listener has changed its state.
		 * 
		 * @param e
		 *           a ChangeEvent object
		 */
		public void stateChanged(final ChangeEvent e) {
			setSelectedEditor(this.pane.getSelectedIndex());
		}
	}

	/**
	 * A tab enable change listener.
	 */
	private class TabEnableChangeListener implements PropertyChangeListener {

		/**
		 * Default constructor.
		 */
		public TabEnableChangeListener() {
		}

		/**
		 * This method gets called when a bound property is changed.
		 * 
		 * @param evt
		 *           A PropertyChangeEvent object describing the event source
		 *           and the property that has changed.
		 */
		public void propertyChange(final PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("enabled") == false) {
				Log.debug("PropertyName");
				return;
			}
			if (evt.getSource() instanceof RootEditor == false) {
				Log.debug("Source");
				return;
			}
			final RootEditor editor = (RootEditor) evt.getSource();
			updateRootEditorEnabled(editor);
		}
	}

	private ArrayList rootEditors;
	private JTabbedPane tabbedPane;
	private int selectedRootEditor;
	private JComponent currentToolbar;
	private JPanel toolbarContainer;
	private Action closeAction;
	private JMenuBar jMenuBar;

	/**
	 * Default constructor.
	 */
	public AbstractTabbedUI() {
		this.selectedRootEditor = -1;

		this.toolbarContainer = new JPanel();
		this.toolbarContainer.setLayout(new BorderLayout());

		this.tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		this.tabbedPane.addChangeListener(new TabChangeHandler(this.tabbedPane));

		this.rootEditors = new ArrayList();

		setLayout(new BorderLayout());
		add(this.toolbarContainer, BorderLayout.NORTH);
		add(this.tabbedPane, BorderLayout.CENTER);

		this.closeAction = createCloseAction();
	}

	protected JTabbedPane getTabbedPane() {
		return this.tabbedPane;
	}

	/**
	 * Returns the menu bar.
	 * 
	 * @return The menu bar.
	 */
	public JMenuBar getJMenuBar() {
		return this.jMenuBar;
	}

	protected void setJMenuBar(final JMenuBar menuBar) {
		final JMenuBar oldMenuBar = this.jMenuBar;
		this.jMenuBar = menuBar;
		firePropertyChange(JMENUBAR_PROPERTY, oldMenuBar, menuBar);
	}

	/**
	 * Creates a close action.
	 * 
	 * @return A close action.
	 */
	protected Action createCloseAction() {
		return new ExitAction();
	}

	/**
	 * Returns the close action.
	 * 
	 * @return The close action.
	 */
	public Action getCloseAction() {
		return this.closeAction;
	}

	/**
	 * Returns the prefix menus.
	 * 
	 * @return The prefix menus.
	 */
	protected abstract JMenu[] getPrefixMenus();

	/**
	 * The postfix menus.
	 * 
	 * @return The postfix menus.
	 */
	protected abstract JMenu[] getPostfixMenus();

	/**
	 * Adds menus.
	 * 
	 * @param menuBar
	 * @param customMenus
	 */
	private void addMenus(final JMenuBar menuBar, final JMenu[] customMenus) {
		for (int i = 0; i < customMenus.length; i++) {
			menuBar.add(customMenus[i]);
		}
	}

	/**
	 * Creates a menu bar.
	 * 
	 * @param root
	 * @return A menu bar.
	 */
	private JMenuBar createEditorMenubar(final RootEditor root) {

		JMenuBar menuBar = getJMenuBar();
		if (menuBar == null) {
			menuBar = new JMenuBar();
		} else {
			menuBar.removeAll();
		}
		addMenus(menuBar, getPrefixMenus());
		addMenus(menuBar, root.getMenus());
		addMenus(menuBar, getPostfixMenus());
		return menuBar;
	}

	/**
	 * Adds a root editor.
	 * 
	 * @param rootPanel
	 *           the root panel.
	 */
	public void addRootEditor(final RootEditor rootPanel) {
		this.rootEditors.add(rootPanel);
		this.tabbedPane.add(rootPanel.getEditorName(), rootPanel.getMainPanel());
		rootPanel.addPropertyChangeListener("enabled", new TabEnableChangeListener());
		updateRootEditorEnabled(rootPanel);
		if (this.rootEditors.size() == 1) {
			setSelectedEditor(0);
		}
	}

	/**
	 * Returns the specified editor.
	 * 
	 * @param pos
	 *           the position index.
	 * @return The editor at the given position.
	 */
	public RootEditor getRootEditor(final int pos) {
		return (RootEditor) this.rootEditors.get(pos);
	}

	/**
	 * Returns the selected editor.
	 * 
	 * @return The selected editor.
	 */
	public int getSelectedEditor() {
		return this.selectedRootEditor;
	}

	/**
	 * Sets the selected editor.
	 * 
	 * @param selectedEditor
	 *           the selected editor.
	 */
	public void setSelectedEditor(final int selectedEditor) {
		final int oldEditor = this.selectedRootEditor;
		if (oldEditor == selectedEditor) {
			// no change - so nothing to do!
			return;
		}
		this.selectedRootEditor = selectedEditor;
		// make sure that only the selected editor is active.
		// all other editors will be disabled, if needed and
		// not touched if they are already in the correct state

		for (int i = 0; i < this.rootEditors.size(); i++) {
			final boolean shouldBeActive = (i == selectedEditor);
			final RootEditor container =
								(RootEditor) this.rootEditors.get(i);
			if (container.isActive() &&
								(shouldBeActive == false)) {
				container.setActive(false);
			}
		}

		if (this.currentToolbar != null) {
			closeToolbar();
			this.toolbarContainer.removeAll();
			this.currentToolbar = null;
		}

		for (int i = 0; i < this.rootEditors.size(); i++) {
			final boolean shouldBeActive = (i == selectedEditor);
			final RootEditor container =
								(RootEditor) this.rootEditors.get(i);
			if ((container.isActive() == false) &&
								(shouldBeActive == true)) {
				container.setActive(true);
				setJMenuBar(createEditorMenubar(container));
				this.currentToolbar = container.getToolbar();
				if (this.currentToolbar != null) {
					this.toolbarContainer.add
										(this.currentToolbar, BorderLayout.CENTER);
					this.toolbarContainer.setVisible(true);
					this.currentToolbar.setVisible(true);
				} else {
					this.toolbarContainer.setVisible(false);
				}
			}
		}
	}

	/**
	 * Closes the toolbar.
	 */
	private void closeToolbar() {
		if (this.currentToolbar != null) {
			if (this.currentToolbar.getParent() != this.toolbarContainer) {
				// ha!, the toolbar is floating ...
				// Log.debug (currentToolbar.getParent());
				final Window w = SwingUtilities.windowForComponent(this.currentToolbar);
				if (w != null) {
					w.setVisible(false);
					w.dispose();
				}
			}
			this.currentToolbar.setVisible(false);
		}
	}

	/**
	 * Attempts to exit.
	 */
	protected abstract void attempExit();

	protected void updateRootEditorEnabled(final RootEditor editor) {

		final boolean enabled = editor.isEnabled();
		for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
			final Component tab = this.tabbedPane.getComponentAt(i);
			if (tab == editor.getMainPanel()) {
				this.tabbedPane.setEnabledAt(i, enabled);
				return;
			}
		}
	}
}
