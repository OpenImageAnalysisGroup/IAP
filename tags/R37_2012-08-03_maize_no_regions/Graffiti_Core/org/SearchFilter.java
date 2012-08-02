/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package org;

public interface SearchFilter {
	public boolean accept(GuiRow gr, String searchText);
}
