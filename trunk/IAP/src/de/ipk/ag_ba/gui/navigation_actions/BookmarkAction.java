/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Oct 17, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.webstart.AIPgui;
import de.ipk.ag_ba.gui.webstart.Bookmark;

/**
 * @author klukas
 * 
 */
public class BookmarkAction extends AbstractNavigationAction {

	private final Bookmark bookmark;

	public BookmarkAction(Bookmark bookmark) {
		super("Bookmark Navigation: " + bookmark.getTarget());
		this.bookmark = bookmark;
	}

	@Override
	public String getDefaultTitle() {
		return bookmark.getTitle();
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) throws Exception {
		AIPgui.navigateTo(bookmark.getTarget(), src);
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		return null;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return null;
	}

	public BufferedImage getImage() {
		return bookmark.getImage();
	}

	public Bookmark getBookmark() {
		return bookmark;
	}

}
