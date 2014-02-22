package de.ipk.ag_ba.server.task_management.minitoring_and_networking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class SnapshotCreator {
	
	private final String inputFileDir;
	private final String outputSnapshotDir;
	private final DirectoryMonitor monitor;
	
	public SnapshotCreator(String inputFileDir, String outputSnapshotDir) throws IOException {
		this.inputFileDir = inputFileDir;
		this.outputSnapshotDir = outputSnapshotDir;
		this.monitor = new DirectoryMonitor();
	}
	
	public void saveNewSnapshot(String plantID, String measurementLabel, String fileExt) throws Exception {
		System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: Wait for new image file to appear...");
		String newImageFile = monitor.getNextAppearingFile(inputFileDir, 5000);
		if (newImageFile == null) {
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">WARNING: Detected plant ID " + plantID
					+ ", but could not find a new image being created!");
			throw new Exception("No valid file appeared within the expected time period of a maximum of 5 seconds!");
		}
		newImageFile = inputFileDir + File.separator + newImageFile;
		while (SystemAnalysis.isFileOpen(newImageFile)) {
			Thread.sleep(50);
		}
		System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: Detected plant ID " + plantID + ", create snapshot data...");
		File outpFolder = new File(outputSnapshotDir);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(new File(newImageFile).lastModified()));
		
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH) + 1;
		int d = cal.get(Calendar.DAY_OF_MONTH);
		int h = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		File snapshotDir = new File(outpFolder.getAbsolutePath() + File.separator + measurementLabel + "_" + plantID
				+ "_" + y + "-" + d2(m) + "-" + d2(d) + "_"
				+ d2(h) + "-" + d2(min) + "_" + d2(sec));
		if (!snapshotDir.exists())
			snapshotDir.mkdir();
		
		File imageDir = new File(snapshotDir.getAbsolutePath() + File.separator + "RgbSide");
		if (!imageDir.exists())
			imageDir.mkdir();
		File outputfile = new File(imageDir.getAbsolutePath() + File.separator + "0_0." + fileExt);
		boolean res = new File(newImageFile).renameTo(outputfile);
		
		if (res) {
			FileOutputStream fos = new FileOutputStream(snapshotDir.getAbsolutePath() + File.separator + "info.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			writer.write("IdTag: " + plantID + "\r\n");
			// writer.write("Color: 0" + "\r\n");
			writer.write("Creator: " + getUserName() + "\r\n");
			writer.write("Comment: Camera Ad-hoc Phenotyping (c) 2013 C. Klukas" + "\r\n");
			writer.write("Measurement: " + measurementLabel + "\r\n");
			writer.write("Timestamp: " + m + "/" + d + "/" + y + " " + am_pm(h) + ":" + d2(min) + ":" + sec + " " + am_pm_s(h) + "\r\n");
			// writer.write("Weight before [g]: -1" + "\r\n");
			// writer.write("Weight after [g]: -1" + "\r\n");
			// writer.write("Water amount [ml]: -1" + "\r\n");
			writer.close();
			fos.close();
			fos = new FileOutputStream(imageDir.getAbsolutePath() + File.separator + "info.txt");
			writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			writer.write("Camera label: RgbSide" + "\r\n");
			writer.write("MM pro pixel X: 0" + "\r\n");
			writer.write("MM pro pixel Y: 0" + "\r\n");
			writer.close();
			fos.close();
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">INFO: " + plantID
					+ ": Barcode detected, snapshot data saved and input file moved to target directory.");
		} else
			System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">ERROR: " + plantID
					+ ": input file could not be moved to target directory: " + newImageFile);
		
	}
	
	private static int am_pm(int h) {
		if (h <= 12)
			return h;
		else
			return h - 12;
	}
	
	private static String am_pm_s(int h) {
		if (h >= 0 && h < 12)
			return "AM";
		else
			return "PM";
	}
	
	private static String d2(int v) {
		String r = "" + v;
		if (r.length() > 1)
			return r;
		else
			return "0" + r;
	}
	
	public static boolean windowsRunning() {
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && os.toUpperCase().contains("WINDOWS")) {
			return true;
		} else
			return false;
	}
	
	public static String getUserName() {
		String res;
		if (windowsRunning())
			res = System.getenv("USERNAME");
		else
			res = System.getenv("USER");
		return res;
	}
	
}
