/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.petri_ina_pnt;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

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

/**
 * @author klukas
 *         7.12.2006
 */
public class PNTReader
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
			String pos = getEntry("pos=", line, null);
			if (pos != null && pos.length() > 0) {
				if (pos.startsWith("e,"))
					pos = pos.substring("e,".length());
				ArrayList<Vector2d> bendsCol = new ArrayList<Vector2d>();
				String[] bends = pos.split(" ");
				for (String bend : bends) {
					bend = bend.trim();
					if (bend.indexOf(",") > 0) {
						String xp = bend.substring(0, bend.indexOf(","));
						String yp = bend.substring(bend.indexOf(",") + ",".length());
						double xpd = Double.parseDouble(xp);
						double ypd = Double.parseDouble(yp);
						bendsCol.add(new Vector2d(xpd, viewHeight - ypd));
					}
				}
				if (bendsCol.size() > 2) {
					bendsCol.remove(bendsCol.get(0));
					bendsCol.remove(bendsCol.get(bendsCol.size() - 1));
				}
				AttributeHelper.addEdgeBends(e, bendsCol);
				AttributeHelper.setEdgeBendStyle(e, "smooth");
			}
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
		}
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
	
	private void processNodeDesign(Node n, String line, String defaultFontSize, double viewHeight) {
		try {
			String label = getEntry("label=", line, null);
			if (label != null)
				AttributeHelper.setLabel(n, label);
			String height = getEntry("height=", line, null);
			if (height != null) {
				double h = Double.parseDouble(height);
				double oldWidth = AttributeHelper.getWidth(n);
				AttributeHelper.setSize(n, oldWidth, h * 75);
			}
			String width = getEntry("width=", line, null);
			if (width != null) {
				double w = Double.parseDouble(width);
				double oldHeight = AttributeHelper.getHeight(n);
				AttributeHelper.setSize(n, w * 75, oldHeight);
			}
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
		return new String[] { ".pnt" };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "INA PNT" };
	}
	
	public void read(Reader in, Graph g) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader br = new BufferedReader(in);
		String str = "";
		while (str != null) {
			str = br.readLine();
			lines.add(str);
		}
		PositionGridGenerator pgg = new PositionGridGenerator(100d, 100d, 1024d);
		HashMap<String, Node> id2graphNode = new HashMap<String, Node>();
		String defaultShape = "box";
		String defaultFontSize = "10";
		double viewHeight = 0;
		String lastLine = null;
		for (String line : lines) {
			if (line == null || line.length() <= 0)
				continue;
			if (line.endsWith("\\")) {
				lastLine = line.substring(0, line.length() - 1);
				continue;
			}
			if (lastLine != null) {
				line = lastLine + line;
			}
			lastLine = null;
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
							String shape = "shape=";
							String fontsize = "fontsize=";
							if (line.indexOf(shape) > 0) {
								defaultShape = getEntry(shape, line, defaultShape);
							}
							if (line.indexOf(fontsize) > 0) {
								defaultFontSize = getEntry(fontsize, line, defaultFontSize);
							}
						} else {
							boolean directed = g.isDirected();
							int arrowPos = line.indexOf(" -> ");
							if (arrowPos < 0) {
								arrowPos = line.indexOf(" -- ");
								directed = false;
							}
							int eqPos = line.indexOf("=");
							boolean isEdgeLine = arrowPos > 0;
							if (eqPos > 0 && isEdgeLine && arrowPos > eqPos)
								isEdgeLine = false;
							if (!isEdgeLine) {
								String id = getFirstId(line);
								if (id != null && id.length() > 0 && !id.trim().equalsIgnoreCase("}")) {
									// create node
									// id2graphNode
									Point2D np = pgg.getNextPosition();
									Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(np.getX(), np.getY()));
									processNodeDesign(n, line, defaultFontSize, viewHeight);
									id2graphNode.put(id, n);
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
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
