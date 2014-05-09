// ==============================================================================
//
// ParameterEditPanel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ParameterEditPanel.java,v 1.3 2012-01-17 15:47:01 klukas Exp $

package org.graffiti.editor.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.graffiti.plugin.ToolTipHelper;
import org.graffiti.plugin.editcomponent.StandardValueEditComponent;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.parameter.AbstractSingleParameter;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.SelectionParameter;
import org.graffiti.selection.Selection;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;

/**
 * Represents a parameter edit panel.
 * 
 * @version $Revision: 1.3 $
 */
public class ParameterEditPanel extends JPanel {
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	private List<ValueEditComponent> displayedVEC;
	
	/**
	 * Maps from an displayable class name to the class name of a <code>ValueEditComponent</code>.
	 */
	private Map<?, ?> editTypeMap;
	
	/** The list of parameters to display and edit. */
	private Parameter[] parameters;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Instantiates a new edit panel.
	 * 
	 * @param parameters
	 *           DOCUMENT ME!
	 * @param editTypes
	 *           DOCUMENT ME!
	 * @param selection
	 *           DOCUMENT ME!
	 */
	
	public ParameterEditPanel(Parameter[] parameters, Map<?, ?> editTypes,
			Selection selection, String title, boolean fillSurroundingStyle, String heading) {
		this(parameters, editTypes, selection, title, fillSurroundingStyle, heading, null);
	}
	
