/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class JSpinnerSelectOnTab extends JSpinner {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JSpinnerSelectOnTab() {
		super();
		addSelectionOnFocus();
	}
	
	private void addSelectionOnFocus() {
		((JSpinner.DefaultEditor) this.getEditor()).getTextField().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (e.getSource() instanceof JTextComponent) {
					final JTextComponent textComponent = ((JTextComponent) e.getSource());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							textComponent.selectAll();
						}
					});
				}
			}
		});
	}
	
	// 1)
	// get the editor textField, call it e.g. editor
	//
	// 2)
	// add a changeListener to the spinner
	//
	// 3) in stateChanged (wrapped in a SwingUtilities.invokeLater) put
	// editor.selectAll();
	//
	// private void addSelectionOnArrowChange() {
	// ((JSpinner.DefaultEditor )this.getEditor()).getTextField().addC (new
	// FocusAdapter() {
	// public void focusGained(FocusEvent e) {
	// if (e.getSource() instanceof JTextComponent) {
	// final JTextComponent textComponent=((JTextComponent)e.getSource());
	// SwingUtilities.invokeLater(new Runnable(){
	// public void run() {
	// textComponent.selectAll();
	// }});
	// }
	// }
	// });
	// }
	
	public JSpinnerSelectOnTab(SpinnerNumberModel spinnerNumberModel) {
		super(spinnerNumberModel);
		addSelectionOnFocus();
	}
	
	@Override
	public void setModel(SpinnerModel model) {
		super.setModel(model);
		addSelectionOnFocus();
	}
	
	public float getValueFromSpinner() {
		return Float.parseFloat(getValue().toString());
	}
	
}
