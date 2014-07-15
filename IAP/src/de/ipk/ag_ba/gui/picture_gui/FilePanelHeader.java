/*******************************************************************************
 * The DBE2 Add-on is (c) 2009-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0 (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions of
 * this license, the below stated disclaimer of warranties and the licenses of the used
 * libraries. For further details see license.txt in the root folder of this project.
 ******************************************************************************/
package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FilePanelHeader extends JPanel {
	private static final long serialVersionUID = 6358431643283288603L;
	
	JLabel header;
	JButton button;
	
	public FilePanelHeader(JButton button, JButton[] actions, String desc) {
		setBackground(new Color(240, 245, 240));
		setOpaque(true);
		
		this.button = button;
		
		double[][] size = new double[][] { { 3, TableLayout.PREFERRED, 6, TableLayout.FILL, 6, TableLayout.PREFERRED, 3 },
				{ 3, TableLayout.PREFERRED, 3 } };
		
		setLayout(new TableLayout(size));
		
		add(button, "1,1");
		
		header = new JLabel();
		header.setText("");
		header.validate();
		header.repaint();
		// header.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		
		add(header, "3,1");
		
		if (actions != null && actions.length > 0) {
			ArrayList<JComponent> jl = new ArrayList<JComponent>();
			jl.add(new JLabel(desc));
			for (JButton jb : actions)
				jl.add(jb);
			JComponent bg = TableLayout.getMultiSplit(jl, TableLayout.PREFERRED, TableLayout.FILL, 0, 3, 0, 3);
			add(bg, "5,1");
		}
	}
	
	public void setText(String msg) {
		header.setText(msg);
		header.validate();
		header.repaint();
	}
	
	public void enableButton(boolean enableButton) {
		button.setEnabled(enableButton);
	}
	
	public boolean isButtonEnabled() {
		return button.isEnabled();
	}
}
