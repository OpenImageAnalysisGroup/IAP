/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 8, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author klukas
 */
public class EmptyNavigationAction extends AbstractNavigationAction {
	
	private final String image;
	private final String activeImage;
	private final ArrayList<NavigationButton> nelist;
	private final String name;
	private NavigationButton src;
	private boolean invisibleWhenActive = false;
	private String desc;
	
	public EmptyNavigationAction(String name, String tooltip, String image, String activeImage) {
		super(tooltip);
		this.name = name;
		this.image = image;
		this.activeImage = activeImage;
		nelist = new ArrayList<NavigationButton>();
	}
	
	@Override
	public void addAdditionalEntity(NavigationButton ne) {
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
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return nelist;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>(currentSet);
		if (!invisibleWhenActive)
			result.add(src);
		return result;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	public void setInvisibleWhenActive(boolean active) {
		this.invisibleWhenActive = active;
	}
	
	public void setIntroductionText(String desc) {
		this.desc = desc;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (desc != null)
			return new MainPanelComponent(desc);
		else
			return null;
	}
	
}
