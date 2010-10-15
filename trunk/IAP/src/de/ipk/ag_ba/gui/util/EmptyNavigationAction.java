/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public class EmptyNavigationAction extends AbstractNavigationAction {

	private final String image;
	private final String activeImage;
	private final ArrayList<NavigationGraphicalEntity> nelist;
	private final String name;
	private NavigationGraphicalEntity src;
	private boolean invisibleWhenActive = false;

	public EmptyNavigationAction(String name, String tooltip, String image, String activeImage) {
		super(tooltip);
		this.name = name;
		this.image = image;
		this.activeImage = activeImage;
		nelist = new ArrayList<NavigationGraphicalEntity>();
	}

	@Override
	public void addAdditionalEntity(NavigationGraphicalEntity ne) {
		nelist.add(ne);
	}

	@Override
	public String getDefaultImage() {
		return image;
	}

	@Override
	public String getDefaultNavigationImage() {
		return activeImage;
	}

	@Override
	public String getDefaultTitle() {
		return name;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return nelist;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> result = new ArrayList<NavigationGraphicalEntity>(currentSet);
		if (!invisibleWhenActive)
			result.add(src);
		return result;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		this.src = src;
	}

	public void setInvisibleWhenActive(boolean active) {
		this.invisibleWhenActive = active;
	}
}
