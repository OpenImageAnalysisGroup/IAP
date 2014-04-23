/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.awt.Color;
import java.util.Collection;

import org.AttributeHelper;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlColor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlGraphicsType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlNumber;

public class Graphics {
	
	private String name;
	private kgmlNumber x;
	private kgmlNumber y;
	private kgmlGraphicsType type;
	private kgmlNumber width;
	private kgmlNumber height;
	private kgmlColor fgcolor;
	private kgmlColor bgcolor;
	
	public static int missingEntryGraphicsSize = 8;
	
	public Graphics(
						String name,
						kgmlNumber x,
						kgmlNumber y,
						kgmlGraphicsType type,
						kgmlNumber width,
						kgmlNumber height,
						kgmlColor fgcolor,
						kgmlColor bgcolor,
						boolean isGeneProduct) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.type = type;
		this.width = width;
		this.height = height;
		this.fgcolor = fgcolor;
		this.bgcolor = bgcolor;
		
		if (this.width == null)
			this.width = new kgmlNumber(45);
		if (this.height == null)
			this.height = new kgmlNumber(17);
		if (this.fgcolor == null)
			this.fgcolor = new kgmlColor("#000000");
		if (this.bgcolor == null) {
			if (isGeneProduct)
				this.bgcolor = new kgmlColor("#BFFFBF");
			else
				this.bgcolor = new kgmlColor("#FFFFFF");
		}
	}
	
	public static Graphics getGraphicsFromKgmlElement(Element graphicsElement) {
		if (graphicsElement == null)
			return null;
		else {
			String nameValue = KGMLhelper.getAttributeValue(graphicsElement, "name", null);
			String fgcolorValue = KGMLhelper.getAttributeValue(graphicsElement, "fgcolor", null);
			String bgcolorValue = KGMLhelper.getAttributeValue(graphicsElement, "bgcolor", null);
			String typeValue = KGMLhelper.getAttributeValue(graphicsElement, "type", null);
			String xValue = KGMLhelper.getAttributeValue(graphicsElement, "x", null);
			String yValue = KGMLhelper.getAttributeValue(graphicsElement, "y", null);
			String widthValue = KGMLhelper.getAttributeValue(graphicsElement, "width", null);
			String heightValue = KGMLhelper.getAttributeValue(graphicsElement, "height", null);
			String coordsForLine = KGMLhelper.getAttributeValue(graphicsElement, "coords", null);
			
			String name = nameValue;
			kgmlNumber x = xValue != null ? kgmlNumber.getNumber(xValue) : null;
			kgmlNumber y = yValue != null ? kgmlNumber.getNumber(yValue) : null;
			kgmlGraphicsType type = kgmlGraphicsType.getGraphicsType(typeValue);
			kgmlNumber width = kgmlNumber.getNumber(widthValue);
			kgmlNumber height = kgmlNumber.getNumber(heightValue);
			kgmlColor fgcolor = kgmlColor.getKgmlColor(fgcolorValue);
			kgmlColor bgcolor = kgmlColor.getKgmlColor(bgcolorValue);
			
			if (type == kgmlGraphicsType.line) {
				// for lines the coords value specify the location of the graphical element
				// example: ko01040.xml
				// coords seems to specify the start and end points of a line
				// it is mapped to x,y and width,height values
				// width or height will normally be of value 1.
				// in case the line is not vertical or horizontal, a problem arises, as entries
				// can't currently specify graph edges, but they specify nodes, therefore, it would be
				// necessarry to create two graph nodes for this entry and a connecting line
				// this may be added later
				if (coordsForLine != null && coordsForLine.length() > 0 && x == null && y == null) {
					String[] coo = coordsForLine.split(",");
					if (coo.length == 4) {
						String x1 = coo[0];
						String y1 = coo[1];
						String x2 = coo[2];
						String y2 = coo[3];
						x = kgmlNumber.getNumber(x1);
						y = kgmlNumber.getNumber(y1);
						width = kgmlNumber.getNumber(x2);
						height = kgmlNumber.getNumber(y2);
						width = kgmlNumber.getNumber((width.getValue() - x.getValue()) + "");
						height = kgmlNumber.getNumber((height.getValue() - y.getValue()) + "");
					}
				}
				
			}
			
			// check if it is possible to easily find out if this is a gene product
			// this value is eventually not important
			boolean isGeneProduct = false;
			
			return new Graphics(name, x, y, type, width, height, fgcolor, bgcolor, isGeneProduct);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Element getKgmlGraphicsElement() {
		Element graphicsElement = new Element("graphics");
		if (name != null)
			KGMLhelper.addNewAttribute(graphicsElement, "name", name);
		if (fgcolor != null)
			KGMLhelper.addNewAttribute(graphicsElement, "fgcolor", fgcolor.toString());
		if (bgcolor != null)
			KGMLhelper.addNewAttribute(graphicsElement, "bgcolor", bgcolor.toString());
		if (type != null)
			KGMLhelper.addNewAttribute(graphicsElement, "type", type.toString());
		if (x != null)
			KGMLhelper.addNewAttribute(graphicsElement, "x", x.toString());
		if (y != null)
			KGMLhelper.addNewAttribute(graphicsElement, "y", y.toString());
		if (width != null)
			KGMLhelper.addNewAttribute(graphicsElement, "width", width.toString());
		if (height != null)
			KGMLhelper.addNewAttribute(graphicsElement, "height", height.toString());
		return graphicsElement;
	}
	
	public void processNodeDesign(Node n, boolean hasComponents) {
		if (name != null)
			KeggGmlHelper.setKeggGraphicsTitle(n, name);
		if (x != null)
			KeggGmlHelper.setKeggGraphicsX(n, x.getValue());
		if (y != null)
			KeggGmlHelper.setKeggGraphicsY(n, y.getValue());
		if (width != null)
			KeggGmlHelper.setKeggGraphicsWidth(n, width.getValue());
		if (height != null)
			KeggGmlHelper.setKeggGraphicsHeight(n, height.getValue());
		if (fgcolor != null)
			KeggGmlHelper.setKeggGraphicsFgColor(n, fgcolor.getColor());
		if (bgcolor != null) {
			Color bgc = bgcolor.getColor();
			if (hasComponents)
				KeggGmlHelper.setKeggGraphicsBgColor(n, new Color(bgc.getRed(), bgc.getGreen(), bgc.getBlue(), 120));
			else
				KeggGmlHelper.setKeggGraphicsBgColor(n, bgc);
		} else
			if (hasComponents) {
				KeggGmlHelper.setKeggGraphicsBgColor(n, new Color(255, 255, 255, 120));
			}
		
		if (type != null)
			KeggGmlHelper.setKeggGraphicsType(n, type);
		
		AttributeHelper.setBorderWidth(n, 1);
		if (name != null) {
			if (name.contains("TITLE:")) {
				// name = name.substring("TITLE".length());
				AttributeHelper.setBorderWidth(n, 3);
				AttributeHelper.setRoundedEdges(n, 1);
			}
		}
		LabelAttribute la = AttributeHelper.getLabel(-1, n);
		if (la != null) {
			la.setFontSize(10);
			la.wordWrap();
			
			if (type != null && type == kgmlGraphicsType.circle)
				la.setAlignment(GraphicAttributeConstants.AUTO_OUTSIDE);
		}
	}
	
	public static void processDefaultNodeDesign(Node n, Entry entry) {
		// set attribute and style to indicate "hidden in view" status of this entry
		// the attribute needs to be processed while creating a graphics or entry
		// element from a GML graph node
		
		// set position, style, title from default or generated
		KeggGmlHelper.setKeggGraphicsWidth(n, missingEntryGraphicsSize);
		KeggGmlHelper.setKeggGraphicsHeight(n, missingEntryGraphicsSize);
		// KeggGmlHelper.setKeggGraphicsBgColor(n, Color.LIGHT_GRAY);
		KeggGmlHelper.setKeggGraphicsType(n, kgmlGraphicsType.circle);
		String id = entry.getName().getId();
		if (id != null && id.contains(":"))
			id = id.substring(id.indexOf(":") + 1);
		KeggGmlHelper.setKeggGraphicsTitle(n, "<html>" + id + "<br> (" + entry.getType() + ")");
		
		LabelAttribute la = AttributeHelper.getLabel(-1, n);
		if (la != null) {
			la.setFontSize(8);
			la.setAlignment(GraphicAttributeConstants.AUTO_OUTSIDE);
		}
		
		AttributeHelper.setBorderWidth(n, 1);
	}
	
	public static Graphics getGraphicsFromGraphNode(
						Node graphNode,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors) {
		String name = KeggGmlHelper.getKeggGraphicsTitle(graphNode);
		if (name == null || name.length() <= 0) {
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NO_KEGG_LABEL, graphNode));
		}
		kgmlNumber x = new kgmlNumber(KeggGmlHelper.getKeggGraphicsX(graphNode));
		kgmlNumber y = new kgmlNumber(KeggGmlHelper.getKeggGraphicsY(graphNode));
		if (x.getValue() < 0) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.NEGATIVE_X_VALUE, graphNode));
		}
		if (y.getValue() < 0) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.NEGATIVE_Y_VALUE, graphNode));
		}
		kgmlGraphicsType type = KeggGmlHelper.getKeggGraphicsType(graphNode);
		if (type == null) {
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.INVALID_GRAPHICS_TYPE, graphNode));
		}
		kgmlNumber width = new kgmlNumber(KeggGmlHelper.getKeggGraphicsWidth(graphNode));
		kgmlNumber height = new kgmlNumber(KeggGmlHelper.getKeggGraphicsHeight(graphNode));
		if (x.getValue() < 0) {
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NON_POSITIVE_WIDTH_VALUE, graphNode));
		}
		if (y.getValue() < 0) {
			warnings.add(new Gml2PathwayWarningInformation(Gml2PathwayWarning.NON_POSITIVE_HEIGHT_VALUE, graphNode));
		}
		Color fgc = KeggGmlHelper.getKeggGraphicsFgColor(graphNode);
		if (fgc == null)
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.INVALID_FOREGROUNDCOLOR, graphNode));
		
		kgmlColor fgcolor = new kgmlColor(fgc);
		Color bgc = KeggGmlHelper.getKeggGraphicsBgColor(graphNode);
		if (bgc == null)
			errors.add(new Gml2PathwayErrorInformation(Gml2PathwayError.INVALID_BACKGROUNDCOLOR, graphNode));
		kgmlColor bgcolor = new kgmlColor(bgc);
		
		boolean isGeneProduct = false; // this value is not used for Pathway to KGML conversion
		return new Graphics(name, x, y, type, width, height, fgcolor, bgcolor, isGeneProduct);
	}
	
	public Color getBGcolor() {
		return bgcolor.getColor();
	}
	
	public Color getFGcolor() {
		return fgcolor.getColor();
	}
	
	public void setBGcolor(Color bgc) {
		this.bgcolor = new kgmlColor(bgc);
	}
	
	public void setFGcolor(Color fgc) {
		this.fgcolor = new kgmlColor(fgc);
	}
	
	public void setGraphicsType(kgmlGraphicsType graphicsType) {
		this.type = graphicsType;
	}
	
	public void setWidth(int width) {
		this.width = new kgmlNumber(width);
	}
	
	public void setHeight(int height) {
		this.height = new kgmlNumber(height);
	}
}
