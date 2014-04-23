/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Mar 17, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.viewcomponents;

import java.awt.Point;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import org.ErrorMsg;
import org.HelperClass;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graphics.EdgeLabelPositionAttribute;
import org.graffiti.plugin.view.GraphElementShape;
import org.graffiti.plugins.attributecomponents.simplelabel.PointPair;
import org.graffiti.util.Pair;

/**
 * @author klukas
 */
public class EdgeComponentHelper implements HelperClass {
	public static void updatePositionForEdgeMapping(Attribute aaa, GraphElementShape geShape,
						String attributeName,
						String attributeFolder,
						double flatness,
						int width, int height, Point loc) {
		String positionAttributePathAndName = attributeFolder + Attribute.SEPARATOR + attributeName;
		
		EdgeLabelPositionAttribute posAttr = null;
		try {
			CollectionAttribute ca = (CollectionAttribute) aaa.getAttributable().getAttribute(positionAttributePathAndName);
			if (ca != null && (ca instanceof EdgeLabelPositionAttribute)) {
				posAttr = (EdgeLabelPositionAttribute) ca;
			} else {
				posAttr = new EdgeLabelPositionAttribute(attributeName);
				posAttr.setCollection(ca.getCollection());
				CollectionAttribute parent = ca.getParent();
				parent.remove(ca);
				parent.add(posAttr, false);
			}
		} catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
		}
		if (posAttr == null) {
			posAttr = new EdgeLabelPositionAttribute(attributeName);
			try {
				aaa.getAttributable().addAttribute(posAttr, attributeFolder);
			} catch (AttributeNotFoundException err) {
				return;
			}
		}
		
		// Edge edge = (Edge) ge;
		// EdgeGraphicAttribute edgeAttr = (EdgeGraphicAttribute) edge.getAttribute(GRAPHICS);
		Point2D labelLoc = null;
		
