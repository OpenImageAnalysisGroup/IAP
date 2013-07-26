package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.AttributeHelper;
import org.NamedColorSet;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.attributecomponent.AbstractAttributeComponent;
import org.graffiti.plugin.view.CoordinateSystem;
import org.graffiti.plugin.view.ShapeNotFoundException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;

public class PathwayLinkVisualizationComponent
					extends AbstractAttributeComponent implements MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	NodePathwayLinkVisualizationAttribute va = null;
	ArrayList<DrawingGroup> items = null;
	
	double circleRadius = 0d;
	
	private boolean highlight = false;
	
	private int modeOfOperation = 1;
	
	private static long creationcount = 0;
	private long creationid = -1;
	
	private boolean isRectangle = false;
	
	public PathwayLinkVisualizationComponent() {
		creationcount++;
		creationid = creationcount;
	}
	
	@Override
	public void setShift(Point shift) {
		super.setShift(shift);
		adjustComponentSize();
		// setBounds(shift.x-getWidth()/2, shift.y-getHeight()/2, getWidth(), getHeight());
	}
	
	@Override
	public void attributeChanged(Attribute attr) throws ShapeNotFoundException {
		if (attr == null || attr.getId() == null)
			recreate();
		else {
			String id = attr.getId();
			if (id.equals("x") || id.equals("y") || id.equals("width") || id.equals("height"))
				adjustComponentSize();
			
		}
	}
	
	@Override
	public void createNewShape(CoordinateSystem coordSys) throws ShapeNotFoundException {
		super.createNewShape(coordSys);
	}
	
	@Override
	public void recreate() throws ShapeNotFoundException {
		setOpaque(false);
		adjustComponentSize();
		Node n = (Node) getAttribute().getAttributable();
		TreeSet<String> pathwayLinks = new TreeSet<String>(AttributeHelper.getPathwayReferences(n, true));
		Graph g = n.getGraph();
		TreeSet<String> allLinks = new TreeSet<String>(AttributeHelper.getPathwayReferences(g, true));
		items = getItemsToBeDrawnFromLinkInfo(allLinks, pathwayLinks);
		if (attr.getValue() != null && (attr.getValue() instanceof String)) {
			String mode = (String) attr.getValue();
			modeOfOperation = 0;
			if (mode == null || mode.length() == 0 || mode.equals("mode0") || mode.equals("false"))
				modeOfOperation = 0;
			else {
				if (mode.equals("mode1") || mode.equals("true"))
					modeOfOperation = 1;
				if (mode.equals("mode2"))
					modeOfOperation = 2;
			}
		}
	}
	
	private ArrayList<DrawingGroup> getItemsToBeDrawnFromLinkInfo(
						TreeSet<String> allLinks, TreeSet<String> pathwayLinks) {
		ArrayList<DrawingGroup> result = new ArrayList<DrawingGroup>();
		
		allLinks.addAll(pathwayLinks);
		
		TreeSet<String> level1 = new TreeSet<String>();
		TreeSet<String> level2 = new TreeSet<String>();
		for (String s : allLinks) {
			s = addKEGGpathwayGroupIfPossible(s);
			s = StringManipulationTools.stringReplace(s, "%46", ".");
			int idx = s.indexOf(".");
			String s2 = s;
			if (idx > 0) {
				s = s.substring(0, idx);
				int idx2 = s.indexOf(".", idx + ".".length());
				if (idx2 > 0) {
					s2 = s2.substring(0, idx2);
				}
			}
			level1.add(s);
			level2.add(s2);
		}
		
		HashSet<String> pathwayLinksUntilLevel2 = new HashSet<String>();
		for (String s : pathwayLinks) {
			s = addKEGGpathwayGroupIfPossible(s);
			s = StringManipulationTools.stringReplace(s, "%46", ".");
			int idx = s.indexOf(".");
			String s2 = s;
			if (idx > 0) {
				s = s.substring(0, idx);
				int idx2 = s.indexOf(".", idx + ".".length());
				if (idx2 > 0) {
					s2 = s2.substring(0, idx2);
				}
			}
			pathwayLinksUntilLevel2.add(s2);
		}
		
		HashMap<String, ArrayList<Boolean>> level1id2BL = new HashMap<String, ArrayList<Boolean>>();
		for (String l1 : level1) {
			ArrayList<Boolean> al = new ArrayList<Boolean>();
			for (String l2 : level2) {
				if (l2.startsWith(l1))
					al.add(pathwayLinksUntilLevel2.contains(l2));
			}
			result.add(new DrawingGroup(l1, al));
			level1id2BL.put(l1, al);
		}
		return result;
	}
	
	private String addKEGGpathwayGroupIfPossible(String s) {
		if (s != null && s.indexOf("%32%45%32map") > 0) {
			// this is a KEGG pathway link (ends with ' - mapXXX')
			String mapID = s.substring(s.indexOf("%32%45%32map") + "%32%45%32".length());
			String mapName = s;
			if (mapName.startsWith("filepath|"))
				mapName = mapName.substring("filepath|".length());
			if (mapName.indexOf("%32%45%32map") > 0)
				mapName = mapName.substring(0, mapName.indexOf("%32%45%32map"));
			String[] grouping = KeggHelper.getGroupFromMapNumber(mapID, mapName);
			boolean fp = false;
			if (s.startsWith("filepath|")) {
				s = s.substring("filepath|".length());
				fp = true;
			}
			for (int idx = grouping.length - 1; idx >= 0; idx--)
				s = grouping[idx] + "." + s;
			if (fp)
				s = "filepath|" + s;
			return s;
		} else
			return s;
	}
	
	@Override
	public void paint(Graphics g) {
		if (getMode() == 0)
			return;
		super.paint(g);
		double w = getWidth();
		double h = getHeight();
		double centerX = w / 2d;
		double centerY = h / 2d;
		if (w > 0 && h > 0) {
			Graphics2D g2 = (Graphics2D) g;
			ArrayList<Color> colors = getColors(items);
			int maxNubmerOfLinks = 0;
			for (DrawingGroup dg : items) {
				ArrayList<Boolean> ll = dg.draw;
				maxNubmerOfLinks += ll.size();
			}
			if (getMode() == 1)
				drawColorfulPieSegments(w, h, centerX, centerY, g2, colors, maxNubmerOfLinks);
			if (getMode() == 2)
				drawColorfulCircles(w, h, centerX, centerY, g2, colors, maxNubmerOfLinks);
			// if (lastMouseEvent!=null)
			// g.drawString(lastMouseEvent.getX()+"/"+lastMouseEvent.getY(), 0, 15);
		}
	}
	
	/**
	 * @param items2
	 * @return
	 */
	private ArrayList<Color> getColors(ArrayList<DrawingGroup> items) {
		ArrayList<String> groups = new ArrayList<String>();
		for (DrawingGroup dg : items)
			groups.add(dg.group);
		return NamedColorSet.getColors(groups, false);
	}
	
	private int getMode() {
		return modeOfOperation;
	}
	
	private void drawColorfulPieSegments(double w, double h, double centerX,
						double centerY, Graphics2D g2, ArrayList<Color> colors,
						int maxNubmerOfLinks) {
		double degreeStep = 360 / maxNubmerOfLinks;
		
		w = w * 0.7d;
		h = h * 0.7d;
		double degree = 135;
		int i = 0;
		for (DrawingGroup dg : items) {
			ArrayList<Boolean> ll = dg.draw;
			Color c = colors.get(i++);
			int re = c.getRed();
			int gr = c.getGreen();
			int bl = c.getBlue();
			boolean atLeastOneLink = false;
			int countEmptyOnes = 0;
			for (Boolean linked : ll) {
				if (linked) {
					atLeastOneLink = true;
					break;
				}
				
				countEmptyOnes++;
			}
			if (!atLeastOneLink)
				degree -= degreeStep * countEmptyOnes;
			for (Boolean linked : ll) {
				// degree+=degreeStep;
				if (!atLeastOneLink)
					continue;
				
				// double sinbeta = 0; //Math.sin(0);
				// double cosbeta = 1; // Math.cos(0);
				//
				// double sinalpha = Math.sin(degree);
				// double cosalpha = Math.cos(degree);
				
				// double xx = centerX + (w/2d * cosalpha * cosbeta - h/2d * sinalpha * sinbeta);
				// double yy = centerY + (w/2d * cosalpha * sinbeta + h/2d * sinalpha * cosbeta);
				
				// g2.fill(new Ellipse2D.Double(xx-circleRadius/2, yy-circleRadius/2, circleRadius, circleRadius));
				
				double d = degree;
				double ds = degreeStep;
				
				double ww = 1.4 * w;
				double hh = 1.4 * h;
				
				Area area;
				double ff1;
				if (linked || isRectangle)
					ff1 = 0.0d;
				else
					ff1 = 0.12d;
				
				if (isRectangle && getMode() == 1) {
					ff1 = 0.0;
				}
				
				if (highlight) {
					ff1 = ff1 * 1.5;
				}
				
				area = new Area(new Arc2D.Double(new Rectangle2D.Double(ww * ff1, hh * ff1, ww * (1 - 2 * ff1), hh * (1 - 2 * ff1)), d, ds, Arc2D.PIE));
				double ff2 = 0.25d;
				
				if (highlight) {
					ff2 = ff2 * 1.5;
				}
				
				if (isRectangle && getMode() == 1) {
					ff2 = ff2 * 1;
				}
				
				if (isRectangle && getMode() == 1) {
					if (highlight) {
						ff2 = ff2 * 0.7;
					}
					double fff2 = ff2;
					if (!linked)
						ff2 = fff2 * 1;
					else
						ff2 = fff2 * 0.5;
					Area rect1 = new Area(new Rectangle2D.Double(ww * ff2, hh * ff2, ww * (1 - 2 * ff2), hh * (1 - 2 * ff2)));
					area.intersect(rect1);
					
					ff2 = fff2 * 1.4;
					Area rect2 = new Area(new Rectangle2D.Double(ww * ff2, hh * ff2, ww * (1 - 2 * ff2), hh * (1 - 2 * ff2)));
					area.subtract(rect2);
				} else {
					Area area2 = new Area(new Arc2D.Double(new Rectangle2D.Double(ww * ff2, hh * ff2, ww * (1 - 2 * ff2), hh * (1 - 2 * ff2)), d - 0.0001,
										ds + 2 * 0.0001, Arc2D.PIE));
					area.subtract(area2);
				}
				if (linked) {
					g2.setPaint(new Color(re, gr, bl));
					g2.fill(area);
				} else {
					if (atLeastOneLink) {
						g2.setPaint(new Color(re, gr, bl));
						g2.fill(area);
					}
				}
				degree -= degreeStep;
			}
			
		}
	}
	
	private void drawColorfulCircles(double w, double h, double centerX,
						double centerY, Graphics2D g2, ArrayList<Color> colors,
						int maxNubmerOfLinks) {
		double degreeStep = 2 * Math.PI / maxNubmerOfLinks;
		
		// GeneralPath gp = new GeneralPath();
		// gp.moveTo(20, 20);
		// gp.lineTo(30, 30);
		// gp.closePath();
		//
		// g2d.fill(gp);
		// g2d.draw(gp);
		//
		// if (gp.contains(mp.x, mp.y))
		// highlight(xyz);
		
		w = w * 0.7d;
		h = h * 0.7d;
		double degree = -degreeStep;
		int i = 0;
		for (DrawingGroup dg : items) {
			ArrayList<Boolean> ll = dg.draw;
			Color c = colors.get(i++);
			int re = c.getRed();
			int gr = c.getGreen();
			int bl = c.getBlue();
			for (Boolean linked : ll) {
				degree += degreeStep;
				if (!linked)
					continue;
				g2.setPaint(new Color(re, gr, bl, 140));
				
				double sinbeta = 0; // Math.sin(0);
				double cosbeta = 1; // Math.cos(0);
				
				double sinalpha = Math.sin(degree);
				double cosalpha = Math.cos(degree);
				
				double xx = centerX + (w / 2d * cosalpha * cosbeta - h / 2d * sinalpha * sinbeta);
				double yy = centerY + (w / 2d * cosalpha * sinbeta + h / 2d * sinalpha * cosbeta);
				
				g2.fill(new Ellipse2D.Double(xx - circleRadius / 2, yy - circleRadius / 2, circleRadius, circleRadius));
			}
		}
	}
	
	@Override
	public void adjustComponentSize() {
		int w = getWidth();
		int h = getHeight();
		if (attr == null)
			return;
		GraphElement ge = (GraphElement) this.attr.getAttributable();
		
		if (ge instanceof Node) {
			Node n = (Node) ge;
			String shape = AttributeHelper.getShape(n);
			if (shape.toUpperCase().indexOf("RECT") >= 0)
				isRectangle = true;
			Vector2d size = AttributeHelper.getSize(n);
			if (getMode() == 2) {
				w = (int) (size.x * 0.9d);
				h = (int) (size.y * 0.9d);
			} else {
				w = (int) (size.x);
				h = (int) (size.y);
			}
			
			if (highlight) {
				w = w * 4;
				h = h * 4;
			}
			
			if (isRectangle && getMode() == 1) {
				w = (int) (w * 1.2d);
				h = (int) (h * 1.2d);
			}
			
			circleRadius = size.x < size.y ? size.x * 0.3d : size.y * 0.3d;
			
			if (highlight)
				circleRadius = circleRadius * 2;
			
			if (w < 0)
				w = 0;
			if (h < 0)
				h = 0;
			
			setSize(w, h);
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			setLocation((int) (pos.x - w / 2d), (int) (pos.y - h / 2d));
		} else {
			// attribute is not supported to be added to edges
		}
	}
	
	@Override
	public void highlight(boolean value, MouseEvent e) {
		highlight = value;
		adjustComponentSize();
		repaint();
	}
	
	@Override
	public String toString() {
		return creationid + "";
	}
	
	// mouse motion
	
	public void mouseDragged(MouseEvent e) {
		//
		
	}
	
	public void mouseMoved(MouseEvent e) {
	}
}
