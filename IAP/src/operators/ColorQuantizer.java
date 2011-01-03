/*
 * Created on June 15, 2005
 * @author Rafael Santos (rafael.santos@lac.inpe.br)
 * Part of the Java Advanced Imaging Stuff site
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI)
 * STATUS: Complete
 * Redistribution and usage conditions must be done under the
 * Creative Commons license:
 * English: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.en
 * Portuguese: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.pt
 * More information on design and applications are on the projects' page
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI).
 */
package operators;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import display.DisplayTwoSynchronizedImages;

/**
 * This class demonstrates the use of the colorquantize operator. It allows the
 * user to choose parameters and number of colors and to see the results.
 */
@SuppressWarnings("restriction")
public class ColorQuantizer extends JFrame implements ActionListener {
	private PlanarImage image; // the original image
	private DisplayTwoSynchronizedImages display; // the display component
	private JComboBox method; // the quantization method checkbox.
	private JComboBox colors; // the number of colors checkbox.
	
	/**
	 * The constructor of the class creates the user interface and registers
	 * the event listeners.
	 * 
	 * @param filename
	 *           the file name of the image (we'll use it on the title bar)
	 * @param image
	 *           the PlanarImage to be quantized
	 */
	public ColorQuantizer(String filename, PlanarImage image) {
		super("Interactive color quantization of image " + filename);
		this.image = image;
		// Set the content pane's layout
		getContentPane().setLayout(new BorderLayout());
		// Create and set the image display component
		display = new DisplayTwoSynchronizedImages(image, image);
		getContentPane().add(display, BorderLayout.CENTER);
		// Create a small control panel.
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// Create the quantization method combo box.
		controlPanel.add(new JLabel("Quantization Method"));
		method = new JComboBox(new String[] { "Median-Cut", "NeuQuant", "Oct-Tree" });
		method.addActionListener(this);
		controlPanel.add(method);
		controlPanel.add(new JLabel("   "));
		// Create the number of colors combo box.
		controlPanel.add(new JLabel("Number of Colors"));
		colors = new JComboBox(new String[] { "2", "4", "8", "16", "32", "64", "128" });
		colors.addActionListener(this);
		controlPanel.add(colors);
		// Add the control panel to the frame
		getContentPane().add(controlPanel, BorderLayout.NORTH);
		// Set the closing operation so the application is finished.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack(); // adjust the frame size using preferred dimensions.
		setVisible(true); // show the frame.
		// Simulate an event just to binarize the image.
		actionPerformed(null);
	}
	
	/**
	 * This method will be executed when the "automatic" button is pushed.
	 */
	public void actionPerformed(ActionEvent e) {
		// We get the threshold using histograms and the minimum fuzziness method.
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(image);
		switch (method.getSelectedIndex()) // quantization algorithm
		{
			case 0:
				pb.add(ColorQuantizerDescriptor.MEDIANCUT);
				break;
			case 1:
				pb.add(ColorQuantizerDescriptor.NEUQUANT);
				break;
			case 2:
				pb.add(ColorQuantizerDescriptor.OCTTREE);
				break;
		}
		pb.add(Integer.parseInt((String) colors.getSelectedItem()));
		switch (method.getSelectedIndex()) // upper-bound, algorithm-dependent.
		{
			case 0:
				pb.add(32768);
				break;
			case 1:
				pb.add(100);
				break;
			case 2:
				pb.add(65536);
				break;
		}
		pb.add(null); // the ROI
		pb.add(1);
		pb.add(1); // the period
		// Calculate the quantized image.
		PlanarImage quantizedImage = JAI.create("colorquantizer", pb);
		// Change the UI
		display.setImage2(quantizedImage);
	}
	
	/**
	 * The application entry point.
	 * 
	 * @param args
	 *           the command line arguments.
	 */
	public static void main(String[] args) {
		// We need one argument: the image filename.
		if (args.length != 1) {
			System.err.println("Usage: java operators.ColorQuantizer image");
			System.exit(0);
		}
		// Read the image.
		PlanarImage image = JAI.create("fileload", args[0]);
		// Create the GUI and start the application.
		new ColorQuantizer(args[0], image);
	}
	
} // end class