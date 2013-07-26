package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UtransportWithBiochemicalReaction extends UtilitySuperClassToGraph
{

	public static void addAttributesToNode(GraphElement elem, transportWithBiochemicalReaction i)
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.143"));
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

		setSpontaneous(elem, i.getSPONTANEOUS());
		setDeltaG(elem, i.getDELTA_G());
		setDeltaH(elem, i.getDELTA_H());
		setDeltaS(elem, i.getDELTA_S());
		setECNumber(elem, i.getEC_NUMBER());
		setKPrime(elem, i.getKEQ());
		setSpontaneous(elem, i.getSPONTANEOUS());

	}

}
