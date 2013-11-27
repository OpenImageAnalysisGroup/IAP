/**
 * This class sets the attributes of Events
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.text.parser.ParseException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLEventAssignmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLEventHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Event_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds an event to the model
	 * 
	 * @param g
	 *        contains the data
	 * @param model
	 *        the data will be added to this model
	 * @param headline
	 *        indicates where the information will be written from
	 * @param internHeadline
	 *        intern representation of headline
	 */
	public void addEvent(Graph g, Model model, String internHeadline) {
		SBMLEventHelper eventHelper = new SBMLEventHelper(g, internHeadline);
		Event event = model.createEvent();
		addSBaseAttributes(event, g, internHeadline);
		if (eventHelper.isSetID()) {
			String id = eventHelper.getID();
			if (Event.isValidId(id, event.getLevel(), event.getVersion())) {
				event.setId(eventHelper.getID());
			}
		}
		if (eventHelper.isSetName()) {
			event.setName(eventHelper.getName());
		}
		if (eventHelper.isSetUseValuesFromTriggerTime()) {
			event.setUseValuesFromTriggerTime(eventHelper
					.getUseValuesFromTriggerTime());
		}
		
		// Trigger must be set
		Trigger trigger = event.createTrigger();
		if (eventHelper.isSetTriggerFunction()) {
			try {
				trigger.setMath(ASTNode.parseFormula(eventHelper
						.getTriggerFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (eventHelper.isSetInitialValue()) {
			trigger.setInitialValue(eventHelper.getInitialValue());
		}
		if (eventHelper.isSetPersistent()) {
			trigger.setPersistent(eventHelper.getPersistent());
		}
		
		if (eventHelper.isSetPriorityFunction()) {
			Priority priority = event.createPriority();
			try {
				priority.setMath(ASTNode.parseFormula(eventHelper
						.getPriorityFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			addSBaseAttributes(priority, g, internHeadline);
		}
		
		if (eventHelper.isSetDelayFunction()) {
			Delay delay = event.createDelay();
			try {
				delay.setMath(ASTNode.parseFormula(eventHelper
						.getDelayFunction()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			addSBaseAttributes(delay, g, internHeadline);
		}
		
		String eventAssignmentVariable = "";
		int eventAssignmentCount = 1;
		// while(event.getNumEventAssignments() <= eventAssignmentCount){
		while (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.VARIABLE).toString())) {
			
			SBMLEventAssignmentHelper eaHelper = eventHelper
					.addEventAssignment(g, internHeadline, eventAssignmentCount);
			if (eaHelper.isSetVariable()) {
				eventAssignmentVariable = eaHelper.getVariable();
			}
			if ((!SBML_Constants.EMPTY.equals(eventAssignmentVariable))
					&& null != eventAssignmentVariable) {
				EventAssignment eventAssignment = event.createEventAssignment();
				addSBaseAttributes(eventAssignment, g, internHeadline);
				
				eventAssignment.setVariable(eventAssignmentVariable);
				if (eaHelper.isSetFunction()) {
					try {
						eventAssignment.setMath(ASTNode.parseFormula(eaHelper
								.getFunction()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			++eventAssignmentCount;
			eventAssignmentVariable = "";
		}
	}
	
}