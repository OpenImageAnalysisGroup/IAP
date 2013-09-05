/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.graffiti.attributes.Attributable;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.actions.URLattributeAction;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

/**
 * @author Christian Klukas
 *         (c) 2004, 2008 IPK-Gatersleben
 */
public class URLAttributeEditor extends AbstractValueEditComponent {
	
	protected JButton jButtonShowURL;
	protected JTextField jEditField;
	
	protected static Collection<URLattributeAction> attributeActions = MainFrame.getInstance().getActionManager().getActions();
	
	public URLAttributeEditor(final Displayable disp) {
		super(disp);
		
		String curVal = ((URLAttribute) getDisplayable()).getString();
		jButtonShowURL = new JButton(getDescription((URLAttribute) getDisplayable(), false, false));
		jButtonShowURL.addActionListener(getActionListener((URLAttribute) getDisplayable(), false));
		
		String tooltip = curVal;
		if (tooltip != null) {
			for (URLattributeAction ua : attributeActions) {
				if (tooltip.startsWith(ua.getPreIdentifyer())) {
					tooltip = tooltip.substring(ua.getPreIdentifyer().length());
					break;
				}
			}
			jButtonShowURL.setToolTipText(tooltip);
		}
		jButtonShowURL.setOpaque(false);
		
		jEditField = new JTextField(curVal);
		jEditField.setPreferredSize(new Dimension(10, (int) jEditField.getPreferredSize().getHeight()));
	}
	
	public static String getDescription(URLAttribute displayable, boolean shortDesc, boolean modifyCommand) {
		String curVal = displayable.getString();
		String prior = "<html><small>";
		if (shortDesc)
			prior = "";
		URLattributeAction ua = getAttributeAction(curVal);
		if (ua != null)
			return prior + ua.getCommandDescription(shortDesc, modifyCommand);
		else
			return "Unknown Action";
	}
	
	private static URLattributeAction getAttributeAction(String attributeValue) {
		URLattributeAction defaultUA = null;
		for (URLattributeAction ua : attributeActions) {
			if (ua.getPreIdentifyer().length() <= 0)
				defaultUA = ua;
			else {
				if (attributeValue.startsWith(ua.getPreIdentifyer()))
					return ua;
			}
		}
		return defaultUA;
	}
	
	public static ActionListener getActionListener(final URLAttribute disp, final boolean modifyCommand) {
		if (disp == null)
			return getDefaultActionListener("Error: Displayable is NULL!");
		URLattributeAction ua = getAttributeAction(disp.getString());
		if (ua == null)
			return getDefaultActionListener("Error: No default handler for current attribute value! (" + disp.getString() + ")");
		return ua.getActionListener(disp, getGraph(disp), getGraphElement(disp), modifyCommand);
	}
	
	private static ActionListener getDefaultActionListener(final String msg) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainFrame.showMessageDialog(msg, "Information");
			}
		};
	}
	
	private static GraphElement getGraphElement(URLAttribute disp) {
		Attributable a = disp.getAttributable();
		if (a != null && a instanceof GraphElement)
			return (GraphElement) a;
		else
			return null;
	}
	
	private static Graph getGraph(URLAttribute disp) {
		Attributable a = disp.getAttributable();
		if (a != null && a instanceof GraphElement)
			return ((GraphElement) a).getGraph();
		else {
			if (a != null && a instanceof Graph)
				return (Graph) a;
			else
				return null;
		}
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplit(
							jEditField,
							jButtonShowURL,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			jButtonShowURL.setEnabled(false);
			jEditField.setText(EMPTY_STRING);
		} else {
			jButtonShowURL.setEnabled(true);
			String curVal = ((URLAttribute) getDisplayable()).getString();
			jEditField.setText(curVal);
			URLattributeAction ua = getAttributeAction(curVal);
			if (ua != null)
				jButtonShowURL.setText(ua.getCommandDescription(false, false));
			else
				jButtonShowURL.setText("Unknown Action");
		}
	}
	
	public void setValue() {
		if (!jEditField.getText().equals(EMPTY_STRING))
			((URLAttribute) displayable).setString(jEditField.getText());
	}
	
	public static boolean supportsModifyCommand(URLAttribute displayable) {
		if (displayable == null)
			return false;
		String curVal = (displayable).getString();
		if (curVal == null)
			return false;
		URLattributeAction ua = getAttributeAction(curVal);
		if (ua == null)
			return false;
		return ua.supportsModifyCommand();
	}
}
