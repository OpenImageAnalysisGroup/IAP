/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.core.ImageBundle;
import org.graffiti.editor.GraffitiFrame;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.managers.pluginmgr.DefaultPluginEntry;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.managers.pluginmgr.PluginManager;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ProvidesDirectMouseClickContextMenu;
import org.graffiti.plugin.algorithm.ProvidesEdgeContextMenu;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.extension.Extension;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.modes.defaults.MegaTools;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.NodeComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.jruby.Ruby;
import org.jruby.RubyIO;

import bsh.Interpreter;

/*
 * Created on 01.10.2003
 */

/**
 * This is the default context menu manager, that is used by the enhanced
 * editing tools for displaying a context menu. Currently two ways to add
 * context menus exist: (1) Plugin-Algorithms can implement the interface <code>AlgorithmWithContextMenu</code>. (2a) BeanShell Scripts can be placed
 * in the home folder (extension .bsh). There the menu command text is detected
 * from the first line of text. "// (at)Test-Command " would result in the Menu
 * Item "Test-Command" ("//" is replaced by "" and then a String.trim() call is
 * issued). Only scripts with a (at) in the first line will be placed in the
 * context menu. (2b) The search folder (standard is the home directory) for the
 * .bsh commands can be modified by adding a "scriptHomes" variable in the
 * "~./.bshrc" init script. My sources the .bshrc file and uses this variable.
 * Example: .bshrc: scriptHomes = "/home/klukas;/tmp"; ==> the home and the temp
 * path will be scanned for ".bsh" files.
 * 
 * @author Christian Klukas
 */
public class DefaultContextMenuManager extends ContextMenuManager {
	
	protected static Ruby ruby = null;
	private static PrintStream out;
	private static ArrayList<String> ignoreFiles = new ArrayList<String>();
	
