package placement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RectangleDrawerFrame extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	enum Algorithm {
		ACTIVESET, MOSEK, FSA, ACTIVESET_SPLIT
	}
	
	RectangleDrawerFrame(String title) {
		super(title);
	}
	
	Algorithm algorithm = Algorithm.ACTIVESET_SPLIT;
	boolean completeConstraints = false;
	boolean orthogonalOrderingConstraints = false;
	boolean animate = false;
	
	public static void main(String args[]) {
		final RectangleDrawerFrame f = new RectangleDrawerFrame("Rectangle Drawer");
		Box hBox1 = Box.createHorizontalBox();
		Box hBox2 = Box.createHorizontalBox();
		Box vBox = Box.createVerticalBox();
		final RectangleDrawerPanel d = new RectangleDrawerPanel();
		d.setSize(new Dimension(800, 600));
		d.setBackground(Color.WHITE);
		JButton cleanupButton = new JButton("Remove Overlaps");
		JButton clearButton = new JButton("Clear");
		JButton undoButton = new JButton("Undo");
		JButton loadButton = new JButton("Load");
		JButton saveButton = new JButton("Save");
		JButton randomButton = new JButton("Random");
		JButton printButton = new JButton("Print");
		final JTextField xGapField = new JTextField("5");
		xGapField.setMaximumSize(new Dimension(100, 30));
		final JTextField yGapField = new JTextField("5");
		yGapField.setMaximumSize(new Dimension(100, 30));
		JCheckBox completeConstraintsCB = new JCheckBox("n^2");
		JCheckBox orthogonalConstraintsCB = new JCheckBox("OO");
		JCheckBox animateCB = new JCheckBox("Animate");
		final JRadioButton activeSetRB = new JRadioButton("AS");
		activeSetRB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				f.algorithm = Algorithm.ACTIVESET;
			}
			
		});
		
		final JRadioButton activeSetSplitRB = new JRadioButton("AS'");
		activeSetSplitRB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				f.algorithm = Algorithm.ACTIVESET_SPLIT;
			}
			
		});
		
		final JRadioButton mosekRB = new JRadioButton("QP");
		mosekRB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				f.algorithm = Algorithm.MOSEK;
			}
			
		});
		mosekRB.setEnabled(false);
		final JRadioButton fsaRB = new JRadioButton("FSA");
		fsaRB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				f.algorithm = Algorithm.FSA;
			}
			
		});
		ButtonGroup bg = new ButtonGroup();
		bg.add(activeSetRB);
		bg.add(activeSetSplitRB);
		bg.add(mosekRB);
		bg.add(fsaRB);
		switch (f.algorithm) {
			case ACTIVESET:
				activeSetRB.setSelected(true);
				break;
			case ACTIVESET_SPLIT:
				activeSetSplitRB.setSelected(true);
				break;
			case MOSEK:
				mosekRB.setSelected(true);
				break;
			case FSA:
				fsaRB.setSelected(true);
				break;
		}
		completeConstraintsCB.setSelected(f.completeConstraints);
		orthogonalConstraintsCB.setSelected(f.orthogonalOrderingConstraints);
		animateCB.setSelected(f.animate);
		hBox2.add(cleanupButton);
		hBox2.add(xGapField);
		hBox2.add(yGapField);
		hBox1.add(clearButton);
		hBox1.add(randomButton);
		hBox1.add(undoButton);
		hBox1.add(loadButton);
		hBox1.add(saveButton);
		hBox1.add(printButton);
		// hBox.add(completeConstraintsCB);
		hBox2.add(orthogonalConstraintsCB);
		hBox2.add(animateCB);
		hBox2.add(activeSetRB);
		hBox2.add(activeSetSplitRB);
		hBox2.add(mosekRB);
		hBox2.add(fsaRB);
		vBox.add(d);
		vBox.add(hBox1);
		vBox.add(hBox2);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(vBox);
		f.setSize(700, 700);
		f.setVisible(true);
		cleanupButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				d.backup();
				int xGap = Integer.parseInt(xGapField.getText());
				int yGap = Integer.parseInt(yGapField.getText());
				if (f.algorithm == Algorithm.FSA) {
					PFS r = new PFS(xGap, yGap);
					r.place(d.getRectangles());
				} else {
					QPRectanglePlacement r = new QPRectanglePlacement(
										f.algorithm == Algorithm.ACTIVESET_SPLIT,
										f.completeConstraints,
										f.orthogonalOrderingConstraints,
										f.algorithm == Algorithm.MOSEK, xGap, yGap,
										f.animate);
					if (f.animate) {
						r.addObserver(d);
					}
					d.rectangleColourMap = new Hashtable<Rectangle2D, Color>();
					r.place(d.getRectangles(), d.rectangleColourMap);
				}
				d.fitToScreen();
				d.repaint();
			}
			
		});
		clearButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				d.backup();
				d.clear();
			}
			
		});
		undoButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				d.undo();
				d.repaint();
			}
			
		});
		loadButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				BlocksFileFilter ff = d.getFileFilter();
				JFileChooser chooser = new JFileChooser(".");
				chooser.addChoosableFileFilter(ff);
				chooser.setFileFilter(ff);
				if (ff.lastSelectedFile != null) {
					chooser.setSelectedFile(ff.lastSelectedFile);
				}
				int returnVal = chooser.showOpenDialog(f);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					d.load(f);
				}
			}
		});
		randomButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				d.generateRandom();
			}
		});
		printButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				PrinterJob printJob = PrinterJob.getPrinterJob();
				printJob.setPrintable(d);
				@SuppressWarnings("unused")
				PageFormat pf = printJob.pageDialog(printJob.defaultPage());
				if (printJob.printDialog()) {
					try {
						printJob.print();
					} catch (Exception ex) {
					}
				}
			}
		});
		saveButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				BlocksFileFilter ff = d.getFileFilter();
				JFileChooser chooser = new JFileChooser(".");
				chooser.setFileFilter(ff);
				if (ff.lastSelectedFile != null) {
					chooser.setSelectedFile(ff.lastSelectedFile);
				}
				int returnVal = chooser.showSaveDialog(f);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					ObjectOutput output = null;
					String path = chooser.getSelectedFile().getPath();
					if (!path.endsWith(".blocks")) {
						path = path + ".blocks";
					}
					File file = new File(path);
					try {
						// use buffering
						OutputStream buffer = new BufferedOutputStream(
											new FileOutputStream(file));
						output = new ObjectOutputStream(buffer);
						output.writeObject(d.getRectangles());
					} catch (IOException ex) {
						ex.printStackTrace();
					} finally {
						try {
							if (output != null) {
								// flush and close "output" and its underlying
								// streams
								output.close();
								ff.lastSelectedFile = file;
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		completeConstraintsCB.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				f.completeConstraints = f.completeConstraints ? false : true;
			}
			
		});
		orthogonalConstraintsCB.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				f.orthogonalOrderingConstraints = f.orthogonalOrderingConstraints ? false : true;
			}
			
		});
		animateCB.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent arg0) {
				f.animate = f.animate ? false : true;
			}
			
		});
		if (args.length > 0) {
			System.out.println("Arg " + args[0]);
			d.load(new File(args[0]));
			cleanupButton.getActionListeners()[0].actionPerformed(null);
		}
	}
	
}
