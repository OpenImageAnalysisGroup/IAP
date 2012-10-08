package de.ipk.ag_ba.server.pdf_report;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class PdfCreator {
	
	private File tempDirectory;
	
	final TreeMap<Long, String> output = new TreeMap<Long, String>();
	final ThreadSafeOptions tso = new ThreadSafeOptions();
	
	private final File optTargetDirectoryOrTargetFile;
	
	private ArrayList<String> lastOutput;
	
	public PdfCreator(File optTargetDirectoryOrTargetFile) {
		this.optTargetDirectoryOrTargetFile = optTargetDirectoryOrTargetFile;
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
	
	public void saveReportToFile(byte[] result, boolean xlsx) throws IOException {
		File report = getTargetFile(xlsx);
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(result);
		fos.close();
	}
	
	public void saveClusterDataToFile(byte[] result, boolean xlsx) throws IOException {
		File report = getTargetFileClustering(xlsx);
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(result);
		fos.close();
	}
	
	public File getTargetFile(boolean xlsx) {
		File report = new File(tempDirectory.getAbsoluteFile() + File.separator + "report." + (xlsx ? "xlsx" : "csv"));
		return report;
	}
	
	public File getTargetFileClustering(boolean xlsx) {
		File report = new File(tempDirectory.getAbsoluteFile() + File.separator + "report.clustering." + (xlsx ? "xlsx" : "csv"));
		return report;
	}
	
	public void executeRstat(final String[] parameter, ExperimentInterface exp,
			final BackgroundTaskStatusProviderSupportingExternalCall optStatus, final ArrayList<String> lastOutput, int timeoutMinutes) throws IOException {
		this.lastOutput = lastOutput;
		readAndModifyLaTexFile("reportDefGeneralSection.tex", exp);
		
		String name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.cmd";
		if (AttributeHelper.windowsRunning())
			name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.bat";
		else {
			new File(name).setExecutable(true);
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">EXECUTE: " + name +
				", WITH PARAMETERS: " + StringManipulationTools.getStringList(parameter, " / "));
		
		final String nameF = name;
		
		final ObjectRef myRef = new ObjectRef();
		
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
					MyThread t1 = null;
					try {
						t1 = new MyThread(new Runnable() {
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
					MyThread t2 = null;
					try {
						t2 = new MyThread(new Runnable() {
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
						t1.start();
						t2.start();
						BackgroundThreadDispatcher.waitFor(new MyThread[] { t1, t2 });
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
							"30 minutes and has therefore been canceled.");
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
		for (SubstanceInterface si : e)
			for (ConditionInterface ci : si) {
				String id = StringManipulationTools.string2Latex(ci.getConditionId() + "");
				String sp = StringManipulationTools.string2Latex(ci.getSpecies() != null ? ci.getSpecies() : "");
				String gt = StringManipulationTools.string2Latex(ci.getGenotype() != null ? ci.getGenotype() : "");
				String v = StringManipulationTools.string2Latex(ci.getVariety() != null ? ci.getVariety() : "");
				String t = StringManipulationTools.string2Latex(ci.getTreatment() != null ? ci.getTreatment() : "");
				String gc = StringManipulationTools.string2Latex(ci.getGrowthconditions() != null ? ci.getGrowthconditions() : "");
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
	
	public void openTargetDirectory() {
		AttributeHelper.showInBrowser(tempDirectory + "");
	}
	
	public String getOutput() {
		return "<html>" + StringManipulationTools.getStringList(output.values(), "<br>");
	}
}
