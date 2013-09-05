/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 22, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands;

/**
 * @author klukas
 */
public enum DeletionCommand {
	TRASH("Move to Trash", "img/ext/trash-delete.png"),
	UNTRASH("Put Back", "img/ext/trash-undelete.png"),
	DELETE("Delete", "img/ext/edit-delete.png"),
	EMPTY_TRASH_DELETE_ALL_TRASHED_IN_LIST("Empty Trash", "img/ext/trash-delete-all2.png"),
	TRASH_GROUP_OF_EXPERIMENTS("Move ALL to Trash", "img/ext/trash-delete.png"),
	UNTRASH_ALL("Put Back All", "img/ext/trash-undelete.png");
	
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
