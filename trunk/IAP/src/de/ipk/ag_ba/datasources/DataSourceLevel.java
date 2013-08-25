/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public interface DataSourceLevel {
	
	public Collection<DataSourceLevel> getSubLevels();
	
	public Collection<ExperimentReference> getExperiments();
	
	public Collection<PathwayWebLinkItem> getPathways();
	
	public NavigationImage getIconInactive();
	
	public NavigationImage getIconActive();
	
	public String getName();
	
	public ArrayList<Book> getReferenceInfos();
	
	public void setDescription(String description);
	
	public String getDescription();
	
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception;
	
	public Collection<NavigationButton> getAdditionalEntitiesShownAtEndOfList(NavigationButton src) throws Exception;
}
