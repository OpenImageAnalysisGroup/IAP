package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.iharder.dnd.FileDrop;

import org.ApplicationStatus;
import org.ErrorMsg;
import org.FeatureSet;
import org.HelperClass;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.attributes.AttributeTypesManager;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.graph.Graph;
import org.graffiti.managers.pluginmgr.DefaultPluginEntry;
import org.graffiti.managers.pluginmgr.DefaultPluginManager;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.managers.pluginmgr.PluginManager;
import org.graffiti.managers.pluginmgr.PluginManagerException;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.util.PluginHelper;
import org.graffiti.util.ProgressViewer;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.BSHinfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.BSHscriptMenuEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.DefaultContextMenuManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.DatabaseFileStatusService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataPresenter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessingManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.muntjak.tinylookandfeel.Theme;

public class GravistoMainHelper implements HelperClass {
	
	private static final GravistoPreferences prefs = GravistoPreferences.userNodeForPackage(GravistoMainHelper.class);
	
	private static final PluginManager pluginManager = new DefaultPluginManager(getPreferences());
	
	public static GravistoPreferences getPreferences() {
		return prefs;
	}
	
	public static PluginManager getPluginManager() {
		AttributeTypesManager attributeTypesManager = new AttributeTypesManager();
		pluginManager.addPluginManagerListener(attributeTypesManager);
		return pluginManager;
	}
	
	public static void loadPlugins(Collection<String> pluginLocations, final ProgressViewer progressViewer)
			throws PluginManagerException {
		
		ArrayList<String> validLocations = new ArrayList<String>();
		for (String s : pluginLocations) {
			if (s != null && s.length() > 0 && !s.endsWith("javadoc.xml"))
				validLocations.add(s);
		}
		pluginLocations = validLocations;
		
		PluginManager pluginManager = getPluginManager();
		
		final List<String> messages = new LinkedList<String>();
		
		final ArrayList<PluginEntry> pluginEntries = new ArrayList<PluginEntry>();
		
		// progressViewer.setText("Process Init Command Add-ons...");
		//
		// try {
		// AddonManagerPlugin.loadInitAddons();
		// } catch(Exception e) {
		// ErrorMsg.addErrorMessage(e);
		// }
		
		int nnc = pluginLocations.size();
		progressViewer.setText("Read Plugin-Description Files... (" + nnc + ")");
		final ClassLoader cl = Main.class.getClassLoader();
		
		ExecutorService run = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs());
		
		for (String pluginLocation : pluginLocations) {
			if (pluginLocation != null && pluginLocation.length() > 0) {
				final String fpluginLocation = pluginLocation.substring(2);
				run.submit(new Runnable() {
					public void run() {
						try {
							String pluginLocation = StringManipulationTools.stringReplace(fpluginLocation, "\\", "/");
							if (!pluginLocation.startsWith("vanted_feature/") && pluginLocation.contains("/vanted_feature/"))
								pluginLocation = pluginLocation.substring(pluginLocation.indexOf("/vanted_feature/")
										+ "/".length());
							if (!pluginLocation.startsWith("de/") && pluginLocation.contains("/de/"))
								pluginLocation = pluginLocation.substring(pluginLocation.indexOf("/de/") + "/".length());
							if (!pluginLocation.startsWith("org/") && pluginLocation.contains("/org/"))
								pluginLocation = pluginLocation.substring(pluginLocation.indexOf("/org/") + "/".length());
							try {
								URL pluginUrl = cl.getResource(pluginLocation);
								PluginDescription desc = PluginHelper.readPluginDescription(pluginUrl);
								synchronized (pluginEntries) {
									pluginEntries.add(new DefaultPluginEntry(pluginUrl.toString(), desc));
								}
							} catch (PluginManagerException err) {
								System.out.println("Plugin Manager Exception for " + pluginLocation);
								ErrorMsg.addErrorMessage("Plugin Manager Exception for " + pluginLocation + ". Exception: "
										+ err.getLocalizedMessage());
								System.err.println(err.getLocalizedMessage());
								messages.add(err.getLocalizedMessage());
							}
						} catch (Exception err) {
							System.out.println(err.getClass().getSimpleName() + " for " + fpluginLocation);
							ErrorMsg.addErrorMessage("Exception for " + fpluginLocation + ". Exception: "
									+ err.getLocalizedMessage());
							messages.add(err.getLocalizedMessage());
						}
					}
				});
				
			}
		}
		
