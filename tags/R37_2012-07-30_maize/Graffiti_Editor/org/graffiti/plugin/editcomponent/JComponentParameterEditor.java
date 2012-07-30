package org.graffiti.plugin.editcomponent;

import java.util.Collection;

import javax.swing.JComponent;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.plugin.Displayable;

public class JComponentParameterEditor implements ValueEditComponent {
	
	private Displayable disp;
	
	public JComponentParameterEditor(Displayable disp) {
		this.disp = disp;
	}
	
	public JComponent getComponent() {
		return (JComponent) disp.getValue();
	}
	
	public Displayable getDisplayable() {
		return disp;
	}
	
	public boolean getShowEmpty() {
		return false;
	}
	
	public boolean isEnabled() {
		return true;
	}
	
	public void setDisplayable(Displayable disp) {
		this.disp = disp;
	}
	
	public void setEditFieldValue() {
		// empty
	}
	
	public void setEnabled(boolean enabled) {
		// empty
	}
	
	public void setShowEmpty(boolean showEmpty) {
		// empty
	}
	
	public void setValue() {
		// empty
	}
	
	public void postAttributeAdded(AttributeEvent e) {
		// empty
	}
	
	public void postAttributeChanged(AttributeEvent e) {
		// empty
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeAdded(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeChanged(AttributeEvent e) {
		//
		
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
		//
		
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		//
		
	}
	
	public void transactionStarted(TransactionEvent e) {
		//
		
	}
	
	public void postDirectedChanged(EdgeEvent e) {
		//
		
	}
	
	public void postEdgeReversed(EdgeEvent e) {
		//
		
	}
	
	public void postSourceNodeChanged(EdgeEvent e) {
		//
		
	}
	
	public void postTargetNodeChanged(EdgeEvent e) {
		//
		
	}
	
	public void preDirectedChanged(EdgeEvent e) {
		//
		
	}
	
	public void preEdgeReversed(EdgeEvent e) {
		//
		
	}
	
	public void preSourceNodeChanged(EdgeEvent e) {
		//
		
	}
	
	public void preTargetNodeChanged(EdgeEvent e) {
		//
		
	}
	
	public void postEdgeAdded(GraphEvent e) {
		//
		
	}
	
	public void postEdgeRemoved(GraphEvent e) {
		//
		
	}
	
	public void postGraphCleared(GraphEvent e) {
		//
		
	}
	
	public void postNodeAdded(GraphEvent e) {
		//
		
	}
	
	public void postNodeRemoved(GraphEvent e) {
		//
		
	}
	
	public void preEdgeAdded(GraphEvent e) {
		//
		
	}
	
	public void preEdgeRemoved(GraphEvent e) {
		//
		
	}
	
	public void preGraphCleared(GraphEvent e) {
		//
		
	}
	
	public void preNodeAdded(GraphEvent e) {
		//
		
	}
	
	public void preNodeRemoved(GraphEvent e) {
		//
		
	}
	
	public void postInEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void postInEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void postOutEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void postOutEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void postUndirectedEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void postUndirectedEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void preInEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void preInEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void preOutEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void preOutEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void preUndirectedEdgeAdded(NodeEvent e) {
		//
		
	}
	
	public void preUndirectedEdgeRemoved(NodeEvent e) {
		//
		
	}
	
	public void setValue(Collection<Displayable> attributes) {
		for (Displayable d : attributes) {
			setDisplayable(d);
			setValue();
		}
	}
	
	public void setParameter(String setting, Object value) {
	}
}
