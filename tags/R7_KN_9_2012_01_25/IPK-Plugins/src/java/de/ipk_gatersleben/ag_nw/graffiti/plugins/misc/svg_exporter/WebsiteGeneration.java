/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jul 13, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.color.ColorUtil;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NeedsSwingThread;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class WebsiteGeneration extends AbstractEditorAlgorithm implements
					NeedsSwingThread {
	
	private final String borderStyle = "frameborder=NO"; // "border=0 bordercolor=\"white\"";
	// // frameborder=NO
	// framescpacing=1
	// border=2
	
	private String colorStart = "#FFFFFF";
	private String colorEnd = "#ebebeb";
	private String colorFooterEnd = "#bbbbbb";
	private String colorDescription = "#FDFDFD";
	
	private final String colorBackgroundStart = "#8C8C8C";
	private final String colorBackgroundEnd = "#3F3F3F";
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	@SuppressWarnings("deprecation")
	public void execute() {
		final ArrayList<EditorSession> ws = new ArrayList<EditorSession>();
		for (EditorSession es : sortByFileName(MainFrame.getEditorSessions())) {
			ws.add(es);
		}
		final File selPath = OpenFileDialogService
							.getDirectoryFromUser("Select Output-Folder");
		if (selPath != null && selPath.isDirectory()) {
			ArrayList<Object> paramsA = new ArrayList<Object>();
			ArrayList<Object> paramsB = new ArrayList<Object>();
			ArrayList<Object> paramsC = new ArrayList<Object>();
			ArrayList<Object> paramsD = new ArrayList<Object>();
			paramsA.add("Website-Title//");
			paramsA.add("<h1>My Pathway Website</h1>");
			paramsB.add("Website-Description//");
			paramsB
								.add("VANTED graph to website export. VANTED is available at <a href=\"http://vanted.ipk-gatersleben.de\" target=\"_blank\">http://vanted.ipk-gatersleben.de</a>.");
			paramsC.add("//");
			paramsC.add("<small>Copyright (c) " + (new Date().getYear() + 1900)
								+ " by NAME OF INSTITUTION.</small>");
			paramsD.add("Header");
			paramsD.add(ColorUtil.getColorFromHex(colorStart));
			paramsD.add("Navigation");
			paramsD.add(ColorUtil.getColorFromHex(colorEnd));
			paramsD.add("Network Description");
			paramsD.add(ColorUtil.getColorFromHex(colorEnd));
			paramsD.add("Footer");
			paramsD.add(ColorUtil.getColorFromHex(colorFooterEnd));
			for (EditorSession es : ws) {
				paramsA.add("Graph " + es.getFileName());
				String fn = es.getFileName();
				if (fn.indexOf(".") > 0)
					fn = fn.substring(0, fn.lastIndexOf("."));
				paramsA.add(AttributeHelper.getAttributeValue(es.getGraph(), "",
									"title", fn, ""));
				
				paramsB.add("Graph " + es.getFileName() + "//");
				paramsB.add(AttributeHelper.getAttributeValue(es.getGraph(), "",
									"description", "", ""));
			}
			ArrayList<ArrayList<Object>> res = MyInputHelper
								.getMultipleInput(
													" Open a set of networks and choose this command to create a simple website, containing links to graphical representations<br>"
															+
															" of the currently opened network files.<br>"
															+
															"Use the menu command Edit/Add Link... to create links assigned to network nodes, which point to other networks or to<br>"
															+
															"websites. This way it is possible to create and publish your own library of interlinked network files, possibly with<br>"
															+
															"references to other website resources." +
															"<br>" +
															"<br>" +
															"Please specify the title for the whole website and for each loaded network:<br>"
																		+ "<br>"
																		+ "Hint: If the network titles contains a dot (.) character, "
																		+ "the networks will be put into categories.<br>"
																		+ "For example the title A.B would place the network "
																		+ "into the group 'A' sown with network name 'B'.<br><br>",
													"Titles", paramsA,
													"Please specify website and graph descriptions:<br><br>",
													"Descriptions", paramsB,
													"Please specify website footer:<br><br>", "Footer",
													paramsC, "Please specify the color scheme:",
													"Background-Colors", paramsD);
			
			if (res != null) {
				String websiteTitle = "", websiteDescription = "", footer = "";
				int idx = 0;
				for (Object o : res.get(0)) {
					String s = (String) o;
					if (idx == 0)
						websiteTitle = s;
					else
						AttributeHelper.setAttribute(ws.get(idx - 1).getGraph(), "",
											"title", s);
					idx++;
				}
				idx = 0;
				for (Object o : res.get(1)) {
					String s = (String) o;
					if (idx == 0)
						websiteDescription = s;
					else
						AttributeHelper.setAttribute(ws.get(idx - 1).getGraph(), "",
											"description", s);
					idx++;
				}
				idx = 0;
				for (Object o : res.get(2)) {
					String s = (String) o;
					if (idx == 0)
						footer = s;
					idx++;
				}
				idx = 0;
				for (Object o : res.get(3)) {
					if (idx == 0)
						colorStart = ColorUtil.getHexFromColor((Color) o);
					if (idx == 1)
						colorEnd = ColorUtil.getHexFromColor((Color) o);
					if (idx == 2)
						colorDescription = ColorUtil.getHexFromColor((Color) o);
					if (idx == 3)
						colorFooterEnd = ColorUtil.getHexFromColor((Color) o);
					idx++;
				}
				String fileNameMainDescription = "overview.html";
				
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
									"Generate Website", "Initialize...");
				final String fWebsiteTitle = websiteTitle;
				final String fWebsiteDescription = websiteDescription;
				final String fFooter = footer;
				final String fFileNameMainDescription = fileNameMainDescription;
				BackgroundTaskHelper.issueSimpleTask(getName(),
									"Generate Website...", new Runnable() {
										public void run() {
											try {
												generateWebsite(selPath, fWebsiteTitle,
																	fWebsiteDescription, fFooter, ws,
																	fFileNameMainDescription, status);
											} catch (IOException e) {
												ErrorMsg.addErrorMessage(e);
												MainFrame.showMessageDialog(
																	"Please select a valid output folder!",
																	"Error");
											}
										}
									}, new Runnable() {
										public void run() {
											AttributeHelper.showInBrowser(selPath.getAbsolutePath()
																+ File.separator + "index.html");
										}
									}, status);
			}
		} else {
			MainFrame.showMessageDialog("Please select a valid output folder!",
								"Error");
		}
	}
	
	private Collection<EditorSession> sortByFileName(
						Set<EditorSession> editorSessions) {
		TreeMap<String, EditorSession> names = new TreeMap<String, EditorSession>();
		for (EditorSession e : editorSessions)
			names.put(e.getFileName(), e);
		return names.values();
	}
	
	private void generateWebsite(File targetPath, String websiteTitle,
						String websiteDescription, String footer, ArrayList<EditorSession> ws,
						String fileNameMainDescription,
						BackgroundTaskStatusProviderSupportingExternalCall status)
						throws IOException {
		
		// writeSimpleWebsiteBody(targetPath, "index.html", websiteTitle,
		// "<div id=\"page\">§" +
		// "<iframe src=\"content.html\" frameborder=\"0\" style=\"height:90;width:90%;\"></iframe>§"
		// +
		// "</div>§",
		// "index");
		
		generateMainFrameSet(targetPath,
							"index.html", // "content.html",
				"navigation.html", "header.html", "footer.html", "overview.html",
							websiteTitle);
		
		writeNavigation(targetPath, "navigation.html", ws, websiteTitle,
							fileNameMainDescription);
		
		writeSimpleWebsiteBody(targetPath, "header.html", "Header", websiteTitle,
							"gradient");
		writeSimpleWebsiteBody(targetPath, "footer.html", "Footer", footer,
							"footer");
		
		writeSimple(
							targetPath,
							"style.css",
							"" + "body {§" + "  margin: 5;§" + "  padding: 5;§"
												+ "  font-family: Arial, Helvetica, sans-serif;§"
												+ "  background: "
												+ colorEnd
												+ ";§"
												+ "  scrollbar-base-color: "
												+ colorEnd
												+ ";§"
												+ "  scrollbar-arrow-color: "
												+ colorStart
												+ ";§"
												+ "  scrollbar-DarkShadow-Color: "
												+ colorFooterEnd
												+ ";§"
												+ "}§"
												+ "body.index {§"
												+ "  height: 100%;§"
												+ "  margin: 0;§"
												+ "  padding: 0§"
												+ "  border: none;§"
												+ "  text-align: center;"
												+ "  background: -moz-linear-gradient(0% 270deg, "
												+ colorBackgroundStart
												+ ", "
												+ colorBackgroundEnd
												+ ");§"
												+ "  background: -webkit-gradient(linear, left top, left bottom, to("
												+ colorBackgroundEnd
												+ "), from("
												+ colorBackgroundStart
												+ "));§"
												+ "  background-repeat: no-repeat;§"
												+ "  background-color: "
												+ colorBackgroundEnd
												+ ";§"
												+ "}§"
												+ "body.gradient {§"
												+ "  background: -moz-linear-gradient(0% 270deg, "
												+ colorStart
												+ ", "
												+ colorEnd
												+ ");§"
												+ "  background: -webkit-gradient(linear, left top, left bottom, to("
												+ colorEnd
												+ "), from("
												+ colorStart
												+ "));§"
												+ "  background-repeat: no-repeat;§"
												+ "  background-color: "
												+ colorEnd
												+ ";§"
												+ "  text-align: center;§"
												+ "}§"
												+ "body.gradient {§"
												+ "  text-align: center;§"
												+ "}§"
												+ "body.zoom {§"
												+ "  background: -moz-linear-gradient(0% 270deg, #FDFDFD, "
												+ colorEnd
												+ ");§"
												+ "  background: -webkit-gradient(linear, left top, left bottom, from("
												+ colorDescription
												+ "), to("
												+ colorEnd
												+ "));§"
												+ "  background-repeat: no-repeat;§"
												+ "}§"
												+ "body.description {§"
												+ "  background: "
												+ colorDescription
												+ ";§"
												+ "}§"
												+ "body.footer {§"
												+ "  background: -moz-linear-gradient(0% 270deg, "
												+ colorEnd
												+ ", "
												+ colorFooterEnd
												+ ");§"
												+ "  background: -webkit-gradient(linear, left top, left bottom, to("
												+ colorFooterEnd
												+ "), from("
												+ colorEnd
												+ "));§"
												+ "  background-repeat: no-repeat;§"
												+ "  background-color: "
												+ colorFooterEnd
												+ ";§"
												+ "}§"
												+ "body.navigation2 {§"
												+ "  background: "
												+ colorEnd
												+ ";§"
												+ "}§"
												+ "§"
												+ "a {§"
												+ "  text-decoration: none;§"
												+ "}§"
												+ "a.zoom {§"
												+ "  text-decoration: none;§"
												+ "}§"
												+ "#page {§"
												+ "  position:relative;§" + "  top: 2%;§" + "}§");
		
		StringBuilder iconTable = new StringBuilder();
		iconTable.append("<br><br>§" + "<table border=0>§");
		
		int index = 1, max = ws.size();
		int n3 = 0;
		status.setCurrentStatusValue(0);
		for (EditorSession es : ws) {
			if (n3 == 0)
				iconTable.append("<tr>§");
			status.setCurrentStatusText1("Generate images for");
			status.setCurrentStatusText2(es.getFileName());
			iconTable
								.append("<td valign=\"top\" align=\"center\" width=300 height=280>");
			exportGraphImages(targetPath, es,
								getWebsiteNameForGraph(es.getGraph()), iconTable, status,
								1d / max * 100d);
			iconTable.append("</td>");
			status.setCurrentStatusValueFine(100d * index / max);
			n3++;
			index++;
			if (n3 == 3) {
				n3 = 0;
				iconTable.append("</tr>§");
			}
		}
		
		iconTable.append("<table>§");
		
		writeSimpleWebsiteBody(targetPath, fileNameMainDescription, websiteTitle
							+ " (overview)", websiteDescription + "§" + iconTable.toString(),
							"overview");
		status.setCurrentStatusText1("Website generated");
		status.setCurrentStatusText2("One moment, opening result...");
	}
	
	private void writeSimpleWebsiteBody(File targetPath, String fileName,
						String title, String bodyContent, String optCssBodyClass)
						throws IOException {
		writeSimple(
							targetPath,
							fileName,
							""
												+ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">§"
												+ "<html>§"
												+ "<head>§"
												+ "	<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">§"
												+ "	<title>"
												+ StringManipulationTools
																	.UnicodeToHtml(StringManipulationTools
																						.removeHTMLtags(title)
																						+ " - main menu")
												+ "</title>§"
												+ "  <link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />§"
												+ "</head>§"
												+ "<body"
												+ (optCssBodyClass == null ? "" : " class=\""
																	+ optCssBodyClass + "\"") + ">§" + bodyContent
												+ "</body>§" + "</html>§");
	}
	
	private void writeNavigation(File targetPath, String fileNameNavigation,
						ArrayList<EditorSession> ws, String websiteTitle,
						String fileNameMainDescription) throws IOException {
		
		StringBuilder pathwayLinks = new StringBuilder();
		
		// read existing links
		TreeSet<String> titles = new TreeSet<String>();
		HashMap<String, String> fileName2title = new HashMap<String, String>();
		readAndProcessExistingGraphLinks(targetPath, fileNameNavigation, ws,
							titles, fileName2title);
		
		NavigationTree tree = new NavigationTree(titles, fileName2title);
		
		tree.outputLinks(pathwayLinks);
		
		writeSimpleWebsiteBody(
							targetPath,
							"feedback.html",
							"E-Mail Feedback",
							"<form action=\"mailto:\" method=\"post\" enctype=\"text/plain\">§"
												+ "E-Mail Feedback:<br><textarea name=\"Feedback\" id=\"Feedback\" rows=\"30\" cols=\"60\"></textarea>§"
												+ "<p><input type=\"submit\" value=\"Send Feedback\">§"
												+ "</form>", null);
		
		writeSimple(
							targetPath,
							fileNameNavigation,
							""
												+ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">§"
												+ "<html>§"
												+ "<head>§"
												+ "	<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">§"
												+ "	<title>"
												+ StringManipulationTools
																	.UnicodeToHtml(StringManipulationTools
																						.removeHTMLtags(websiteTitle)
																						+ " - main menu")
												+ "</title>§"
												+ "  <link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />§"
												+ "</head>§" + "<body class=\"navigation\">§" + "	<a href=\""
												+ fileNameMainDescription
												+ "\" target=\"main\">Overview</a><br><br>§"
												+ pathwayLinks.toString() + "§"
												+ "	<a href=\"feedback.html\" target=\"main\">Feedback</a>§"
												+ "</body>§" + "</html>§");
	}
	
	private void readAndProcessExistingGraphLinks(File targetPath,
						String fileNameNavigation, ArrayList<EditorSession> ws,
						TreeSet<String> titles, HashMap<String, String> fileName2title) {
		
		HashSet<String> fileNames = new HashSet<String>();
		try {
			TextFile nav = new TextFile(targetPath.getAbsolutePath()
								+ File.separator + fileNameNavigation);
			for (String n : nav) {
				// <li><a href="graph_2_4.html"
				// target="main">AFLB3&#00032;maturation</a>
				if (n.indexOf("graph_") > 0 && n.indexOf("target=\"main\">") > 0) {
					String fileName = n.substring(n.indexOf("graph_"));
					fileName = fileName.substring(0, fileName.indexOf("\""));
					String title = n
										.substring(n.indexOf("<!-- ") + "<!-- ".length());
					title = title.substring(0, title.indexOf(" -->"));
					title = StringManipulationTools.htmlToUnicode(title);
					titles.add(title);
					fileNames.add(fileName);
					fileName2title.put(fileName, title);
				}
			}
		} catch (Exception e) {
			// empty
		}
		int idx = 1;
		for (EditorSession es : ws) {
			String title = (String) AttributeHelper.getAttributeValue(es
								.getGraph(), "", "title", "Untitled Graph " + idx, "");
			String fileName = getWebsiteNameForGraph(es.getGraph());
			if (fileNames.contains(fileName)) {
				String t = fileName2title.get(fileName);
				fileName2title.remove(t);
				fileNames.remove(fileName);
				titles.remove(t);
			}
			titles.add(title);
			fileNames.add(fileName);
			fileName2title.put(fileName, title);
			idx++;
		}
	}
	
	private String getWebsiteNameForGraph(Graph graph) {
		return "graph_" + getMD5(graph.getName()) + ".html";
	}
	
	private String getMD5(String name) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(name.getBytes(Charset.forName("utf-8")));
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	private void generateMainFrameSet(File targetPath, String fileNameFrameSet,
						String fileNameNavigation, String fileNameHeader,
						String fileNameFooter, String fileNameMainDescription,
						String websiteTitle) throws IOException {
		writeSimple(
							targetPath,
							fileNameFrameSet,
							""
												+ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">§"
												+ "<html>§"
												+ "<head>§"
												+ "	<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">§"
												+ "	<title>"
												+ StringManipulationTools
																	.UnicodeToHtml(StringManipulationTools
																						.removeHTMLtags(websiteTitle))
												+ "</title>§"
												+ "  <link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />§"
												+ "</head>§"
												+ "<frameset rows=\"80,*,35\" "
												+ borderStyle
												+ ">§"
												+ "	<frame name=\"header\" src=\""
												+ fileNameHeader
												+ "\">§"
												+ "	<frameset cols=\"200,*\" "
												+ borderStyle
												+ ">§"
												+ "		<frame name=\"navigation\" src=\""
												+ fileNameNavigation
												+ "\">§"
												+ "		<frame name=\"main\" src=\""
												+ fileNameMainDescription
												+ "\">§"
												+ "	</frameset>§"
												+ "	<frame name=\"footer\" src=\""
												+ fileNameFooter
												+ "\">§"
												+ "</frameset>§" + "</html>");
	}
	
	private void writeSimple(File targetPath, String fileName, String content)
						throws IOException {
		content = StringManipulationTools.stringReplace(content, "§", System
							.getProperty("line.separator"));
		TextFile.writeE(targetPath.getAbsolutePath() + File.separator + fileName,
							content, "UTF-8");
	}
	
	private void exportGraphImages(File targetPath, EditorSession es,
						String fileName, StringBuilder iconTable,
						BackgroundTaskStatusProviderSupportingExternalCall status,
						double progressStep) throws IOException {
		String pathwayDescription = (String) AttributeHelper.getAttributeValue(es
							.getGraph(), "", "description", "Description not available.", "");
		String pathwayTitle = (String) AttributeHelper.getAttributeValue(es
							.getGraph(), "", "title", "Title not specified.", "");
		String pureFn = fileName.substring(0, fileName.lastIndexOf("."));
		generateGraphImagesAndFrameSet(
							iconTable,
							es,
							targetPath,
							fileName,
							pathwayDescription,
							pathwayTitle,
							new SizeSettingZoom[] { SizeSettingZoom.L200, SizeSettingZoom.L150,
												SizeSettingZoom.L125, SizeSettingZoom.L100,
												SizeSettingZoom.L75, SizeSettingZoom.L50, SizeSettingZoom.L25 },
							new String[] { pureFn + "_200.html", pureFn + "_150.html",
												pureFn + "_125.html", pureFn + "_100.html",
												pureFn + "_75.html", pureFn + "_50.html", pureFn + "_25.html" },
							status, progressStep);
	}
	
	private void generateGraphImagesAndFrameSet(StringBuilder iconTable,
						EditorSession es, File targetPath, String graphOverviewFileName,
						String pathwayDescription, String pathwayTitle,
						SizeSettingZoom[] zoomFactors, String[] fileNames,
						BackgroundTaskStatusProviderSupportingExternalCall status,
						double progressStep) throws IOException {
		
		// generate frameset
		
		StringBuilder zoomLinks = new StringBuilder();
		zoomLinks.append("<small>Image Size: ");
		int idx = 0;
		for (SizeSettingZoom z : zoomFactors) {
			zoomLinks.append("<a href=\"" + fileNames[idx]
																	+ "\" class=\"zoom\" target=\"graphcontent\">"
																	+ StringManipulationTools.UnicodeToHtml(z.toString())
																	+ "</a>&nbsp; ");
			idx++;
		}
		zoomLinks.append("</small>");
		
		String fileNameHeaderDescription = StringManipulationTools.stringReplace(
							graphOverviewFileName, ".html", "")
							+ "_description.html";
		writeSimpleWebsiteBody(
							targetPath,
							fileNameHeaderDescription,
							"Description for " + graphOverviewFileName,
							"<h2>"
												+ StringManipulationTools
																	.UnicodeToHtml(getLastItemOf(pathwayTitle))
												+ "</h2>"
												+ getPathwayPath("<h3>", "</h3>",
																	getAllButLastItemOf(pathwayTitle)) + pathwayDescription,
							"description");
		
		String fileNameHeaderForZoom = StringManipulationTools.stringReplace(
							graphOverviewFileName, ".html", "")
							+ "_zoom.html";
		writeSimpleWebsiteBody(targetPath, fileNameHeaderForZoom,
							"Zoom selection for " + graphOverviewFileName,
							zoomLinks.toString(), "zoom");
		
		writeSimple(
							targetPath,
							graphOverviewFileName,
							""
												+ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">§"
												+ "<html>§"
												+ "<header>§"
												+ "	<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">§"
												+ "	<title>"
												+ StringManipulationTools
																	.UnicodeToHtml(StringManipulationTools
																						.removeHTMLtags(graphOverviewFileName))
												+ "</title>§"
												+ "  <link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />§"
												+ "</header>§" + "<frameset rows=\"130,40,*\" " + borderStyle
												+ ">§" + "	<frame name=\"graphdescription\" src=\""
												+ fileNameHeaderDescription + "\">§"
												+ "	<frame name=\"graphzoom\" src=\"" + fileNameHeaderForZoom
												+ "\">§" + "	<frame name=\"graphcontent\" src=\""
												+ fileNames[fileNames.length - 1] + "\">§" + "</frameset>§"
												+ "</html>");
		
		// generate zoomed images & html
		PngJpegAlgorithmParams settings = new PngJpegAlgorithmParams();
		settings.setCreateHTMLmap(true);
		settings.setCreateJPG(false);
		settings.setIncludeTooltip(false);
		settings.setIncludeURLinTooltip(false);
		settings.setCustomTarget("main");
		{
			settings.setScaleSetting(SizeSetting.FIXED);
			settings.setScaleFixedUseWidthOrHeightValue(100);
			settings.setMaxHeight(200);
			settings.setMaxWidth(200);
			String pureFn = graphOverviewFileName.substring(0,
								graphOverviewFileName.lastIndexOf("."));
			String fn = pureFn + "_icon.png";
			String title = (String) AttributeHelper.getAttributeValue(es
								.getGraph(), "", "title", "Untitled Graph " + idx, "");
			iconTable.append("<a href=\"" + pureFn
								+ ".html\" target=\"main\"><img border=2 src=\"" + fn
								+ "\"</a><br>§" + "<small>"
								+ StringManipulationTools.UnicodeToHtml(getLastItemOf(title))
								+ "</small>");
			PngJpegAlgorithm.createPNGimageFromGraph(es.getGraph(), targetPath
								.getAbsolutePath()
								+ File.separator + fn, settings);
		}
		settings.setScaleSetting(SizeSetting.ZOOM);
		settings.setScaleFixedUseWidth(false);
		double stepSize = 1d / (zoomFactors.length + 1) * progressStep;
		status.getCurrentStatusValueFine();
		for (int i = 0; i < zoomFactors.length; i++) {
			status.setCurrentStatusValueFine(status.getCurrentStatusValueFine()
								+ stepSize);
			SizeSettingZoom zoom = zoomFactors[i];
			settings.setScaleZoomSetting(zoom);
			String fn = fileNames[i];
			PngJpegAlgorithm.createPNGimageFromGraph(es.getGraph(), targetPath
								.getAbsolutePath()
								+ File.separator + fn, settings);
		}
	}
	
	/**
	 * @param string
	 * @param string2
	 * @param allButLastItemOf
	 * @return
	 */
	private String getPathwayPath(String tagA, String tagB,
						String allButLastItemOf) {
		if (allButLastItemOf.length() == 0)
			return "";
		else
			return tagA + allButLastItemOf + tagB;
	}
	
	/**
	 * @param pathwayTitle
	 * @return
	 */
	private String getAllButLastItemOf(String pathwayTitle) {
		StringBuilder res = new StringBuilder();
		String[] f = pathwayTitle.split("\\.");
		if (f.length > 1) {
			for (int i = 0; i < f.length - 1; i++) {
				res.append(f[i]);
				if (i < f.length - 2)
					res.append(" > ");
			}
		}
		return res.toString();
	}
	
	private String getLastItemOf(String title) {
		String[] n = title.split("\\.");
		return n[n.length - 1];
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false; // is handled by the algorithm itself
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Create Website...";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public ImageIcon getIcon() {
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(
							path + "/browser.png"));
		return icon;
	}
	
	@Override
	public boolean showMenuIcon() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.graffiti.plugin.algorithm.EditorAlgorithm#activeForView(org.graffiti
	 * .plugin.view.View)
	 */
	public boolean activeForView(View v) {
		return v != null;
	}
	
}
