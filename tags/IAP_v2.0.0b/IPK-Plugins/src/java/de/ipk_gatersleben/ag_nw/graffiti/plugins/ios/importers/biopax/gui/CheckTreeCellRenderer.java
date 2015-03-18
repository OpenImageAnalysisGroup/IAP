package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer
{
	private CheckTreeSelectionModel selectionModel;
	private TreeCellRenderer delegate;
	private TristateCheckBox checkBox = new TristateCheckBox();

	public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel)
	{
		this.delegate = delegate;
		this.selectionModel = selectionModel;
		setLayout(new BorderLayout());
		setOpaque(false);
		checkBox.setOpaque(false);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		TreePath path = tree.getPathForRow(row);
		if (path != null)
		{
			if (selectionModel.isPathSelected(path, true))
				checkBox.setState(TristateCheckBox.SELECTED);
			else
				checkBox.setState(selectionModel.isPartiallySelected(path) ? TristateCheckBox.DONT_CARE : TristateCheckBox.NOT_SELECTED);
		}
		removeAll();
		add(checkBox, BorderLayout.WEST);
		add(renderer, BorderLayout.CENTER);
		return this;
	}
}
