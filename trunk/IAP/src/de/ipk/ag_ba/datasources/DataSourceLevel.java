/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources;

import java.awt.image.BufferedImage;
import java.util.Collection;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public interface DataSourceLevel {

	public Collection<DataSourceLevel> getSubDataSourceLevels();

	public Collection<ExperimentReference> getExperimentsAtThisLevel();

	public Collection<PathwayWebLinkItem> getPathwaysAtThisLevel();

	public BufferedImage getIconForThisLevel();

	public BufferedImage getOpenedIconForThisLevel();

	public String getName();
}
