// ==============================================================================
//
// DefaultPluginManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultPluginManager.java,v 1.2 2011-02-05 20:33:31 klukas Exp $

package org.graffiti.managers.pluginmgr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.graffiti.core.StringBundle;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;
import org.graffiti.util.PluginHelper;
import org.graffiti.util.ProgressViewer;
import org.graffiti.util.StringSplitter;

/**
 * Manages the list of plugins.
 * 
 * @version $Revision: 1.2 $
 */
public class DefaultPluginManager
					implements PluginManager {
	// ~ Static fields/initializers =============================================
	
	public static DefaultPluginManager lastInstance = null;
	
	/** The logger for the current class. */
	private static final Logger logger = Logger.getLogger(DefaultPluginManager.class.getName());
	
	// ~ Instance fields ========================================================
	
	/** The <code>StringBundle</code> of the plugin manager. */
	protected StringBundle sBundle = StringBundle.getInstance();
	
	/**
	 * Maps from a plugin name (<code>String</code>) to a plugin entry
	 * (<code>Entry</code>).
	 */
	private Hashtable<String, PluginEntry> pluginEntries;
	
	/**
	 * Holds the plugin entries of the last search. This avoids researching
	 * everytime a dependent plugin is automatically searched.
	 */
	// private List<PluginEntry> entries;
	
	/** The list of plugin manager listeners. */
	private List<PluginManagerListener> pluginManagerListeners;
	
	/** The preferences of the plugin manager. */
	private GravistoPreferences prefs;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new <code>PluginManger</code> instance.
	 * 
	 * @param prefs
	 *           the preferences, which contain information about what to
	 *           load during the instanciation of the plugin manager.
	 */
	public DefaultPluginManager(GravistoPreferences prefs) {
		this.prefs = prefs;
		this.pluginEntries = new Hashtable<String, PluginEntry>();
		this.pluginManagerListeners = new LinkedList<PluginManagerListener>();
		lastInstance = this;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Checks if the plugin is already installed, i.e. if the plugin's name is
	 * in the list of plugin entries.
	 * 
	 * @param name
	 *           name of the plugin.
	 * @return <code>true</code> if the plugin's name is in the list of plugin
	 *         entries, <code>false</code> otherwise.
	 */
	public boolean isInstalled(String name) {
		if (name == null)
			return true;
		else
			return pluginEntries.containsKey(name);
	}
	
	/**
	 * Sets the <code>loadOnStartup</code> flag of the given object, to the
	 * given value.
	 * 
	 * @param name
	 *           the name of the plugin.
	 * @param loadOnStartup
	 *           <code>true</code>, if the plugin should be loaded
	 *           at startup.
	 */
	public void setLoadOnStartup(String name, Boolean loadOnStartup) {
		(pluginEntries.get(name)).setLoadOnStartup(loadOnStartup);
	}
	
	/**
	 * Returns the corrent list of plugin entries.
	 * 
	 * @return a <code>Collection</code> containing all the plugin entries.
	 */
	public Collection<PluginEntry> getPluginEntries() {
		synchronized (pluginEntries) {
			return new ArrayList<PluginEntry>(pluginEntries.values());
		}
	}
	
	/**
	 * Returns the plugin instance of the given plugin name.
	 * 
	 * @param name
	 *           the name of the plugin.
	 * @return the instance of the plugin of the given name.
	 */
	public GenericPlugin getPluginInstance(String name) {
		synchronized (pluginEntries) {
			if (pluginEntries.get(name) != null)
				return (pluginEntries.get(name)).getPlugin();
			else
				return null;
		}
	}
	
	/**
	 * Adds the given plugin manager listener to the list of listeners.
	 * 
	 * @param listener
	 *           the new listener to add to the list.
	 */
	public void addPluginManagerListener(PluginManagerListener listener) {
		synchronized (pluginManagerListeners) {
			pluginManagerListeners.add(listener);
		}
	}
	
	/**
	 * Returns a new instance of the plugin &quot;main&quot; class with the
	 * given plugin name.
	 * 
	 * @param pluginLocation
	 *           the URL to the plugin.
	 * @return the instantiated plugin.
	 * @exception PluginManagerException
	 *               an error occured while loading or
	 *               instantiating the plugin.
	 */
	public GenericPlugin createInstance(URL pluginLocation)
						throws PluginManagerException {
		return createInstance(pluginLocation, null);
	}
	
	/**
	 * Returns a new instance of the plugin &quot;main&quot; class with the
	 * given plugin name. The progress made is displayed with progressViewer.
	 * 
	 * @param pluginLocation
	 *           the URL to the plugin.
	 * @param progressViewer
	 *           the progress viewer that display the progress made
	 * @return the instantiated plugin.
	 * @exception PluginManagerException
	 *               an error occured while loading or
	 *               instantiating the plugin.
	 */
	public GenericPlugin createInstance(URL pluginLocation,
						ProgressViewer progressViewer)
						throws PluginManagerException {
		PluginDescription description = PluginHelper.readPluginDescription(pluginLocation);
		
		GenericPlugin pluginInstance = createInstance(description,
							progressViewer);
		
		// // add the plugin to the list of instanciated plugins.
		// addPlugin(description, pluginInstance, pluginLocation, Boolean.TRUE);
		//
		// // inform all listeners about the new plugin.
		// firePluginAdded(pluginInstance, description);
		return pluginInstance;
	}
	
	/**
	 * Loads the plugin from the given location.
	 * 
	 * @param description
	 *           DOCUMENT ME!
	 * @param pluginLocation
	 *           the location of the plugin.
	 * @param loadOnStartup
	 *           <code>true</code>, if the given plugin should be
	 *           loaded at the startup.
	 * @exception PluginManagerException
	 *               if an error occurs while loading or
	 *               instantiating the plugin.
	 */
	public void loadPlugin(PluginDescription description, URL pluginLocation,
						Boolean loadOnStartup)
						throws PluginManagerException {
		loadPlugins(new PluginEntry[] { new DefaultPluginEntry(pluginLocation.toString(), description) });
	}
	
	/**
	 * Loads the plugin from the given location.
	 * 
	 * @param plugins
	 *           the plugin entries describing the plugins to be loaded
	 * @exception PluginManagerException
	 *               if an error occurs while loading or
	 *               instantiating the plugin.
	 */
	public void loadPlugins(PluginEntry[] plugins)
						throws PluginManagerException {
		loadPlugins(plugins, null);
	}
	
	public void loadPlugins(PluginEntry[] plugins, ProgressViewer progressViewer) throws PluginManagerException {
		loadPlugins(plugins, progressViewer, false);
	}
	
	/**
	 * Loads the plugin from the given location.The progress made is displayed
	 * with progressViewer.
	 * 
	 * @param plugins
	 *           the plugin entries describing the plugins to be loaded
	 * @param progressViewer
	 *           the progress viewer that display the progress made
	 * @exception PluginManagerException
	 *               if an error occurs while loading or
	 *               instantiating the plugin.
	 */
	public void loadPlugins(
						final PluginEntry[] plugins,
						final ProgressViewer progressViewer,
						final boolean doAutomatic)
						throws PluginManagerException {
		if (progressViewer != null)
			progressViewer.setText("Analyze plugin dependencies...");
		
		HashMap<String, PluginEntry> name2plugin = new HashMap<String, PluginEntry>();
		for (PluginEntry plugin : plugins) {
			if (plugin.getDescription() == null) {
				System.err.println("Invalid plugin description for " + plugin.getFileName());
			} else {
				if (name2plugin.containsKey(plugin.getDescription().getName()))
					System.err.println("Non-unique plugin name: " + plugin.getDescription().getName() + " // " + plugin.getFileName());
				name2plugin.put(plugin.getDescription().getName(), plugin);
			}
		}
		for (PluginEntry plugin : plugins) {
			if (plugin.getDescription() == null)
				continue;
			List<PluginDependency> deps = plugin.getDescription().getDependencies();
			if (deps != null && deps.size() > 0) {
				for (PluginDependency dep : deps) {
					PluginEntry pe = name2plugin.get(dep.getName());
					if (pe == null)
						System.err.println("Plugin " + dep.getName() + " is unknown! (required by " + plugin.getFileName() + ")");
					else {
						if (pe.getDescription() == null)
							System.err.println("Plugin definition " + pe.getFileName() + " provides no description!");
						else
							pe.getDescription().addChild(plugin);
					}
				}
			}
		}
		
		final HashSet<String> loading = new HashSet<String>();
		
		if (progressViewer != null)
			progressViewer.setText("Load priority plugins...");
		
		ExecutorService runVIP;
		if (ReleaseInfo.isRunningAsApplet())
			runVIP = Executors.newFixedThreadPool(1);
		else
			runVIP = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		loadSetOfPlugins(plugins, progressViewer, runVIP, loading, true);
		if (!ReleaseInfo.isRunningAsApplet())
			runVIP.shutdown();
		
		if (progressViewer != null)
			progressViewer.setText("Load plugins...");
		
		ExecutorService run;
		if (ReleaseInfo.isRunningAsApplet())
			run = Executors.newFixedThreadPool(1);
		else
			run = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		loadSetOfPlugins(plugins, progressViewer, run, loading, false);
		if (!ReleaseInfo.isRunningAsApplet())
			run.shutdown();
		
		if (!ReleaseInfo.isRunningAsApplet()) {
			int maxTime = 60;
			try {
				if (run.awaitTermination(maxTime, TimeUnit.SECONDS)) {
					if (progressViewer != null)
						progressViewer.setText("All plugins loaded!");
				} else {
					synchronized (loading) {
						System.err.println("Loading of plugin " + loading.size() + " not finished (time-out).");
						System.err.println("Possible error causes (intialization time over " + maxTime + " seconds):");
						System.err.println("* Plugin implementation errors");
						System.err.println("* A very slow computer, or starting the application under high system load");
						if (progressViewer != null)
							progressViewer.setText("Time-out: " + loading.size() + " plugins not initialized!");
					}
					Thread.sleep(5000);
				}
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		synchronized (loading) {
			for (String s : loading)
				System.err.println("Loading of plugin " + s + " not finished (time-out).");
		}
		savePrefs();
	}
	
	private void loadSetOfPlugins(final PluginEntry[] plugins,
						final ProgressViewer progressViewer, ExecutorService run,
						final HashSet<String> loading, boolean loadPriorityPlugins) {
		for (PluginEntry plugin : plugins) {
			final URL pluginUrl;
			try {
				pluginUrl = plugin.getPluginUrl();
			} catch (MalformedURLException e1) {
				ErrorMsg.addErrorMessage(e1);
				continue;
			}
			final PluginDescription desc = plugin.getDescription();
			if (desc == null)
				System.err.println("Invalid plugin description for file " + pluginUrl.toString());
			else
				if ((loadPriorityPlugins && desc.isPriorityPlugin()) || (!loadPriorityPlugins && !desc.isPriorityPlugin())) {
					Runnable r = new Runnable() {
						public void run() {
							try {
								if (desc.getDependencies().size() > 0)
									return;
								
								synchronized (loading) {
									loading.add(desc.getName());
								}
								
								if (!loadPlugin(pluginUrl, desc, progressViewer)) {
									ErrorMsg.addErrorMessage("ERROR: could not load plugin: " + pluginUrl);
								} else {
									synchronized (loading) {
										loading.remove(desc.getName());
									}
									loadChilds(desc);
								}
								
							} catch (PluginAlreadyLoadedException info) {
								System.out.println(info.getMessage());
								// can be ignored
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
						
						private void loadChilds(PluginDescription desc) {
							for (PluginEntry pe : desc.getChildPlugins()) {
								URL url;
								try {
									url = pe.getPluginUrl();
									synchronized (loading) {
										loading.add(pe.getDescription().getName());
									}
									if (!loadPlugin(url, pe.getDescription(), progressViewer))
										ErrorMsg.addErrorMessage("ERROR: could not load plugin: " + url);
									else {
										synchronized (loading) {
											loading.remove(pe.getDescription().getName());
										}
										loadChilds(pe.getDescription());
									}
								} catch (Exception err) {
									ErrorMsg.addErrorMessage(err);
									continue;
								}
							}
							
						}
					};
					if (!ReleaseInfo.isRunningAsApplet())
						run.submit(r);
					else
						r.run();
				}
		}
	}
	
	/**
	 * @param plugins
	 * @param progressViewer
	 * @param messages
	 * @param loadLater
	 * @param i
	 * @throws PluginManagerException
	 */
	private boolean loadPlugin(URL pluginUrl, PluginDescription desc,
						ProgressViewer progressViewer) throws PluginManagerException {
		
		if (desc == null)
			return true;
		
		List<?> deps = null;
		if (desc != null)
			deps = desc.getDependencies();
		
		if ((deps == null) || deps.isEmpty()) {
			return addPlugin(desc, pluginUrl, Boolean.TRUE, progressViewer);
		} else {
			// check if deps are satisfied
			boolean satisfied = true;
			
			for (Iterator<?> it = deps.iterator(); it.hasNext();) {
				PluginDependency dep = (PluginDependency) it.next();
				
				if (!pluginEntries.containsKey(dep.getName())) {
					// System.out.println("Plugin "+desc.getName()+" needs "+dep.getName()+"!");
					
					satisfied = false;
					break;
				}
			}
			
			if (satisfied) {
				return addPlugin(desc, pluginUrl, Boolean.TRUE, progressViewer);
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Loads the plugins which should be loaded on startup.
	 * 
	 * @exception PluginManagerException
	 *               if an error occurred while loading one
	 *               of the plugins.
	 */
	public void loadStartupPlugins()
						throws PluginManagerException {
		loadStartupPlugins(null);
	}
	
	/**
	 * Loads the plugins which should be loaded on startup. The progress made
	 * is displayed with progressViewer.
	 * 
	 * @param progressViewer
	 *           the progress viewer that display the progress made
	 * @exception PluginManagerException
	 *               if an error occurred while loading one
	 *               of the plugins.
	 */
	public void loadStartupPlugins(ProgressViewer progressViewer)
						throws PluginManagerException {
		// load the user's standard plugins
		int numberOfPlugins = prefs.getInt("numberOfPlugins", 0);
		
		// If available initialize the progressViewer
		if (progressViewer != null)
			progressViewer.setMaximum(numberOfPlugins);
		
		List<String> messages = new LinkedList<String>();
		
		PluginEntry[] pluginEntries = new PluginEntry[numberOfPlugins];
		
		int cnt = 0;
		
		for (int i = 1; i <= numberOfPlugins; i++) {
			String pluginLocation = prefs.get("pluginLocation" + i, null);
			
			if (pluginLocation != null) {
				try {
					URL pluginUrl = new URL(pluginLocation);
					PluginDescription desc = PluginHelper.readPluginDescription(pluginUrl);
					pluginEntries[cnt++] = new DefaultPluginEntry(pluginLocation,
										desc);
				} catch (MalformedURLException mue) {
					System.err.println(mue.getLocalizedMessage());
					messages.add(mue.getMessage());
				}
			}
		}
		
		try {
			loadPlugins(pluginEntries, progressViewer);
		} catch (PluginManagerException pme) {
			messages.add(pme.getMessage());
		}
		
		// collect info of all exceptions into one exception
		if (!messages.isEmpty()) {
			String msg = "";
			
			for (Iterator<String> itr = messages.iterator(); itr.hasNext();) {
				msg += ((String) itr.next() + "\n");
			}
			
			throw new PluginManagerException("exception.loadStartup\n",
								msg.trim());
		}
	}
	
	/**
	 * Removes the given plugin manager listener from the list of listeners.
	 * 
	 * @param listener
	 *           the listener to remove from the list of listeners.
	 */
	public void removePluginManagerListener(PluginManagerListener listener) {
		synchronized (pluginManagerListeners) {
			boolean success = pluginManagerListeners.remove(listener);
			
			if (!success) {
				logger.warning("trying to remove a non existing" +
									" plugin manager listener");
			}
		}
	}
	
	/**
	 * Saves the plugin manager's prefs.
	 * 
	 * @exception PluginManagerException
	 *               if an error occurrs while saving the
	 *               preferences.
	 */
	public void savePrefs()
						throws PluginManagerException {
		if (prefs == null)
			return;
		try {
			// get rid of the old preferences ...
			prefs.clear();
			
			// search for all plugins, which should be loaded at startup
			// and put their urls into this list
			List<URL> plugins = new LinkedList<URL>();
			
			for (Iterator<PluginEntry> i = pluginEntries.values().iterator(); i.hasNext();) {
				PluginEntry e = (PluginEntry) i.next();
				
				if (e.getLoadOnStartup().equals(Boolean.TRUE)) {
					plugins.add(e.getPluginLocation());
				}
			}
			
			// and write the new ones
			prefs.putInt("numberOfPlugins", plugins.size());
			
			int count = 1;
			
			for (Iterator<URL> i = plugins.iterator(); i.hasNext();) {
				prefs.put("pluginLocation" + count, i.next().toString());
				count++;
			}
		} catch (Exception e) {
			throw new PluginManagerException("exception.SavePrefs",
								e.getMessage());
		}
	}
	
	/**
	 * Adds the given plugin file to the list of plugins. The progress made is
	 * displayed with progressViewer.
	 * 
	 * @param description
	 *           the name of the plugin's main class.
	 * @param pluginLocation
	 *           the location of the given plugin.
	 * @param loadOnStartup
	 *           <code>true</code> if the plugin should be loaded on
	 *           the startup of the system, <code>false</code> otherwise.
	 * @param progressViewer
	 *           the progress viewer that display the progress made
	 * @throws PluginManagerException
	 *            DOCUMENT ME!
	 */
	private boolean addPlugin(PluginDescription description,
						URL pluginLocation, Boolean loadOnStartup, ProgressViewer progressViewer) throws PluginManagerException {
		// assert plugin != null;
		assert description != null;
		
		// create an instance of the plugin's main class
		GenericPlugin plugin = createInstance(description, progressViewer);
		
		if (plugin == null) {
			System.err.println("ERROR: COULD NOT CREATE PLUGIN");
			if (description != null)
				System.err.println("Description/Name: " + description.getName());
			else
				System.err.println("Plugin Description is NULL");
			if (pluginLocation != null)
				System.err.println("PluginLocation: " + pluginLocation.toString());
			else
				System.err.println("Plugin Location is NULL");
			
			String errMsg = "<br>ERROR: COULD NOT CREATE PLUGIN<br>";
			
			if (description != null)
				errMsg += "Plugin Description/Name: " + description.getName() + "<br>";
			else
				errMsg += "Plugin Description is NULL<br>";
			if (pluginLocation != null)
				errMsg += "PluginLocation: " + pluginLocation.toString() + "<br>";
			else
				errMsg += "Plugin Location is NULL";
			throw new PluginManagerException("Plugin Loading Failed", errMsg);
		}
		
		synchronized (pluginEntries) {
			pluginEntries.put(description.getName(),
								new DefaultPluginEntry(description, plugin, loadOnStartup,
													pluginLocation));
			// inform all listeners about the new plugin.
			if (!description.isOptional() || new org.SettingsHelperDefaultIsTrue().isEnabled(description.getName()))
				firePluginAdded(plugin, description);
		}
		
		// construct the path for the plugin in the preferences
		// e.g. org.graffiti.plugins.io.graphviz.DOTSerializerPlugin becomes
		// org/graffiti/plugins/io/graphviz/DOTSerializerPlugin
		String[] strings = StringSplitter.split(description.getMain(), ".");
		StringBuffer pluginNode = new StringBuffer();
		
		for (int i = 0; i < strings.length; i++) {
			pluginNode.append(strings[i]);
			
			if (i < (strings.length - 1)) {
				pluginNode.append("/");
			}
		}
		
		if (prefs != null) {
			GravistoPreferences pluginPrefs = prefs.node("pluginPrefs/" +
								pluginNode.toString());
			
			// configure the plugin's preferences
			if (plugin != null)
				plugin.configure(pluginPrefs);
		}
		return true;
	}
	
	/**
	 * Creates an instance of the plugin from its description. The progress
	 * made is displayed with progressViewer.
	 * 
	 * @param description
	 *           the description of the plugin to be instantiated
	 * @param progressViewer
	 *           the progress viewer that display the progress made
	 * @return the instantiated plugin.
	 * @exception PluginManagerException
	 *               an error occurrs while loading or
	 *               instantiating the plugin.
	 */
	private GenericPlugin createInstance(PluginDescription description,
						ProgressViewer progressViewer)
						throws PluginManagerException {
		
		if (description == null) {
			return null;
		}
		String name = description.getName();
		boolean loaded = false;
		if (isInstalled(name)) {
			loaded = true;
			if (!ReleaseInfo.isRunningAsApplet()) {
				System.err.println("Applet? " + ReleaseInfo.isRunningAsApplet());
				throw new PluginAlreadyLoadedException("Plugin name " + name + " already defined/plugin already loaded!");
			}
		}
		
		// If available show statustext to the user
		if (progressViewer != null)
			progressViewer.setText("Loading " + description.getName() + "...");
		
		GenericPlugin pluginInstance;
		
		try { // to instanciate the plugin's main class
			if (!loaded)
				pluginInstance = (GenericPlugin) InstanceLoader.createInstance(description.getMain());
			else {
				pluginInstance = getPluginInstance(description.getName());
			}
		} catch (InstanceCreationException ice) {
			System.out.println("Instance Creation Exception: " + description.getMain());
			throw new PluginManagerException(ice.getMessage() + " (cause: " + ice.getCause().getMessage() + ") ",
								description.toString());
		} catch (NoClassDefFoundError nce) {
			System.out.println("No Class Definition Found: " + description.getMain());
			throw new PluginManagerException(nce.getMessage() + " (cause: " + nce.getCause().getMessage() + ") ",
								description.toString());
		} catch (Throwable tre) {
			System.out.println("Instance Creation Exception: " + description.getMain());
			throw new PluginManagerException(tre.getMessage() + " (cause: " + tre.getCause().getMessage() + ") ",
												description.toString());
		}
		// update status (if available).
		if (progressViewer != null) {
			progressViewer.setText("Loading " + description.getName() + ": OK");
			progressViewer.setValue(progressViewer.getValue() + 1);
		}
		
		return pluginInstance;
	}
	
	/**
	 * Registers the plugin as a plugin manager listener, if it is of instance <code>PluginManagerListener</code> and calls the <code>pluginAdded</code> in all
	 * plugin manager listeners.
	 * 
	 * @param plugin
	 *           the added plugin.
	 * @param desc
	 *           the description of the added plugin.
	 */
	private void firePluginAdded(GenericPlugin plugin, PluginDescription desc) {
		// register the plugin as a plugin manager listener, if needed
		if (plugin instanceof PluginManagerListener) {
			addPluginManagerListener((PluginManagerListener) plugin);
		}
		
		ArrayList<PluginManagerListener> pml = new ArrayList<PluginManagerListener>();
		synchronized (pluginManagerListeners) {
			pml.addAll(pluginManagerListeners);
		}
		
		for (PluginManagerListener listener : pml) {
			if (plugin != null) {
				try {
					listener.pluginAdded(plugin, desc);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
	}
	
	public synchronized Collection<RSSfeedDefinition> getPluginFeeds() {
		ArrayList<RSSfeedDefinition> feeds = new ArrayList<RSSfeedDefinition>();
		ArrayList<PluginEntry> pes = new ArrayList<PluginEntry>();
		synchronized (pluginEntries) {
			pes.addAll(pluginEntries.values());
		}
		for (PluginEntry pe : pes) {
			if (pe.getDescription().hasRSSfeedDefined())
				feeds.add(pe.getDescription().getFeed());
		}
		
		return feeds;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
