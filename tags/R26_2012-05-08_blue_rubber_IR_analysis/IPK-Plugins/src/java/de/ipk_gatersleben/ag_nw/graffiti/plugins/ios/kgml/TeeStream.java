/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.io.PrintStream;

// All writes to this print stream are copied to two print streams
public class TeeStream extends PrintStream {
	PrintStream out;
	
	public TeeStream(PrintStream out1, PrintStream out2) {
		super(out1);
		this.out = out2;
	}
	
	@Override
	public void write(byte buf[], int off, int len) {
		try {
			super.write(buf, off, len);
			out.write(buf, off, len);
		} catch (Exception e) {
		}
	}
	
	@Override
	public void flush() {
		super.flush();
		out.flush();
	}
}