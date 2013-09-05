/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.commands.datasource;

import java.util.ArrayList;

/**
 * @author klukas
 */
public class Library {
	
	private final ArrayList<Book> books = new ArrayList<Book>();
	
	public void add(Book book) {
		books.add(book);
	}
	
	public ArrayList<Book> getBooksInFolder(String folder) {
		ArrayList<Book> res = new ArrayList<Book>();
		for (Book book : books) {
			if (book.getFolder().equals(folder))
				res.add(book);
		}
		return res;
	}
}
