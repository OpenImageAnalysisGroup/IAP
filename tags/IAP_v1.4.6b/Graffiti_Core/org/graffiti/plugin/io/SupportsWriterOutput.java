package org.graffiti.plugin.io;

import java.io.IOException;
import java.io.Writer;

import org.graffiti.graph.Graph;

public interface SupportsWriterOutput {
	public void write(Writer output, Graph resultGraph) throws IOException;
}
