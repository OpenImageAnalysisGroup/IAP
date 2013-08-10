// ==============================================================================
//
// LabelComponent.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: LabelComponent.java,v 1.3 2012-11-07 14:42:20 klukas Exp $

package org.graffiti.plugins.attributecomponents.simplelabel;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.LabelFrameSetting;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.EdgeLabelPositionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.graphics.NodeLabelPositionAttribute;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.view.NodeShape;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.util.Pair;

/**
 * This component represents a label for a node or an edge.
 * 
 * @version $Revision: 1.3 $
 */
public class LabelComponent extends AbstractAttributeComponent implements
		GraphicAttributeConstants {
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Flatness value used for the <code>PathIterator</code> used to place
	 * labels.
	 */
	protected final double flatness = 1.0d;
	
	/** Standard width of JTextField. */
	protected final int DEFAULT_WIDTH = 20;
	
	/** The <code>JLabel</code> that represents the label text. */
	protected ViewLabel label;
	
	/** The <code>LabelAttribute</code> that is displayed via this component. */
	protected LabelAttribute labelAttr;
	
	protected boolean dropShadow = false;
	protected LabelFrameSetting frame = LabelFrameSetting.NO_FRAME;
	
	protected int offx = 0;
	protected int offy = 0;
	
	protected int corrX = 0;
	
	Point loc = new Point();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>LabelComponent</code>
	 */
	public LabelComponent() {
		super();
		this.shift = new Point();
	}
	
	// ~ Methods ================================================================
	
	@Override
	public void highlight(boolean value, MouseEvent e) {
		super.highlight(value, e);
		label.highlight(value);
	}
	
	/**
	 * obvious
	 * 
	 * @param attr
	 */
	@Override
	public void setAttribute(Attribute attr) {
		this.attr = attr;
		this.labelAttr = (LabelAttribute) attr;
	}
	
	/**
	 * Called when an attribute of the attribute represented by this component
	 * has changed.
	 * 
	 * @param attr
	 *           the attribute that has triggered the event.
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	public void attributeChanged(Attribute attr) {
		if (attr.getPath().startsWith(Attribute.SEPARATOR + GRAPHICS + Attribute.SEPARATOR + COORDINATE)) {
			setLocation((int) (loc.getX() + shift.getX()), (int) (loc.getY() + shift.getY())); // -1
			return;
		} else
			recreate();
		repaint();
	}
	
	/**
	 * Used when the shape changed in the datastructure. Makes the painter
	 * create a new shape.
	 * 
	 * @throws ShapeNotFoundException
	 *            DOCUMENT ME!
	 */
	@Override
	public void recreate() {
		// System.out.println("recreating Label " + this);
		removeAll();
		if (labelAttr == null)
			ErrorMsg.addErrorMessage("LabelComponent: labelAttr == null!");
		String labelText = "";
		try {
			labelText = labelAttr.getLabel();
			if (labelText != null && labelText.indexOf("TITLE:") == 0)
				labelText = labelText.substring("TITLE:".length()).toUpperCase();
		} catch (Exception e) {
			return;
		}
		String align = ""; // labelAttr.getAlignmentText();
		
		frame = labelAttr.getLabelFrameSetting();
		
		double strokeWidth = 1d;
		
		Color borderColor = Color.BLACK;
		
		try {
			if (attr.getAttributable() instanceof Node) {
				strokeWidth = attr.getAttributable().getDouble("graphics.frameThickness");
				String al = labelAttr.getAlignment();
				if (al != null && al.length() == 3 && al.startsWith("b"))
					borderColor = AttributeHelper.getOutlineColor(attr.getAttributable());
			}
			if (attr.getAttributable() instanceof Edge) {
				strokeWidth = attr.getAttributable().getDouble("graphics.frameThickness");
				borderColor = AttributeHelper.getOutlineColor(attr.getAttributable());
			}
			
		} catch (Exception err) {
			//
		}
		
		dropShadow = labelAttr.getUseDropShadow();
		
		dropShadow = false;
		
		label = new ViewLabel(labelText, frame, strokeWidth, false, dropShadow, borderColor);
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		if (align.indexOf("left") >= 0) {
			label.setHorizontalAlignment(SwingConstants.LEFT);
		} else
			if (align.indexOf("center") >= 0) {
				label.setHorizontalAlignment(SwingConstants.CENTER);
				if (labelText.startsWith("<html>"))
					labelText = StringManipulationTools.stringReplace(labelText, "<html>", "<html><center>");
			} else
				if (align.indexOf("right") >= 0) {
					label.setHorizontalAlignment(SwingConstants.RIGHT);
					if (labelText.startsWith("<html>"))
						labelText = StringManipulationTools.stringReplace(labelText, "<html>", "<html><p align=\"right\">");
				}
		
		setLabelSettings(label, labelAttr.getTextcolor());
		
		label.setShow(labelAttr.getFontStyle().contains("mouse"));
		
		setLayout(new TableLayout(
				new double[][] {
						new double[] { TableLayoutConstants.PREFERRED },
						new double[] { TableLayoutConstants.PREFERRED } }));
		
		offx = 0;
		offy = 0;
		
		updateBorderOrShadow(strokeWidth);
		
		adjustComponentSize();
		
		add(label, "0,0");
		
		synchronized (getTreeLock()) {
			if (isShowing())
				validate();
			else
				validate();
		}
	}
	
	private void updateBorderOrShadow(double strokeWidth) {
		if (dropShadow) {
			offx += labelAttr.getShadowOffX();
			offy += labelAttr.getShadowOffY();
			Color shadowColor = labelAttr.getShadowTextColor();
			label.setShadowColor(shadowColor);
			label.setShadowOffset(offx, offy);
		} else {
			if (frame != LabelFrameSetting.NO_FRAME) {
				label.setOpaque(false);
				label.setBackground(Color.WHITE);
				label.setBorder(getBoxBorder(0, 0, 0, 0, frame, strokeWidth, true));
			}
		}
	}
	
	private Border getBoxBorder(final int xo, final int yo, final int wo, final int ho,
			final LabelFrameSetting frame, final double strokeWidth, final boolean fill) {
		Border result = new Border() {
			
			boolean empty = false;
			
			@Override
			public Insets getBorderInsets(Component c) {
				if (frame == LabelFrameSetting.ELLIPSE || frame == LabelFrameSetting.CIRCLE
						|| frame == LabelFrameSetting.CIRCLE_FILLED || frame == LabelFrameSetting.CIRCLE_HALF_FILLED) {
					ViewLabel vl = (ViewLabel) c;
					JLabel ll = new JLabel(vl.getText());
					ll.setFont(vl.getFont());
					int prefW = ll.getPreferredSize().width;
					int prefH = ll.getPreferredSize().height;
					int sw = (int) Math.ceil(getEllipseWidthFromRectangle(prefW, prefH) / 4);
					int sh = (int) Math.ceil(getEllipseHeightFromRectangle(prefW, prefH) / 4);
					if (vl.getText().length() <= 0)
						empty = true;
					if (frame == LabelFrameSetting.CIRCLE || frame == LabelFrameSetting.CIRCLE_FILLED || frame == LabelFrameSetting.CIRCLE_HALF_FILLED) {
						sh += Math.abs(prefW - prefH) / 2;
						sw += Math.abs(prefW - prefH) / 2;
						return new Insets(xo + sw, yo + sh, wo + sw, ho + sh);
					} else
						return new Insets(1 + xo + sw, 3 + yo + sh, 1 + wo + sw, 3 + ho + sh);
				} else
					return new Insets(1 + xo, 3 + yo, 1 + wo, 3 + ho);
			}
			
			private double getEllipseHeightFromRectangle(double width, double height) {
				double a = width / 2d;
				double b = height / 2d;
				double c = Math.sqrt(a * a + b * b);
				Double c1 = c * 2 / 3;
				return c1 / 2;
			}
			
			private double getEllipseWidthFromRectangle(double width, double height) {
				double a = width / 2d;
				double b = height / 2d;
				double c = Math.sqrt(a * a + b * b);
				Double c2 = c * 3 / 2;
				return c2 / 2;
			}
			
			@Override
			public boolean isBorderOpaque() {
				return false;
			}
			
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				if (empty)
					return;
				// paintRectangleOrOval(frame, g, x, y, width, height, false, strokeWidth);
			}
			
		};
		return result;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
	
	private static final HashMap<TextAttribute, Object> fontAttributes = getFontAttributes();
	
	private void setLabelSettings(JLabel label, Color c) {
		label.setForeground(c);
		String fontName = labelAttr.getFontName();
		int fontStyleInt = labelAttr.getFontStyleJava();
		int fontSize = labelAttr.getFontSize();
		
		setCachedFont(label, fontName, fontStyleInt, fontSize);
		label.setOpaque(false);
		label.setSize((int) label.getPreferredSize().getWidth(), (int) label
				.getPreferredSize().getHeight());
	}
	
	private static final HashMap<String, Font> knownFontSettings = new HashMap<String, Font>();
	
	private void setCachedFont(JLabel label, String fontName, int fontStyleInt,
			int fontSize) {
		
		String fontSettings = fontName + "/" + fontStyleInt + "/" + fontSize;
		Font f = knownFontSettings.get(fontSettings);
		if (f == null) {
			f = new Font(fontName, fontStyleInt, fontSize);
			f = f.deriveFont(fontAttributes);
			knownFontSettings.put(fontSettings, f);
		}
		label.setFont(f);
	}
	
	private static HashMap<TextAttribute, Object> getFontAttributes() {
		HashMap<TextAttribute, Object> result = new HashMap<TextAttribute, Object>();
		try {
			// if (!AttributeHelper.macOSrunning()) {
			setFontAttributes(result);
			// }
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	private static void setFontAttributes(HashMap<TextAttribute, Object> result) {
		result.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
		result.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
	}
	
	@Override
	public void adjustComponentSize() {
		try {
			int h = label.getPreferredSize().height + 1;
			int w = label.getPreferredSize().width + 1;
			// System.out.println(h+" "+w+" "+this.hashCode()+" "+frame);
			// if (dropShadow) {
			// h+=offx*2;
			// w+=offy*2;
			// }
			setSize(w, h);
			updateLabelPosition();
			labelAttr.setLastComponentWidth(w);
			labelAttr.setLastComponentHeight(h);
			labelAttr.setLastLabel(label);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
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
	protected Pair calculateDists(PathIterator pi, Point2D segStartPos,
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
	
	/**
	 * Using the information from the associated <code>LabelAttribute</code> and the shape and position of the <code>GraphElement</code> to
	 * calculate and set the position of the label.
	 */
	public void updateLabelPosition() {
		// calculate position of label
		double labelWidth = this.getWidth();
		double labelHeight = this.getHeight();
		// System.out.println("Update Label Position ["+labelWidth+"/"+labelHeight+"]");
		
		String align = labelAttr.getAlignment();
		
		if (align != null) {
			if (align.equals(AlignmentSetting.HIDDEN.toGMLstring()))
				setVisible(false);
			else
				setVisible(true);
		}
		
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		
		if (ge instanceof Node) {
			double sizeX = geShape.getBounds2D().getWidth();
			double sizeY = geShape.getBounds2D().getHeight();
			
			if (geShape instanceof NodeShape) {
				sizeX += ((NodeShape) geShape).shapeWidthCorrection();
				sizeY += ((NodeShape) geShape).shapeHeightCorrection();
			}
			
			double borderWidth = AttributeHelper.getFrameThickNess(ge);
			
			// calculate position relative to this component ...
			double coordX = 0; // top left
			double coordY = 0; // bottom right
			double centerX = (sizeX / 2d);
			double centerY = sizeY / 2d;
			
			if (geShape.getXexcess() > 0) {
				coordX += geShape.getXexcess();
				centerX = (sizeX / 2d);
			}
			if (geShape.getYexcess() > 0) {
				coordY += geShape.getYexcess();
				centerY = (sizeY / 2d + geShape.getYexcess());
			}
			
			align = processAutoAlignment(align);
			label.setDefaultTextPositioning();
			double cx = coordX + centerX - (labelWidth / 2.0d);
			double cy = centerY - (labelHeight / 2.0d);
			double belowy = coordY + sizeY + LABEL_DISTANCE;
			double rightx = coordX + sizeX + LABEL_DISTANCE;
			double leftx = coordX - labelWidth - LABEL_DISTANCE;
			double abovey = coordY - LABEL_DISTANCE - labelHeight;
			double insrx = coordX + sizeX - labelWidth - borderWidth / 2d;
			double inslx = coordX - +borderWidth / 2d;
			double insbotty = (coordY + sizeY) - labelHeight - LABEL_DISTANCE - borderWidth;
			double bordboty = coordY + sizeY - labelHeight / 2d - borderWidth / 2d;
			double bordercx = coordX + sizeX / 2d - (labelWidth / 2d);
			double borderlx = bordercx - sizeX * (1d / 2d - 1d / 4d);
			double borderrx = bordercx + sizeX * (1d / 2d - 1d / 4d);
			double bortopy = coordY - labelHeight / 2d + borderWidth / 2d;
			if (CENTERED.equals(align)) {
				loc.setLocation(cx, cy);
			} else {
				if (BELOW.equals(align)) {
					loc.setLocation(cx, belowy);
				} else {
					if (BELOWRIGHT.equals(align)) {
						loc.setLocation(rightx, belowy);
					} else {
						if (BELOWLEFT.equals(align)) {
							loc.setLocation(leftx, belowy);
						} else {
							if (BORDER_BOTTOM_CENTER.equals(align)) {
								loc.setLocation(bordercx, bordboty);
							} else {
								if (BORDER_BOTTOM_LEFT.equals(align)) {
									loc.setLocation(borderlx, bordboty);
								} else {
									if (BORDER_BOTTOM_RIGHT.equals(align)) {
										loc.setLocation(borderrx, bordboty);
									} else {
										if (BORDER_TOP_CENTER.equals(align)) {
											loc.setLocation(bordercx, bortopy);
										} else
											if (BORDER_TOP_LEFT.equals(align)) {
												loc.setLocation(borderlx, bortopy);
											} else
												if (BORDER_TOP_RIGHT.equals(align)) {
													loc.setLocation(borderrx, bortopy);
												} else {
													if (INSIDEBOTTOM.equals(align)) {
														loc.setLocation(cx, insbotty);
													} else {
														if (ABOVE.equals(align)) {
															loc.setLocation(cx, abovey);
														} else
															if (ABOVELEFT.equals(align)) {
																loc.setLocation(leftx, abovey);
															} else
																if (LEFT.equals(align)) {
																	loc.setLocation(leftx, cy);
																} else
																	if (BORDER_LEFT_TOP.equals(align)) {
																		loc.setLocation(coordX - labelWidth / 2d + borderWidth / 2d, centerY - (labelHeight / 2.0d) - sizeY
																				* (1d / 2d - 1d / 4d));
																	} else
																		if (BORDER_LEFT_CENTER.equals(align)) {
																			loc.setLocation(coordX - labelWidth / 2d + borderWidth / 2d, cy);
																		} else
																			if (BORDER_LEFT_BOTTOM.equals(align)) {
																				loc.setLocation(coordX - labelWidth / 2d + borderWidth / 2d, cy + sizeY
																						* (1d / 2d - 1d / 4d));
																			} else
																				if (BORDER_RIGHT_TOP.equals(align)) {
																					loc.setLocation(coordX + sizeX - labelWidth / 2d - borderWidth / 2d, centerY
																							- (labelHeight / 2.0d) - sizeY * (1d / 2d - 1d / 4d));
																				} else
																					if (BORDER_RIGHT_CENTER.equals(align)) {
																						loc.setLocation(coordX + sizeX - labelWidth / 2d - borderWidth / 2d, cy);
																					} else
																						if (BORDER_RIGHT_BOTTOM.equals(align)) {
																							loc.setLocation(coordX + sizeX - labelWidth / 2d - borderWidth / 2d, cy + sizeY
																									* (1d / 2d - 1d / 4d));
																						} else
																							if (ABOVERIGHT.equals(align)) {
																								loc.setLocation(rightx, abovey);
																							} else
																								if (RIGHT.equals(align)) {
																									loc.setLocation(rightx, cy);
																								} else
																									if (INSIDETOPLEFT.equals(align)) {
																										loc.setLocation(coordX + borderWidth / 2d, coordY
																												+ LABEL_DISTANCE + borderWidth);
																									} else {
																										if (INSIDETOPRIGHT.equals(align)) {
																											loc.setLocation(insrx, coordY
																													+ LABEL_DISTANCE + borderWidth);
																										} else {
																											if (INSIDELEFT.equals(align)) {
																												loc.setLocation(inslx, cy);
																											} else
																												if (INSIDERIGHT.equals(align)) {
																													loc.setLocation(insrx, cy);
																												} else
																													if (INSIDEBOTTOMLEFT.equals(align)) {
																														loc.setLocation(inslx,
																																insbotty);
																													} else
																														if (INSIDEBOTTOMRIGHT.equals(align)) {
																															loc.setLocation(insrx, insbotty);
																														} else
																															if (INSIDETOP.equals(align)) {
																																loc.setLocation(cx, coordY
																																		+ LABEL_DISTANCE
																																		+ borderWidth);
																															} else {
																																// no supported alignment constant: try to parse 'align' as x-y
																																// position
																																String[] posarr = align.split(";");
																																Vector2d pos = null;
																																if (posarr.length == 2) {
																																	try {
																																		pos = new Vector2d(Double.parseDouble(posarr[0]),
																																				Double.parseDouble(posarr[1]));
																																	} catch (Exception err) {
																																		pos = null;
																																	}
																																}
																																if (pos != null) {
																																	loc.setLocation(pos.x - (labelWidth / 2.0d), pos.y
																																			- (labelHeight / 2.0d));
																																} else {
																																	// no supported alignment constant: use relative positions
																																	try {
																																		NodeLabelPositionAttribute posAttr = ((NodeLabelAttribute) this.labelAttr)
																																				.getPosition();
																																		
																																		if (posAttr == null) {
																																			posAttr = new NodeLabelPositionAttribute(POSITION);
																																		}
																																		
																																		loc
																																				.setLocation(
																																						centerX
																																								+ ((posAttr.getRelHor() * sizeX) / 2d)
																																								+ ((posAttr.getLocalAlign() - 1d) * (labelWidth / 2d)),
																																						(centerY + ((posAttr.getRelVert() * sizeY) / 2d))
																																								- (labelHeight / 2d));
																																	} catch (Exception err) {
																																		loc.setLocation(centerX - (labelWidth / 2.0d), cy);
																																	}
																																}
																															}
																										}
																									}
													}
												}
									}
								}
							}
						}
					}
				}
			}
		} else {
			// System.out.println("recalculating edgelabel " + this);
			// label is an edgelabel
			EdgeLabelPositionAttribute posAttr = ((EdgeLabelAttribute) this.labelAttr)
					.getPosition();
			
			if (posAttr == null) {
				posAttr = new EdgeLabelPositionAttribute(POSITION);
			}
			
			// Edge edge = (Edge) ge;
			// EdgeGraphicAttribute edgeAttr = (EdgeGraphicAttribute) edge.getAttribute(GRAPHICS);
			Point2D labelLoc = null;
			
			if (posAttr.getAlignSegment() <= 0) {
				// calc pos rel to whole edge
				PathIterator pi = geShape.getPathIterator(null, flatness);
				double dist = (this.iterateTill(pi, null)).getX();
				
				pi = geShape.getPathIterator(null, flatness);
				labelLoc = this.iterateTill(pi, new Double(posAttr.getRelAlign()
						* dist));
			} else {
				// calc pos rel to spec seg
				PathIterator pi = geShape.getPathIterator(null);
				
				// fst = sum of length of first (alignSegment-1) segments
				// snd = (length of segment number alignSegment)
				PointPair segPos = calculateSegPos(pi, posAttr.getAlignSegment());
				
				if (segPos == null) {
					pi = geShape.getPathIterator(null, flatness);
					
					double dist = (this.iterateTill(pi, null)).getX();
					
					pi = geShape.getPathIterator(null, flatness);
					labelLoc = this.iterateTill(pi, new Double(posAttr.getRelAlign()
							* dist));
				} else {
					pi = geShape.getPathIterator(null, flatness);
					
					Pair dists = this.calculateDists(pi, segPos.getFst(), segPos
							.getSnd());
					
					// move along path till correct pos
					pi = geShape.getPathIterator(null, flatness);
					labelLoc = this.iterateTill(pi, new Double((Double) dists.getFst()
							+ (posAttr.getRelAlign() * (Double) dists.getSnd())));
				}
			}
			
			loc.setLocation(labelLoc.getX() - (labelWidth / 2.0d)
					+ posAttr.getAbsHor(), labelLoc.getY() - (labelHeight / 2.0d)
					+ posAttr.getAbsVert());
		}
		setLocation((int) (loc.getX() + shift.getX()), (int) (loc.getY() + shift
				.getY()));
		
		// setLocation((int) loc.getX(), (int) loc.getY());
		
		repaint();
	}
	
	private String processAutoAlignment(String alignment) {
		if (alignment.equals(AUTO_OUTSIDE)) {
			Attributable a = getAttribute().getAttributable();
			if (a != null && a instanceof Node) {
				Node n = (Node) a;
				return getBestAutoOutsideSetting(n);
			} else
				return alignment;
		} else
			return alignment;
	}
	
	public static String getBestAutoOutsideSetting(Node n) {
		if (n == null)
			return ABOVE;
		Quadrant lowestQuadrant = getBestQuadrant(n);
		if (lowestQuadrant == Quadrant.LEFT)
			return LEFT;
		if (lowestQuadrant == Quadrant.RIGHT)
			return RIGHT;
		if (lowestQuadrant == Quadrant.TOP)
			return ABOVE;
		if (lowestQuadrant == Quadrant.BOTTOM)
			return BELOW;
		return BELOW;
	}
	
	private static Quadrant getBestQuadrant(Node n) {
		int isBadTop = 0;
		int isBadLeft = 0;
		int isBadRight = 0;
		int isBadBottom = 0;
		for (Node neighbour : n.getNeighbors()) {
			if (getQuadrant(neighbour, n) == Quadrant.TOP)
				isBadTop += 1;
			else
				if (getQuadrant(neighbour, n) == Quadrant.LEFT)
					isBadLeft += 1;
				else
					if (getQuadrant(neighbour, n) == Quadrant.RIGHT)
						isBadRight += 1;
					else
						if (getQuadrant(neighbour, n) == Quadrant.BOTTOM)
							isBadBottom += 1;
		}
		Quadrant lowestQuadrant;
		
		if (isBadTop <= isBadLeft && isBadTop <= isBadRight && isBadTop <= isBadBottom)
			lowestQuadrant = Quadrant.TOP;
		else
			if (isBadLeft <= isBadTop && isBadLeft < isBadRight && isBadTop <= isBadBottom)
				lowestQuadrant = Quadrant.LEFT;
			else
				if (isBadRight <= isBadTop && isBadRight <= isBadLeft && isBadTop <= isBadBottom)
					lowestQuadrant = Quadrant.RIGHT;
				else
					if (isBadBottom <= isBadTop && isBadTop <= isBadLeft && isBadTop <= isBadRight)
						lowestQuadrant = Quadrant.BOTTOM;
					else
						lowestQuadrant = Quadrant.BOTTOM;
		return lowestQuadrant;
	}
	
	private static Quadrant getQuadrant(Node neighbour, Node refNode) {
		Point2D pRef = AttributeHelper.getPosition(refNode);
		Point2D pNei = AttributeHelper.getPosition(neighbour);
		if (pRef.getX() < pNei.getX()) {
			if (Math.abs(pNei.getY() - pRef.getY()) <= Math.abs(pNei.getX() - pRef.getX()))
				return Quadrant.RIGHT;
			else
				if (pNei.getY() >= pRef.getY())
					return Quadrant.BOTTOM;
				else
					return Quadrant.TOP;
		} else {
			if (Math.abs(pNei.getY() - pRef.getY()) <= Math.abs(pNei.getX() - pRef.getX()))
				return Quadrant.LEFT;
			else
				if (pNei.getY() >= pRef.getY())
					return Quadrant.BOTTOM;
				else
					return Quadrant.TOP;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param pi
	 *           DOCUMENT ME!
	 * @param segnr
	 *           DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected PointPair calculateSegPos(PathIterator pi, int segnr) {
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
	protected Point2D iterateTill(PathIterator pi, Double d) {
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
	
	static void paintRectangleOrOval(LabelFrameSetting frame,
			Graphics gg, float x, float y, float width, float height, boolean fill,
			double strokeWidth, Color borderColor, boolean emptyText) {
		
		Graphics2D g2d = (Graphics2D) gg;
		
		// if (strokeWidth>3)
		// strokeWidth = 1d;
		
		g2d.setStroke(new BasicStroke((float) strokeWidth));
		strokeWidth += 1;
		x += strokeWidth / 2d;
		y += strokeWidth / 2d;
		width -= strokeWidth;
		height -= strokeWidth;
		
		Color oc = g2d.getColor();
		if (fill)
			g2d.setColor(Color.WHITE);
		else
			g2d.setColor(borderColor);
		
		if (frame == LabelFrameSetting.RECTANGLE) {
			if (fill)
				g2d.fill(new Rectangle2D.Double(x, y, width, height));
			else
				g2d.draw(new Rectangle2D.Double(x, y, width, height));
		} else
			if (frame == LabelFrameSetting.SIDE_LINES) {
				double off = 0;
				double offH = 0;
				if (emptyText) {
					off = 5;
					offH = 2;
				}
				if (fill)
					g2d.fill(new Rectangle2D.Double(x + off, y, width - off * 2, height - offH));
				else {
					g2d.draw(new Rectangle2D.Double(x + off, y, 0, height - offH));
					g2d.draw(new Rectangle2D.Double(x - off + width, y, 0, height - offH));
				}
			} else
				if (frame == LabelFrameSetting.RECTANGLE_CORNER_CUT) {
					GeneralPath cutrect = new GeneralPath();
					float c = (width < height ? width * 0.25f : height * 0.25f);
					cutrect.moveTo(x, y + c);
					cutrect.lineTo(x + c, y);
					cutrect.lineTo(x + width - c, y);
					cutrect.lineTo(x + width, y + c);
					cutrect.lineTo(x + width, y + height - c);
					cutrect.lineTo(x + width - c, y + height);
					cutrect.lineTo(x + c, y + height);
					cutrect.lineTo(x, y + height - c);
					cutrect.lineTo(x, y + c);
					cutrect.closePath();
					if (fill)
						g2d.fill(cutrect);
					else {
						g2d.draw(cutrect);
						g2d.draw(cutrect);
					}
				} else
					if (frame == LabelFrameSetting.RECTANGLE_ROUNDED) {
						double mwh = (width < height ? width * 0.5 : height * 0.5);
						if (fill)
							g2d.fill(new RoundRectangle2D.Double(x, y, width, height, mwh, mwh));
						else
							g2d.draw(new RoundRectangle2D.Double(x, y, width, height, mwh, mwh));
					} else
						if (frame == LabelFrameSetting.RECTANGLE_BOTTOM_ROUND) {
							GeneralPath gp = new GeneralPath();
							float c = (width < height ? width * 0.5f : height * 0.5f);
							gp.moveTo(x, y);
							gp.lineTo(x + width, y);
							gp.lineTo(x + width, y + height - c);
							// gp.quadTo(x + width, y + height, x + width - c, y + height);
							
							int roundingSimulationSteps = 10;
							double xc = (x + width - c);
							double yc = (y + height - c);
							double singleStep = Math.PI / 2 / roundingSimulationSteps;
							for (int k = 1; k < roundingSimulationSteps; k++) {
								int step = roundingSimulationSteps - k;
								double xp = xc - Math.sin(singleStep * step + Math.PI) * c;
								double yp = yc - Math.cos(singleStep * step + Math.PI) * c;
								gp.lineTo(xp, yp);
							}
							gp.lineTo(x + width - c, y + height);
							
							gp.lineTo(x + c, y + height);
							// gp.quadTo(x, y + height, x, y + height - c);
							
							xc = (x + c);
							yc = (y + height) - c;
							for (int k = 1; k < roundingSimulationSteps; k++) {
								int step = roundingSimulationSteps - k;
								double xp = xc - Math.cos(singleStep * step) * c;
								double yp = yc + Math.sin(singleStep * step) * c;
								gp.lineTo(xp, yp);
							}
							
							gp.lineTo(x, y);
							gp.closePath();
							if (fill)
								g2d.fill(gp);
							else {
								g2d.draw(gp);
								g2d.draw(gp);
							}
						} else
							if (frame == LabelFrameSetting.CAPSULE) {
								double mwh = (width < height ? width : height);
								if (fill)
									g2d.fill(new RoundRectangle2D.Double(x, y, width, height, mwh, mwh));
								else
									g2d.draw(new RoundRectangle2D.Double(x, y, width, height, mwh, mwh));
							} else
								if (frame == LabelFrameSetting.ELLIPSE || frame == LabelFrameSetting.CIRCLE ||
										frame == LabelFrameSetting.CIRCLE_FILLED || frame == LabelFrameSetting.CIRCLE_HALF_FILLED ||
										frame == LabelFrameSetting.PIN) {
									if (frame == LabelFrameSetting.PIN) {
										float minR = width < height ? width : height;
										float offX = (width - minR) / 2f;
										float offY = (height - minR) / 2f;
										g2d.getColor();
										g2d.draw(new Ellipse2D.Double(x + offX, y + offY, minR, minR));
										if (fill) {
											AffineTransform at = g2d.getTransform();
											g2d.fill(new Ellipse2D.Double(x + offX, y + offY, minR, minR));
											g2d.setPaint(borderColor);
											
											float dx = 0;
											float dy = 0;
											AffineTransform o = g2d.getTransform();
											boolean nice = false;
											if (nice) {
												// draw "real pin"
												at.rotate(-Math.PI / 4, -dx + x + offX + minR / 2, -dy + y + offY + minR / 2);
												g2d.setTransform(at);
												
												g2d.fill(new Ellipse2D.Double(-dx + x + offX + minR / 3, -dy + y + offY + minR / 4 - minR / 8, minR / 3, minR / 12));
												g2d.fill(new Ellipse2D.Double(-dx + x + offX + minR / 4, -dy + y + offY + minR / 2 - minR / 8, minR / 2, minR / 8));
												g2d.fill(new Rectangle2D.Double(-dx + x + offX + minR / 2.5, -dy + y + offY + minR / 3.5 - minR / 8, minR / 5, minR / 4));
												GeneralPath triangle = new GeneralPath();
												triangle.moveTo(-dx + minR / 30f + x + offX + minR / 2.5f, -dy + y + offY + minR / 3.5f - minR / 8f + minR / 4f);
												triangle
														.lineTo(-dx - minR / 30f + x + offX + minR / 2.5f + minR / 5f, -dy + y + offY + minR / 3.5f - minR / 8f
																+ minR / 4f);
												triangle.lineTo(-dx + x + offX + minR / 2f, -dy + y + offY + minR / 1.2f);
												triangle.closePath();
												g2d.fill(triangle);
											} else {
												// draw "simplified pin" (two lines)
												g2d.drawLine((int) (x + offX + minR / 8), (int) (y + offY + minR / 1.3), (int) (x + offX + minR * 0.75),
														(int) (y + offY + minR * 0.12));
												g2d.drawLine((int) (x + offX + minR / 2 - 1), (int) (y + offY + minR / 2 - 1), (int) (x + offX + minR * 0.82 + 1), (int) (y
														+ offY + minR * 0.82 + 1));
											}
											g2d.setTransform(o);
										}
									} else
										if (frame == LabelFrameSetting.ELLIPSE) {
											if (!fill)
												g2d.draw(new Ellipse2D.Double(x, y, width, height));
											else
												g2d.fill(new Ellipse2D.Double(x, y, width, height));
										} else {
											double minR = width < height ? width : height;
											double offX = (width - minR) / 2;
											double offY = (height - minR) / 2;
											Color ccc = g2d.getColor();
											int cc = ccc.getRed() + ccc.getGreen() + ccc.getBlue();
											if (fill) {
												g2d.fill(new Ellipse2D.Double(x + offX, y + offY, minR, minR));
												if (frame == LabelFrameSetting.CIRCLE_HALF_FILLED) {
													g2d.setPaint(borderColor);
													g2d.fill(new Arc2D.Double(x + offX, y + offY, minR, minR, -90, 180, Arc2D.PIE));
												}
												if (frame == LabelFrameSetting.CIRCLE_FILLED) {
													g2d.setPaint(borderColor);
													g2d.fill(new Arc2D.Double(x + offX, y + offY, minR, minR, 0, 360, Arc2D.PIE));
												}
											} else {
												if (cc == 255 + 255 + 255) {
													g2d.setColor(Color.black);
													g2d.draw(new Ellipse2D.Double(x + offX, y + offY, minR, minR));
													g2d.setColor(Color.white);
												} else
													g2d.draw(new Ellipse2D.Double(x + offX, y + offY, minR, minR));
											}
										}
								}
		g2d.setColor(oc);
	}
	
	public LabelFrameSetting getLabelFrameSetting() {
		
		return frame;
		
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
