package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author Christian Klukas
 */
public class Appender {
	
	private final StringBuilder stringBuilder;
	private final OutputStream outputStream;
	
	public Appender(StringBuilder sb) {
		this.stringBuilder = sb;
		this.outputStream = null;
	}
	
	public Appender(OutputStream outputStream) {
		this.stringBuilder = null;
		this.outputStream = outputStream;
	}
	
	public void append(String string) throws IOException {
		if (stringBuilder != null)
			stringBuilder.append(string);
		if (outputStream != null)
			outputStream.write(string.getBytes(Charset.forName("UTF-8")));
	}
	
}
