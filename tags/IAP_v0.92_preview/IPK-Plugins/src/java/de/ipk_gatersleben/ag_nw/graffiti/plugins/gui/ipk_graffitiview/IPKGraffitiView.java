/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// ==============================================================================
//
// GraffitiView.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: IPKGraffitiView.java,v 1.2 2012-11-07 14:47:56 klukas Exp $

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.Release;
import org.ReleaseInfo;
import org.Vector2df;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.options.OptionPane;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.DrawMode;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.plugins.views.defaults.NodeComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors.ClusterColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.DefaultContextMenuManager;

/**
 * An implementation of <code>org.graffiti.plugin.view.View2D</code>, that
 * displays a graph. Since it also shows changes in the graph it listens for
 * changes in the graph, attributes, nodes and edges.
 * 
 * @see javax.swing.JPanel
 * @see org.graffiti.plugin.view.View2D
 */
public class IPKGraffitiView
					extends GraffitiView
					implements OptionPane, Printable, PaintStatusSupport {
	
	private static final long serialVersionUID = 1L;
	
	ClusterBackgroundDraw gcbd = new ClusterBackgroundDraw();
	boolean dirty = true;
	
	private BackgroundTaskStatusProviderSupportingExternalCall optStatus;
	
	private static boolean useAntialiasing = true;
	
	public IPKGraffitiView() {
		super();
		// GravistoService.getInstance().addKnownOptionPane(IPKGraffitiView.class, this);
		setBorder(null);
	}
	
	@Override
	public void setGraph(Graph g) {
		super.setGraph(g);
		dirty = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugins.views.defaults.GraffitiView#createNodeComponent(java.util.Map, org.graffiti.graph.Node)
	 */
	@Override
	protected NodeComponent createNodeComponent(Map<GraphElement, GraphElementComponent> gecMap, Node node) {
		
		NodeComponent nodeComponent = (NodeComponent) getGraphElementComponent(node);
		
		if (isHidden(node)) {
			if (nodeComponent != null)
				processNodeRemoval(node);
			return null;
		}
		
		if (nodeComponent == null) {
			nodeComponent = (NodeComponent) gecMap.get(node);
			if (nodeComponent == null)
				nodeComponent = new IPKnodeComponent(node);
			// nodeComponent = IPKnodeComponent.getNewAndMatchingNodeComponent(node, currentGraph);
		}
		
		nodeComponent.clearDependentComponentList();
		gecMap.put(node, nodeComponent);
		
		return nodeComponent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		synchronized (this) {
			
			RepaintManager repaintManager = RepaintManager.currentManager(this);
			repaintManager.setDoubleBufferingEnabled(true);
			// useAntialiasing = true;
			// if (useAntialiasing) {
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
								RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			
			// } else {
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_OFF);
			// ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
			// RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			// }
			
			super.paintComponent(g);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		// long startTime=System.currentTimeMillis();
		// System.out.println("paint ("+g.getClipBounds().x+" : "+g.getClipBounds().y+" | "+g.getClipBounds().width+" : "+g.getClipBounds().height+")");
		
		// g.setColor(Color.ORANGE);
		if (!printInProgress)
			((Graphics2D) g).transform(zoom);
		
		// for (int i=0; i<10; i++) {
		// g.drawLine(i*xs, 0, i*xs, 500);
		// g.drawLine(0, i*xs, 500, i*xs);
		// }
		
		if (!printInProgress && drawMode != DrawMode.REDUCED)
			drawBackground(g);
		
		super.paint(g);
		// lastPaintTime=System.currentTimeMillis()-startTime;
		// if (lastPaintTime>maxDrawTime) {
		// System.err.println("Redraw time: "+lastPaintTime+"ms");
		// }
	}
	
	private void drawBackground(Graphics g) {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR &&
							AttributeHelper.hasAttribute(getGraph(), ClusterColorAttribute.attributeFolder, ClusterColorAttribute.attributeName)) {
			Boolean enablebackground = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "", "background_coloring", new Boolean(false), new Boolean(false),
								true);
			Boolean clearOuter = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_fill_outer_region", new Boolean(false),
								new Boolean(false), true);
			Boolean spaceFill = (Boolean) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_space_fill", new Boolean(true), new Boolean(true),
								true);
			if (enablebackground) {
				Double radius = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_radius", new Double(200), new Double(200), true);
				Double alpha = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_low_alpha", new Double(0.2), new Double(0.2), true);
				Double grid = (Double) AttributeHelper.getAttributeValue(getGraph(), "", "clusterbackground_grid", new Double(50), new Double(50), true);
				drawClusterBackground(g, g.getClipBounds(), grid.intValue(), !clearOuter, radius.intValue(), alpha.floatValue(), spaceFill);
			}
		}
	}
	
	private void drawClusterBackground(Graphics g, Rectangle clipBounds, float mesh, boolean outClear, int out,
						float lowAlpha, boolean spaceFill) {
		if (lowAlpha < 0f)
			lowAlpha = 0f;
		if (lowAlpha > 1f)
			lowAlpha = 1f;
		if (out < 0)
			out = 0;
		// int xs = 50;
		// g.setColor(Color.BLUE);
		// for (int i=0; i<10; i++) {
		// g.drawLine(i*xs, 0, i*xs, 500);
		// g.drawLine(0, i*xs, 500, i*xs);
		// }
		// g.drawString("paint ("+g.getClipBounds().x+" : "+g.getClipBounds().y+" | "+g.getClipBounds().width+
		// " : "+g.getClipBounds().height+")",
		// 50, 50);
		
		if (dirty) {
			dirty = false;
			gcbd = new ClusterBackgroundDraw();
			gcbd.init(getGraph());
		}
		
		ClusterBackgroundDraw cbd = gcbd;
		
		boolean image = false;
		if (optStatus != null && image)
			optStatus.setCurrentStatusText2("Scan node positions...");
		if (clipBounds == null || printInProgress) {
			if (!printInProgress)
				clipBounds = new Rectangle(0, 0, (int) cbd.maxX + 500, (int) cbd.maxY + 500);
			if (mesh > 5)
				mesh = 5;
			image = true;
		}
		boolean meshResizeAllowed = true;
		if (mesh < 0) {
			mesh = -mesh;
			meshResizeAllowed = false;
		}
		if (mesh < 1)
			mesh = 1;
		float xstart = clipBounds.x - clipBounds.x % mesh;
		float ystart = clipBounds.y - clipBounds.y % mesh;
		float xend = clipBounds.x + clipBounds.width + mesh;
		float yend = clipBounds.y + clipBounds.height + mesh;
		long startTime = System.currentTimeMillis();
		
		if (optStatus != null && image)
			optStatus.setCurrentStatusText2("Draw colorized background...");
		
		for (float x = xstart; x <= xend + mesh; x = x + mesh) {
			if (optStatus != null && image)
				optStatus.setCurrentStatusValueFine((x - xstart) / (xend - xstart) * 100d);
			for (float y = ystart; y <= yend + mesh; y = y + mesh) {
				double minDistance = Double.MAX_VALUE;
				Node minNode = null;
				for (Map.Entry<Node, Vector2df> e : cbd.node2position.entrySet()) {
					Vector2df p = e.getValue();
					float dist = (float) Math.sqrt((p.x - x) * (p.x - x) + (p.y - y) * (p.y - y));
					if (dist < minDistance) { // && (cbd.node2cluster.get(e.getKey()).length()>0)) {
						minNode = e.getKey();
						minDistance = dist;
					}
				}
				if (minDistance > out && outClear) {
					double dL = x - cbd.minX;
					double dT = y - cbd.minY;
					double dR = cbd.maxX - x;
					double dB = cbd.maxY - y;
					if (minDistance > getPositiveMin(dL, dT, dR, dB))
						continue;
				}
				if (!spaceFill && minDistance > out)
					continue;
				
				Color targetColor = null;
				if (cbd.node2cluster.containsKey(minNode)) {
					String cluster = cbd.node2cluster.get(minNode);
					targetColor = cbd.cluster2color.get(cluster);
					if (targetColor == null)
						targetColor = getBackground();
				}
				
				if (targetColor != null) {
					if (lowAlpha < 1f)
						targetColor = getTargetColor(targetColor, minDistance, out, 1f, lowAlpha);
					g.setColor(targetColor);
					
					g.fillRect((int) (x - mesh / 2f), (int) (y - mesh / 2f), (int) mesh, (int) mesh);
				}
			}
			long currTime = System.currentTimeMillis();
			int maxTime = (mesh < 50 ? 20 : 100);
			if (currTime - startTime > maxTime && !image && meshResizeAllowed) {
				x -= mesh / 2 + 1;
				mesh = mesh * 2;
				startTime = System.currentTimeMillis();
				MainFrame.showMessage("Mesh size has been temporarily increased to " + mesh + " to improve performance.", MessageType.INFO);
			}
		}
		if (optStatus != null && image) {
			optStatus.setCurrentStatusValueFine(100);
			optStatus.setCurrentStatusText2("Painting network...");
		}
		
		// g.setColor(Color.BLUE);
		// g.drawRect((int)cbd.minX, (int)cbd.minY, (int)(cbd.maxX-cbd.minX), (int)(cbd.maxY-cbd.minY));
		
	}
	
	private Color getTargetColor(Color c, double dist, double maxd, float src, float tgt) {
		float alpha;
		if (dist < maxd)
			alpha = (float) ((maxd - dist) / maxd * (src - tgt) + tgt);
		else
			alpha = tgt;
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255));
	}
	
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		super.postAttributeChanged(e);
		// dirty = true;
	}
	
	@Override
	public void transactionFinished(TransactionEvent event, BackgroundTaskStatusProviderSupportingExternalCall status) {
		super.transactionFinished(event, status);
		dirty = true;
	}
	
	@Override
	public void close() {
		super.close();
		optStatus = null;
		gcbd = null;
	}
	
	private double getPositiveMin(double dl, double dt, double dr, double db) {
		double min = Double.MAX_VALUE;
		dl = Math.abs(dl);
		dt = Math.abs(dt);
		dr = Math.abs(dr);
		db = Math.abs(db);
		if (dl < min)
			min = dl;
		if (dt < min)
			min = dt;
		if (dr < min)
			min = dr;
		if (db < min)
			min = db;
		return min;
	}
	
	public static boolean getUseAntialiasingSetting() {
		return useAntialiasing;
	}
	
	@Override
	public JPopupMenu getComponentPopupMenu() {
		if(!MegaTools.wasScrollPaneMovement())
			return new DefaultContextMenuManager().getContextMenu(
							MegaTools.getLastMouseE());
		return  null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getComponent()
	 */
	public JComponent getOptionDialogComponent() {
		JPanel options = new JPanel();
		
		double border = 5;
		double[][] size =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{
												border, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border }
		}; // Rows
		
		options.setLayout(new TableLayout(size));
		
		JCheckBox checkBoxUseAntiAliasing = new JCheckBox("Use Anti-Aliasing", useAntialiasing);
		
		options.putClientProperty("checkBoxUseAntiAliasing", checkBoxUseAntiAliasing);
		
		options.add(checkBoxUseAntiAliasing, "1,1");
		options.revalidate();
		
		return options;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#init()
	 */
	public void init(JComponent options) {
		JCheckBox checkBoxUseAntiAliasing = (JCheckBox) options.getClientProperty("checkBoxUseAntiAliasing");
		checkBoxUseAntiAliasing.setSelected(useAntialiasing);
	}
	
	@Override
	public String getViewName() {
		return getOptionName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#save()
	 */
	public void save(JComponent options) {
		JCheckBox checkBoxUseAntiAliasing = (JCheckBox) options.getClientProperty("checkBoxUseAntiAliasing");
		useAntialiasing = checkBoxUseAntiAliasing.isSelected();
		GravistoService.getInstance().getMainFrame().repaint();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getCategory()
	 */
	public String getCategory() {
		return "View";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getOptionName()
	 */
	public String getOptionName() {
		return "Graph Network View (default)";
	}
	
	private boolean printInProgress = false;
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		synchronized (this) {
			System.out.println("Printing Page " + pageIndex + ", Page Orientation: " + pageFormat.getOrientation());
			Graphics2D g2 = (Graphics2D) graphics;
			Dimension d = getViewComponent().getPreferredSize();
			d.width = (int) (d.width / getZoom().getScaleX());
			d.height = (int) (d.height / getZoom().getScaleY());
			double panelWidth = d.width; // width in pixels
			double panelHeight = d.height; // height in pixels
			double pageHeight = pageFormat.getImageableHeight(); // height of printer page
			double pageWidth = pageFormat.getImageableWidth(); // width of printer page
			double scale1 = pageWidth / panelWidth;
			double scale2 = pageHeight / panelHeight;
			double scale = min2(scale1, scale2);
			int numPages = 1; // (int) Math.ceil(scale * panelHeight / pageHeight);
			int response;
			if (pageIndex >= numPages) {
				response = NO_SUCH_PAGE;
			} else {
				g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				g2.translate(0f, -pageIndex * pageHeight);
				g2.scale(scale, scale);
				printInProgress = true;
				paint(g2);
				printInProgress = false;
				response = Printable.PAGE_EXISTS;
			}
			return response;
		}
	}
	
	/**
	 * @param smallestX
	 *           Value 1
	 * @param cx
	 *           Value 2
	 * @return The smaller one of the parameters
	 */
	private double min2(double smallestX, double cx) {
		return smallestX < cx ? smallestX : cx;
	}
	
	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		this.optStatus = optStatus;
	}
	
	public boolean statusDrawInProgress() {
		return optStatus != null;
	}
}
