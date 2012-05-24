/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 02.11.2006 by Christian Klukas
 */
package org;

import java.awt.event.ActionEvent;
import java.io.File;

public interface MultipleFileLoader {
	public void loadGraphInBackground(final File[] files, ActionEvent ae);
}
