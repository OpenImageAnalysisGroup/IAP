/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 5, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;
import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public abstract class AbstractNavigationAction implements NavigationAction {

	private final ArrayList<NavigationButton> additionalEntities = new ArrayList<NavigationButton>();
	protected BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
						"", "");
	private final String tooltip;

	public AbstractNavigationAction(String tooltip) {
		this.tooltip = tooltip;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.webstart.NavigationAction
	 * #getResultMainPanel()
	 */
	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}

	@Override
	public void addAdditionalEntity(NavigationButton ne) {
		additionalEntities.add(ne);
	}

	public ArrayList<NavigationButton> getAdditionalEntities() {
		return additionalEntities;
	}

	public BackgroundTaskStatusProvider getStatusProvider() {
		return status;
	}

	public String getDefaultTitle() {
		return null;
	}

	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}

	public String getDefaultImage() {
		return null;
	}

	public String getDefaultTooltip() {
		return tooltip;
	}

	@Override
	public boolean getProvidesActions() {
		return true;
	}

	public NavigationImage getImageIcon() {
		return null;
	}
}
