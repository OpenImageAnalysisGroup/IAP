package org.graffiti.options;

import javax.swing.JComponent;

/**
 * The interface all option panes must implement. The <i>name</i> of an option
 * pane is returned by the <code>getName()</code> method. the label displayed
 * in the option pane's tab is obtained from the <code>options.<i>name</i>.label</code> property.
 * <p>
 * Note that in most cases, it is easier to extend <code>AbstractOptionPane</code>.
 * </p>
 * 
 * @author flierl
 * @version $Revision: 1.1 $
 */
public interface OptionPane {
	// ~ Methods ================================================================
	
	/**
	 * Returns the component, that should be displayed for this option pane.
	 * 
	 * @return DOCUMENT ME!
	 */
	JComponent getOptionDialogComponent();
	
	/**
	 * Returns the internal name of this option pane. The option pane's label
	 * is set to the value of the property <code>options.<i>name</i>.label</code>.
	 * 
	 * @return DOCUMENT ME!
	 */
	String getName();
	
	String getCategory();
	
	String getOptionName();
	
	/**
	 * This method is called every time this option pane is displayed. The <code>AbstractOptionPane</code> class uses this to create the option
	 * pane's GUI only when needed.
	 */
	void init(JComponent options);
	
	/**
	 * Called when the options dialog's "ok" button is clicked. This should
	 * save any properties being edited in this option pane.
	 */
	void save(JComponent options);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
