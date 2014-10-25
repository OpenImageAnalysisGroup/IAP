package de.ipk.ag_ba.gui.picture_gui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.StringManipulationTools;

/**
 * @author Christian Klukas
 */
public class FilterConnector {
	
	private JTextField tf;
	private TreeSelectionListener treeSelectionListener;
	private TreeSelectionEvent e;
	private JLabel infolabel;
	private int matches = 0;
	private int notmatches = 0;
	
	public void textFieldDefined(JTextField tf, JLabel infolabel) {
		this.infolabel = infolabel;
		infolabel.setForeground(Color.GRAY);
		if (this.tf != tf) {
			tf.getDocument().addDocumentListener(new DocumentListener() {
				
				@Override
				public void removeUpdate(DocumentEvent e) {
					updateClick();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) {
					updateClick();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
					updateClick();
				}
			});
		}
		this.tf = tf;
	}
	
	private String lastRotationString = null;
	
	public boolean matches(String label, final boolean isLastInList, boolean count) {
		String rs = null;
		if (!isLastInList)
			rs = StringManipulationTools.getRotationString(1);
		if (isLastInList || !rs.equals(lastRotationString)) {
			lastRotationString = rs;
			final String lrs = rs;
			final boolean diff = notmatches > 0;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (isLastInList) {
						if (matches == 0 && notmatches > 0)
							infolabel.setForeground(Color.RED);
						else
							infolabel.setForeground(Color.GRAY);
					} else
						infolabel.setForeground(Color.LIGHT_GRAY);
					if (!isLastInList && matches + notmatches == 0)
						infolabel.setText((isLastInList ? "" : "- - -"));
					else
						if (diff)
							infolabel.setText((isLastInList ? "" : lrs + " ") + matches + "/" + (matches + notmatches) + (isLastInList ? "" : " " + lrs));
						else
							infolabel.setText((isLastInList ? "" : lrs + " ") + matches + (isLastInList ? "" : " " + lrs));
				}
			});
		}
		
		if (tf == null || label == null || tf.getText().isEmpty() || label.isEmpty()) {
			if (count)
				matches++;
			return true;
		}
		boolean match = label.contains(tf.getText());
		if (count)
			if (match)
				matches++;
			else
				notmatches++;
		return match;
	}
	
	public void itemClicked(TreeSelectionListener treeSelectionListener, TreeSelectionEvent e) {
		this.treeSelectionListener = treeSelectionListener;
		this.e = e;
		matches = 0;
		notmatches = 0;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infolabel.setText("");
			}
		});
	}
	
	public void updateClick() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infolabel.setText("");
			}
		});
		matches = 0;
		notmatches = 0;
		if (treeSelectionListener != null)
			treeSelectionListener.valueChanged(e);
	}
}
