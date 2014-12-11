package application;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * @author Christian Klukas
 */
public class FXsplash extends JFrame {
	
	public FXsplash(int width, int height, JComponent mainGUI) {
		init(width, height, mainGUI);
	}
	
	private void init(int width, int height, JComponent mainGUI) {
		setSize(width, height);
		setUndecorated(true);
		setType(Type.UTILITY);
		setBackground(Color.BLACK);
		
		// content pane
		JComponent contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new LineBorder(Color.BLACK, 1));
		contentPane.setBackground(null);
		contentPane.setLayout(TableLayout.getLayout(width, height));
		
		contentPane.add(mainGUI, "0,0");
		
		setContentPane(contentPane);
		
		pack();
		
		// center on display
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenDim.width - getWidth()) / 2,
				(screenDim.height - getHeight()) / 2);
	}
	
}
