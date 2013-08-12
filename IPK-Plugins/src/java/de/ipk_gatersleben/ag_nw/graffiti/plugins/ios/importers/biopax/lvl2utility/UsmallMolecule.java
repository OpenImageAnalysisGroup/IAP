package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import org.biopax.paxtools.model.level2.smallMolecule;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UsmallMolecule extends UtilitySuperClassToGraph
{

	public static void addAttributesToNode(GraphElement elem, smallMolecule i)
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.141"));
		setAvailability(elem, i.getAVAILABILITY());
		setComment(elem, i.getCOMMENT());
		setDataSource(elem, i.getDATA_SOURCE());
		setName(elem, i.getNAME());
		setShortName(elem, i.getSHORT_NAME());
		setRDFId(elem, i.getRDFId());
		setSynonyms(elem, i.getSYNONYMS());
		setXRef(elem, i.getXREF());

		setChemicalFormula(elem, i.getCHEMICAL_FORMULA());
		setStructure(elem, i.getSTRUCTURE());
		setMolecularWeight(elem, i.getMOLECULAR_WEIGHT());
	}
}
