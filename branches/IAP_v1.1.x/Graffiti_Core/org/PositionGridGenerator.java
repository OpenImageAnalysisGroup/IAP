/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 31.08.2004 by Christian Klukas
 */
package org;

import java.awt.geom.Point2D;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class PositionGridGenerator {
	
	double curX = 0;
	double curY = 0;
	
	double xSpace;
	double ySpace;
	double maxX;
	
	/**
	 * Create a new PositionGridGenerator.
	 * 
	 * @param xSpace
	 *           The step size in X-direction.
	 * @param ySpace
	 *           The step size in Y-direction.
	 * @param maxX
	 *           The maximum X-coordinate, if exceeded, a new row will be started.
	 */
	public PositionGridGenerator(double xSpace, double ySpace, double maxX) {
		this.xSpace = xSpace;
		this.ySpace = ySpace;
		this.maxX = maxX;
		this.curX = xSpace;
		this.curY = ySpace;
	}
	
	public Point2D getNextPosition() {
		curX += xSpace;
		Point2D result = new Point2D.Double(curX, curY);
		
		if (curX > maxX + xSpace) {
			curX = xSpace;
			curY += ySpace;
		}
		
		return result;
	}
	
	public Vector2d getNextPositionVec2d() {
		curX += xSpace;
		Vector2d result = new Vector2d(curX, curY);
		
		if (curX > maxX + xSpace) {
			curX = xSpace;
			curY += ySpace;
		}
		
		return result;
	}
}