		run.shutdown();
		try {
			run.awaitTermination(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}
		
		int loaded = pluginEntries.size();
		
		PluginEntry[] loadedPlugins = new PluginEntry[loaded];
		int i2 = 0;
		for (PluginEntry pe : pluginEntries) {
			if (pe == null)
				continue;
			loadedPlugins[i2] = pe;
			i2++;
		}
		try {
			((DefaultPluginManager) pluginManager).loadPlugins(loadedPlugins, progressViewer, true);
		} catch (PluginManagerException pme) {
			System.err.println("Plugin Manager Exception: " + pme.getLocalizedMessage());
			ErrorMsg.addErrorMessage("PluginManagerException: " + pme.getLocalizedMessage());
			messages.add(pme.getMessage());
		}
		
		// collect info of all exceptions into one exception
		if (!messages.isEmpty()) {
			String msg = "";
			for (Iterator<String> itr = messages.iterator(); itr.hasNext();) {
				msg += (itr.next() + "\n");
			}
			throw new PluginManagerException("exception.loadStartup\n", msg.trim());
		}
	}
	
	public static void setLookAndFeel() {
		
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		
		try {
			if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_java_look_and_feel").exists()) {
				TextFile tf = new TextFile(new FileReader(new File(ReleaseInfo.getAppFolderWithFinalSep()
						+ "setting_java_look_and_feel")));
				String look = tf.get(0);
				
				System.out.print("Look and feel " + look);
				
				UIManager.setLookAndFeel(look);
				System.out.println(": OK");
			} else {
				// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				
				if (os != null && !os.toUpperCase().contains("LINUX") && !os.toUpperCase().contains("SUN") && !os.toUpperCase().contains("MAC")) {
					try {
						// if (!ErrorMsg.isMac())
						if (!ReleaseInfo.isRunningAsApplet())
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						/*
						 * elseUIManager.setLookAndFeel(UIManager.
						 * getCrossPlatformLookAndFeelClassName());
						 */
					} catch (Exception e) {
						System.out.println("Info: could not activate desired java windows and button style"); //$NON-NLS-1$
					}
				} else {
					Theme.loadTheme(Theme.getThemeDescription("VANTED"));
					UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel");
				}
			}
		} catch (Exception e) {
			System.out.println(": Exception");
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static void createApplicationSettingsFolder(SplashScreenInterface splashScreen) {
		if (ReleaseInfo.isRunningAsApplet())
			return;
		if (!new File(ReleaseInfo.getAppFolder()).isDirectory()) {
			splashScreen.setVisible(false);
			boolean success = (new File(ReleaseInfo.getAppFolder())).mkdirs();
			if (!success) {
				JOptionPane.showMessageDialog(null, "<html>" + "<h3>The " + DBEgravistoHelper.DBE_GRAVISTO_VERSION
						+ " - settings folder could not be created</h3>"
						+ "Program execution can not continue as the settings folder is needed for<br>"
						+ "storage of program settings and temporary files:<br>" + ReleaseInfo.getAppFolder() + "</html>");
				System.err.println("EXIT");
				System.exit(1);
			}
		}
	}
	
	private static ArrayList<DragAndDropHandler> dragAndDropHandlers = new ArrayList<DragAndDropHandler>();
	
	public static void addDragAndDropHandler(DragAndDropHandler handler) {
		boolean found = false;
		for (DragAndDropHandler dh : dragAndDropHandlers) {
			if (dh.toString().equalsIgnoreCase(handler.toString())) {
				found = true;
				break;
			}
		}
		if (!found)
			dragAndDropHandlers.add(handler);
	}
	
	public static void removeDragAndDropHandler(DragAndDropHandler handler) {
		dragAndDropHandlers.remove(handler);
	}
	
	private static void installDragAndDropHandler(MainFrame mainFrame) {
		new FileDrop(mainFrame, new FileDrop.Listener() {
			public void filesDropped(final File[] files) {
				BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
					public void run() {
						processDroppedFiles(files, true);
					}
				});
			}
		}, new Runnable() {
			public void run() {
				MainFrame.showMessage("<html><b>Drag &amp; Drop action detected:</b> release mouse button to load file",
						MessageType.INFO);
			}
		}, new Runnable() {
			public void run() {
				// MainFrame.showMessage("Drag & Drop action canceled",
				// MessageType.INFO);
			}
		});
	}
	
	public static MainFrame initApplication(String[] args, SplashScreenInterface splashScreen, ClassLoader cl,
			String addPluginFile, String addPlugin) {
		return initApplicationExt(args, splashScreen, cl, addPluginFile, new String[] { addPlugin });
	}
	
	public static MainFrame initApplicationExt(String[] args, SplashScreenInterface splashScreen, ClassLoader cl,
			String addPluginFile, String[] addPlugins) {
		splashScreen.setText("Read plugin information");
		
		// construct and open the editor's main frame
		GravistoPreferences uiPrefs = getPreferences().node("ui");
		uiPrefs.put("showPluginManagerMenuOptions", "false");
		uiPrefs.put("showPluginMenu", "false");
		
		splashScreen.setText("Read plugin information..");
		
		JPanel statusPanel = new JPanel();
		// statusPanel.
		final MainFrame mainFrame = new MainFrame(getPluginManager(), uiPrefs, statusPanel, true);
		
		installDragAndDropHandler(mainFrame);
		
		// ClassLoader cl = Main.class.getClassLoader();
		URL r1 = cl.getResource("plugins1.txt");
		URL r2 = cl.getResource("plugins2.txt");
		URL r3 = cl.getResource("plugins3.txt");
		URL r4 = cl.getResource("plugins4.txt");
		URL r5 = null;
		if (addPluginFile != null)
			r5 = cl.getResource(addPluginFile);
		
		URL rExcl = cl.getResource("plugins_exclude.txt");
		
		// System.out.println("Plugins1: "+(r1!=null ? r1.toExternalForm() :
		// "null"));
		// System.out.println("Plugins2: "+(r2!=null ? r2.toExternalForm() :
		// "null"));
		// System.out.println("Plugins3: "+(r3!=null ? r3.toExternalForm() :
		// "null"));
		// System.out.println("Plugins4: "+(r4!=null ? r4.toExternalForm() :
		// "null"));
		// System.out.println("PluginsE: "+(rExcl!=null ? rExcl.toExternalForm() :
		// "null"));
		// System.out.println("Plugins5 (opt.): "+(r5!=null ? r5.toExternalForm()
		// : "null"));
		
		splashScreen.setText("Read plugin information...");
		
		ArrayList<String> locations = new ArrayList<String>();
		try {
			locations.addAll(new TextFile(r1));
			locations.addAll(new TextFile(r2));
			locations.addAll(new TextFile(r3));
			locations.addAll(new TextFile(r4));
			if (addPlugins != null)
				for (String p : addPlugins)
					if (p != null)
						locations.add("//" + p);
			if (r5 != null)
				locations.addAll(new TextFile(r5));
			locations.remove("");
			ArrayList<String> locations_exclude = new ArrayList<String>();
			locations_exclude.addAll(new TextFile(rExcl));
			for (Iterator<String> it = locations_exclude.iterator(); it.hasNext();) {
				String remove = it.next();
				if (!locations.remove(remove)) {
					// windows compatibility remove also not exact matches
					for (Iterator<String> itl = locations.iterator(); itl.hasNext();) {
						String loc = itl.next();
						remove = remove.toUpperCase();
						remove = StringManipulationTools.stringReplace(remove, "./", "");
						remove = StringManipulationTools.stringReplace(remove, "\"", "");
						remove = StringManipulationTools.stringReplace(remove, "/", "\\");
						remove = StringManipulationTools.stringReplace(remove, "\\", "");
						remove = StringManipulationTools.stringReplace(remove, " ", "");
						String loc2 = loc.toUpperCase();
						loc2 = StringManipulationTools.stringReplace(loc2, "\\", "");
						loc2 = StringManipulationTools.stringReplace(loc2, " ", "");
						if (loc2.indexOf(remove) >= 0) {
							locations.remove(loc);
							break;
						}
					}
				}
			}
			splashScreen.setMaximum(locations.size() - 1);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (NullPointerException npe) {
			System.err.println("Internal error: Plugin Description files could not be loaded.");
			System.err.println("Don't forget to start createfilelist from the make folder.");
			System.err.println("See make - intro.txt for details.");
			System.err.println("-- Program needs to be stopped");
			JOptionPane.showMessageDialog(null, "<html><h2>ERROR: Plugin-Description files could not be loaded</h2>"
					+ "Program execution can not continue.<br>"
					+ "Pleas check out the \"make\" project and execute<br>" +
					"the createfilelist script from the make folder.<br>"
					+ "See also the make - intro.txt in the make project for details.<br>"
					+ "The application needs to be closed.</html>");
			System.err.println("EXIT");
			System.exit(1);
		}
		
		// printLocations(locations, "info");
		splashScreen.setText("Load plugins...");
		try {
			loadPlugins(locations, splashScreen);
		} catch (PluginManagerException pme) {
			ErrorMsg.addErrorMessage(pme.getLocalizedMessage());
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				JMenu dummyScipt = new JMenu("Dummy Script");
				DefaultContextMenuManager.returnScriptMenu(dummyScipt);
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
		splashScreen.setText("Processing finished");
		
		splashScreen.setInitialisationFinished();
		ErrorMsg.setAppLoadingCompleted(ApplicationStatus.PROGRAM_LOADING_FINISHED);
		if (args != null) {
			for (String resource : args) {
				if (resource.contains("\\\\"))
					resource = resource.replace("\\\\", "/");
				if (resource.contains("\\"))
					resource = resource.replace("\\", "/");
				try {
					IOurl url = new IOurl(resource);
					if (url != null) {
						if (url.getFileName().toLowerCase().endsWith(".bsh")) {
							if (!canRead(url, "Cannot read script file "))
								continue;
							BSHinfo info = new BSHinfo(url);
							BSHscriptMenuEntry.executeScript(info, url.getFileName());
						} else {
							for (String ext : MainFrame.getInstance().getIoManager().getGraphFileExtensions())
								if (url.getFileName().toLowerCase().endsWith(ext)) {
									if (!canRead(url, "Cannot read file "))
										continue;
									final Graph g = mainFrame.getGraph(url, url.getFileName());
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											mainFrame.showGraph(g, new ActionEvent(mainFrame, 1,
													"load graph passed with arguments"));
										}
									});
									break;
								}
						}
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		
		GravistoService.loadFiles();
		
		if (ReleaseInfo.isFirstRun()) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							int res = JOptionPane
									.showConfirmDialog(
											mainFrame,
											"<html>"
													+ "Some of the commands contained in this application require access to certain databases.<br>"
													+ "Database files are required for commands related to database identifier handling.<br>"
													+ "For example to exchange compound IDs with compound names (menu command<br>"
													+ "'Nodes/Interpret Database-Identifiers') and for synonyme processing during<br>"
													+ "data mapping.<br><br>"
													+ "Click 'Yes' to open the download command window. You may use the menu command<br>"
													+ "'Help/Database Status' at a later time to download or update the database files.",
											"Show database download window?", JOptionPane.YES_NO_OPTION);
							if (res == 0) {
								DatabaseFileStatusService.showStatusDialog();
							}
						}
					});
				}
				
			};
			Thread tt = new Thread(r);
			tt.setName("Ask for database download");
			tt.start();
		} else {
			final String lastVersion = ReleaseInfo
					.getOldVersionIfAppHasBeenUpdated(DBEgravistoHelper.DBE_GRAVISTO_VERSION);
			if (lastVersion != null) {
				Runnable r = new Runnable() {
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							ErrorMsg.addErrorMessage(e);
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.ADDON_LOADING))
									return;
								if (AddonManagerPlugin.getInstance() == null
										|| AddonManagerPlugin.getInstance().getAddons().size() <= 0) // &&
									// AddonManagerPlugin.getInstance().getDeactivatedAddons().size()<=0)
									JOptionPane.showMessageDialog(mainFrame, "<html>" + "<h3>Application has been updated!</h3>"
											+ "Previous installation of " + lastVersion + " is replaced by "
											+ DBEgravistoHelper.DBE_GRAVISTO_VERSION + ".<br><br>"
											+ "Side panel 'Help'/'News' contains information about updates.", "Information",
											JOptionPane.INFORMATION_MESSAGE);
							}
						});
					}
					
				};
				Thread tt = new Thread(r);
				tt.setName("Ask for database download");
				tt.start();
			}
		}
		
		return mainFrame;
	}
	
	private static boolean canRead(IOurl url, String prefixErrorMessage) {
		
		if (!new File(url.getDetail() + IOurl.SEPERATOR + url.getFileName()).canRead()) {
			ErrorMsg.addErrorMessage(prefixErrorMessage + url.getDetail() + IOurl.SEPERATOR + url.getFileName() + "!");
			return false;
		}
		return true;
		
	}
	
	public static void processDroppedFiles(File[] files, boolean includeGraphFileLoaders) {
		processDroppedFiles(files, includeGraphFileLoaders, null);
	}
	
	private static void processData(HashMap<DragAndDropHandler, List<File>> processor2files, DragAndDropHandler dh,
			final Class<ExperimentDataProcessor> processor) {
		if (dh instanceof ExperimentDataDragAndDropHandler) {
			ExperimentDataDragAndDropHandler ddh = (ExperimentDataDragAndDropHandler) dh;
			ddh.setExperimentDataReceiver(new ExperimentDataPresenter() {
				public void processReceivedData(TableData td, String experimentName, ExperimentInterface doc, JComponent gui) {
					if (processor == null)
						ExperimentDataProcessingManager.getInstance().processIncomingData(doc);
					else {
						ExperimentDataProcessingManager.getInstance().processIncomingData(doc, processor);
					}
				}
			});
		}
		dh.process(processor2files.get(dh));
		if (dh instanceof DataDragAndDropHandler) {
			ExperimentInterface mds = ((DataDragAndDropHandler) dh).getProcessingResults();
			// if(mds == null || mds.size() == 0)
			// ErrorMsg.addErrorMessage("Internal Error: Loader could not correctly process the data");
			// else
			if (processor == null)
				ExperimentDataProcessingManager.getInstance().processIncomingData(mds);
			else
				ExperimentDataProcessingManager.getInstance().processIncomingData(mds, processor);
		}
	}
	
	public static void processDroppedFiles(final File[] files, boolean includeGraphFileLoaders,
			final Class<ExperimentDataProcessor> processor) {
		
		final ArrayList<File> ignoredFiles = new ArrayList<File>();
		final HashMap<File, ArrayList<DragAndDropHandler>> workingSet = new HashMap<File, ArrayList<DragAndDropHandler>>();
		
		if (files == null)
			return;
		
		final HashSet<DragAndDropHandler> myList = new HashSet<DragAndDropHandler>();
		
		if (includeGraphFileLoaders)
			myList.add(new DragAndDropHandler() {
				public boolean process(List<File> files) {
					for (File f : files)
						GravistoService.getInstance().loadFile(f.getAbsolutePath()); // load
					// graph
					return true;
				}
				
				public boolean canProcess(File f) {
					return MainFrame.getInstance().isInputSerializerKnown(f);
				}
				
				@Override
				public String toString() {
					return "Load graph";
				}
				
				public boolean hasPriority() {
					return true;
				}
			});
		myList.addAll(dragAndDropHandlers);
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Processing Drag and Drop", "Please wait...");
		
		Runnable prepareProcess = new Runnable() {
			@Override
			public void run() {
				
				// if a file is a directory: recursively get all files in the directory
				HashSet<File> realfiles = new HashSet<File>();
				ArrayList<File> directories = new ArrayList<File>();
				
				for (File f : files)
					if (f.isDirectory())
						directories.add(f);
					else
						realfiles.add(f);
				
				for (File dir : directories)
					addFiles(realfiles, dir);
				
				int cnt = 0, all = realfiles.size() * myList.size();
				
				cyclefiles: for (File f : realfiles) {
					boolean canProcess = false;
					status.setCurrentStatusText2("of \"" + f.getName() + "\"");
					
					for (DragAndDropHandler handler : myList) {
						status.setCurrentStatusValue(100 * (cnt++) / all);
						
						if (handler.canProcess(f)) {
							if (status.wantsToStop())
								break cyclefiles;
							canProcess = true;
							if (!workingSet.containsKey(f))
								workingSet.put(f, new ArrayList<DragAndDropHandler>());
							workingSet.get(f).add(handler);
						}
					}
					
					if (!canProcess) {
						ignoredFiles.add(f);
					}
				}
				if (ignoredFiles.size() > 0) {
					StringBuilder msg = new StringBuilder();
					msg.append("<html>Drag &amp; Drop handlers could not process the following file(s):<br><ul>");
					for (File f : ignoredFiles) {
						msg.append("<li>" + f.getAbsolutePath());
					}
					msg.append("</ul>");
					MainFrame.showMessageDialogWithScrollBars(msg.toString(), "Could not load file(s)");
				}
			}
		};
		Runnable process = new Runnable() {
			@Override
			public void run() {
				if (status.wantsToStop())
					return;
				FileHandlerUserDecision fhud = new FileHandlerUserDecision(workingSet);
				
				for (File f : workingSet.keySet())
					fhud.addRows(f);
				Object[] res;
				
				if (fhud.atLeastOneFileNeedsUserDecision())
					res = MyInputHelper.getInput("", "Select File Processor", "", fhud.getFolderPanel());
				else
					res = new Object[] {};
				if (res != null) {
					// process files
					final HashMap<DragAndDropHandler, List<File>> processor2files = new HashMap<DragAndDropHandler, List<File>>();
					
					for (Entry<File, JComboBox> e : fhud.getUserSelection().entrySet()) {
						DragAndDropHandler dh = (DragAndDropHandler) e.getValue().getSelectedItem();
						if (!processor2files.containsKey(dh))
							processor2files.put(dh, new ArrayList<File>());
						processor2files.get(dh).add(e.getKey());
					}
					if (status.wantsToStop())
						return;
					// execute VIP handlers (i.e. graph loaders)
					for (DragAndDropHandler dh : processor2files.keySet()) {
						if (dh.hasPriority())
							processData(processor2files, dh, processor);
					}
					Thread t = new Thread(new Runnable() {
						public void run() {
							while (MainFrame.getInstance().isGraphLoadingInProgress()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// empty
								}
							}
							BackgroundTaskHelper.executeLaterOnSwingTask(50, new Runnable() {
								public void run() {
									// execute normal handlers
									for (DragAndDropHandler dh : processor2files.keySet()) {
										if (!dh.hasPriority())
											processData(processor2files, dh, processor);
									}
								}
							});
							
						}
					});
					t.setName("Wait for graph loading to be finished");
					t.start();
				}
			}
		};
		
		BackgroundTaskHelper.issueSimpleTask("Processing Drag and Drop", "Please wait...", prepareProcess, process, status);
	}
	
	private static HashSet<File> addFiles(HashSet<File> files, File dir) {
		if (!dir.isDirectory()) {
			files.add(dir);
			return files;
		}
		
		for (File file : dir.listFiles())
			addFiles(files, file);
		return files;
	}
	
}
