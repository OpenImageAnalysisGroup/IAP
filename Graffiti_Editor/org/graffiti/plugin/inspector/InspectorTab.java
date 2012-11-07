// ==============================================================================
//
// InspectorTab.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: InspectorTab.java,v 1.2 2012-11-07 14:42:07 klukas Exp $

package org.graffiti.plugin.inspector;

import java.awt.Color;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import org.ErrorMsg;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.SelectionListener;

/**
 * An <code>InspectorTab</code> is a generic component for an <code>InspectorPlugin</code>.
 * 
 * @see JComponent
 * @see InspectorPlugin
 */
public abstract class InspectorTab
		extends JComponent {
	// ~ Instance fields ========================================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The panel that holds the table of the attributes and the buttons for
	 * adding and removing attributes as well as the "apply" button.
	 */
	public EditPanel editPanel;
	
	/**
	 * The title of the <code>InspectorTab</code> which will appear as the
	 * title of the tab.
	 */
	protected String title;
	
	private ImageIcon icon;
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the EditPanel of this tab.
	 * 
	 * @return DOCUMENT ME!
	 */
	public EditPanel getEditPanel() {
		return this.editPanel;
	}
	
	/**
	 * Returns the title of the current <code>InspectorTab</code>.
	 * 
	 * @return the title of the current <code>InspectorTab</code>.
	 */
	public String getTitle() {
		return this.title;
	}
	
	@Override
	public String getName() {
		return getTitle();
	}
	
	public abstract boolean visibleForView(View v);
	
	private boolean currentlyHighlight = false;
	
	public void focusAndHighlight(final InspectorTab whenFinishedHighlight, final boolean highlight, final boolean cycleChildren) {
		final int time = 800;
		if (currentlyHighlight)
			return;
		currentlyHighlight = true;
		JTabbedPane tp = (JTabbedPane) getParent();
		if (tp != null) {
			tp.setSelectedComponent(this);
			final Border oldB = getBorder();
			final InspectorTab fit = this;
			if (whenFinishedHighlight != null)
				whenFinishedHighlight.focusAndHighlight(null, false, cycleChildren);
			if (highlight)
				setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), getTitle()));
			repaint();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						try {
							Thread.sleep(time);
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
						if (highlight)
							fit.setBorder(oldB);
						fit.repaint();
						if (whenFinishedHighlight != null) {
							whenFinishedHighlight.focusAndHighlight(null, highlight, cycleChildren);
							if (whenFinishedHighlight instanceof ContainsTabbedPane) {
								ContainsTabbedPane sh = (ContainsTabbedPane) whenFinishedHighlight;
								if (cycleChildren) {
									cycleHighlight(whenFinishedHighlight,
											highlight, oldB, sh);
								}
							}
						} else {
							if (cycleChildren && fit instanceof SubtabHostTab) {
								SubtabHostTab sh = (SubtabHostTab) fit;
								cycleHighlight(sh,
										highlight, oldB, sh);
							}
						}
					} finally {
						currentlyHighlight = false;
					}
				}
				
				private void cycleHighlight(
						final InspectorTab tab,
						final boolean highlight, final Border oldB,
						ContainsTabbedPane sh) {
					if (highlight)
						tab.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), tab.getTitle()));
					tab.repaint();
					JTabbedPane jtp = sh.getTabbedPane();
					for (int i = 0; i < jtp.getTabCount(); i++) {
						jtp.setSelectedIndex(i);
						try {
							Thread.sleep(time / jtp.getTabCount());
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					jtp.setSelectedIndex(0);
					if (highlight)
						tab.setBorder(oldB);
					tab.repaint();
				}
			};
			Thread t = new Thread(r);
			t.setName(getName());
			t.start();
			return;
		}
	}
	
	public static void focusAndHighlightComponent(final JComponent thisss, final String title, final InspectorTab whenFinishedHighlight,
			final boolean highlight, final boolean cycleChildren) {
		final int time = 800;
		JTabbedPane tp = (JTabbedPane) thisss.getParent();
		if (tp != null) {
			tp.setSelectedComponent(thisss);
			final Border oldB = thisss.getBorder();
			
			if (whenFinishedHighlight != null)
				whenFinishedHighlight.focusAndHighlight(null, false, cycleChildren);
			if (highlight)
				thisss.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), title));
			thisss.repaint();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(time);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
					if (highlight)
						thisss.setBorder(oldB);
					thisss.repaint();
					if (whenFinishedHighlight != null) {
						whenFinishedHighlight.focusAndHighlight(null, highlight, cycleChildren);
						if (whenFinishedHighlight instanceof ContainsTabbedPane) {
							ContainsTabbedPane sh = (ContainsTabbedPane) whenFinishedHighlight;
							if (cycleChildren) {
								cycleHighlight(whenFinishedHighlight,
										highlight, oldB, sh);
							}
						}
					} else {
						if (cycleChildren && thisss instanceof SubtabHostTab) {
							SubtabHostTab sh = (SubtabHostTab) thisss;
							cycleHighlight(sh,
									highlight, oldB, sh);
						}
					}
				}
				
				private void cycleHighlight(
						final InspectorTab tab,
						final boolean highlight, final Border oldB,
						ContainsTabbedPane sh) {
					if (highlight)
						tab.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 4), tab.getTitle()));
					tab.repaint();
					JTabbedPane jtp = sh.getTabbedPane();
					for (int i = 0; i < jtp.getTabCount(); i++) {
						jtp.setSelectedIndex(i);
						try {
							Thread.sleep(time / jtp.getTabCount());
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					jtp.setSelectedIndex(0);
					if (highlight)
						tab.setBorder(oldB);
					tab.repaint();
				}
			};
			Thread t = new Thread(r);
			t.setName(title);
			t.start();
			return;
		}
	}
	
	public void setEditPanelInformation(
			Map<?, ?> valueEditComponents,
			Map<GraphElement, GraphElement> map) {
		if (getEditPanel() != null) {
			getEditPanel().setEditComponentMap(valueEditComponents);
			getEditPanel().setGraphElementMap(map);
		}
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}
	
	public ImageIcon getIcon() {
		return icon;
	}
	
	public boolean isSelectionListener() {
		return (this instanceof SelectionListener);
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
