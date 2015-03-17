package de.ipk.ag_ba.gui.picture_gui.filter;

import info.clearthought.layout.TableLayout;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author klukas
 */
public class FilterGui {
	boolean valid = false;
	
	private JComponent filterGui() {
		TableLayout tl = new TableLayout(new double[][]
		{
				{ 5, TableLayout.PREFERRED, 5, 300, 5 },
				{
						5,
						TableLayout.PREFERRED,
						5,
						TableLayout.PREFERRED,
						2,
						TableLayout.PREFERRED,
						2,
						TableLayout.PREFERRED,
						5
				}
		});
		JPanel res = new JPanel(tl);
		
		res.add(new JLabel("Define Filter:"), "1,1");
		
		res.add(new JLabel("Condition:"), "1,3");
		res.add(new JLabel("Plant ID:"), "1,5");
		res.add(new JLabel("Rotation Angle (number):"), "1,7");
		
		JTextField inpCondition = new JTextField();
		JTextField inpPlantID = new JTextField();
		JTextField inpRotationAngle = new JTextField();
		inpRotationAngle.setInputVerifier(new NumericRangeInputVerifier());
		
		res.add(inpCondition, "3,3");
		res.add(inpPlantID, "3,5");
		res.add(inpRotationAngle, "3,7");
		
		res.validate();
		
		return TableLayout.get3SplitVertical(res, null,
				TableLayout.get3Split(null,
						TableLayout.get3Split(new JButton("Apply Filter"), null, new JButton("Show All (Unfiltered)"), TableLayout.PREFERRED, 5,
								TableLayout.PREFERRED),
						null, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL),
				TableLayout.PREFERRED, 5, TableLayout.PREFERRED);
	}
	
	private JFrame dialog;
	
	protected void init() {
		this.dialog = new JFrame("Filter View");
		this.dialog.setResizable(false);
		this.dialog.setLayout(TableLayout.getLayout(TableLayout.PREFERRED, TableLayout.PREFERRED));
		this.dialog.getContentPane().add(filterGui(), "0,0");
		this.dialog.pack();
		this.dialog.setSize(900, 500);
		this.dialog.setLocation(10, 10);
	}
	
	public void show() {
		valid = false;
		if (this.dialog == null) {
			init();
		}
		this.dialog.setVisible(true);
	}
	
	protected void close() {
		this.dialog.setVisible(false);
		this.dialog.dispose();
	}
	
	public static void main(String args[]) {
		FilterGui obj = new FilterGui();
		obj.init();
		obj.show();
		
	}
	
	private class NumericRangeInputVerifier extends InputVerifier {
		
		@Override
		public boolean verify(JComponent input) {
			JTextField textField = (JTextField) input;
			if (textField.getText().isEmpty()) {
				textField.setBackground(Color.WHITE);
				return true;
			}
			try {
				int a = Integer.parseInt(textField.getText());
				textField.setBackground(Color.WHITE);
				return a > -Integer.MAX_VALUE;
			} catch (Exception e) {
				textField.setBackground(Color.RED);
				return false;
			}
		}
	}
}
