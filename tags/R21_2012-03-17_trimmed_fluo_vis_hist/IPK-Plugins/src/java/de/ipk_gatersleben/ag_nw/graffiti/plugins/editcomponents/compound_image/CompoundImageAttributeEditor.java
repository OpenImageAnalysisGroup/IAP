/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.compound_image;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.jdesktop.swingx.graphics.GraphicsUtilities;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.CachedWebDownload;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class CompoundImageAttributeEditor extends AbstractValueEditComponent {
	protected JLabel imageContainer;
	
	protected JTextField compoundNumber;
	
	private static final String IMAGE_NA = "Image N/A";
	
	public CompoundImageAttributeEditor(Displayable disp) {
		super(disp);
		compoundNumber = new JTextField();
		compoundNumber.setToolTipText("Edit text to use a different compound structure image");
		String val = disp.getValue().toString();
		compoundNumber.setText(val);
		updateGraphicComponent(CompoundImageAttributeComponent.checkAndChangePath(val, (Attribute) displayable));
	}
	
	private static HashSet<String> knownInvalidUrls = new HashSet<String>();
	
	public static JLabel getCompoundImageComponent(JLabel currentInstance, String imgName, boolean acceptNULLreturn) {
		return getCompoundImageComponent(currentInstance, imgName, acceptNULLreturn, -1, -1);
	}
	
	@SuppressWarnings("deprecation")
	public static JLabel getCompoundImageComponent(
						JLabel currentInstance,
						String imgName,
						boolean acceptNULLreturn, int sizex, int sizey) {
		
		ImageIcon graphicComponent = null;
		if (!knownInvalidUrls.contains(imgName)) {
			try {
				String cachedFileName;
				if (imgName != null && imgName.length() > 0 && (!imgName.contains("/")) && (!imgName.contains("\\") && (!imgName.contains(".")))) {
					if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
						cachedFileName = CachedWebDownload.getCacheURL(new URL("http://www.genome.ad.jp/Fig/compound_small/" + imgName + ".gif"), imgName + ".gif",
											"compound_image").toExternalForm();
					else
						cachedFileName = null;
				} else {
					cachedFileName = CachedWebDownload.getCacheURL(new URL(imgName), CachedWebDownload.getFileIdFromUrl(imgName), "image").toExternalForm();
				}
				BufferedImage bi = GraphicsUtilities.loadCompatibleImage(new URL(cachedFileName));
				graphicComponent = new ImageIcon(bi);
			} catch (NullPointerException npe) {
				graphicComponent = null;
			} catch (MalformedURLException e) {
				graphicComponent = null;
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
				graphicComponent = null;
			}
			if (graphicComponent == null) {
				try {
					BufferedImage bi = GraphicsUtilities.loadCompatibleImage(new File(imgName).toURL());
					graphicComponent = new ImageIcon(bi);
					// graphicComponent = new ImageIcon(imgName);
				} catch (Exception errrr) {
					graphicComponent = null;
				}
			}
			if (graphicComponent == null) {
				try {
					graphicComponent = new ImageIcon(new URL(imgName));
				} catch (MalformedURLException mfu) {
					graphicComponent = null;
				} catch (NullPointerException npe3) {
					graphicComponent = null;
				}
			}
		}
		if (currentInstance == null) {
			currentInstance = new JLabel();
		}
		if (graphicComponent != null) {
			try {
				while (graphicComponent.getImageLoadStatus() == MediaTracker.LOADING) {
					Thread.sleep(20);
				}
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
			
			if (sizex > 0 && sizey > 0)
				graphicComponent = new ImageIcon(GravistoService.getScaledImage(graphicComponent.getImage(), sizex, sizey));
			
			currentInstance.setIcon(graphicComponent);
			
			currentInstance.setPreferredSize(new Dimension(graphicComponent.getIconWidth(), graphicComponent.getIconHeight()));
			currentInstance.setText(null);
		} else {
			knownInvalidUrls.add(imgName);
			if (!acceptNULLreturn) {
				// currentInstance.setText(IMAGE_NA);
				currentInstance.setIcon(new ImageIcon(GravistoService.getResource(CompoundImageAttributeEditor.class, "MissingImage", "png")));
			} else
				return null;
		}
		return currentInstance;
	}
	
	private void updateGraphicComponent(String imgName) {
		imageContainer = getCompoundImageComponent(imageContainer, imgName, false, 128, 128);
	}
	
	protected static String checkImageName(String imgName) {
		if (imgName != null) {
			if (imgName.startsWith("cpd:"))
				imgName = imgName.substring("cpd:".length());
			
			CompoundEntry ce = CompoundService.getInformation(imgName);
			if (ce != null) {
				return ce.getID() + ".gif";
			}
			if (imgName.startsWith("C") && !imgName.contains(".")) {
				imgName = imgName + ".gif";
			}
		}
		return imgName;
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplitVertical(imageContainer, compoundNumber,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED);
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			imageContainer.setIcon(null);
			imageContainer.setText(EMPTY_STRING);
			compoundNumber.setText(EMPTY_STRING);
		} else {
			updateGraphicComponent(CompoundImageAttributeComponent.checkAndChangePath(displayable.getValue().toString(), (Attribute) displayable));
		}
	}
	
	public void setValue() {
		String text = compoundNumber.getText();
		if (!text.equals(EMPTY_STRING) && !text.equals(IMAGE_NA)
							&& !this.displayable.getValue().toString().equals(text)) {
			this.displayable.setValue(text);
		}
	}
}
