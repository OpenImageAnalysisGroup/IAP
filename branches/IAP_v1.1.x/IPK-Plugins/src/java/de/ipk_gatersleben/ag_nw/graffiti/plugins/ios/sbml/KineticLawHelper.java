package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.attributes.Attributable;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.AttributeListener;
import org.graffiti.event.ListenerManager;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

public class KineticLawHelper implements AttributeListener {
	
	Graph g;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Writer attWriter;
	
	Map<String, List<Node>> _reactionClones;
	
	/**
	 * Provides necessary methods
	 */
	SBML_SBase_Reader attReader;
	
	List<SBMLLocalParameter> localParameterList;
	
	/**
	 * Constructor. Initializes the graph
	 * 
	 * @param g
	 *        the graph where the information is read from
	 */
	public KineticLawHelper(Graph g, Map<String, List<Node>> reactionClones) {
		this.g = g;
		attWriter = new SBML_SBase_Writer();
		attReader = new SBML_SBase_Reader();
		_reactionClones = reactionClones;
		localParameterList = new ArrayList<SBMLLocalParameter>();
		initKineticLawNideIDs();
		ListenerManager listenerManager = g.getListenerManager();
		listenerManager.addAllTimeAttributeListener(this);
	}
	
	public SBMLLocalParameter addLocalParemeter(Graph g,
			String presentedAttributeName, String internAttributeName) {
		SBMLLocalParameter localParameter = new SBMLLocalParameter(g,
				presentedAttributeName, internAttributeName);
		localParameterList.add(localParameter);
		return localParameter;
	}
	
	public SBMLLocalParameter addLocalParameter(Graph g, int localParameterCount) {
		SBMLLocalParameter localParameter = new SBMLLocalParameter(g,
				localParameterCount);
		localParameterList.add(localParameter);
		return localParameter;
	}
	
