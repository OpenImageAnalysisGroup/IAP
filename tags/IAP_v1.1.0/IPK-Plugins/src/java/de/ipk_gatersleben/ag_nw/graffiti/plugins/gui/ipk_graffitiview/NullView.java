/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 23.11.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.AWTEvent;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.MessageListener;
import org.graffiti.plugin.view.View;

public class NullView implements View {
	
	private String status = "";
	private JComponent viewComponent = initViewComponent();
	private Graph graph;
	private JButton myButton;
	
	public void setAttributeComponentManager(AttributeComponentManager acm) {
	}
	
	private JComponent initViewComponent() {
		TableLayout tl = new TableLayout(
							new double[][] {
												{ 20, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL },
												{ 20, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL }
				});
		
		JButton myButton = new JButton(getButtonText());
		this.myButton = myButton;
		JPanel result = new JPanel();
		result.setLayout(tl);
		result.add(myButton, "1,1");
		return result;
	}
	
	private String getButtonText() {
		if (graph != null) {
			return "<html>NullView:<br>" +
								"Graph Name :" + graph.getName() + "<br>" +
								"Status : " + status;
		} else {
			return "NullView: No graph assigned!";
		}
	}
	
	private void updateButtonText() {
		if (myButton != null) {
			myButton.setText(getButtonText());
			myButton.invalidate();
		}
	}
	
	public Map<?, ?> getComponentElementMap() {
		return new HashMap<Object, Object>();
	}
	
	public GraphElementComponent getComponentForElement(GraphElement ge) {
		return null;
	}
	
	public void setGraph(Graph graph) {
		this.graph = graph;
		updateButtonText();
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public JComponent getViewComponent() {
		return viewComponent;
	}
	
	public String getViewName() {
		return "Null View";
	}
	
	public void addMessageListener(MessageListener ml) {
	}
	
	public void close() {
	}
	
	public void completeRedraw() {
	}
	
	public void removeMessageListener(MessageListener ml) {
	}
	
	public void repaint(GraphElement ge) {
	}
	
	public void postEdgeAdded(GraphEvent e) {
	}
	
	public void postEdgeRemoved(GraphEvent e) {
	}
	
	public void postGraphCleared(GraphEvent e) {
	}
	
	public void postNodeAdded(GraphEvent e) {
	}
	
	public void postNodeRemoved(GraphEvent e) {
	}
	
	public void preEdgeAdded(GraphEvent e) {
	}
	
	public void preEdgeRemoved(GraphEvent e) {
	}
	
	public void preGraphCleared(GraphEvent e) {
	}
	
	public void preNodeAdded(GraphEvent e) {
	}
	
	public void preNodeRemoved(GraphEvent e) {
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall s) {
		status = "Transaction Finished";
		updateButtonText();
	}
	
	public void transactionStarted(TransactionEvent e) {
		status = "Transaction Running";
		updateButtonText();
	}
	
	public void postInEdgeAdded(NodeEvent e) {
	}
	
	public void postInEdgeRemoved(NodeEvent e) {
	}
	
	public void postOutEdgeAdded(NodeEvent e) {
	}
	
	public void postOutEdgeRemoved(NodeEvent e) {
	}
	
	public void postUndirectedEdgeAdded(NodeEvent e) {
	}
	
	public void postUndirectedEdgeRemoved(NodeEvent e) {
	}
	
	public void preInEdgeAdded(NodeEvent e) {
	}
	
	public void preInEdgeRemoved(NodeEvent e) {
	}
	
	public void preOutEdgeAdded(NodeEvent e) {
	}
	
	public void preOutEdgeRemoved(NodeEvent e) {
	}
	
	public void preUndirectedEdgeAdded(NodeEvent e) {
	}
	
	public void preUndirectedEdgeRemoved(NodeEvent e) {
	}
	
	public void postDirectedChanged(EdgeEvent e) {
	}
	
	public void postEdgeReversed(EdgeEvent e) {
	}
	
	public void postSourceNodeChanged(EdgeEvent e) {
	}
	
	public void postTargetNodeChanged(EdgeEvent e) {
	}
	
	public void preDirectedChanged(EdgeEvent e) {
	}
	
	public void preEdgeReversed(EdgeEvent e) {
	}
	
	public void preSourceNodeChanged(EdgeEvent e) {
	}
	
	public void preTargetNodeChanged(EdgeEvent e) {
	}
	
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	public Insets getAutoscrollInsets() {
		return null;
	}
	
	public void autoscroll(Point cursorLocn) {
	}
	
	public void zoomChanged(AffineTransform newZoom) {
	}
	
	public AffineTransform getZoom() {
		return null;
	}
	
	public boolean putInScrollPane() {
		return true;
	}
	
	public Set<AttributeComponent> getAttributeComponentsForElement(
						GraphElement ge) {
		return new HashSet<AttributeComponent>();
	}
	
	public JComponent getViewToolbarComponentTop() {
		return null;
	}
	
	public JComponent getViewToolbarComponentBottom() {
		return null;
	}
	
	public JComponent getViewToolbarComponentLeft() {
		return null;
	}
	
	public JComponent getViewToolbarComponentRight() {
		return null;
	}
	
	public JComponent getViewToolbarComponentBackground() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.View#closing(java.awt.event.WindowEvent)
	 */
	public void closing(AWTEvent e) {
		// empty
	}
	
	public boolean worksWithTab(InspectorTab tab) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.Zoomable#redrawActive()
	 */
	public boolean redrawActive() {
		return false;
	}
}
