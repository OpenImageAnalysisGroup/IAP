package org.graffiti.plugin.parameter;

import javax.swing.JComponent;

/**
 * @author klukas
 * @version $Revision: 1.1 $
 */
public class JComponentParameter
					extends AbstractSingleParameter {
	private JComponent gui;
	
	public JComponentParameter(JComponent val, String name, String description) {
		super(null, name, description);
		this.gui = val;
	}
	
	@Override
	public Object getValue() {
		return gui;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
