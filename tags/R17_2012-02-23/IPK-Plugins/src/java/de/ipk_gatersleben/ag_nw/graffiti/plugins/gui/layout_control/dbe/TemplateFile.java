package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JButton;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;

public class TemplateFile {
	
	private final String title;
	private final URL url;
	
	public TemplateFile(String title, URL url, RunnableForFile openAfterSaving) {
		this.title = title;
		this.url = url;
	}
	
	public URL getURL() {
		return url;
	}
	
	public JButton getButton() {
		JButton tempBT = new JButton(title);
		tempBT.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				String targetFileName = FileHelper.getFileName("xls", "\"" + title + "\" Template");
				if (targetFileName == null || targetFileName.length() <= 0) {
					MainFrame.showMessageDialog("Input Template \"" + title + "\" not saved!", "Operation aborted");
					return;
				}
				
				try {
					
					File tgt = new File(targetFileName);
					InputStream in = getURL().openStream();
					FileOutputStream out = new FileOutputStream(tgt);
					
					int b;
					long sz = 0;
					while ((b = in.read()) != -1) {
						out.write(b);
						sz++;
					}
					sz = sz / 1024;
					
					in.close();
					out.close();
					// if (true||checkBoxOpen.isSelected()) {
					MainFrame.showMessage("Input Template saved as " + targetFileName + " (" + sz + " kb), open file with system default application...",
										MessageType.INFO);
					AttributeHelper.showInBrowser(tgt.toURL().toString());
					// } else
					// MainFrame.showMessageDialog("Input Template saved as "+targetFileName+" ("+sz+" kb)", "Input Template Created");
				} catch (NullPointerException err) {
					MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
					ErrorMsg.addErrorMessage(err);
				} catch (FileNotFoundException err) {
					MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
					ErrorMsg.addErrorMessage(err);
				} catch (IOException err) {
					MainFrame.showMessageDialog("Error: " + err.getLocalizedMessage(), "Could not save file");
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		return tempBT;
	}
	
}
