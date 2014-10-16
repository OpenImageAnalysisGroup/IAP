package de.ipk.ag_ba;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.ActionHome;
import de.ipk.ag_ba.commands.ActionNavigateDataSource;
import de.ipk.ag_ba.commands.lt.ActionLTnavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk.ag_ba.server.task_management.BackupSupport;
import de.ipk.ag_ba.server.task_management.CloudComputingService;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;

/**
 * @author Christian Klukas
 *         1.2.2012
 */
public class Console {
	public static void main(String[] args) {
		for (String i : IAPmain.getMainInfoLines())
			System.out.println(i);
		if (paramContains(new String[] { "help", "h", "?" }, args)) {
			System.out.println("***************************************************");
			System.out.println("* Usage                                           *");
			System.out.println("* - no parameters: interactive console interface  *");
			System.out.println("* - /help, /h, /?: this help                      *");
			System.out.println("* - /exec:XYZ    : execute commands X, Y, Z       *");
			System.out.println("*                  these commands correspond to   *");
			System.out.println("*                  the keys you use inside the    *");
			System.out.println("*                  console interface              *");
			System.out.println("***************************************************");
			System.exit(0);
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Welcome! About to initalize the application...");
		
		String currentDirectory = System.getProperty("user.dir");
		if (currentDirectory != null && !currentDirectory.isEmpty() && new File(currentDirectory).isDirectory()) {
			VirtualFileSystem.addItem(new VirtualFileSystemVFS2(
					"user.dir",
					VfsFileProtocol.LOCAL,
					"Current Directory",
					"File I/O",
					"",
					null,
					null,
					currentDirectory,
					false,
					false,
					null
					));
		}
		
		Console c = new Console();
		while (true) {
			c.printGUI();
			c.waitForStatusChange();
		}
	}
	
	private static boolean paramContains(String[] search, String[] args) {
		if (args != null)
			for (String a : args) {
				a = a.trim();
				a = StringManipulationTools.stringReplace(a, "/", "");
				a = StringManipulationTools.stringReplace(a, "-", "");
				if (search != null)
					for (String s : search)
						if (a.equalsIgnoreCase(s))
							return true;
			}
		return false;
	}
	
	private void waitForStatusChange() {
		
	}
	
	private void printGUI() {
		int idx = 1;
		HashMap<String, NavigationButton> cmd2b = new HashMap<String, NavigationButton>();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.print("Location :");
		for (NavigationButton n : getNavigationBarActions()) {
			String cmd = (idx++) + "";
			System.out.print((idx > 2 ? " >" : " ") + " [" + cmd + "] " + StringManipulationTools.removeHTMLtags(n.getTitle()));
			cmd2b.put(cmd, n);
		}
		System.out.println();
		ArrayList<NavigationButton> actions = getFilteredActionBarActions();
		if (actions != null && actions.size() > 0) {
			System.out.print("Actions  :");
			char idxC = 'A' - 1;
			int secondIndex = 1;
			for (NavigationButton n : actions) {
				if (n.getTitle() == null)
					continue;
				String title = StringManipulationTools.removeHTMLtags(n.getTitle());
				if (isValidCommand(title)) {
					idxC += 1;
					if (idxC > 'Z') {
						idxC = 'A';
						secondIndex += 1;
					}
					String cmd = idxC + "";
					if (secondIndex > 1)
						cmd += secondIndex + "";
					System.out.print("  [" + cmd + "] " + title);
					cmd2b.put(cmd, n);
				}
			}
			System.out.println();
		}
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println(SystemAnalysis.getCurrentTime()
				+ ">INFO: READY, enter number or character to navigate or execute a command, just press ENTER to update display");
		System.out.print(SystemAnalysis.getCurrentTime() + ">");
		String input = SystemAnalysis.getCommandLineInput();
		if (input != null)
			input = input.trim();
		if (input != null && cmd2b.containsKey(input.toUpperCase())) {
			NavigationButton n = cmd2b.get(input.toUpperCase());
			System.out.print(SystemAnalysis.getCurrentTime() + ">EXECUTE: \"" + StringManipulationTools.removeHTMLtags(n.getTitle() + "\""));
			executeCommand(n);
			try {
				int navigation = Integer.parseInt(input);
				while (navigationBarContent.size() > navigation)
					navigationBarContent.remove(navigationBarContent.size() - 1);
			} catch (NumberFormatException e) {
				ArrayList<NavigationButton> rs = n.getAction().getResultNewNavigationSet(navigationBarContent);
				if (rs != null)
					rs = new ArrayList<NavigationButton>(rs);
				if (rs != null && rs.size() > 1) {
					clearNavigationBarActions();
					for (NavigationButton nb : rs)
						addNavigationBarAction(nb);
				} else {
					// addNavigationBarAction(n);
				}
			}
			ArrayList<NavigationButton> rs = n.getAction().getResultNewActionSet();
			if (rs != null) {
				clearActionBarActions();
				for (NavigationButton nb : rs)
					addActionBarAction(nb);
			}
		}
	}
	
	private boolean isValidCommand(String title) {
		String[] invalid = new String[] {
				"Edit", // (graph)
				"Add files",
				"IAP-VANTED",
				"CCTV (Maize)",
				"CCTV (Barley)",
				"Exp. Planning",
				"Documentation",
				"Logout",
				"View Data",
				"Schedule Experiment",
				"Performance Test",
				"Watering Table",
				"Show in IAP-VANTED"
		};
		invalid = IAPoptions.getInstance().getStringAll("IAP-CONSOLE-MODE", "invalid_cmds", invalid);
		for (String i : invalid)
			if (i.equalsIgnoreCase(title))
				return false;
		return true;
	}
	
	private void executeCommand(NavigationButton n) {
		if (n.getExecution() != null) {
			n.performAction();
			System.out.println();
		} else
			try {
				CommandLineStatusProvider sp = new CommandLineStatusProvider(null, -1);
				NavigationAction action = n.getAction();
				if (action != null) {
					action.setSource(action, guiSetting);
					action.setStatusProvider(sp);// "#", 10));
					if (action instanceof SpecialCommandLineSupport) {
						boolean ok = false;
						try {
							ok = ((SpecialCommandLineSupport) action).prepareCommandLineExecution();
						} catch (Exception e) {
							System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
						}
						if (ok) {
							action.performActionCalculateResults(n);
							sp.finishPrint();
							((SpecialCommandLineSupport) action).postProcessCommandLineExecution();
						} else
							System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Operation has been cancelled upon user request or upon error");
					} else {
						action.performActionCalculateResults(n);
						sp.finishPrint();
					}
					MainPanelComponent res = action.getResultMainPanel();
					if (res != null) {
						Collection<String> out = res.getHTML();
						boolean first = true;
						if (out != null)
							for (String o : out) {
								if (o == null || o.trim().isEmpty())
									continue;
								if (!first)
									System.out.println("> ----------------------");
								System.out.println("> " + StringManipulationTools.removeHTMLtags(o.replace("<br>", "\r\n> ").replace("<li>", "\r\n> - ")));
								first = false;
							}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	ArrayList<NavigationButton> navigationBarContent = new ArrayList<NavigationButton>();
	ArrayList<NavigationButton> actionBarContent = new ArrayList<NavigationButton>();
	
	private final UrlCacheManager urlCacheManager;
	
	private boolean validImagingSystemLogin = false;
	private String ltUser;
	
	private boolean validDomainLogin = false;
	private String domainUser;
	private String[] domainUserGroups;
	private GUIsetting guiSetting;
	
	private static Boolean first = true;
	
	public Console() {
		IAPmain.setRunMode(IAPrunMode.CONSOLE);
		
		synchronized (first) {
			SystemAnalysis.simulateHeadless = true;
			
			this.urlCacheManager = new UrlCacheManager();
			
			if (first) {
				registerIOhandlers();
				
				ReleaseInfo.setRunningAsApplet(null);
				
				{
					MongoDB m = MongoDB.getDefaultCloud();
					if (m != null) {
						CloudComputingService cc = CloudComputingService.getInstance(m);
						cc.setEnableCalculations(false);
						cc.switchStatus(m);
					}
					
					if (IAPmain.isSettingEnabled(IAPfeature.TOMCAT_AUTOMATIC_HSM_BACKUP)) {
						BackupSupport sb = BackupSupport.getInstance();
						sb.scheduleBackup();
					}
				}
				
				String[] args = new String[] { "IAP Console" };
				SplashScreenInterface emptySplashScreen = new SplashScreenDontPrintProgress();
				new Main(false, (args.length > 0 ? args[0] : DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT), args, new String[] {}, emptySplashScreen, "pluginsIAP.txt");
			}
			first = false;
		}
	}
	
	private void registerIOhandlers() {
		ResourceIOManager.registerIOHandler(LoadedDataHandler.getInstance());
		ResourceIOManager.registerIOHandler(new LTftpHandler());
		for (MongoDB m : MongoDB.getMongos())
			ResourceIOManager.registerIOHandler(m.getHandler());
		
		DataMappingTypeManager3D.replaceVantedMappingTypeManager();
	}
	
	public ArrayList<NavigationButton> getNavigationBarActions() {
		if (navigationBarContent.isEmpty()) {
			// add IAP home navigation button
			ActionHome ha = new ActionHome(new BackgroundTaskStatusProviderSupportingExternalCallImpl("", ""));
			guiSetting = new GUIsetting(null, null, null);
			ha.setSource(ha, guiSetting);
			addNavigationBarAction(new NavigationButton(ha, null));
			ha.performActionCalculateResults(null);
			clearActionBarActions();
			ArrayList<NavigationButton> rs = ha.getResultNewActionSet();
			if (rs != null)
				for (NavigationButton nb : rs) {
					if (nb.getAction() != null)
						nb.getAction().setSource(nb.getAction(), guiSetting);
					addActionBarAction(nb);
				}
		}
		
		updateAllActionCustomization();
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(navigationBarContent);
		return res;
	}
	
	public ArrayList<NavigationButton> getFilteredActionBarActions() {
		updateAllActionCustomization();
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		for (NavigationButton n : actionBarContent)
			// if (!mapper.isForbiddenNavigation(n))
			result.add(n);
		return result;
	}
	
	public void addActionBarAction(NavigationButton nr) {
		actionBarContent.add(nr);
	}
	
	public void addNavigationBarAction(NavigationButton nr) {
		navigationBarContent.add(nr);
	}
	
	public void clearNavigationBarActions() {
		navigationBarContent.clear();
	}
	
	public void clearActionBarActions() {
		actionBarContent.clear();
	}
	
	public void removeNavigationBarAction(int i) {
		navigationBarContent.remove(i);
	}
	
	public UrlCacheManager getUrlCacheManager() {
		return urlCacheManager;
	}
	
	public boolean isSessionForImagingSystemUserValidated() {
		return validImagingSystemLogin || (validDomainLogin && !this.domainUser.equals("public"));
	}
	
	public boolean isSessionForDomainUserValidated() {
		return validDomainLogin || validImagingSystemLogin;
	}
	
	public String validateImagingSystemLogin(String user, String pass) throws Exception {
		try {
			boolean ok = new LTdataExchange().isUserKnown(user, pass);
			if (ok) {
				validImagingSystemLogin = true;
				this.ltUser = user;
				updateAllActionCustomization();
				System.out.println("OK: Imaging System access enabled for user: " + user + " // " + SystemAnalysis.getCurrentTime());
				return "OK: Imaging System data access is enabled.";
			} else {
				System.out.println("ERROR: Imaging System access denied for user: " + user + " // " + SystemAnalysis.getCurrentTime());
				return "Please check your login data. " +
						"If in doubt, contact the local imaging system administrators or key users to update or create a personal account.";
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	// public String validateDomainLogin(String user, String pass) throws Exception {
	// try {
	// boolean ok = (user.equals("public") && pass.equals("user")) ||
	// new DatabaseDataExchange().isValidDomainUser(user, pass);
	// if (ok) {
	// validDomainLogin = true;
	// this.domainUser = user;
	// if (user.equals("public"))
	// this.domainUserGroups = new String[] { "public" };
	// else
	// this.domainUserGroups = new String[] { "public" };// = new DatabaseDataExchange().getUserGroups(user, pass);
	//
	// updateAllActionCustomization();
	// System.out.println("OK: Data processing access enabled for user: " + user + " // " + SystemAnalysis.getCurrentTime());
	// return "OK: Data processing access is enabled.";
	// } else {
	// System.out.println("ERROR: Data processing access denied for user: " + user + " // " + SystemAnalysis.getCurrentTime());
	// return "Please check your login data. " +
	// "If in doubt, contact the IPK research group image analysis.";
	// }
	// } catch (Exception e) {
	// throw new Exception(e.getMessage());
	// }
	// }
	
	private void updateAllActionCustomization() {
		for (NavigationButton nb : navigationBarContent) {
			customizeAction(nb.getAction());
		}
		for (NavigationButton nb : actionBarContent) {
			customizeAction(nb.getAction());
		}
	}
	
	public void customizeAction(NavigationAction action) {
		if (action != null && action instanceof ActionLTnavigation) {
			ActionLTnavigation ltde = (ActionLTnavigation) action;
			if (validImagingSystemLogin)
				ltde.setLogin(ltUser);
			else
				ltde.setLogin(null);
		}
		
		if (action != null && action instanceof ActionMongoExperimentsNavigation) {
			ActionMongoExperimentsNavigation ltde = (ActionMongoExperimentsNavigation) action;
			if (validDomainLogin)
				ltde.setLogin(domainUser);
			else
				ltde.setLogin(null);
		}
		if (action != null && action instanceof ActionNavigateDataSource) {
			ActionNavigateDataSource dsna = (ActionNavigateDataSource) action;
			Object o = dsna.getDataSourceLevel();
			if (o != null && o instanceof HsmFileSystemSource) {
				HsmFileSystemSource hsms = (HsmFileSystemSource) o;
				hsms.setLogin(domainUser, null);
			}
			// if (validDomainLogin)
			// ltde.setLogin(domainUser);
			// else
			// ltde.setLogin(null);
		}
		
		if (action != null && action instanceof ActionMongoOrLTexperimentNavigation) {
			ActionMongoOrLTexperimentNavigation ltde = (ActionMongoOrLTexperimentNavigation) action;
			if (validDomainLogin)
				ltde.setLogin(domainUser);
			else
				ltde.setLogin(null);
		}
		
	}
	
	public void logoutImagingSystemUser() {
		validImagingSystemLogin = false;
		ltUser = null;
	}
	
	public void logoutDomainUser() {
		validDomainLogin = false;
		domainUser = null;
		domainUserGroups = null;
	}
	
	public ExperimentInterface getLoadedExperiment(String experimentUrl) {
		for (int i = navigationBarContent.size() - 1; i >= 0; i--) {
			NavigationButton navigationButton = navigationBarContent.get(i);
			NavigationAction action = navigationButton.getAction();
			if (action != null && action instanceof ActionMongoOrLTexperimentNavigation) {
				ActionMongoOrLTexperimentNavigation a = (ActionMongoOrLTexperimentNavigation) action;
				String dbID = a.getExperimentReference().getHeader().getDatabaseId();
				if (dbID.equals(experimentUrl))
					return a.getExperimentReference();
			}
		}
		return null;
	}
	
}
