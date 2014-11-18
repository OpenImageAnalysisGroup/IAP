package de.ipk.ag_ba.commands.experiment.process.report;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

/**
 * @author Christian Klukas
 */
public class StringBuilderOrOutput {
	
	LinkedList<String> sb = new LinkedList<String>();
	
	OutputStream out;
	
	private File targetFile;
	
	private ThreadSafeOptions written;
	long sbContent = 0;
	
	public void setOutputFile(File targetFile) throws FileNotFoundException {
		this.targetFile = targetFile;
		out = new BufferedOutputStream(new FileOutputStream(targetFile));
	}
	
	public void appendLine(String string, ThreadSafeOptions written) throws IOException {
		this.written = written;
		if (out == null) {
			sb.add(string);
			sbContent += string.length();
		} else {
			char[] ca = string.toCharArray();
			for (char c : ca) {
				out.write(c);
			}
			written.addLong(ca.length);
		}
	}
	
	public boolean close() throws IOException {
		if (out != null) {
			out.close();
			return true;
		} else
			return false;
	}
	
	public long length() {
		if (out == null)
			return sbContent;
		else
			return written.getLong();
	}
	
	public File getTargetFile() {
		return targetFile;
	}
	
	public boolean hasFileOutput() {
		return targetFile != null;
	}
}
