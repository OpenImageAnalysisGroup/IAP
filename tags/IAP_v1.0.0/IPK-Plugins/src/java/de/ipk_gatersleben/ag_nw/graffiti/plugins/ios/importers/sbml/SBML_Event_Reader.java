/**
 * This class reads in Events
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;
import java.util.List;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Trigger;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLEventAssignmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLEventHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Event_Reader extends SBML_SBase_Reader {
	
	/**
	 * Adds an events to the graph tab
	 * 
	 * @param eventList
	 *        contains the events of the model
	 * @param g
	 *        is the data structure for reading in the information
	 */
	public void addEvents(List<Event> eventList, Graph g) {
		Iterator<Event> itEvent = eventList.iterator();
		int eventCount = 1;
		while (itEvent.hasNext()) {
			Event event = itEvent.next();
			
			String internHeadline = new StringBuffer(SBML_Constants.SBML_EVENT)
					.append(eventCount).toString();
			String presentedHeadline = new StringBuffer("SBML Event ").append(
					eventCount).toString();
			
			SBMLEventHelper eventHelper = new SBMLEventHelper(g,
					internHeadline, presentedHeadline);
			
			String eventID = event.getId();
			String eventName = event.getName();
			Boolean useValuesFromTriggerTime = false;
			if (event.isSetUseValuesFromTriggerTime()) {
				useValuesFromTriggerTime = event.getUseValuesFromTriggerTime();
			}
			Trigger trigger = event.getTrigger();
			String triggerFormula = "";
			try {
				if (trigger.isSetMath()) {
					if (null != trigger.getMath()) {
						triggerFormula = trigger.getMath().toFormula();
					}
				}
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			
			Boolean triggerInitialValue = false;
			Boolean triggerPersistent = false;
			try {
				if (trigger.isSetInitialValue())
					triggerInitialValue = trigger.getInitialValue();
				if (trigger.isSetPersistent())
					triggerPersistent = trigger.getPersistent();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			// Boolean triggerInitialValue = trigger.getInitialValue();
			// Boolean triggerPersistent = trigger.getPersistent();
			
			String prioritySBOTerm = "";
			String priorityMetaID = "";
			String priorityFormula = "";
			if (event.isSetPriority()) {
				Priority priority = event.getPriority();
				if (priority.isSetMetaId()) {
					priorityMetaID = priority.getMetaId();
				}
				if (priority.isSetSBOTerm()) {
					prioritySBOTerm = priority.getSBOTermID();
				}
				if (priority.isSetAnnotation()) {
					if (priority.getAnnotation().isSetRDFannotation()) {
						eventHelper.setPriorityAnnotation(priority
								.getAnnotation());
					}
					if (priority.getAnnotation().isSetNonRDFannotation()) {
						eventHelper.setPriorityNonRDFAnnotation(priority
								.getAnnotation().getNonRDFannotation());
					}
				}
				
				if (priority.isSetNotes()) {
					eventHelper.setPriorityNotes(priority.getNotesString(),
							priority.getNotes());
				}
				// addNotes(priority.getNotes(), priority.getNotesString(),g,
				// internHeadline, new StringBuffer(internHeadline).
				// append(SBML_Constants.PRIORITY_NOTES).toString());
				try {
					if (priority.isSetMath()) {
						if (null != priority.getMath()) {
							priorityFormula = priority.getMath().toFormula();
						}
					}
				} catch (SBMLException e) {
					e.printStackTrace();
				}
			}
			
			String delayMetaID = "";
			String delaySBOTerm = "";
			String delayFormula = "";
			if (event.isSetDelay()) {
				Delay delay = event.getDelay();
				if (delay.isSetMetaId()) {
					delayMetaID = delay.getMetaId();
				}
				if (delay.isSetSBOTerm()) {
					delaySBOTerm = delay.getSBOTermID();
				}
				
				if (delay.isSetAnnotation()) {
					if (delay.getAnnotation().isSetRDFannotation()) {
						eventHelper.setdelayAnnotation(delay.getAnnotation());
					}
					if (delay.getAnnotation().isSetNonRDFannotation()) {
						eventHelper.setdelayNonRDFAnnotation(delay
								.getAnnotation().getNonRDFannotation());
					}
				}
				
				// addNotes(delay.getNotes(), delay.getNotesString(),g,
				// internHeadline, new StringBuffer(internHeadline).
				// append(SBML_Constants.DELAY_NOTES).toString());
				
				if (delay.isSetNotes()) {
					eventHelper.setDelayNotes(delay.getNotesString(),
							delay.getNotes());
				}
				try {
					if (delay.isSetMath()) {
						if (null != delay.getMath()) {
							delayFormula = delay.getMath().toFormula();
						}
					}
				} catch (SBMLException e) {
					e.printStackTrace();
				}
			}
			if (event.isSetId()
					&& Event.isValidId(eventID, event.getLevel(),
							event.getVersion())) {
				eventHelper.setID(eventID);
			}
			if (event.isSetName()) {
				eventHelper.setName(eventName);
			}
			if (event.isSetMetaId()) {
				eventHelper.setMetaID(event.getMetaId());
			}
			if (event.isSetSBOTerm()) {
				eventHelper.setSBOTerm(event.getSBOTermID());
			}
			if (event.isSetAnnotation()) {
				if (event.getAnnotation().isSetRDFannotation()) {
					eventHelper.setAnnotation(event.getAnnotation());
				}
				if (event.getAnnotation().isSetNonRDFannotation()) {
					eventHelper.setNonRDFAnnotation(event.getAnnotation()
							.getNonRDFannotation());
				}
			}
			if (event.isSetUseValuesFromTriggerTime()) {
				eventHelper
						.setUseValuesFromTriggerTime(useValuesFromTriggerTime);
			}
			if (event.isSetNotes()) {
				eventHelper.setNotes(event.getNotesString(), event.getNotes());
			}
			if (trigger.isSetInitialValue()) {
				eventHelper.setInitialValue(triggerInitialValue);
			}
			if (trigger.isSetPersistent()) {
				eventHelper.setPersistent(triggerPersistent);
			}
			if (trigger.isSetMath()) {
				eventHelper.setTriggerFunction(triggerFormula);
			}
			if (!priorityFormula.equals("")) {
				eventHelper.setPriorityFunction(priorityFormula);
			}
			if (!priorityMetaID.equals("")) {
				eventHelper.setPriorityMetaID(priorityMetaID);
			}
			if (!prioritySBOTerm.equals("")) {
				eventHelper.setPrioritySBOTerm(prioritySBOTerm);
			}
			if (!delayFormula.equals("")) {
				eventHelper.setDelayFunction(delayFormula);
			}
			if (!delayMetaID.equals("")) {
				eventHelper.setDelayMetaID(delayMetaID);
			}
			if (!delaySBOTerm.equals("")) {
				eventHelper.setDelaySBOTerm(delaySBOTerm);
			}
			
			List<EventAssignment> listEventAssignment = event
					.getListOfEventAssignments();
			Iterator<EventAssignment> itEventAssignment = listEventAssignment
					.iterator();
			int eventAssignmentCount = 1;
			while (itEventAssignment.hasNext()) {
				EventAssignment eventAssignment = itEventAssignment.next();
				SBMLEventAssignmentHelper eaHelper = eventHelper
						.addEventAssignment(g, internHeadline,
								presentedHeadline, eventAssignmentCount);
				
				// initEventAssignmentNideIDs(presentedHeadline, internHeadline,
				// eventAssignmentCount);
				/*
				 * String keyEAVariable =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Event Assignment " + eventAssignmentCount + " Variable");
				 * String keyEAMetaId =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Event Assignment " + eventAssignmentCount + " Meta ID");
				 * String keyEASBOTerm =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Event Assignment " + eventAssignmentCount + " SBOTerm");
				 * String keyEAToolTip =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Event Assignment " + eventAssignmentCount + " ToolTip");
				 * String keyEAFormula =
				 * SBML_Constants.addToNiceIdList(presentedHeadline,
				 * "Event Assignment " + eventAssignmentCount + " Formula");
				 */
				
				String variable = eventAssignment.getVariable();
				String eventAssignmentFormula = "";
				try {
					if (eventAssignment.isSetMath()) {
						if (null != eventAssignment.getMath()) {
							eventAssignmentFormula = eventAssignment.getMath()
									.toFormula();
						}
					}
				} catch (SBMLException e) {
					e.printStackTrace();
				}
				
				if (eventAssignment.isSetVariable()) {
					eaHelper.setVariable(variable);
				}
				if (eventAssignment.isSetMath()) {
					eaHelper.setFunction(eventAssignmentFormula);
				}
				if (eventAssignment.isSetMetaId()) {
					eaHelper.setMetaID(eventAssignment.getMetaId());
				}
				if (eventAssignment.isSetSBOTerm()) {
					eaHelper.setSBOTerm(eventAssignment.getSBOTermID());
				}
				
				if (eventAssignment.isSetAnnotation()) {
					if (eventAssignment.getAnnotation().isSetRDFannotation()) {
						eaHelper.setAnnotation(eventAssignment.getAnnotation());
					}
					if (eventAssignment.getAnnotation().isSetNonRDFannotation()) {
						eaHelper.setNonRDFAnnotation(eventAssignment
								.getAnnotation().getNonRDFannotation());
					}
				}
				if (eventAssignment.isSetNotes()) {
					eaHelper.setNotes(eventAssignment.getNotesString(),
							eventAssignment.getNotes());
				}
				
				eventAssignmentCount++;
			}
			
			eventCount++;
		}
	}
	
}