/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 18, 2009 by Christian Klukas
 */
package org.graffiti.editor;

import org.graffiti.plugin.view.View;

/**
 * @author klukas
 */
public interface ConfigureViewAction extends Runnable {
	
	public void storeView(View v);
	
}
