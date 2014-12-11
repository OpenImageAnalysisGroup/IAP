package application;

import info.clearthought.layout.TableLayout;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ReleaseInfo;

/**
 * @author Christian Klukas
 */
public class FXsplash extends JFrame {
	
	public FXsplash(int width, int height, JComponent mainGUI, boolean undecorated) {
		init(width, height, mainGUI, undecorated);
	}
	
	private void init(int width, int height, JComponent mainGUI, boolean undecorated) {
		setBackground(Color.BLACK);
		JComponent contentPane = new JPanel();
		if (undecorated) {
			setResizable(undecorated);
			setUndecorated(undecorated);
			setType(Type.UTILITY);
			contentPane.setLayout(TableLayout.getLayout(width, height));
			setAlwaysOnTop(true);
		} else {
			contentPane.setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		}
		// setBackground(Color.BLACK);
		setTitle("IAP V" + ReleaseInfo.IAP_VERSION_STRING);
		
		// content pane
		// contentPane.setBackground(Color.WHITE);
		// contentPane.setBorder(new LineBorder(Color.BLACK, 1));
		// contentPane.setBackground(null);
		
		contentPane.add(mainGUI, "0,0");
		
		setContentPane(contentPane);
		setSize(width, height);
		pack();
		setSize(width, height);
		
	}
	
}
