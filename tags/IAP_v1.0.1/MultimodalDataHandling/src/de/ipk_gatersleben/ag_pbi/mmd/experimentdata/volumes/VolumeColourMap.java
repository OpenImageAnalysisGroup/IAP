/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JPanel;

import org.ErrorMsg;
import org.color.ColorUtil;
import org.graffiti.editor.GravistoService;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class VolumeColourMap {
	
	public static enum Colourmap {
		
		BLUEYELLOW("Blue-Yellow", "blueyellow.lut"),
		BLACKBODY("Blackbody", "blackbody.lut"),
		BRYHOT("Bryhot", "bryhot.lut"),
		CARDIAC("Cardiac", "cardiac.lut"),
		GRAY("Gray", "gray.lut"),
		JET256("Jet 256", "jet256.lut"),
		PROPERHOT("Properhot", "properhot.lut"),
		COLOURS16("16 colours", "colours16.lut"),
		COLOURS6("6 colours", "colours6.lut"),
		COLOURS6_ALT("6 other colours", "colours6_alt.lut");
		
		private String name;
		private String filename;
		
		private Colourmap(String name, String filename) {
			this.name = name;
			this.filename = filename;
		}
		
		private String getFileName() {
			return filename;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
		public String getAsString() {
			return getStringColourArray(getAsArray());
		}
		
		public static int[][] getColourArrayFromString(String cmap) {
			int[][] colourArray = new int[4][256];
			try {
				for (int j = 0; j < 256; j++) {
					colourArray[0][j] = Integer.parseInt(cmap.substring(0, cmap.indexOf(' ')));
					cmap = cmap.substring(cmap.indexOf(' ') + 1);
					colourArray[1][j] = Integer.parseInt(cmap.substring(0, cmap.indexOf(' ')));
					cmap = cmap.substring(cmap.indexOf(' ') + 1);
					colourArray[2][j] = Integer.parseInt(cmap.substring(0, cmap.indexOf(' ')));
					cmap = cmap.substring(cmap.indexOf(' ') + 1);
					colourArray[3][j] = Integer.parseInt(cmap.substring(0, cmap.indexOf('|')));
					cmap = cmap.substring(cmap.indexOf('|') + 1);
				}
			} catch (Exception e) {
				return colourArray = createMonoColourMap(100, 100, 100);
			}
			return colourArray;
		}
		
		public static String getStringColourArray(int[][] cmap) {
			String colourArray = "";
			for (int j = 0; j < 256; j++) {
				colourArray += cmap[0][j] + " ";
				colourArray += cmap[1][j] + " ";
				colourArray += cmap[2][j] + " ";
				colourArray += cmap[3][j] + "|";
			}
			return colourArray;
		}
		
		/**
		 * Opens up one of the pre prepared lookup tables which are simple text
		 * files
		 * containing 3 columns of data (for R, G and B), with the row number
		 * being the
		 * corresponding gray value. These values are taken and put into a 2D
		 * array.
		 * 
		 * @param cmap_number
		 *           The number of the colourmap in the list.
		 * @return The colourmap in an array.
		 * @throws FileNotFoundException
		 *            The file couldn't be found at the location.
		 * @throws IOException
		 *            Signals that an I/O exception has occurred.
		 */
		public int[][] getAsArray() {
			
			int[][] colourArray = new int[4][256];
			int gCount = 0;
			int rgbCount = 0;
			
			// parse the map
			try {
				TextFile tf = new TextFile(GravistoService.getResource(this.getClass(), this.getFileName()));
				for (String line : tf) {
					StringTokenizer st = new StringTokenizer(line);
					
					// ARGB: we set the alpha value, which is not part of the ".lut"-files
					colourArray[rgbCount++][gCount] = getStandardTransferFunction(gCount);
					
					while (st.hasMoreTokens()) {
						String s = st.nextToken();
						Integer ic = new Integer(s);
						int icol = ic.intValue();
						colourArray[rgbCount][gCount] = icol;
						rgbCount++;
					}
					rgbCount = 0;
					gCount++;
				}
			} catch (IOException e) {
				System.out.println("Colourmap-file couldn't be found, setting standard coloring");
				colourArray = getColourArrayFromString(getDefaultColourMap(false));
			}
			return colourArray;
		}
		
		public static int[][] createMonoColourMap(Color c) {
			return createMonoColourMap(c.getRed(), c.getGreen(), c.getBlue());
		}
		
		/**
		 * Creates a mono "colour map", which has the same colour for all gray
		 * values.
		 * 
		 * @param rr
		 *           The red-component of the colour
		 * @param gg
		 *           The green-component of the colour
		 * @param bb
		 *           The blue-component of the colour
		 * @return The colourmap in an array.
		 */
		public static int[][] createMonoColourMap(int rr, int gg, int bb) {
			int[][] colourArray = new int[4][256];
			for (int i = 0; i < 256; i++) {
				colourArray[0][i] = getStandardTransferFunction(i);
				colourArray[1][i] = rr;
				colourArray[2][i] = gg;
				colourArray[3][i] = bb;
			}
			return colourArray;
		}
		
		public static int[][] createIntensityColourMap(Color c) {
			return createIntensityColourMap(c.getRed(), c.getGreen(), c.getBlue());
		}
		
		/**
		 * Creates a intensity "colour map", where the gray values are coloured
		 * according to the input colour.
		 * 
		 * @param rr
		 *           The red-component of the colour
		 * @param gg
		 *           The green-component of the colour
		 * @param bb
		 *           The blue-component of the colour
		 * @return The colourmap in an array.
		 */
		public static int[][] createIntensityColourMap(int rr, int gg, int bb) {
			int[][] colourArray = new int[4][256];
			int max = Math.max(rr, Math.max(gg, bb));
			for (int i = 0; i < 256; i++) {
				colourArray[0][i] = getStandardTransferFunction(i);
				colourArray[1][i] = max == 0 ? 0 : Math.round(i * rr / max);
				colourArray[2][i] = max == 0 ? 0 : Math.round(i * gg / max);
				colourArray[3][i] = max == 0 ? 0 : Math.round(i * bb / max);
			}
			return colourArray;
		}
		
		public static String getDefaultColourMap(boolean isColoredVolume) {
			if (isColoredVolume)
				return getStringColourThreshold(Color.white, -1);
			else
				return Colourmap.GRAY.getAsString();
		}
		
		public static String getStringColourThreshold(Color thresholdColor, Integer thresholdValue) {
			return "colorthreshold " + ColorUtil.getHexFromColor(thresholdColor) + " " + thresholdValue;
		}
		
		public static int getColourThreshold(String attributeValue) {
			if (attributeValue == null || !attributeValue.startsWith("colorthreshold ") || attributeValue.split(" ").length != 3)
				return -1;
			
			String temp = attributeValue.substring("colorthreshold ".length());
			temp = temp.substring(temp.indexOf(" ") + " ".length());
			int i = -1;
			try {
				i = Integer.parseInt(temp);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			return i;
		}
		
		public static Color getColourThresholdColour(String attributeValue) {
			if (attributeValue == null || !attributeValue.startsWith("colorthreshold ") || attributeValue.split(" ").length != 3)
				return Color.white;
			
			String temp = attributeValue.substring("colorthreshold ".length());
			temp = temp.substring(0, temp.indexOf(" "));
			return ColorUtil.getColorFromHex(temp);
		}
	}
	
	public static enum Transferfunction {
		LINEAR("Linear Ascending"),
		SIGMOIDAL("Sigmoidal/Threshold Function"),
		BANDFILTER("Bandfilter"),
		BANDPASS("Bandpass"),
		SINUS("Sinus Function");
		
		private String name;
		
		private Transferfunction(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
		public int[] getAsArray(double param1, double param2) {
			int[] function = new int[256];
			
			int start1 = (int) Math.round(param1);
			int start2 = (int) Math.round(param2);
			
			switch (this) {
				case LINEAR:
					if (param1 < 0)
						param1 = 0;
					if (param2 > 255)
						param2 = 255;
					if (param1 > param2)
						return getDefaultTransferFunction();
					for (int i = 0; i < param1; i++)
						function[i] = 0;
					for (int i = 0; i < (param2 - param1); i++)
						function[i] = (int) Math.round(255d * i / (param2 - param1));
					for (int i = start2; i < 256; i++)
						function[i] = 255;
					return function;
				case BANDFILTER:
					if (param1 < 0)
						param1 = 0;
					if (param2 > 255)
						param2 = 255;
					if (param1 > param2)
						return getDefaultTransferFunction();
					for (int i = 0; i < param1; i++)
						function[i] = 255;
					for (int i = Math.max(start1, 0); i < param2; i++)
						function[i] = 0;
					for (int i = Math.max(start2, 0); i < 256; i++)
						function[i] = 255;
					return function;
				case BANDPASS:
					if (param1 < 0)
						param1 = 0;
					if (param2 > 255)
						param2 = 255;
					if (param1 > param2)
						return getDefaultTransferFunction();
					for (int i = 0; i < param1; i++)
						function[i] = 0;
					for (int i = Math.max(start1, 0); i < param2; i++)
						function[i] = 255;
					for (int i = Math.max(start2, 0); i < 256; i++)
						function[i] = 0;
					return function;
				case SIGMOIDAL:
					if (param1 < 0)
						param1 = -param1;
					for (int i = 0; i < 256; i++)
						function[i] = (int) Math.round(255d / (1 + Math.exp(-param1 * ((i - param2) / 500d))));
					return function;
				case SINUS:
					for (int i = 0; i < 256; i++)
						function[i] = (int) Math.round(125.5d + 125.5d * Math.sin(param1 / 100 * i + param2));
					return function;
				default:
					return getDefaultTransferFunction();
			}
			
		}
		
		public static int[] getDefaultTransferFunction() {
			int[] function = new int[256];
			for (int i = 0; i < 255; i++)
				function[i] = getStandardTransferFunction(i);
			return function;// Transferfunction.SIGMOIDAL.getAsString(10000, 21);
		}
		
		// public static String getStringTransferFunction(int[] tf) {
		// String func = "";
		// for (int j = 0; j < 256; j++)
		// func += tf[j] + "|";
		// return func;
		// }
		
		// public static int[] getArrayTransferFunction(String tf) {
		// int[] func = new int[256];
		// for (int j = 0; j < 256; j++) {
		// func[j] = Integer.parseInt(tf.substring(0, tf.indexOf('|')));
		// tf = tf.substring(tf.indexOf('|') + 1);
		// }
		// return func;
		// }
		
		// public int[] getAsArray(double param1, double param2) {
		// return getArrayTransferFunction(this.getAsString(param1, param2));
		// }
	}
	
	private static int getStandardTransferFunction(int voxelvalue) {
		return voxelvalue < 20 ? 0 : 255;
	}
	
	private final ColourBar cb;
	
	public VolumeColourMap() {
		cb = new ColourBar();
	}
	
	public int[][] getActiveColourMap() {
		return cb.cmap;
	}
	
	public JPanel paintColourMapOnPanel(int[][] cmap) {
		cb.setColourMap(cmap);
		return cb;
	}
	
	public class ColourBar extends JPanel {
		
		private static final long serialVersionUID = 1L;
		private int[][] cmap;
		
		public ColourBar() {
			setBackground(Color.BLACK);
			setOpaque(false);
		}
		
		public void setColourMap(int[][] cmap) {
			this.cmap = cmap;
			repaint();
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			g2.setColor(Color.BLACK);
			
			int x = 5;
			int y = 0;
			int width = 255;
			int length = 8;
			
			g2.drawRect(x, y + 2, width + 2, length + 2);
			g2.drawRect(x, y + length + 4, width + 2, length + 2);
			
			for (int i = 0; i <= 255; i++) {
				
				Color c = new Color(i, i, i);
				
				g2.setColor(c);
				g2.fillRect(x + 1 + i, y + 1 + 2, 1, length + 1);
				Color valCFalse = new Color(cb.cmap[1][i], cb.cmap[2][i], cb.cmap[3][i], cb.cmap[0][i]);
				g2.setColor(valCFalse);
				g2.fillRect(x + 1 + i, y + length + 5, 1, length + 1);
				
			}
		}
		
	}
	
	public int[][] mergeColorAndTransfer(Colourmap cmap, int[] transferfunction) {
		int[][] cmaparray = cmap.getAsArray();
		int[][] function = new int[4][256];
		for (int i = 0; i < 255; i++) {
			function[0][i] = transferfunction[i];
			function[1][i] = cmaparray[1][i];
			function[2][i] = cmaparray[2][i];
			function[3][i] = cmaparray[3][i];
		}
		return function;
	}
	
}
