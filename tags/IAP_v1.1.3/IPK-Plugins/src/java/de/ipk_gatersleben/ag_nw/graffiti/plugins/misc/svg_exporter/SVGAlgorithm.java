/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.swing.JComponent;

import org.ErrorMsg;
import org.Vector2d;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugin.view.AbstractView;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view.FastView;

/**
 * The print algorithm.
 * 
 * @author Christian Klukas
 * @version $Revision: 1.1 $
 */
public class SVGAlgorithm
					extends AbstractAlgorithm
					implements NeedsSwingThread {
	private boolean addBorder = false;
	private static String filename;
	
	/**
	 * Empty contructor.
	 */
	public SVGAlgorithm() {
		// Do nothing than calling inherit contructor.
		super();
	}
	
	public SVGAlgorithm(boolean addBorder) {
		// Do nothing than calling inherit contructor.
		this();
		this.addBorder = addBorder;
	}
	
	/**
	 * Returns the display name (in menu area) for this plugin.
	 * <p>
	 * If graffiti sometimes supports mutiple languages, this method have to be refactored.
	 * </p>
	 * 
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create SVG image";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	/**
	 * Unused for this plugin.
	 * 
	 * @throws PreconditionException
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	@Override
	public void check() throws PreconditionException {
		PngJpegAlgorithm.checkZoom();
	}
	
	public void execute() {
		filename = GravistoService.getInstance().getMainFrame().getActiveEditorSession().getGraph().getName();
		if (filename != null)
			filename = PngJpegAlgorithm.replaceExtension(filename, "svg");
		execute(FileHelper.getFileName("svg", "Image File", filename));
	}
	
	public void execute(String filename) {
		if (filename == null)
			return;
		SVGAlgorithm.filename = filename;
		EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
		
		View theView = session.getActiveView();
		
		JComponent viewerComponent = theView.getViewComponent();
		
		PngJpegAlgorithm.setDoubleBuffered(viewerComponent, false);
		
		SVGGraphics2D svgGenerator = null;
		Exception last = null;
		for (int i = 1; i <= 5; i++) {
			try {
				svgGenerator = paintSVG(theView, viewerComponent);
				break;
			} catch (Exception e) {
				last = e;
				System.err.println("Run " + i + " exception: ");
				e.printStackTrace();
			}
		}
		if (svgGenerator == null) {
			MainFrame.showMessageDialog("SVG file could not be created", "Error");
			if (last != null)
				ErrorMsg.addErrorMessage(last);
			return;
		}
		
		boolean useCSS = false; // we want to use CSS style attribute
		
		try {
			String charSet = null;
			if (Charset.isSupported("UTF8"))
				charSet = "UTF8";
			else
				charSet = "UTF-8";
			
			System.out.print("Output charset is " + charSet + "... ");
			
			FileOutputStream fos = new FileOutputStream(filename);
			
			Writer out2 = new OutputStreamWriter(fos, charSet);
			
			try {
				svgGenerator.stream(out2, useCSS);
			} catch (IOException e4) {
				ErrorMsg.addErrorMessage(e4);
			}
			
			try {
				out2.close();
			} catch (IOException e3) {
				ErrorMsg.addErrorMessage(e3.getLocalizedMessage());
			}
		} catch (UnsupportedEncodingException e1) {
			ErrorMsg.addErrorMessage(e1);
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		PngJpegAlgorithm.setDoubleBuffered(viewerComponent, true);
	}
	
	private SVGGraphics2D paintSVG(View theView, JComponent viewerComponent) throws Exception {
		// Get a DOMImplementation
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		
		// Create an instance of org.w3c.dom.Document
		Document document = domImpl.createDocument(null, "svg", null);
		
		// Create an instance of the SVG Generator
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		
		Vector2d dim;
		if (PngJpegAlgorithm.isViewOfTypeGraphView(theView)) {
			Rectangle r = PngJpegAlgorithm.getViewRectFromSelection(theView, graph.getGraphElements());
			
			Graphics2D g2d = (Graphics2D) viewerComponent.getGraphics();
			
			g2d.setClip(0, 0, r.width, r.height);
			Point2D newDimP = g2d.getTransform().transform(
								new Point2D.Double(r.getMaxX() + (addBorder && r.getX() > 0 ? (int) r.getX() : 0), r.getMaxY()
													+ (addBorder && r.getY() > 0 ? (int) r.getY() : 0)), null);
			dim = new Vector2d(newDimP.getX(), newDimP.getY());
		} else {
			if (theView instanceof FastView) {
				FastView fv = (FastView) theView;
				int w = fv.getChartWidth();
				int h = fv.getChartHeight();
				dim = new Vector2d(w, h);
			} else {
				dim = NodeTools.getMaximumXY(graph.getNodes(),
									1.1,
									-10,
									-10, true);
			}
		}
		
		svgGenerator.setSVGCanvasSize(new Dimension((int) dim.x, (int) dim.y));
		
		AbstractView view =
							(AbstractView) MainFrame.getInstance().getActiveSession().getActiveView();
		Container v = view.getParent();
		Color backCol = v.getBackground();
		if (backCol.getRGB() != -1) {
			svgGenerator.setColor(backCol);
			svgGenerator.fillRect(0, 0, (int) dim.x, (int) dim.y);
		}
		
		boolean ok = false;
		try {
			System.out.print("Paint SVG...");
			// viewerComponent.paint(svgGenerator);
			
			AbstractTool.getActiveTool().preProcessImageCreation();
			viewerComponent.print(svgGenerator);
			ok = true;
			System.out.println("OK");
		} finally {
			if (!ok)
				System.out.println("ERROR");
			AbstractTool.getActiveTool().postProcessImageCreation();
		}
		return svgGenerator;
	}
}
