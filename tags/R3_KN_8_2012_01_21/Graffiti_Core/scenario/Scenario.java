/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.03.2007 by Christian Klukas
 */
package scenario;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;
import org.ReleaseInfo;

public class Scenario {
	
	ArrayList<String> imports = new ArrayList<String>();
	ArrayList<String> commands = new ArrayList<String>();
	String scenarioName;
	String menuGroup = "";
	boolean readError = false;
	
	File file;
	
	/**
	 * Create a new scenario
	 */
	public Scenario(String menuTitle, String scenarioName) {
		this.scenarioName = scenarioName;
		if (menuTitle == null)
			this.menuGroup = "";
		else
			this.menuGroup = menuTitle;
	}
	
	public Scenario(File f) {
		try {
			load(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			ErrorMsg.addErrorMessage(e);
		}
		file = f;
	}
	
	public Scenario(InputStream is) {
		load(is);
	}
	
	private void load(InputStream is) {
		readError = true;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String str;
			int line = 0;
			boolean headerRead = false;
			int importsRead = 0;
			while ((str = in.readLine()) != null) {
				line++;
				if (!headerRead && str.startsWith("//@")) {
					headerRead = true;
					String name = str.substring("//@".length());
					if (name.indexOf(":") >= 0) {
						menuGroup = name.substring(0, name.indexOf(":"));
						scenarioName = name.substring(name.indexOf(":") + ":".length());
					} else {
						scenarioName = name;
						menuGroup = "";
					}
				}
				if (headerRead) {
					if (str.startsWith("import ")) {
						if (importsRead < 1)
							importsRead = 1;
						imports.add(str);
					} else {
						commands.add(str);
					}
				}
			}
			in.close();
			readError = false;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public synchronized void addImport(String bshScriptCommand) {
		if (bshScriptCommand == null || bshScriptCommand.length() == 0)
			return;
		ArrayList<String> cmdl = new ArrayList<String>();
		cmdl.add(bshScriptCommand);
		addImports(cmdl);
	}
	
	public synchronized void addImports(String[] bshScriptCommands) {
		if (bshScriptCommands == null || bshScriptCommands.length == 0)
			return;
		ArrayList<String> cmdl = new ArrayList<String>();
		for (String s : bshScriptCommands)
			cmdl.add(s);
		addImports(cmdl);
	}
	
	public synchronized void addImports(Collection<String> bshScriptCommands) {
		for (String i : bshScriptCommands) {
			boolean found = false;
			for (String s : imports) {
				if (s.equals(i)) {
					found = true;
					break;
				}
			}
			if (!found)
				imports.add(i);
		}
	}
	
	public String getName() {
		return scenarioName;
	}
	
	public synchronized void addCommands(String[] bshScriptCommands) {
		if (bshScriptCommands == null || bshScriptCommands.length == 0)
			return;
		ArrayList<String> cmdl = new ArrayList<String>();
		for (String s : bshScriptCommands)
			cmdl.add(s);
		addCommands(cmdl);
	}
	
	public synchronized void addCommands(Collection<String> bshScriptCommands) {
		commands.addAll(bshScriptCommands);
	}
	
	public synchronized void addPluginCommand(ProvidesScenarioSupportCommands plugin) {
		addImports(plugin.getScenarioImports());
		addCommands(plugin.getScenarioCommands());
	}
	
	public synchronized Collection<String> getScenarioCommands() {
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(getHeader());
		result.addAll(imports);
		result.add("");
		result.add("// set to false, to enable user to customize algorithm parameters");
		result.add("boolean useStoredParameters = true;");
		result.add("");
		result.addAll(commands);
		return result;
	}
	
	private synchronized Collection<String> getHeader() {
		ArrayList<String> header = new ArrayList<String>();
		String menu = menuGroup.length() > 0 ? menuGroup + ":" : "";
		header.add("//@" + menu + scenarioName);
		header.add("//");
		return header;
	}
	
	@Override
	public synchronized String toString() {
		StringBuilder sb = new StringBuilder();
		for (String s : getScenarioCommands())
			sb.append(s + "\r\n");
		return sb.toString();
	}
	
	public boolean isValid() {
		if (readError)
			return false;
		
		boolean nameOk = scenarioName != null && scenarioName.length() > 0;
		boolean importsOk = imports != null;
		boolean sourceOk = commands != null;
		return nameOk && importsOk && sourceOk;
	}
	
	public String getMenu() {
		return menuGroup;
	}
	
	public String getFileName() {
		if (file != null)
			return file.getAbsolutePath();
		String menu = menuGroup.length() > 0 ? menuGroup + "_" : "";
		String path = ReleaseInfo.getAppFolderWithFinalSep();
		return path + menu + scenarioName + ".bsh";
	}
	
	public void setName(String name) {
		this.scenarioName = name;
	}
	
	public void setMenuGroup(String group) {
		this.menuGroup = group;
	}
}
