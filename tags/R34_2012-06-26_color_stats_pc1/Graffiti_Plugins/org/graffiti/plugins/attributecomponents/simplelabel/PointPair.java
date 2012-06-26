package org.graffiti.plugins.attributecomponents.simplelabel;

import java.awt.geom.Point2D;

/**
 * DOCUMENT ME!
 * 
 * @author $Author: klukas $
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:03:40 $
 */
public class PointPair {
	/** DOCUMENT ME! */
	private Point2D fst = null;
	
	/** DOCUMENT ME! */
	private Point2D snd = null;
	
	/**
	 * Creates a new PointPair object.
	 */
	public PointPair() {
	}
	
	/**
	 * Creates a new PointPair object.
	 * 
	 * @param f
	 *           DOCUMENT ME!
	 * @param s
	 *           DOCUMENT ME!
	 */
	PointPair(Point2D f, Point2D s) {
		fst = f;
		snd = s;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param f
	 *           DOCUMENT ME!
	 */
	public void setFst(Point2D f) {
		fst = f;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Point2D getFst() {
		return fst;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param s
	 *           DOCUMENT ME!
	 */
	public void setSnd(Point2D s) {
		snd = s;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Point2D getSnd() {
		return snd;
	}
}