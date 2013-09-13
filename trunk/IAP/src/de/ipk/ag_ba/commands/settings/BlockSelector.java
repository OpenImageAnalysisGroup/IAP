package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.BlockType;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.FolderPanel;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class BlockSelector {
	
	private final String title, desc;
	private final boolean firstStepTypeSelection;
	
	public BlockSelector(boolean firstStepTypeSelection, String title, String desc) {
		this.firstStepTypeSelection = firstStepTypeSelection;
		this.title = title;
		this.desc = desc;
	}
	
	public String getBlockSelection() {
		boolean modal = true;
		MyInputHelper.getInput("[" + (modal ? "" : "nonmodal") + "]<html>" + (desc == null ? "" : "<br>" + getDefaultDescription() + "<br><br>"), title,
				getAnalysisBlockTypes());
		return null;
	}
	
	private String getDefaultDescription() {
		if (firstStepTypeSelection)
			return "Select the analysis block type:";
		else
			return "Select the analysis block:";
	}
	
	private Object[] getAnalysisBlockTypes() {
		Object[] res = new Object[BlockType.values().length * 2];
		int i = 0;
		for (BlockType bt : BlockType.values()) {
			res[i++] = "";
			res[i++] = getBlockSelectionButton(bt);
		}
		return res;
	}
	
	private JPanel getBlockSelectionButton(BlockType bt) {
		JButton res = new JButton();
		String sizetags = "<br>";
		res.setText("<html>" + sizetags + "<b>" + bt.getName() + sizetags + sizetags);
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean closeDialogBeforeExecution = true;
				if (closeDialogBeforeExecution)
					FolderPanel.closeParentDialog((JButton) e.getSource());
				
				// select block somehow...
			}
		});
		res.setToolTipText("Show analysis blocks belonging to group " + bt.getName().toLowerCase());
		return TableLayout.getSplitVertical(res, null, TableLayout.PREFERRED, 5);
	}
	
}