		if (posAttr.getAlignSegment() <= 0) {
			// calc pos rel to whole edge
			PathIterator pi = geShape.getPathIterator(null, flatness);
			double dist = (iterateTill(pi, null)).getX();
			
			pi = geShape.getPathIterator(null, flatness);
			labelLoc = iterateTill(pi, new Double(posAttr.getRelAlign()
								* dist));
		} else {
			// calc pos rel to spec seg
			PathIterator pi = geShape.getPathIterator(null);
			
			// fst = sum of length of first (alignSegment-1) segments
			// snd = (length of segment number alignSegment)
			PointPair segPos = calculateSegPos(pi, posAttr.getAlignSegment());
			
			if (segPos == null) {
				pi = geShape.getPathIterator(null, flatness);
				
				double dist = (iterateTill(pi, null)).getX();
				
				pi = geShape.getPathIterator(null, flatness);
				labelLoc = iterateTill(pi, new Double(posAttr.getRelAlign()
									* dist));
			} else {
				pi = geShape.getPathIterator(null, flatness);
				
				Pair dists = calculateDists(pi, segPos.getFst(), segPos.getSnd());
				
				// move along path till correct pos
				pi = geShape.getPathIterator(null, flatness);
				labelLoc = iterateTill(pi, new Double((Double) dists.getFst()
									+ (posAttr.getRelAlign() * (Double) dists.getSnd())));
			}
		}
		loc.setLocation(labelLoc.getX() - (width / 2.0d)
							+ posAttr.getAbsHor(), labelLoc.getY() - (height / 2.0d)
							+ posAttr.getAbsVert());
	}
	
	/**
	 * Calculates a pair of two values: fst = sum of length of first (seg-1)
	 * segments snd = length of segment number seg
	 * 
	 * @param pi
	 *           DOCUMENT ME!
	 * @param segStartPos
	 *           DOCUMENT ME!
	 * @param segEndPos
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected static Pair calculateDists(PathIterator pi, Point2D segStartPos,
						Point2D segEndPos) {
		double[] seg = new double[6];
		
		double dist = 0;
		
		// fst
		double firstdist = 0;
		
		// snd
		double segnrdist = 0;
		double diffdist = 0;
		double lastx = 0;
		double lasty = 0;
		double newx = 0;
		double newy = 0;
		int type;
		int segcnt = 0;
		boolean haveFound = false;
		boolean foundStart = false;
		
		try {
			type = pi.currentSegment(seg);
			lastx = seg[0];
			lasty = seg[1];
			
			while (!pi.isDone() && !haveFound) {
				segcnt++;
				pi.next();
				type = pi.currentSegment(seg);
				
				switch (type) {
					case java.awt.geom.PathIterator.SEG_MOVETO:

						if (!pi.isDone()) {
							diffdist = Point2D.distance(lastx, lasty, seg[0], seg[1]);
							firstdist = dist;
							dist += diffdist;
							newx = seg[0];
							newy = seg[1];
							
							if (!foundStart
												&& ((lastx - segStartPos.getX()) <= Double.MIN_VALUE)
												&& ((lasty - segStartPos.getY()) <= Double.MIN_VALUE)) {
								foundStart = true;
								segnrdist = 0;
							} else
								if (foundStart) {
									segnrdist += diffdist;
								}
							
							if (((newx - segEndPos.getX()) <= Double.MIN_VALUE)
												&& ((newy - segEndPos.getY()) <= Double.MIN_VALUE)) {
								haveFound = true;
							}
						}
						
						break;
					
					case java.awt.geom.PathIterator.SEG_LINETO:
						newx = seg[0];
						newy = seg[1];
						diffdist = Point2D.distance(lastx, lasty, newx, newy);
						
						// System.out.println("diffStart = (" + (lastx-segStartPos.getX()) + ", " + (lasty-segStartPos.getY()) + ")");
						if (!foundStart
											&& (Math.abs(lastx - segStartPos.getX()) <= Double.MIN_VALUE)
											&& (Math.abs(lasty - segStartPos.getY()) <= Double.MIN_VALUE)) {
							foundStart = true;
							
							// System.out.println("found start");
							firstdist = dist;
							segnrdist = 0;
						}
						
						dist += diffdist;
						
						if (foundStart) {
							segnrdist += diffdist;
						}
						
						// System.out.println("diffEnd = (" + (newx-segStartPos.getX()) + ", " + (newy-segStartPos.getY()) + ")");
						if ((Math.abs(newx - segEndPos.getX()) <= Double.MIN_VALUE)
											&& (Math.abs(newy - segEndPos.getY()) <= Double.MIN_VALUE)) {
							// assert !foundStart :
							haveFound = true;
							
							// System.out.println("found end");
						}
						
						break;
				}
				
				lastx = newx;
				lasty = newy;
			}
		} catch (java.util.NoSuchElementException e) {
		}
		
		// System.out.println("returning " + firstdist + "    " + segnrdist);
		return new Pair(firstdist, segnrdist);
	}
	
	protected static PointPair calculateSegPos(PathIterator pi, int segnr) {
		// assert segnr>=0 :
		double[] seg = new double[6];
		
		// double firstx = 0;
		// double firsty = 0;
		double lastx = 0;
		double lasty = 0;
		
		double newx = 0;
		double newy = 0;
		
		int type;
		int segcnt = 0;
		
		PointPair pp = new PointPair();
		
		try {
			type = pi.currentSegment(seg);
			lastx = seg[0];
			lasty = seg[1];
			
			// firstx = lastx;
			// firsty = lasty;
			while (!pi.isDone() && (segcnt < segnr)) {
				segcnt++;
				pi.next();
				type = pi.currentSegment(seg);
				
				switch (type) {
					case java.awt.geom.PathIterator.SEG_MOVETO:

						if (!pi.isDone()) {
							newx = seg[0];
							newy = seg[1];
						} else {
							return null;
						}
						
						break;
					
					case java.awt.geom.PathIterator.SEG_LINETO:
						newx = seg[0];
						newy = seg[1];
						
						break;
					
					case java.awt.geom.PathIterator.SEG_QUADTO:
						newx = seg[2];
						newy = seg[3];
						
						break;
					
					case java.awt.geom.PathIterator.SEG_CUBICTO:
						newx = seg[4];
						newy = seg[5];
						
						break;
				}
				
				// System.out.println("now found segnr=" + segcnt + "; at (" + newx + ", " + newy + ")");
				if (segcnt == segnr) {
					// System.out.println("found segstartpospoint at (" + lastx + ", " + lasty + ")");
					pp.setFst(new Point2D.Double(lastx, lasty));
					
					// System.out.println("found segendpospoint at (" + newx + ", " + newy + ")");
					pp.setSnd(new Point2D.Double(newx, newy));
				}
				
				lastx = newx;
				lasty = newy;
			}
		} catch (java.util.NoSuchElementException e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		// pp.setSnd(new Point2D.Double(lastx, lasty));
		if (segcnt < segnr) {
			// System.out.println("segnr out of bounds");
			pp = null;
		}
		
		return pp;
	}
	
	/**
	 * If d == null then calculates length of path given by pi if d is a value
	 * then calculates a position on the path near the distance given by this
	 * parameter, measured from the start.
	 * 
	 * @param pi
	 *           <code>PathIterator</code> describing the path
	 * @param d
	 *           null or distance
	 * @return distance at first component of <code>Point2D</code> or the
	 *         position wanted as <code>point2D</code> .
	 */
	protected static Point2D iterateTill(PathIterator pi, Double d) {
		double[] seg = new double[6];
		double limitDist;
		
		if (d == null) {
			limitDist = Double.POSITIVE_INFINITY;
		} else {
			limitDist = d.doubleValue();
		}
		
		double dist = 0;
		double lastx = 0;
		double lasty = 0;
		int type;
		
		try {
			type = pi.currentSegment(seg);
			lastx = seg[0];
			lasty = seg[1];
			
			while (!pi.isDone() && (dist < limitDist)) {
				pi.next();
				type = pi.currentSegment(seg);
				
				switch (type) {
					case java.awt.geom.PathIterator.SEG_MOVETO:

						if (!pi.isDone()) {
							dist += Point2D.distance(lastx, lasty, seg[0], seg[1]);
							lastx = seg[0];
							lasty = seg[1];
						}
						
						break;
					
					case java.awt.geom.PathIterator.SEG_LINETO:
						dist += Point2D.distance(lastx, lasty, seg[0], seg[1]);
						
						if ((d != null) && (dist >= limitDist)) {
							// System.out.println(dist +"    "+ limitDist);
							double diffx = seg[0] - lastx;
							double diffy = seg[1] - lasty;
							double diffsqr = Math.sqrt((diffx * diffx) + (diffy * diffy));
							
							// System.out.println("diffx
							// lastx += Math.sqrt(diffsq - diffy*diffy);
							// lasty += Math.sqrt(diffsq - diffx*diffx);
							double factor = (diffsqr - dist + limitDist) / diffsqr;
							lastx += (diffx * factor);
							lasty += (diffy * factor);
						} else {
							lastx = seg[0];
							lasty = seg[1];
						}
						
						break;
					
					case java.awt.geom.PathIterator.SEG_QUADTO:

						// unnecessary since this approximation uses only lines
						// System.out.println(" quad");
						dist += Point2D.distance(lastx, lasty, seg[2], seg[3]);
						lastx = seg[2];
						lasty = seg[3];
						
						break;
					
					case java.awt.geom.PathIterator.SEG_CUBICTO:

						// unnecessary since this approximation uses only lines
						// System.out.println(" cube");
						dist += Point2D.distance(lastx, lasty, seg[4], seg[5]);
						lastx = seg[4];
						lasty = seg[5];
						
						break;
				}
			}
		} catch (java.util.NoSuchElementException e) {
		}
		
		if (d == null) {
			return new Point2D.Double(dist, 0);
		} else {
			return new Point2D.Double(lastx, lasty);
		}
	}
}
