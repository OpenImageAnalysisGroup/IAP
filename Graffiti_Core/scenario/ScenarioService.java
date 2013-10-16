/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.03.2007 by Christian Klukas
 */
package scenario;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.Action;

import org.HelperClass;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.parameter.Parameter;

public class ScenarioService implements HelperClass {
	
	private static Scenario currentScenario;
	private static boolean recordingActive = false;
	
	private static ScenarioGui gui;
	
	public static Scenario getCurrentScenario() {
		return currentScenario;
	}
	
	public static void setCurrentScenario(Scenario scenario) {
		ScenarioService.currentScenario = scenario;
	}
	
	public static Scenario createScenario() {
		return new Scenario("", "");
	}
	
	public static boolean isRecording() {
		return recordingActive;
	}
	
	public static void recordStop() {
		recordingActive = false;
	}
	
	@SuppressWarnings("deprecation")
	public static void recordStart() {
		currentScenario = new Scenario("Workflow", "Recorded at " + new Date().toLocaleString());
		recordingActive = true;
	}
	
	public static void setGUI(ScenarioGui gui) {
		ScenarioService.gui = gui;
	}
	
	public static void postWorkflowStep(Algorithm algorithm, Parameter[] params) {
		if (isRecording()) {
			if (algorithm instanceof ScenarioServiceIgnoreAlgorithm)
				return;
			if (gui != null)
				gui.postWorkflowStep(algorithm, params);
			currentScenario.addImports(getImportsForAlgorithm(algorithm, params));
			currentScenario.addCommands(getStartCommandsForAlgorithm(algorithm, params));
		}
	}
	
	public static void postWorkflowStep(Action action) {
		if (isRecording()) {
			if (gui != null)
				gui.postWorkflowStep(action);
			currentScenario.addImports(getImportsForAction(action));
			currentScenario.addCommands(getCommandsForAction(action));
		}
	}
	
	private static Collection<String> getCommandsForAction(Action action) {
		ArrayList<String> res = new ArrayList<String>();
		
		String name = (String) action.getValue("name");
		res.add("GraffitiAction.performAction(\"" + name + "\");");
		
		return res;
	}
	
	private static Collection<String> getImportsForAction(Action action) {
		ArrayList<String> res = new ArrayList<String>();
		
		res.add("import org.graffiti.plugin.actions.GraffitiAction;");
		
		return res;
	}
	
	private static Collection<String> getImportsForAlgorithm(
						Algorithm algorithm, Parameter[] params) {
		ArrayList<String> res = new ArrayList<String>();
		
		res.add("import org.graffiti.editor.GravistoService;");
		res.add("import org.graffiti.editor.MainFrame;");
		
		Package p = algorithm.getClass().getPackage();
		res.add("import " + p.getName() + ".*;");
		
		boolean canStoreParams = getCanStoreParams(params);
		if (canStoreParams) {
			for (String i : getParameterInstanciationImports(params))
				res.add(i);
		}
		return res;
	}
	
	private static Collection<String> getStartCommandsForAlgorithm(
						Algorithm algorithm, Parameter[] params) {
		ArrayList<String> res = new ArrayList<String>();
		
		boolean canStoreParams = getCanStoreParams(params);
		boolean processesStoredParamsByItself = (algorithm instanceof ScenarioServiceHandlesStoredParametersOption);
		res.add("// Starting Algorithm " + algorithm.getName());
		res.add("{");
		if (processesStoredParamsByItself) {
			res.add("   if (!useStoredParameters) {");
			res.add("      GravistoService.run(\"" + algorithm.getName() + "\");");
			res.add("   }");
		} else
			if (canStoreParams) {
				res.add("   if (useStoredParameters) {");
				res.add("      " + algorithm.getClass().getSimpleName() + " algo = new " + algorithm.getClass().getSimpleName() + "();");
				res.add("      GravistoService.attachData(algo);");
				for (String c : getParameterInstanciationCommands(params, "      "))
					res.add(c);
				res.add("      algo.setParameters(params);");
				res.add("      algo.check();");
				res.add("      algo.execute();");
				res.add("      algo.reset();");
				res.add("   } else {");
				res.add("      GravistoService.run(\"" + algorithm.getName() + "\");");
				res.add("   }");
			} else {
				res.add("   // complex parameters could not be stored as script commands, using user-provided parameters instead");
				res.add("   GravistoService.run(\"" + algorithm.getName() + "\");");
			}
		res.add("}");
		return res;
	}
	
	private static Collection<String> getParameterInstanciationImports(Parameter[] params) {
		ArrayList<String> res = new ArrayList<String>();
		if (params == null || params.length == 0)
			return res;
		else {
			res.add("import org.graffiti.plugin.parameter.Parameter;");
			for (Parameter p : params) {
				ProvidesScenarioSupportCommand sp = (ProvidesScenarioSupportCommand) p;
				for (String i : sp.getScenarioImports())
					res.add(i);
			}
		}
		return res;
	}
	
	private static Collection<String> getParameterInstanciationCommands(
						Parameter[] params, String frontSpace) {
		ArrayList<String> res = new ArrayList<String>();
		if (params == null)
			res.add(frontSpace + "Parameter[] params = null;");
		else {
			if (params.length == 0)
				res.add(frontSpace + "Parameter[] params = new Parameter[] {};");
			else {
				res.add(frontSpace + "Parameter[] params = new Parameter[] {");
				int idx = 0;
				int max = params.length - 1;
				for (Parameter p : params) {
					ProvidesScenarioSupportCommand sp = (ProvidesScenarioSupportCommand) p;
					res.add(frontSpace + frontSpace + sp.getScenarioCommand() + (idx < max ? "," : ""));
					idx++;
				}
				res.add(frontSpace + "};");
			}
		}
		return res;
	}
	
	private static boolean getCanStoreParams(Parameter[] params) {
		if (params == null || params.length == 0)
			return true;
		else {
			boolean allOK = true;
			for (Parameter p : params) {
				if (!(p instanceof ProvidesScenarioSupportCommand)) {
					allOK = false;
					break;
				}
			}
			return allOK;
		}
	}
	
	public static Collection<Scenario> getAvailableScnenarios() {
		Collection<Scenario> result = new ArrayList<Scenario>();
		String folder = ReleaseInfo.getAppFolderWithFinalSep();
		File dir = new File(folder);
		for (File f : dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.canRead() && pathname.getAbsolutePath().toLowerCase().endsWith(".bsh");
			}
		})) {
			
			Scenario s = new Scenario(f);
			if (s.isValid())
				result.add(s);
		}
		return result;
	}
	
	public static void postWorkflowStep(String title, String[] imports, String[] commands) {
		if (isRecording()) {
			if (gui != null)
				gui.postWorkflowStep(title, imports, commands);
			currentScenario.addImports(imports);
			ArrayList<String> comments = new ArrayList<String>();
			comments.add("// " + title);
			comments.add("{");
			currentScenario.addCommands(comments);
			for (int i = 0; i < commands.length; i++)
				commands[i] = "   " + commands[i];
			currentScenario.addCommands(commands);
			comments.clear();
			comments.add("}");
			currentScenario.addCommands(comments);
		}
	}
}
