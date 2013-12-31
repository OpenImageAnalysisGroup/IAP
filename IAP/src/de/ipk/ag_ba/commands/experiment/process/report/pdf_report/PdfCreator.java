package de.ipk.ag_ba.commands.experiment.process.report.pdf_report;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class PdfCreator {
	
	private File tempDirectory;
	
	final TreeMap<Long, String> output = new TreeMap<Long, String>();
	final ThreadSafeOptions tso = new ThreadSafeOptions();
	
	private final File optTargetDirectoryOrTargetFile;
	
	private boolean useIndividualReportNames;
	
	private String customClusterTargetFile;
	
	public PdfCreator(File optTargetDirectoryOrTargetFile) {
		this.optTargetDirectoryOrTargetFile = optTargetDirectoryOrTargetFile;
		this.tempDirectory = optTargetDirectoryOrTargetFile;
	}
	
	public void prepareTempDirectory() throws IOException {
		if (optTargetDirectoryOrTargetFile != null) {
			this.tempDirectory = optTargetDirectoryOrTargetFile;
		} else {
			String fn = ReleaseInfo.getAppFolderWithFinalSep() + System.nanoTime();
			this.tempDirectory = new File(fn);
			boolean res = tempDirectory.mkdir();
			if (!res)
				output.put(System.nanoTime(), "ERROR: Could not create temp directory: " + fn);
			else
				output.put(System.nanoTime(), "INFO: Created temp directory: " + fn);
		}
		output.put(System.nanoTime(), "INFO: Temp directory existing?: " + tempDirectory.canRead());
	}
	
	public File saveReportToFile(byte[] result, boolean xlsx, ExperimentHeaderInterface optEH) throws IOException {
		File report = getTargetFile(xlsx, optEH);
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(result);
		fos.close();
		return report;
	}
	
	public void saveClusterDataToFile(byte[] result, boolean xlsx) throws IOException {
		File report = getTargetFileClustering(xlsx);
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(result);
		fos.close();
	}
	
	public File getTargetFile(boolean xlsx, ExperimentHeaderInterface optExpHeader) {
		if (optExpHeader != null && useIndividualReportNames) {
			String en = optExpHeader.getExperimentName();
			en = StringManipulationTools.getFileSystemName(en);
			File report = new File(tempDirectory.getAbsoluteFile() + File.separator + en + "." + (xlsx ? "xlsx" : "csv"));
			return report;
		} else {
			File report = new File(tempDirectory.getAbsoluteFile() + File.separator + "report." + (xlsx ? "xlsx" : "csv"));
			return report;
		}
	}
	
	public File getTargetFileClustering(boolean xlsx) {
		File report;
		if (customClusterTargetFile != null)
			report = new File(customClusterTargetFile);
		else
			report = new File(tempDirectory.getAbsoluteFile() + File.separator + "report.clustering." + (xlsx ? "xlsx" : "csv"));
		return report;
	}
	
	public void executeRstat(String[] para, ExperimentInterface exp,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus, final ArrayList<String> lastOutput, int timeoutMinutes) throws IOException {
		
		para = extendParameter(para,
				SystemOptions.getInstance().getBoolean("PDF Report Generation", "enforce minimum R package versions", true),
				SystemOptions.getInstance().getBoolean("PDF Report Generation", "install missing required R packages", false),
				SystemOptions.getInstance().getBoolean("PDF Report Generation", "automatic update of R packages", false),
				SystemOptions.getInstance().getBoolean("PDF Report Generation", "script debug function", false),
				SystemOptions.getInstance().getBoolean("PDF Report Generation", "catch errors", true));
		
		readAndModifyLaTexFile("reportDefGeneralSection.tex", exp);
		
		String name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.cmd";
		if (AttributeHelper.windowsRunning())
			name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.bat";
		else {
			new File(name).setExecutable(true);
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">EXECUTE: " + name +
				", WITH PARAMETERS: " + StringManipulationTools.getStringList(para, " / "));
		
		final String nameF = name;
		final ObjectRef myRef = new ObjectRef();
		final String[] parameter = para;
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Process ls_proc;
					String[] nameArray = new String[parameter.length + 1];
					nameArray[0] = nameF;
					for (int i = 0; i < parameter.length; i++)
						nameArray[i + 1] = parameter[i];
					
					ls_proc = Runtime.getRuntime().exec(nameArray, null, null);
					
					myRef.setObject(ls_proc);
					
					final DataInputStream ls_in = new DataInputStream(ls_proc.getInputStream());
					final DataInputStream ls_in2 = new DataInputStream(ls_proc.getErrorStream());
					LocalComputeJob t1 = null;
					try {
						t1 = new LocalComputeJob(new Runnable() {
							@SuppressWarnings("deprecation")
							@Override
							public void run() {
								String response;
								try {
									while ((response = ls_in.readLine()) != null) {
										output.put(System.nanoTime(), "INFO:  " + response);
										System.out.println(response);
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText2(StringManipulationTools.stringReplace(response, "\"", ""));
										synchronized (lastOutput) {
											lastOutput.add(response);
											while (lastOutput.size() > 20)
												lastOutput.remove(0);
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, "PDF OUT");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					LocalComputeJob t2 = null;
					try {
						t2 = new LocalComputeJob(new Runnable() {
							@SuppressWarnings("deprecation")
							@Override
							public void run() {
								String response;
								try {
									while ((response = ls_in2.readLine()) != null) {
										output.put(System.nanoTime(), "ERROR: " + response);
										System.err.println(response);
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
										if (optStatus != null && response != null && response.trim().length() > 0)
											optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": ERROR: "
													+ StringManipulationTools.stringReplace(response, "\"", ""));
										synchronized (lastOutput) {
											lastOutput.add(SystemAnalysis.getCurrentTime() + ": ERROR: " + response);
											while (lastOutput.size() > 20)
												lastOutput.remove(0);
										}
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, "PDF ERR");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					try {
						BackgroundThreadDispatcher.addTask(t1);
						BackgroundThreadDispatcher.addTask(t2);
						BackgroundThreadDispatcher.waitFor(new LocalComputeJob[] { t1, t2 });
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (optStatus != null)
						optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
					if (optStatus != null)
						optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": Finished PDF creation");
				} catch (IOException e) {
					output.put(System.nanoTime(), "ERROR: EXCEPTION: " + e.getMessage());
					if (optStatus != null)
						optStatus.setCurrentStatusText1(optStatus.getCurrentStatusMessage2());
					if (optStatus != null)
						optStatus.setCurrentStatusText2(SystemAnalysis.getCurrentTime() + ": ERROR: " + e.getMessage());
					tso.setBval(1, true);
				}
				tso.setBval(0, true);
				System.out.println(SystemAnalysis.getCurrentTime() + ">FINISHED PDF CREATION TASK");
			}
		};
		
		Thread t = new Thread(r, "Execute " + name);
		t.start();
		
		try {
			long start = System.currentTimeMillis();
			while (!tso.getBval(0, false)) {
				Thread.sleep(20);
				long now = System.currentTimeMillis();
				if (now - start > 1000 * 60 * timeoutMinutes && myRef.getObject() != null) {
					output.put(System.nanoTime(), "ERROR: TIME-OUT: " +
							"Report generation took more than " +
							timeoutMinutes + " minutes and has therefore been cancelled.");
					tso.setBval(1, true);
					if (myRef.getObject() != null) {
						Process ls_proc = (Process) myRef.getObject();
						ls_proc.destroy();
					}
					break;
				}
			}
		} catch (InterruptedException e) {
			tso.setBval(1, true);
			throw new UnsupportedOperationException(e);
		}
	}
	
	private String[] extendParameter(String[] parameter, boolean... b) {
		String[] res = new String[parameter.length + b.length];
		for (int i = 0; i < parameter.length; i++)
			res[i] = parameter[i];
		for (int i = parameter.length; i < parameter.length + b.length; i++)
			res[i] = ("" + b[i - parameter.length]).toUpperCase();
		return res;
	}
	
	/**
	 * Modify latex file
	 * 
	 * @param fn
	 *           file name
	 * @throws IOException
	 */
	private void readAndModifyLaTexFile(String fn, ExperimentInterface experiment) throws IOException {
		
		File fff = new File(tempDirectory.getAbsolutePath() + File.separator + fn);
		String c = TextFile.read(fff);
		String experimentName = safe(StringManipulationTools.stringReplace(experiment.getName(), ".", " "));
		
		c = StringManipulationTools.stringReplace(c, "--experimentname--",
				StringManipulationTools.stringReplace(experimentName, " (", "\\ ("));
		
		experimentName = StringManipulationTools.stringReplace(experimentName, "\\textunderscore", "_");
		if (experimentName.length() > 80) {
			experimentName = experimentName.substring(0, 80).concat(" ...");
		}
		experimentName = StringManipulationTools.stringReplace(experimentName, "_", "\\textunderscore");
		
		c = StringManipulationTools.stringReplace(c, "--experimentnameShort--", experimentName);
		c = StringManipulationTools.stringReplace(c, "--coordinator--", safe(experiment.getCoordinator()));
		c = StringManipulationTools.stringReplace(c, "--EndExp--",
				safe(SystemAnalysis.getCurrentTime(experiment.getImportDate() != null ? experiment.getImportDate().getTime() : 0)));
		c = StringManipulationTools.stringReplace(c, "--StorageDate--",
				safe(SystemAnalysis.getCurrentTime(experiment.getHeader().getStorageTime() != null ? experiment.getHeader().getStorageTime().getTime() : 0)));
		c = StringManipulationTools.stringReplace(c, "--StartExp--",
				safe(SystemAnalysis.getCurrentTime(experiment.getStartDate() != null ? experiment.getStartDate().getTime() : 0)));
		c = StringManipulationTools.stringReplace(c, "--NumExp--", safe(experiment.getNumberOfMeasurementValues() + ""));
		c = StringManipulationTools.stringReplace(c, "--Outliers--",
				safe(experiment.getHeader().getGlobalOutlierInfo() != null ?
						StringManipulationTools.stringReplace(experiment.getHeader().getGlobalOutlierInfo(), "//", "; ")
						: ""));
		c = StringManipulationTools.stringReplace(c, "--SequenceEXP--",
				safe(experiment.getHeader().getSequence() != null ?
						StringManipulationTools.stringReplace(experiment.getHeader().getSequence(), "//", "& \\tabularnewline ")
						: ""));
		
		c = StringManipulationTools.stringReplace(c, "--ImagesExp--", safe(experiment.getHeader().getNumberOfFiles() + ""));
		
		String st = experiment.getHeader().getSizekb() / 1024 / 1024 + " GB";
		if (experiment.getHeader().getSizekb() / 1024 < 10000)
			st = experiment.getHeader().getSizekb() / 1024 + " MB";
		
		c = StringManipulationTools.stringReplace(c, "--StorageExp--",
				st);
		
		c = StringManipulationTools.stringReplace(c, "--RemarkExp--",
				safe(experiment.getHeader().getRemark()));
		
		c = StringManipulationTools.stringReplace(c, " // ", " \\tabularnewline ");
		
		// --ID-- & --Genotype-- & --Variety-- & --Treatment-- & --Sequence-- & --xyz--\tabularnewline
		// \hline
		// %factorlist
		String rows = StringManipulationTools.getStringList(getConditions(experiment), "\n");
		c = StringManipulationTools.stringReplace(c, "--factorlist--", rows);
		
		TextFile.write(tempDirectory.getAbsolutePath() + File.separator + fn, c);
	}
	
	public TreeSet<String> getConditions(ExperimentInterface e) {
		TreeSet<String> result = new TreeSet<String>();
		HashSet<String> treatments = new HashSet<String>();
		HashSet<String> growthConds = new HashSet<String>();
		for (SubstanceInterface si : e)
			for (ConditionInterface ci : si) {
				String t = StringManipulationTools.string2Latex(ci.getTreatment() != null ? ci.getTreatment() : "");
				String gc = StringManipulationTools.string2Latex(ci.getGrowthconditions() != null ? ci.getGrowthconditions() : "");
				if (t != null && !t.trim().isEmpty())
					treatments.add(t);
				if (gc != null && !gc.trim().isEmpty())
					growthConds.add(gc);
			}
		
		for (SubstanceInterface si : e)
			for (ConditionInterface ci : si) {
				String id = StringManipulationTools.string2Latex(ci.getConditionId() + "");
				String sp = StringManipulationTools.string2Latex(ci.getSpecies() != null ? ci.getSpecies() : "");
				String gt = StringManipulationTools.string2Latex(ci.getGenotype() != null ? ci.getGenotype() : "");
				String v = StringManipulationTools.string2Latex(ci.getVariety() != null ? ci.getVariety() : "");
				String t = StringManipulationTools.string2Latex(ci.getTreatment() != null ? ci.getTreatment() : "");
				String gc = StringManipulationTools.string2Latex(ci.getGrowthconditions() != null ? ci.getGrowthconditions() : "");
				if (sp != null && sp.length() > 40)
					sp = sp.substring(0, 36) + " ...";
				if (gt != null && gt.length() > 40)
					gt = gt.substring(0, 36) + " ...";
				if (v != null && v.length() > 40)
					v = v.substring(0, 40) + " ...";
				if (t != null && t.length() > 40)
					t = t.substring(0, 36) + " ...";
				if (gc != null && gc.length() > 40)
					gc = gc.substring(0, 36) + " ...";
				if (treatments.size() == 1 && !t.trim().isEmpty())
					t = "(all equal)";
				if (growthConds.size() == 1 && !gc.trim().isEmpty())
					gc = "(all equal)";
				boolean first = true;
				String row = "";
				// t.split(";")
				for (String inRow : new String[] { t }) {
					if (first) {
						row = safe(id + " & " + sp + " & " + gt + " & " + v + " & " + inRow + " & " + gc + " \\tabularnewline");
						first = false;
					} else {
						row += safe(" &  &  &  & " + inRow + " & &  \\tabularnewline");
					}
				}
				
				row += " \\hline";
				row = StringManipulationTools.stringReplace(row, "=", ": ");
				row = StringManipulationTools.stringReplace(row, "(", " (");
				row = StringManipulationTools.stringReplace(row, ";", ", ");
				row = StringManipulationTools.stringReplace(row, "  ", " ");
				result.add(row);
			}
		return result;
	}
	
	private String safe(String name) {
		String res = StringManipulationTools.stringReplace(name, "_", "\\textunderscore ");
		return res;
	}
	
	public void saveScripts(String[] files) throws Exception {
		ClassLoader cl = PdfCreator.class.getClassLoader();
		String path = PdfCreator.class.getPackage().getName().replace('.', '/');
		
		for (String file : files) {
			FileOutputStream out = new FileOutputStream(tempDirectory.getAbsolutePath() + File.separator + file);
			
			URL res = cl.getResource(path + "/" + file);
			
			if (res != null) {
				InputStream inpStream = res.openStream();
				if (inpStream != null) {
					if (!SystemAnalysis.isWindowsRunning() && file.endsWith(".cmd")) {
						String cnt = TextFile.read(inpStream, -1);
						cnt = StringManipulationTools.stringReplace(cnt, "\r\n", "\n");
						TextFile txt = new TextFile();
						for (String line : cnt.split("\n"))
							txt.add(line);
						txt.write(out);
					} else
						ResourceIOManager.copyContent(inpStream, out);
					output.put(System.nanoTime(), "INFO: Copy OK: " + path + "/" + file);
				} else
					output.put(System.nanoTime(), "ERROR: EXCEPTION: " + "Can't get input stream for resource: " + path + "/" + file);
			} else
				output.put(System.nanoTime(), "ERROR: EXCEPTION: " + "Can't get URL for resource: " + path + "/" + file);
		}
	}
	
	public byte[] getPDFcontent() throws IOException, Exception {
		IOurl url = FileSystemHandler.getURL(new File(tempDirectory.getAbsolutePath() + File.separator + "report.pdf"));
		return ResourceIOManager.getInputStreamMemoryCached(url).getBuffTrimmed();
	}
	
	public boolean hasPDFcontent() {
		File f = new File(tempDirectory.getAbsolutePath() + File.separator + "report.pdf");
		return f.canRead() && !tso.getBval(1, false);
	}
	
	public byte[] getErrorContent() throws IOException {
		MyByteArrayOutputStream bos = new MyByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(bos);
		for (String val : output.values()) {
			out.write(val.getBytes());
			out.write("\r\n".getBytes());
		}
		out.close();
		return bos.getBuffTrimmed();
	}
	
	public void deleteAllWithout(String[] files, String[] validSubDirs) {
		deletAllInThisDir(tempDirectory, files, validSubDirs);
	}
	
	private void deletAllInThisDir(File dir, String[] filesNotDelete, String[] validSubDirs) {
		// System.out.println("... delete all old files in the directory: " + dir.getName());
		boolean found = false;
		for (File file : dir.listFiles()) {
			if (!file.isDirectory()) {
				if (arrayContains(filesNotDelete, file.getName(), true)) {
					found = true;
					break;
				}
			}
		}
		if (!found)
			throw new UnsupportedOperationException("Target dir " + dir + " is not removed, did not find result files!");
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				if (arrayContains(validSubDirs, file.getName(), true)) {
					deletAllInThisDir(file, filesNotDelete, validSubDirs);
					file.delete();
				}
			} else {
				if (!arrayContains(filesNotDelete, file.getName(), true)) {
					if (!file.delete()) {
						System.err.println(file + " could not be deleted!");
					}
				}
			}
			
		}
		// System.out.println("... all files from directory " + dir.getName() + " are now deleted!");
	}
	
	public boolean arrayContains(Object[] array, Object value, boolean ignoreCase) {
		for (int i = 0; i < array.length; i++) {
			if (((array[i] == null) && (value == null))) {
				return true;
			} else {
				if (ignoreCase) {
					if (((String) array[i]).equalsIgnoreCase((String) value)) {
						return true;
					}
				} else {
					if (array[i].equals(value)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public void deleteDirectory() {
		deleteDir(tempDirectory);
	}
	
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				File child = new File(dir, children[i]);
				if (child.isDirectory())
					throw new UnsupportedOperationException("Internal Error, attempt to delete temp-sub-directory " + child.getAbsolutePath()); // don't delete
					
				boolean success = deleteDir(child);
				if (!success) {
					return false;
				}
			}
		}
		
		// The directory is now empty so delete it
		return dir.delete();
	}
	
	public String getPDFurl() {
		return new File(tempDirectory.getAbsolutePath() + File.separator + "report.pdf").getAbsolutePath();
	}
	
	public IOurl getPdfIOurl() {
		return FileSystemHandler.getURL(new File(tempDirectory.getAbsolutePath() + File.separator + "report.pdf"));
	}
	
	public void openTargetDirectory() {
		AttributeHelper.showInBrowser(tempDirectory + "");
	}
	
	public String getOutput() {
		return "<html>" + StringManipulationTools.getStringList(output.values(), "<br>");
	}
	
	public File getTempDirectory() {
		return tempDirectory;
	}
	
	public IOurl getTempDirectoryIOurl() {
		return FileSystemHandler.getURL(tempDirectory);
	}
	
	public void setUseIndividualReportNames(boolean useIndividualReportNames) {
		this.useIndividualReportNames = useIndividualReportNames;
	}
	
	public void setCustomClusterTargetFile(String customClusterTargetFile) {
		this.customClusterTargetFile = customClusterTargetFile;
	}
}
