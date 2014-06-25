package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.ErrorMsg;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.paxtools.model.level3.TransportWithBiochemicalReaction;
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
		try
		{
			if (i instanceof BiochemicalReaction && !(i instanceof TransportWithBiochemicalReaction))
			{
				UBioChemicalReaction.addAttributesToNode(elem, (BiochemicalReaction) i);
			} else
				if (i instanceof ComplexAssembly)
				{
					UComplexAssembly.addAttributesToNode(elem, (ComplexAssembly) i);
				} else
					if (i instanceof Degradation)
					{
						UDegradation.addAttributesToNode(elem, (Degradation) i);
					} else
						if (i instanceof Transport)
						{
							UTransport.addAttributesToNode(elem, (Transport) i);
						} else
							if (i instanceof TransportWithBiochemicalReaction)
							{
								UTransportWithBiochemicalReaction.addAttributesToNode(elem, (TransportWithBiochemicalReaction) i);
							} else
								if (i instanceof GeneticInteraction)
								{
									UGeneticInteraction.addAttributesToNode(elem, (GeneticInteraction) i);
								} else
									if (i instanceof MolecularInteraction)
									{
										UMolecularInteraction.addAttributesToNode(elem, (MolecularInteraction) i);
									} else
										if (i instanceof TemplateReaction)
										{
											UTemplateReaction.addAttributesToNode(elem, (TemplateReaction) i);
										} else
											if (i instanceof Catalysis)
											{
												UCatalysis.addAttributesToNode(elem, (Catalysis) i);
											} else
												if (i instanceof TemplateReactionRegulation)
												{
													UTemplateReactionRegulation.addAttributesToNode(elem, (TemplateReactionRegulation) i);
												} else
													if (i instanceof Modulation)
													{
														UModulation.addAttributesToNode(elem, (Modulation) i);
													} else
														if (i instanceof Complex)
														{
															UComplex.addAttributesToNode(elem, (Complex) i);
														} else
															if (i instanceof Dna)
															{
																UDna.addAttributesToNode(elem, (Dna) i);
															} else
																if (i instanceof DnaRegion)
																{
																	UDnaRegion.addAttributesToNode(elem, (DnaRegion) i);
																} else
																	if (i instanceof Protein)
																	{
																		UProtein.addAttributesToNode(elem, (Protein) i);
																	} else
																		if (i instanceof Rna)
																		{
																			URna.addAttributesToNode(elem, (Rna) i);
																		} else
																			if (i instanceof RnaRegion)
																			{
																				URnaRegion.addAttributesToNode(elem, (RnaRegion) i);
																			} else
																				if (i instanceof SmallMolecule)
																				{
																					USmallMolecule.addAttributesToNode(elem, (SmallMolecule) i);
																				} else
																					if (i instanceof Gene)
																					{
																						UGene.addAttributesToNode(elem, (Gene) i);
																					} else
																						if (i instanceof PhysicalEntity && !(i instanceof Complex) && !(i instanceof Dna)
																								&& !(i instanceof DnaRegion) && !(i instanceof Rna) && !(i instanceof RnaRegion)
																								&& !(i instanceof SmallMolecule) && !(i instanceof Gene))
																						{
																							UPhysicalEntity.addAttributesToNode(elem, (PhysicalEntity) i);
																						} else
																							if (i instanceof Conversion && !(i instanceof BiochemicalReaction)
																									&& !(i instanceof ComplexAssembly) && !(i instanceof Transport)
																									&& !(i instanceof Degradation) && !(i instanceof TransportWithBiochemicalReaction))
																							{
																								UConversion.addAttributesToNode(elem, (Conversion) i);
																							} else
																								if (i instanceof Control && !(i instanceof Catalysis) && !(i instanceof Modulation)
																										&& !(i instanceof TemplateReactionRegulation))
																								{
																									UControl.addAttributesToNode(elem, (Control) i);
																								} else
																									if (i instanceof Pathway)
																									{
																										UPathway.addAttributesToNode(elem, (Pathway) i);
																									} else
																									{
																										System.out.println("Object wasn't found while reading OWL-File. It was a: "
																												+ i.getClass());
																									}
			
		} catch (IllegalArgumentException e)
		{
			ErrorMsg.addErrorMessage(e);
		} catch (IllegalAccessException e)
		{
			ErrorMsg.addErrorMessage(e);
		} catch (InvocationTargetException e)
		{
			ErrorMsg.addErrorMessage(e);
		}
	}
	
}
