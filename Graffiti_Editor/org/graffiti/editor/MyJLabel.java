/*
 * Created on 21.09.2005 by Christian Klukas
 */
package org.graffiti.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

public class MyJLabel extends JLabel {
	private static final long serialVersionUID = 1L;
	private String fullText = "";
	
	public MyJLabel(String text) {
		super();
		setText(text);
		
		KeyStroke statusHotkey = KeyStroke.getKeyStroke(
							KeyEvent.VK_F2, 0, false);
		String STATUS_KEY = "MyStatus";
		Action uirobot = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				setEnabled(false); // stop any other events from interfering
				if (fullText != null && fullText.trim().length() > 0)
					MainFrame.showMessageDialogWithScrollBars(fullText, "Status Message");
				setEnabled(true);
			}
		};
		
		GlobalHotkeyManager hotkeyManager = GlobalHotkeyManager.getInstance();
		hotkeyManager.getInputMap().put(statusHotkey, STATUS_KEY);
		hotkeyManager.getActionMap().put(STATUS_KEY, uirobot);
		
		this.addMouseListener(new MouseListener() {
			
			public void mouseClicked(MouseEvent e) {
				if (fullText != null && fullText.trim().length() > 0)
					MainFrame.showMessageDialogWithScrollBars(fullText, "Status Message");
			}
			
			public void mousePressed(MouseEvent e) {
				//
				
			}
			
			public void mouseReleased(MouseEvent e) {
				//
				
			}
			
			public void mouseEntered(MouseEvent e) {
				//
				
			}
			
			public void mouseExited(MouseEvent e) {
				//
				
			}
		});
	}
	
	@Override
	public void setText(String text) {
		fullText = text;
		JLabel test = new JLabel(text);
		if (test.getPreferredSize().height > 150) {
			super.setText(getTrimmText(text, "<!-- optstart -->", "<!-- optend -->"));
		} else {
			super.setText(text);
		}
	}
	
	private String getTrimmText(String text, String t1, String t2) {
		int a = text.indexOf(t1);
		int b = text.indexOf(t2);
		if (a >= 0 && b >= 0) {
			return text.substring(0, a) + "[To view the data table press F2]" + text.substring(b + t2.length());
		} else
			return text;
	}
	
}
