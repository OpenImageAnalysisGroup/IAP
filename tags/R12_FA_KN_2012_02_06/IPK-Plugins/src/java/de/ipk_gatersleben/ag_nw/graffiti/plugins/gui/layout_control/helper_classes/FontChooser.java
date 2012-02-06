/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

// FontChooser.java
// A font chooser that allows users to pick a font by name, size, style, and
// color. The color selection is provided by a JColorChooser pane. This
// dialog builds an AttributeSet suitable for use with JTextPane.
//

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.Displayable;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_settings.FontAttribute;

public class FontChooser extends JDialog implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	JColorChooser colorChooser;
	
	JComboBox fontName;
	
	JCheckBox fontBold, fontItalic;
	
	JTextField fontSize;
	
	JLabel previewLabel;
	
	SimpleAttributeSet attributes;
	
	Font newFont;
	
	Color newColor;
	
	private static FontChooser fc = null;
	
	public FontChooser(Frame parent, Font initFont, Color initColor) {
		super(parent, "Font Chooser", true);
		// setSize(600, 550);
		setBounds(parent.getX(), parent.getY(), 600, 550);
		attributes = new SimpleAttributeSet();
		
		// Make sure that any way the user cancels the window does the right
		// thing
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeAndCancel();
			}
		});
		
		// Start the long process of setting up our interface
		Container c = getContentPane();
		
		JPanel fontPanel = new JPanel();
		
		String[] fontnames = GraphicsEnvironment.getLocalGraphicsEnvironment()
							.getAvailableFontFamilyNames();
		fontName = new JComboBox(fontnames);
		
		fontName.addActionListener(this);
		fontSize = new JTextField(new Integer(initFont.getSize()).toString(), 4);
		fontSize.setHorizontalAlignment(SwingConstants.RIGHT);
		fontSize.addActionListener(this);
		fontSize.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}
			
			public void focusLost(FocusEvent e) {
				actionPerformed(null);
			}
		});
		
		fontBold = new JCheckBox("Bold");
		fontBold.setSelected(initFont.isBold());
		fontBold.addActionListener(this);
		fontItalic = new JCheckBox("Italic");
		fontItalic.setSelected(initFont.isItalic());
		fontItalic.addActionListener(this);
		
		fontPanel.add(fontName);
		fontPanel.add(new JLabel(" Size: "));
		fontPanel.add(fontSize);
		fontPanel.add(fontBold);
		fontPanel.add(fontItalic);
		
		c.add(fontPanel, BorderLayout.NORTH);
		
		// Set up the color chooser panel and attach a change listener so that
		// color updates get reflected in our preview label.
		colorChooser = new JColorChooser(initColor);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updatePreviewColor();
			}
		});
		c.add(colorChooser, BorderLayout.CENTER);
		
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewLabel = new JLabel("Here's a sample of this font.");
		previewLabel.setForeground(colorChooser.getColor());
		previewPanel.add(previewLabel, BorderLayout.CENTER);
		
		// Add in the Ok and Cancel buttons for our dialog box
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeAndSave();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				closeAndCancel();
			}
		});
		
		JPanel controlPanel = new JPanel();
		controlPanel.add(okButton);
		controlPanel.add(cancelButton);
		previewPanel.add(controlPanel, BorderLayout.SOUTH);
		
		// Give the preview label room to grow.
		previewPanel.setMinimumSize(new Dimension(100, 100));
		previewPanel.setPreferredSize(new Dimension(100, 100));
		
		c.add(previewPanel, BorderLayout.SOUTH);
		updateFontAndColor(initFont, initColor);
	}
	
	// Ok, something in the font changed, so figure that out and make a
	// new font for the preview label
	public void actionPerformed(ActionEvent ae) {
		// Check the name of the font
		if (!StyleConstants.getFontFamily(attributes).equals(
							fontName.getSelectedItem())) {
			StyleConstants.setFontFamily(attributes, (String) fontName
								.getSelectedItem());
		}
		// Check the font size (no error checking yet)
		if (StyleConstants.getFontSize(attributes) != Integer.parseInt(fontSize
							.getText())) {
			StyleConstants.setFontSize(attributes, Integer.parseInt(fontSize
								.getText()));
		}
		// Check to see if the font should be bold
		if (StyleConstants.isBold(attributes) != fontBold.isSelected()) {
			StyleConstants.setBold(attributes, fontBold.isSelected());
		}
		// Check to see if the font should be italic
		if (StyleConstants.isItalic(attributes) != fontItalic.isSelected()) {
			StyleConstants.setItalic(attributes, fontItalic.isSelected());
		}
		// and update our preview label
		updatePreviewFont();
	}
	
	// Get the appropriate font from our attributes object and update
	// the preview label
	protected void updatePreviewFont() {
		String name = StyleConstants.getFontFamily(attributes);
		boolean bold = StyleConstants.isBold(attributes);
		boolean ital = StyleConstants.isItalic(attributes);
		int size = StyleConstants.getFontSize(attributes);
		
		// Bold and italic donâ€™t work properly in beta 4.
		Font f = new Font(name,
							(bold ? Font.BOLD : 0) + (ital ? Font.ITALIC : 0), size);
		previewLabel.setFont(f);
	}
	
	// Get the appropriate color from our chooser and update previewLabel
	protected void updatePreviewColor() {
		previewLabel.setForeground(colorChooser.getColor());
		// Manually force the label to repaint
		previewLabel.repaint();
	}
	
	public Font getNewFont() {
		return newFont;
	}
	
	public Color getNewColor() {
		return newColor;
	}
	
	public AttributeSet getAttributes() {
		return attributes;
	}
	
	public void closeAndSave() {
		// Save font & color information
		newFont = previewLabel.getFont();
		newColor = previewLabel.getForeground();
		
		// Close the window
		setVisible(false);
	}
	
	public void closeAndCancel() {
		// Erase any font information and then close the window
		newFont = null;
		newColor = null;
		setVisible(false);
	}
	
	public static FontChooser showFontChooser(Displayable disp) {
		if (fc == null)
			fc = new FontChooser(
								GravistoService.getInstance().getMainFrame(),
								((FontAttribute) disp).getFont(),
								((FontAttribute) disp).getColor());
		else {
			fc.updateFontAndColor(
								((FontAttribute) disp).getFont(),
								((FontAttribute) disp).getColor()
								);
		}
		fc.setVisible(true);
		return fc;
	}
	
	private void updateFontAndColor(Font initFont, Color color) {
		for (int i = 0; i < fontName.getItemCount(); i++) {
			if (fontName.getItemAt(i).equals(initFont.getName()))
				fontName.setSelectedIndex(i);
		}
		fontSize.setText(new Integer(initFont.getSize()).toString());
		fontBold.setSelected(initFont.isBold());
		fontItalic.setSelected(initFont.isItalic());
		colorChooser.setColor(color);
		updatePreviewColor();
		updatePreviewFont();
		actionPerformed(null);
	}
}