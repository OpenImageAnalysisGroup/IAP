/*
 * Created on 21.09.2005 by Christian Klukas
 */
package org.graffiti.editor;

import info.clearthought.layout.TableLayout;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MyScrollLabel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public MyScrollLabel(String msg) {
		this.setLayout(TableLayout.getLayout(500, 300));
		final JScrollPane jsp = new JScrollPane(new JEditorPane("text/html", msg));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jsp.getVerticalScrollBar().setValue(0);
			}
		});
		add(jsp, "0,0");
		validate();
	}
	
	public MyScrollLabel(String msg, double width, double height) {
		this.setLayout(TableLayout.getLayout(width, height));
		final JScrollPane jsp = new JScrollPane(new JEditorPane("text/html", msg));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jsp.getVerticalScrollBar().setValue(0);
			}
		});
		add(jsp, "0,0");
		validate();
	}
}
