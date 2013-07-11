/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.gui.GraffitiContainer;
import org.graffiti.plugin.gui.GraffitiMenu;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.PatternGraffitiHelper;

/**
 * Shows the info dialog for PatternGraffiti
 * 
 * @author Christian Klukas
 */
public class MenuItemInfoDialog
					extends GraffitiMenu
					implements GraffitiContainer {
	// to avoid collisions let ID be package name + menu + name of the menu
	
	private static final long serialVersionUID = 1L;
	/**
	 * DOCUMENT ME!
	 */
	public static final String ID =
						"de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog.MenuItemInfoDialog";
	
	/**
	 * Create Menu.
	 */
	public MenuItemInfoDialog() {
		super();
		setName("ipk.help");
		setText("Help");
		setMnemonic('H');
		setEnabled(true);
		JMenuItem info = new JMenuItem("About PatternGravisto");
		
		info.setMnemonic('I');
		info.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0)
			{
				ClassLoader cl = this.getClass().getClassLoader();
				String path = this.getClass().getPackage().getName().replace('.', '/');
				
				ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
				JOptionPane.showOptionDialog(
									null, "<html><b>" +
														PatternGraffitiHelper.PATTERN_GRAFFITI_VERSION +
														"</b><p><p>" + "PatternGravisto (c) 2003-2007 IPK-Gatersleben" +
														"<br>" + "Gravisto (c) 2001-2004 University of Passau" +
														"<p><p>http://nwg.bic-gh.de<p>http://www.gravisto.org",
									// PatternGraffitiHelper.getPluginStatusText()
						"Info about PatternGravisto",
									JOptionPane.DEFAULT_OPTION,
									JOptionPane.INFORMATION_MESSAGE,
									icon, null, null);
			}
		});
		
		JMenuItem error = new JMenuItem("Status");
		error.setMnemonic('E');
		error.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] errorMsgs = ErrorMsg.getErrorMessages();
				String err;
				if (errorMsgs.length > 0) {
					err = "<html>" + errorMsgs.length + " Exceptions:<p>";
					for (int i = 0; i < errorMsgs.length; i++) {
						if (errorMsgs[i] != null)
							err = err + "<p>" + errorMsgs[i];
					}
				} else {
					err = "No error messages available.";
				}
				ErrorMsg.clearErrorMessages();
				GravistoService.getInstance().getMainFrame().showMessageDialog(err);
			}
		});
		
		insert(error, 1);
		insert(info, 2);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getId() {
		return ID;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
