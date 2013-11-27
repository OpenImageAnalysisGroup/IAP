package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.ImageAnalysisBlock;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.FolderPanel;
import org.MarkComponent;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * @author klukas
 */
public class BlockTypeSelector {
	
	private final String title, desc;
	private final TextReceiver resultReceiver;
	private final ImageAnalysisBlock currentSelection;
	
	public BlockTypeSelector(String title, String desc, TextReceiver resultReceiver, ImageAnalysisBlock currentSelection) {
		this.title = title;
		this.desc = desc;
		this.resultReceiver = resultReceiver;
		this.currentSelection = currentSelection;
	}
	
	public void showDialog() {
		boolean modal = true;
		MyInputHelper.getInput("[" + (modal ? "" : "nonmodal") + "]<html>" + (desc == null ? "" : "<br>"
				+ getDefaultDescription() + "<br><br>"), title,
				getAnalysisBlockTypes());
	}
	
	private String getDefaultDescription() {
		return desc;
	}
	
	private Object[] getAnalysisBlockTypes() {
		Object[] res = new Object[BlockType.values().length * 2];
		int i = 0;
		for (BlockType bt : BlockType.values()) {
			res[i++] = "<html>" + BlockSelector.getBlockTypeAnnotation(bt);
			if (currentSelection != null) {
				MarkComponent mc = new MarkComponent(getBlockSelectionButton(bt),
						bt == currentSelection.getBlockType(), TableLayout.FILL, false, 5);
				res[i++] = mc;
			} else
				res[i++] = getBlockSelectionButton(bt);
		}
		return res;
	}
	
	private JPanel getBlockSelectionButton(final BlockType bt) {
		JButton res = new JButton();
		String sizetags = "<br>";
		res.setText("<html>" + sizetags + "<b>" + bt.getName() + sizetags + sizetags);
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean closeDialogBeforeExecution = true;
				if (closeDialogBeforeExecution)
					FolderPanel.closeParentDialog((JButton) e.getSource());
				new BlockSelector(bt, "Select " + bt.getName() + " Analysis Block",
						"Select the desired " + bt.getName().toLowerCase() + " analysis block:",
						resultReceiver, currentSelection).showDialog();
			}
		});
		BlockSelector.style(res);
		res.setToolTipText("Show analysis blocks belonging to group " + bt.getName().toLowerCase());
		return TableLayout.getSplitVertical(res, null, TableLayout.PREFERRED, 5);
	}
}
