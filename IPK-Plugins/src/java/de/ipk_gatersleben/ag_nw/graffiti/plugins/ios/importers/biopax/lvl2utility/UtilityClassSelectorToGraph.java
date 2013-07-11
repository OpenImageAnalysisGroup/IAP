package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import org.ErrorMsg;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level2.transport;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.graffiti.graph.GraphElement;

/**
 * this class and its static method transforms a biopax element to a node in a
 * graph
 * 
 * @author ricardo
 */
public class UtilityClassSelectorToGraph
{
	
	public static void chooseClassToPutAttributesToNodes(GraphElement elem, Object i)
	{
		if (i instanceof physicalEntityParticipant)
		{
			i = ((physicalEntityParticipant) i).getPHYSICAL_ENTITY();
		}
		try
		{
			if (i instanceof biochemicalReaction && !(i instanceof transportWithBiochemicalReaction))
			{
				UbioChemicalReaction.addAttributesToNode(elem, (biochemicalReaction) i);
			} else
				if (i instanceof complexAssembly)
				{
					UcomplexAssembly.addAttributesToNode(elem, (complexAssembly) i);
				} else
					if (i instanceof transport)
					{
						Utransport.addAttributesToNode(elem, (transport) i);
					} else
						if (i instanceof transportWithBiochemicalReaction)
						{
							UtransportWithBiochemicalReaction.addAttributesToNode(elem, (transportWithBiochemicalReaction) i);
						} else
							if (i instanceof catalysis)
							{
								Ucatalysis.addAttributesToNode(elem, (catalysis) i);
							} else
								if (i instanceof modulation)
								{
									Umodulation.addAttributesToNode(elem, (modulation) i);
								} else
									if (i instanceof complex)
									{
										Ucomplex.addAttributesToNode(elem, (complex) i);
									} else
										if (i instanceof dna)
										{
											Udna.addAttributesToNode(elem, (dna) i);
										} else
											if (i instanceof protein)
											{
												Uprotein.addAttributesToNode(elem, (protein) i);
											} else
												if (i instanceof rna)
												{
													Urna.addAttributesToNode(elem, (rna) i);
												} else
													if (i instanceof smallMolecule)
													{
														UsmallMolecule.addAttributesToNode(elem, (smallMolecule) i);
													} else
														if (i instanceof physicalEntity && !(i instanceof complex) && !(i instanceof dna) && !(i instanceof rna)
																&& !(i instanceof smallMolecule))
														{
															UphysicalEntity.addAttributesToNode(elem, (physicalEntity) i);
														} else
															if (i instanceof conversion && !(i instanceof biochemicalReaction) && !(i instanceof complexAssembly)
																	&& !(i instanceof transport) && !(i instanceof transportWithBiochemicalReaction))
															{
																Uconversion.addAttributesToNode(elem, (conversion) i);
															} else
																if (i instanceof control && !(i instanceof catalysis) && !(i instanceof modulation))
																{
																	Ucontrol.addAttributesToNode(elem, (control) i);
																} else
																	if (i instanceof pathway)
																	{
																		Upathway.addAttributesToNode(elem, (pathway) i);
																	} else
																		if (i instanceof physicalInteraction)
																		{
																			UphysicalInteraction.addAttributesToNode(elem, (physicalInteraction) i);
																		} else
																		{
																			System.out.println("Object wasn't found while reading OWL-File. It was a: " + i.getClass());
																		}
			
		} catch (Exception e)
		{
			ErrorMsg.addErrorMessage(e);
		}
	}
	
}