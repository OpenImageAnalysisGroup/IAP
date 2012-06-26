// ==============================================================================
//
// BooleanEditComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: BooleanEditComponent.java,v 1.1 2011-01-31 09:03:26 klukas Exp $

package org.graffiti.plugins.editcomponents.defaults;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugin.parameter.BooleanParameter;

/**
 * Represents a component, which can edit a boolean value.
 * 
 * @version $Revision: 1.1 $
 */
public class BooleanEditComponent
					extends AbstractValueEditComponent {
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private Icon defaultIcon;
	
	/** DOCUMENT ME! */
	private Icon defaultSelectedIcon;
	
	/** DOCUMENT ME! */
	private Icon emptyIcon;
	
	/** The gui element of this component. */
	private JCheckBox checkBox;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new boolean edit component, referencing the given
	 * displayable.
	 * 
	 * @param disp
	 *           DOCUMENT ME!
	 */
	public BooleanEditComponent(final Displayable disp) {
		super(disp);
		this.checkBox = new JCheckBox();
		if (disp instanceof BooleanParameter && ((BooleanParameter) disp).isLeftAligned()) {
			checkBox.setText(((BooleanParameter) disp).getName());
		}
		
		checkBox.putClientProperty("displayable", disp);
		
		checkBox.setOpaque(false);
		// this.checkBox.setAlignmentX(0.5f);
		
		// iBundle = org.graffiti.core.ImageBundle.getInstance();
		// defaultIcon = new ImageIcon(iBundle.getImage("bool.notselected"));
		// emptyIcon = new ImageIcon(iBundle.getImage("bool.half.notselected"));
		// defaultSelectedIcon = new ImageIcon(iBundle.getImage("bool.selected"));
		
		checkBox.setPressedIcon(emptyIcon);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				checkDependentParameters(disp);
			}
		});
		
		checkBox.addActionListener(new AbstractAction()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e)
				{
					if (showEmpty)
					{
						showEmpty = false;
						setEditFieldValue();
					}
					checkDependentParameters(disp);
				}
		});
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the <code>JComponent</code> for editing this edit component.
	 * 
	 * @return the <code>JComponent</code> for editing this edit component.
	 */
	public JComponent getComponent() {
		if (displayable.getIcon() != null) {
			JComponent jc = displayable.getIcon();
			jc.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					checkBox.setSelected(!checkBox.isSelected());
				}
				
				public void mousePressed(MouseEvent e) {
				}
				
				public void mouseReleased(MouseEvent e) {
				}
				
				public void mouseEntered(MouseEvent e) {
				}
				
				public void mouseExited(MouseEvent e) {
				}
			});
			if (checkBox != null)
				checkBox.setPreferredSize(new Dimension(checkBox.getMinimumSize().width, checkBox.getPreferredSize().height));
			JPanel jp = (JPanel) TableLayout.getSplit(checkBox, jc, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
			jp.setOpaque(false);
			return jp;
		} else
			return checkBox;
	}
	
	/**
	 * Sets the current value of the <code>Attribute</code> in the
	 * corresponding <code>JComponent</code>.
	 */
	public void setEditFieldValue() {
		if (showEmpty) {
			checkBox.setSelectedIcon(emptyIcon);
			checkBox.setIcon(emptyIcon);
		} else {
			checkBox.setSelectedIcon(defaultSelectedIcon);
			checkBox.setIcon(defaultIcon);
		}
		
		checkBox.setSelected(((Boolean) this.displayable.getValue()).booleanValue());
	}
	
	/**
	 * Sets the value of the displayable specified in the <code>JComponent</code>. But only if it is different.
	 */
	public void setValue() {
		boolean bb = false;
		
		if (!(this.displayable.getValue() instanceof Boolean)) {
			bb = new Boolean((String) this.displayable.getValue());
		} else {
			bb = (Boolean) this.displayable.getValue();
		}
		if (!(bb == this.checkBox.isSelected())) {
			displayable.setValue(new Boolean(checkBox.isSelected()));
			
			// check if this edit component is about to edit the graph attribute directed, if so, update graph
			if (displayable != null && displayable instanceof BooleanAttribute && displayable.getName().equals("directed")) {
				BooleanAttribute ba = (BooleanAttribute) displayable;
				if (ba.getAttributable() != null && (ba.getAttributable() instanceof Graph)) {
					Graph gg = (Graph) ba.getAttributable();
					gg.setDirected(ba.getBoolean(), true);
				}
			}
		}
	}
	
	private void checkDependentParameters(final Displayable disp) {
		if (disp instanceof BooleanParameter) {
			BooleanParameter bp = (BooleanParameter) disp;
			BooleanParameter[] dp = bp.getDependentParameters();
			if (dp != null && dp.length > 0) {
				JDialog jd = (JDialog) ErrorMsg.findParentComponent(checkBox, JDialog.class);
				if (jd != null) {
					ArrayList<Object> res = new ArrayList<Object>();
					ErrorMsg.findChildComponents(jd, JCheckBox.class, res);
					for (Object o : res) {
						if (o instanceof JCheckBox) {
							JCheckBox depCheck = (JCheckBox) o;
							Object oo = depCheck.getClientProperty("displayable");
							boolean found = false;
							if (oo != null)
								for (BooleanParameter b : dp) {
									if (oo == b) {
										found = true;
									}
								}
							if (found)
								if (checkBox.isSelected())
									depCheck.setEnabled(true);
								else
									depCheck.setEnabled(false);
						}
					}
				}
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