	ImageBundle iBundle = ImageBundle.getInstance();
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.graffiti.managers.IContextMenuManager#getContextMenu(java.lang.Object,
	 * java.awt.event.MouseEvent)
	 */
	public JPopupMenu getContextMenu(MouseEvent e) {
		PluginManager pm = GravistoService.getInstance().getMainFrame().getPluginManager();
		Collection<PluginEntry> plugins = pm.getPluginEntries();
		Iterator<PluginEntry> iter = plugins.iterator();
		
		JPopupMenu result = new JPopupMenu();
		
		JMenu pluginEntries = new JMenu("Network");
		JMenu scriptEntries = new JMenu("Script");
		JMenu windowEntries = new JMenu("Window");
		JSeparator lineEntry = new JSeparator();
		JMenu nodeEntries = new JMenu("Nodes");
		JMenu edgeEntries = new JMenu("Edges");
		
		final EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
		Selection selection = null;
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		
		Graph graph = null;
		if (session != null)
			graph = session.getGraph();
		
		Collection<Node> nodes = null;
		Collection<Edge> edges = null;
		
		if (selection != null && selection.isEmpty()) {
			nodes = new ArrayList<Node>(); // graph.getNodes();
			edges = new ArrayList<Edge>(); // graph.getEdges();
			
			// if you right-click on a node or edge (and nothing else is selected)
			// select this element
			Component lastMouseSrc = MegaTools.getLastMouseSrc();
			GraphElement tobeadded = null;
			if (lastMouseSrc != null && (lastMouseSrc instanceof NodeComponent)) {
				NodeComponent nc = (NodeComponent) lastMouseSrc;
				tobeadded = nc.getGraphElement();
				nodes.add((Node) tobeadded);
			} else
				if (lastMouseSrc != null && (lastMouseSrc instanceof EdgeComponent)) {
					EdgeComponent ec = (EdgeComponent) lastMouseSrc;
					tobeadded = ec.getGraphElement();
					edges.add((Edge) tobeadded);
				}
			if (tobeadded != null) {
				final Selection sel = new Selection("id");
				sel.add(tobeadded);
				session.getSelectionModel().setActiveSelection(sel);
			}
		} else {
			if (selection != null) {
				nodes = selection.getNodes();
				edges = selection.getEdges();
			}
		}
		int added = 0;
		for (JMenuItem mi : getDirectMouseClickContextCommands(org.graffiti.plugins.modes.defaults.MegaTools
							.getLastMouseE(), org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseSrc(), graph, plugins)) {
			result.add(mi);
			added++;
		}
		if (added > 0)
			result.add(new JSeparator());
		
		getPluginMenuItems(iter, pluginEntries, nodeEntries, edgeEntries, nodes, edges);
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.SCRIPT_ACCESS))
			returnScriptMenu(scriptEntries);
		
		if (pluginEntries.getItemCount() > 0)
			result.add(pluginEntries);
		
		windowEntries.add(getDetachWindowCommand());
		result.add(windowEntries);
		
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.SCRIPT_ACCESS))
			if (scriptEntries.getItemCount() > 0)
				result.add(scriptEntries);
		if (nodeEntries.getItemCount() > 0 || edgeEntries.getItemCount() > 0)
			result.add(lineEntry);
		if (nodeEntries.getItemCount() > 0)
			result.add(nodeEntries);
		if (edgeEntries.getItemCount() > 0)
			result.add(edgeEntries);
		
		return result;
	}
	
	private JMenuItem getDetachWindowCommand() {
		JMenuItem menu = new JMenuItem("Detach/Attach");
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GraffitiFrame.detachOrAttachActiveFrame(false);
			}
		});
		return menu;
	}
	
	private Collection<JMenuItem> getDirectMouseClickContextCommands(MouseEvent lastMouseE, Component lastMouseSrc,
						Graph graph, Collection<PluginEntry> pluginEntries) {
		Collection<JMenuItem> result = new ArrayList<JMenuItem>();
		for (Object o : pluginEntries) {
			PluginEntry pe = (PluginEntry) o;
			GenericPlugin p = pe.getPlugin();
			if (p instanceof ProvidesDirectMouseClickContextMenu) {
				ProvidesDirectMouseClickContextMenu mp = (ProvidesDirectMouseClickContextMenu) p;
				for (JMenuItem mi : mp.getContextCommand(org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseE(),
									org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseSrc(), graph)) {
					result.add(mi);
				}
			}
		}
		return result;
	}
	
	private static ArrayList<String> pathList = getScriptHomes();
	
	public static void returnScriptMenu(JMenu scriptEntries) {
		// read Scripts
		synchronized (pathList) {
			if (pathList.isEmpty()) {
				try {
					String s = ReleaseInfo.getAppFolder();
					pathList.add(s);
				} catch (Exception e) {
					// empty
				}
				
				// pathList.add("." + sep + "build" + sep + "classes" + sep +
				// "commands");
			}
			
			for (String pathName : pathList) {
				File path = new File(pathName);
				
				String[] list;
				
				list = path.list(new FilenameFilter() {
					private final Pattern pattern = Pattern.compile("(.*\\.bsh|.*\\.rb)");
					
					public boolean accept(File dir, String name) {
						name = name.toLowerCase();
						return pattern.matcher(new File(name).getName()).matches();
					}
				});
				if (list != null)
					for (int i = 0; i < list.length; i++) {
						if (list[i].toLowerCase().endsWith(".bsh"))
							addBSHentry(scriptEntries, path, list, i);
						if (list[i].toLowerCase().endsWith(".rb"))
							addRBentry(scriptEntries, path, list, i);
					}
			}
		}
		
	}
	
	private static void addBSHentry(JMenu scriptEntries, File path, String[] list, int i) {
		String fileName = path + "/" + list[i];
		if (ignoreFiles.contains(fileName))
			return;
		BSHinfo info = new BSHinfo(FileSystemHandler.getURL(new File(fileName)));
		BSHscriptMenuEntry newMenu = new BSHscriptMenuEntry(info.firstLine, fileName, info.nodeCommand);
		newMenu.setAction(new GraffitiAction("scriptcommand", MainFrame.getInstance(), "beanshell") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public boolean isEnabled() {
				try {
					Session s = GravistoService.getInstance().getMainFrame().getActiveSession();
					Graph g = GravistoService.getInstance().getMainFrame().getActiveSession().getGraph();
					return (g != null && s != null);
				} catch (NullPointerException npe) {
					return false;
				}
			}
			
			public void actionPerformed(ActionEvent e) {
			}
		});
		initMenuItemExecCode(newMenu);
		if (info.targetMenu != null && info.targetMenu.length() > 0) {
			if (info.targetMenu.equalsIgnoreCase("runonce")) {
				newMenu.doClick();
				ActionListener[] al = newMenu.getActionListeners();
				for (ActionListener a : al) {
					a.actionPerformed(new ActionEvent(newMenu, 0, null));
				}
			} else {
				// System.out.println("Add BSH menu item to target menu: " + info.targetMenu);
				// System.out.println("BSH file: " + fileName);
				// System.out.println("First Line: " + info.firstLine);
				MainFrame.getInstance().addMenuItemForAlgorithmOrExtension(newMenu, info.targetMenu);
			}
			ignoreFiles.add(fileName);
		} else {
			scriptEntries.add(newMenu);
		}
	}
	
	private static void addRBentry(JMenu scriptEntries, File path, String[] list, int i) {
		String fileName = path + "/" + list[i];
		String firstLine = null;
		try {
			firstLine = getFirstOrSecondLine(FileSystemHandler.getURL(new File(fileName)), "#");
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("First line of ruby script should contain the graph window context-menu label:<br>"
								+ "Example:<br>" + "[Line 1] # Test Command 1");
			firstLine = "# Unnamed Ruby Script Command (" + list[i] + ")";
		}
		
		if (firstLine.startsWith("#"))
			firstLine = firstLine.replaceFirst("#", "");
		else
			firstLine = fileName + " (no #desc in first line)";
		
		RubyScriptMenuEntry newMenu = new RubyScriptMenuEntry(firstLine, fileName);
		
		initMenuItemExecCode(newMenu);
		scriptEntries.add(newMenu);
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param cmdExec
	 *           DOCUMENT ME!
	 */
	private static void initMenuItemExecCode(JMenuItem cmdExec) {
		ClassLoader cl = DefaultContextMenuManager.class.getClassLoader();
		String path = DefaultContextMenuManager.class.getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/scriptentry.png"));
		cmdExec.setIcon(icon);
		cmdExec.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg) {
				if (arg.getSource() instanceof BSHscriptMenuEntry) {
					BSHscriptMenuEntry src = (BSHscriptMenuEntry) arg.getSource();
					src.execute();
				} else
					if (arg.getSource() instanceof RubyScriptMenuEntry) {
						RubyScriptMenuEntry src = (RubyScriptMenuEntry) arg.getSource();
						String cmd = src.getCmdFile();
						if (ruby == null) {
							ruby = Ruby.getDefaultInstance();
							ruby.getLoadService().init(ruby, new ArrayList());
						}
						String res = eval("load '" + cmd + "'");
						if (res != null && res.length() > 0)
							MainFrame.showMessageDialog("Result of " + src.getText() + "\n" + res, "Result (" + cmd + ")");
					} else
						ErrorMsg.addErrorMessage("Internal Error: Unknown Script Command Entry Tpye!");
			}
		});
	}
	
	/**
	 * evaluate a string and returns the standard output.
	 * 
	 * @param script
	 *           the String to eval as a String
	 * @return the value printed out on stdout and stderr by
	 **/
	protected static String eval(String script) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		out = new PrintStream(result);
		RubyIO lStream = new RubyIO(ruby, out);
		ruby.getGlobalVariables().set("$stdout", lStream);
		ruby.getGlobalVariables().set("$>", lStream);
		ruby.getGlobalVariables().set("$stderr", lStream);
		
		ruby.loadScript("test", new StringReader(script), false);
		StringBuffer sb = new StringBuffer(new String(result.toByteArray()));
		// for (int idx = sb.indexOf("\n"); idx != -1; idx = sb.indexOf("\n")) {
		// sb.deleteCharAt(idx);
		// }
		out.flush();
		return sb.toString();
	}
	
	private static Interpreter i = new Interpreter();
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param pathList
	 *           DOCUMENT ME!
	 */
	private static ArrayList<String> getScriptHomes() {
		ArrayList<String> pathList = new ArrayList<String>();
		try {
			i.source(ReleaseInfo.getAppFolderWithFinalSep() + ".bshrc");
			
			if (i.eval("scriptHomes") != null) {
				String[] pathes = i.eval("scriptHomes").toString().split(";");
				
				for (int i2 = 0; i2 < pathes.length; i2++) {
					if (!pathList.contains(pathes[i2])) {
						pathList.add(pathes[i2]);
					}
				}
			}
		} catch (FileNotFoundException fne) {
			// empty
		} catch (Exception e2) {
			// ErrorMsg.addErrorMessage(e2.getLocalizedMessage());
		}
		return pathList;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param iter
	 *           DOCUMENT ME!
	 * @param pluginEntries
	 *           DOCUMENT ME!
	 */
	private void getPluginMenuItems(Iterator<PluginEntry> iter, JMenu pluginEntries, JMenu nodeEntries,
						JMenu edgeEntries, Collection<Node> selectedNodes, Collection<Edge> selectedEdges) {
		while (iter.hasNext()) {
			DefaultPluginEntry element = (DefaultPluginEntry) iter.next();
			if (element.getPlugin() == null)
				continue;
			GenericPlugin plugin = element.getPlugin();
			processPlugins(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges, plugin);
			processAlgorithms(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges, plugin);
			processExtensions(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges, plugin);
		}
	}
	
	// /**
	// * This method is called by the editing tools in order to pre-process a
	// * mouse click. Depending on the result of the call the mouse click
	// * should be ignored. If a right-click is issued, a context menu is built
	// * as described in the class description.
	// *
	// * @param e The <code>MouseEvent</code> to be processed.
	// *
	// * @return true if context menu is shown, in this case, the mouseevent
	// * should not be processed any further.
	// */
	// public boolean processMouseButton(MouseEvent e)
	// {
	// if (SwingUtilities.isRightMouseButton(e)) {
	// cm = getContextMenu(e);
	//
	// if (e.getSource() instanceof Component) {
	// Component src = (Component) e.getSource();
	//
	// System.out.println("CM SRC: " + src.toString());
	//
	// GraffitiView s = (GraffitiView) src;
	// AffineTransform zoom = s.getZoom();
	//
	// Point2D.Double srcp = new Point2D.Double(e.getX(),
	// e.getY());
	// Point2D.Double dest = new Point2D.Double();
	//
	// zoom.transform(srcp, dest);
	//
	// cm.show(src,
	// new Double(dest.getX()).intValue(),
	// new Double(dest.getY()).intValue());
	// cm.setVisible(true);
	// cm.setFocusable(true);
	// e.consume();
	// return true;
	// }
	// }
	//
	// return false;
	// }
	
	private void processPlugins(JMenu pluginEntries, JMenu nodeEntries, JMenu edgeEntries,
						Collection<Node> selectedNodes, Collection<Edge> selectedEdges, GenericPlugin plugin) {
		if ((plugin instanceof ProvidesGeneralContextMenu) || (plugin instanceof ProvidesNodeContextMenu)
							|| (plugin instanceof ProvidesEdgeContextMenu)) {
			processContextMenuInterfaces(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges, plugin);
		}
	}
	
	private void processAlgorithms(JMenu pluginEntries, JMenu nodeEntries, JMenu edgeEntries,
						Collection<Node> selectedNodes, Collection<Edge> selectedEdges, GenericPlugin plugin) {
		if (plugin.getAlgorithms() != null) {
			Algorithm[] algos = plugin.getAlgorithms();
			if (algos.length > 0) {
				for (int i = 0; i < algos.length; i++) {
					processContextMenuInterfaces(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges,
										algos[i]);
				}
			}
		}
	}
	
	private void processExtensions(JMenu pluginEntries, JMenu nodeEntries, JMenu edgeEntries,
						Collection<Node> selectedNodes, Collection<Edge> selectedEdges, GenericPlugin plugin) {
		if (plugin.getExtensions() != null) {
			Extension[] extensions = plugin.getExtensions();
			if (extensions.length > 0) {
				for (int i = 0; i < extensions.length; i++) {
					processContextMenuInterfaces(pluginEntries, nodeEntries, edgeEntries, selectedNodes, selectedEdges,
										extensions[i]);
				}
			}
		}
	}
	
	/**
	 * @param pluginEntries
	 * @param nodeEntries
	 * @param edgeEntries
	 * @param selectedNodes
	 * @param selectedEdges
	 * @param extensions
	 * @param i
	 */
	private void processContextMenuInterfaces(JMenu pluginEntries, JMenu nodeEntries, JMenu edgeEntries,
						Collection<Node> selectedNodes, Collection<Edge> selectedEdges, Object object) {
		if (object instanceof Algorithm) {
			GravistoService.getInstance().algorithmAttachData((Algorithm) object);
		}
		if (object instanceof ProvidesGeneralContextMenu) {
			ProvidesGeneralContextMenu p = (ProvidesGeneralContextMenu) object;
			JMenuItem[] r = p.getCurrentContextMenuItem();
			if (r != null) {
				for (int i = 0; i < r.length; i++)
					pluginEntries.add(r[i]);
			}
		}
		if (object instanceof ProvidesNodeContextMenu) {
			ProvidesNodeContextMenu p = (ProvidesNodeContextMenu) object;
			if (p != null) {
				JMenuItem[] r = p.getCurrentNodeContextMenuItem(selectedNodes);
				if (r != null) {
					for (int i = 0; i < r.length; i++)
						nodeEntries.add(r[i]);
				}
			}
		}
		if (object instanceof ProvidesEdgeContextMenu) {
			ProvidesEdgeContextMenu p = (ProvidesEdgeContextMenu) object;
			if (p != null) {
				JMenuItem[] r = p.getCurrentEdgeContextMenuItem(selectedEdges);
				if (r != null) {
					for (int i = 0; i < r.length; i++)
						edgeEntries.add(r[i]);
				}
			}
		}
	}
	
	/**
	 * This method reads and returns the first line of a text file. It returns
	 * the second line if the first line does not contain a "(at)" character.
	 * 
	 * @param url
	 *           Name of the text-file to be read.
	 * @return First line of text.
	 */
	static String getFirstOrSecondLine(IOurl url, String matchFirst) {
		BufferedReader in;
		
		try {
			in = new BufferedReader(new InputStreamReader(url.getInputStream(), "UTF-8"));
			String result = in.readLine();
			if (result == null)
				return null;
			if (result.indexOf(matchFirst) < 0) {
				result = in.readLine();
			}
			
			result = result.replaceFirst("//", "");
			result = result.trim();
			in.close();
			in = null;
			return result;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return null;
		}
	}
	
	public static String getContent(String fileName) {
		BufferedReader in;
		try {
			StringBuilder allLines = new StringBuilder();
			FileReader fr = new FileReader(fileName);
			in = new BufferedReader(fr);
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				allLines.append(inputLine + "\r\n");
			in.close();
			in = null;
			fr.close();
			fr = null;
			return allLines.toString();
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return "";
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return "";
		}
	}
	
	public static String getContent(IOurl url) {
		try {
			StringBuilder allLines = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				allLines.append(inputLine + "\r\n");
			in.close();
			in = null;
			return allLines.toString();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e.getLocalizedMessage());
			return "";
		}
	}
	
	public void ensureActiveSession(MouseEvent e) {
		View targetView = (View) e.getComponent();
		if (targetView == null)
			return;
		if (GravistoService.getInstance().getMainFrame().getActiveSession() == null)
			return;
		if (targetView != GravistoService.getInstance().getMainFrame().getActiveSession().getActiveView()) {
			Iterator<Session> itSessions = MainFrame.getSessions().iterator();
			boolean found = false;
			while (itSessions.hasNext() && !found) {
				Session mySession = itSessions.next();
				Iterator<View> itViews = mySession.getViews().iterator();
				while (itViews.hasNext() && !found) {
					View myView = itViews.next();
					if (myView == targetView) {
						mySession.setActiveView(myView);
						GravistoService.getInstance().getMainFrame().setActiveSession(mySession, myView);
						found = true;
					}
				}
			}
		}
	}
	
}
