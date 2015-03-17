/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import info.clearthought.layout.SingleFiledLayout;

import java.awt.Point;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.view.ShapeNotFoundException;
import org.graffiti.plugins.attributecomponents.simplelabel.LabelComponent;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

public class CompoundImageAttributeComponent extends AbstractAttributeComponent
					implements GraphicAttributeConstants {
	private static final long serialVersionUID = 1L;
	
	protected JLabel imageComponent;
	
	double imgWidth = 0;
	double imgHeight = 0;
	
	double offX = 0;
	double offY = 0;
	
	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
		// attr is often a CollectionAttribute,
		// e.g. after pressing apply in the inspector.
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		// (GraphElement) attr.getAttributable();
		
		if (ge instanceof Node) {
			Node n = (Node) ge;
			if (attr instanceof CollectionAttribute) {
				// empty
			} else
				if (attr instanceof CollectionAttribute) {
					if (attr.getPath().equals("")) {
						changeParameters(((CollectionAttribute) attr).getCollection()
											.get(GRAPHICS), n);
					} else
						if (attr.getPath().equals(GRAPHICS)) {
							changeParameters(attr, n);
						} else {
							recreate();
						}
				} else
					if (attr.getId().equals("component")) {
						// System.out.println("\nStep 1 (attr changed): "+AttributeHelper.getSubstanceName(n, "unknown")+" --> "+(String) attr.getValue());
						recreate();
						// setSize(getPreferredSize());
					} else
						if (attr.getPath().startsWith(
											Attribute.SEPARATOR + GRAPHICS + Attribute.SEPARATOR
																+ COORDINATE)) {
							// setLocation(getNewX(n), getNewY(n));
						} else {
							recreate();
						}
		}
	}
	
	@Override
	public void recreate() throws ShapeNotFoundException {
		// System.out.print("recreate.");
		
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		
		setOpaque(false);
		setBackground(null);
		
		if (ge instanceof Node) {
			Node n = (Node) ge;
			
			String position = (String) AttributeHelper.getAttributeValue(n, "image", "image_position",
								GraphicAttributeConstants.AUTO_OUTSIDE, "", true);
			
			Double border = (Double) AttributeHelper.getAttributeValue(n, "image", "image_border", 5d, 5d);
			
			setLayout(new SingleFiledLayout());
			attr.getAttributable();
			imageComponent = CompoundImageAttributeEditor.getCompoundImageComponent(
								imageComponent,
								checkAndChangePath(attr.getValue().toString(), attr),
								false); // true
			if (imageComponent != null && !position.equals(GraphicAttributeConstants.HIDDEN)) {
				add(imageComponent);
				imgWidth = imageComponent.getPreferredSize().width + 2 * border;
				imgHeight = imageComponent.getPreferredSize().height + 2 * border;
				Vector2d nodeSize = AttributeHelper.getSize(n);
				int off = 4;
				boolean labelAndImageAutoOutside = false;
				if (position.equals(GraphicAttributeConstants.AUTO_OUTSIDE)) {
					position = LabelComponent.getBestAutoOutsideSetting(n);
					String lblpos = AttributeHelper.getLabelPosition(getAttribute().getAttributable());
					if (lblpos != null && lblpos.equalsIgnoreCase(GraphicAttributeConstants.AUTO_OUTSIDE)) {
						labelAndImageAutoOutside = true;
						if (position.equals(GraphicAttributeConstants.LEFT))
							position = GraphicAttributeConstants.RIGHT;
						else {
							if (position.equals(GraphicAttributeConstants.RIGHT))
								position = GraphicAttributeConstants.LEFT;
						}
					}
				}
				if (position.equals(GraphicAttributeConstants.CENTERED) || position.equals(GraphicAttributeConstants.CENTERED_FIT)) {
					offX = (nodeSize.x - imgWidth) / 2;
					offY = (nodeSize.y - imgHeight) / 2;
				} else
					if (position.equals(GraphicAttributeConstants.LEFT)) {
						offX = -imgWidth - off;
						offY = (nodeSize.y - imgHeight) / 2;
					} else
						if (position.equals(GraphicAttributeConstants.RIGHT)) {
							offX = nodeSize.x + off;
							offY = (nodeSize.y - imgHeight) / 2;
						} else
							if (position.equals(GraphicAttributeConstants.ABOVE)) {
								offX = (nodeSize.x - imgWidth) / 2;
								offY = -imgHeight - off;
								if (labelAndImageAutoOutside)
									offY -= (Double) AttributeHelper.getAttributeValue(getAttribute().getAttributable(),
														"labelgraphics", "fontSize", new Double(12), new Double(12), false);
							} else
								if (position.equals(GraphicAttributeConstants.BELOW)) {
									offX = (nodeSize.x - imgWidth) / 2;
									offY = nodeSize.y + off;
									if (labelAndImageAutoOutside)
										offY += (Double) AttributeHelper.getAttributeValue(getAttribute().getAttributable(),
															"labelgraphics", "fontSize", new Double(12), new Double(12), false);
								}
			}
			offX += border;
			offY += border;
			if (imageComponent == null) {
				imgWidth = 25;
				imgHeight = 25;
				setSize(0, 0);
			} else
				setSize(imageComponent.getPreferredSize());
			
			updatePosition();
			
			validate();
			repaint();
			
			Vector2d ns = AttributeHelper.getSize(n);
			final Node nn = n;
			boolean centerfitresize = (ns.x != imgWidth + 2 || ns.y != imgHeight + 2) && CompoundImagePositionAttributeEditor.isCenteredFitPosition(n);
			boolean centerresize = (ns.x < imgWidth || ns.y < imgHeight) && CompoundImagePositionAttributeEditor.isCenteredPosition(n);
			if (centerfitresize || centerresize) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						AttributeHelper.setSize(nn, imgWidth + 2, imgHeight + 2);
						AttributeHelper.setShape(nn, RectangleNodeShape.class.getCanonicalName());
						
					}
				});
			}
		}
	}
	
	public static String checkAndChangePath(String imgName, Attribute a) {
		File imgfile = new File(imgName);
		if (!imgfile.exists()) {
			if (a.getAttributable() instanceof Node) {
				Node nd = (Node) a.getAttributable();
				String graphname = nd.getGraph().getName(true);
				if (graphname != null) {
					File relimgfile = new File(new File(graphname).getParent() + "/" + imgName);
					if (relimgfile.exists())
						return relimgfile.getAbsolutePath();
				}
			}
		}
		return imgName;
	}
	
	protected void updatePosition() {
		if (imageComponent != null)
			setLocation(shift.x + (int) offX, shift.y + (int) offY);
		// setLocation((int)(shift.x-imgWidth/2d), (int)(shift.y-imgHeight/2d));
		// setLocation(new Double(shift.x+AttributeHelper.getWidth((Node)getAttribute().getAttributable())).intValue()+5,
		// new Double(shift.y-imgHeight/2).intValue());
	}
	
	private void changeParameters(Object graphicsAttr, Node n)
						throws ShapeNotFoundException {
		if ((graphicsAttr != null)
							&& (graphicsAttr instanceof CollectionAttribute)) {
			CollectionAttribute cAttr = (CollectionAttribute) graphicsAttr;
			Object annotationObject = cAttr.getCollection().get("component");
			
			if ((annotationObject != null)
								&& (annotationObject instanceof CompoundAttribute)) {
				System.out.println("annotation changed");
				
				// ChartAttribute testAttr = (ChartAttribute) annotationObject;
				recreate();
			}
			
			Object coordinateObject = cAttr.getCollection().get(COORDINATE);
			Object dimensionObject = cAttr.getCollection().get(DIMENSION);
			
			if (((coordinateObject != null) && (coordinateObject instanceof CoordinateAttribute))
								|| ((dimensionObject != null) && (dimensionObject instanceof DimensionAttribute))) {
				System.out.println("coordinates or dimension changed");
				updatePosition();
			}
		} else {
			// recreate();
		}
	}
	
	@Override
	public void setShift(Point shift) {
		super.setShift(shift);
		updatePosition();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.AttributeComponent#adjustComponentSize()
	 */
	@Override
	public void adjustComponentSize() {
	}
	
}
