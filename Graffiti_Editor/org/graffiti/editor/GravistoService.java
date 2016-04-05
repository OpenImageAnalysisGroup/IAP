package org.graffiti.editor;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.HelperClass;
import org.ObjectRef;
import org.Release;
import org.ReleaseInfo;
import org.Scalr;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.actions.RunAlgorithm;
import org.graffiti.editor.dialog.DefaultParameterDialog;
import org.graffiti.editor.dialog.ParameterDialog;
import org.graffiti.graph.Graph;
import org.graffiti.managers.pluginmgr.DefaultPluginEntry;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.options.OptionPane;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.CalculatingAlgorithm;
import org.graffiti.plugin.algorithm.EditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ProvidesAccessToOtherAlgorithms;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

import scenario.ScenarioService;

/**
 * Provides access to global variables, needed for various extensions to
 * Graffiti. Plugins can use the Preferences structure to save settings.
 * 
 * @author Christian Klukas
 */
public class GravistoService implements HelperClass, MemoryHogInterface {
	
	/**
	 * The only and single instance of this object
	 */
	private static GravistoService instance;
	
	/**
	 * Contains <code>optionPanelRecord</code> structures which describe a option
	 * window.
	 */
	private final ArrayList<OptionPane> optionPane = new ArrayList<OptionPane>();
	
	private final ArrayList<Object> optionPaneIdentifiers = new ArrayList<Object>();
	
	/**
	 * DOCUMENT ME!
	 */
	private volatile boolean plugins_MoveSelectionsAllowed = true;
	
	/**
	 * DOCUMENT ME!
	 */
	public volatile Object selectionSyncObject = new Object();
	
	/**
	 * A list of frames, which are used by the pattern editor.
	 */
	private List<JInternalFrame> frames;
	
	/**
	 * DOCUMENT ME!
	 */
	private List<Session> patternSessions;
	
	public GravistoService() {
		addKnownMemoryHog(this);
	}
	
	/**
	 * Returns the single instance of this class.
	 * 
	 * @return The single instance of this "Singleton".
	 */
	public static synchronized GravistoService getInstance() {
		if (instance == null) {
			instance = new GravistoService();
		}
		
		return instance;
	}
	
	/**
	 * Adds a optionPane to the list of known options-panes. The list of known
	 * option-panes can be retrieved with <code>getKnownOptionPanes</code>.
	 * 
	 * @param optionPane
	 *           A new known optionPane. If already known, not added again.
	 */
	public void addKnownOptionPane(Object identifyer, OptionPane optionPane) {
		if (!this.optionPaneIdentifiers.contains(identifyer)) {
			this.optionPaneIdentifiers.add(identifyer);
			this.optionPane.add(optionPane);
		}
	}
	
	/**
	 * @return Returns a list of known optionPanes. (type <code>OptionPane</code> )
	 */
	public ArrayList<OptionPane> getKnownOptionPanes() {
		return optionPane;
	}
	
	/**
	 * A global variable, for communication between the IPK Editing Tools and
	 * some IPK Layouter. This will be later eventually be removed.
	 * 
	 * @return If value is true, the selection should not be moved by the
	 *         layouter algorithms.
	 */
	public synchronized boolean pluginSelectionMoveAllowed() {
		return plugins_MoveSelectionsAllowed;
	}
	
	/**
	 * A global variable, for communication between the IPK Editing Tools and
	 * some IPK Layouter. This will be later eventually be removed.
	 * 
	 * @param value
	 *           set to true, if the selection should not be moved by the
	 *           layouter algorithms.
	 */
	public synchronized void pluginSetMoveAllowed(boolean value) {
		plugins_MoveSelectionsAllowed = value;
	}
	
	/**
	 * Returns a <code>Vector</code> which contains a list of sessions, loaded in
	 * the main view.
	 * 
	 * @return <code>Vector</code> with elements of the type <code>EditorSession</code>. Returns empty Vector, if no sessions
	 *         are loaded.
	 */
	public Vector<Session> getMainSessions() {
		Vector<Session> result = new Vector<Session>();
		
		Set<Session> sessions = MainFrame.getSessions();
		
		for (Iterator<Session> it = sessions.iterator(); it.hasNext();) {
			Session curS = it.next();
			
			if ((patternSessions == null) || (patternSessions.indexOf(curS) < 0)) {
				result.add(curS);
			}
		}
		
		return result;
	}
	
	/**
	 * A <code>List</code> of the pattern sessions. PatternSessions are sessions,
	 * which are loaded in the pattern tab.
	 * 
	 * @return The pattern sessions.
	 */
	public List<Session> getPatternSessionList() {
		return patternSessions;
	}
	
	/**
	 * Returns the main frame (application window).
	 * 
	 * @return The main frame.
	 */
	public MainFrame getMainFrame() {
		return MainFrame.getInstance();
	}
	
	/**
	 * Returns a <code>Vector</code> which contains a list of graphs from the
	 * main view.
	 * 
	 * @return <code>Vector</code> with elements of the type <code>Graph</code>.
	 */
	public Vector<Graph> getMainGraphs() {
		Vector<Graph> result = new Vector<Graph>();
		
		for (Iterator<Session> it = getMainFrame().getSessionsIterator(); it.hasNext();) {
			Session curS = it.next();
			
			if ((patternSessions == null) || (patternSessions.indexOf(curS) < 0)) {
				result.add(curS.getGraph());
			}
		}
		
		return result;
	}
	
	/**
	 * Returns a <code>Vector</code> which contains a list of pattern graphs.
	 * 
	 * @return <code>Vector</code> with elements of the type <code>Graph</code>.
	 *         If no patterns are loaded or available, this method returns an
	 *         empty <code>Vector</code>.
	 */
	public ArrayList<Graph> getPatternGraphs() {
		ArrayList<Graph> result = new ArrayList<Graph>();
		if (patternSessions != null) {
			for (int i = 0; i < patternSessions.size(); i++) {
				if (patternSessions.get(i) != null) {
					result.add((patternSessions.get(i)).getGraph());
				}
			}
		}
		return result;
	}
	
	/**
	 * Adds a new internal frame to the list of pattern editor frames. Can be
	 * used by the method <code>isEditorFrameSelected</code> for the decision,
	 * whether a given frame is a editor frame or a pattern editor frame.
	 * 
	 * @param frame
	 *           New pattern editor frame.
	 */
	public void addFrame(GraffitiInternalFrame frame) {
		if (frames == null) {
			frames = new ArrayList<JInternalFrame>();
		}
		
		frames.add(frame);
	}
	
