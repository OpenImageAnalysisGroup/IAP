/**
 * This class writes the SBase Attributes
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_SBase_Writer {
	
	/**
	 * Saves the number of the current Unit
	 */
	int sBaseAttributeUnitCount = 1;
	
	/**
	 * Saves the number of the current EventAssignment
	 */
	int SBaseAttributeFromEACount = 1;
	
	/**
	 * Saves the number of the current local parameter
	 */
	private int localParameterCount = 1;
	
	/**
	 * To make the code shorter
	 */
	public final String ATT = AttributeHelper.attributeSeparator;
	
	private HashMap<String, String> nameSpaceCollector = new HashMap<String, String>();
	
	/**
	 * This Method helps processing the attributes with a variable headline
	 * inherited by SBase
	 * 
	 * @param sbase
	 *           has the attribute which will be set
	 * @param g
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param intern
	 *           representation of headline
	 */
	public void addSBaseAttributes(AbstractSBase sbase, Graph g, String niceID) {
		if (sbase instanceof org.sbml.jsbml.Priority) {
			addSBaseAttributeFromPriority((Priority) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Delay) {
			addSBaseAttributeFromDelay((Delay) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.EventAssignment) {
			addSBaseAttributeFromEventAssignment((EventAssignment) sbase, g,
					niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Unit) {
			addSBaseAttributeFromUnit((Unit) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.FunctionDefinition) {
			addSBaseAttributesFromFunctionDefinition(
					(FunctionDefinition) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.UnitDefinition) {
			addSBaseAttributesFromUnitDefinition((UnitDefinition) sbase, g,
					niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Compartment) {
			addSBaseAttributesFromCompartment((Compartment) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Parameter) {
			addSBaseAttributesFromParameter((Parameter) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.InitialAssignment) {
			addSBaseAttributesFromInitialAssignmnet((InitialAssignment) sbase,
					g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Rule) {
			addSBaseAttributesFromRule((Rule) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Constraint) {
			addSBaseAttributesFromConstraint((Constraint) sbase, g, niceID);
		}
		if (sbase instanceof org.sbml.jsbml.Event) {
			addSBaseAttributesFromEvent((Event) sbase, g, niceID);
		}
	}
	
	public void addSBaseAttributes(AbstractSBase sbase, Graph g) {
		if (sbase instanceof SBMLDocument) {
			// Processing the name spaces
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.NAMESPACE)) {
				String globalNamespaces = (String) getAttribute(g,
						SBML_Constants.SBML, SBML_Constants.NAMESPACE);
				if (!SBML_Constants.EMPTY.equals(globalNamespaces)) {
					String[] globalNamespacesArr = globalNamespaces.split(" ");
					String namespaceName;
					String prefix = SBML_Constants.EMPTY;
					String uri;
					String postfix = null;
					for (int i = 0; i < globalNamespacesArr.length; i++) {
						if (globalNamespacesArr[i].contains(":")) {
							prefix = globalNamespacesArr[i].split(":")[0];
							if (prefix.equals("xmlns")) {
								prefix = SBML_Constants.EMPTY;
							}
							postfix = globalNamespacesArr[i].split(":")[1];
						}
						if (null == postfix) {
							postfix = globalNamespacesArr[i];
						}
						uri = globalNamespacesArr[i].split("=")[1].replaceAll(
								"\"", SBML_Constants.EMPTY);
						namespaceName = globalNamespacesArr[i].split("=")[0];
						((SBMLDocument) sbase).addNamespace(namespaceName,
								prefix, uri);
						nameSpaceCollector.put(prefix + ":" + namespaceName,
								uri);
					}
				}
			}
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.SBML_NOTES)) {
				XMLNode notes = (XMLNode) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.SBML_NOTES);
				if (!SBML_Constants.EMPTY.equals(notes)) {
					sbase.setNotes(notes);
				}
			}
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.SBML_META_ID)) {
				String metaid = (String) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.SBML_META_ID);
				if (!SBML_Constants.EMPTY.equals(metaid)) {
					sbase.setMetaId(metaid);
				}
			}
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.SBML_SBOTERM)) {
				String sboterm = (String) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.SBML_SBOTERM);
				if (!SBML_Constants.EMPTY.equals(sboterm)) {
					sbase.setSBOTerm(sboterm);
				}
			}
			addAnnotation(sbase, g, null, SBML_Constants.SBML,
					SBML_Constants.SBML_ANNOTATION,
					SBML_Constants.SBML_NON_RDF_ANNOTATION, true);
		}
		if (sbase instanceof Model) {
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_NOTES)) {
				XMLNode notes = (XMLNode) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_NOTES);
				if (!SBML_Constants.EMPTY.equals(notes)) {
					sbase.setNotes(notes);
				}
			}
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_META_ID)) {
				String metaid = (String) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_META_ID);
				if (!SBML_Constants.EMPTY.equals(metaid)) {
					sbase.setMetaId(metaid);
				}
			}
			if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_SBOTERM)) {
				String sboterm = (String) getAttribute(g, SBML_Constants.SBML,
						SBML_Constants.MODEL_SBOTERM);
				if (!SBML_Constants.EMPTY.equals(sboterm)) {
					sbase.setSBOTerm(sboterm);
				}
			}
			addAnnotation(sbase, g, null, SBML_Constants.SBML,
					SBML_Constants.MODEL_ANNOTATION,
					SBML_Constants.MODEL_NON_RDF_ANNOTATION, true);
		}
	}
	
	// gIsSet is true if g is used und not element
	private void addAnnotation(AbstractSBase sbase, Graph g,
			GraphElement element, String headline, String rdfAttributeName,
			String nonRDFAttributeName, Boolean gIsSet) {
		if (gIsSet) {
			Annotation annotation = null;
			if (AttributeHelper.hasAttribute(g, headline, rdfAttributeName)) {
				annotation = (Annotation) getAttribute(g, headline,
						rdfAttributeName);
				
			}
			if (AttributeHelper.hasAttribute(g, headline, nonRDFAttributeName)) {
				if (annotation == null) {
					annotation = new Annotation();
				}
				annotation.appendNoRDFAnnotation((String) getAttribute(g,
						headline, nonRDFAttributeName));
				Map<String, String> namespaces = sbase.getSBMLDocument()
						.getSBMLDocumentNamespaces();
				Set<Entry<String, String>> namespacesEntrySet = namespaces
						.entrySet();
				for (Entry<String, String> entry : namespacesEntrySet) {
					String key = entry.getKey();
					String prefix = "xmlns";
					String namespaceName = SBML_Constants.EMPTY;
					if (key.contains(":")) {
						prefix = key.split(":")[0];
						namespaceName = key.split(":")[1];
					} else {
						namespaceName = key;
					}
					if (annotation.getNonRDFannotation().contains(
							"<" + namespaceName + ":")) {
						annotation.addAnnotationNamespace(namespaceName,
								prefix, entry.getValue());
					}
				}
			}
			if (null != annotation) {
				if (annotation.isSetNonRDFannotation()) {
					if (annotation.getNonRDFannotation().contains("<rdf:")) {
						sbase.getSBMLDocument().addNamespace("rdf", "xmlns",
								"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
					}
				}
				sbase.setAnnotation(annotation);
			}
		} else {
			Annotation annotation = null;
			if (AttributeHelper.hasAttribute(element, headline,
					rdfAttributeName)) {
				annotation = (Annotation) getAttribute(element, headline,
						rdfAttributeName);
				
			}
			if (AttributeHelper.hasAttribute(element, headline,
					nonRDFAttributeName)) {
				if (annotation == null) {
					annotation = new Annotation();
				}
				annotation.appendNoRDFAnnotation((String) getAttribute(element,
						headline, nonRDFAttributeName));
				
				Map<String, String> namespaces;
				if (!(sbase instanceof SBMLDocument)
						&& (!(sbase instanceof SimpleSpeciesReference))) {
					namespaces = sbase.getSBMLDocument()
							.getSBMLDocumentNamespaces();
					
				} else
					if (sbase instanceof SimpleSpeciesReference) {
						namespaces = nameSpaceCollector;
					} else {
						namespaces = ((SBMLDocument) sbase)
								.getSBMLDocumentNamespaces();
					}
				if (null != namespaces) {
					Set<Entry<String, String>> namespacesEntrySet = namespaces
							.entrySet();
					for (Entry<String, String> entry : namespacesEntrySet) {
						String key = entry.getKey();
						String prefix = "xmlns";
						String namespaceName = SBML_Constants.EMPTY;
						if (key.contains(":")) {
							prefix = key.split(":")[0];
							namespaceName = key.split(":")[1];
						} else {
							namespaceName = key;
						}
						if (annotation.getNonRDFannotation().contains(
								"<" + namespaceName + ":")) {
							annotation.addAnnotationNamespace(namespaceName,
									prefix, entry.getValue());
						}
					}
				}
				if (null != annotation) {
					if (annotation.getNonRDFannotation().contains("<rdf:")) {
						sbase.getSBMLDocument()
								.addNamespace("rdf", "xmlns",
										"http://www.sbml.org/sbml/level3/version1/layout/version1");
						
					}
					sbase.setAnnotation(annotation);
				}
			}
		}
	}
	
	private void addSBaseAttributesFromEvent(Event sbase, Graph g, String niceID) {
		
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromConstraint(Constraint sbase, Graph g,
			String niceID) {
		
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromRule(Rule sbase, Graph g, String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromInitialAssignmnet(
			InitialAssignment sbase, Graph g, String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromParameter(Parameter sbase, Graph g,
			String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromCompartment(Compartment sbase, Graph g,
			String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	private void addSBaseAttributesFromUnitDefinition(UnitDefinition sbase,
			Graph g, String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
		
	}
	
	private void addSBaseAttributesFromFunctionDefinition(
			FunctionDefinition sbase, Graph g, String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				sbase,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.ANNOTATION)
						.toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.NON_RDF_ANNOTATION).toString(), true);
	}
	
	public void addSBaseAttributes(AbstractSBase sbase, Edge e, String niceID) {
		
		if (AttributeHelper.getSBMLrole(e).equals("modifier")) {
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.MODIFIER_META_ID)) {
				String metaID = (String) getAttribute(e, niceID,
						SBML_Constants.MODIFIER_META_ID);
				if (!SBML_Constants.EMPTY.equals(metaID)) {
					sbase.setMetaId(metaID);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.MODIFIER_NOTES)) {
				XMLNode notes = (XMLNode) getAttribute(e, niceID,
						SBML_Constants.MODIFIER_NOTES);
				if (!SBML_Constants.EMPTY.equals(notes)) {
					sbase.appendNotes(notes);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.MODIFIER_SBOTERM)) {
				String sboTerm = (String) getAttribute(e, niceID,
						SBML_Constants.MODIFIER_SBOTERM);
				if (!SBML_Constants.EMPTY.equals(sboTerm)) {
					sbase.setSBOTerm(sboTerm);
				}
			}
			addAnnotation(sbase, null, e, niceID,
					SBML_Constants.MODIFIER_ANNOTATION,
					SBML_Constants.MODIFIER_NON_RDF_ANNOTATION, false);
			
		}
		
		if (AttributeHelper.getSBMLrole(e).equals("reactant")) {
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.REACTANT_META_ID)) {
				String metaID = (String) getAttribute(e, niceID,
						SBML_Constants.REACTANT_META_ID);
				if (!SBML_Constants.EMPTY.equals(metaID)) {
					sbase.setMetaId(metaID);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.REACTANT_NOTES)) {
				XMLNode notes = (XMLNode) getAttribute(e, niceID,
						SBML_Constants.REACTANT_NOTES);
				if (!SBML_Constants.EMPTY.equals(notes)) {
					sbase.appendNotes(notes);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.REACTANT_SBOTERM)) {
				String sboTerm = (String) getAttribute(e, niceID,
						SBML_Constants.REACTANT_SBOTERM);
				if (!SBML_Constants.EMPTY.equals(sboTerm)) {
					sbase.setSBOTerm(sboTerm);
				}
			}
			addAnnotation(sbase, null, e, niceID,
					SBML_Constants.REACTANT_ANNOTATION,
					SBML_Constants.REACTANT_NON_RDF_ANNOTATION, false);
			
		}
		
		if (AttributeHelper.getSBMLrole(e).equals("product")) {
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.PRODUCT_META_ID)) {
				String metaID = (String) getAttribute(e, niceID,
						SBML_Constants.PRODUCT_META_ID);
				if (!SBML_Constants.EMPTY.equals(metaID)) {
					sbase.setMetaId(metaID);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.PRODUCT_NOTES)) {
				XMLNode notes = (XMLNode) getAttribute(e, niceID,
						SBML_Constants.PRODUCT_NOTES);
				if (!SBML_Constants.EMPTY.equals(notes)) {
					sbase.appendNotes(notes);
				}
			}
			if (AttributeHelper.hasAttribute(e, niceID,
					SBML_Constants.PRODUCT_SBOTERM)) {
				String sboTerm = (String) getAttribute(e, niceID,
						SBML_Constants.PRODUCT_SBOTERM);
				if (!SBML_Constants.EMPTY.equals(sboTerm)) {
					sbase.setSBOTerm(sboTerm);
				}
			}
			addAnnotation(sbase, null, e, niceID,
					SBML_Constants.PRODUCT_ANNOTATION,
					SBML_Constants.PRODUCT_NON_RDF_ANNOTATION, false);
		}
	}
	
	/**
	 * Shortens the statement for getting the value of an attribute
	 * 
	 * @param g
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param attributeName
	 *           is the attribute to be read
	 * @return the value of the attribute
	 */
	public Object getAttribute(Graph g, String headline, String attributeName) {
		return AttributeHelper.getAttributeValue(g, headline, attributeName,
				SBML_Constants.EMPTY, null);
	}
	
	public Object getAttribute(Edge e, String headline, String attributeName) {
		return AttributeHelper.getAttributeValue(e, headline, attributeName,
				SBML_Constants.EMPTY, null);
	}
	
	/**
	 * This method writes the SBase Attributes of Priority
	 * 
	 * @param priority
	 *           the SBase Attributes will be added to this object
	 * @param g
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param niceID
	 *           intern representation of headline
	 */
	private void addSBaseAttributeFromPriority(Priority priority, Graph g,
			String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.PRIORITY_META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.PRIORITY_META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				priority.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.PRIORITY_NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.PRIORITY_NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				priority.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.PRIORITY_SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.PRIORITY_SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				priority.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				priority,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(
						SBML_Constants.PRIORITY_ANNOTATION).toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.PRIORITY_NON_RDF_ANNOTATION).toString(),
				true);
		
	}
	
	/**
	 * This method writes the SBase Attributes of Delay
	 * 
	 * @param delay
	 *           the SBase Attributes will be added to this object
	 * @param g
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param niceID
	 *           intern representation of headline
	 */
	private void addSBaseAttributeFromDelay(Delay delay, Graph g, String niceID) {
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.DELAY_META_ID).toString())) {
			String metaID = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.DELAY_META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				delay.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.DELAY_NOTES).toString())) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.DELAY_NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(notes)) {
				delay.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID)
				.append(SBML_Constants.DELAY_SBOTERM).toString())) {
			String sboTerm = (String) getAttribute(g, niceID, new StringBuffer(
					niceID).append(SBML_Constants.DELAY_SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				delay.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				delay,
				g,
				null,
				niceID,
				new StringBuffer(niceID)
						.append(SBML_Constants.DELAY_ANNOTATION).toString(),
				new StringBuffer(niceID).append(
						SBML_Constants.DELAY_NON_RDF_ANNOTATION).toString(),
				true);
	}
	
	/**
	 * This method writes the SBase Attributes of EventAssignment
	 * 
	 * @param eventAssignment
	 *           the SBase Attributes will be added to this object
	 * @param g
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param niceID
	 *           intern representation of headline
	 */
	private void addSBaseAttributeFromEventAssignment(
			EventAssignment eventAssignment, Graph g, String niceID) {
		String ea_metaid = new StringBuffer(niceID)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(SBaseAttributeFromEACount)
				.append(SBML_Constants.META_ID).toString();
		if (AttributeHelper.hasAttribute(g, niceID, ea_metaid)) {
			String metaID = (String) getAttribute(g, niceID, ea_metaid);
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				eventAssignment.setMetaId(metaID);
			}
		}
		String ea_tooltip = new StringBuffer(niceID)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(SBaseAttributeFromEACount).append(SBML_Constants.NOTES)
				.toString();
		if (AttributeHelper.hasAttribute(g, niceID, ea_tooltip)) {
			XMLNode notes = (XMLNode) getAttribute(g, niceID, ea_tooltip);
			if (!SBML_Constants.EMPTY.equals(notes)) {
				eventAssignment.appendNotes(notes);
			}
		}
		String ea_sboterm = new StringBuffer(niceID)
				.append(SBML_Constants.EVENT_ASSIGNMENT)
				.append(SBaseAttributeFromEACount)
				.append(SBML_Constants.SBOTERM).toString();
		if (AttributeHelper.hasAttribute(g, niceID, ea_sboterm)) {
			String sboTerm = (String) getAttribute(g, niceID, ea_sboterm);
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				eventAssignment.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(
				eventAssignment,
				g,
				null,
				niceID,
				new StringBuffer(niceID)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(SBaseAttributeFromEACount)
						.append(SBML_Constants.ANNOTATION).toString(),
				new StringBuffer(niceID)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(SBaseAttributeFromEACount)
						.append(SBML_Constants.NON_RDF_ANNOTATION).toString(),
				true);
		SBaseAttributeFromEACount++;
	}
	
	private void addSBaseAttributeFromUnit(Unit unit, Graph g, String niceID) {
		String su_metaid = new StringBuffer(niceID)
				.append(SBML_Constants.SUB_UNIT)
				.append(sBaseAttributeUnitCount).append(SBML_Constants.META_ID)
				.toString();
		if (AttributeHelper.hasAttribute(g, niceID, su_metaid)) {
			String metaID = (String) getAttribute(g, niceID, su_metaid);
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				unit.setMetaId(metaID);
			}
		}
		String attributeName = new StringBuffer(niceID)
				.append(SBML_Constants.SUB_UNIT)
				.append(sBaseAttributeUnitCount).append(SBML_Constants.NOTES)
				.toString();
		if (AttributeHelper.hasAttribute(g, niceID, attributeName)) {
			XMLNode note = (XMLNode) getAttribute(g, niceID, attributeName);
			if (!SBML_Constants.EMPTY.equals(note)) {
				unit.appendNotes(note);
			}
		}
		String su_sboterm = new StringBuffer(niceID)
				.append(SBML_Constants.SUB_UNIT)
				.append(sBaseAttributeUnitCount).append(SBML_Constants.SBOTERM)
				.toString();
		if (AttributeHelper.hasAttribute(g, niceID, su_sboterm)) {
			String sboTerm = (String) getAttribute(g, niceID, su_sboterm);
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				unit.setSBOTerm(sboTerm);
			}
		}
		
		addAnnotation(
				unit,
				g,
				null,
				niceID,
				new StringBuffer(niceID).append(SBML_Constants.SUB_UNIT)
						.append(sBaseAttributeUnitCount)
						.append(SBML_Constants.ANNOTATION).toString(),
				new StringBuffer(niceID).append(SBML_Constants.SUB_UNIT)
						.append(sBaseAttributeUnitCount)
						.append(SBML_Constants.NON_RDF_ANNOTATION).toString(),
				true);
		
		sBaseAttributeUnitCount++;
	}
	
	/**
	 * Method adds all sBase attributes to an object of a model
	 * 
	 * @param sbase
	 *           will set new attributes
	 * @param node
	 *           is the current graph element
	 * @param headline
	 *           indicates where the information should be read from
	 * @param niceID
	 *           the intern representation of headline
	 */
	public void addSBaseAttributes(AbstractSBase sbase, Node node) {
		if (sbase instanceof org.sbml.jsbml.LocalParameter) {
			addSBaseAttributeFromLocalParameter((LocalParameter) sbase, node);
		}
		if (sbase instanceof org.sbml.jsbml.Species) {
			addSBaseAttributeFromSpecies((Species) sbase, node);
		}
		if (sbase instanceof org.sbml.jsbml.Reaction) {
			addSBaseAttributeFromReaction((Reaction) sbase, node);
		}
		if (sbase instanceof org.sbml.jsbml.KineticLaw) {
			addSBaseAttributeFromKineticLaw((KineticLaw) sbase, node);
		}
	}
	
	private void addSBaseAttributeFromKineticLaw(KineticLaw sbase, Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_META_ID)) {
			String metaID = (String) getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_META_ID);
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_NOTES)) {
			XMLNode notes = (XMLNode) getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_NOTES);
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_SBOTERM)) {
			String sboTerm = (String) getAttribute(node,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_SBOTERM);
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(sbase, null, node, SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_ANNOTATION,
				SBML_Constants.KINETIC_LAW_NON_RDF_ANNOTATION, false);
		
	}
	
	private void addSBaseAttributeFromReaction(Reaction sbase, Node node) {
		
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.REACTION_META_ID)) {
			String metaID = (String) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.REACTION_META_ID);
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.REACTION_NOTES)) {
			XMLNode notes = (XMLNode) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.REACTION_NOTES);
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.REACTION_SBOTERM)) {
			String sboTerm = (String) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.REACTION_SBOTERM);
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(sbase, null, node, SBML_Constants.SBML,
				SBML_Constants.REACTION_ANNOTATION,
				SBML_Constants.REACTION_NON_RDF_ANNOTATION, false);
		
	}
	
	private void addSBaseAttributeFromSpecies(Species sbase, Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_META_ID)) {
			String metaID = (String) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_META_ID);
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				sbase.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_NOTES)) {
			XMLNode notes = (XMLNode) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_NOTES);
			if (!SBML_Constants.EMPTY.equals(notes)) {
				sbase.appendNotes(notes);
			}
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_SBOTERM)) {
			String sboTerm = (String) getAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SBOTERM);
			if (!SBML_Constants.EMPTY.equals(sboTerm)) {
				sbase.setSBOTerm(sboTerm);
			}
		}
		addAnnotation(sbase, null, node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ANNOTATION,
				SBML_Constants.SPECIES_NON_RDF_ANNOTATION, false);
		
	}
	
	/**
	 * This method writes the SBase Attributes of LocalPatameter
	 * 
	 * @param localParameter
	 *           the SBase Attributes will be added to this object
	 * @param node
	 *           the SBase Attributes will be added to this object
	 * @param headline
	 *           indicates where the information should be read from
	 * @param niceID
	 *           the intern representation of headline
	 */
	private void addSBaseAttributeFromLocalParameter(
			LocalParameter localParameter, Node node) {
		if (AttributeHelper.hasAttribute(
				node,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.META_ID).toString())) {
			String metaID = (String) getAttribute(
					node,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.META_ID).toString());
			if (!SBML_Constants.EMPTY.equals(metaID)) {
				localParameter.setMetaId(metaID);
			}
		}
		if (AttributeHelper.hasAttribute(
				node,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.SBOTERM).toString())) {
			String SBOTerm = (String) getAttribute(
					node,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.SBOTERM).toString());
			if (!SBML_Constants.EMPTY.equals(SBOTerm)) {
				localParameter.setSBOTerm(SBOTerm);
			}
		}
		if (AttributeHelper.hasAttribute(
				node,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.NOTES).toString())) {
			XMLNode ToolTip = (XMLNode) getAttribute(
					node,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.NOTES).toString());
			if (!SBML_Constants.EMPTY.equals(ToolTip)) {
				localParameter.appendNotes(ToolTip);
			}
		}
		addAnnotation(
				localParameter,
				null,
				node,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.ANNOTATION).toString(),
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.NON_RDF_ANNOTATION).toString(),
				false);
		
		localParameterCount++;
		
	}
	
	/**
	 * Shortens the statement for getting the value of an attribute
	 * 
	 * @param n
	 *           contains the values for the export
	 * @param headline
	 *           indicates where the information should be read from
	 * @param attributeName
	 *           is the attribute to be read
	 * @return the value of the attribute
	 */
	public Object getAttribute(Node n, String headline, String attributeName) {
		return AttributeHelper.getAttributeValue(n, headline, attributeName,
				null, null);
	}
	
	public Object getAttribute(GraphElement n, String headline,
			String attributeName) {
		return AttributeHelper.getAttributeValue(n, headline, attributeName,
				null, null);
	}
	
	/**
	 * Method returns all headlines in the graph tab which begin with a certain
	 * pattern
	 * 
	 * @param g
	 *           contains the values for the export
	 * @param pattern
	 *           is the headline to be found
	 * @return all headlines with the required beginning
	 */
	public ArrayList<String> headlineHelper(Graph g, String pattern) {
		Map<String, Attribute> rr = g.getAttributes().getCollection();
		if (rr != null) {
			Set<Entry<String, Attribute>> entries = rr.entrySet();
			Iterator<Entry<String, Attribute>> itEntries = entries.iterator();
			ArrayList<String> result = new ArrayList<String>();
			while (itEntries.hasNext()) {
				Map.Entry<java.lang.String, org.graffiti.attributes.Attribute> entry = (Map.Entry<java.lang.String, org.graffiti.attributes.Attribute>) itEntries
						.next();
				if (entry.getKey().startsWith(pattern)) {
					result.add(entry.getKey());
				}
			}
			return result;
		} else
			return new ArrayList<String>();
	}
	
	/**
	 * Method returns all headlines in the node tab which begin with a certain
	 * pattern
	 * 
	 * @param g
	 *           contains the values for the export
	 * @param pattern
	 *           is the headline to be found
	 * @return all headlines with the required beginning
	 */
	public ArrayList<String> headlineHelper(Node n, String pattern) {
		Map<String, Attribute> rr = n.getAttributes().getCollection();
		if (rr != null) {
			Set<Entry<String, Attribute>> entries = rr.entrySet();
			Iterator<Entry<String, Attribute>> itEntries = entries.iterator();
			ArrayList<String> result = new ArrayList<String>();
			while (itEntries.hasNext()) {
				Map.Entry<java.lang.String, org.graffiti.attributes.Attribute> entry = (Map.Entry<java.lang.String, org.graffiti.attributes.Attribute>) itEntries
						.next();
				if (entry.getKey().startsWith(pattern)) {
					result.add(entry.getKey());
				}
			}
			return result;
		} else
			return new ArrayList<String>();
	}
}