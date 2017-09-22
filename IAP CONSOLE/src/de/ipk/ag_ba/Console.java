package de.ipk.ag_ba;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.editor.SplashScreenInterface;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.ActionHome;
import de.ipk.ag_ba.commands.ActionNavigateDataSource;
import de.ipk.ag_ba.commands.experiment.scripts.VirtualIoProvider;
import de.ipk.ag_ba.commands.lt.ActionLTnavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.SpecialCommandLineSupport;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.LTftpHandler;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.UserDefinedImageAnalysisPipelineTask;
import de.ipk.ag_ba.server.databases.DatabaseTargetNull;
import de.ipk.ag_ba.server.gwt.UrlCacheManager;
import de.ipk.ag_ba.server.task_management.BackupSupport;
import de.ipk.ag_ba.server.task_management.CloudComputingService;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.MainM;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author Christian Klukas
 *         1.2.2012
 */
public class Console {
	public static void main(String[] args) {
		IAPmain.setRunMode(IAPrunMode.CONSOLE);
		ErrorMsg.setRethrowErrorMessages(true);
		Console c = null;
		if (args.length == 0 || !args[0].toUpperCase().equalsIgnoreCase("/DI")) {
			for (String i : IAPmain.getMainInfoLines())
				System.out.println(i);
			if (paramContains(new String[] { "help", "h", "?" }, args)) {
				System.out.println("***********************************************************");
				System.out.println("* Usage                                                   *");
				System.out.println("* - no parameters: interactive console interface          *");
				System.out.println("* - /help, /h, /? - this help                             *");
				System.out.println("* - XYZ           - execute commands X, Y, Z              *");
				System.out.println("*                   these commands correspond to          *");
				System.out.println("*                   the keys you use inside the           *");
				System.out.println("*                   console interface                     *");
				System.out.println("* - _A,_B         - execute command named 'A' and         *");
				System.out.println("*                   then 'B'. Use underscore and          *");
				System.out.println("*                   comma to specify title lookup         *");
				System.out.println("*                   and to separate commands              *");
				System.out.println("* - the environment variable 'exec' is also               *");
				System.out.println("*   evaluated and may be used as an alternative           *");
				System.out.println("*   way to specify the commands to be executed            *");
				System.out.println("* - /SE [imgtype] [img[+back]] [pipeline] [resultfile]    *");
				System.out.println("*       [output path for images] [time from filename T/F] *");
				System.out.println("*                   execute pipeline on single or         *");
				System.out.println("*                   multiple images (using file           *");
				System.out.println("*                   mask and store results in             *");
				System.out.println("*                   single csv file or in separate        *");
				System.out.println("*                   files if just the extension           *");
				System.out.println("*                   is provided (e.g. '.txt').            *");
				System.out.println("*                   imgtypes: 'vis.top', ...              *");
				System.out.println("*                   If the image name of name mask        *");
				System.out.println("*                   contains an '+', the prefix will be   *");
				System.out.println("*                   the name of the imag file and the     *");
				System.out.println("*                   part after the '+' will be the        *");
				System.out.println("*                   reference image in the pipeline.      *");
				System.out.println("* - /DI MAX/MIN/MEAN [file-spec] [result file]            *");
				System.out.println("***********************************************************");
				System.exit(0);
			}
			
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: Welcome! About to initalize the application...");
			
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
						null));
			}
			
			c = new Console();
		}
		
		if (args.length > 0 && args[0].toUpperCase().equalsIgnoreCase("/DI")) {
			try {
				String fileSpec = args[1];
				String opSpec = args[2];
				String targetFileSpec = args[3];
				
				if (new File(fileSpec).exists()) {
					// process single file by just copying
					Image img = new Image(FileSystemHandler.getURL(new File(fileSpec)));
					img.saveToFile(targetFileSpec);
				} else {
					String path = ".";
					if (fileSpec.contains("/")) {
						path = fileSpec.split("/")[0];
						fileSpec = fileSpec.split("/", 2)[1];
					}
					
					ThreadSafeOptions tsoImgR = new ThreadSafeOptions();
					ThreadSafeOptions tsoImgG = new ThreadSafeOptions();
					ThreadSafeOptions tsoImgB = new ThreadSafeOptions();
					ThreadSafeOptions tsoImageCount = new ThreadSafeOptions();
					
					ArrayList<LocalComputeJob> tasks = new ArrayList<>();
					
					try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(path), fileSpec)) {
						dirStream.forEach(pppI -> {
							// process ppp.toString()
							try {
								final Path ppp = pppI;
								tasks.add(BackgroundThreadDispatcher.addTask(new Runnable() {
									
									@Override
									public void run() {
										try {
											Image img;
											img = new Image(FileSystemHandler.getURL(ppp.toFile()), false);
											tsoImageCount.addInt(1);
											
											int[][] pxRGB = img.io().channels().getRGBintensities();
											int[] pxR = pxRGB[0];
											int[] pxG = pxRGB[1];
											int[] pxB = pxRGB[2];
											
											if (img.io().countColors() < 10) {
												throw new RuntimeException("Only " + img.io().countColors() + " colors in " + ppp.toAbsolutePath());
											}
											
											synchronized (tsoImageCount) {
												if (tsoImgR.getParam(0, null) != null) {
													int[] prevR = (int[]) tsoImgR.getParam(0, null);
													int[] prevG = (int[]) tsoImgG.getParam(0, null);
													int[] prevB = (int[]) tsoImgB.getParam(0, null);
													
													if (prevR.length != pxR.length)
														throw new Exception("Image size for " + ppp.toString() + " differs from previous image size!");
													else
														switch (opSpec.toUpperCase()) {
															case "MAX":
																for (int i = 0; i < pxR.length; i++) {
																	prevR[i] = Math.max(prevR[i], pxR[i]);
																	prevG[i] = Math.max(prevG[i], pxG[i]);
																	prevB[i] = Math.max(prevB[i], pxB[i]);
																}
																break;
															case "MIN":
																for (int i = 0; i < pxR.length; i++) {
																	prevR[i] = Math.min(prevR[i], pxR[i]);
																	prevG[i] = Math.min(prevG[i], pxG[i]);
																	prevB[i] = Math.min(prevB[i], pxB[i]);
																}
																break;
															case "MEAN":
																for (int i = 0; i < pxR.length; i++) {
																	prevR[i] = prevR[i] + pxR[i];
																	prevG[i] = prevG[i] + pxG[i];
																	prevB[i] = prevB[i] + pxB[i];
																}
																break;
															default:
																throw new RuntimeException("Unknown operation mode (only max/min/mean supported): " + opSpec);
														}
												} else {
													tsoImgR.setParam(0, pxR);
													tsoImgG.setParam(0, pxG);
													tsoImgB.setParam(0, pxB);
													tsoImgR.setLong(img.getWidth());
												}
											}
										} catch (Exception e) {
											e.printStackTrace();
											System.exit(1);
										}
									}
								}, ppp.toString()));
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}
						});
						try {
							BackgroundThreadDispatcher.waitFor(tasks);
						} catch (InterruptedException e) {
							e.printStackTrace();
							System.exit(1);
						}
						int[] prevR = (int[]) tsoImgR.getParam(0, null);
						int[] prevG = (int[]) tsoImgG.getParam(0, null);
						int[] prevB = (int[]) tsoImgB.getParam(0, null);
						if (opSpec.toUpperCase().equals("MEAN")) {
							int n = tsoImageCount.getInt();
							for (int i = 0; i < prevR.length; i++) {
								prevR[i] = prevR[i] / n;
								prevG[i] = prevG[i] / n;
								prevB[i] = prevB[i] / n;
							}
						}
						new Image((int) tsoImgR.getLong(), (int) (prevR.length / tsoImgR.getLong()), prevR, prevG, prevB).saveToFile(targetFileSpec);
					}
					System.exit(0);
				}
			} catch (Exception err) {
				err.printStackTrace();
				System.exit(1);
			}
		}
		
		if (args.length > 0 && args[0].toUpperCase().equalsIgnoreCase("/SE")) {
			String fileSpec = args[2];
			LinkedList<String> referenceImages = new LinkedList<>();
			if (fileSpec.contains("+")) {
				String[] fileNameAndMask = fileSpec.split("\\+", 2);
				fileSpec = fileNameAndMask[0];
				String refSpec = fileNameAndMask[1];
				if (new File(refSpec).exists()) {
					referenceImages.add(new File(refSpec).getAbsolutePath());
				} else {
					String path = ".";
					if (refSpec.contains("/")) {
						path = refSpec.split("/")[0];
						refSpec = refSpec.split("/", 2)[1];
					}
					
					try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(path), refSpec)) {
						dirStream.forEach(ppp -> {
							referenceImages.add(ppp.toString());
						});
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				if (referenceImages.isEmpty())
					throw new RuntimeException("Could not find reference images from specification '" + refSpec + "'!");
			}
			if (new File(fileSpec).exists()) {
				String refSpec = referenceImages.isEmpty() ? null : referenceImages.get(0);
				processSingleFile(args, fileSpec, refSpec);
			} else {
				ArrayList<LocalComputeJob> tasks = new ArrayList<>();
				String path = ".";
				if (fileSpec.contains("/")) {
					path = fileSpec.split("/")[0];
					fileSpec = fileSpec.split("/", 2)[1];
				}
				try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(path), fileSpec)) {
					dirStream.forEach(ppp -> {
						try {
							final String refSpec = referenceImages.isEmpty() ? null : referenceImages.get(0);
							if (!referenceImages.isEmpty())
								referenceImages.add(referenceImages.remove(0));
							
							tasks.add(BackgroundThreadDispatcher.addTask(new Runnable() {
								
								@Override
								public void run() {
									processSingleFile(args, ppp.toString(), refSpec);
								}
							}, "Process " + ppp.toString()));
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					});
				} catch (IOException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				try {
					BackgroundThreadDispatcher.waitFor(tasks);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			System.exit(0);
		}
		
		ArrayList<String> commandsFromArg = new ArrayList<>();
		
		for (String id : new String[] { "exec", "EXEC" }) {
			String ev = System.getenv(id);
			if (ev != null)
				
				parseInput(commandsFromArg, ev);
		}
		
		if (args.length > 0)
		
		{
			for (String a : args)
				parseInput(commandsFromArg, a);
		}
		while (true) {
			c.printGUI(commandsFromArg);
			c.waitForStatusChange();
		}
	}
	
	private static void processSingleFile(String[] args, String img, String refImg) {
		String resFile = args[4];
		if (args.length > 5) {
			String imgName = img.substring(0, img.lastIndexOf("."));
			if (imgName.contains("/"))
				imgName = imgName.substring(imgName.lastIndexOf("/") + 1);
			resFile = Paths.get(args[5], imgName + resFile).toString();
		} else
			if (resFile.startsWith(".")) {
				resFile = img.substring(0, img.lastIndexOf(".")) + resFile;
			}
		try {
			executePipeline(args[1], img, refImg, args[3], resFile, args.length > 5 ? args[5] : "", args.length > 6 ? ("" + args[6]).toUpperCase().startsWith("T") : false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void executePipeline(String imgtype, String img, String refImg, String pipeline, String resultfile, String resultImagePath, boolean timeFromFileName) throws IOException, InterruptedException {
		if (!new File(img).exists())
			throw new RuntimeException("Input image file '" + img + "' does not exist!");
		
		if (refImg != null && !refImg.isEmpty() && !new File(refImg).exists())
			throw new RuntimeException("Input reference image file '" + refImg + "' does not exist!");
		
		if (!new File(pipeline).exists())
			throw new RuntimeException("Input analysis pipeline file '" + pipeline + "' does not exist!");
		
		Experiment exp = new Experiment();
		exp.getHeader().setExperimentname("console execution/" + SystemAnalysis.getCurrentTimeInclSec());
		exp.getHeader().setExperimentType("pipeline/" + pipeline);
		
		SubstanceInterface sub = new Substance3D();
		sub.setName(imgtype);
		exp.add(sub);
		
		ConditionInterface con = new Condition3D(sub);
		sub.add(con);
		
		long d2 = new File(img).lastModified();
		
		URI folderRoute = new File(new File(img).getParent()).toURI();
		Path p = Paths.get(folderRoute).toAbsolutePath().getParent();
		
		con.setSpecies(p.getFileName().toString());
		
		Sample3D samp = new Sample3D(con);
		
		if (timeFromFileName) {
			try {
				String timeN = org.StringManipulationTools.getNumbersFromString(new File(img).getName());
				samp.setTime(Integer.parseInt(timeN));
				String timeUnitN = org.StringManipulationTools.removeNumbersFromString(new File(img).getName());
				timeUnitN = timeUnitN.substring(0, timeUnitN.lastIndexOf("."));
				timeUnitN = timeUnitN.trim();
				timeUnitN = timeUnitN.replaceAll("_+$", "");
				timeUnitN = timeUnitN.replaceAll("-+$", "");
				timeUnitN = timeUnitN.replaceAll("^_+", "");
				timeUnitN = timeUnitN.replaceAll("^-+", "");
				samp.setTimeUnit(timeUnitN);
				samp.setSampleFineTimeOrRowId(-1l);
			} catch (Exception e) {
				samp.setTime(-1);
				samp.setTimeUnit("no number in filename");
			}
		} else {
			// time from file attributes
			BasicFileAttributes attr = null;
			attr = Files.readAttributes(p, BasicFileAttributes.class);
			FileTime a = attr.creationTime();
			long d1 = a.toInstant().toEpochMilli();
			samp.setTime(1 + (int) ((d2 - d1) / (1000 * 60 * 60 * 24)));
			samp.setTimeUnit("day");
			samp.setSampleFineTimeOrRowId(d1);
		}
		
		con.add(samp);
		
		ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
		workload.add(samp);
		
		boolean fileOutputForImages = !(resultImagePath == null || resultImagePath.isEmpty() || resultImagePath.equals("-"));
		boolean printToStdOutForImages = resultImagePath.equals("-");
		
		ImageData id = new ImageData(samp);
		id.setURL(FileSystemHandler.getURL(new File(img)));
		if (refImg != null && !refImg.trim().isEmpty()) {
			id.setLabelURL(FileSystemHandler.getURL(new File(refImg)));
			SystemAnalysis.printlnOnce("INFO: Reference image: " + new File(refImg).getAbsolutePath(), true);
		} else
			SystemAnalysis.printlnOnce("INFO: No reference image defined", true);
		
		SystemAnalysis.printlnOnce("INFO: Analysis pipeline: " + pipeline, true);
		// System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Input image: " + new File(img).getAbsolutePath());
		
		samp.add(id);
		String fn = new File(img).getName();
		fn = fn.indexOf(".") > 0 ? fn.substring(0, fn.indexOf(".")) : fn;
		id.setQualityAnnotation(fn);
		
		VirtualIoProvider viop = new VirtualIoProvider();
		viop.setInstance(SystemOptions.getInstance("absolute:" + pipeline, null));
		
		String fnt = StringManipulationTools.stringReplace(pipeline, ".pipeline.ini", "");
		PipelineDesc pd = new PipelineDesc(pipeline, viop, fnt, fnt, "(not tested with specific IAP version)");
		
		AbstractPhenotypingTask analysisTaskFinal = new UserDefinedImageAnalysisPipelineTask(pd);
		analysisTaskFinal.setInput(exp.getHeader(), new TreeMap<>(), workload, null, null, 0, 1);
		DatabaseTargetNull dbt = (DatabaseTargetNull) analysisTaskFinal.getDatabaseTargetInst();
		
		dbt.saveImagesToSingleFolder = fileOutputForImages;
		dbt.ignoreTimeInOutputName = true;
		if (printToStdOutForImages)
			dbt.printImagesToConsole = true;
		else
			dbt.targetFolder = resultImagePath;
		
		analysisTaskFinal.setCommandlineMode(true);
		analysisTaskFinal.performAnalysis(new BackgroundTaskStatusProviderSupportingExternalCallImpl(null, null));
		ExperimentInterface out = analysisTaskFinal.getOutput();
		boolean fileOutput = !(resultfile == null || resultfile.isEmpty() || resultfile.equals("-"));
		boolean printToStdOut = resultfile.equals("-");
		
		if (out != null) {
			if (printToStdOut)
				System.out.println();
			
			ArrayList<NumericMeasurementInterface> resultList = Substance3D.getAllMeasurements(out);
			
			if (resultList != null && !resultList.isEmpty()) {
				try (FileWriter fw = fileOutput ? new FileWriter(resultfile) : null) {
					for (NumericMeasurementInterface nmi : resultList) {
						if (nmi instanceof NumericMeasurement) {
							NumericMeasurement nm = (NumericMeasurement) nmi;
							String sub2 = nm.getParentSample().getParentCondition().getParentSubstance().getName();
							String cnt = SystemAnalysis.getCurrentTimeInclSec() + "/RESULT/"
									+ nm.getQualityAnnotation() + "/" + nm.getParentSample().getTime() + "/"
									+ nm.getParentSample().getParentCondition().getConditionName() + "/"
									+ sub2 + "/" + nm.getValue() + "/" + nm.getUnit();
							if (!fileOutput) {
								if (printToStdOut)
									System.out.println(cnt);
							} else {
								fw.write(cnt + System.lineSeparator());
							}
						}
					}
				}
			}
		}
	}
	
	private static void parseInput(ArrayList<String> commandsFromArg, String ev) {
		if (ev == null)
			return;
		for (String s : ev.split(",")) {
			if (!s.startsWith("_"))
				for (char sl : s.toCharArray())
					commandsFromArg.add(sl + "");
			else
				commandsFromArg.add(s);
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
	
	private void printGUI(ArrayList<String> commandsFromArg) {
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
					cmd2b.put("_" + title.toUpperCase(), n);
				}
			}
			System.out.println();
		}
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println(SystemAnalysis.getCurrentTime()
				+ ">INFO: READY, enter number or character to navigate or execute a command, just press ENTER to update display");
		System.out.print(SystemAnalysis.getCurrentTime() + ">");
		String input;
		if (commandsFromArg.isEmpty()) {
			String inp = SystemAnalysis.getCommandLineInput();
			parseInput(commandsFromArg, inp);
		}
		if (commandsFromArg.isEmpty())
			commandsFromArg.add("");
		input = commandsFromArg.remove(0);
		
		if (input != null)
			input = input.trim();
		if (input != null && cmd2b.containsKey(input.toUpperCase())) {
			NavigationButton n = cmd2b.get(input.toUpperCase());
			System.out.println(SystemAnalysis.getCurrentTime() + ">EXECUTE: \"" + StringManipulationTools.removeHTMLtags(n.getTitle() + "\""));
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
				"IAP/VANTED",
				"CCTV (Maize)",
				"CCTV (Barley)",
				"Exp. Planning",
				"Documentation",
				"Logout",
				"View Data",
				"Schedule Experiment",
				"Performance Test",
				"Watering Table",
				"Show in IAP/VANTED"
		};
		invalid = SystemOptions.getInstance().getStringAll("IAP-CONSOLE-MODE", "invalid_cmds", invalid);
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
									System.out.println(SystemAnalysis.getCurrentTime() + ">----------------------");
								System.out.println(SystemAnalysis.getCurrentTime() + ">"
										+ StringManipulationTools.removeHTMLtags(
												o.replace("<br>", "\r\n> ").replace("<li>", "\r\n> - "))
												.replace("&gt;", ">"));
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
				new MainM((args.length > 0 ? args[0] : DBEgravistoHelper.DBE_GRAVISTO_NAME_SHORT), args,
						new String[] {}, emptySplashScreen, "pluginsIAP.txt", false);
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
			ActionHome ha = new ActionHome(new BackgroundTaskStatusProviderSupportingExternalCallImpl("", ""), guiSetting);
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
