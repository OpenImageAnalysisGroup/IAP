package de.ipk.ag_ba.server.pdf_report;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.TreeMap;

import org.AttributeHelper;
import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class PdfCreator {
	
	private File tempDirectory;
	
	final TreeMap<Long, String> output = new TreeMap<Long, String>();
	final ThreadSafeOptions tso = new ThreadSafeOptions();
	
	private final ExperimentInterface experiment;
	
	public PdfCreator(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	public void prepareTempDirectory() throws IOException {
		String fn = ReleaseInfo.getAppFolderWithFinalSep() + System.nanoTime();
		this.tempDirectory = new File(fn);
		boolean res = tempDirectory.mkdir();
		if (!res)
			output.put(System.nanoTime(), "ERROR: Could not create temp directory: " + fn);
		else
			output.put(System.nanoTime(), "INFO: Created temp directory: " + fn);
		
		output.put(System.nanoTime(), "INFO: Temp directory existing?: " + tempDirectory.canRead());
		// createTempDirectory();
	}
	
	private File createTempDirectory() throws IOException {
		File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
		
		if (!(temp.delete()))
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		
		if (!(temp.mkdir()))
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		
		return temp;
	}
	
	public void saveReportCSV(byte[] result, boolean xlsx) throws IOException {
		File report = new File(tempDirectory.getAbsoluteFile() + File.separator + "report." + (xlsx ? "xlsx" : "csv"));
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(result);
		fos.close();
	}
	
	public void executeRstat(final String[] parameter) throws IOException {
		readAndModify("report2.tex");
		
		String name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.cmd";
		if (AttributeHelper.windowsRunning())
			name = tempDirectory.getAbsolutePath() + File.separator + "diagramIAP.bat";
		else {
			new File(name).setExecutable(true);
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">EXECUTE: " + name + ", WITH PARAMETER (" + StringManipulationTools.getStringList(parameter, " / ")
				+ ")");
		
		final String nameF = name;
		
		final ObjectRef myRef = new ObjectRef();
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Process ls_proc;
					if (parameter != null && parameter.length > 0)
						ls_proc = Runtime.getRuntime().exec(nameF);
					else
						ls_proc = Runtime.getRuntime().exec(nameF, parameter);
					
					myRef.setObject(ls_proc);
					
					DataInputStream ls_in = new DataInputStream(ls_proc.getInputStream());
					DataInputStream ls_in2 = new DataInputStream(ls_proc.getErrorStream());
					
					String response;
					while ((response = ls_in.readLine()) != null) {
						output.put(System.nanoTime(), "INFO:  " + response);
					}
					while ((response = ls_in2.readLine()) != null) {
						output.put(System.nanoTime(), "ERROR: " + response);
					}
				} catch (IOException e) {
					output.put(System.nanoTime(), "ERROR: EXCEPTION: " + e.getMessage());
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
				if (now - start > 1000 * 60 * 30 && myRef.getObject() != null) {
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
	private void readAndModify(String fn) throws IOException {
		
		File fff = new File(tempDirectory.getAbsolutePath() + File.separator + fn);
		String c = TextFile.read(fff);
		c = StringManipulationTools.stringReplace(c, "--experimentname--", safe(experiment.getName()));
		c = StringManipulationTools.stringReplace(c, "--StartExp--", safe(SystemAnalysis.getCurrentTime(experiment.getStartDate().getTime())));
		c = StringManipulationTools.stringReplace(c, "--EndExp--", safe(SystemAnalysis.getCurrentTime(experiment.getImportDate().getTime())));
		c = StringManipulationTools.stringReplace(c, "--NumExp--", safe(experiment.getNumberOfMeasurementValues() + ""));
		
		c = StringManipulationTools.stringReplace(c, "--ImagesExp--", safe(experiment.getHeader().getNumberOfFiles() + ""));
		
		c = StringManipulationTools.stringReplace(c, "--StorageExp--",
				safe(experiment.getHeader().getSizekb() + " KB"));
		
		c = StringManipulationTools.stringReplace(c, "--RemarkExp--",
				safe(experiment.getHeader().getRemark()));
		
		TextFile.write(tempDirectory.getAbsolutePath() + File.separator + fn, c);
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
		IOurl url = FileSystemHandler.getURL(new File(tempDirectory.getAbsolutePath() + File.separator + "report2.pdf"));
		return ResourceIOManager.getInputStreamMemoryCached(url).getBuffTrimmed();
	}
	
	public boolean hasPDFcontent() {
		File f = new File(tempDirectory.getAbsolutePath() + File.separator + "report2.pdf");
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
		return new File(tempDirectory.getAbsolutePath() + File.separator + "report2.pdf").getAbsolutePath();
	}
	
	public void openTargetDirectory() {
		AttributeHelper.showInBrowser(tempDirectory + "");
	}
}
