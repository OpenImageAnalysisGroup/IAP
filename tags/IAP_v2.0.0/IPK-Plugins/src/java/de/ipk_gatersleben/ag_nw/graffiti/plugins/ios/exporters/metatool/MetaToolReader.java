package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.metatool;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;

public class MetaToolReader extends AbstractInputSerializer {
	
	@Override
	public void read(InputStream in, Graph g) throws IOException {
		//
		
	}
	
	@Override
	public void read(Reader reader, Graph newGraph) throws Exception {
		//
		
	}
	
	@Override
	public String[] getExtensions() {
		//
		return null;
	}
	
	@Override
	public String[] getFileTypeDescriptions() {
		//
		return null;
	}
	
}
