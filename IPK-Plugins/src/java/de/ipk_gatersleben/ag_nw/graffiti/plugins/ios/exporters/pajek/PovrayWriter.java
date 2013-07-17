/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.pajek;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractOutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class PovrayWriter
					extends AbstractOutputSerializer {
	
	public void write(OutputStream out, Graph g) throws IOException {
		PrintStream stream = new PrintStream(out);
		printGraph(stream, g);
		stream.close();
	}
	
	private static String LS = "\r\n";
	
	private void printGraph(PrintStream stream, Graph g) {
		
		String edgeStyle = "", nodeStyle = "", nodePigment = "", edgePigment = "";
		String LSH = "\\";
		Object[] res = MyInputHelper.getInput("Please specify the optional node and edge finish",
							"Povray Finish Settings",
							new Object[] {
												"Node Style", "interior { " + LSH +
																	"ior 1.3" + LSH +
																	"}" + LSH,
												"Node Pigment Settings", "filter 0.2" + LSH,
												"Node Normals", "normal { " + LSH + "dents 0.1" + LSH + "scale 0.05" + LSH + "}" + LSH,
												"Edge Style", "interior { " + LSH +
																	"ior 1.3" + LSH +
																	"}" + LSH,
												"Edge Pigment Settings", "filter 0.8" + LSH,
												"Edge Normals", "normal { " + LSH + "dents 0.1" + LSH + "scale 0.05" + LSH + "}" + LSH
		});
		if (res != null) {
			nodeStyle = (String) res[0] + (String) res[2];
			edgeStyle = (String) res[3] + (String) res[5];
			nodeStyle = StringManipulationTools.stringReplace(nodeStyle, "\\", "\r\n");
			edgeStyle = StringManipulationTools.stringReplace(edgeStyle, "\\", "\r\n");
			nodePigment = (String) res[1];
			edgePigment = (String) res[4];
			nodePigment = StringManipulationTools.stringReplace(nodePigment, "\\", "\r\n");
			edgePigment = StringManipulationTools.stringReplace(edgePigment, "\\", "\r\n");
		}
		
		stream.print("camera" + LS + "{" + LS + " location <0, 0, 0>" + LS + "look_at  <" + getCenter(g, 1) + ">" + LS
							+ "}" + LS +
							"light_source" + LS + "{" + LS + "<" + getCenter(g, 3) + ">" + LS + "color rgb <1, 1, 1>" + LS + "}" + LS +
							// "global_settings"+LS+
				// "{"+LS+
				// "ambient_light rgb<1,1,1>"+LS+
				// "}"+LS+
				"background" + LS +
							"{" + LS +
							"color rgb<1,1,1>" + LS +
							"}" + LS +
							// "fog"+LS+
				// "{"+LS+
				// "distance 100"+LS+ // getCenterDistance(g, 30)
				// "color rgb<0.3,0.3,0.3>"+LS+
				// "}"+LS+
				// "plane"+LS+
				// "{<1,1,1>, 20000"+LS+
				// "	texture {"+LS+
				// "		pigment {"+LS+
				// "			color rgb<0.4,0.4,0.4>"+LS+
				// "		}"+LS+
				// "	}"+LS+
				// "}"+LS+
				"light_source" + LS +
							"{" + LS +
							" <-1700, -500, -900>" + LS +
							" color rgb <1, 1, 1>" + LS +
							" area_light <0, 0, 0>, <0, 0, 0>, 10, 10" + LS +
							"    adaptive 3" + LS +
							"    jitter" + LS +
							"}" + LS
							);
		
		for (Node n : g.getNodes()) {
			stream.print(getPovRayNodeDesc(n, nodeStyle, nodePigment));
		}
		
		for (Edge e : g.getEdges()) {
			stream.print(getPovRayEdgeDesc(e, edgeStyle, edgePigment));
		}
	}
	
	private String getCenter(Graph g, double div) {
		if (g.getNumberOfNodes() <= 0)
			return "0,0,1";
		double xs, ys, zs;
		xs = 0;
		ys = 0;
		zs = 0;
		int cnt = 0;
		for (Node n : g.getNodes()) {
			cnt++;
			xs += AttributeHelper.getPositionX(n);
			ys += AttributeHelper.getPositionY(n);
			zs += AttributeHelper.getPositionZ(n, false);
		}
		double x = xs / cnt / div;
		double y = ys / cnt / div;
		double z = zs / cnt / div;
		return x + "," + y + "," + z;
	}
	
	private String getPovRayNodeDesc(Node n, String nodeStyle, String nodePigment) {
		if (nodeStyle.indexOf("\r\n") < 0 && nodeStyle.indexOf("\n") < 0)
			nodeStyle = StringManipulationTools.stringReplace(nodeStyle, "\n", "\r\n");
		
		return "sphere" + LS +
							"{" + LS +
							"  <" + getX(n) + "," + getY(n) + "," + getZ(n) + ">, " + getWidth(n) + LS +
							"  texture " + LS +
							"  {" + LS +
							"    pigment" + LS +
							"    {   " + LS +
							"      color rgb <" + getColor(n) + ">" + LS +
							nodePigment +
							"    }" + LS +
							"  }" + LS +
							nodeStyle +
							"}" + LS;
	}
	
	private String getPovRayEdgeDesc(Edge e, String edgeStyle, String edgePigment) {
		if (e.getSource() == e.getTarget()) {
			return "";
		}
		if (edgeStyle.indexOf("\r\n") < 0 && edgeStyle.indexOf("\n") < 0)
			edgeStyle = StringManipulationTools.stringReplace(edgeStyle, "\n", "\r\n");
		return "cylinder \r\n" +
							"  {\r\n" +
							"    <" + getPositionDesc(e.getSource()) + ">, <" + getPositionDesc(e.getTarget()) + ">, " + getWidth(e) + "\r\n" +
							"    texture \r\n" +
							"      {\r\n" +
							"        pigment \r\n" +
							"          {\r\n" +
							"            color rgb <" + getColor(e) + ">\r\n" +
							edgePigment +
							"          }\r\n" +
							"      }\r\n" +
							edgeStyle +
							"  }\r\n";
	}
	
	private String getColor(Node ge) {
		Color c = AttributeHelper.getFillColor(ge);
		return (c.getRed() / 255d) + "," + (c.getGreen() / 255d) + "," + (c.getBlue() / 255d);
	}
	
	private String getColor(Edge ge) {
		Color c = AttributeHelper.getOutlineColor(ge);
		return (c.getRed() / 255d) + "," + (c.getGreen() / 255d) + "," + (c.getBlue() / 255d);
	}
	
	private String getWidth(Edge e) {
		return "" + AttributeHelper.getFrameThickNess(e);
	}
	
	private String getPositionDesc(Node n) {
		return getX(n) + "," + getY(n) + "," + getZ(n);
	}
	
	private String getWidth(Node n) {
		return "" + AttributeHelper.getWidth(n);
	}
	
	private String getX(Node n) {
		return AttributeHelper.getPositionX(n) + "";
	}
	
	private String getY(Node n) {
		return AttributeHelper.getPositionY(n) + "";
	}
	
	private String getZ(Node n) {
		String z = AttributeHelper.getPositionZ(n, false) + "";
		// System.out.println(z);
		return z;
	}
	
	public String[] getExtensions() {
		return new String[] { ".pov" };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "Povray" };
	}
	
}
