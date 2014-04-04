package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.ImageAnalysisBlock;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.FolderPanel;
import org.MarkComponent;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

/**
 * @author klukas
 */
public class BlockSelector {
	
	private final String title, desc;
	private final TextReceiver resultReceiver;
	private final BlockType blockType;
	private final ImageAnalysisBlock currentSelection;
	
	public BlockSelector(BlockType blockType, String title, String desc, TextReceiver resultReceiver, ImageAnalysisBlock currentSelection) {
		this.blockType = blockType;
		this.title = title;
		this.desc = desc;
		this.resultReceiver = resultReceiver;
		this.currentSelection = currentSelection;
	}
	
	public void showDialog() {
		boolean modal = true;
		MyInputHelper.getInput("[" + (modal ? "" : "nonmodal") + "]<html>"
				+ (desc == null ? "" : "<br>" + getDefaultDescription() + "<br><br>"),
				title,
				getAnalysisBlockTypes());
	}
	
	private String getDefaultDescription() {
		return desc;
	}
	
	private Object[] getAnalysisBlockTypes() {
		int n = 0;
		for (ImageAnalysisBlock bl : IAPpluginManager.getInstance().getKnownAnalysisBlocks()) {
			if (bl.getBlockType() == null)
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Block " + bl.getClass().getCanonicalName() + " doesn't specify its block type.");
			if (bl.getBlockType() == blockType) {
				n++;
			}
		}
		Object[] res = new Object[n * 2];
		int i = 0;
		
		for (ImageAnalysisBlock bl : IAPpluginManager.getInstance().getKnownAnalysisBlocks()) {
			if (bl.getBlockType() == blockType) {
				res[i++] = "<html>" + getBlockDescriptionAnnotation("", bl);
				if (currentSelection != null) {
					MarkComponent mc = new MarkComponent(getBlockSelectionButton(bl),
							bl.getClass().getCanonicalName().equals(currentSelection.getClass().getCanonicalName()), TableLayout.FILL, false, 5);
					res[i++] = mc;
				} else
					res[i++] = getBlockSelectionButton(bl);
			}
		}
		return res;
	}
	
	private JPanel getBlockSelectionButton(final ImageAnalysisBlock inst) {
		JButton res = new JButton();
		style(res);
		res.setText("<html><table><tr>"
				+ "<td><b>" + inst.getName() + "</b><br>" +
				"<font color='gray' size=-2>" + StringManipulationTools.getWordWrap(inst.getDescription(), 60) + "</font>"
				+ "</td></table>");
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean closeDialogBeforeExecution = true;
				if (closeDialogBeforeExecution)
					FolderPanel.closeParentDialog((JButton) e.getSource());
				resultReceiver.setText(inst.getClass().getCanonicalName());
			}
		});
		res.setHorizontalAlignment(SwingConstants.LEFT);
		res.setToolTipText(null);
		return TableLayout.getSplitVertical(res, null, TableLayout.PREFERRED, 5);
	}
	
	public static void style(JButton button) {
		button.setForeground(Color.BLACK);
		button.setBackground(Color.WHITE);
		Border line = new LineBorder(Color.BLACK);
		Border margin = new EmptyBorder(5, 15, 5, 15);
		Border compound = new CompoundBorder(line, margin);
		button.setBorder(compound);
	}
	
	public static String getBlockDescriptionAnnotation(String inp, ImageAnalysisBlock inst) {
		String res = "";
		for (String inf : inp.split("//")) {
			String gs = "<font color='green'>";
			String ge = "</font>";
			String rs = "<font color='red'>";
			String re = "</font>";
			String ns = "<font color='gray'>";
			String ne = "</font>";
			String is = "<font color='blue'>";
			String ie = "</font>";
			
			String vi = gs + (inst.getCameraInputTypes() != null && inst.getCameraInputTypes().contains(CameraType.VIS) ? "&#9632;" : "&#9633;") + ge;
			String fi = rs + (inst.getCameraInputTypes() != null && inst.getCameraInputTypes().contains(CameraType.FLUO) ? "&#9632;" : "&#9633;") + re;
			String ni = ns + (inst.getCameraInputTypes() != null && inst.getCameraInputTypes().contains(CameraType.NIR) ? "&#9632;" : "&#9633;") + ne;
			String ii = is + (inst.getCameraInputTypes() != null && inst.getCameraInputTypes().contains(CameraType.IR) ? "&#9632;" : "&#9633;") + ie;
			
			String vo = gs + (inst.getCameraInputTypes() != null && inst.getCameraOutputTypes().contains(CameraType.VIS) ? "&#9632;" : "&#9633;") + ge;
			String fo = rs + (inst.getCameraInputTypes() != null && inst.getCameraOutputTypes().contains(CameraType.FLUO) ? "&#9632;" : "&#9633;") + re;
			String no = ns + (inst.getCameraInputTypes() != null && inst.getCameraOutputTypes().contains(CameraType.NIR) ? "&#9632;" : "&#9633;") + ne;
			String io = is + (inst.getCameraInputTypes() != null && inst.getCameraOutputTypes().contains(CameraType.IR) ? "&#9632;" : "&#9633;") + ie;
			
			res += "<table border='0'><tr>" +
					"<td>" + getBlockTypeAnnotation(inst.getBlockType()) + "</td>" +
					"<td>" + inf
					+ "</td><td><font color='gray' size='-2'><code>"
					+ " IN &#9656; " + vi + " " + fi + " " + ni + " " + ii + ""
					+ "<br> OUT&#9656; " + vo + " " + fo + " " + no + " " + io + "</code></font></td></tr></table>";
		}
		return res;
	}
	
	public static String getBlockTypeAnnotation(BlockType bt) {
		String type = "<span style=\"background-color:" + bt.getColor() + "\">";
		if (!type.isEmpty())
			type += "<font color='gray' size='-4'>" +
					"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br>" +
					"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font></span>";
		
		return type;
	}
}
