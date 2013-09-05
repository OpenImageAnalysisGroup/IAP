package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import org.biopax.paxtools.model.level2.pathway;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class Upathway extends UtilitySuperClassToGraph
{

	public static void addAttributesToNode(GraphElement elem, pathway i)
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.136")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAVAILABILITY());
		setComment(elem, i.getCOMMENT());
		setDataSource(elem, i.getDATA_SOURCE());
		setName(elem, i.getNAME());
		setShortName(elem, i.getSHORT_NAME());
		setRDFId(elem, i.getRDFId());
		setSynonyms(elem, i.getSYNONYMS());
		setXRef(elem, i.getXREF());

		setOrganism(elem, i.getORGANISM());
	}

}
