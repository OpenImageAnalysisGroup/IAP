/*
 * Created on Jun 10, 2005
 * @author Rafael Santos (rafael.santos@lac.inpe.br)
 * Part of the Java Advanced Imaging Stuff site
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI)
 * STATUS: Complete, but could be improved, for example, with:
 * - Plotting more than one band of the histogram.
 * - Considering the minimum number of pixels in a bin.
 * - Customization as a JavaBean.
 * Redistribution and usage conditions must be done under the
 * Creative Commons license:
 * English: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.en
 * Portuguese: http://creativecommons.org/licenses/by-nc-sa/2.0/br/deed.pt
 * More information on design and applications are on the projects' page
 * (http://www.lac.inpe.br/~rafael.santos/Java/JAI).
 */
package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.media.jai.Histogram;
import javax.swing.JComponent;

/**
 * This class displays a histogram (instance of Histogram) as a component. Only
 * the first histogram band ins considered for plotting. The component has a
 * tooltip which displays the bin index and bin count for the bin under the
 * mouse cursor.
 */
public class DisplayHistogram extends JComponent implements MouseMotionListener {
	private static final long serialVersionUID = 1L;
	// The histogram and its title.
	private Histogram histogramR;
	private String title;
	// Some data and hints for the histogram plot.
	private int[] countsR;
	private int[] countsG;
	private int[] countsB;
	private double maxCount;
	private int indexMultiplier = 1;
	private int skipIndexes = 8;
	// The components' dimensions.
	private int width, height = 200;
	// Some constants for this component.
	private int verticalTicks = 5;
	private Insets border = new Insets(15, 35, 10, 10);
	private int binWidth = 3;
	private Color backgroundColor = Color.BLACK;
	private Color barColorR = new Color(255, 170, 170, 120);
	private Color barColorG = new Color(170, 255, 170, 120);
	private Color barColorB = new Color(170, 170, 255, 120);
	private Color marksColor = new Color(200, 200, 200);
	private Font fontSmall = new Font("monospaced", 0, 10);
	private Font fontLarge = new Font("default", Font.ITALIC, 20);
	
	/**
	 * The constructor for this class, which will set its fields' values and get
	 * some information about the histogram.
	 * 
	 * @param histogram
	 *           the histogram to be plotted.
	 * @param title
	 *           the title of the plot.
	 */
	public DisplayHistogram(Histogram histogramR, Histogram histogramG, Histogram histogramB, String title) {
		this.title = title;
		init(histogramR, histogramG, histogramB);
		addMouseMotionListener(this);
	}
	
	/**
	 * Override the default bin width (for plotting)
	 */
	public void setBinWidth(int newWidth) {
		binWidth = newWidth;
		width = histogramR.getNumBins(0) * binWidth;
	}
	
	/**
	 * Override the default height for the plot.
	 * 
	 * @param h
	 *           the new height.
	 */
	public void setHeight(int h) {
		height = h;
	}
	
	/**
	 * Override the index multiplying factor (for bins with width != 1)
	 */
	public void setIndexMultiplier(int i) {
		indexMultiplier = i;
	}
	
	/**
	 * Override the index skipping factor (determines how many labels will be
	 * printed on the index axis).
	 */
	public void setSkipIndexes(int i) {
		skipIndexes = i;
	}
	
	/**
	 * Set the maximum value (used to scale the histogram y-axis). The default
	 * value is defined in the constructor and can be overriden with this method.
	 */
	public void setMaxCount(int m) {
		maxCount = m;
	}
	
	/**
	 * This method informs the maximum size of this component, which will be the
	 * same as the preferred size.
	 */
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	/**
	 * This method informs the minimum size of this component, which will be the
	 * same as the preferred size.
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	/**
	 * This method informs the preferred size of this component, which will be
	 * constant.
	 */
	public Dimension getPreferredSize() {
		return new Dimension(width + border.left + border.right, height
				+ border.top + border.bottom);
	}
	