	public ParameterEditPanel(Parameter[] parameters, Map<?, ?> editTypes,
			Selection selection, String title, boolean fillSurroundingStyle, String heading, JComponent descComponent) {
		super();
		
		this.parameters = parameters;
		this.displayedVEC = new LinkedList<ValueEditComponent>();
		setEditTypeMap(editTypes);
		
		ActionListener helpL = null;
		String helpTopic = AttributeHelper.getHelpTopicFor(title, "parameter dialog");
		if (helpTopic == null)
			helpTopic = AttributeHelper.getHelpTopicFor(heading, "parameter dialog");
		// if (helpTopic != null)
		// helpL = JLabelJavaHelpLink.getHelpActionListener(helpTopic);
		FolderPanel myPanel = new FolderPanel(title, false, false, false, helpL);
		
		int paramCnt = 0;
		if (parameters != null) {
			for (Object o : parameters)
				if (o != null)
					paramCnt++;
		}
		
		// if (fillSurroundingStyle) {
		// setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 0));
		// } else {
		// setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN));
		// // myPanel.setFrameColor(getBackground(), Color.BLACK, 0, 3);
		// // myPanel.setBackground(getBackground());
		// }
		
		double[][] size = new double[][] {
				{ TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL }
		};
		
		setLayout(new TableLayout(size));
		
		myPanel.setBackground(null);
		
		myPanel.setFrameColor(null, Color.BLACK, 0, 2);
		// myPanel.setFrameColor(new Color(255, 255, 255), Color.BLACK, 0, 2);
		
		myPanel.setOpaque(false);
		buildTable(selection, myPanel);
		
		// if (descComponent!=null)
		// myPanel.addFirstGuiComponentRow(descComponent, new JLabel(title), false);
		myPanel.layoutRows();
		
		JComponent jc = myPanel;
		
		if (paramCnt > 0) {
			jc = new JScrollPane(myPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jc.setBorder(null);
			jc.setOpaque(false);
		}
		
		if (descComponent != null) {
			add(TableLayout.getSplit(descComponent, jc, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED), "0,0");
		} else
			add(jc, "0,0");
		
		validate();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the map of displayable types to the given map.
	 * 
	 * @param map
	 *           DOCUMENT ME!
	 */
	public void setEditTypeMap(Map<?, ?> map) {
		this.editTypeMap = map;
	}
	
	/**
	 * Sets the paramter array this panel displays.
	 * 
	 * @param params
	 */
	public void setParameters(Parameter[] params) {
		this.parameters = params;
	}
	
	/**
	 * Returns the array of parameters with the values updated from the dialog.
	 * 
	 * @return Parameter[]
	 */
	public Parameter[] getUpdatedParameters() {
		for (Iterator<ValueEditComponent> it = displayedVEC.iterator(); it.hasNext();) {
			(it.next()).setValue();
		}
		
		return this.parameters;
	}
	
	/**
	 * Builds the table that is used for editing parameters
	 * 
	 * @param selection
	 *           list of parameters.
	 * @param myPanel
	 *           The folder Panel where the rows are added
	 */
	public void buildTable(Selection selection, FolderPanel myPanel) {
		myPanel.clearGuiComponentList();
		displayedVEC = new LinkedList<ValueEditComponent>();
		addValueEditComponents(myPanel, selection);
		myPanel.layoutRows();
	}
	
	/**
	 * Returns a (noneditable) textfield showing the value of the <code>toString</code> method of the parameter. Used when there is no
	 * registered <code>ValueEditComponent</code>.
	 * 
	 * @param parameter
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private ValueEditComponent getStandardEditComponent(Parameter parameter) {
		ValueEditComponent vec = null;
		
		if (parameter != null && parameter instanceof BooleanParameter) {
			try {
				vec = (ValueEditComponent) InstanceLoader.createInstance("org.graffiti.plugins.editcomponents.defaults.BooleanEditComponent", parameter);
			} catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		} else {
			vec = new StandardValueEditComponent(parameter);
			
			// JTextField textField = new JTextField(parameter.getValue().toString());
			JTextField textField = (JTextField) vec.getComponent();
			textField.setEditable(false);
			textField.setMinimumSize(new Dimension(0, 20));
			textField.setPreferredSize(new Dimension(100, 30));
			textField.setMaximumSize(new Dimension(2000, 40));
		}
		return vec;
	}
	
	/**
	 * Add one row in the panel.
	 * 
	 * @param idPanel
	 * @param editFieldPanel
	 * @param parameter
	 * @param ecClass
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	private void addRow(FolderPanel myPanel, Parameter parameter, Class<?> ecClass) {
		String name = parameter.getName();
		boolean multiLine = false;
		if (name != null && name.endsWith("//")) {
			name = name.substring(0, name.length() - 2);
			multiLine = true;
		}
		JLabel descLabel = new JLabel(name);
		descLabel.setToolTipText(parameter.getDescription());
		descLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
		ValueEditComponent editComp = null;
		
		try {
			editComp = (ValueEditComponent) InstanceLoader.createInstance(ecClass,
					"org.graffiti.plugin.Displayable", parameter);
			if (multiLine)
				editComp.setParameter("multiline", true);
			ToolTipHelper.addToolTip(editComp.getComponent(), parameter
					.getDescription());
		} catch (InstanceCreationException ice) {
			ErrorMsg.addErrorMessage(ice);
			throw new RuntimeException(
					"Could not create an instance of a ValueEditComponent class. "
							+ ice);
		}
		
		editComp.setDisplayable(parameter);
		editComp.setEditFieldValue();
		JComponent editCompComp = editComp.getComponent();
		// idPanel.add(textField);
		if (parameter != null && (
				(parameter instanceof AbstractSingleParameter && ((AbstractSingleParameter) parameter).isLeftAligned()))) {
			myPanel.addGuiComponentRow(editCompComp, null, false);
			editCompComp.setToolTipText(parameter.getDescription());
		} else
			myPanel.addGuiComponentRow(descLabel, editCompComp, false);
		displayedVEC.add(editComp);
	}
	
	/**
	 * Add one row in the panel using a standard edit component.
	 * 
	 * @param parameter
	 * @param idPanel
	 * @param editFieldPanel
	 */
	private void addStandardTextEditComponentRow(FolderPanel myPanel, Parameter parameter) {
		ValueEditComponent editComp = getStandardEditComponent(parameter);
		displayedVEC.add(editComp);
		
		JLabel descLabel = new JLabel(parameter.getName());
		descLabel.setToolTipText(parameter.getDescription());
		descLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		descLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
		JComponent editCompComp = editComp.getComponent();
		editCompComp.setToolTipText(parameter.getDescription());
		
		myPanel.addGuiComponentRow(descLabel, editCompComp, false);
	}
	
	private void addValueEditComponents(FolderPanel myPanel,
			Selection selection) {
		if (parameters != null)
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] instanceof SelectionParameter) {
					// use currently active (given) selection instead
					parameters[i] = new SelectionParameter(parameters[i].getName(),
							parameters[i].getDescription());
					parameters[i].setValue(selection);
				}
				
				/*
				 * check whether there exists a ValueEditComponent, if not use
				 * standard edit component
				 */
				Class<?> ecClass = null;
				if (parameters[i] != null && editTypeMap != null)
					ecClass = (Class<?>) this.editTypeMap.get(parameters[i].getClass());
				
				if (ecClass != null) {
					// if we have a registered component to display it, add it
					addRow(myPanel, parameters[i], ecClass);
				} else {
					// no component registered for this basic displayable
					if (parameters[i] != null)
						addStandardTextEditComponentRow(myPanel, parameters[i]);
				}
			}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
