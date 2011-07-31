package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class TabMetaCrop extends PathwayWebLinkTab {
	private static final long serialVersionUID = 1L;
	
	public TabMetaCrop() {
		super("MetaCrop", "http://vanted.ipk-gatersleben.de/addons/metacrop/gml/",
							"pathways", "pathway", "http://metacrop.ipk-gatersleben.de/", false);
	}
	
	public TabMetaCrop(String title, String url, String content, String contentSingle, String infoURL, boolean ommitEmptyGroupItems, String downloadButtonText) {
		super(title, url, content, contentSingle, infoURL, ommitEmptyGroupItems, downloadButtonText);
	}
	
	@Override
	public void addAnnotationsToGraphElements(Graph graph) {
		String refURL = getPathwayReference(graph.getName(true), "http://metacrop.ipk-gatersleben.de/");
		AttributeHelper.setReferenceURL(graph, refURL);
		for (Node n : graph.getNodes()) {
			NodeHelper node = new NodeHelper(n);
			// "http://pgrc-35.ipk-gatersleben.de/pls/htmldb_pgrc/f?p=112:10:::NO::P10_PATHWAY_OBJECT_ID:reaction_1373_407_104"
			String sbmlID = org.AttributeHelper.getSBMLid(node);
			if (sbmlID != null && sbmlID.trim().length() > 0)
				node.setURL("http://pgrc-35.ipk-gatersleben.de/pls/htmldb_pgrc/f?p=112:10:::NO::P10_PATHWAY_OBJECT_ID:" + sbmlID);
		}
	}
	
	private String getPathwayReference(String pathwayName, String returnIfUnknown) {
		String[] knownNamesAndIDs = new String[] {
							"Alanine, Valine, Leucine biosynthesis;12",
							"Arabinoxylan, Beta-Glucan, Cellulose biosynthesis;50",
							"Arginine biosynthesis;19",
							"Ascorbate biosynthesis;24",
							"Ascorbate-Glutathione cycle;25",
							"Asparagine biosynthesis;74",
							"C4-metabolism (NADP-ME subtype);115",
							"Calvin cycle;37",
							"Chlorogenic acid biosynthesis;28",
							"Fatty acid biosynthesis;35",
							"Fermentation;32",
							"Folate biosynthesis;112",
							"Fructan biosynthesis;30",
							"Glutathione biosynthesis;26",
							"Glycolysis, Gluconeogenesis;34",
							"Glyoxylate cycle;84",
							"GS-GOGAT cycle;63",
							"Histidine biosynthesis;16",
							"Isoleucine biosynthesis;13",
							"Lysine biosynthesis;18",
							"Methionine biosynthesis;88",
							"Methionine recycling;58",
							"NAD+/NADP+ de novo biosynthesis;48",
							"Oxidative phophorylation;113",
							"Pentose phosphate pathway;91",
							"Phenylalanine, Tyrosine, Tryptophan biosynthesis;10",
							"Proline biosynthesis;20",
							"Purine de novo biosynthesis;22",
							"Pyrimidine de novo biosynthesis;23",
							"Serine, Glycine, Cysteine biosynthesis;11",
							"Shikimate biosynthesis;9",
							"Starch metabolism (monocots);99",
							"Sucrose breakdown pathway (dicots);4",
							"Sucrose breakdown pathway (monocots);65",
							"Sugar metabolism;31",
							"TAG biosynthesis (simpl.);49",
							"TCA cycle;68",
							"Threonine biosynthesis;14"
		};
		String prefURL = "http://pgrc-35.ipk-gatersleben.de/pls/htmldb_pgrc/f?p=112:7:2455003602406520::NO::P7_PATHWAY_ID:";
		if (pathwayName.toUpperCase().endsWith(".GML"))
			pathwayName = pathwayName.substring(0, pathwayName.length() - ".GML".length());
		pathwayName = pathwayName.toUpperCase();
		for (String s : knownNamesAndIDs) {
			if (s.indexOf(";") < 0)
				continue;
			s = s.toUpperCase();
			String a, b;
			a = s.substring(0, s.lastIndexOf(";"));
			b = s.substring(s.lastIndexOf(";") + ";".length());
			if (a.length() <= 0 || b.length() <= 0)
				continue;
			if (pathwayName.endsWith(a)) {
				return prefURL + b;
			}
		}
		return returnIfUnknown;
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v == null || v instanceof GraphView;
	}
	
	@Override
	protected String[] getValidExtensions() {
		return new String[] { ".gml", ".graphml" };
	}
	
}
