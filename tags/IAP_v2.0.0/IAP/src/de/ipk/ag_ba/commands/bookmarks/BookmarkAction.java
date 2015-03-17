/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 17, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.bookmarks;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.Unicode;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.Bookmark;
import de.ipk.ag_ba.gui.webstart.IAPgui;

/**
 * @author klukas
 */
public class BookmarkAction extends AbstractNavigationAction implements ImageProvider {
	
	private final Bookmark bookmark;
	private final NavigationAction actionHome;
	
	public BookmarkAction(Bookmark bookmark, NavigationAction actionHome) {
		super(pretify(Unicode.STAR + " Bookmark " + Unicode.STAR + "<br>" + bookmark.getTarget()));
		this.bookmark = bookmark;
		this.actionHome = actionHome;
	}
	
	private static String pretify(String target) {
		String res = "<html>" + StringManipulationTools.stringReplace(target, ".", " " + Unicode.ARROW_RIGHT + " ");
		res = StringManipulationTools.stringReplace(res, Unicode.STAR, "*");
		res = StringManipulationTools.stringReplace(res, Unicode.ARROW_RIGHT, "&gt;");
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return bookmark.getTitle();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		IAPgui.navigateTo(bookmark.getTarget(), src, actionHome);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	public BufferedImage getImage() {
		return bookmark.getImage();
	}
	
	public Bookmark getBookmark() {
		return bookmark;
	}
	
	public String getStaticIconId() {
		return bookmark.getStaticIconId();
	}
	
}
