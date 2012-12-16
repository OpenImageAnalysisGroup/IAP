package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.ImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class BlCutZoomedImages extends AbstractBlock {
	
	boolean debugValues = true;
	
	private double[][] zoomLevels;
	
	@Override
	protected void prepare() {
		super.prepare();
		String zoomID = "zoom-top:";
		if (options.getCameraPosition() == CameraPosition.SIDE)
			zoomID = "zoom-side:";
		
		zoomLevels = getFillGradeFromOutlierString(zoomID);
		
		if (debugValues) {
			if (input().images().vis() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.VIS, input(), getProperties(), options, getBlockPosition());
			if (input().images().nir() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.NIR, input(), getProperties(), options, getBlockPosition());
			if (input().images().ir() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.IR, input(), getProperties(), options, getBlockPosition());
		}
	}
	
	private static void debugIt(final Class blockType, final FlexibleImageType inp,
			final FlexibleMaskAndImageSet input,
			final BlockResultSet brs, final ImageProcessorOptions options,
			final int blockPos) {
		FlexibleImage vis = input.images().getImage(inp);
		FlexibleImageSet in = input.images().copy();
		FlexibleImage visFluo = vis.io().copy().crossfade(in.fluo().copy().resize(vis.getWidth(), vis.getHeight()), 0.5d).getImage();
		final ZoomedImage ic = new ZoomedImage(visFluo.getAsBufferedImage());
		final JScrollPane jsp = new JScrollPane(ic);
		jsp.setBorder(BorderFactory.createLoweredBevelBorder());
		
		JButton okButton = new JButton();
		final JTextField textField = new JTextField("");
		textField.setText(input.images().getImageInfo(inp).getParentSample().getParentCondition().getExperimentHeader().getGlobalOutlierInfo());
		
		okButton.setAction(new AbstractAction("Set Value & Refresh View") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					input.images().getImageInfo(inp).getParentSample().getParentCondition().getExperimentHeader().setGlobalOutlierInfo(textField.getText());
					
					ImageAnalysisBlockFIS inst = (ImageAnalysisBlockFIS) blockType.newInstance();
					FlexibleImageSet a = input.images().copy();
					FlexibleImageSet b = input.masks().copy();
					FlexibleMaskAndImageSet ab = new FlexibleMaskAndImageSet(a, b);
					((BlCutZoomedImages) inst).debugValues = false;
					inst.setInputAndOptions(ab, options, brs, blockPos, null);
					ab = inst.process();
					FlexibleImageSet in = ab.images();
					
					int vs = jsp.getVerticalScrollBar().getValue();
					int hs = jsp.getHorizontalScrollBar().getValue();
					FlexibleImage selImage = ab.images().getImage(inp);
					if (selImage == null)
						throw new Exception("Input image not available");
					ImageOperation visFluo = selImage.io().copy()
							// .multiplicateImageChannelsWithFactors(new double[] { 0.4, 0.4, 0.4 })
							.crossfade(
									in.fluo().copy()
											.resize(selImage.getWidth(), selImage.getHeight()),
									in.vis() != null && in.vis() != selImage ? in.vis().copy()
											.resize(selImage.getWidth(), selImage.getHeight()) : null);
					
					ic.setImage(visFluo.getAsBufferedImage());
					jsp.setViewportView(ic);
					jsp.revalidate();
					jsp.getVerticalScrollBar().setValue(vs);
					jsp.getHorizontalScrollBar().setValue(hs);
					textField.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
					MainFrame.showMessageDialog("Error: " + e.getMessage(), "Error");
				}
			}
		});
		
		JComponent editAndUpdate = TableLayout.get3Split(
				textField,
				null,
				TableLayout.get3Split(
						ic.getZoomSlider(), null, okButton, TableLayout.PREFERRED, 5, TableLayout.PREFERRED),
				TableLayout.FILL, 5, TableLayout.PREFERRED);
		JComponent v = TableLayout.get3SplitVertical(
				jsp, null, editAndUpdate, TableLayout.FILL, 5, TableLayout.PREFERRED);
		v.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		MainFrame.showMessageWindow("Vis & Fluo", v);
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		if (image == null)
			return image;
		int w = 1624;
		int h = 1234;
		if (image.getWidth() < image.getHeight()) {
			w = 1234;
			h = 1624;
		}
		return cut(image.io().adjustWidthHeightRatio(w, h, 10));
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		return cut(mask.io().adjustWidthHeightRatio(1624, 1234, 10));
	}
	
	private FlexibleImage cut(ImageOperation img) {
		double zoom = Double.NaN;
		double zoomY = Double.NaN;
		double offX = Double.NaN;
		double offY = Double.NaN;
		switch (img.getType()) {
			case VIS:
				zoom = zoomLevels[0][0];
				offX = zoomLevels[1][0];
				offY = zoomLevels[2][0];
				zoomY = zoomLevels.length >= 4 && zoomLevels[3].length >= 1 ? zoomLevels[3][0] : zoom;
				break;
			case FLUO:
				zoom = zoomLevels[0].length >= 2 ? zoomLevels[0][1] : 1;
				offX = zoomLevels[1].length >= 2 ? zoomLevels[1][1] : 0;
				offY = zoomLevels[2].length >= 2 ? zoomLevels[2][1] : 0;
				zoomY = zoomLevels.length >= 4 && zoomLevels[3].length >= 2 ? zoomLevels[3][1] : zoom;
				break;
			case NIR:
				zoom = zoomLevels[0].length >= 3 ? zoomLevels[0][2] : 1;
				offX = zoomLevels[1].length >= 3 ? zoomLevels[1][2] : 0;
				offY = zoomLevels[2].length >= 3 ? zoomLevels[2][2] : 0;
				zoomY = zoomLevels.length >= 4 && zoomLevels[3].length >= 3 ? zoomLevels[3][2] : zoom;
				break;
			case IR:
				zoom = zoomLevels[0].length >= 4 ? zoomLevels[0][3] : 1;
				offX = zoomLevels[1].length >= 4 ? zoomLevels[1][3] : 0;
				offY = zoomLevels[2].length >= 4 ? zoomLevels[2][3] : 0;
				zoomY = zoomLevels.length >= 4 && zoomLevels[3].length >= 4 ? zoomLevels[3][3] : zoom;
				break;
		}
		System.out.println("ZOOM: " + zoom + " // X = " + offX + " // Y = " + offY + " // ZOOM Y: " + zoomY);
		// add border or cut outside
		int verticalTooTooMuch = (int) ((1d - zoom) * img.getHeight());
		int b = -verticalTooTooMuch / 2;
		return img
				.scale(1, zoomY, false)
				.addBorder(b, (int) (b / 2d + offX), (int) (b / 2d + offY), ImageOperation.BACKGROUND_COLORint)
				.getImage();
	}
	
	/**
	 * Example: zoom-top:75;75;75;75 ==> carrier fills 75 percent of VIS;FLUO;NIR;IR images
	 */
	private double[][] getFillGradeFromOutlierString(String zoomID) {
		ImageData i = input().images().getVisInfo();
		if (i == null)
			i = input().images().getFluoInfo();
		if (i == null)
			i = input().images().getNirInfo();
		String outlierDef = i.getParentSample().getParentCondition().getExperimentGlobalOutlierInfo();
		if (outlierDef != null && outlierDef.contains(zoomID)) {
			for (String s : outlierDef.split("//")) {
				s = s.trim();
				if (s.startsWith(zoomID)) {
					s = s.substring(zoomID.length());
					String[] levels = s.split(";");
					double[][] res = new double[4][4];
					res[0][0] = levels.length > 0 ? Double.parseDouble(levels[0].split(":")[0]) / 100d : 1d;
					res[0][1] = levels.length > 1 ? Double.parseDouble(levels[1].split(":")[0]) / 100d : 1d;
					res[0][2] = levels.length > 2 ? Double.parseDouble(levels[2].split(":")[0]) / 100d : 1d;
					res[0][3] = levels.length > 3 ? Double.parseDouble(levels[3].split(":")[0]) / 100d : 1d;
					
					res[1][0] = levels.length > 0 ? Double.parseDouble(levels[0].split(":")[1]) : 0d;
					res[1][1] = levels.length > 1 ? Double.parseDouble(levels[1].split(":")[1]) : 0d;
					res[1][2] = levels.length > 2 ? Double.parseDouble(levels[2].split(":")[1]) : 0d;
					res[1][3] = levels.length > 3 ? Double.parseDouble(levels[3].split(":")[1]) : 0d;
					
					res[2][0] = levels.length > 0 ? Double.parseDouble(levels[0].split(":")[2]) : 0d;
					res[2][1] = levels.length > 1 ? Double.parseDouble(levels[1].split(":")[2]) : 0d;
					res[2][2] = levels.length > 2 ? Double.parseDouble(levels[2].split(":")[2]) : 0d;
					res[2][3] = levels.length > 3 ? Double.parseDouble(levels[3].split(":")[2]) : 0d;
					
					res[3][0] = levels.length > 0 && levels[0].split(":").length > 3 ? Double.parseDouble(levels[0].split(":")[3]) : 1d;
					res[3][1] = levels.length > 1 && levels[1].split(":").length > 3 ? Double.parseDouble(levels[1].split(":")[3]) : 1d;
					res[3][2] = levels.length > 2 && levels[2].split(":").length > 3 ? Double.parseDouble(levels[2].split(":")[3]) : 1d;
					res[3][3] = levels.length > 3 && levels[3].split(":").length > 3 ? Double.parseDouble(levels[3].split(":")[3]) : 1d;
					
					return res;
				}
			}
			return new double[][] { { 1d, 1d, 1d, 1d }, { 0d, 0d, 0d, 0d }, { 0d, 0d, 0d, 0d }, { 1d, 1d, 1d, 1d } };
		} else
			return new double[][] { { 1d, 1d, 1d, 1d }, { 0d, 0d, 0d, 0d }, { 0d, 0d, 0d, 0d }, { 1d, 1d, 1d, 1d } };
	}
}
