/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of the Tiny Look and Feel *
 * Copyright 2003 - 2008 Hans Bickel *
 * *
 * For licensing information and credits, please refer to the *
 * comment in file de.muntjak.tinylookandfeel.TinyLookAndFeel *
 * *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package de.muntjak.tinylookandfeel;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;

/**
 * TinyTextAreaUI
 * 
 * @version 1.0
 * @author Hans Bickel
 */
public class TinyTextAreaUI extends BasicTextAreaUI {
	/**
	 * Method createUI.
	 * 
	 * @param c
	 * @return ComponentUI
	 */
	public static ComponentUI createUI(JComponent c) {
		return new TinyTextAreaUI();
	}
}
