package org.graffiti.plugin.io.resources;

public abstract class AbstractResourceIOHandler implements ResourceIOHandler {
	
	public IOurl saveAs(IOurl source, String targetFilename) throws Exception {
		throw new UnsupportedOperationException("Save not implemented for IO handler " + this.getClass().getCanonicalName());
	}
	
	public IOurl save(IOurl source) throws Exception {
		return saveAs(source, source.getFileName());
	}
	
}
