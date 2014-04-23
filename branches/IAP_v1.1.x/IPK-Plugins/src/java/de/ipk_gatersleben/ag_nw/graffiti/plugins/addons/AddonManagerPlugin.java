/*
 * 
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.FeatureSet;
import org.HomeFolder;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.UpdateInfoResult;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.managers.pluginmgr.PluginXMLParser;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugins.inspectors.defaults.Inspector;
import org.graffiti.session.Session;
import org.graffiti.util.InstanceLoader;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.DragAndDropHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * This class manages Add-ons, which are Vanted-plugins loaded during runtime.
 * Usually such an Add-on brings more functionality than plugins, e.g. extending
 * Vanted by complete Flux-Balance Analysis or 3D-networks. Add-ons are
 * downloaded/distributed as a jar-file. An exemplary Add-on can be found in the
 * CVS and serve as a starting point.
 * 
 * @author Hendrik Rohn, Christian Klukas
 */
public class AddonManagerPlugin extends IPK_EditorPluginAdapter implements DragAndDropHandler {
	private static final String ADDONS_TO_BE_UPDATED_DIR = ReleaseInfo.getAppSubdirFolderWithFinalSep("addons_to_be_updated");
	private static final String ADDON_DIRECTORY = ReleaseInfo.getAppSubdirFolderWithFinalSep("addons");
	private static final File deactivatedlist = new File(ADDON_DIRECTORY + "deactivated.txt");
	
	private final ArrayList<Addon> addons = new ArrayList<Addon>();
	
	private static PluginXMLParser p = new PluginXMLParser();
	private static InputStream is;
	
	private static AddonManagerPlugin instance;
	private boolean startupAddonsLoaded = false;
	
	/**
	 * @return The Addonmanager instance or null, if no instance was created
	 *         (e.g. deselected)
	 */
	public synchronized static AddonManagerPlugin getInstance() {
		return instance;
	}
	
