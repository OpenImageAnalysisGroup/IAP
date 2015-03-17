/*
 * Created by IntelliJ IDEA.
 * User: tmo
 * Date: 25.07.2002
 * Time: 11:47:39
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.jfree.ui;

import java.awt.Color;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.BorderUIResource;

/**
 * A utility class...
 */
public class UIUtilities {

	/**
	 * Set up the user interface.
	 */
	public static void setupUI() {
		try {
			final String classname = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(classname);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final UIDefaults defaults = UIManager.getDefaults();

		defaults.put(
							"PopupMenu.border",
							new BorderUIResource.EtchedBorderUIResource(
												EtchedBorder.RAISED, defaults.getColor("controlShadow"),
												defaults.getColor("controlLtHighlight")
							)
							);

		MatteBorder matteborder = new MatteBorder(1, 1, 1, 1, Color.black);
		EmptyBorder emptyborder = new MatteBorder(2, 2, 2, 2, defaults.getColor("control"));
		BorderUIResource.CompoundBorderUIResource compBorder = new BorderUIResource.CompoundBorderUIResource(emptyborder, matteborder);
		BorderUIResource.EmptyBorderUIResource emptyBorderUI = new BorderUIResource.EmptyBorderUIResource(0, 0, 0, 0);
		defaults.put("SplitPane.border", emptyBorderUI);
		defaults.put("Table.scrollPaneBorder", emptyBorderUI);
		defaults.put("ComboBox.border", compBorder);
		defaults.put("TextField.border", compBorder);
		defaults.put("TextArea.border", compBorder);
		defaults.put("CheckBox.border", compBorder);
		defaults.put("ScrollPane.border", emptyBorderUI);

	}

}
