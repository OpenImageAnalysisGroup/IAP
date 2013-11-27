/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.io.File;

import org.HelperClass;
import org.OpenFileDialogService;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class OpenMageFileDialogService implements HelperClass {
	public static File getXMLfile() {
		return OpenFileDialogService.getFile(new String[] { ".XML" }, "MAGE-ML File (*.xml)");
	}
}
