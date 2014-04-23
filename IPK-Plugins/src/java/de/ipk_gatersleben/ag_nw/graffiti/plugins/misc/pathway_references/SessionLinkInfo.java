package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.net.URISyntaxException;

import org.graffiti.graph.Graph;
import org.graffiti.session.EditorSession;

public class SessionLinkInfo implements Comparable<SessionLinkInfo> {
	int sessionHashCode;
	String fileName;
	
	Graph graph;
	
	public SessionLinkInfo(EditorSession otherSession) {
		sessionHashCode = otherSession.hashCode();
		fileName = otherSession.getGraph().getName(false);
		this.graph = otherSession.getGraph();
	}
	
	public SessionLinkInfo(Graph otherGraph) throws URISyntaxException {
		sessionHashCode = otherGraph.hashCode();
		fileName = otherGraph.getName(true);
		this.graph = otherGraph;
	}
	
	@Override
	public int hashCode() {
		return sessionHashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof SessionLinkInfo))
			return false;
		else
			return sessionHashCode == ((SessionLinkInfo) obj).sessionHashCode;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public int compareTo(SessionLinkInfo o) {
		return fileName.compareTo(o.fileName);
	}
	
	public Graph getGraph() {
		return graph;
	}
	
}