	/**
	 * This method will paint the component.
	 */
	protected void paintComponent(Graphics g) {
		try {
			Graphics2D g2d = (Graphics2D) g;
			// Draw the background.
			g2d.setColor(backgroundColor);
			g2d.fillRect(0, 0, getSize().width, getSize().height);
			// Draw some marks.
			g2d.setColor(marksColor);
			// g2d.drawRect(border.left, border.top, width, height);
			// Draw the histogram bars.
			
			for (int bin = 0; bin < histogramR.getNumBins(0); bin++) {
				int x = border.left + bin * binWidth;
				
				for (int i = 0; i < 3; i++) {
					
					g2d.setColor(barColorB);
					double barStarts = border.top + height * (maxCount - countsB[bin]) / (1. * maxCount);
					double barEnds = Math.ceil(height * countsB[bin] / (1. * maxCount));
					g2d.fillRect(x, (int) barStarts, binWidth, (int) barEnds);
					
					g2d.setColor(barColorR);
					barStarts = border.top + height * (maxCount - countsR[bin]) / (1. * maxCount);
					barEnds = Math.ceil(height * countsR[bin] / (1. * maxCount));
					g2d.fillRect(x, (int) barStarts, binWidth, (int) barEnds);
					
					g2d.setColor(barColorG);
					barStarts = border.top + height * (maxCount - countsG[bin]) / (1. * maxCount);
					barEnds = Math.ceil(height * countsG[bin] / (1. * maxCount));
					g2d.fillRect(x, (int) barStarts, binWidth, (int) barEnds);
				}
			}
			// Draw the values on the horizontal axis. We will plot only 1/8th of
			// them.
			g2d.setColor(marksColor);
			g2d.setFont(fontSmall);
			FontMetrics metrics = g2d.getFontMetrics();
			int halfFontHeight = metrics.getHeight() / 2;
			for (int bin = 0; bin <= histogramR.getNumBins(0); bin++) {
				if (bin % skipIndexes == 0) {
					String label = "" + (indexMultiplier * bin);
					while (label.length() < 4)
						label = " " + label;
					label = reverse(label);
					int textHeight = metrics.stringWidth(label); // remember it
					// will be rotated!
					// int x = border.left+bin*binWidth+binWidth/2;
					g2d.translate(border.left + bin * binWidth + halfFontHeight,
							border.top + height + textHeight / 2 + 2);
					g2d.rotate(-Math.PI / 2);
					g2d.drawString(label, 0, 0);
					g2d.rotate(Math.PI / 2);
					g2d.translate(-(border.left + bin * binWidth + halfFontHeight),
							-(border.top + height + textHeight / 2 + 2));
				}
			}
			// Draw the values on the vertical axis. Let's draw only some of them.
			double step = (int) (maxCount / verticalTicks);
			for (int l = 0; l <= verticalTicks; l++) // last will be done separately
			{
				String label;
				if (l == verticalTicks)
					label = "" + (int) maxCount;
				else
					label = "" + (int) (l * step);
				int textWidth = metrics.stringWidth(label);
				g2d.drawString(label, border.left - 2 - textWidth, border.top + height
						- l * (height / verticalTicks));
			}
			// Draw the title.
			g2d.setFont(fontLarge);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			metrics = g2d.getFontMetrics();
			int textWidth = metrics.stringWidth(title);
			g2d.drawString(title,
					(border.left + width + border.right - textWidth) / 2, 28);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String reverse(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);
		
		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}
	
	/**
	 * This method does not do anything, it is here to keep the
	 * MouseMotionListener interface happy.
	 */
	public void mouseDragged(MouseEvent e) {
	}
	
	/**
	 * This method will be called when the mouse is moved over the component. It
	 * will set the tooltip text on the component to show the histogram data.
	 */
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		// Don't show anything out of the plot region.
		if ((x > border.left) && (x < border.left + width) && (y > border.top)
				&& (y < border.top + height)) {
			// Convert the X to an index on the histogram.
			x = (x - border.left) / binWidth;
			int yR = countsR[x];
			int yG = countsG[x];
			int yB = countsB[x];
			setToolTipText("<html><code>" +
					"Intensity " + (indexMultiplier * x * 2) + "<br>" +
							"Red&nbsp;&nbsp;: " + yR + "<br>" +
							"Green: " + yG + "<br>" +
							"Blue&nbsp;: " + yB);
		} else {
			setToolTipText(null);
		}
	}
	
	/**
	 * @param histogram2
	 */
	public void init(Histogram histogramR, Histogram histogramG, Histogram histogramB) {
		this.histogramR = histogramR;
		// Calculate the components dimensions.
		width = histogramR.getNumBins(1) * binWidth;
		// Get the histogram data.
		countsR = histogramR.getBins(0);
		countsG = histogramG.getBins(1);
		countsB = histogramB.getBins(2);
		// Get the max and min counts.
		maxCount = Integer.MIN_VALUE;
		for (int c = 0; c < countsR.length; c++) {
			maxCount = Math.max(maxCount, countsR[c]);
			maxCount = Math.max(maxCount, countsG[c]);
			maxCount = Math.max(maxCount, countsB[c]);
		}
		
	}
	
	/**
	 * @param color
	 */
	public void setBG(Color color) {
		backgroundColor = color;
	}
	
} // end class