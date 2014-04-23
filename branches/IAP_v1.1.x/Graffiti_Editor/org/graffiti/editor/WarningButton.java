package org.graffiti.editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

public class WarningButton extends JComponent {
	
	private final JButton bt;
	
	public WarningButton(String btText, final Runnable doOnClick) {
		bt = new JButton(btText);
		bt.setBackground(new Color(255, 255, 220));
		bt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOnClick.run();
			}
		});
		
	}
	
	public JButton getButton() {
		return bt;
	}
	
}
