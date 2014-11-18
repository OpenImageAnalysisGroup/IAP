package de.ipk.ag_ba.commands.experiment.scripts.helperClasses;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class FileSaver {
	public static void saveScripts(Object refForImageLocation, ArrayList<String> files, File targetFolder) throws Exception {
		ClassLoader cl = refForImageLocation.getClass().getClassLoader();
		String path = refForImageLocation.getClass().getPackage().getName().replace('.', '/');
		
		for (String file : files) {
			FileOutputStream out = new FileOutputStream(targetFolder.getAbsolutePath() + File.separator + file);
			
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
				}
			}
		}
	}
	
	public static void copy(String from, String to) throws IOException {
		File f1 = new File(from);
		File f2 = new File(to);
		
		InputStream in = new FileInputStream(f1);
		OutputStream out = new FileOutputStream(f2);
		
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}
