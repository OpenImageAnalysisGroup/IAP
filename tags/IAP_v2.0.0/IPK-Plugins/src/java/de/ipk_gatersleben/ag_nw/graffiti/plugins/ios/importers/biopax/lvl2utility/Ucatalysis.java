package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import org.biopax.paxtools.model.level2.catalysis;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class Ucatalysis extends UtilitySuperClassToGraph
{

	public static void addAttributesToNode(GraphElement elem, catalysis i)
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.129")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAVAILABILITY());
		setComment(elem, i.getCOMMENT());
		setDataSource(elem, i.getDATA_SOURCE());
		setEvidence(elem, i.getEVIDENCE());
		setInteractionType(elem, i.getINTERACTION_TYPE());
		setName(elem, i.getNAME());
		setShortName(elem, i.getSHORT_NAME());
		setRDFId(elem, i.getRDFId());
		setSynonyms(elem, i.getSYNONYMS());
		setXRef(elem, i.getXREF());

		setCatalysisDirection(elem, i.getDIRECTION());
		setCofactor(elem, i.getCOFACTOR());
		setControlType(elem, i.getCONTROL_TYPE());
	}
}
