package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.biopax.paxtools.model.Model;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * this class and its static method transforms a attributes on a node to an
 * biopax element
 * 
 * @author ricardo
 */
public class UtilityClassSelectorFromGraph extends HelperClass
{
	
	public static void chooseClassToPutAttributesToModell(GraphElement elem, Graph g, Model model)
	{
		// if try fails it was a normal edge
		if (AttributeHelper.hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.126"))) //$NON-NLS-1$
		{
			String NodeType = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
			try
			{
				if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.166"))) //$NON-NLS-1$
				{
					UBioChemicalReaction.readAttributesFromNode(elem, g, model);
				} else
					if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.167"))) //$NON-NLS-1$
					{
						UComplexAssembly.readAttributesFromNode(elem, g, model);
					} else
						if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.168"))) //$NON-NLS-1$
						{
							UDegradation.readAttributesFromNode(elem, g, model);
						} else
							if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.169"))) //$NON-NLS-1$
							{
								UTransport.readAttributesFromNode(elem, g, model);
							} else
								if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.170"))) //$NON-NLS-1$
								{
									UTransportWithBiochemicalReaction.readAttributesFromNode(elem, g, model);
								} else
									if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.171"))) //$NON-NLS-1$
									{
										UGeneticInteraction.readAttributesFromNode(elem, g, model);
									} else
										if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.172"))) //$NON-NLS-1$
										{
											UMolecularInteraction.readAttributesFromNode(elem, g, model);
										} else
											if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.173"))) //$NON-NLS-1$
											{
												UTemplateReaction.readAttributesFromNode(elem, g, model);
											} else
												if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.174"))) //$NON-NLS-1$
												{
													UCatalysis.readAttributesFromNode(elem, g, model);
												} else
													if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.175"))) //$NON-NLS-1$
													{
														UTemplateReactionRegulation.readAttributesFromNode(elem, g, model);
													} else
														if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.176"))) //$NON-NLS-1$
														{
															UModulation.readAttributesFromNode(elem, g, model);
														} else
															if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.150"))) //$NON-NLS-1$
															{
																UComplex.readAttributesFromNode(elem, g, model);
															} else
																if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.151"))) //$NON-NLS-1$
																{
																	UDna.readAttributesFromNode(elem, g, model);
																} else
																	if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.152"))) //$NON-NLS-1$
																	{
																		UDnaRegion.readAttributesFromNode(elem, g, model);
																	} else
																		if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.153"))) //$NON-NLS-1$
																		{
																			UProtein.readAttributesFromNode(elem, g, model);
																		} else
																			if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.154"))) //$NON-NLS-1$
																			{
																				URna.readAttributesFromNode(elem, g, model);
																			} else
																				if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.155"))) //$NON-NLS-1$
																				{
																					URnaRegion.readAttributesFromNode(elem, g, model);
																				} else
																					if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.156"))) //$NON-NLS-1$
																					{
																						USmallMolecule.readAttributesFromNode(elem, g, model);
																					} else
																						if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.157"))) //$NON-NLS-1$
																						{
																							UGene.readAttributesFromNode(elem, g, model);
																						} else
																							if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.158"))) //$NON-NLS-1$
																							{
																								UPhysicalEntity.readAttributesFromNode(elem, g, model);
																							} else
																								if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.177"))) //$NON-NLS-1$
																								{
																									UConversion.readAttributesFromNode(elem, g, model);
																								} else
																									if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.178"))) //$NON-NLS-1$
																									{
																										UControl.readAttributesFromNode(elem, g, model);
																									} else
																										if (NodeType.equals(Messages.getString("UtilityClassSelectorFromGraph.179"))) //$NON-NLS-1$
																										{
																											UPathway.readAttributesFromNode(elem, g, model);
																										} else
																										{
																											System.out
																													.println("Object wasn't found while reading graph. It was a: " + NodeType); //$NON-NLS-1$
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
	
}