	/**
	 * Adds a Session to the list of patternSessions. This method is called by
	 * the patternInspector in the action handler for the load and new button
	 * action.
	 * 
	 * @param session
	 *           The new session, which should be known as a session, containing
	 *           a pattern graph.
	 */
	public void addPatternSession(Session session) {
		if (patternSessions == null) {
			patternSessions = new ArrayList<Session>();
		}
		
		patternSessions.add(session);
	}
	
	/**
	 * Checks if an editor frame in the main view is selected.
	 * 
	 * @return True, if an editor frame is selected.
	 */
	public boolean isEditorFrameSelected() {
		boolean result = false;
		
		if (frames != null) {
			// MainFrame mf=getInstance().getMainFrame();
			// Session currentSession=mf.getActiveSession();
			for (Iterator<JInternalFrame> it = frames.iterator(); it.hasNext();) {
				JInternalFrame frame = it.next();
				
				if (frame != null) {
					if (frame instanceof GraffitiInternalFrame) {
						if (frame.isSelected()) {
							result = true;
							break;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Unselects all editor frames in the main view.
	 */
	public void framesDeselect() {
		if (frames != null) {
			// MainFrame mf=getInstance().getMainFrame();
			// Session currentSession=mf.getActiveSession();
			for (JInternalFrame frame : frames) {
				try {
					if (frame instanceof GraffitiInternalFrame) {
						frame.setSelected(false);
					}
				} catch (PropertyVetoException e) {
					// ignore, no problem
				}
			}
		}
	}
	
	/**
	 * Returns a algorithm instance, defined by its name (e.g. menu item text)
	 * 
	 * @param name
	 *           The menu item text.
	 * @return The algorithm instance.
	 */
	public Algorithm getAlgorithmInstanceFromFriendlyName(String name) {
		Collection<PluginEntry> plugins = DefaultPluginManager.lastInstance.getPluginEntries();
		
		for (Iterator<PluginEntry> pi = plugins.iterator(); pi.hasNext();) {
			DefaultPluginEntry curPluginEntry = (DefaultPluginEntry) pi.next();
			Algorithm[] myAlgos = curPluginEntry.getPlugin().getAlgorithms();
			
			if (myAlgos.length > 0) {
				for (int i = 0; i < myAlgos.length; i++) {
					if (myAlgos[i] instanceof ProvidesGeneralContextMenu && myAlgos[i] instanceof RunAlgorithm) {
						ProvidesGeneralContextMenu acm = (ProvidesGeneralContextMenu) myAlgos[i];
						
						if (acm.getCurrentContextMenuItem().toString().equalsIgnoreCase(name)
								|| myAlgos[i].getName().equalsIgnoreCase(name)) {
							return myAlgos[i];
						}
					}
					Algorithm algo = myAlgos[i];
					
					if (algo.getName() != null && algo.getName().equalsIgnoreCase(name)) {
						return myAlgos[i];
					}
					
					if (algo instanceof ProvidesAccessToOtherAlgorithms) {
						ProvidesAccessToOtherAlgorithms pa = (ProvidesAccessToOtherAlgorithms) algo;
						if (pa.getAlgorithmList() != null)
							for (Algorithm a : pa.getAlgorithmList()) {
								if (a.getName() != null && a.getName().equalsIgnoreCase(name)) {
									return a;
								}
							}
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a plugin instance, given by its classname.
	 * 
	 * @param pluginDescription
	 * @return The plugin instance, if the plugin is loaded.
	 */
	public DefaultPluginEntry getPluginInstanceFromPluginDescription(String pluginDescription) {
		Collection<PluginEntry> plugins = DefaultPluginManager.lastInstance.getPluginEntries();
		
		for (Iterator<PluginEntry> pi = plugins.iterator(); pi.hasNext();) {
			DefaultPluginEntry curPluginEntry = (DefaultPluginEntry) pi.next();
			if (curPluginEntry.getDescription().getName().toUpperCase().indexOf(pluginDescription.toUpperCase()) >= 0) {
				return curPluginEntry;
			}
		}
		
		return null;
	}
	
	public static void run(String pluginNameOrClassName, ActionEvent event) {
		Graph g = MainFrame.getInstance().getActiveSession().getGraph();
		getInstance().runPlugin(pluginNameOrClassName, g, event);
	}
	
	/**
	 * Starts a plugin and returns, as soon as the plugin execution has finished.
	 * 
	 * @param pluginNameOrClassName
	 *           of Algorithm to execute or Menu Item Text (from PluginMenu or
	 *           Context Menu) or Classname of Plugin.
	 * @param g
	 *           Graph instance the plugin should work with.
	 */
	public void runPlugin(final String pluginNameOrClassName, final Graph g, final ActionEvent event) {
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				Algorithm algo = new GravistoService().getAlgorithmInstanceFromFriendlyName(pluginNameOrClassName);
				MainFrame.showMessage("Execute plugin " + pluginNameOrClassName + "", MessageType.INFO);
				if (algo == null)
					MainFrame.showMessageDialog("Unknown Algorithm: " + pluginNameOrClassName, "Internal Error");
				else
					runAlgorithm(algo, event);
			} else
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						Algorithm algo = new GravistoService().getAlgorithmInstanceFromFriendlyName(pluginNameOrClassName);
						MainFrame.showMessage("Execute plugin " + pluginNameOrClassName + "", MessageType.INFO);
						if (algo == null)
							MainFrame.showMessageDialog("Unknown Algorithm: " + pluginNameOrClassName, "Internal Error");
						else
							runAlgorithm(algo, event);
					}
				});
			// class Exec {
			// public void myRun(GravistoService gs) {
			// Algorithm algo =
			// gs.getAlgorithmInstanceFromFriendlyName(pluginNameOrClassName);
			//
			// MainFrame.showMesssage(
			// "Execute plugin " + pluginNameOrClassName
			// + "",
			// MessageType.INFO);
			// runAlgorithm(algo);
			// }
			// }
			// (new Exec()).myRun(this);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (InvocationTargetException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public void runAlgorithm(Algorithm algorithm, ActionEvent event) {
		runAlgorithm(algorithm, algorithm.mayWorkOnMultipleGraphs(), event);
	}
	
	public void runAlgorithm(Algorithm algorithm, boolean enableMultipleSessionProcessing, ActionEvent event) {
		Selection activeSel = null;
		try {
			activeSel = getMainFrame().getActiveEditorSession().getSelectionModel().getActiveSelection();
		} catch (NullPointerException npe) {
			// ignore here, the algorithm should make correct error handling for
			// null graph or selection
		}
		Graph graph = null;
		try {
			graph = getMainFrame().getActiveSession().getGraph();
		} catch (NullPointerException npe) {
			// ignore here, the algorithm should make correct error handling for
			// null graph or selection
		}
		
		runAlgorithm(algorithm, graph, activeSel, enableMultipleSessionProcessing, event);
	}
	
	/**
	 * @param listener
	 */
	public void algorithmAttachData(Algorithm algorithm) {
		Graph graph = null;
		if (getMainFrame().getActiveSession() != null)
			graph = getMainFrame().getActiveSession().getGraph();
		
		EditorSession session = getMainFrame().getActiveEditorSession();
		Selection selection = null;
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		algorithm.attach(graph, selection);
	}
	
	public static void attachData(Algorithm algorithm) {
		getInstance().algorithmAttachData(algorithm);
	}
	
	public void runAlgorithm(Algorithm algorithm, Graph graph, Selection selection, ActionEvent event) {
		runAlgorithm(algorithm, graph, selection, false, event);
	}
	
	/**
	 * @param algorithm
	 * @param nonInteractiveGraph
	 * @param nonInteractiveSelection
	 */
	public void runAlgorithm(Algorithm algorithm, Graph graph, Selection selection,
			boolean enableMultipleSessionProcessing, ActionEvent event) {
		algorithm.attach(graph, selection);
		algorithm.setActionEvent(event);
		try {
			algorithm.check();
		} catch (PreconditionException e) {
			StringBuilder sb = new StringBuilder();
			processError(algorithm, graph, sb, e);
			MainFrame.showMessageDialog("<html>" + sb.toString(), "Execution Error");
			return;
		}
		Parameter[] parameters = algorithm.getParameters();
		ThreadSafeOptions tsoParamDialogReturn = new ThreadSafeOptions();
		if ((parameters != null) && (parameters.length != 0) || (algorithm instanceof AlgorithmWithComponentDescription)) {
			boolean doReturn = false;
			
			doReturn = doThreadSafe(algorithm, selection, enableMultipleSessionProcessing, parameters, doReturn,
					tsoParamDialogReturn);
			
			if (doReturn)
				return;
		}
		ParameterDialog paramDialog = (ParameterDialog) tsoParamDialogReturn.getParam(0, null);
		if (parameters == null && algorithm instanceof EditorAlgorithm) {
			paramDialog = ((EditorAlgorithm) algorithm).getParameterDialog(selection);
		}
		
		Parameter[] params = (paramDialog == null) ? new Parameter[] {} : paramDialog.getEditedParameters();
		
		boolean stop = false;
		if (!(algorithm instanceof AlgorithmWithComponentDescription) && algorithm.getDescription() != null
				&& algorithm.getDescription().trim().length() > 0 && (parameters == null || parameters.length <= 0)
				&& SwingUtilities.isEventDispatchThread()) {
			int res = JOptionPane.showConfirmDialog(MainFrame.getInstance(), algorithm.getDescription(),
					StringManipulationTools.removeHTMLtags(algorithm.getName()), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null);
			if (res == JOptionPane.CANCEL_OPTION) {
				stop = true;
				MainFrame.showMessage(algorithm.getName() + " not started", MessageType.INFO);
			}
		}
		if (!stop) {
			StringBuilder errors = new StringBuilder();
			if (enableMultipleSessionProcessing) {
				multiGraphExecution(algorithm, graph, selection, event, paramDialog, params, errors);
			} else {
				algorithm.setParameters(params);
				algorithm.execute();
				ScenarioService.postWorkflowStep(algorithm, params);
				if (algorithm instanceof CalculatingAlgorithm) {
					JOptionPane.showMessageDialog(null, "<html>Result of algorithm:<p>"
							+ ((CalculatingAlgorithm) algorithm).getResult().toString());
				}
				algorithm.reset();
			}
			
		}
	}
	
	private boolean doThreadSafe(final Algorithm algorithm, final Selection selection,
			final boolean enableMultipleSessionProcessing, final Parameter[] parameters, final boolean doReturn,
			final ThreadSafeOptions tsoParamDialogReturn) {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.executeThreadSafe(new Runnable() {
			@Override
			public void run() {
				boolean res = doThreadSafeDoIt(algorithm, selection, enableMultipleSessionProcessing, parameters, doReturn,
						tsoParamDialogReturn);
				tso.setParam(0, res);
			}
		});
		return (Boolean) tso.getParam(0, null);
	}
	
	private boolean doThreadSafeDoIt(Algorithm algorithm, Selection selection, boolean enableMultipleSessionProcessing,
			Parameter[] parameters, boolean doReturn, ThreadSafeOptions tsoParamDialogReturn) {
		ParameterDialog paramDialog = null;
		if (algorithm instanceof EditorAlgorithm) {
			paramDialog = ((EditorAlgorithm) algorithm).getParameterDialog(selection);
		}
		
		if (paramDialog == null) {
			JComponent desc = null;
			if (algorithm instanceof AlgorithmWithComponentDescription) {
				try {
					desc = ((AlgorithmWithComponentDescription) algorithm).getDescriptionComponent();
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					desc = null;
				}
			}
			String algName = algorithm.getName();
			if (algorithm instanceof EditorAlgorithm) {
				algName = ((EditorAlgorithm) algorithm).getShortName();
			}
			paramDialog = new DefaultParameterDialog(getMainFrame().getEditComponentManager(), getMainFrame(), parameters,
					selection, StringManipulationTools.removeHTMLtags(algName), algorithm.getDescription(), desc,
					checkRelease(algorithm.mayWorkOnMultipleGraphs() && enableMultipleSessionProcessing));
		}
		
		if (!paramDialog.isOkSelected()) {
			doReturn = true;
		}
		tsoParamDialogReturn.setParam(0, paramDialog);
		return doReturn;
	}
	
	private void multiGraphExecution(Algorithm algorithm, Graph graph, Selection selection, ActionEvent event,
			ParameterDialog paramDialog, Parameter[] params, StringBuilder errors) {
		Collection<Session> sessions;
		if (paramDialog != null)
			sessions = paramDialog.getTargetSessions();
		else {
			sessions = new ArrayList<Session>();
			if (MainFrame.getSessions().size() == 1 || !algorithm.mayWorkOnMultipleGraphs()) {
				if (MainFrame.getInstance().isSessionActive())
					sessions.add(MainFrame.getInstance().getActiveSession());
			} else
				if (MainFrame.getSessions().size() > 1) {
					Object[] options = { "Active Graph", "Open Graphs (" + MainFrame.getSessions().size() + ")" };
					int res = JOptionPane.showOptionDialog(MainFrame.getInstance(), "Please select the working set.",
							StringManipulationTools.removeHTMLtags(algorithm.getName()), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (res == JOptionPane.YES_OPTION) {
						sessions.add(MainFrame.getInstance().getActiveSession());
					} else
						sessions.addAll(MainFrame.getSessions());
				}
		}
		boolean startLater = sessions.size() == 0;
		boolean runnn = false;
		for (Session s : sessions) {
			Graph g = s.getGraph();
			Selection sel;
			if (g == graph) {
				startLater = true;
				continue;
			}
			if (s instanceof EditorSession) {
				EditorSession es = (EditorSession) s;
				sel = es.getSelectionModel().getActiveSelection();
			} else
				sel = new Selection("empty");
			algorithm.attach(g, sel);
			algorithm.setParameters(params);
			try {
				algorithm.check();
				algorithm.execute();
				runnn = true;
				if (algorithm instanceof CalculatingAlgorithm) {
					JOptionPane.showMessageDialog(null, "<html>Result of algorithm:<p>"
							+ ((CalculatingAlgorithm) algorithm).getResult().toString());
				}
			} catch (PreconditionException e) {
				processError(algorithm, g, errors, e);
			}
		}
		if (runnn)
			algorithm.reset();
		if (startLater) {
			algorithm.attach(graph, selection);
			algorithm.setParameters(params);
			try {
				algorithm.setActionEvent(event);
				algorithm.check();
				algorithm.execute();
				ScenarioService.postWorkflowStep(algorithm, params);
				if (algorithm instanceof CalculatingAlgorithm) {
					JOptionPane.showMessageDialog(null, "<html>Result of algorithm:<p>"
							+ ((CalculatingAlgorithm) algorithm).getResult().toString());
				}
				algorithm.reset();
			} catch (PreconditionException e) {
				processError(algorithm, graph, errors, e);
			}
		}
		if (errors.length() > 0) {
			MainFrame.showMessageDialogWithScrollBars("<html>" + errors.toString(), "Execution Error");
		}
	}
	
	private boolean checkRelease(boolean mayWorkOnMultipleGraphs) {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return false;
		else
			return mayWorkOnMultipleGraphs;
	}
	
	private void processError(Algorithm algorithm, Graph graph, StringBuilder errors, PreconditionException e) {
		String name = algorithm.getName();
		if (name == null) {
			name = algorithm.getClass().getSimpleName();
		}
		if (graph != null)
			errors.append("Can not start <i>" + name + "</i> on graph " + graph.getName() + ":<br><br>" + e.getMessage()
					+ "<br><br>");
		else
			errors.append("Can not start <i>" + name + "</i>" + ":<br><br>" + e.getMessage() + "<br><br>");
	}
	
	/**
	 * @param frame
	 */
	public void removeFrame(GraffitiInternalFrame frame) {
		frames.remove(frame);
	}
	
	private static ArrayList<String> fileNames = new ArrayList<String>();
	
	public void loadFile(String fileName) {
		synchronized (fileNames) {
			fileNames.add(fileName);
		}
		if (ErrorMsg.getAppLoadingStatus() != ApplicationStatus.INITIALIZATION) {
			loadFiles();
		}
	}
	
	public static void loadFiles() {
		ArrayList<File> toDo = new ArrayList<File>();
		synchronized (fileNames) {
			for (String f : fileNames) {
				File ft = new File(f);
				if (ft.exists() && ft.canRead())
					toDo.add(ft);
			}
			fileNames.clear();
		}
		try {
			if (MainFrame.getInstance() != null)
				MainFrame.getInstance().loadGraphInBackground(toDo.toArray(new File[] {}), null, true);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public void loadFiles(File[] files) {
		if (files != null)
			for (File f : files)
				loadFile(f.getAbsolutePath());
	}
	
	public Session getSessionFromView(View thisView) {
		for (Session s : MainFrame.getSessions())
			if (s.getViews().contains(thisView))
				return s;
		return null;
	}
	
	public static void addActionOnKeystroke(JDialog comp, ActionListener action, KeyStroke key) {
		comp.getRootPane().registerKeyboardAction(action, key, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}
	
	public static BufferedImage blurImage(BufferedImage image, float blurRadius) {
		return new GaussFilter(blurRadius / 1.5f).filter(image, null);
	}
	
	// @SuppressWarnings("unchecked")
	// public static BufferedImage blurImage(BufferedImage image, int blurRadius)
	// {
	// float[] matrix = new float[blurRadius * blurRadius];
	// for (int i = 0; i < blurRadius * blurRadius; i++) {
	// matrix[i] = 1.0f / blurRadius / blurRadius;
	// }
	//
	// Map map = new HashMap();
	//
	// map.put(RenderingHints.KEY_INTERPOLATION,
	// RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	//
	// map.put(RenderingHints.KEY_RENDERING,
	// RenderingHints.VALUE_RENDER_QUALITY);
	//
	// map.put(RenderingHints.KEY_ANTIALIASING,
	// RenderingHints.VALUE_ANTIALIAS_ON);
	//
	// RenderingHints hints = new RenderingHints(map);
	// BufferedImageOp op = new ConvolveOp(new Kernel(blurRadius, blurRadius,
	// matrix), ConvolveOp.EDGE_NO_OP, hints);
	// try {
	// BufferedImage bi = op.filter(image, null);
	// showImage(bi, "Test");
	// return bi;
	// } catch (Exception e) {
	// System.out.println("Can't blur with radius " + blurRadius);
	// return image;
	// }
	// }
	
	public static BufferedImage getImage(URL fileUrl) throws IOException {
		return ImageIO.read(fileUrl);
	}
	
	private static boolean useRetina = SystemAnalysis.isRetina();
	
	/**
	 * @param w
	 *           negative values have special meaning, they are ignored
	 * @param h
	 *           at least w or h needs to be positive
	 */
	public static BufferedImage getScaledImage(Image icon, int w, int h) {
		BufferedImage destImage = new BufferedImage(icon.getWidth(null), icon.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = destImage.createGraphics();
		g2d.drawImage(icon, 0, 0, null);
		g2d.dispose();
		if (w == Integer.MAX_VALUE && h == Integer.MAX_VALUE)
			return destImage;
		else
			return getScaledImage(destImage, w, h);
	}
	
	public static BufferedImage getBufferedImage(Image icon) {
		BufferedImage destImage = new BufferedImage(icon.getWidth(null), icon.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = destImage.createGraphics();
		graphics.drawImage(icon, 0, 0, null);
		return destImage;
	}
	
	public static final class RetinaIcon extends ImageIcon {
		
		private static final long serialVersionUID = 1L;
		private int w;
		private int h;
		
		public RetinaIcon(final Image image) {
			super(image);
			this.w = image.getWidth(getImageObserver());
			this.h = image.getHeight(getImageObserver());
			this.w = (int) (w / SystemAnalysis.getHiDPIScaleFactor());
			this.h = (int) (h / SystemAnalysis.getHiDPIScaleFactor());
		}
		
		@Override
		public int getIconWidth() {
			return w;
		}
		
		@Override
		public int getIconHeight() {
			return h;
		}
		
		@Override
		public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
			ImageObserver observer = getImageObserver();
			
			if (observer == null) {
				observer = c;
			}
			
			Image image = getImage();
			int width = image.getWidth(observer);
			int height = image.getHeight(observer);
			final Graphics2D g2d = (Graphics2D) g.create(x, y, width, height);
			
			g2d.scale(1f / SystemAnalysis.getHiDPIScaleFactor(), 1f / SystemAnalysis.getHiDPIScaleFactor());
			g2d.drawImage(image, 0, 0, observer);
			g2d.scale(1, 1);
			g2d.dispose();
		}
	}
	
	/**
	 * @param w
	 *           negative values have special meaning, they are ignored
	 * @param h
	 *           at least w or h needs to be positive
	 */
	public static BufferedImage getScaledImage(BufferedImage icon, int w, int h) {
		if (icon.getWidth() <= w && icon.getHeight() <= h)
			return icon;
		try {
			double srcWidth = icon.getWidth();
			double srcHeight = icon.getHeight();
			
			// ignore negative target values so that the other value is solely used
			double longSideForSource = w > 0 ? (h > 0 ? (double) Math.max(srcWidth, srcHeight) : srcWidth) : srcHeight;
			double longSideForDest = w > 0 ? (h > 0 ? (double) Math.max(w, h) : w) : h;
			double multiplier = longSideForDest / longSideForSource;
			int destWidth = (int) (srcWidth * multiplier);
			int destHeight = (int) (srcHeight * multiplier);
			
			boolean useNewCode = true;
			
			if (useNewCode) {
				return Scalr.resize(icon, Scalr.Method.ULTRA_QUALITY, destWidth, destHeight);
			}
			
			double abc = 2d;
			if (Math.abs(w) < 32)
				abc = 2;
			int blur = (int) (abc / multiplier);
			
			icon = blurImage(icon, blur);
			
			BufferedImage destImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = destImage.createGraphics();
			AffineTransform affineTransform = AffineTransform.getScaleInstance(multiplier, multiplier);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.drawImage(icon, affineTransform, null);
			
			return destImage;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return icon;
		}
	}
	
	// public static Image toHiDPIResolution(final Image image) {
	// if (!SystemAnalysis.isRetina()) {
	// return image;
	// }
	//
	// Image hiDPICapableImage = image;
	// try {
	// final Class cImageClass = Class.forName("apple.awt.CImage");
	// final Method getCreator = cImageClass.getDeclaredMethod("getCreator");
	// getCreator.setAccessible(true);
	// final Object creator = getCreator.invoke(null);
	// final Image nativeImage = (Image) creator.getClass()
	// .getMethod("createImage", Image.class).invoke(creator, image);
	//
	// final Method getNSImage = nativeImage.getClass()
	// .getDeclaredMethod("getNSImage");
	// getNSImage.setAccessible(true);
	// final Object pointer = getNSImage.invoke(nativeImage);
	// final float scaleFactor = SystemAnalysis.getHiDPIScaleFactor();
	// final int scaledWidth = (int) (image.getWidth(null) / scaleFactor);
	// final int scaledHeight = (int) (image.getHeight(null) / scaleFactor);
	// hiDPICapableImage = (BufferedImage) creator.getClass()
	// .getMethod("createImageWithSize", Long.TYPE, Integer.TYPE, Integer.TYPE)
	// .invoke(creator, pointer, scaledWidth, scaledHeight);
	//
	// // dereference first image, to avoid double release
	// final Field fNSImage = nativeImage.getClass().getDeclaredField("fNSImage");
	// fNSImage.setAccessible(true);
	// fNSImage.setLong(nativeImage, 0L);
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// return hiDPICapableImage;
	// }
	
	public static ShowImage showImage(BufferedImage img, String title) {
		JFrame frame = new JFrame(title);
		ShowImage panel = new ShowImage(img);
		frame.getContentPane().add(panel);
		frame.setSize(img.getWidth(), img.getHeight());
		frame.setVisible(true);
		return panel;
	}
	
	private static HashSet<MemoryHogInterface> memoryHogs = new HashSet<MemoryHogInterface>();
	
	public static JLabel getMemoryInfoLabel(final boolean shortInfo) {
		final JLabel memLabel = new JLabel(getCurrentMemoryInfo(false));
		memLabel.setToolTipText("Click for memory garbage collection (incl. Database Flush)<br>Shift-Click for pure GC.");
		memLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				freeMemory(!e.isShiftDown());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		Timer t = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				memLabel.setText(getCurrentMemoryInfo(shortInfo));
				if (shortInfo)
					memLabel.setToolTipText(getCurrentMemoryInfo(false).replaceFirst(":", " (click to garbage-collect):")
							.replaceFirst("<font color='gray'>", ""));
				memLabel.repaint(1000);
			}
		});
		t.start();
		return memLabel;
	}
	
	private static String getCurrentMemoryInfo(boolean shortInfo) {
		Runtime r = Runtime.getRuntime();
		int divisor = 1024;
		String memoryConfig;
		if (shortInfo) {
			memoryConfig = ((r.totalMemory() / divisor / divisor) - (r.freeMemory() / divisor / divisor)) + ""
					+ "&nbsp;MB";
			return "<html>" + "<font color='gray'><small>" + memoryConfig + "<br>"
					+ MainFrame.getInstance().getNumberOfOpenSessions() + "&nbsp;ES";
			
		} else {
			memoryConfig = ((r.totalMemory() / divisor / divisor) - (r.freeMemory() / divisor / divisor)) + "" + " MB, "
					+ (r.totalMemory() / divisor / divisor) + " MB, " + (r.maxMemory() / divisor / divisor) + " MB";
			return "<html>" + "<font color='gray'><small>Memory info:<br>&nbsp;&nbsp;&nbsp;active, alloc, max memory: "
					+ memoryConfig;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ImageIcon loadIcon(Class class1, String name) {
		return loadIcon(class1, name, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public static ImageIcon peekIcon(Class class1, String name) {
		return peekIcon(class1, name, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	// private static WeakHashMap<String, BufferedImage> cachedIcons = new WeakHashMap<String, BufferedImage>();
	private static HashMap<String, BufferedImage> cachedIcons = new HashMap<String, BufferedImage>();
	
	public static ImageIcon loadIcon(Class class1, String name, int w, int h) {
		return loadIcon(class1, name, w, h, true);
	}
	
	public static ImageIcon peekIcon(Class class1, String name, int w, int h) {
		return peekIcon(class1, name, w, h, true);
	}
	
	public static ImageIcon peekIcon(Class class1, String name, int w, int h, boolean warnIfNotFound) {
		String id = class1.getCanonicalName() + ";" + name + ";" + w + ";" + h;
		BufferedImage ci = cachedIcons.get(id);
		if (ci != null)
			return new ImageIcon(ci);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public static ImageIcon loadIcon(Class class1, String name, int w, int h, boolean warnIfNotFound) {
		String id = class1.getCanonicalName() + ";" + name + ";" + w + ";" + h;
		BufferedImage ci = cachedIcons.get(id);
		if (ci != null)
			return new ImageIcon(ci);
		ImageIcon result = null;
		URL url = getResource(class1, name);
		BufferedImage img = url == null ? null : GravistoService.getScaledImage(new ImageIcon(url).getImage(), w, h);
		if (url == null)
			result = null;
		else
			result = new ImageIcon(img);
		if (img != null)
			cachedIcons.put(id, img);
		else
			if (warnIfNotFound) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Could not load icon '" + name + "' !");
				return loadIcon(GravistoService.class, "images/Stop24.gif", w, h, false);
			}
		return result;
	}
	
	public static BufferedImage loadImage(@SuppressWarnings("rawtypes") Class class1, String name, int w, int h) throws Exception {
		URL url = getResource(class1, name);
		if (url == null)
			return null;
		else {
			BufferedImage bi = GravistoService.getScaledImage(new ImageIcon(url).getImage(), w, h);
			return bi;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static URL getResource(Class class1, String name) {
		return getResource(class1, name, null);
	}
	
	@SuppressWarnings("unchecked")
	public static URL getResource(Class location, String filename, String optExt) {
		ClassLoader cl = location.getClassLoader();
		String path = location.getPackage().getName().replace('.', '/');
		if (optExt == null)
			return cl.getResource(path + "/" + filename);
		else
			return cl.getResource(path + "/" + filename + "." + optExt);
	}
	
	@SuppressWarnings("unchecked")
	public static IOurl getIOurl(Class location, String filename, String optExt) throws IOException {
		URL rr = getResource(location, filename);
		if (rr == null)
			throw new IOException("Could not get resource URL for file " + filename + ". Reference class: " + location);
		String fn = rr.getFile();
		if (fn == null)
			throw new IOException("Could not get file name info for file " + filename + ". Reference class: " + location);
		File f = new File(fn);
		
		return FileSystemHandler.getURL(f);
	}
	
	public static void addKnownMemoryHog(MemoryHogInterface memoryHog) {
		synchronized (memoryHogs) {
			for (MemoryHogInterface mh : memoryHogs)
				if (mh.getClass() == memoryHog.getClass())
					return;
			memoryHogs.add(memoryHog);
		}
	}
	
	public static void ensureActiveViewAndSession(MouseEvent e) {
		try {
			View v = (View) ErrorMsg.findParentComponent(e.getComponent(), View.class);
			
			Iterator<Session> itSessions = MainFrame.getSessions().iterator();
			boolean found = false;
			while (itSessions.hasNext() && !found) {
				Session mySession = itSessions.next();
				Iterator<View> itViews = mySession.getViews().iterator();
				while (itViews.hasNext() && !found) {
					View myView = itViews.next();
					if (myView == v) {
						mySession.setActiveView(myView);
						if (GravistoService.getInstance().getMainFrame().getActiveEditorSession() != mySession)
							GravistoService.getInstance().getMainFrame().setActiveSession(mySession, myView);
						found = true;
					}
				}
			}
		} finally {
			
		}
	}
	
	public static String getFileModificationDateAndTime(File file, String returnIfNotFound) {
		File f = file;
		if (f.exists()) {
			long lm = f.lastModified();
			Date lmd = new Date(lm);
			return lmd.toString();
		} else
			return "file not found";
	}
	
	/**
	 * Prints a log message in case this method is not called on the Event
	 * Dispatch Thread.
	 */
	public static void checkEventDispatchThread() {
		if (!SwingUtilities.isEventDispatchThread()) {
			System.out.println("Method Call not on Event Dispatch Thread:");
			Thread.dumpStack();
		}
	}
	
	public EditorSession getSessionFromGraph(Graph graph) {
		for (EditorSession es : MainFrame.getEditorSessions()) {
			if (es.getGraph() == graph)
				return es;
		}
		return null;
	}
	
	public static void freeMemory(boolean freeAlsoMemoryHogs) {
		if (freeAlsoMemoryHogs)
			synchronized (memoryHogs) {
				for (MemoryHogInterface mh : memoryHogs) {
					mh.freeMemory();
				}
			}
		System.gc();
	}
	
	// based on
	// http://www.tutorials.de/forum/java/255281-zip-entpacken-problem.html
	public static void unzipFile(File archive) throws Exception {
		try {
			File destDir = new File(archive.getParent());
			if (!destDir.exists()) {
				destDir.mkdir();
			}
			
			ZipFile zipFile = new ZipFile(archive);
			Enumeration<?> entries = zipFile.entries();
			
			byte[] buffer = new byte[16384];
			int len;
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				
				String entryFileName = entry.getName();
				
				File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				
				if (!entry.isDirectory()) {
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
							new File(destDir, entryFileName)));
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
					
					while ((len = bis.read(buffer)) > 0) {
						bos.write(buffer, 0, len);
					}
					
					bos.flush();
					bos.close();
					bis.close();
				}
			}
			zipFile.close();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static File buildDirectoryHierarchyFor(String entryName, File destDir) {
		int lastIndex = entryName.lastIndexOf('/');
		// String entryFileName = entryName.substring(lastIndex + 1);
		String internalPathToEntry = entryName.substring(0, lastIndex + 1);
		return new File(destDir, internalPathToEntry);
	}
	
	/**
	 * ToDo Test this method (untested copy).
	 */
	@SuppressWarnings("unchecked")
	public static void saveResource(Class reference, String folder, String fileName, String targetFileName)
			throws IOException {
		ClassLoader cl = reference.getClassLoader();
		
		String path = reference.getPackage().getName().replace('.', '/');
		
		File tgt = new File(targetFileName);
		FileOutputStream out = new FileOutputStream(tgt);
		long sz = 0;
		InputStream inpS = cl.getResourceAsStream(path + "/" + folder + "/" + fileName);
		InputStream in = inpS;
		
		int b;
		while ((b = inpS.read()) != -1) {
			out.write(b);
			sz++;
		}
		sz = sz / 1024;
		in.close();
		out.close();
	}
	
	public static void enableContent(JComponent comp, boolean enabled) {
		for (int i = 0; i < comp.getComponentCount(); i++)
			if (comp.getComponent(i) instanceof JComponent)
				enableContent((JComponent) comp.getComponent(i), enabled);
		comp.setEnabled(enabled);
	}
	
	public static String getHashFromFile(String filename, HashType type) throws Exception {
		File f = new File(filename);
		return getHashFromFile(f, type);
	}
	
	public static String getHashFromFile(File f, HashType type) throws Exception {
		return getHashFromInputStream(new FileInputStream(f), null, type);
	}
	
	public static String getHashFromInputStream(InputStream is, HashType type) throws Exception {
		return getHashFromInputStream(is, null, type);
	}
	
	public static String getHashFromInputStream(InputStream is, ObjectRef optFileSize, HashType type) throws Exception {
		return getHashFromInputStream(new InputStream[] { is }, new ObjectRef[] { optFileSize }, type, false)[0];
	}
	
	public static String[] getHashFromInputStream(final InputStream[] iss, final ObjectRef[] optFileSize, final HashType type, boolean threaded)
			throws Exception {
		if (iss == null)
			return null;
		
		ArrayList<Thread> tl = new ArrayList<Thread>();
		final ArrayList<ObjectRef> resultList = new ArrayList<ObjectRef>();
		for (int i = 0; i < iss.length; i++)
			resultList.add(new ObjectRef());
		for (int i = 0; i < iss.length; i++) {
			final int iii = i;
			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					InputStream is = iss[iii];
					if (is == null)
						return;
					MessageDigest digest;
					try {
						digest = MessageDigest.getInstance(type.toString());
					} catch (NoSuchAlgorithmException e1) {
						throw new Error(e1);
					}
					byte[] buffer = new byte[1024 * 1024];
					int read = 0;
					long len = 0;
					try {
						while ((read = is.read(buffer)) > 0) {
							len += read;
							digest.update(buffer, 0, read);
						}
						if (optFileSize[iii] != null)
							optFileSize[iii].addLong(len);
					} catch (IOException e) {
						ErrorMsg.addErrorMessage(e);
					} finally {
						try {
							is.close();
						} catch (IOException e) {
							ErrorMsg.addErrorMessage(e);
						}
					}
					
					byte[] md5sum = digest.digest();
					BigInteger bigInt = new BigInteger(1, md5sum);
					String output = bigInt.toString(16);
					resultList.get(iii).setObject(output);
				}
			});
			t1.setName("Hash Calculation (Stream " + i + ")");
			if (threaded)
				t1.start();
			else
				t1.run();
			tl.add(t1);
		}
		if (threaded)
			for (int i = 0; i < iss.length; i++)
				tl.get(i).join();
		String[] res = new String[iss.length];
		for (int i = 0; i < iss.length; i++)
			res[i] = (String) resultList.get(i).getObject();
		return res;
	}
	
	/**
	 * Method <code>showViewChooserDialog </code> invokes a view chooser dialog
	 * for choosing view types. The parameter withNewSession specifies whether
	 * the new view starts within an existing session or within a new session.
	 * 
	 * @param session
	 *           the session in which to open the new view.
	 * @param returnScrollPane
	 *           DOCUMENT ME!
	 * @param e
	 * @param interaction
	 * @return DOCUMENT ME!
	 */
	public JScrollPane showViewChooserDialog(final EditorSession session, boolean returnScrollPane, ActionEvent e,
			LoadSetting interaction, final ConfigureViewAction configNewView) {
		if (!returnScrollPane && !SwingUtilities.isEventDispatchThread())
			ErrorMsg.addErrorMessage("Internal Error: showViewChooserDialog not on event dispatch thread");
		String[] views;
		if (ManagerManager.getInstance(null).viewManager != null)
			views = ManagerManager.getInstance(null).viewManager.getViewNames();
		else
			views = new String[] { "org.graffiti.plugins.views.defaults.GraffitiView" };
		if (views.length == 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), MainFrame.getInstance().sBundle.getString("viewchooser.pluginNotAdded"),
					MainFrame.getInstance().sBundle.getString("viewchooser.errorDialog.title"), JOptionPane.ERROR_MESSAGE);
		} else
			if (views.length == 1) {
				if (MainFrame.getInstance().sessions.contains(session)) {
					return MainFrame.getInstance().createInternalFrame(views[0], session.getGraph().getName(), returnScrollPane, false);
				} else {
					JScrollPane jsp = (JScrollPane) MainFrame.getInstance().createInternalFrame(views[0], session.getGraph().getName(), session,
							returnScrollPane, false, false, configNewView, true);
					return jsp;
				}
			} else {
				if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
					if (interaction == LoadSetting.VIEW_CHOOSER_FOR_LARGE_GRAPHS_ONLY
							&& session.getGraph() != null
							&& ((session.getGraph().getNumberOfNodes() + session.getGraph().getNumberOfEdges() > 1000) || ((e != null && (e
									.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK))))
						interaction = LoadSetting.VIEW_CHOOSER_ALWAYS; // show view
					// chooser dialog
					// in case the
					// graph size is
					// large
				}
				if (interaction == LoadSetting.VIEW_CHOOSER_FOR_LARGE_GRAPHS_ONLY)
					interaction = LoadSetting.VIEW_CHOOSER_NEVER;
				
				// from here on only VIEW_CHOOSER_ALWAYS or
				// VIEW_CHOOSER_NEVER_ALWAYS_DEFAULT should be set
				
				if (interaction == LoadSetting.VIEW_CHOOSER_NEVER
						|| interaction == LoadSetting.VIEW_CHOOSER_NEVER_SHOW_DONT_ADD_VIEW_TO_EDITORSESSION) {
					String defaultView = ManagerManager.getInstance(null).viewManager.getDefaultView();
					if (MainFrame.getInstance() != null && MainFrame.getInstance().sessions.contains(session)) {
						return MainFrame.getInstance().createInternalFrame(defaultView, session.getGraph().getName(), session, returnScrollPane, false,
								configNewView);
					} else {
						Graph g = session.getGraph();
						String name = null;
						if (g != null)
							name = g.getName();
						if (name == null)
							name = "[NULL]";
						JScrollPane jsp = (JScrollPane) MainFrame.getInstance().createInternalFrame(defaultView, name, session, returnScrollPane, false,
								false, configNewView, interaction != LoadSetting.VIEW_CHOOSER_NEVER_SHOW_DONT_ADD_VIEW_TO_EDITORSESSION);
						return jsp;
					}
				} else {
					// interaction is VIEW_CHOOSER_ALWAYS
					ViewTypeChooser viewChooser = new ViewTypeChooser(MainFrame.getInstance(), MainFrame.getInstance().sBundle.getString("viewchooser.title") + " ("
							+ session.getGraph().getNumberOfNodes() + " nodes, " + session.getGraph().getNumberOfEdges()
							+ " edges)", ManagerManager.getInstance(null).viewManager.getViewDescriptions());
					
					viewChooser.setLocationRelativeTo(MainFrame.getInstance());
					viewChooser.setVisible(true);
					
					// The user did not select a view.
					if (viewChooser.getSelectedView() == -1) {
						return null;
					}
					
					final String selectedView = views[viewChooser.getSelectedView()];
					
					if (viewChooser.getUserSelectionCreateInternalFrame()) {
						if (selectedView != null) {
							if (MainFrame.getInstance().sessions.contains(session)) {
								return MainFrame.getInstance().createInternalFrame(selectedView, session.getGraph().getName(), session, returnScrollPane,
										false, configNewView);
							} else {
								return (JScrollPane) MainFrame.getInstance().createInternalFrame(selectedView, session.getGraph().getName(), session,
										returnScrollPane, false, false, configNewView, true);
							}
						}
					} else {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								GraffitiInternalFrame gif = (GraffitiInternalFrame) MainFrame.getInstance().createInternalFrame(selectedView, session
										.getGraph().getName(), session, false, true, false, configNewView, true);
								GraffitiFrame gf = new GraffitiFrame(gif, false);
								gf.setExtendedState(Frame.MAXIMIZED_BOTH);
								gf.setVisible(true);
								MainFrame.getInstance().addDetachedFrame(gf);
							}
						});
					}
				}
			}
		
		return null;
	}
	
	public static void processUrlTextContent(IOurl url, LineProcessor lineProcessor) throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
		String str;
		while ((str = in.readLine()) != null) {
			lineProcessor.process(str);
		}
		in.close();
	}
	
	public static void setProxy() {
		boolean useProxy = SystemOptions.getInstance().getBoolean("Network", "http-proxy-enabled", false);
		String proxyUrl = SystemOptions.getInstance().getString("Network", "http-proxy-host", "");
		Integer proxyPort = SystemOptions.getInstance().getInteger("Network", "http-proxy-port", 3128);
		/* http.nonProxyHosts:a list of hosts that should be reached directly, bypassing the proxy. 
		This is a list of patterns separated by '|'. The patterns may start or end with a '*' for wildcards. 
		Any host matching one of these patterns will be reached through a direct connection instead of through a proxy. */
		String proxyBypass = SystemOptions.getInstance().getString("Network", "host(s)-for-proxybypass", "");
		
		boolean useSocksProxy = SystemOptions.getInstance().getBoolean("Network", "socks-proxy-enabled", false);
		String socksProxyUrl = SystemOptions.getInstance().getString("Network", "socks-proxy-host", "");
		Integer socksProxyPort = SystemOptions.getInstance().getInteger("Network", "socks-proxy-port", 1080);
		
		boolean useProxyAuth = SystemOptions.getInstance().getBoolean("Network", "Proxy-Authentication-enable", false);
		final String proxyUser = SystemOptions.getInstance().getString("Network", "Proxy-Authentication-user", "");
		final String proxyPass = SystemOptions.getInstance().getString("Network", "Proxy-Authentication-password", "");
		
		if (useProxy) {
			// HTTP/HTTPS Proxy
			System.setProperty("http.proxyHost", proxyUrl);
			System.setProperty("http.proxyPort", proxyPort + "");
			System.setProperty("https.proxyHost", proxyUrl);
			System.setProperty("https.proxyPort", proxyPort + "");
			System.setProperty("http.nonProxyHosts", proxyBypass);
			if (useProxyAuth) {
				System.setProperty("http.proxyUser", proxyUser);
				System.setProperty("http.proxyPassword", proxyPass);
			}
		}
		if (useSocksProxy) {
			// SOCKS Proxy
			System.setProperty("socksProxyHost", socksProxyUrl);
			System.setProperty("socksProxyPort", socksProxyPort + "");
			if (useProxyAuth) {
				System.setProperty("java.net.socks.username", proxyUser);
				System.setProperty("java.net.socks.password", proxyPass);
			}
		}
		if (useProxyAuth && (useProxy || useSocksProxy))
			Authenticator.setDefault(
					new Authenticator() {
						@Override
						public PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(
									proxyUser, proxyPass.toCharArray());
						}
					}
					);
	}
	
	@Override
	public void freeMemory() {
		cachedIcons.clear();
	}
}