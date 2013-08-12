/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.prefs.Preferences;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.AttributeHelper;
import org.graffiti.core.ImageBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.gui.ModeToolbar;
import org.graffiti.plugin.gui.ToolButton;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.plugins.modes.defaultEditMode.DefaultEditMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SelectNodesComponent;

/**
 * This plugin contains the modified standard editing tools. Because the
 * StandardTools use private (and not protected) variables for the Tools, I
 * decided to copy the source and modify the 3 calls for the Tool-Creation.
 * As no advanced functionality is provided by the plugin code itself, this
 * should be ok for now.
 * 
 * @author Christian Klukas
 * @version $Revision: 1.2 $
 */
public class IPK_StandardTools
					extends IPK_EditorPluginAdapter
					implements ViewListener, SessionListener {
	
	/**
	 * The <code>ImageBundle</code> of the main frame.
	 */
	private final ImageBundle iBundle = ImageBundle.getInstance();
	
	/**
	 * DOCUMENT ME!
	 */
	// private Tool label;
	
	/**
	 * The tools this plugin provides.
	 */
	private final Tool megaCreate;
	
	/**
	 * DOCUMENT ME!
	 */
	private final Tool megaMove;
	
	/**
	 * DOCUMENT ME!
	 */
	// private Tool megaIPKsubstrat;
	
	/**
	 * DOCUMENT ME!
	 */
	// private ToolButton labelButton;
	
	/**
	 * The buttons for the tools this plugin provides.
	 */
	ToolButton megaCreateButton;
	
	/**
	 * DOCUMENT ME!
	 */
	private final ToolButton megaMoveButton;
	
	/**
	 * Creates a new StandardTools object.
	 */
	public IPK_StandardTools() {
		
		this.algorithms = new Algorithm[] {
							new RecreateView(),
							new CloseAllWindows(),
							new EvaluateKGML()
		};
		
		// label = new IPK_AdvancedLabelTool();
		megaCreate = new IPK_MegaCreateTool();
		megaMove = new IPK_MegaMoveTool();
		// megaIPKsubstrat = new IPK_SubstrateCreateTool();
		tools = new Tool[] {
							megaCreate,
							megaMove // ,
		// label,
		// megaIPKsubstrat
		};
		
		megaCreateButton =
							new ToolButton(megaCreate,
												DefaultEditMode.sid,
												iBundle.getImageIcon("tool.megaCreate"));
		megaCreateButton.addAncestorListener(new AncestorListener() {
			
			public void ancestorAdded(AncestorEvent event) {
				// look for other tools in the enclosing container
				// AND REMOVE THEM
				Component[] l = megaCreateButton.getParent().getComponents();
				if (l != null && l.length > 0)
					for (int i = 0; i < l.length; i++) {
						if (!(l[i] instanceof ToolButton)) {
							megaCreateButton.getParent().remove(l[i]);
						} else {
							ToolButton t = (ToolButton) l[i];
							boolean found = false;
							for (int i2 = 0; i2 < tools.length; i2++) {
								if (t.getTool() == tools[i2])
									found = true;
							}
							if (!found)
								megaCreateButton.getParent().remove(t);
						}
						((ModeToolbar) megaCreateButton.getParent()).setName(""); // Edit-Tools");
					}
				megaCreateButton.getParent().validate();
				megaCreateButton.getParent().repaint();
			}
			
			public void ancestorMoved(AncestorEvent event) {
				
			}
			
			public void ancestorRemoved(AncestorEvent event) {
				
			}
		});
		
		megaCreateButton.setToolTipText("Create Nodes and Edges");
		
		megaMoveButton =
							new ToolButton(megaMove,
												DefaultEditMode.sid,
												iBundle.getImageIcon("tool.megaMove"));
		
		megaMoveButton.setToolTipText("<html>Select Nodes and Edges (press shift to change selection behaviour)<br>" +
														"and move selected Nodes and Edge-Bends and edit Labels");
		
		megaCreateButton.addKeyListener(getLabelKeylistener());
		megaMoveButton.addKeyListener(getLabelKeylistener());
		/*
		 * labelButton =
		 * new ToolButton(label,
		 * DefaultEditMode.sid,
		 * iBundle.getImageIcon("tool.label"));
		 * labelButton.setToolTipText("Add/Modify Node- and Edge-Labels");
		 * /*
		 * megaIPKsubstratButton =
		 * new ToolButton(megaIPKsubstrat,
		 * DefaultEditMode.sid,
		 * iBundle.getImageIcon("tool.megaCreate"));
		 */
		guiComponents = new GraffitiComponent[] {
							megaCreateButton,
							megaMoveButton
		};
		// guiComponents[2] = labelButton;
		// guiComponents[3] = megaIPKsubstratButton;
	}
	
	private KeyListener getLabelKeylistener() {
		KeyListener result = new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (!((e.getModifiers() == 1) ||
							(e.getModifiers() == 0))
							) {
								// System.out.println("Ignore: "+e.getModifiers()+" / "+e.getKeyChar());
								return;
							}
							try {
								Selection s = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
								if (s.getElements().size() <= 0) {
									if (!(Character.isLetterOrDigit(e.getKeyChar())))
										return;
									// activate search field
									SelectNodesComponent.focus(e);
								}
							} catch (Exception err) {
								// empty
							}
							char c = e.getKeyChar();
							if (processLabelEdit(c))
								e.consume();
						}
			
			private boolean processLabelEdit(char c) {
				// if (!(Character.isLetterOrDigit(c) || (c==8) || (c=='\n')))
				// return false;
				if ((c == 127) || (c < 32 /* space */&& !((c == '\n') || (c == 8))))
					return false;
				try {
					Selection s = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
					for (GraphElement ge : s.getElements()) {
						if (c == 8) {
							String currentLabel = AttributeHelper.getLabel(ge, null);
							if (currentLabel != null) {
								if (currentLabel.startsWith("<html>") && currentLabel.endsWith("<br>")) {
									currentLabel = currentLabel.substring(0, currentLabel.length() - "<br>".length());
									AttributeHelper.setLabel(ge, currentLabel);
								} else
									if (currentLabel != null && currentLabel.length() > 0) {
										AttributeHelper.setLabel(ge, currentLabel.substring(0, currentLabel.length() - 1));
									}
							}
						} else
							if (c == '\n') {
								String currentLabel = AttributeHelper.getLabel(ge, null);
								if (currentLabel != null) {
									if (!currentLabel.startsWith("<html>"))
										currentLabel = "<html>" + currentLabel;
									currentLabel = currentLabel + "<br>";
									AttributeHelper.setLabel(ge, currentLabel);
								}
							} else {
								String currentLabel = AttributeHelper.getLabel(ge, null);
								if (currentLabel == null)
									currentLabel = "";
								currentLabel = currentLabel + c;
								AttributeHelper.setLabel(ge, currentLabel);
							}
					}
					return true;
				} catch (Exception e) {
					// empty
					return false;
				}
			}
			
			public void keyPressed(KeyEvent e) {
			}
			
			public void keyReleased(KeyEvent e) {
			}
		};
		return result;
	}
	
	/**
	 * Sets the preferences in all tools this plugin provides.
	 * 
	 * @param preferences
	 *           the preferences node for this plugin.
	 * @see org.graffiti.plugin.GenericPlugin#configure(Preferences)
	 */
	@Override
	public void configure(GravistoPreferences preferences) {
		super.configure(preferences);
		megaCreate.setPrefs(this.prefs.node("megaCreateTool"));
		megaMove.setPrefs(this.prefs.node("megaMoveTool"));
		// label.setPrefs(this.prefs.node("labelTool"));
		// megaIPKsubstrat.setPrefs(this.prefs.node("IPKsubstrat"));
	}
	
	// JComponent lastComp = null;
	// KeyListener lastListener = null;
	
	public void viewChanged(View newView) {
		
		if (newView == null || !(newView instanceof GraffitiView)) {
			megaCreateButton.setEnabled(false);
			megaMoveButton.setEnabled(false);
		} else {
			megaCreateButton.setEnabled(true);
			megaMoveButton.setEnabled(true);
		}
		
		// if (lastComp!=null && lastListener!=null) {
		// lastComp.removeKeyListener(lastListener);
		// lastComp = null;
		// lastListener = null;
		// }
		// if (newView!=null && newView instanceof GraffitiView) {
		// lastListener = getLabelKeylistener();
		// lastComp = MainFrame.getInstance().getJDesktopPane();
		// lastComp.addKeyListener(lastListener);
		// lastComp.addFocusListener(new FocusListener() {
		// @Override
		// public void focusGained(FocusEvent e) {
		// System.out.println("G");
		// }
		//
		// @Override
		// public void focusLost(FocusEvent e) {
		// System.out.println("L");
		// }});
		// }
	}
	
	@Override
	public boolean isViewListener() {
		return true;
	}
	
	@Override
	public boolean isSessionListener() {
		return true;
	}
	
	public void sessionChanged(Session s) {
		if (s == null) {
			megaCreateButton.setEnabled(false);
			megaMoveButton.setEnabled(false);
		} else {
			megaCreateButton.setEnabled(true);
			megaMoveButton.setEnabled(true);
			viewChanged(s.getActiveView());
		}
	}
	
	public void sessionDataChanged(Session s) {
		// empty
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
