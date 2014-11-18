package org.graffiti.plugins.ios.exporters.gml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.graffiti.graph.Graph;

/**
 * @author Christian Klukas
 */
public class GMLgzWriter
					extends GMLWriter {
	
	@Override
	public String[] getExtensions() {
		String[] exts = { ".gml.gz" };
		return exts;
	}
	
	@Override
	public String[] getFileTypeDescriptions() {
		return new String[] { "GML (compressed)" };
	}
	
	@Override
	public void write(OutputStream stream, Graph g)
						throws IOException {
		GZIPOutputStream os = null;
		try {
			os = new GZIPOutputStream(stream);
			super.write(os, g);
		} finally {
			if (os != null)
				os.close();
		}
	}
}
