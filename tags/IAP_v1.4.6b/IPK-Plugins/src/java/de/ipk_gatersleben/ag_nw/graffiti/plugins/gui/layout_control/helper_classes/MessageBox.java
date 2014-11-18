/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Displays a Delphi/Borland style MessageBox. <CODE>
 *     MessageBox m = new MessageBox(this,"Name dialog","Your first
     name","Tov Are");
 *     m.Show();
 *     if (m.getText() != null){
 *         System.out.println("Your first name is: "+m.getText());
 *     }else{
 *         System.out.println("The user refused to enter his name.");
 *     }
 * </CODE>
 */
public class MessageBox extends Dialog implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L;
	Button ok;
	Button cancel;
	TextField inp;
	String current;
	
	/**
	 * @param f
	 *           The parent frame.
	 * @param title
	 *           The dialog title
	 * @param prompt
	 *           The text before the input field.
	 * @param default The default text.
	 */
	public MessageBox(Frame f, String title, String prompt, String defaultS) {
		super(f, title, true);
		setLayout(new GridLayout(1, 2));
		current = defaultS;
		/*
		 * The Text entry control.
		 */
		Panel tPan = new Panel();
		inp = new TextField(current, 20);
		
		tPan.add(new Label(prompt));
		tPan.add(inp);
		/*
		 * The control gadgets.
		 */
		ok = new Button("Ok");
		cancel = new Button("Cancel");
		ok.addActionListener(this);
		cancel.addActionListener(this);
		Panel bPan = new Panel();
		bPan.add(ok);
		bPan.add(cancel);
		/*
		 * Putting it all together.
		 */
		add(tPan);
		add(bPan);
		/*
		 * Closing the window is the same
		 * as cancel.
		 */
		addWindowListener(this);
		pack();
	}
	
	/**
	 * @returns The user input, or null if the user pressed cancel.
	 */
	public String getText() {
		return current;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			current = inp.getText();
			setVisible(false);
			dispose();
		} else
			if (e.getSource() == cancel) {
				current = null;
				setVisible(false);
				dispose();
			}
	}
	
	public void windowOpened(WindowEvent e) {
	}
	
	public void windowClosing(WindowEvent e) {
		current = null;
		setVisible(false);
		dispose();
	}
	
	public void windowClosed(WindowEvent e) {
	}
	
	public void windowIconified(WindowEvent e) {
	}
	
	public void windowDeiconified(WindowEvent e) {
	}
	
	public void windowActivated(WindowEvent e) {
	}
	
	public void windowDeactivated(WindowEvent e) {
	}
}
