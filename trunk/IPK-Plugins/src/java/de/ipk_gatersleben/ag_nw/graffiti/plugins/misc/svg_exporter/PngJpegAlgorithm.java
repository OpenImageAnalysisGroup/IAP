/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.Vector2d;
import org.graffiti.attributes.Attributable;
import org.graffiti.editor.ConfigureViewAction;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.LoadSetting;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.ManagerManager;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.session.EditorSession;
import org.graffiti.util.InstanceCreationException;
import org.w3c.dom.NodeList;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view.FastView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.PaintStatusSupport;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 * @version $Revision: 1.10 $
 */
public class PngJpegAlgorithm extends AbstractAlgorithm implements
		NeedsSwingThread {
	
	private PngJpegAlgorithmParams parameter = null;
	
	public PngJpegAlgorithm(boolean jpg) {
		super();
		parameter = new PngJpegAlgorithmParams();
		parameter.setCreateJPG(jpg);
	}
	
	public String getName() {
		if (parameter.isCreateJPG())
			return "Create JPG image";
		else
			return "Create PNG image";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public void check() throws PreconditionException {
		PngJpegAlgorithm.checkZoom();
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	// private static String getImageMessage() {
	// return ""; //
	// "While creating a bitmap image from the current view, the <u>current zoom</u> level influences<br>"
	// //+
	// "the image size. Increase the zoom level, to get higher resolution images.";
	// }
	//
	// private void infoMsg() {
	// MainFrame.getInstance().showMessageDialog("<html>"
	// + "<b>HTML Image Map creation</b><br>"
	// +
	// "If enabled, a HTML image map containg code for the display of the created PNG image is created.<br>"
	// +
	// "Nodes with a assigned reference URL will be processed, so that HTML links point to the referenced<br>"
	// +
	// "resources. Use the command <u>Elements/Set Reference Information URL</u> to assign URLs to graph<br>"
	// +
	// "elements. The following reference information is considered in this order:<br><ol>"
	// +
	// "<li>Reference Information defined by the command Elements/Set Reference Information URL<br>"
	// +
	// "<li>A (file) link to another graph file, defined by the command Elements/Link Network File<br>"
	// +
	// "The file extension in the link is replaced by '.html', to enable linking to other HTML image maps.<br>"
	// + "<li>KEGG Reference Information URL (for KEGG Pathways)</ol>"
	// //+ "<br>" + // "<br>" + "<b>Size of view</b><br>" + getImageMessage()
	// + "<br></small>", "Info");
	// }
	
	@Override
	public Parameter[] getParameters() {
		
		BooleanParameter bpTransparency = new BooleanParameter(
				parameter.useTransparency(),
				"Enable transparency support",
				"<html>"
						+ "If enabled, transparent images are created (if window background color is white).");
		bpTransparency.setLeftAligned(true);
		
		BooleanParameter bpCreateHTMLmap = new BooleanParameter(
				parameter.isCreateHTMLmap(),
				"Create HTML image map",
				"<html>"
						+ "If enabled, a HTML page containing code for the display<br>"
						+ "of the created bitmap image is created.<br>"
						+ "Reference URL links are created, if they are defined for graph elements.");
		bpCreateHTMLmap.setLeftAligned(true);
		
		BooleanParameter bpIncludeURL = new BooleanParameter(
				parameter.isIncludeURLinTooltip(),
				"Include URLs in tooltips",
				"<html>"
						+ "If enabled, the link alt text, which is shown by some internet browsers when hovering over links<br>"
						+ "will include the target URL.");
		bpIncludeURL.setLeftAligned(true);
		
		BooleanParameter bpIncludeTooltip = new BooleanParameter(
				parameter.isIncludeTooltip(),
				"Include tooltip text",
				"<html>"
						+ "If enabled, the user defined node tooltip text is included in the output.");
		bpIncludeTooltip.setLeftAligned(true);
		
		BooleanParameter bpCustomURLtarget = new BooleanParameter(parameter
				.isCustomTarget(), "Open HTML Link in new Window",
				"<html>If enabled, web-links are opened in a new window.");
		bpCustomURLtarget.setLeftAligned(true);
		
		bpCreateHTMLmap.addDependentParameters(new BooleanParameter[] {
				bpIncludeURL, bpIncludeTooltip, bpCustomURLtarget });
		
		JComponentParameter scaleJComponent = new JComponentParameter(
				getImageSizeSetting(), "", null);
		scaleJComponent.setLeftAligned(true);
		
		return new Parameter[] { scaleJComponent,
				parameter.isCreateJPG() ? null : bpTransparency, bpCreateHTMLmap,
				bpIncludeURL, bpIncludeTooltip, bpCustomURLtarget };
	}
	
	private JComponent getImageSizeSetting() {
		final JRadioButton radioZoom = new JRadioButton("Variable (Zoom)",
				parameter.getScaleSetting() == SizeSetting.ZOOM);
		final JRadioButton radioFixed = new JRadioButton(
				"Fixed Resolution (Pixels)",
				parameter.getScaleSetting() == SizeSetting.FIXED);
		final JRadioButton radioDPI = new JRadioButton("Fixed Resolution (DPI)",
				parameter.getScaleSetting() == SizeSetting.DPI);
		ButtonGroup bg = new ButtonGroup();
		bg.add(radioZoom);
		bg.add(radioFixed);
		bg.add(radioDPI);
		
		final JComboBox zoomLevels = new JComboBox(SizeSettingZoom.values());
		zoomLevels.setSelectedItem(parameter.getScaleZoomSetting());
		zoomLevels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parameter.setScaleZoomSetting((SizeSettingZoom) zoomLevels
						.getSelectedItem());
			}
		});
		
		final JComboBox widthOrHeight = new JComboBox(new String[] { "Width:",
				"Height:" });
		if (!parameter.isScaleFixedUseWidth())
			widthOrHeight.setSelectedIndex(1);
		
		widthOrHeight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parameter
						.setScaleFixedUseWidth(widthOrHeight.getSelectedIndex() == 0);
			}
		});
		final JSpinner widthHeightField = new JSpinner(new SpinnerNumberModel(
				parameter.getScaleFixedUseWidthOrHeightValue(), 50, 10000, 50));
		widthHeightField.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				parameter
						.setScaleFixedUseWidthOrHeightValue((Integer) widthHeightField
								.getValue());
			}
		});
		final JComponent WxHsettings = TableLayout.get3Split(widthOrHeight,
				new JLabel(), widthHeightField, TableLayout.PREFERRED, 0,
				TableLayout.FILL);
		
		final JSpinner spinnerPrintSize = new JSpinner(new SpinnerNumberModel(
				parameter.getScaleDPIprintSize(), 1, 237, 1));
		spinnerPrintSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				parameter.setScaleDPIprintSize((Integer) spinnerPrintSize
						.getValue());
			}
		});
		final JSpinner spinnerDPI = new JSpinner(new SpinnerNumberModel(parameter
				.getScaleDPIprintDPI(), 10, 1200, 10));
		spinnerDPI.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				parameter.setScaleDPIprintDPI((Integer) spinnerDPI.getValue());
			}
		});
		final JComboBox dpiSizeMeasureCombo = new JComboBox(SizeSettingDPIunit
				.values());
		dpiSizeMeasureCombo.setSelectedItem(parameter.getScaleDPIprintSizeUnit());
		dpiSizeMeasureCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parameter
						.setScaleDPIprintSizeUnit((SizeSettingDPIunit) dpiSizeMeasureCombo
								.getSelectedItem());
			}
		});
		
		final JComponent dpiSettings = TableLayout.get3Split(TableLayout
				.get3Split(new JLabel("Print measure:"), null, spinnerPrintSize,
						TableLayout.PREFERRED, 5, TableLayout.PREFERRED), null,
				TableLayout.get3Split(dpiSizeMeasureCombo, new JLabel(" DPI:"),
						spinnerDPI, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED), TableLayout.PREFERRED, 0,
				TableLayout.PREFERRED);
		
		checkRadioState(radioZoom, radioFixed, radioDPI, zoomLevels, WxHsettings,
				dpiSettings);
		
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkRadioState(radioZoom, radioFixed, radioDPI, zoomLevels,
						WxHsettings, dpiSettings);
			}
		};
		radioZoom.addActionListener(al);
		radioFixed.addActionListener(al);
		radioDPI.addActionListener(al);
		
		JComponent rr = new JPanel(new TableLayout(new double[][] {
				{ TableLayout.PREFERRED, 5, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED, 1, TableLayout.PREFERRED, 1,
						TableLayout.PREFERRED } }));
		
		rr.add(radioZoom, "0,0");
		rr.add(zoomLevels, "2,0");
		rr.add(radioFixed, "0,2");
		rr.add(WxHsettings, "2,2");
		rr.add(radioDPI, "0,4");
		rr.add(dpiSettings, "2,4");
		
		rr.setBorder(BorderFactory.createTitledBorder("Output Size"));
		
		return TableLayout.getSplitVertical(new JLabel(), rr, 5,
				TableLayout.PREFERRED);
	}
	
	private void checkRadioState(final JRadioButton radioZoom,
			final JRadioButton radioFixed, final JRadioButton radioDPI,
			final JComboBox zoomLevels, final JComponent WxHsettings,
			final JComponent dpiSettings) {
		zoomLevels.setVisible(radioZoom.isSelected());
		WxHsettings.setVisible(radioFixed.isSelected());
		dpiSettings.setVisible(radioDPI.isSelected());
		
		if (radioZoom.isSelected())
			parameter.setScaleSetting(SizeSetting.ZOOM);
		if (radioFixed.isSelected())
			parameter.setScaleSetting(SizeSetting.FIXED);
		if (radioDPI.isSelected())
			parameter.setScaleSetting(SizeSetting.DPI);
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		
		i++;
		
		if (parameter.isCreateJPG()) {
			i++;
			parameter.setUseTransparency(false);
		} else
			parameter.setUseTransparency(((BooleanParameter) params[i++])
					.getBoolean());
		parameter.setCreateHTMLmap(((BooleanParameter) params[i++]).getBoolean());
		parameter.setIncludeURLinTooltip((((BooleanParameter) params[i++])
				.getBoolean()));
		parameter.setIncludeTooltip((((BooleanParameter) params[i++])
				.getBoolean()));
		parameter.setCustomTarget(((BooleanParameter) params[i++]).getBoolean());
		
		if (parameter.getScaleSetting() == SizeSetting.FIXED) {
			if (parameter.isScaleFixedUseWidth()) {
				parameter.setMaxWidth(parameter
						.getScaleFixedUseWidthOrHeightValue());
				parameter.setMaxHeight(-1);
			} else {
				parameter.setMaxWidth(-1);
				parameter.setMaxHeight(parameter
						.getScaleFixedUseWidthOrHeightValue());
			}
		} else {
			parameter.setMaxWidth(-1);
			parameter.setMaxHeight(-1);
		}
	}
	
	public void setParams(PngJpegAlgorithmParams p) {
		parameter = p;
	}
	
	// @Test
	// public void testPNGexportForGraph() {
	// try {
	// UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// Graph g = new AdjListGraph();
	// g.setName("/home/hendrik/Desktop/test.gml");
	// Node n1 =
	// g.addNode(AttributeHelper.getDefaultGraphicsAttributeForKeggNode(100,
	// 100));
	// Node n2 =
	// g.addNode(AttributeHelper.getDefaultGraphicsAttributeForKeggNode(200,
	// 200));
	// g.addEdge(n1, n2, true);
	// PngJpegAlgorithm.createPNGimageFromGraph(g);
	// }
	
	String targetString = null;
	
	ActionEvent lastEvent = null;
	String lastFolder = null;
	
	private boolean askBeforeOverwrite = true;
	
	private boolean processMultipleViews;
	
	public Collection<String> lastLinks;
	
	public Collection<String> storedLinks;
	
	private de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter.LinkProcessor linkProcessor;
	
	private boolean saveScripts;
	
	public static void createPNGimageFromGraph(Graph g) {
		PngJpegAlgorithm a = new PngJpegAlgorithm(false);
		ObjectRef resultView = new ObjectRef();
		a.attach(g, null);
		a.execute(g, replaceExtension(g.getName(true), "png"), "png",
				BufferedImage.TYPE_INT_RGB, resultView, null);
	}
	
	public static BufferedImage createPNGimageFromGraphGetBI(Graph g, int maxW, int maxH) {
		PngJpegAlgorithmParams settings = new PngJpegAlgorithmParams();
		settings.setScaleSetting(SizeSetting.FIXED);
		settings.setMaxWidth(maxW);
		settings.setMaxHeight(maxW);
		return PngJpegAlgorithm.getActiveGraphViewImage(g, BufferedImage.TYPE_INT_ARGB, "png", settings, null, false);
	}
	
	public static void createPNGimageFromGraph(Graph g,
			String fullPathAndFileName, PngJpegAlgorithmParams settings) {
		PngJpegAlgorithm a = new PngJpegAlgorithm(false);
		a.setParams(settings);
		a.setAskBeforeOverwrite(false);
		ObjectRef resultView = new ObjectRef();
		a.attach(g, null);
		a.execute(g, replaceExtension(fullPathAndFileName, "png"), "png",
				BufferedImage.TYPE_INT_RGB, resultView, null);
	}
	
	public void execute() {
		
		ObjectRef refLastEvent = new ObjectRef();
		refLastEvent.setObject(lastEvent);
		
		ObjectRef refLastFolder = new ObjectRef();
		refLastFolder.setObject(lastFolder);
		
		String fileName;
		if (parameter.isCreateJPG())
			fileName = PngJpegAlgorithm.getCachedFileName("jpg", graph, this,
					refLastEvent, refLastFolder);
		else
			fileName = PngJpegAlgorithm.getCachedFileName("png", graph, this,
					refLastEvent, refLastFolder);
		
		if (fileName == null)
			return;
		
		lastEvent = (ActionEvent) refLastEvent.getObject();
		lastFolder = (String) refLastFolder.getObject();
		
		ObjectRef resultView = new ObjectRef();
		
		if (parameter.useTransparency() && !parameter.isCreateJPG())
			execute(graph, fileName, "png", BufferedImage.TYPE_4BYTE_ABGR,
					resultView, null);
		else
			execute(graph, fileName, parameter.isCreateJPG() ? "jpg" : "png",
					BufferedImage.TYPE_INT_RGB, resultView, null);
		
	}
	
	/**
	 * @param name
	 * @param string
	 * @return
	 */
	public static String replaceExtension(String name, String newExt) {
		if (name != null)
			if (name.indexOf(".") > 0)
				name = name.substring(0, name.lastIndexOf(".")) + "." + newExt;
		return name;
	}
	
	// private void writeEdge(double zoom, String pre, TextFile stream, Edge
	// edge,
	// Component edgeComponent) {
	// if (edgeComponent == null)
	// return;
	// int x = (int) (edgeComponent.getLocation().x * zoom);
	// int y = (int) (edgeComponent.getLocation().y * zoom);
	// int width = (int) (edgeComponent.getWidth() * zoom);
	// int height = (int) (edgeComponent.getHeight() * zoom);
	//
	// String title = getLabel(edge);
	// String href = getHRef(edge);
	// String alt = title;
	// int x1 = x - (width / 2);
	// int y1 = y - (height / 2);
	// int x2 = x + (width / 2);
	// int y2 = y + (height / 2);
	//
	// String coords = x1 + "," + y1 + "," + x2 + "," + y2;
	//
	// AttributeHelper.setToolTipText(edge, pre + "<area shape=\"rect\" " + href
	// + " title=\"" + title + "\" alt=\"" + alt + "\" coords=\"" + coords
	// + "\"/>");
	//
	// if (href != null && href.length() > 0)
	// stream.add(pre + "<area shape=\"rect\" " + href + " title=\"" + title
	// + "\" alt=\"" + alt + "\" coords=\"" + coords + "\"/>");
	// }
	
	private void writeNode(double zoom, String pre, TextFile stream, Node node,
			boolean backgroundURL) {
		
		String shape = AttributeHelper.getShape(node);
		
		if (shape.equals("org.graffiti.plugins.views.defaults.EllipseNodeShape")) {
			writeEllipse(zoom, pre, stream, node, backgroundURL);
			return;
		}
		if (shape.equals("org.graffiti.plugins.views.defaults.CircleNodeShape")) {
			writeCircle(zoom, pre, stream, node, backgroundURL);
			return;
		}
		if (shape
				.equals("org.graffiti.plugins.views.defaults.RectangleNodeShape")) {
			writeRectangle(zoom, pre, stream, node, backgroundURL);
			return;
		}
		writeRectangle(zoom, pre, stream, node, backgroundURL);
	}
	
	private void writeRectangle(double zoom, String pre, TextFile stream,
			Node node, boolean backgroundURL) {
		int x = (int) (AttributeHelper.getPositionX(node) * zoom);
		int y = (int) (AttributeHelper.getPositionY(node) * zoom);
		int width = (int) (AttributeHelper.getWidth(node) * zoom);
		int height = (int) (AttributeHelper.getHeight(node) * zoom);
		
		String title = getLabel(node);
		String href = getHRef(node);
		String alt = title;
		int x1 = x - (width / 2);
		int y1 = y - (height / 2);
		int x2 = x + (width / 2);
		int y2 = y + (height / 2);
		
		String coords = x1 + "," + y1 + "," + x2 + "," + y2;
		if (href != null && href.length() > 0)
			stream.add(pre + "<area shape=\"rect\" " + href + " title=\"" + title
					+ "\" alt=\"" + alt + "\" coords=\"" + coords + "\"/>");
		else
			if (backgroundURL)
				stream.add(pre + "<area shape=\"rect\" nohref=\"true\" coords=\""
						+ coords + "\"/>");
	}
	
	private void writeEllipse(double zoom, String pre, TextFile stream,
			Node node, boolean backgroundURL) {
		int x = (int) (AttributeHelper.getPositionX(node) * zoom);
		int y = (int) (AttributeHelper.getPositionY(node) * zoom);
		int width = (int) (AttributeHelper.getWidth(node) * zoom);
		int height = (int) (AttributeHelper.getHeight(node) * zoom);
		
		String title = getLabel(node);
		String href = getHRef(node);
		String alt = title;
		
		String coords = getEllipseCoords(x, y, width / 2, height / 2);
		
		if (href != null && href.length() > 0)
			stream.add(pre + "<area shape=\"poly\" " + href + " title=\"" + title
					+ "\" alt=\"" + alt + "\" coords=\"" + coords + "\"/>");
		else
			if (backgroundURL)
				stream.add(pre + "<area shape=\"poly\" nohref=\"true\" coords=\""
						+ coords + "\"/>");
	}
	
	private String getEllipseCoords(int x, int y, int a, int b) {
		StringBuilder sb = new StringBuilder();
		for (double alpha = 0; alpha < Math.PI * 2; alpha += Math.PI / 2 / 5) {
			if (sb.length() > 0)
				sb.append(",");
			double px = x + a * Math.cos(alpha);
			double py = y + b * Math.sin(alpha);
			int pxi = (int) px;
			int pyi = (int) py;
			sb.append(pxi + ",");
			sb.append(pyi);
		}
		return sb.toString();
	}
	
	private void writeCircle(double zoom, String pre, TextFile stream,
			Node node, boolean backgroundURL) {
		int x = (int) (AttributeHelper.getPositionX(node) * zoom);
		int y = (int) (AttributeHelper.getPositionY(node) * zoom);
		int width = (int) (AttributeHelper.getWidth(node) * zoom);
		
		String title = getLabel(node);
		String href = getHRef(node);
		String alt = title;
		
		int r = width / 2;
		
		String coords = x + "," + y + "," + r;
		
		if (href != null && href.length() > 0)
			stream.add(pre + "<area shape=\"circle\" " + href + " title=\""
					+ title + "\" alt=\"" + alt + "\" coords=\"" + coords + "\"/>");
		else
			if (backgroundURL)
				stream.add(pre + "<area shape=\"circle\" nohref=\"true\" coords=\""
						+ coords + "\"/>");
	}
	
	private String getHRef(Attributable ge) {
		String u = AttributeHelper.getReferenceURL(ge);
		if (u == null || u.length() <= 0) {
			if (ge instanceof GraphElement)
				u = AttributeHelper.getPathwayReference(ge);
			if (u != null && u.indexOf(".") > 0) {
				u = u.substring(0, u.lastIndexOf(".")) + ".html";
			}
			if (u == null || u.length() <= 0) {
				if (ge instanceof Node)
					u = KeggGmlHelper.getKeggLinkUrl((Node) ge);
			}
		}
		
		if (u != null && u.length() > 0 && linkProcessor != null)
			u = linkProcessor.getProcessedLink(u);
		
		if (u != null && u.length() > 0 && this.targetString != null
				&& this.targetString.length() > 0)
			return "href=\"" + u + "\"" + this.targetString;
		else
			if (u != null && u.length() > 0)
				return "href=\"" + u + "\"";
			else
				return u;
	}
	
	private String getLabel(GraphElement ge) {
		String lbl = AttributeHelper.getLabel(ge, "");
		if (lbl.length() > 0) {
			lbl = StringManipulationTools.stringReplace(lbl, "<br>", "&#013");
			lbl = StringManipulationTools.removeHTMLtags(lbl);
			if (parameter.isIncludeURLinTooltip())
				lbl = lbl + " | ";
		}
		if (parameter.isIncludeURLinTooltip())
			lbl = lbl + getHRef(ge);
		if (parameter.isIncludeTooltip() && (ge instanceof Node)) {
			String tt = AttributeHelper.getToolTipText((Node) ge);
			if (tt != null && tt.length() > 0) {
				tt = StringManipulationTools.stringReplace(tt, "<br>", "&#013");
				tt = StringManipulationTools.removeHTMLtags(tt);
				tt = tt.trim();
				lbl = lbl + " | " + tt;
				if (lbl.startsWith(" | "))
					lbl = lbl.substring(" | ".length());
			}
			// String tt = IPKnodeComponent.getTooltipForNode((Node)ge, false);
			// if (tt!=null) {
			// tt = ErrorMsg.stringReplace(tt, "<br>", "&#013");
			// tt = ErrorMsg.stringReplace(tt, "</td>", "&#013");
			// tt = ErrorMsg.stringReplace(tt, "</th>", "&#013");
			// tt = ErrorMsg.stringReplace(tt, "<p>", "&#013");
			// tt = ErrorMsg.stringReplace(tt, "</table>", "&#013");
			// while (tt.indexOf("&#013&#013")>=0) {
			// tt = ErrorMsg.stringReplace(tt, "&#013&#013", "&#013");
			// }
			// tt = ErrorMsg.removeHTMLtags(tt);
			// tt = tt.trim();
			// lbl = lbl+" | "+tt;
			// }
		}
		return lbl;
	}
	
	public void execute(final Graph targetGraph, final String filenameMain,
			final String imageType, final int imageTypeVal, ObjectRef resultView,
			final ImageFileResultProcessor imageFileResultProcessor) {
		if (filenameMain == null)
			return;
		
		if (parameter.isCustomTarget()) {
			if (parameter.getCustomTarget() == null)
				targetString = " target=\"_blank\"";
			else
				targetString = " target=\"" + parameter.getCustomTarget() + "\"";
		} else {
			targetString = null;
		}
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Create Image File...", "Please wait");
		
		status.setCurrentStatusText2("Draw Image");
		Collection<BufferedImageResult> result = getActiveGraphViewImage(
				targetGraph, imageTypeVal, imageType, status);
		for (BufferedImageResult res : result) {
			BufferedImage bi = res.getBufferedImage();
			resultView.setObject(res.getView());
			
			if (bi == null)
				return;
			
			double scale = res.getScale();
			
			String filename = filenameMain;
			
			if (processMultipleViews && result.size() > 1) {
				filename = filename.substring(0, filename.length()
						- ".png".length())
						+ ".view_"
						+ res.getView().getClass().getSimpleName()
						+ ".png";
			}
			
			// save the image File
			System.out.println("Generating image " + filename);
			
			res.setFileName(filename);
			
			File file = new File(filename);
			
			try {
				status.setCurrentStatusText2("Write file to disk...");
				
				if (parameter.getScaleSetting() == SizeSetting.DPI) {
					
					// if (parameter.isCreateJPG()) {
					// FileOutputStream os = new FileOutputStream(file);
					// JPEGImageEncoder jpegEncoder = JPEGCodec
					// .createJPEGEncoder(os);
					// JPEGEncodeParam jpegEncodeParam = jpegEncoder
					// .getDefaultJPEGEncodeParam(bi);
					// // jpegEncodeParam
					// // .setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
					// jpegEncodeParam.setXDensity(parameter.getScaleDPIprintDPI());
					// jpegEncodeParam.setYDensity(parameter.getScaleDPIprintDPI());
					// jpegEncodeParam.setQuality(1f, false);
					// jpegEncoder.encode(bi, jpegEncodeParam);
					// } else {
					writePngFile(bi, file.getAbsolutePath(), parameter
							.getScaleDPIprintDPI());
					// }
				} else {
					// if (parameter.isCreateJPG()) {
					// FileOutputStream os = new FileOutputStream(file);
					// JPEGImageEncoder jpegEncoder = JPEGCodec
					// .createJPEGEncoder(os);
					// JPEGEncodeParam jpegEncodeParam = jpegEncoder
					// .getDefaultJPEGEncodeParam(bi);
					// jpegEncodeParam.setQuality(1f, false);
					// jpegEncoder.encode(bi, jpegEncodeParam);
					// } else
					ImageIO.write(bi, imageType, file);
				}
				
				status.setCurrentStatusValueFine(100d);
				status.setCurrentStatusText2("File has been created");
				MainFrame.showMessage("Created " + file.getAbsolutePath() + " ("
						+ file.length() / 1024 + " KB)", MessageType.INFO);
				if (imageFileResultProcessor != null)
					imageFileResultProcessor.processCreatedImageFile(file);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			
			createHTMLifRequested(filename, resultView, scale,
					imageFileResultProcessor);
		}
		if ((processMultipleViews) || (storedLinks != null)) {
			String fn = filenameMain;
			fn = StringManipulationTools.stringReplace(filenameMain, ".png",
					".tabbed.html");
			Collection<String> links = new ArrayList<String>();
			for (BufferedImageResult r : result)
				links.add(replaceExtension(new File(r.getFileName()).getName(),
						"html"));
			if (storedLinks != null)
				links.addAll(storedLinks);
			if (saveScripts)
				PngJpegAlgorithm.saveScriptCode(fn);
			PngJpegAlgorithm.createTabHTMLfor(fn, new File(fn).getName(), links);
			if (imageFileResultProcessor != null)
				imageFileResultProcessor.processCreatedTabWebsiteFile(new File(fn));
			
			this.lastLinks = links;
		}
	}
	
	private void createHTMLifRequested(String fileName, ObjectRef resultView,
			double scale, ImageFileResultProcessor imageFileResultProcessor) {
		if (parameter.isCreateHTMLmap()) {
			String ofn = fileName;
			if (!parameter.isCreateJPG()) {
				if (fileName.toUpperCase().endsWith(".PNG"))
					fileName = fileName.substring(0, fileName.length()
							- ".png".length());
			} else {
				if (fileName.toUpperCase().endsWith(".JPG"))
					fileName = fileName.substring(0, fileName.length()
							- ".JPG".length());
				fileName = fileName.substring(0, fileName.length()
						- ".JPEG".length());
			}
			String mapFileName = fileName + ".html";
			if (new File(mapFileName).exists() && isAskBeforeOverwrite()) {
				Object[] res = MyInputHelper.getInput("File already exists:<br>"
						+ "<b>" + mapFileName + "</b><br><br>" + "Overwrite?",
						"File exists", new Object[] {});
				if (res == null)
					return;
			}
			View view = (View) resultView.getObject();
			
			// if (g2d!=null && g2d.getTransform().getScaleX() < 1)
			TextFile out = new TextFile();
			try {
				ofn = new File(ofn).getName();
				if (!processMultipleViews && !ofn.contains("full.tab."))
					ofn = StringManipulationTools.stringReplace(ofn, "gml.full.png",
							"gml.full.tab.png");
				String gn = graph.getName();
				gn = StringManipulationTools.UnicodeToHtml(gn);
				out.add("<html>");
				out.add("<head>");
				out.add("\t<title>Graph: " + gn + " (" + graph.getNumberOfNodes()
						+ " nodes, " + graph.getNumberOfEdges() + " edges)</title>");
				out.add("</head>");
				out.add("<body >");
				out.add("\t<img src=\"" + ofn + "\" border=\"0\" usemap=\"#" + gn
						+ "\">");
				out.add("\t<map id=\"" + gn + "\" name=\"" + gn + "\">");
				
				String defaultURL = getHRef(graph);
				boolean backgroundURL = defaultURL != null
						&& defaultURL.length() > 0;
				// if (view!=null)
				// for (Edge e : graph.getEdges()) {
				// writeEdge(zoom, "\t\t", out, e, view.getComponentForElement(e));
				// }
				
				if (view != null && view instanceof GraffitiView) {
					GraffitiView gv = (GraffitiView) view;
					for (GraphElement ge : gv.getSortedGraphElements(true)) {
						if (ge instanceof Node) {
							writeNode(scale, "\t\t", out, (Node) ge, backgroundURL);
						}
					}
				} else {
					for (Node n : graph.getNodes()) {
						writeNode(scale, "\t\t", out, n, backgroundURL);
					}
				}
				
				out.add("\t</map>");
				out.add("</body>");
				out.add("</html>");
				out.write(mapFileName);
				if (imageFileResultProcessor != null)
					imageFileResultProcessor.processCreatedWebsiteFile(new File(
							mapFileName));
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	private static void writePngFile(RenderedImage image, String filename,
			int dotsPerInch) {
		
		String dotsPerMeter = String.valueOf((int) (dotsPerInch / 0.0254) + 1);
		
		Iterator<ImageWriter> imageWriters = ImageIO
				.getImageWritersByFormatName("png");
		
		while (imageWriters.hasNext()) {
			ImageWriter iw = imageWriters.next();
			
			ImageWriteParam iwp = iw.getDefaultWriteParam();
			IIOMetadata metadata = iw.getDefaultImageMetadata(
					new ImageTypeSpecifier(image), iwp);
			
			String pngFormatName = metadata.getNativeMetadataFormatName();
			IIOMetadataNode pngNode = (IIOMetadataNode) metadata
					.getAsTree(pngFormatName);
			
			IIOMetadataNode physNode = null;
			NodeList childNodes = pngNode.getElementsByTagName("pHYs");
			if (childNodes.getLength() == 0) {
				physNode = new IIOMetadataNode("pHYs");
				pngNode.appendChild(physNode);
			} else
				if (childNodes.getLength() == 1) {
					physNode = (IIOMetadataNode) childNodes.item(0);
				} else {
					ErrorMsg
							.addErrorMessage("Internal Error: found multiple pHYs nodes");
				}
			
			physNode.setAttribute("pixelsPerUnitXAxis", dotsPerMeter);
			physNode.setAttribute("pixelsPerUnitYAxis", dotsPerMeter);
			physNode.setAttribute("unitSpecifier", "meter");
			
			try {
				metadata.setFromTree(pngFormatName, pngNode);
				IIOImage iioImage = new IIOImage(image, null, metadata);
				File file = new File(filename);
				ImageOutputStream ios = ImageIO.createImageOutputStream(file);
				iw.setOutput(ios);
				iw.write(iioImage);
				ios.flush();
				ios.close();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				continue;
			}
			
			break;
		}
		
	}
	
	public static String getCachedFileName(String extension, Graph graph,
			Algorithm algorithm, ObjectRef lastEvent, ObjectRef lastFolder) {
		String f = null;
		if (algorithm.getActionEvent() != null
				&& algorithm.getActionEvent() == lastEvent.getObject()) {
			f = (String) lastFolder.getObject()
					+ "/"
					+ PngJpegAlgorithm.replaceExtension(graph.getName(false),
							extension);
			if (new File(f).exists()) {
				if (JOptionPane.showConfirmDialog(GravistoService.getInstance()
						.getMainFrame(),
						"<html>Do you want to overwrite the existing file <i>" + f
								+ "</i>?</html>", "Overwrite File?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					// empty
				} else {
					f = null;
					lastEvent = null;
				}
			}
		}
		if (f == null || !new File((String) lastFolder.getObject()).exists()) {
			f = FileHelper.getFileName(extension, "Image File", PngJpegAlgorithm
					.replaceExtension(graph.getName(false), extension));
		}
		
		try {
			lastEvent.setObject(algorithm.getActionEvent());
			if (f != null)
				lastFolder.setObject(new File(f).getParent());
			
			if (f != null
					&& !f.endsWith(PngJpegAlgorithm.replaceExtension(graph
							.getName(false), extension))) {
				lastEvent.setObject(null);
				lastFolder.setObject(null);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return f;
	}
	
	public static void checkZoom() throws PreconditionException {
		
	}
	
	public static Rectangle getViewRectFromSelection(View view,
			Collection<GraphElement> graphElements) {
		return getViewRectFromSelection(view, graphElements, false);
	}
	
	public static Rectangle getViewRectFromSelection(View view,
			Collection<GraphElement> graphElements, boolean print) {
		Rectangle viewRect = null;
		for (GraphElement ge : graphElements) {
			if (AttributeHelper.isHiddenGraphElement(ge))
				continue;
			GraphElementComponent gvc = view.getComponentForElement(ge);
			if (gvc == null)
				continue;
			if (viewRect == null)
				viewRect = gvc.getBounds();
			else
				viewRect.add(gvc.getBounds());
			if (gvc != null)
				for (Object o : gvc.getAttributeComponents()) {
					if (o instanceof JComponent) {
						JComponent jc = (JComponent) o;
						if (viewRect == null)
							viewRect = jc.getBounds();
						else
							viewRect.add(jc.getBounds());
					}
				}
		}
		// if (ReleaseInfo.getRunningReleaseStatus()!=Release.KGML_EDITOR) {
		// Boolean enablebackground =
		// (Boolean)AttributeHelper.getAttributeValue(view.getGraph(), "",
		// "background_coloring", new Boolean(false), new Boolean(false), true);
		// if (enablebackground) {
		// Double radius =
		// (Double)AttributeHelper.getAttributeValue(view.getGraph(), "",
		// "clusterbackground_radius", new Double(200), new Double(200), true);
		// viewRect.add(viewRect.x+viewRect.width+radius,
		// viewRect.y+viewRect.height+radius);
		// }
		// }
		// if (print)
		// viewRect.add(0, 0);
		if (viewRect == null)
			viewRect = new Rectangle(100, 100);
		
		return viewRect;
	}
	
	public Collection<BufferedImageResult> getActiveGraphViewImage(
			final Graph targetGraph, final int imageType,
			final String fileExtension,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return getActiveGraphViewImage(targetGraph, imageType, fileExtension, optStatus, false);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<BufferedImageResult> getActiveGraphViewImage(
			final Graph targetGraph, final int imageType,
			final String fileExtension,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			boolean allowBackgroundThread) {
		if (allowBackgroundThread || SwingUtilities.isEventDispatchThread())
			return getActiveGraphViewImageOnSwingThread(targetGraph, imageType,
					fileExtension, optStatus);
		else {
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						Object o = getActiveGraphViewImageOnSwingThread(targetGraph,
								imageType, fileExtension, optStatus);
						tso.setParam(0, o);
					}
				});
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (InvocationTargetException e) {
				ErrorMsg.addErrorMessage(e);
			}
			return (Collection<BufferedImageResult>) tso.getParam(0, null);
		}
	}
	
	private Collection<BufferedImageResult> getActiveGraphViewImageOnSwingThread(
			Graph targetGraph, int imageType, String fileExtension,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		
		EditorSession session = GravistoService.getInstance()
				.getSessionFromGraph(targetGraph);
		View view = null;
		if (session == null) {
			session = new EditorSession(targetGraph);
			MainFrame mf = MainFrame.getInstance();
			if (mf == null) {
				if (SystemAnalysis.isHeadless()) {
					session = GravistoService.getInstance().getSessionFromGraph(targetGraph);
					if (session == null) {
						String[] views = new String[] { "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView" };
						
						String viewName = views[0];
						try {
							view = ManagerManager.getInstance(null).viewManager.createView(viewName);
							view.setAttributeComponentManager(ManagerManager.getInstance(null).attributeComponentManager);
							view.setGraph(targetGraph);
						} catch (InstanceCreationException e) {
							ErrorMsg.addErrorMessage("Could not create view " + viewName + ". Error: " + e.getLocalizedMessage());
							return null;
						}
					} else {
						view = session.getActiveView();
					}
				}
			} else
				mf = new MainFrame();
			if (mf != null) {
				JScrollPane sp = mf.showViewChooserDialog(session, true, null,
						LoadSetting.VIEW_CHOOSER_NEVER_SHOW_DONT_ADD_VIEW_TO_EDITORSESSION,
						new ConfigureViewAction() {
							private View v;
							
							public void run() {
								((IPKGraffitiView) v).threadedRedraw = false;
							}
							
							public void storeView(View v) {
								this.v = v;
							}
						});
				view = (View) sp.getViewport().getView();
			}
		} else {
			view = session.getActiveView();
		}
		
		Collection<BufferedImageResult> res = new ArrayList<BufferedImageResult>();
		
		Collection<View> views = new ArrayList<View>();
		if (processMultipleViews) {
			EditorSession es = (EditorSession) MainFrame.getInstance()
					.getEditorSessionForGraph(view.getGraph());
			if (es != null)
				views.addAll(es.getViews());
		}
		if (views.size() == 0)
			views.add(view);
		
		for (View vi : views) {
			Container v = ((JComponent) vi).getParent();
			Color backCol = new Color(-1);
			if (v != null)
				backCol = v.getBackground();
			
			ObjectRef scale = new ObjectRef();
			BufferedImage bi = createImageFromView(imageType, fileExtension, vi,
					backCol, optStatus, scale);
			if (bi != null) {
				BufferedImageResult r = new BufferedImageResult(bi, (Double) scale
						.getObject(), vi);
				res.add(r);
			}
		}
		
		return res;
	}
	
	private BufferedImage createImageFromView(int imageType,
			String fileExtension, View view, Color backColor,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ObjectRef scale) {
		Graph graph = view.getGraph();
		
		if (graph == null)
			return null;
		if (view instanceof PaintStatusSupport) {
			PaintStatusSupport pss = (PaintStatusSupport) view;
			if (pss.statusDrawInProgress()) {
				MainFrame.showMessageDialog(
						"Please wait until current drawing has completed.",
						"Image Creation in Progress");
				return null;
			}
		}
		
		JComponent viewerComponent = view.getViewComponent();
		
		// setDoubleBuffered(viewerComponent, false);
		
		Vector2d dimSrc;
		
		if (optStatus != null && (view instanceof PaintStatusSupport)) {
			PaintStatusSupport psp = (PaintStatusSupport) view;
			psp.setStatusProvider(optStatus);
		}
		double zoomScaleFromView = 1d;
		Rectangle viewRectangle = null;
		if (isViewOfTypeGraphView(view)) {
			// get window size
			/*
			 * dim = NodeTools.getMaximumXY(graph.getNodes(), 1.1, -10, -10, true);
			 */
			
			viewRectangle = getViewRectFromSelection(view, graph.getGraphElements());
			
			double dimx = viewRectangle.getX() + viewRectangle.getWidth() + (viewRectangle.getX() > 0 ? viewRectangle.getX() : 0);
			double dimy = viewRectangle.getY() + viewRectangle.getHeight() + (viewRectangle.getY() > 0 ? viewRectangle.getY() : 0);
			
			dimSrc = new Vector2d(dimx, dimy);
			Graphics2D g2d = (Graphics2D) viewerComponent.getGraphics();
			if (g2d != null) {
				// graph is opened, retrieve current zoom level and set zoom to 1:1,
				// will be restored later
				zoomScaleFromView = g2d.getTransform().getScaleX();
				if (view instanceof ZoomListener) {
					((ZoomListener) view).zoomChanged(new AffineTransform());
				}
				g2d.setTransform(new AffineTransform());
			} else
				zoomScaleFromView = 1d;
		} else {
			if (view instanceof FastView) {
				FastView fv = (FastView) view;
				int w = fv.getChartWidth();
				int h = fv.getChartHeight();
				dimSrc = new Vector2d(w, h);
			} else {
				ErrorMsg
						.addErrorMessage("Internal Error: Creating image for this view type is not supported!");
				return null;
			}
		}
		
		Vector2d dim = new Vector2d(dimSrc);
		double outputScale = setDimAccordingToOutputSize(dimSrc, dim,
				zoomScaleFromView);
		
		if (parameter.getClipX() != -1 && dim.x > parameter.getClipX()) {
			double scaleClip = parameter.getClipX() / dim.x + 0;
			dim.x = parameter.getClipX();
			dimSrc.x = scaleClip * dimSrc.x;
		}
		if (parameter.getClipY() != -1 && dim.y > parameter.getClipY()) {
			double scaleClip = parameter.getClipY() / dim.y;
			dim.y = parameter.getClipY();
			dimSrc.y = scaleClip * dimSrc.y;
		}
		
		BufferedImage bi = new BufferedImage((int) dim.x, (int) dim.y, imageType);
		Graphics2D g = bi.createGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		
		try {
			if (MegaTools.getActiveTool() != null)
				MegaTools.getActiveTool().preProcessImageCreation();
			
			// clear background with active window background color
			if (backColor.getRGB() != -1 || fileExtension.equalsIgnoreCase("jpg")
					|| bi.getTransparency() == BufferedImage.OPAQUE) {
				g.setColor(backColor);
				g.fillRect(0, 0, (int) dim.x, (int) dim.y);
			}
			g.setTransform(new AffineTransform(outputScale, 0, 0, outputScale, 0,
					0));
			
			// paint graph
			// boolean isOp = viewerComponent.isOpaque();
			// viewerComponent.setOpaque(false);
			// viewerComponent.paint(g);
			
			setDoubleBuffered(viewerComponent, false);
			Rectangle ob = viewerComponent.getBounds();
			viewerComponent.setBounds(0, 0, (int) dimSrc.x, (int) dimSrc.y);
			// viewerComponent.print(g);
			viewerComponent.paint(g);
			viewerComponent.setBounds(ob);
			setDoubleBuffered(viewerComponent, true);
			// viewerComponent.setOpaque(isOp);
			// setDoubleBuffered(viewerComponent, true);
			
			if (optStatus != null && (view instanceof PaintStatusSupport)) {
				PaintStatusSupport psp = (PaintStatusSupport) view;
				psp.setStatusProvider(null);
			}
		} finally {
			
			if (view instanceof ZoomListener
					&& Math.abs(zoomScaleFromView - 1) > 0.0001) {
				AffineTransform at = new AffineTransform(zoomScaleFromView, 0, 0,
						zoomScaleFromView, 0, 0);
				((ZoomListener) view).zoomChanged(at);
			}
			
			if (MegaTools.getActiveTool() != null)
				MegaTools.getActiveTool().postProcessImageCreation();
		}
		
		scale.setObject(outputScale);
		
		// // clip top-left border if possible
		// if (!parameter.isWithBorder() && viewRectangle != null && parameter.getClipX() < 0 && parameter.getClipY() < 0) {
		// if (viewRectangle.getX() > 0 && viewRectangle.getY() >= 0) {
		// int vx = (int) viewRectangle.getX();
		// int vy = (int) viewRectangle.getY();
		// int vw = (int) viewRectangle.getWidth();
		// int vh = (int) viewRectangle.getHeight();
		// System.out.println("AVAIL:  " + bi.getWidth() + " / " + bi.getHeight());
		// System.out.println("DESIRE: " + vx + " / " + vy + " : " + vw + " / " + vy);
		// bi = bi.getSubimage(vx, vy, vw, vh);
		// } else
		// if (viewRectangle.getX() <= 0 && viewRectangle.getY() <= 0)
		// ; // keep image as it is
		// else
		// if (viewRectangle.getX() <= 0)
		// bi = bi.getSubimage(0, (int) viewRectangle.getY(), (int) (viewRectangle.getWidth() + viewRectangle.getX()), (int) viewRectangle.getHeight());
		// else
		// if (viewRectangle.getY() <= 0)
		// bi = bi.getSubimage((int) viewRectangle.getX(), 0, (int) viewRectangle.getWidth(),
		// (int) (viewRectangle.getHeight() + viewRectangle.getY()));
		// }
		return bi;
	}
	
	/**
	 * @param dimSrc
	 *           Image size, drawn at 100% zoom
	 * @param dim
	 *           Resulting desired image size, dependent on scale settings.
	 *           (RETURN VALUE)
	 * @param zoomScaleFromView
	 *           Active zoom level of view (1 if not zoomed or not zoomable or
	 *           zoom level not available).
	 * @return Resulting needed scale factor, used later for setting the
	 *         transform of the output graphics.
	 */
	private double setDimAccordingToOutputSize(Vector2d dimSrc, Vector2d dim,
			double zoomScaleFromView) {
		switch (parameter.getScaleSetting()) {
			case ZOOM:
				return processScaleZoom(dimSrc, dim, zoomScaleFromView);
			case FIXED:
				return processScaleFixed(dimSrc, dim);
			case DPI:
				return processScaleDPI(dimSrc, dim);
			default:
				ErrorMsg.addErrorMessage("Internal Error: Unknown Scale Setting");
				dim.x = dimSrc.x;
				dim.y = dimSrc.y;
				return 1;
		}
	}
	
	/**
	 * Processes scaleDPIprintSize, scaleDPIprintSizeUnit and scaleDPIprintDPI to
	 * ensure that the output when scaled to the desired print size meets the
	 * desired DPI count.
	 * 
	 * @param dimSrc
	 *           Input image size (100% zoom level).
	 * @param dim
	 *           Output image size will be set according to desired settings.
	 * @return Resulting needed graphics scale level to fill the output image.
	 */
	private double processScaleDPI(Vector2d dimSrc, Vector2d dim) {
		double targetPrintSize = parameter.getScaleDPIprintSize();
		double targetPrintSizeInch;
		switch (parameter.getScaleDPIprintSizeUnit()) {
			case mm:
				targetPrintSizeInch = targetPrintSize / 25.4d;
				break;
			case cm:
				targetPrintSizeInch = targetPrintSize / 2.54d;
				break;
			case inch:
				targetPrintSizeInch = targetPrintSize;
				break;
			default:
				ErrorMsg
						.addErrorMessage("Internal Error: Unknown DPI output size unit.");
				targetPrintSizeInch = targetPrintSize;
				break;
		}
		
		int minimumOutputSize = (int) (targetPrintSizeInch * parameter
				.getScaleDPIprintDPI());
		double scale;
		if (dimSrc.x > dimSrc.y) {
			scale = minimumOutputSize / dimSrc.x;
			dim.x = minimumOutputSize;
			dim.y = dimSrc.y * scale;
		} else {
			scale = minimumOutputSize / dimSrc.y;
			dim.x = dimSrc.x * scale;
			dim.y = minimumOutputSize;
		}
		return scale;
	}
	
	private double processScaleFixed(Vector2d dimSrc, Vector2d dim) {
		double desiredScaleWidth = Double.MAX_VALUE;
		double desiredScaleHeight = Double.MAX_VALUE;
		if (parameter.getMaxWidth() > 0) {
			if (parameter.getMaxHeight() == -1)
				dim.x = dimSrc.x < parameter.getMaxWidth() ? parameter
						.getMaxWidth() : dimSrc.x;
			else
				dim.x = parameter.getMaxWidth();
			desiredScaleWidth = dim.x / dimSrc.x;
		}
		if (parameter.getMaxHeight() > 0) {
			dim.y = parameter.getMaxHeight();
			desiredScaleHeight = parameter.getMaxHeight() / dimSrc.y;
		}
		double scale = desiredScaleWidth < desiredScaleHeight ? desiredScaleWidth
				: desiredScaleHeight;
		if (parameter.getMaxWidth() <= 0) {
			dim.x = dimSrc.x * scale;
		}
		if (parameter.getMaxHeight() <= 0) {
			dim.y = dimSrc.y * scale;
		}
		return scale;
	}
	
	private double processScaleZoom(Vector2d dimSrc, Vector2d dim,
			double zoomScaleFromView) {
		double scale = parameter.getScaleZoomSetting()
				.getScale(zoomScaleFromView);
		dim.x = dimSrc.x * scale;
		dim.y = dimSrc.y * scale;
		return scale;
	}
	
	/**
	 * @param viewerComponent
	 */
	public static void setDoubleBuffered(JComponent jc, boolean val) {
		jc.setDoubleBuffered(false);
		Component[] comps = jc.getComponents();
		
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JComponent)
				setDoubleBuffered((JComponent) comps[i], val);
		}
	}
	
	public static boolean isViewOfTypeGraphView(View theView) {
		if (theView instanceof GraffitiView)
			return true;
		else
			return false;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	public static BufferedImage getActiveGraphViewImage(Graph targetGraph,
			int imageType, String fileExtension, PngJpegAlgorithmParams params,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		return getActiveGraphViewImage(targetGraph, imageType, fileExtension, params, optStatus, false);
	}
	
	public static BufferedImage getActiveGraphViewImage(Graph targetGraph,
			int imageType, String fileExtension, PngJpegAlgorithmParams params,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus, boolean allowBackgroundThread) {
		PngJpegAlgorithm alg = new PngJpegAlgorithm(fileExtension.equals("jpg"));
		alg.parameter = params;
		return alg.getActiveGraphViewImage(targetGraph, imageType, fileExtension,
				optStatus, allowBackgroundThread).iterator().next().getBufferedImage();
	}
	
	public void setAskBeforeOverwrite(boolean askBeforeOverwrite) {
		this.askBeforeOverwrite = askBeforeOverwrite;
	}
	
	public boolean isAskBeforeOverwrite() {
		return askBeforeOverwrite;
	}
	
	/**
	 * @param mappingName
	 * @param storedLinks2
	 * @param string
	 */
	public static void createHTMLfor(String outputFileName, String mappingName,
			Collection<String> links) {
		outputFileName = replaceExtension(outputFileName, "html");
		String ht = outputFileName;
		TextFile out = new TextFile();
		try {
			outputFileName = new File(outputFileName).getName();
			outputFileName = replaceExtension(outputFileName, "png");
			String gn = mappingName;
			gn = StringManipulationTools.UnicodeToHtml(gn);
			out.add("<html>");
			out.add("<head>");
			out.add("\t<title>" + gn + "</title>");
			out.add(getTabImportCodeAndSaveScripts(outputFileName));
			out.add("</head>");
			out.add("<body >");
			
			out.add("<div id=\"tabs\">");
			out.add("<ul>");
			out.add("<li><a href=\"#img\"><span>" + gn + "</span></a></li>");
			for (String l : links) {
				String ln = l;
				boolean isImport = false;
				if (ln.indexOf("MetadataView") >= 0) {
					isImport = true;
				}
				boolean isMapping = false;
				if (ln.indexOf("MappingView") >= 0) {
					isMapping = true;
				}
				ln = StringManipulationTools.UnicodeToHtml(ln.substring(0,
						ln.indexOf(".")).replaceAll("_", " "));
				if (isImport)
					ln += " (Metadata View)";
				else
					if (isMapping)
						ln += " (Mapping View)";
				
				out.add("<li><a href=\"" + l + "\"><span>" + ln
						+ "</span></a></li>");
			}
			out.add("</ul>");
			out.add("<div id=\"img\"><img src=\"" + outputFileName
					+ "\" border=\"0\"></div>");
			out.add("</body>");
			out.add("</html>");
			out.write(ht);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static void createTabHTMLfor(String fn, String title,
			Collection<String> links) {
		TextFile out = new TextFile();
		try {
			String gn = StringManipulationTools.UnicodeToHtml(title);
			out.add("<html>");
			out.add("<head>");
			out.add("\t<title>" + gn + "</title>");
			out.add(getTabImportCodeAndSaveScripts(fn));
			out.add("</head>");
			out.add("<body >");
			out.add("<div id=\"tabs\">");
			out.add("<ul>");
			for (String l : links) {
				if (l.endsWith(".gml.html")) {
					l = l.substring(0, l.length() - ".gml.html".length()) + ".html";
				}
				l = StringManipulationTools.stringReplace(l, "", "%20");
				String ln = l;
				boolean isImport = false;
				if (ln.indexOf("MetadataView") >= 0) {
					isImport = true;
				}
				boolean isMapping = false;
				if (ln.indexOf("MappingView") >= 0) {
					isMapping = true;
				}
				
				ln = StringManipulationTools.stringReplace(ln, ".html", "");
				if (ln.indexOf(".") > 0 && !isImport && !isMapping)
					ln = ln.substring(ln.lastIndexOf(".") + 1);
				if (ln.indexOf(".") > 0)
					ln = StringManipulationTools.UnicodeToHtml(ln.substring(0,
							ln.indexOf(".")).replaceAll("_", " "));
				else
					ln = StringManipulationTools.UnicodeToHtml(ln.replaceAll("_",
							" "));
				if (isImport)
					ln += " (Metadata View)";
				else
					if (isMapping)
						ln += " (Mapping View)";
				
				out.add("<li><a href=\"" + l + "\"><span>" + ln
						+ "</span></a></li>");
			}
			out.add("</ul>");
			out.add("</div>");
			out.add("</body>");
			out.add("</html>");
			out.write(fn);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static void saveScriptCode(String outputFileName) {
		String path = new File(new File(outputFileName).getParent())
				.getAbsolutePath();
		if (!new File(path + "/jquery").exists())
			new File(path + "/jquery").mkdirs();
		
		if (!new File(path + "/jquery/jquery.zip").exists()) {
			save("res", "jquery.zip", path + "/jquery/jquery.zip");
			try {
				GravistoService.unzipFile(new File(path + "/jquery/jquery.zip"));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
	
	private static String getTabImportCodeAndSaveScripts(String outputFileName) {
		return "  <link type=\"text/css\" href=\"jquery/base/ui.all.css\" rel=\"stylesheet\" />"
				+ "<script type=\"text/javascript\" src=\"jquery/jquery-1.3.2.js\"></script>"
				+ "<script type=\"text/javascript\" src=\"jquery/ui/ui.core.js\"></script>"
				+ "<script type=\"text/javascript\" src=\"jquery/ui/ui.tabs.js\"></script>"
				+ "<script type=\"text/javascript\">"
				+ "$(document).ready(function(){"
				+ "$(\"#tabs\").tabs();"
				+ "$('#tabs ul li a').click(function () {location.hash = $(this).attr('href');window.scroll(0,0);});"
				+ "});" + "</script>";
	}
	
	private static void save(String folder, String fileName,
			String targetFileName) {
		ClassLoader cl = PngJpegAlgorithm.class.getClassLoader();
		
		String path = PngJpegAlgorithm.class.getPackage().getName().replace('.',
				'/');
		try {
			File tgt = new File(targetFileName);
			FileOutputStream out = new FileOutputStream(tgt);
			long sz = 0;
			InputStream inpS = cl.getResourceAsStream(path + "/" + folder + "/"
					+ fileName);
			InputStream in = inpS;
			
			int b;
			while ((b = inpS.read()) != -1) {
				out.write(b);
				sz++;
			}
			sz = sz / 1024;
			in.close();
			out.close();
		} catch (FileNotFoundException err) {
			ErrorMsg.addErrorMessage(err);
		} catch (IOException err) {
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	public void setProcessMultipleViews(boolean b) {
		this.processMultipleViews = b;
	}
	
	public void addLinkProcessor(LinkProcessor linkProcessor) {
		this.linkProcessor = linkProcessor;
	}
	
	/**
	 * @param b
	 */
	public void setSaveScripts(boolean b) {
		this.saveScripts = b;
	}
	
}
