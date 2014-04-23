// ==============================================================================
//
// StringEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: StringEditComponent.java,v 1.3 2013-04-19 09:17:37 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugins.editcomponents.ComponentBorder;
import org.graffiti.plugins.editcomponents.ComponentBorder.Edge;

/**
 * <code>StringEditComponent</code> provides an edit component for editing
 * strings. The edit field has just one line.
 * 
 * @version $Revision: 1.3 $
 * @see org.graffiti.plugin.editcomponent.AbstractValueEditComponent
 * @see javax.swing.text.JTextComponent
 * @see TextAreaEditComponent
 */
public class StringEditComponent
		extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** The text field containing the value of the displayable. */
	protected JTextComponent textComp;
	
	protected JComponent searchComponent;
	
	private Boolean multiline;
	
	// ~ Constructors ===========================================================
	
	// /**
	// * The attribute displayed by this component;
	// */
	// private StringAttribute strAttr;
	
	/**
	 * Constructs a new <code>StringEditComponent</code>.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public StringEditComponent(Displayable disp) {
		super(disp);
		
		searchComponent = getSearchComponent();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>JComponent</code> of this edit component.
	 * 
	 * @return the <code>JComponent</code> of this edit component.
	 */
	@Override
	public JComponent getComponent() {
		
		if (textComp == null) {
			// System.out.println("creating StringAttribute");
			boolean multiRow = multiline != null && multiline.booleanValue();
			
			if (multiRow) {
				textComp = new JTextArea(getDisplayable().getValue().toString());
				// ((JTextArea)textComp).setWrapStyleWord(true);
				((JTextArea) textComp).setLineWrap(true);
				((JTextArea) textComp).setRows(5);
				textComp.setBorder(BorderFactory.createEtchedBorder());
				
			} else {
				textComp = new JTextField(getDisplayable().getValue().toString());
				try {
					if (getDisplayable().getName().toUpperCase().contains("PASSWORD")) {
						textComp = new JPasswordField(getDisplayable().getValue().toString());
					}
				} catch (Exception e) {
					// empty
				}
				try {
					if (getDisplayable().getName().toUpperCase().contains("DIRECTORY")) {
						Action ba = new AbstractAction("...") {
							private static final long serialVersionUID = 1L;
							
							@Override
							public void actionPerformed(ActionEvent e) {
								File f = OpenFileDialogService.getDirectoryFromUser("Select Folder");
								if (f != null)
									textComp.setText(f.getAbsolutePath());
							}
						};
						JButton selButton = new JButton(ba);
						selButton.setToolTipText("Browse for local folder");
						ComponentBorder cb = new ComponentBorder(selButton, Edge.RIGHT);
						cb.install(textComp);
					}
				} catch (Exception e) {
					// empty
				}
			}
			
			textComp.setMinimumSize(new Dimension(0, 30));
			textComp.setPreferredSize(new Dimension(multiRow ? 500 : 50, 30));
			textComp.setMaximumSize(new Dimension(2000, 30));
		}
		
		if (!(getDisplayable() instanceof Attribute))
			return textComp;
		else
			return TableLayout.getSplit(textComp, searchComponent, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	private JComponent getSearchComponent() {
		final JButton s = new JButton("Select");
		s.setOpaque(false);
		s.setToolTipText("Click to select all graph elements with the same attribute value");
		s.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String search = textComp.getText().toUpperCase();
				search = StringManipulationTools.removeHTMLtags(search);
				if (!(getDisplayable() instanceof Attribute)) {
					s.setEnabled(false);
					ErrorMsg.addErrorMessage("Internal error, can't perform attribute value search");
					return;
				}
				Attribute attr = (Attribute) getDisplayable();
				
				String path = attr.getPath();
				if (path.indexOf(".") >= 0)
					path = path.substring(0, path.lastIndexOf("."));
				String attributeName = attr.getId();
				
				Collection<GraphElement> select = new ArrayList<GraphElement>();
				
				boolean subsearch = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
				
				for (GraphElement ge : MainFrame.getInstance().getActiveEditorSession().getGraph().getGraphElements()) {
					String val = (String) AttributeHelper.getAttributeValue(ge, path, attributeName, null, "");
					if (val == null)
						continue;
					val = StringManipulationTools.removeHTMLtags(val);
					if (subsearch) {
						if (val.toUpperCase().contains(search))
							select.add(ge);
					} else {
						if (val.equalsIgnoreCase(search))
							select.add(ge);
					}
				}
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().addAll(select);
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
				
				MainFrame.showMessage("Added " + select.size() + " elements to selection. Press Shift while clicking the search button to search within text.",
						MessageType.INFO);
			}
		});
		return s;
	}
	
	/**
	 * Sets the current value of the displayable in the corresponding <code>JComponent</code>.
	 */
	@Override
	public void setEditFieldValue() {
		if (showEmpty) {
			this.textComp.setText(EMPTY_STRING);
		} else {
			this.textComp.setText(displayable.getValue().toString());
		}
		searchComponent.setEnabled(!showEmpty);
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. But only if it is different.
	 */
	@Override
	public void setValue() {
		String text = this.textComp.getText();
		
		if (!text.equals(EMPTY_STRING) &&
				!this.displayable.getValue().toString().equals(text)) {
			this.displayable.setValue(text);
		}
	}
	
	@Override
	public void setParameter(String setting, Object value) {
		super.setParameter(setting, value);
		if (setting.equals("multiline"))
			multiline = (Boolean) value;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
