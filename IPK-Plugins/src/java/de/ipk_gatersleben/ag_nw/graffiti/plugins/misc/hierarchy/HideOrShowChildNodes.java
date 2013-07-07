package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

public class HideOrShowChildNodes extends AbstractAlgorithm
					implements ProvidesNodeContextMenu {
	
	public enum HideOrShowOperation {
		showAllChildren, showDirectChildren, hideAllChildren;
		
		@Override
		public String toString() {
			switch (this) {
				case showAllChildren:
					return "Show all child nodes";
				case hideAllChildren:
					return "Hide all child nodes";
				case showDirectChildren:
					return "Show direct child nodes";
				default:
					return "unknown enum constant!";
			}
		}
	}
	
	private HideOrShowOperation modeOfOperation;
	
	public JMenuItem[] getCurrentNodeContextMenuItem(
						Collection<Node> selectedNodes) {
		if (selectedNodes == null || selectedNodes.size() < 1) {
			return null;
		} else {
			JMenuItem showAllChildren, showDirectChildren, hideAllChildren;
			showAllChildren = new JMenuItem(getShowAllChildrenAction());
			showDirectChildren = new JMenuItem(getShowDirectChildrenAction());
			hideAllChildren = new JMenuItem(getHideAllChildrenAction());
			
			showAllChildren.setText(HideOrShowOperation.showAllChildren.toString());
			showDirectChildren.setText(HideOrShowOperation.showDirectChildren.toString());
			hideAllChildren.setText(HideOrShowOperation.hideAllChildren.toString());
			
			return new JMenuItem[] {
								showAllChildren,
								showDirectChildren,
								hideAllChildren };
		}
	}
	
	private Action getHideAllChildrenAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String toString() {
				return HideOrShowOperation.hideAllChildren.toString();
			}
			
			public void actionPerformed(ActionEvent arg0) {
				execute(HideOrShowOperation.hideAllChildren);
			}
		};
	}
	
	protected void execute(HideOrShowOperation modeOfOperation) {
		this.modeOfOperation = modeOfOperation;
		startAlgorithm();
	}
	
	private void startAlgorithm() {
		GravistoService.getInstance().algorithmAttachData(this);
		try {
			this.check();
			this.execute();
		} catch (PreconditionException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		this.reset();
	}
	
	private Action getShowDirectChildrenAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String toString() {
				return HideOrShowOperation.showDirectChildren.toString();
			}
			
			public void actionPerformed(ActionEvent arg0) {
				execute(HideOrShowOperation.showDirectChildren);
			}
		};
	}
	
	private Action getShowAllChildrenAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public String toString() {
				return HideOrShowOperation.showAllChildren.toString();
			}
			
			public void actionPerformed(ActionEvent arg0) {
				execute(HideOrShowOperation.showAllChildren);
			}
		};
	}
	
	public void execute() {
		try {
			graph.getListenerManager().transactionStarted(this);
			switch (modeOfOperation) {
				case showAllChildren:
					AttributeHelper.setVisibilityOfChildElements(selection.getNodes(), false);
					break;
				
				case showDirectChildren:
					HashSet<GraphElement> childElements = new HashSet<GraphElement>();
					HashSet<Node> nodes = new HashSet<Node>();
					nodes.addAll(selection.getNodes());
					for (Node n : selection.getNodes()) {
						for (Edge e : n.getDirectedOutEdges()) {
							if (nodes.contains(e.getTarget()) && nodes.contains(e.getSource()))
								continue;
							childElements.add(e);
							childElements.add(e.getTarget());
						}
						for (Edge e : n.getUndirectedEdges()) {
							if (nodes.contains(e.getTarget()) && nodes.contains(e.getSource()))
								continue;
							childElements.add(e);
							childElements.add(e.getSource());
							childElements.add(e.getTarget());
						}
					}
					childElements.removeAll(selection.getNodes());
					AttributeHelper.setHidden(childElements, false);
					break;
				
				case hideAllChildren:
					AttributeHelper.setVisibilityOfChildElements(selection.getNodes(), true);
					break;
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	public String getName() {
		return "Change Visibility of Child-Nodes";
	}
	
	@Override
	public String getCategory() {
		return null;// "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command you may change the visibility of<br>" +
							"child-nodes in a hierarchy. This command processes<br>" +
							"the currently selected nodes.<br><br>" +
							"Hint: Especially when making nodes visibile again, you<br>" +
							"should re-apply the desired layout. Previously hidden<br>" +
							"nodes may be shown initially in inappropriate positions.<br><br>";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (selection == null || selection.getNodes().size() < 1)
			throw new PreconditionException("Please select at least one graph node!");
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new ObjectListParameter(HideOrShowOperation.hideAllChildren,
							"Mode of operation",
							"Select the desired mode of operation",
							HideOrShowOperation.values()) };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		this.modeOfOperation = (HideOrShowOperation) ((ObjectListParameter) params[i++]).getValue();
	}
	
}
