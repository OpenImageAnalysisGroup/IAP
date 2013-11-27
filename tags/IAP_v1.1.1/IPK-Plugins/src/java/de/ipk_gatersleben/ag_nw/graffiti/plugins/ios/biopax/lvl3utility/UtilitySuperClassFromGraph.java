package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.util.ArrayList;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.CatalysisDirectionType;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.DeltaG;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.FragmentFeature;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.KPrime;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * this class's functions gets all available attributes and helps to write them
 * into an owl model
 * 
 * @author ricardo
 */
public class UtilitySuperClassFromGraph extends HelperClass
{
	protected static void getAvailability(GraphElement elem, Entity interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.0"), elem))
		{
			interaction.addAvailability(a.getValue().toString());
		}
	}
	
	protected static void getCatalysisDirection(GraphElement elem, Catalysis interaction)
	{
		String value = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.1"));
		if (value.length() > 0)
			try
			{
				interaction.setCatalysisDirection(CatalysisDirectionType.valueOf(value));
			} catch (IllegalArgumentException e)
			{
				ErrorMsg.addErrorMessage(e);
			}
	}
	
	protected static void getCellularLocation(GraphElement elem, PhysicalEntity interaction, Model model)
	{
		// Left-out: Comments
		
		// RDFId of CellularLocationVocabulary
		String RDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.2"));
		if (RDFId.length() > 0)
		{
			CellularLocationVocabulary CLV;
			
			if (!model.containsID(RDFId))
			{
				// CellularLocationVocabulary wasn't already read
				CLV = model.addNew(CellularLocationVocabulary.class, RDFId);
				
				// 1.Find out terms
				for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.3"), elem))
				{
					CLV.addTerm(a.getValue().toString());
				}
				
				// 2. Find out XRefs
				for (int i = 1; i <= getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.4"), elem).size(); i++)
				{
					// first get XRef's RDFId
					Attribute XrefRDFIdAttribute = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.4"), elem, i);
					String xrefRDFId = XrefRDFIdAttribute.getValue().toString();
					
					Xref xref = model.addNew(UnificationXref.class, xrefRDFId);
					
					// get Comments
					for (Attribute aa : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.5"), elem, i))
					{
						xref.addComment(aa.getValue().toString());
					}
					// DB
					Attribute XrefDB = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.6"), elem, i);
					if (XrefDB != null)
						xref.setDb(XrefDB.getValue().toString());
					// DBVersion
					Attribute XrefDBVersion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.7"), elem, i);
					if (XrefDBVersion != null)
						xref.setDbVersion(XrefDBVersion.getValue().toString());
					// Id
					Attribute XrefId = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.8"), elem, i);
					if (XrefId != null)
						xref.setId(XrefId.getValue().toString());
					// IdVersion
					Attribute XrefIdVersion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.9"), elem, i);
					if (XrefIdVersion != null)
						xref.setIdVersion(XrefIdVersion.getValue().toString());
					CLV.addXref(xref);
				}
			} else
			{
				// CellularLocationVocabulary was already read
				CLV = (CellularLocationVocabulary) model.getByID(RDFId);
			}
			interaction.setCellularLocation(CLV);
		}
	}
	
	protected static void getCofactor(GraphElement elem, Catalysis interaction, Model model)
	{
		/*
		 * left-out attributes: - cellularLocation, feature,
		 * memberPhysicalEntity, notFeature, availability, comment, dataSource,
		 * evidence, name, xref
		 */
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.11"), elem))
		{
			String cofactorRDFId = a.getValue().toString();
			
			if (!model.containsID(cofactorRDFId))
			{
				/*
				 * not already inserted into model iterate over the nodes of the
				 * Graph to find out the kind of node the component was
				 */
				List<Node> nodes = elem.getGraph().getNodes();
				Node cofactor = null;
				
				for (int i = 0; i < nodes.size(); i++)
				{
					Node iNode = nodes.get(i);
					Attribute attr = iNode.getAttribute(Messages.getString("UtilitySuperClassToGraph.82"));
					String iNodeRDFId = attr.getValue().toString();
					
					if (iNodeRDFId.equals(cofactorRDFId))
					{
						// cofactor node found
						cofactor = nodes.get(i);
						UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(cofactor, cofactor.getGraph(), model);
					}
				}
			}
			PhysicalEntity p = (PhysicalEntity) model.getByID(cofactorRDFId);
			interaction.addCofactor(p);
		}
	}
	
	protected static void getComment(GraphElement elem, Entity interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.12"), elem))
		{
			interaction.addComment(a.getValue().toString());
		}
	}
	
	protected static void getComponent(GraphElement elem, Complex interaction, Model model)
	{
		/*
		 * left-out attributes: - cellularLocation, feature,
		 * memberPhysicalEntity, notFeature, availability, comment, dataSource,
		 * evidence, name, xref
		 */
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.14"), elem))
		{
			String componentRDFId = a.getValue().toString();
			if (!model.containsID(componentRDFId))
			{
				// not already inserted
				/*
				 * iterate over the nodes of the Graph to find out the kind of
				 * node the component was
				 */
				List<Node> nodes = elem.getGraph().getNodes();
				Node component = null;
				boolean nodeFoundInGraph = false;
				for (int i = 0; i < nodes.size(); i++)
				{
					Node iNode = nodes.get(i);
					Attribute attr = iNode.getAttribute(Messages.getString("UtilitySuperClassToGraph.82"));
					String iNodeRDFId = attr.getValue().toString();
					
					if (iNodeRDFId.equals(componentRDFId))
					{
						// ComponentNodeFound
						component = nodes.get(i);
						UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(component, component.getGraph(), model);
						nodeFoundInGraph = true;
					}
				}
				if (!nodeFoundInGraph)
				{
					// Node wasn't found in Graph, because components of a
					// complex don't get imported
					// Node has to be reconstructed in the model only by ID
					model.addNew(PhysicalEntity.class, componentRDFId);
				}
			}
			PhysicalEntity p = (PhysicalEntity) model.getByID(componentRDFId);
			interaction.addComponent(p);
		}
	}
	
	protected static void getComponentStoichiometry(GraphElement elem, Complex interaction, Model model)
	{
		/*
		 * search for a node with the RDFId of that is written in the
		 * ComponentStoichiometry tab and at it this as Stoichiometry to the
		 * model
		 */
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.16"), elem))
		{
			String ComponentStoichiometryRDFId = a.getValue().toString();
			if (!model.containsID(ComponentStoichiometryRDFId))
			{
				// not already inserted
				/*
				 * iterate over the nodes of the Graph to find out the kind of
				 * node the component was
				 */
				List<Node> nodes = elem.getGraph().getNodes();
				Node component = null;
				for (int i = 0; i < nodes.size(); i++)
				{
					Node iNode = nodes.get(i);
					Attribute attr = iNode.getAttribute(".BioPax.RDFId");
					String iNodeRDFId = attr.getValue().toString();
					
					if (iNodeRDFId.equals(ComponentStoichiometryRDFId))
					{
						// ComponentNode Found
						component = nodes.get(i);
						UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(component, component.getGraph(), model);
					}
				}
			}
		}
	}
	
	protected static void getControlType(GraphElement elem, Control interaction)
	{
		if (interaction instanceof Catalysis)
		{
			String ControlTypeName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.19"));
			if (ControlTypeName.length() > 0)
				((Catalysis) interaction).setControlType(ControlType.valueOf(ControlTypeName));
		} else
			if (interaction instanceof Modulation)
			{
				String ControlTypeName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.19"));
				if (ControlTypeName.length() > 0)
					interaction.setControlType(ControlType.valueOf(ControlTypeName));
			}
	}
	
	protected static void getConversionDirection(GraphElement elem, Conversion interaction)
	{
		String ConversionDirectionName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.20"));
		if (ConversionDirectionName.length() > 0)
			interaction.setConversionDirection(ConversionDirectionType.valueOf(ConversionDirectionName));
	}
	
	protected static void getDataSource(GraphElement elem, Entity interaction, Model model)
	{
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.23"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			Provenance p;
			
			Attribute a = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.23"), elem, i);
			String RDFId = a.getValue().toString();
			if (!model.containsID(RDFId))
			{
				p = model.addNew(Provenance.class, RDFId);
				
				Attribute aa = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.22"), elem, i);
				if (aa != null)
				{
					String displayName = aa.getValue().toString();
					p.setDisplayName(displayName);
				}
				
				/*
				 * find all names belonging to that provenance
				 */
				ArrayList<Attribute> secondset = getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.21"), elem, i);
				for (Attribute aaa : secondset)
				{
					p.addName(aaa.getValue().toString());
				}
			} else
			{
				p = (Provenance) model.getByID(RDFId);
			}
			interaction.addDataSource(p);
		}
	}
	
	protected static void getDeltaG(GraphElement elem, BiochemicalReaction interaction, Model model)
	{
		/*
		 * left-out attributes: - Comments
		 */
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.29"), elem);
		
		for (int i = 1; i <= set.size(); i++)
		{
			Attribute getDeltaGPrime0 = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.24"), elem, i);
			Attribute IonicStrength = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.25"), elem, i);
			Attribute getPh = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.26"), elem, i);
			Attribute getPMg = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.27"), elem, i);
			Attribute getTemperature = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.28"), elem, i);
			Attribute getRDFId = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.29"), elem, i);
			
			DeltaG delta = model.addNew(DeltaG.class, getRDFId.getValue().toString());
			if (getDeltaGPrime0 != null)
				delta.setDeltaGPrime0(Float.valueOf(getDeltaGPrime0.getValue().toString()));
			if (IonicStrength != null)
				delta.setIonicStrength(Float.valueOf(IonicStrength.getValue().toString()));
			if (getPh != null)
				delta.setPh(Float.valueOf(getPh.getValue().toString()));
			if (getPMg != null)
				delta.setPMg(Float.valueOf(getPMg.getValue().toString()));
			if (getTemperature != null)
				delta.setTemperature(Float.valueOf(getTemperature.getValue().toString()));
			
			interaction.addDeltaG(delta);
		}
	}
	
	protected static void getDeltaH(GraphElement elem, BiochemicalReaction interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.30"), elem))
		{
			interaction.addDeltaH(Float.valueOf(a.getValue().toString()));
		}
	}
	
	protected static void getDeltaS(GraphElement elem, BiochemicalReaction interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.31"), elem))
		{
			interaction.addDeltaS(Float.valueOf(a.getValue().toString()));
		}
	}
	
	protected static void getDisplayName(GraphElement elem, Entity interaction)
	{
		
		String DisplayName = "";
		DisplayName = AttributeHelper.getLabel(elem, DisplayName);
		interaction.setDisplayName(DisplayName);
	}
	
	protected static void getECNumber(GraphElement elem, BiochemicalReaction interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.32"), elem))
		{
			interaction.addECNumber(a.getValue().toString());
		}
	}
	
	protected static void getEntityReference(GraphElement elem, SimplePhysicalEntity interaction, Model model)
	{
		/*
		 * left-out attributes: - entityFeature, entityReferenceType, evidence,
		 * memberEntityReference, name ,xref
		 */
		String EntityReferenceRDFId = "";
		String EntityReferenceName = "";
		@SuppressWarnings("rawtypes")
		Class t = null;
		if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.33")))
		{
			EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.33"));
			if (EntityReferenceRDFId.length() > 0)
			{
				t = DnaReference.class;
				EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.34"));
			}
		} else
			if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.35")))
			{
				EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.35"));
				if (EntityReferenceRDFId.length() > 0)
				{
					t = DnaRegionReference.class;
					EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.36"));
				}
			} else
				if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.37")))
				{
					
					EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.37"));
					if (EntityReferenceRDFId.length() > 0)
					{
						t = RnaReference.class;
						EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.38"));
					}
				} else
					if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.39")))
					{
						EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.39"));
						if (EntityReferenceRDFId.length() > 0)
						{
							t = RnaRegionReference.class;
							EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.40"));
						}
					} else
						if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.41")))
						
						{
							EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.41"));
							if (EntityReferenceRDFId.length() > 0)
							{
								t = SmallMoleculeReference.class;
								EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.42"));
							}
						} else
							if (hasAttribute(elem, Messages.getString("UtilitySuperClassToGraph.43")))
							{
								EntityReferenceRDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.43"));
								if (EntityReferenceRDFId.length() > 0)
								{
									t = ProteinReference.class;
									EntityReferenceName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.44"));
								}
							}
		
		if (!model.containsID(EntityReferenceRDFId) && EntityReferenceRDFId.length() > 0)
		{
			@SuppressWarnings("unchecked")
			// EntityReference eRef = model.addNew(t, EntityReferenceRDFId);
			// explicit cast seems to be necessary for Java versions below 1.6.31
			EntityReference eRef = (EntityReference) model.addNew(t, EntityReferenceRDFId);
			if (EntityReferenceName.length() > 0)
				eRef.setDisplayName(EntityReferenceName);
			
			interaction.setEntityReference(eRef);
		} else
		{
			EntityReference eRef = (EntityReference) model.getByID(EntityReferenceRDFId);
			interaction.setEntityReference(eRef);
		}
	}
	
	protected static void getEvidence(GraphElement elem, Entity interaction, Model model)
	{
		/*
		 * left-out attributes: - confidence, evidenceCode, experimentalForm,
		 * xref, comment
		 */
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.46"), elem);
		for (Attribute a : set)
		{
			Evidence e = model.addNew(Evidence.class, a.getValue().toString());
			interaction.addEvidence(e);
		}
	}
	
	protected static void getFeature(GraphElement elem, PhysicalEntity interaction, Model model)
	{
		ArrayList<Attribute> set;
		/*
		 * left-out attributes: - evidence, featureLocation,
		 * featureLocationType, memberFeature, comment
		 */
		// normal EntityFeature
		// #######################################################################
		set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.47"), elem);
		for (Attribute a : set)
		{
			String RDFId = a.getValue().toString();
			EntityFeature feature;
			if (!model.containsID(RDFId))
			{
				feature = model.addNew(EntityFeature.class, RDFId);
			} else
			{
				feature = (EntityFeature) model.getByID(RDFId);
			}
			interaction.addFeature(feature);
		}
		// Fragment Feature
		// #######################################################################
		set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.48"), elem);
		for (Attribute a : set)
		{
			String RDFId = a.getValue().toString();
			FragmentFeature feature;
			if (!model.containsID(RDFId))
			{
				feature = model.addNew(FragmentFeature.class, RDFId);
			} else
			{
				feature = (FragmentFeature) model.getByID(RDFId);
			}
			interaction.addFeature(feature);
		}
		// Modification Feature
		
		set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.49"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			Attribute RDFIdAttribute = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.49"), elem, i);
			if (RDFIdAttribute != null)
			{
				String RDFId = RDFIdAttribute.getValue().toString();
				ModificationFeature MF;
				if (!model.containsID(RDFId))
				{
					MF = model.addNew(ModificationFeature.class, RDFId);
					
					// Modification Type
					Attribute aa = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.50"), elem, i);
					String ModificationTypeRDFId = aa.getValue().toString();
					
					// SequenceModificationVocabulary
					SequenceModificationVocabulary SMV;
					if (!model.containsID(ModificationTypeRDFId))
					{
						SMV = model.addNew(SequenceModificationVocabulary.class, ModificationTypeRDFId);
						
						for (Attribute term : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.51"), elem, i))
						{
							SMV.addTerm(term.getValue().toString());
						}
						for (Attribute comment : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.52"), elem, i))
						{
							SMV.addComment(comment.getValue().toString());
						}
					} else
					{
						SMV = (SequenceModificationVocabulary) model.getByID(ModificationTypeRDFId);
					}
					MF.setModificationType(SMV);
					
					// FeatureLocation
					aa = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.53"), elem, i);
					if (aa != null)
					{
						String FeatureLocationRDFId = aa.getValue().toString();
						
						SequenceLocation SL;
						if (!model.containsID(FeatureLocationRDFId))
						{
							SL = model.addNew(SequenceLocation.class, FeatureLocationRDFId);
							for (Attribute comment : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.54"), elem, i))
							{
								SL.addComment(comment.getValue().toString());
							}
							
						} else
						{
							SL = (SequenceLocation) model.getByID(FeatureLocationRDFId);
						}
						MF.setFeatureLocation(SL);
					}
					
					// FeatureLocationType
					aa = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.55"), elem, i);
					if (aa != null)
					{
						String FeatureLocationTypeRDFId = aa.getValue().toString();
						
						SequenceRegionVocabulary SRV;
						if (!model.containsID(FeatureLocationTypeRDFId))
						{
							SRV = model.addNew(SequenceRegionVocabulary.class, FeatureLocationTypeRDFId);
							
							for (Attribute term : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.56"), elem, i))
							{
								SRV.addTerm(term.getValue().toString());
							}
							for (Attribute comment : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.57"), elem, i))
							{
								SRV.addComment(comment.getValue().toString());
							}
						} else
						{
							SRV = (SequenceRegionVocabulary) model.getByID(FeatureLocationTypeRDFId);
						}
						MF.setFeatureLocationType(SRV);
					}
				} else
				{
					MF = (ModificationFeature) model.getByID(RDFId);
				}
				
				interaction.addFeature(MF);
			}
		}
		
		for (Attribute RDFIdAttr : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.58"), elem))
		{
			String RDFId = RDFIdAttr.getValue().toString();
			EntityFeature E = model.addNew(EntityFeature.class, RDFId);
			interaction.addFeature(E);
		}
		
	}
	
	protected static void getInteractionScore(GraphElement elem, GeneticInteraction interaction, Model model)
	{
		String RDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.61"));
		if (RDFId.length() > 0)
		{
			Score score = model.addNew(Score.class, RDFId);
			String value = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.60"));
			if (value.length() > 0)
				score.setValue(value);
			
			String sourcerdfid = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.59"));
			if (sourcerdfid.length() > 0)
			{
				Provenance provenance = model.addNew(Provenance.class, sourcerdfid);
				score.setScoreSource(provenance);
			}
			interaction.setInteractionScore(score);
		}
	}
	
	protected static void getInteractionType(GraphElement elem, Interaction interaction, Model model)
	{
		/*
		 * left-out attributes: - xref, comment
		 */
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.63"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			Attribute RDFIdAttr = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.63"), elem, i);
			String RDFId = RDFIdAttr.getValue().toString();
			InteractionVocabulary iV;
			if (!model.containsID(RDFId))
			{
				iV = model.addNew(InteractionVocabulary.class, RDFId);
				for (Attribute term : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.62"), elem, i))
				{
					iV.addTerm(term.getValue().toString());
				}
			} else
			{
				iV = (InteractionVocabulary) model.getByID(RDFId);
			}
			interaction.addInteractionType(iV);
		}
	}
	
	protected static void getKPrime(GraphElement elem, BiochemicalReaction interaction, Model model)
	{
		
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.70"), elem);
		
		for (int i = 1; i <= set.size(); i++)
		{
			Attribute KPrime = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.66"), elem, i);
			Attribute IonicStrength = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.65"), elem, i);
			Attribute getPh = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.67"), elem, i);
			Attribute getPMg = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.68"), elem, i);
			Attribute getTemperature = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.69"), elem, i);
			Attribute getRDFId = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.70"), elem, i);
			
			KPrime kPrime = model.addNew(KPrime.class, getRDFId.getValue().toString());
			if (KPrime != null)
				kPrime.setKPrime(Float.valueOf(KPrime.getValue().toString()));
			if (IonicStrength != null)
				kPrime.setIonicStrength(Float.valueOf(IonicStrength.getValue().toString()));
			if (getPh != null)
				kPrime.setPh(Float.valueOf(getPh.getValue().toString()));
			if (getPMg != null)
				kPrime.setPMg(Float.valueOf(getPMg.getValue().toString()));
			if (getTemperature != null)
				kPrime.setTemperature(Float.valueOf(getTemperature.getValue().toString()));
			
			for (Attribute comment : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.64"), elem, i))
			{
				kPrime.addComment(comment.getValue().toString());
			}
			interaction.addKEQ(kPrime);
		}
	}
	
	protected static void getMemberPhysicalEntity(GraphElement elem, PhysicalEntity interaction, Model model)
	{
		/*
		 * left-out attributes: - cellularLocation, feature,
		 * memberPhysicalEntity, notFeature, availability, comment, dataSource,
		 * evidence, name, xref
		 */
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.72"), elem);
		// make sure the physical entity is in the graph
		for (int i = 1; i <= set.size(); i++)
		{
			String MemberPhysicalEntityRDFId = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.72"), elem, i).getValue()
					.toString();
			if (!model.containsID(MemberPhysicalEntityRDFId))
			{
				// not already inserted
				/*
				 * iterate over the nodes of the Graph to find out the kind of
				 * node the component was
				 */
				List<Node> nodes = elem.getGraph().getNodes();
				Node component = null;
				boolean nodeFoundInGraph = false;
				for (int j = 0; j < nodes.size(); j++)
				{
					Node iNode = nodes.get(j);
					Attribute attr = iNode.getAttribute(Messages.getString("UtilitySuperClassToGraph.82"));
					String iNodeRDFId = attr.getValue().toString();
					
					if (iNodeRDFId.equals(MemberPhysicalEntityRDFId))
					{
						// ComponentNodeFound
						component = nodes.get(j);
						UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(component, component.getGraph(), model);
						nodeFoundInGraph = true;
					}
				}
				if (!nodeFoundInGraph)
				{
					// Node wasn't found in Graph
					// Node has to be reconstructed in the model only by ID and
					// name
					
					PhysicalEntity p = model.addNew(PhysicalEntity.class, MemberPhysicalEntityRDFId);
					String MemberPhysicalEntityName = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.71"), elem, i)
							.getValue().toString();
					
					if (MemberPhysicalEntityName.length() > 0)
						p.setDisplayName(MemberPhysicalEntityName);
				}
			}
		}
		
		// now add it
		for (Attribute rdfid : set)
		{
			PhysicalEntity p = (PhysicalEntity) model.getByID(rdfid.getValue().toString());
			interaction.addMemberPhysicalEntity(p);
		}
	}
	
	protected static void getName(GraphElement elem, Entity interaction)
	{
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.73"), elem))
		{
			interaction.addName(a.getValue().toString());
		}
	}
	
	protected static void getNotFeature(GraphElement elem, PhysicalEntity interaction, Model model)
	{
		/*
		 * left-out attributes: - evidence, featureLocation,
		 * featureLocationType, memberFeature, comment
		 */
		for (Attribute a : getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.74"), elem))
		{
			String RDFId = a.getValue().toString();
			EntityFeature feature;
			if (!model.containsID(RDFId))
			{
				feature = model.addNew(EntityFeature.class, RDFId);
			} else
			{
				feature = (EntityFeature) model.getByID(RDFId);
			}
			interaction.addNotFeature(feature);
		}
	}
	
	protected static void getOrganism(GraphElement elem, Gene interaction, Model model)
	{
		/*
		 * left-out attributes: - name, xref, comment
		 */
		String RDFId = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.76"));
		if (!model.containsID(RDFId))
		{
			BioSource bioSource = model.addNew(BioSource.class, RDFId);
			String Name = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.75"));
			String CellType = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.77"));
			String Tissue = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.78"));
			if (Name.length() > 0)
			{
				bioSource.setDisplayName(Name);
			}
			if (CellType.length() > 0)
			{
				CellVocabulary cellVocabulary = model.addNew(CellVocabulary.class, CellType);
				bioSource.setCellType(cellVocabulary);
			}
			if (Tissue.length() > 0)
			{
				TissueVocabulary tissueVocabulary = model.addNew(TissueVocabulary.class, Tissue);
				bioSource.setTissue(tissueVocabulary);
			}
			interaction.setOrganism(bioSource);
		} else
		{
			BioSource bioSource = (BioSource) model.getByID(RDFId);
			interaction.setOrganism(bioSource);
		}
	}
	
	protected static void getPhenotype(GraphElement elem, GeneticInteraction interaction, Model model)
	{
		String pheno = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.80"));
		if (pheno.length() > 0)
		{
			String pato = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.79"));
			PhenotypeVocabulary phenoVocabulary = model.addNew(PhenotypeVocabulary.class, pheno);
			if (pato.length() > 0)
				phenoVocabulary.setPatoData(pato);
			
			interaction.setPhenotype(phenoVocabulary);
		}
	}
	
	protected static void getSpontaneous(GraphElement elem, Conversion interaction)
	{
		String bool = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.83"));
		if (bool.length() > 0)
			interaction.setSpontaneous(Boolean.valueOf(bool));
	}
	
	protected static void getStandardName(GraphElement elem, Entity interaction)
	{
		String standardName = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.84"));
		if (standardName.length() > 0)
			interaction.setStandardName(standardName);
	}
	
	protected static void getXRef(GraphElement elem, Entity interaction, Model model)
	{
		// Unification Xref
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.91"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			String RDFId = set.get(i - 1).getValue().toString();
			UnificationXref x;
			if (!model.containsID(RDFId))
			{
				x = model.addNew(UnificationXref.class, RDFId);
				
				Attribute db = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.87"), elem, i);
				if (db != null)
				{
					x.setDb(db.getValue().toString());
				}
				Attribute dbversion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.88"), elem, i);
				if (dbversion != null)
				{
					x.setDbVersion(dbversion.getValue().toString());
				}
				Attribute id = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.89"), elem, i);
				if (id != null)
				{
					x.setId(id.getValue().toString());
				}
				Attribute idversion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.90"), elem, i);
				if (idversion != null)
				{
					x.setIdVersion(idversion.getValue().toString());
				}
			} else
			{
				x = (UnificationXref) model.getByID(RDFId);
			}
			interaction.addXref(x);
		}
		// Publication Xref
		
		set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.95"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			String RDFId = set.get(i - 1).getValue().toString();
			
			PublicationXref x;
			if (!model.containsID(RDFId))
			{
				x = model.addNew(PublicationXref.class, RDFId);
				Attribute db = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.96"), elem, i);
				if (db != null)
				{
					x.setDb(db.getValue().toString());
				}
				Attribute dbversion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.97"), elem, i);
				if (dbversion != null)
				{
					x.setDbVersion(dbversion.getValue().toString());
				}
				Attribute id = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.98"), elem, i);
				if (id != null)
				{
					x.setId(id.getValue().toString());
				}
				Attribute idversion = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.99"), elem, i);
				if (idversion != null)
				{
					x.setIdVersion(idversion.getValue().toString());
				}
				Attribute title = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.100"), elem, i);
				if (title != null)
				{
					x.setTitle(title.getValue().toString());
				}
				Attribute year = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.101"), elem, i);
				if (year != null)
				{
					x.setYear(Integer.valueOf(year.getValue().toString()));
				}
				
				ArrayList<Attribute> secondSet = getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.102"), elem, i);
				for (Attribute a : secondSet)
				{
					x.addAuthor(a.getValue().toString());
				}
				secondSet = getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.103"), elem, i);
				for (Attribute a : secondSet)
				{
					x.addSource(a.getValue().toString());
				}
				secondSet = getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.104"), elem, i);
				for (Attribute a : secondSet)
				{
					x.addUrl(a.getValue().toString());
				}
				
			} else
			{
				x = (PublicationXref) model.getByID(RDFId);
			}
			interaction.addXref(x);
		}
		
		// Relationship Xref
		
		set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.92"), elem);
		for (int i = 1; i <= set.size(); i++)
		{
			String RDFId = set.get(i - 1).getValue().toString();
			RelationshipXref relX;
			if (!model.containsID(RDFId))
			{
				relX = model.addNew(RelationshipXref.class, RDFId);
				
				Attribute RelationshipVocRDFIdAttr = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.93"), elem, i);
				if (RelationshipVocRDFIdAttr != null)
				{
					String RelationshipVocRDFId = RelationshipVocRDFIdAttr.getValue().toString();
					RelationshipTypeVocabulary relVoc;
					if (!model.containsID(RelationshipVocRDFId))
					{
						relVoc = model.addNew(RelationshipTypeVocabulary.class, RelationshipVocRDFId);
						for (Attribute term : getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.94"), elem, i))
						{
							relVoc.addTerm(term.getValue().toString());
						}
					} else
					{
						relVoc = (RelationshipTypeVocabulary) model.getByID(RelationshipVocRDFId);
					}
					relX.setRelationshipType(relVoc);
				}
			} else
			{
				relX = (RelationshipXref) model.getByID(RDFId);
			}
			
			interaction.addXref(relX);
		}
	}
	
	public UtilitySuperClassFromGraph()
	{
		super();
	}
	
}