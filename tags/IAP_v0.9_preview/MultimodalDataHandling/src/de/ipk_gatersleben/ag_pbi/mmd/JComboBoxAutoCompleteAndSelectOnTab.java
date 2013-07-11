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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

// the autocompletion is taken from
// http://www.java.happycodings.com/Java_Swing/code5.html
@SuppressWarnings({"rawtypes", "unchecked"})
public class JComboBoxAutoCompleteAndSelectOnTab extends JComboBox implements JComboBox.KeySelectionManager, FocusListener {
	
	private static final long serialVersionUID = 1L;
	private String searchFor;
	private long lap;
	private final FocusListener listener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			JComboBoxAutoCompleteAndSelectOnTab.this.focusLost(e);
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			JComboBoxAutoCompleteAndSelectOnTab.this.focusGained(e);
		}
	};
	
	public class CBDocument extends PlainDocument {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
			if (str == null)
				return;
			super.insertString(offset, str, a);
			if (!isPopupVisible() && str.length() != 0)
				fireActionEvent();
		}
	}
	
	public JComboBoxAutoCompleteAndSelectOnTab() {
		super();
		addAutoComplete();
	}
	
	public JComboBoxAutoCompleteAndSelectOnTab(Object[] values) {
		super(values);
		addAutoComplete();
	}
	
	public JComboBoxAutoCompleteAndSelectOnTab(ComboBoxModel aModel) {
		super(aModel);
		addAutoComplete();
	}
	
	public JComboBoxAutoCompleteAndSelectOnTab(Vector<?> items) {
		super(items);
		addAutoComplete();
	}
	
	public void addSelectionOnTab() {
		for (Component c : getComponents()) {
			c.removeFocusListener(listener); // we dont want to add more and more
			// listeners
			c.addFocusListener(listener);
		}
	}
	
	private void addAutoComplete() {
		lap = new java.util.Date().getTime();
		setKeySelectionManager(this);
		JTextField tf;
		if (getEditor() != null) {
			tf = (JTextField) getEditor().getEditorComponent();
			if (tf != null) {
				tf.setDocument(new CBDocument());
				addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						JTextField tf = (JTextField) getEditor().getEditorComponent();
						String text = tf.getText();
						ComboBoxModel aModel = getModel();
						String current;
						for (int i = 0; i < aModel.getSize(); i++) {
							current = aModel.getElementAt(i).toString();
							if (current.toLowerCase().startsWith(text.toLowerCase())) {
								tf.setText(current);
								tf.setSelectionStart(text.length());
								tf.setSelectionEnd(current.length());
								break;
							}
						}
					}
				});
			}
		}
	}
	
	public int selectionForKey(char aKey, ComboBoxModel aModel) {
		long now = new java.util.Date().getTime();
		if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE && searchFor.length() > 0) {
			searchFor = searchFor.substring(0, searchFor.length() - 1);
		} else {
			// System.out.println(lap);
			// Kam nie hier vorbei.
			if (lap + 1000 < now)
				searchFor = "" + aKey;
			else
				searchFor = searchFor + aKey;
		}
		lap = now;
		String current;
		for (int i = 0; i < aModel.getSize(); i++) {
			current = aModel.getElementAt(i).toString().toLowerCase();
			if (current.toLowerCase().startsWith(searchFor.toLowerCase()))
				return i;
		}
		return -1;
	}
	
	@Override
	public void focusGained(FocusEvent e) {
		JTextField tf = (JTextField) getEditor().getEditorComponent();
		tf.selectAll();
		
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		JTextField tf = (JTextField) getEditor().getEditorComponent();
		tf.select(0, 0);
	}
	
	public void addFocusListenerToItems(FocusListener listener) {
		for (Component c : getComponents())
			c.addFocusListener(listener);
	}
	
	public void removeFocusListenerFromItems(FocusListener listener) {
		for (Component c : getComponents())
			c.removeFocusListener(listener);
	}
	
	public void addItem(Object objectToBeDisplayed, String toBeDisplayedText) {
		addItem(new StringWrappingObject(objectToBeDisplayed, toBeDisplayedText));
	}
	
	@Override
	public Object getItemAt(int index) {
		Object o = super.getItemAt(index);
		if (o instanceof StringWrappingObject)
			return ((StringWrappingObject) o).objectToBeDisplayed;
		else
			return o;
	}
	
	@Override
	public Object getSelectedItem() {
		Object o = super.getSelectedItem();
		if (o instanceof StringWrappingObject)
			return ((StringWrappingObject) o).objectToBeDisplayed;
		else
			return o;
	}
	
	@Override
	public Object[] getSelectedObjects() {
		Object[] o = super.getSelectedObjects();
		if (o[0] instanceof StringWrappingObject)
			return new Object[] { ((StringWrappingObject) o[0]).objectToBeDisplayed };
		else
			return o;
	}
	
	private class StringWrappingObject {
		
		private final Object objectToBeDisplayed;
		private final String toBeDisplayedText;
		
		public StringWrappingObject(Object objectToBeDisplayed, String toBeDisplayedText) {
			this.objectToBeDisplayed = objectToBeDisplayed;
			this.toBeDisplayedText = toBeDisplayedText;
		}
		
		@Override
		public String toString() {
			return toBeDisplayedText;
		}
		
	}
	
}