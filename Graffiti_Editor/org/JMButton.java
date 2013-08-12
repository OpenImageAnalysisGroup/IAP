package org;

import javax.swing.JButton;
import javax.swing.UIManager;

/**
 * @author klukas
 */
public class JMButton extends JButton {
	private static final long serialVersionUID = 4853578108818002186L;
	
	public JMButton(String text) {
		super(text);
	}
	
	private void mySetText(String text) {
		boolean nativeLookAndFeelActive = UIManager.getLookAndFeel().isNativeLookAndFeel();
		boolean mac = SystemInfo.isMac();
		if (mac && nativeLookAndFeelActive) {
			if (text != null && (text.contains("<br>") || text.contains("<small>"))) {
				if (text.contains("<small>") && !text.contains("<br><small>"))
					putClientProperty("JComponent.sizeVariant", "mini");
				text = StringManipulationTools.stringReplace(text, "<br>", " ");
				text = StringManipulationTools.stringReplace(text, "  ", " ");
				text = StringManipulationTools.removeHTMLtags(text);
				super.setText(text);
			} else
				if (text != null && text.contains("<html>")) {
					text = StringManipulationTools.removeHTMLtags(text);
					super.setText(text);
				} else
					super.setText(text);
			
			putClientProperty("JButton.buttonType", "textured");
			// putClientProperty("JButton.buttonType", "gradient");
		} else
			super.setText(text);
	}
	
	@Override
	public void setText(String text) {
		mySetText(text);
	}
	
	public JMButton() {
		super();
	}
	
}
