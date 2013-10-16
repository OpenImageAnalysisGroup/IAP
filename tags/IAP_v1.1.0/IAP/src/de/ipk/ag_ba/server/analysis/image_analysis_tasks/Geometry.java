/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 31, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

/**
 * @author klukas
 */
public class Geometry {
	
	private int top;
	private int left;
	private int right;
	private long filled;
	
	public Geometry(int top, int left, int right, long filled) {
		this.setTop(top);
		this.setLeft(left);
		this.setRight(right);
		this.filled = filled;
	}
	
	public void setTop(int top) {
		this.top = top;
	}
	
	public long getFilledPixels() {
		return filled;
	}
	
	public int getTop() {
		return top;
	}
	
	public void setLeft(int left) {
		this.left = left;
	}
	
	public int getLeft() {
		return left;
	}
	
	public void setRight(int right) {
		this.right = right;
	}
	
	public int getRight() {
		return right;
	}
	
}