	public void setMetaId(Node reactionNode, String metaID) {
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_META_ID, metaID);
		}
	}
	
	public Boolean isSetMetaID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_META_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getMetaID(Node reactionNode) {
		if (isSetMetaID(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_META_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteMetaID(Node reactionNode) {
		if (isSetMetaID(reactionNode)) {
			AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_META_ID);
		}
	}
	
	public void setSBOTerm(Node reactionNode, String sboTerm) {
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_SBOTERM, sboTerm);
		}
	}
	
	public String getSBOTerm(Node reactionNode) {
		if (isSetSBOTerm(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_SBOTERM);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void deleteSBOTerm(Node reactionNode) {
		if (isSetSBOTerm(reactionNode)) {
			AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_SBOTERM);
		}
	}
	
	public Boolean isSetSBOTerm(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_SBOTERM)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setAnnotation(Node reactionNode, Annotation annotation) {
		AttributeHelper.setAttribute(reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_ANNOTATION, annotation);
	}
	
	public Boolean isSetAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteAnnotation(Node reactionNode) {
		if (isSetAnnotation(reactionNode)) {
			AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_ANNOTATION);
		}
	}
	
	public Annotation getAnnotation(Node reactionNode) {
		if (isSetAnnotation(reactionNode)) {
			return (Annotation) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_ANNOTATION);
		} else {
			return null;
		}
	}
	
	public void setNonRDFAnnotation(Node reactionNode, String nonRDFAnnotation) {
		AttributeHelper
				.setAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
						SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION,
						nonRDFAnnotation);
	}
	
	public Boolean isSetNonRDFAnnotation(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteNonRDFAnnotation(Node reactionNode) {
		if (isSetNonRDFAnnotation(reactionNode)) {
			AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION);
		}
	}
	
	public String getNonRDFAnnotation(Node reactionNode) {
		if (isSetNonRDFAnnotation(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	public void setFunction(Node reactionNode, String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_FUNCTION, function);
		}
	}
	
	public void setNotes(Node reactionNode, String notes, XMLNode notesObj) {
		if (!notes.equals(SBML_Constants.EMPTY)) {
			attReader.addNotes(notesObj, notes, reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_NOTES);
		}
	}
	
	public Boolean isSetNotes(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_NOTES)) {
			return true;
		} else {
			return false;
		}
	}
	
	public XMLNode getNotes(Node reactionNode) {
		if (isSetNotes(reactionNode)) {
			return (XMLNode) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_NOTES);
		} else {
			return null;
		}
	}
	
	public void deleteNotes(Node reactionNode) {
		if (isSetNotes(reactionNode)) {
			AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_NOTES);
		}
	}
	
	public Boolean isSetFunction(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_FUNCTION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getFunction(Node reactionNode) {
		if (isSetFunction(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_FUNCTION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	private void initKineticLawNideIDs() {
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_NOTES,
				"SBML Kinetic Law: Notes");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_META_ID,
				"SBML Kinetic Law: Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_SBOTERM,
				"SBML Kinetic Law: SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_FUNCTION,
				"SBML Kinetic Law: Function");
	}
	
	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		
	}
	
	@Override
	public void transactionStarted(TransactionEvent e) {
		
	}
	
	@Override
	public void postAttributeAdded(AttributeEvent e) {

	}
	
	@Override
	public void postAttributeChanged(AttributeEvent e) {
		Attributable attributeable = e.getAttributeable();
		if (attributeable instanceof Node) {
			if (SBML_Constants.ROLE_REACTION.equals(AttributeHelper.getSBMLrole((Node) attributeable))) {
				Node reactionNode = (Node) attributeable;
				propagateChanges(reactionNode, e.getAttribute().getName(), e.getAttribute().getValue());
			}
		}
	}
	
	private void propagateChanges(Node reactionNode, String attributeName, Object newValue) {
		String reactionId = SBMLHelper.getReactionID(reactionNode);
		List<Node> reactionNodes = _reactionClones.get(reactionId);
		if (reactionNodes == null) {
			reactionNodes = new ArrayList<Node>();
		}
		for (Node node : reactionNodes) {
			
			if (newValue instanceof String && SBMLHelper.isKineticLawFunction(reactionNode)) {
				String function = (String) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_FUNCTION) && function.equals(SBML_Constants.EMPTY)) {
					AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW, SBML_Constants.KINETIC_LAW_FUNCTION);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_FUNCTION) && !SBMLHelper.getKineticLawFunction(node).equals(function)) {
					setFunction(node, function);
					continue;
				}
			}
			if (newValue instanceof String && isSetMetaID(reactionNode)) {
				String metaID = (String) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_META_ID) && metaID.equals(SBML_Constants.EMPTY)) {
					deleteMetaID(node);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_META_ID) && !getMetaID(node).equals(metaID)) {
					setMetaId(node, metaID);
					continue;
				}
			}
			if (newValue instanceof String && isSetSBOTerm(reactionNode)) {
				String sboTerm = (String) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_SBOTERM) && sboTerm.equals(SBML_Constants.EMPTY)) {
					deleteSBOTerm(node);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_SBOTERM) && !getSBOTerm(node).equals(sboTerm)) {
					setSBOTerm(node, sboTerm);
					continue;
				}
			}
			
			if (newValue instanceof XMLNode && isSetNotes(reactionNode)) {
				XMLNode notes = (XMLNode) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_NOTES) && notes.equals(SBML_Constants.EMPTY)) {
					deleteNotes(node);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_NOTES) && !getNotes(node).equals(notes)) {
					setNotes(node, notes.toString(), notes);
					continue;
				}
			}
			if (newValue instanceof Annotation && isSetAnnotation(reactionNode)) {
				Annotation annotation = (Annotation) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_ANNOTATION) && annotation.equals(SBML_Constants.EMPTY)) {
					deleteAnnotation(node);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_ANNOTATION) && !getAnnotation(node).equals(annotation)) {
					setAnnotation(node, annotation);
					continue;
				}
			}
			
			if (newValue instanceof String && isSetNonRDFAnnotation(reactionNode)) {
				String annotation = (String) newValue;
				if (attributeName.equals(SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION) && annotation.equals(SBML_Constants.EMPTY)) {
					deleteNonRDFAnnotation(node);
				} else if (attributeName.equals(SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION) && !getNonRDFAnnotation(node).equals(annotation)) {
					setNonRDFAnnotation(node, annotation);
					continue;
				}
			}
			
			Iterator<SBMLLocalParameter> localParameterIt = localParameterList.iterator();
			int count = 1;
			while (localParameterIt.hasNext()) {
				SBMLLocalParameter localParameter = localParameterIt.next();
				if (newValue instanceof String && SBMLHelper.isLocalParameterID(reactionNode, count)) {
					String localParameterName = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.LOCAL_PARAMETER_ID;
					
					String id = (String) newValue;
					if (id.equals(SBML_Constants.EMPTY)) {
						SBMLHelper.deleteLocalParameterID(node, id);
					}
					else if (attributeName.equals(localParameterName) && !SBMLHelper.getLocalParameterID(node, count).equals(id)) {
						localParameter.setID(node, id);
						continue;
					}
				}
				if (newValue instanceof String && SBMLHelper.isLocalParameterName(reactionNode, count)) {
					String localParameterName = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.LOCAL_PARAMETER_NAME;
					
					String name = (String) newValue;
					if (name.equals(SBML_Constants.EMPTY)) {
						SBMLHelper.deleteLocalParameterName(node, SBMLHelper.getLocalParameterID(node, count));
					}
					else if (attributeName.equals(localParameterName) && !SBMLHelper.getLocalParameterName(node, count).equals(name)) {
						localParameter.setName(node, name);
						continue;
					}
					
				}
				if (newValue instanceof Double && SBMLHelper.isLocalParameterValue(reactionNode, count)) {
					String localParameterName = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.LOCAL_PARAMETER_VALUE;
					
					Double value = (Double) newValue;
					if (value.equals(SBML_Constants.EMPTY)) {
						SBMLHelper.deleteLocalParameterValue(node, SBMLHelper.getLocalParameterID(node, count));
					}
					else if (attributeName.equals(localParameterName) && !SBMLHelper.getLocalParameterValue(node, count).equals(value)) {
						localParameter.setValue(node, value);
						continue;
					}
					
				}
				if (newValue instanceof String && SBMLHelper.isLocalParameterUnits(reactionNode, count)) {
					String localParameterName = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.LOCAL_PARAMETER_UNITS;
					
					String units = (String) newValue;
					if (units.equals(SBML_Constants.EMPTY)) {
						SBMLHelper.deleteLocalParameterUnits(node, SBMLHelper.getLocalParameterID(node, count));
					}
					else if (attributeName.equals(localParameterName) && !SBMLHelper.getLocalParameterUnits(node, count).equals(units)) {
						localParameter.setUnits(node, units);
						continue;
					}
					
				}
				if (newValue instanceof String && localParameter.isSetMetaID(reactionNode)) {
					String localParameterMetaID = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.META_ID;
					
					String metaId = (String) newValue;
					if (attributeName.equals(localParameterMetaID) && metaId.equals(SBML_Constants.EMPTY)) {
						localParameter.deleteMetaID(node);
					}
					else if (attributeName.equals(localParameterMetaID) && !getMetaID(node).equals(metaId)) {
						localParameter.setMetaID(node, metaId);
						continue;
					}
					
				}
				if (newValue instanceof String && localParameter.isSetSBOTerm(reactionNode)) {
					String localParameterSBOTerm = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.SBOTERM;
					
					String sboTerm = (String) newValue;
					String oldValue = getSBOTerm(node);
					if (attributeName.equals(localParameterSBOTerm) && sboTerm.equals(SBML_Constants.EMPTY)) {
						localParameter.deleteSBOTerm(node);
					}
					else if (attributeName.equals(localParameterSBOTerm) && !oldValue.equals(sboTerm)) {
						localParameter.setSBOTerm(node, sboTerm);
						continue;
					}
				}
				
				if (newValue instanceof XMLNode && localParameter.isSetNotes(reactionNode)) {
					String localParameterNote = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.NOTES;
					
					XMLNode notes = (XMLNode) newValue;
					XMLNode notesOld = getNotes(node);
					if (attributeName.equals(localParameterNote) && notes.equals(SBML_Constants.EMPTY)) {
						localParameter.deleteNotes(node);
					}
					else if (attributeName.equals(localParameterNote) && !notesOld.equals(notes)) {
						localParameter.setNotes(node, notes.toString(), notes);
						continue;
					}
				}
				
				if (newValue instanceof Annotation && localParameter.isSetAnnotation(reactionNode)) {
					String localParameterAnnotation = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.ANNOTATION;
					
					Annotation annotation = (Annotation) newValue;
					Annotation oldAnnotation = getAnnotation(node);
					if (attributeName.equals(localParameterAnnotation) && annotation.equals(SBML_Constants.EMPTY)) {
						localParameter.deleteAnnotation(node);
					}
					else if (attributeName.equals(localParameterAnnotation) && !oldAnnotation.equals(annotation)) {
						localParameter.setAnnotation(node, annotation);
						continue;
					}
				}
				
				if (newValue instanceof String && localParameter.isSetNonRDFAnnotation(reactionNode)) {
					String localParameterAnnotation = SBML_Constants.LOCAL_PARAMETER + count + SBML_Constants.NON_RDF_ANNOTATION;
					
					String annotation = (String) newValue;
					String oldAnnotation = getNonRDFAnnotation(node);
					if (attributeName.equals(localParameterAnnotation) && annotation.equals(SBML_Constants.EMPTY)) {
						localParameter.deleteNonRDFAnnotation(node);
					}
					else if (attributeName.equals(localParameterAnnotation) && !oldAnnotation.equals(annotation)) {
						localParameter.setNonRDFAnnotation(node, annotation);
						continue;
					}
				}
				count++;
			}
		}
	}
	
	@Override
	public void postAttributeRemoved(AttributeEvent e) {
		
	}
	
	@Override
	public void preAttributeAdded(AttributeEvent e) {
		
	}
	
	@Override
	public void preAttributeChanged(AttributeEvent e) {
		
	}
	
	@Override
	public void preAttributeRemoved(AttributeEvent e) {
		
	}
}
