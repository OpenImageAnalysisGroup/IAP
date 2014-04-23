package org.graffiti.plugins.ios.exporters.graphviz;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.graffiti.plugins.views.defaults.CircleNodeShape;
import org.graffiti.plugins.views.defaults.EllipseNodeShape;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

/**
 * @author klukas
 *         15.5.2006
 */
public class DOTreader
					extends AbstractInputSerializer {
	@Override
	public void read(InputStream in, Graph g) throws IOException {
		read(new InputStreamReader(in), g);
	}
	
	private double getViewHeight(String line) {
		double res = 0;
		String bb = getEntry("bb=", line, "0,0,0,0");
		if (bb.indexOf(",") > 0) {
			String hs = bb.substring(bb.lastIndexOf(",") + 1);
			if (hs != null && hs.length() > 0) {
				try {
					res = Double.parseDouble(hs);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage("Invalid BB setting: " + line);
				}
			}
		}
		return res;
	}
	
	private void processEdgeDesign(Edge e, String line, double viewHeight) {
		try {
			String label = getEntry("label=", line, null);
			if (label != null)
				AttributeHelper.setLabel(e, label);
			String color = getEntry("color=", line, null);
			if (color != null) {
				Color c = null;
				if (color.indexOf(" ") > 0)
					c = AttributeHelper.getColorFrom3floatValues0to1(color, null);
				else
					c = AttributeHelper.getColorFromName(color, null);
				if (c != null)
					AttributeHelper.setOutlineColor(e, c);
			}
			String pos = getEntry("pos=", line, null);
			if (pos != null && pos.length() > 0) {
				if (pos.startsWith("e,"))
					pos = pos.substring("e,".length());
				// i dont know why, but some bend coordinates had an "\" in it
				pos = StringManipulationTools.stringReplace(pos, "\\", "");
				ArrayList<Vector2d> bendsCol = new ArrayList<Vector2d>();
				String[] bends = pos.split(" ");
				double lastXP = Double.NaN;
				double lastYP = Double.NaN;
				double epsilon = 4;
				for (String bend : bends) {
					bend = bend.trim();
					if (bend.indexOf(",") > 0) {
						String xp = bend.substring(0, bend.indexOf(","));
						String yp = bend.substring(bend.indexOf(",") + ",".length());
						double xpd = Double.parseDouble(xp);
						double ypd = Double.parseDouble(yp);
						if (distance(xpd, ypd, lastXP, lastYP) < epsilon) {
							continue;
						}
						if (!inside(e.getSource(), xpd, ypd, epsilon) && !inside(e.getTarget(), xpd, viewHeight - ypd, epsilon)) {
							bendsCol.add(new Vector2d(xpd, viewHeight - ypd));
						}
						lastXP = xpd;
						lastYP = ypd;
					}
				}
				if (bendsCol.size() > 2) {
					bendsCol.remove(bendsCol.get(0));
					bendsCol.remove(bendsCol.get(bendsCol.size() - 1));
				}
				AttributeHelper.addEdgeBends(e, bendsCol);
				AttributeHelper.setEdgeBendStyle(e, "smooth");
				// AttributeHelper.setEdgeBendStyle(e, "polyline");
			}
			String style = getEntry("style=", line, null);
			if (style != null) {
				if (style.contains("setlinewidth(")) {
					String lineWidth = style.substring(style.indexOf("setlinewidth(") + 13);
					lineWidth = lineWidth.substring(0, lineWidth.indexOf(")"));
					try {
						AttributeHelper.setFrameThickNess(e, Double.parseDouble(lineWidth));
					} catch (NumberFormatException numberformatexception) {
						ErrorMsg.addErrorMessage("Invalid Line Width: " + line);
					}
				}
			}
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	private double distance(double x, double y, double x2, double y2) {
		if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(x2) || Double.isNaN(y2))
			return Double.MAX_VALUE;
		return Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
	}
	
	private boolean inside(Node n, double x, double y, double epsilon) {
		epsilon = epsilon / 2;
		Vector2d np = AttributeHelper.getPositionVec2d(n);
		Vector2d ns = AttributeHelper.getSize(n);
		return new Rectangle2D.Double(np.x - ns.x / 2 - epsilon / 2, np.y - ns.y / 2 - epsilon / 2, ns.x + epsilon, ns.y + epsilon).contains(x, y);
	}
	
	private String getNextEdgeId(String line) {
		if (line.indexOf("->") <= 0 && line.indexOf("--") <= 0)
			return null;
		String rol;
		if (line.indexOf("->") > 0)
			rol = line.substring(line.indexOf("->") + "->".length());
		else
			rol = line.substring(line.indexOf("--") + "--".length());
		rol = rol.trim();
		String id = getFirstId(rol);
		return id;
	}
	
	private void processNodeDesign(Node n, String line,
						String defaultShape,
						String defaultFontSize,
						Color defaultColor,
						double viewHeight, String id) {
		try {
			String label = getEntry("label=", line, null);
			if (label == null)
				label = id;
			AttributeHelper.setLabel(n, label);
			
			boolean isCircle = false;
			String shape = getEntry("shape=", line, null);
			if (shape != null) {
				isCircle = setShapeFromDesc(n, shape);
			} else {
				if (defaultShape != null)
					isCircle = setShapeFromDesc(n, defaultShape);
			}
			
			String height = getEntry("height=", line, null);
			if (height != null) {
				double h = Double.parseDouble(height);
				double oldWidth = AttributeHelper.getWidth(n);
				AttributeHelper.setSize(n, oldWidth, h * 75);
			} else {
				double oldWidth = AttributeHelper.getWidth(n);
				AttributeHelper.setSize(n, oldWidth, new JLabel(label).getPreferredSize().getHeight() + 5);
			}
			String width = getEntry("width=", line, null);
			if (width != null) {
				double w = Double.parseDouble(width);
				double oldHeight = AttributeHelper.getHeight(n);
				AttributeHelper.setSize(n, w * 75, oldHeight);
			} else {
				double oldHeight = AttributeHelper.getHeight(n);
				AttributeHelper.setSize(n, new JLabel(label).getPreferredSize().getWidth() + 5, oldHeight);
			}
			if (isCircle) {
				double w = AttributeHelper.getWidth(n);
				double h = AttributeHelper.getHeight(n);
				if (w > h)
					h = w;
				if (h > w)
					w = h;
				AttributeHelper.setSize(n, w, h);
			}
			
			String color = getEntry("color=", line, null);
			if (color != null) {
				Color c = null;
				if (color.indexOf(" ") > 0)
					c = AttributeHelper.getColorFrom3floatValues0to1(color, null);
				else
					c = AttributeHelper.getColorFromName(color, null);
				if (c != null)
					AttributeHelper.setFillColor(n, c);
			} else
				if (defaultColor != null)
					AttributeHelper.setFillColor(n, defaultColor);
			String pos = getEntry("pos=", line, null);
			if (pos != null) {
				if (pos.indexOf(",") > 0) {
					String x = pos.substring(0, pos.indexOf(","));
					String y = pos.substring(pos.indexOf(",") + 1);
					x = x.trim();
					y = y.trim();
					double xp = Double.parseDouble(x);
					double yp = Double.parseDouble(y);
					AttributeHelper.setPosition(n, xp, viewHeight - yp);
				}
			}
			String fontSize = getEntry("fontsize=", line, defaultFontSize);
			if (fontSize != null) {
				try {
					Double fs = Double.parseDouble(fontSize);
					LabelAttribute la = AttributeHelper.getLabel(-1, n);
					if (la != null)
						la.setFontSize(fs.intValue());
				} catch (NumberFormatException nfe) {
					ErrorMsg.addErrorMessage("Invalid Font-Size specification: " + fontSize);
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("Invalid node settings: " + line);
		}
	}
	
	private boolean setShapeFromDesc(Node n, String shape) {
		if (shape.equalsIgnoreCase("circle")) {
			AttributeHelper.setShape(n, CircleNodeShape.class.getCanonicalName());
			return true;
		}
		if (shape.equalsIgnoreCase("doublecircle")) {
			AttributeHelper.setShape(n, CircleNodeShape.class.getCanonicalName());
			AttributeHelper.setBorderWidth(n, 2);
			return true;
		}
		if (shape.equalsIgnoreCase("ellipse")) {
			AttributeHelper.setShape(n, EllipseNodeShape.class.getCanonicalName());
			return false;
		}
		if (shape.equalsIgnoreCase("box")) {
			AttributeHelper.setShape(n, RectangleNodeShape.class.getCanonicalName());
		}
		return false;
	}
	
	private String getFirstId(String line) {
		try {
			if (line.indexOf("[") > 0) {
				return line.substring(0, line.indexOf("[")).trim();
			} else {
				if (line.endsWith(";"))
					return line.substring(0, line.length() - 1).trim();
				else
					return line;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	private String getEntry(String setting, String line, String defaultReturn) {
		String r1 = getEntryImpl(setting, line, defaultReturn);
		if (r1 == null) {
			setting = StringManipulationTools.stringReplace(setting, "=", " = ");
			String r2 = getEntryImpl(setting, line, defaultReturn);
			return r2;
		} else
			return r1;
	}
	
	private String getEntryImpl(String setting, String line, String defaultReturn) {
		if (line.indexOf(setting) < 0)
			return defaultReturn;
		String restOfLine = line.substring(line.indexOf(setting) + setting.length());
		if (restOfLine.startsWith("\"")) {
			restOfLine = restOfLine.substring(1);
			if (restOfLine.indexOf("\"") >= 0) {
				return restOfLine.substring(0, restOfLine.indexOf("\""));
			}
			ErrorMsg.addErrorMessage("Text in line [" + line + "] is not properly quotet (missing &quot;).");
			return restOfLine;
		} else {
			String del1 = ", ";
			String del2 = "]";
			if (restOfLine.indexOf(del1) >= 0) {
				return restOfLine.substring(0, restOfLine.indexOf(del1));
			} else
				if (restOfLine.indexOf(del2) >= 0) {
					return restOfLine.substring(0, restOfLine.indexOf(del2));
				} else {
					ErrorMsg.addErrorMessage("Text in line [" + line + "] is not properly quotet (missing , or ]).");
					return restOfLine;
				}
		}
	}
	
	public String[] getExtensions() {
		return new String[] { ".dot" };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "DOT" };
	}
	
	public void read(Reader in, Graph g) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(in);
		String str = "";
		while (str != null) {
			str = br.readLine();
			if (str != null)
				while (str.indexOf("]; ") > 0 && str.indexOf("]; ") < str.length() - "]; ".length() - 1) {
					lines.add(str.substring(0, str.indexOf("]; ") + "]; ".length()));
					str = str.substring(str.indexOf("]; ") + "]; ".length());
				}
			if (str != null)
				lines.add(str);
		}
		PositionGridGenerator pgg = new PositionGridGenerator(100d, 100d, 1024d);
		HashMap<String, Node> id2graphNode = new HashMap<String, Node>();
		String defaultShape = "box";
		String defaultFontSize = "10";
		Color defaultColor = Color.WHITE;
		double viewHeight = 0;
		String lastLine = "";
		boolean searchEndTag = false;
		for (String line : lines) {
			if (line == null || line.length() <= 0)
				continue;
			if (line.indexOf("[") >= 0 && line.indexOf("]") < 0) {
				if (line.endsWith("\\"))
					lastLine += line.substring(0, line.length() - 1);
				else
					lastLine += line;
				searchEndTag = true;
				continue;
			}
			if (searchEndTag && line.indexOf("]") < 0) {
				lastLine += line;
				continue;
			}
			if (searchEndTag && line.indexOf("]") >= 0) {
				lastLine += line;
				searchEndTag = false;
			}
			if (line.endsWith("\\")) {
				lastLine += line.substring(0, line.length() - 1);
				continue;
			}
			if (lastLine != null && lastLine.length() > 0) {
				line = lastLine + line;
			}
			lastLine = "";
			line = line.trim();
			if (line.startsWith("digraph ") && line.endsWith("{")) {
				g.setDirected(true);
			} else
				if (line.startsWith("graph ") && line.endsWith("{")) {
					g.setDirected(false);
				} else {
					if (line.startsWith("graph ")) {
						viewHeight = getViewHeight(line);
					} else
						if (line.startsWith("node ")) {
							if (line.indexOf(" =") >= 0)
								line = StringManipulationTools.stringReplace(line, " =", "=");
							if (line.indexOf("= ") >= 0)
								line = StringManipulationTools.stringReplace(line, "= ", "=");
							String shape = "shape=";
							String fontsize = "fontsize=";
							if (line.indexOf(shape) > 0) {
								defaultShape = getEntry(shape, line, defaultShape);
							}
							if (line.indexOf(fontsize) > 0) {
								defaultFontSize = getEntry(fontsize, line, defaultFontSize);
							}
							String color = getEntry("color=", line, null);
							if (color != null) {
								Color c = null;
								if (color.indexOf(" ") > 0)
									c = AttributeHelper.getColorFrom3floatValues0to1(color, null);
								else
									c = AttributeHelper.getColorFromName(color, null);
								if (c != null)
									defaultColor = c;
							}
						} else {
							boolean directed = g.isDirected();
							int arrowPos = line.indexOf("->");
							if (arrowPos < 0) {
								arrowPos = line.indexOf("--");
								directed = false;
							}
							int eqPos = line.indexOf("=");
							boolean isEdgeLine = arrowPos > 0;
							if (eqPos > 0 && isEdgeLine && arrowPos > eqPos)
								isEdgeLine = false;
							if (!isEdgeLine) {
								String id = getFirstId(line);
								if (id != null && id.length() > 0 && !id.trim().toUpperCase().startsWith("SUBGRAPH") && !id.trim().equalsIgnoreCase("}")) {
									if (id.indexOf("\"") < 0 && id.indexOf(" ") >= 0) {
										String[] ids = id.split(" ");
										for (String idd : ids)
											addNodeOrSetStyle(id2graphNode.get(id), g, pgg, id2graphNode, defaultShape, defaultFontSize, defaultColor, viewHeight, line,
																idd);
									} else
										addNodeOrSetStyle(id2graphNode.get(id), g, pgg, id2graphNode, defaultShape, defaultFontSize, defaultColor, viewHeight, line, id);
								}
							} else {
								// create edge
								if (arrowPos <= 0)
									continue;
								String idA = getFirstId(line.substring(0, arrowPos).trim());
								if (idA != null) {
									String idB = getNextEdgeId(line);
									if (idB != null) {
										Node a = id2graphNode.get(idA);
										Node b = id2graphNode.get(idB);
										if (a == null)
											a = addNodeOrSetStyle(null, g, pgg, id2graphNode, defaultShape, defaultFontSize, defaultColor, viewHeight, "", idA);
										if (b == null)
											b = addNodeOrSetStyle(null, g, pgg, id2graphNode, defaultShape, defaultFontSize, defaultColor, viewHeight, "", idB);
										if (a == null || b == null) {
											ErrorMsg.addErrorMessage("Invalid Line Definition (Node not defined): " + line);
										} else {
											Edge e = g.addEdge(a, b, directed, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, directed));
											processEdgeDesign(e, line, viewHeight);
										}
									}
								}
							}
						}
				}
		}
	}
	
	private Node addNodeOrSetStyle(Node existingNode, Graph g, PositionGridGenerator pgg, HashMap<String, Node> id2graphNode, String defaultShape,
						String defaultFontSize, Color defaultColor, double viewHeight, String line, String id) {
		Point2D np = pgg.getNextPosition();
		Node n = existingNode;
		if (n == null) {
			n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(np.getX(), np.getY()));
			AttributeHelper.setBorderWidth(n, 0.5d);
			AttributeHelper.setRoundedEdges(n, 15d);
			id2graphNode.put(id, n);
		}
		processNodeDesign(n, line, defaultShape, defaultFontSize,
							defaultColor, viewHeight, id);
		return n;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
