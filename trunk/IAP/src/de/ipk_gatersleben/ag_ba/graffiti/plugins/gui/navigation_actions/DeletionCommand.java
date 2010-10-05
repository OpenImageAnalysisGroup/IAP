/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 22, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

/**
 * @author klukas
 */
public enum DeletionCommand {
	TRASH("Move to Trash", "img/ext/trash-delete.png"), UNTRASH("Put Back", "img/ext/trash-undelete.png"), DELETE(
			"Delete", "img/ext/edit-delete.png");

	String title, img;

	private DeletionCommand(String title, String img) {
		this.title = title;
		this.img = img;
	}

	@Override
	public String toString() {
		return title;
	}

	public String getImg() {
		return img;
	}
}