	/**
	 * Creates a new menu-entry for the Add-ons and waits for all plugins to be
	 * started. Then the Add-ons can be loaded.
	 */
	public AddonManagerPlugin() {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.ADDON_LOADING))
			return;
		instance = this;
		try {
			GravistoMainHelper.addDragAndDropHandler(this);
			// this.guiComponents = new GraffitiComponent[1];
			// this.guiComponents[0] = new AddonMenu(this);
			
			// waiting until all other plugins have been loaded, then load addons
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					waitLoop: while (true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// empty
						}
						if (ErrorMsg.getAppLoadingStatus() != ApplicationStatus.INITIALIZATION) {
							loadAddonsOnStartup();
							String currv = DBEgravistoHelper.DBE_GRAVISTO_VERSION;
							String oldv = ReleaseInfo.getOldVersionIfAppHasBeenUpdated(currv);
							int cnt = 0;
							synchronized (addons) {
								cnt = addons.size();
							}
							if (oldv != null && cnt > 0)
								showManageAddonDialog("<html>Application has been updated (" + oldv + " -> " + currv
										+ ").<br> Incompatible Add-ons were deactivated.<br>"
										+ "Use the 'Find Updates' function to look for updates.", true);
							startupAddonsLoaded = true;
							break waitLoop;
						}
					}
					ErrorMsg.setAppLoadingCompleted(ApplicationStatus.ADDONS_LOADED);
				}
			});
			t.setName("Wait for custom plugin loading");
			t.start();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		} catch (Error err) {
			ErrorMsg.addErrorMessage(err.getLocalizedMessage());
		}
		synchronized (AddonManagerPlugin.class) {
			AddonManagerPlugin.class.notify();
		}
	}
	
	/**
	 * Load addons on startup.
	 */
	private synchronized void loadAddonsOnStartup() {
		long time = System.currentTimeMillis();
		
		try {
			File addonDirFile = new File(ADDON_DIRECTORY);
			if (!addonDirFile.exists())
				addonDirFile.createNewFile();
			if (!deactivatedlist.exists())
				deactivatedlist.createNewFile();
			
			File updateAddonDir = new File(ADDONS_TO_BE_UPDATED_DIR);
			
			// finalize the update procedure of add-ons by copying it to the
			// appropriate directory
			File[] files = getJarFileList(updateAddonDir);
			if (files != null)
				for (File toBeUpdatedAddonFile : files) {
					try {
						HomeFolder.copyFile(toBeUpdatedAddonFile, new File(ADDON_DIRECTORY + toBeUpdatedAddonFile.getName()));
						toBeUpdatedAddonFile.delete();
					} catch (IOException e) {
						ErrorMsg.addErrorMessage("<html>Add-on " + toBeUpdatedAddonFile.getName() + " could not be updated due to I/O Error:<br>" + e.getMessage());
					}
					System.out.println("Updated Add-on " + toBeUpdatedAddonFile.getName());
				}
			
			File[] fff = null;
			if (updateAddonDir != null)
				fff = updateAddonDir.listFiles();
			if (fff != null && fff.length == 0)
				updateAddonDir.delete();
			
			System.out.println("Trying to load Add-ons... ");
			
			for (File toBeActivatedAddon : getJarFileList(addonDirFile))
				addAddon(toBeActivatedAddon, true);
			
			if (getJarFileList(addonDirFile).length > 0)
				System.out.println("Add-ons loaded in " + (System.currentTimeMillis() - time) + "ms");
			else
				System.out.println("No Add-on found.");
			
			hideNewComponents();
			
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	/**
	 * Gets all jar files from Add-on directory.
	 * 
	 * @param dir
	 *           the dir
	 * @return the jar file list
	 */
	private File[] getJarFileList(File dir) {
		if (dir == null) {
			ErrorMsg.addErrorMessage("Add-on folder not available.");
			return new File[] {};
		}
		return dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				boolean result;
				try {
					result = f.getName().toLowerCase().endsWith(".jar");
					if (result) {
						if (new File(f.getAbsolutePath() + "_deleted").exists()) {
							f.delete();
							new File(f.getAbsolutePath() + "_deleted").delete();
							result = false;
						}
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					result = false;
				}
				return result;
			}
		});
	}
	
	/**
	 * Expands the classloader by the Add-on jarfile.
	 * 
	 * @param files
	 *           the files
	 */
	private void expandClasspathByJarfile(URL[] files) {
		URLClassLoader loader = new URLClassLoader(files, InstanceLoader
				.getCurrentLoader());
		InstanceLoader.overrideLoader(loader);
	}
	
	/**
	 * Adds the addon to Vanted.
	 * 
	 * @param f
	 *           The Add-on-Jar-File.
	 */
	public AddOnInstallResult addAddon(final File f, boolean onStartup) {
		
		try {
			// extend the system-classloader by the jar-url and set as
			// default-system classloader
			expandClasspathByJarfile(Addon.getURLfromJarfile(f));
			
			// load plugindescriptions and the addons
			final URL xmlURL = Addon.getXMLURL(InstanceLoader.getCurrentLoader(), f);
			
			// get rid of empty or otherwise wrong jars (without correct
			// xml-files in root-dir)
			if (xmlURL != null) {
				// if the addon is not active still get all information but
				// don't load it
				URLConnection juc = xmlURL.openConnection();
				is = juc.getInputStream();
				final PluginDescription pd = p.parse(is);
				pd.setAddon(true);
				
				boolean isactive = !isDeactivated(f.getName().toLowerCase().replaceAll(".jar", ""));
				
				boolean incompatibleDeactivated = false;
				
				if (onStartup && ReleaseInfo.isUpdated() == UpdateInfoResult.UPDATED
						&& !new Addon(f, xmlURL, pd, isactive, getInactiveIcon()).isTestedWithRunningVersion()) {
					isactive = false;
					incompatibleDeactivated = true;
				}
				if (!onStartup) {
					// install by user while running VANTED
					if (!new Addon(f, xmlURL, pd, isactive, getInactiveIcon()).isTestedWithRunningVersion()) {
						isactive = false;
						incompatibleDeactivated = true;
					}
				}
				
				ImageIcon icon = null;
				final ObjectRef or = new ObjectRef();
				if (isactive) {
					if (!SwingUtilities.isEventDispatchThread()) {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								try {
									MainFrame.getInstance().getPluginManager().loadPlugin(pd, xmlURL, false);
								} catch (Exception e) {
									or.setObject(e);
								}
							}
						});
						if (or.getObject() != null)
							throw (Exception) or.getObject();
					} else {
						MainFrame.getInstance().getPluginManager().loadPlugin(pd, xmlURL, false);
					}
					icon = getIcon(pd);
				} else {
					icon = getInactiveIcon();
				}
				
				System.out.println("Add-on " + f.getName() + " processed");
				// add all information to lists
				if (!addonAlreadyInList(f)) {
					Addon a = new Addon(f, xmlURL, pd, isactive, icon);
					synchronized (addons) {
						addons.add(a);
					}
					if (incompatibleDeactivated) {
						writeDeactivatedListToFile();
						return AddOnInstallResult.InstalledAndIncompatible;
					} else {
						return AddOnInstallResult.InstalledAndActive;
					}
				} else {
					if (incompatibleDeactivated) {
						writeDeactivatedListToFile();
						return AddOnInstallResult.InstalledAndIncompatible;
					} else
						return AddOnInstallResult.Updated;
				}
			} else
				return AddOnInstallResult.NotAnAddon;
		} catch (Exception e) {
			System.out.println("Add-on " + f.getName() + " could not be loaded into VANTED!");
			return AddOnInstallResult.Error;
		}
	}
	
	private boolean addonAlreadyInList(File f) {
		for (Addon a : addons)
			if (a.getJarFile().compareTo(f) == 0)
				return true;
		return false;
	}
	
	/**
	 * Checks if a certain Add-on is deactivated.
	 * 
	 * @param addonName
	 *           The addon name
	 * @return true, if it is deactivated
	 */
	private boolean isDeactivated(String addonName) {
		
		for (String s : getDeactivatedList())
			if (addonName.equalsIgnoreCase(s))
				return true;
		return false;
	}
	
	/**
	 * Gets the list of deactivated Add-ons from file
	 * .../vanted/addons/deactivated.txt.
	 * 
	 * @param f
	 *           The textfile holding the deactivated Add-ons.
	 * @return the deactivated list from file
	 */
	private String[] getDeactivatedList() {
		try {
			return TextFile.read(deactivatedlist).split("\n");
		} catch (MalformedURLException e1) {
			ErrorMsg.addErrorMessage(e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
			e1.printStackTrace();
		}
		return new String[] {};
	}
	
	/**
	 * Write deactivated list to file.
	 * 
	 * @param list
	 *           The file-list
	 */
	public void writeDeactivatedListToFile() {
		String content = new String("");
		
		for (Addon a : addons)
			if (!a.isActive())
				content += a.getName() + "\n";
		
		try {
			TextFile.write(ADDON_DIRECTORY
					+ "deactivated.txt", content);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Activate deactivated addon.
	 * 
	 * @param number
	 *           the number
	 */
	public boolean activateAddon(int number) {
		try {
			Addon a;
			synchronized (addons) {
				if (number < addons.size())
					a = addons.get(number);
				else
					return false;
			}
			if (a != null && !a.isTestedWithRunningVersion()) {
				String sup = addons.get(number).getDescription().getCompatibleVersion();
				if (sup == null)
					sup = "Add-on contains no compatibility information.<br>" +
							"Check Add-on website for compatible program version(s).";
				else
					sup = "Incompatible with " + DBEgravistoHelper.DBE_GRAVISTO_VERSION + ",<br>but works with " + sup + ".";
				Object[] res = MyInputHelper.getInput("" +
						"[" +
						"<html><center>Force Activation<br><small>(override version check);" +
						"<html><center>Cancel<br><small>(use 'Find Update' button)]" +
						StringManipulationTools.getWordWrap("This Add-on has not been tested with " + DBEgravistoHelper.DBE_GRAVISTO_VERSION + ". " +
								"To ensure stable operation of the application and the Add-on functions, it is recommended " +
								"to use the 'Find Updates' function to look for an updated version of this Add-on.", 45) + "<br><br>" +
						StringManipulationTools.getWordWrap("You may also opt for downloading and installing an older version of this application:", 45) +
						"<br><br>" + sup + "<br><br>",
						"Add-on may be incompatible", new Object[] {});
				if (res == null) {
					return false;
				}
			}
			
			a.setIsActive(true);
			PluginDescription desc = a.getDescription();
			MainFrame.getInstance().getPluginManager().loadPlugin(desc, a.getXMLURL(), false);
			a.setIcon(getIcon(desc));
			
			hideNewComponents();
			
			writeDeactivatedListToFile();
		} catch (PluginManagerException e) {
			ErrorMsg.addErrorMessage(e);
			Addon a;
			synchronized (addons) {
				a = addons.get(number);
			}
			System.out.println("Addon +" + a.getName() + " could not be activated!");
			return false;
		}
		return true;
	}
	
	private void hideNewComponents() {
		// get rid of the buttons etc
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Session s = MainFrame.getInstance().getActiveSession();
				if (s == null)
					MainFrame.getInstance().setActiveSession(null, null);
				else
					MainFrame.getInstance().setActiveSession(s, s.getActiveView());
			}
		});
		
		// get rid of the new tabs
		BackgroundTaskHelper.executeLaterOnSwingTask(100, new Runnable() {
			@Override
			public void run() {
				Session s = MainFrame.getInstance().getActiveSession();
				if (((Inspector) MainFrame.getInstance().getInspectorPlugin()) != null)
					((Inspector) MainFrame.getInstance().getInspectorPlugin()).viewChanged(s == null ? null : s.getActiveView());
			}
		});
	}
	
	private ImageIcon getIcon(PluginDescription desc) {
		try {
			return MainFrame.getInstance().getPluginManager()
					.getPluginInstance(desc.getName()).getIcon();
		} catch (Exception e) {
			ClassLoader cl = GenericPluginAdapter.class.getClassLoader();
			String path = GenericPluginAdapter.class.getPackage().getName().replace('.', '/');
			return new ImageIcon(cl.getResource(path + "/addon-icon.png"));
		}
	}
	
	private ImageIcon getInactiveIcon() {
		ClassLoader cl = AddonManagerPlugin.class.getClassLoader();
		String path = AddonManagerPlugin.class.getPackage().getName().replace('.', '/');
		return new ImageIcon(cl.getResource(path + "/inactive-addon-icon.png"));
	}
	
	public void deactivateAddon(int number) {
		Addon a;
		synchronized (addons) {
			a = addons.get(number);
		}
		a.setIsActive(false);
		a.setIcon(getInactiveIcon());
		writeDeactivatedListToFile();
	}
	
	/**
	 * Install Add-on by copying the jarfile and loading it.
	 * 
	 * @param jardir
	 *           the jardir, which must not be the add-on directory
	 * @param jarname
	 *           the jarname of the add-on
	 * @throws FileNotFoundException
	 *            the file not found exception
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	public boolean installAddon(String jardir, String jarname) throws FileNotFoundException, IOException {
		
		try {
			
			File toBeInstalledAddonJarfile = new File(jardir + ReleaseInfo.getFileSeparator() + jarname);
			URL jarfileurl = toBeInstalledAddonJarfile.toURI().toURL();
			
			expandClasspathByJarfile(new URL[] { jarfileurl });
			URL xmlURL = Addon
					.getXMLURL(InstanceLoader.getCurrentLoader(), toBeInstalledAddonJarfile);
			
			// check if the to be installed addon is correct
			if (xmlURL != null) {
				
				AddOnInstallResult result;
				if (new File(ADDON_DIRECTORY + jarname + "_deleted").exists())
					new File(ADDON_DIRECTORY + jarname + "_deleted").delete();
				// if addon file already exists this means we shall update it
				if (new File(ADDON_DIRECTORY + jarname).exists()) {
					new File(ADDONS_TO_BE_UPDATED_DIR).mkdirs();
					// the add-on will be installed during next startup
					HomeFolder.copyFile(toBeInstalledAddonJarfile, new File(ADDONS_TO_BE_UPDATED_DIR + jarname));
					result = AddOnInstallResult.Updated;
				} else {
					File addonCopyDestination = new File(ADDON_DIRECTORY + jarname);
					HomeFolder.copyFile(toBeInstalledAddonJarfile, addonCopyDestination);
					result = addAddon(addonCopyDestination, false);
				}
				
				switch (result) {
					case InstalledAndActive:
						hideNewComponents();
						
						showManageAddonDialog("<html>Addon \"" + jarname
								+ "\" was correctly installed and may be used.", false);
						return true;
					case Updated:
						showManageAddonDialog("<html>Addon \"" + jarname
								+ "\" will be updated when application is restarted.", false);
						return true;
					case InstalledAndIncompatible:
						showManageAddonDialog("<html>Addon \"" + jarname
								+ "\" is installed but is deactivated (compatibility error).", false);
						return true;
					case NotAnAddon:
						return false;
					case Error:
						showManageAddonDialog("<html>Addon \"" + jarname
								+ "\" could not be installed.", false);
						return false;
				}
				
			} else {
				String xmlString = null;
				JarFile jarFile = new JarFile(toBeInstalledAddonJarfile);
				Enumeration<JarEntry> e = jarFile.entries();
				while (e.hasMoreElements()) {
					JarEntry entry = e.nextElement();
					if (entry.getName().endsWith(".xml")) {
						xmlString = entry.getName();
						break;
					}
				}
				
				String text = "";
				if (xmlString == null || xmlString.contains("/"))
					text += "is not valid due to missing internal xml-file!<br>Please compile a new version together with the xml!";
				else {
					if (xmlString.replace(".xml", "").equals(jarname.replace(".jar", "")))
						text += "is not valid due to incorrect internal xml-file!<br>Please correct it and compile a new version!";
					else
						text += "is not valid, because the internal<br>xml-file \"" + xmlString + "\" must have the same name<br>" +
								"as the jar-file. Please check the correct name,<br>" +
								"maybe you or your browser renamed the jar.";
				}
				MainFrame.showMessageDialog("<html>Add-on file \"" + jarname + "\" " + text,
						"Error installing Add-on");
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Removes the Add-on by renaming it.
	 * 
	 * @param file
	 *           the file
	 * @throws FileNotFoundException
	 *            the file not found exception
	 * @throws IOException
	 *            Signals that an I/O exception has occurred.
	 */
	public void removeAddon(File file) throws FileNotFoundException,
			IOException {
		
		Addon delete = null;
		// don't deleted, rename instead
		if (!file.delete()) {
			// rename if "open" file fails under Windows
			new File(file.getPath() + "_deleted").createNewFile();
		}
		for (Addon a : addons)
			if (a.getJarFile().equals(file)) {
				delete = a;
				break;
			}
		if (delete != null) {
			synchronized (addons) {
				addons.remove(delete);
			}
		}
		
		writeDeactivatedListToFile();
	}
	
	ManageAddonDialog dialog = null;
	
	/**
	 * Shows Add-on-Manage-Dialog.
	 */
	public void showManageAddonDialog() {
		if (dialog != null) {
			dialog.close();
		}
		
		dialog = new ManageAddonDialog(MainFrame.getInstance(), null);
	}
	
	public void showManageAddonDialog(final String msg, final boolean highlightUpdate) {
		showManageAddonDialog();
		BackgroundTaskHelper.executeLaterOnSwingTask(0, new Runnable() {
			@Override
			public void run() {
				dialog.setTopText(msg);
				if (highlightUpdate)
					BackgroundTaskHelper.executeLaterOnSwingTask(0, new Runnable() {
						@Override
						public void run() {
							dialog.highlightFindUpdatesButton();
						}
					});
			}
		});
	}
	
	/**
	 * Gets the all the Add-ons as a list.
	 * 
	 * @return the addons
	 */
	public ArrayList<Addon> getAddons() {
		synchronized (addons) {
			return new ArrayList<Addon>(addons);
		}
	}
	
	@Override
	public boolean process(List<File> files) {
		for (File f : files) {
			if (f.getAbsolutePath().toLowerCase().endsWith(".jar")) {
				try {
					if (MainFrame.getInstance().isEnabled())
						installAddon(f.getParent(), f.getName());
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
					return false;
				}
				return true;
			}
		}
		return true;
	}
	
	public Addon getAddon(int number) {
		synchronized (addons) {
			return addons.get(number);
		}
	}
	
	public Collection<String> getDeactivatedAddons() {
		ArrayList<String> res = new ArrayList<String>();
		for (String s : getDeactivatedList())
			res.add(s);
		return res;
	}
	
	public static boolean addonsLoaded() {
		return instance.startupAddonsLoaded;
	}
	
	@Override
	public boolean canProcess(File f) {
		return f.getAbsolutePath().toLowerCase().endsWith(".jar");
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.DragAndDropHandler
	 * #hasPriority()
	 */
	@Override
	public boolean hasPriority() {
		return false;
	}
	
	public boolean activateAddon(String name) {
		synchronized (addons) {
			int cnt = 0;
			cnt = addons.size();
			for (int i = 0; i < cnt; i++)
				if (addons.get(i).getDescription().getName().equals(name)) {
					activateAddon(i);
					return true;
				}
		}
		return false;
	}
}
