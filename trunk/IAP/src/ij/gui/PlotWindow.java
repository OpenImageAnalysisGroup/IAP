package ij.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.util.*;
import ij.*;
import ij.process.*;
import ij.util.*;
import ij.text.TextWindow;
import ij.plugin.filter.Analyzer;
import ij.measure.Measurements;


/** Obsolete; mostly replaced by the Plot class. */
public class PlotWindow extends ImageWindow implements ActionListener, ClipboardOwner {

	/** Display points using a circle 5 pixels in diameter. */
	public static final int CIRCLE = 0;
	/** Display points using an X-shaped mark. */
	public static final int X = 1;
	/** Display points using an box-shaped mark. */
	public static final int BOX = 3;
	/** Display points using an tiangular mark. */
	public static final int TRIANGLE = 4;
	/** Display points using an cross-shaped mark. */
	public static final int CROSS = 5;
	/** Connect points with solid lines. */
	public static final int LINE = 2;

	private static final int WIDTH = 450;
	private static final int HEIGHT = 200;
	
	private static final String MIN = "pp.min";
	private static final String MAX = "pp.max";
	private static final String PLOT_WIDTH = "pp.width";
	private static final String PLOT_HEIGHT = "pp.height";
	private static final String OPTIONS = "pp.options";
	private static final int SAVE_X_VALUES = 1;
	private static final int AUTO_CLOSE = 2;
	private static final int LIST_VALUES = 4;
	private static final int INTERPOLATE = 8;
	private static final int NO_GRID_LINES = 16;

	private Button list, save, copy;
	private Label coordinates;
	private static String defaultDirectory = null;
	private Font font = new Font("Helvetica", Font.PLAIN, 12);
	private static int options;
	private int defaultDigits = -1;
	private boolean realXValues;
	private int xdigits, ydigits;
	private static Plot staticPlot;
	private Plot plot;
	
	/** Save x-values only. To set, use Edit/Options/
		Profile Plot Options. */
	public static boolean saveXValues;
	
	/** Automatically close window after saving values. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean autoClose;
	
	/** The width of the plot in pixels. */
	public static int plotWidth = WIDTH;

	/** The height of the plot in pixels. */
	public static int plotHeight = HEIGHT;

	/** Display the XY coordinates in a separate window. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean listValues;

	/** Interpolate line profiles. To
		set, use Edit/Options/Profile Plot Options. */
	public static boolean interpolate;

	/** Add grid lines to plots */
	public static boolean noGridLines;

	// static initializer
	static {
		IJ.register(PlotWindow.class); //keeps options from being reset on some JVMs
		options = Prefs.getInt(OPTIONS, SAVE_X_VALUES);
		saveXValues = (options&SAVE_X_VALUES)!=0;
		autoClose = (options&AUTO_CLOSE)!=0;
		listValues = (options&LIST_VALUES)!=0;
		plotWidth = Prefs.getInt(PLOT_WIDTH, WIDTH);
		plotHeight = Prefs.getInt(PLOT_HEIGHT, HEIGHT);
		interpolate = (options&INTERPOLATE)==0; // 0=true, 1=false
		noGridLines = (options&NO_GRID_LINES)!=0; 
   }

 	/**
	* @deprecated
	* replaced by the Plot class.
	*/
	public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		super(createImage(title, xLabel, yLabel, xValues, yValues));
		plot = staticPlot;
	}

 	/**
	* @deprecated
	* replaced by the Plot class.
	*/
	public PlotWindow(String title, String xLabel, String yLabel, double[] xValues, double[] yValues) {
		this(title, xLabel, yLabel, Tools.toFloat(xValues), Tools.toFloat(yValues));
	}
	
	/** Creates a PlotWindow from a Plot object. */
	PlotWindow(Plot plot) {
		super(plot.getImagePlus());
		this.plot = plot;
		draw();
		//addComponentListener(this);
	}

	/** Called by the constructor to generate the image the plot will be drawn on.
		This is a static method because constructors cannot call instance methods. */
	static ImagePlus createImage(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		staticPlot = new Plot(title, xLabel, yLabel, xValues, yValues);
		return new ImagePlus(title, staticPlot.getBlankProcessor());
	}
	
	/** Sets the x-axis and y-axis range. */
	public void setLimits(double xMin, double xMax, double yMin, double yMax) {
		plot.setLimits(xMin, xMax, yMin, yMax);
	}

	/** Adds a set of points to the plot or adds a curve if shape is set to LINE.
	* @param x			the x-coodinates
	* @param y			the y-coodinates
	* @param shape		CIRCLE, X, BOX, TRIANGLE, CROSS or LINE
	*/
	public void addPoints(float[] x, float[] y, int shape) {
		plot.addPoints(x, y, shape);
	}

	/** Adds a set of points to the plot using double arrays.
		Must be called before the plot is displayed. */
	public void addPoints(double[] x, double[] y, int shape) {
		addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
	}
	
	/** Adds error bars to the plot. */
	public void addErrorBars(float[] errorBars) {
		plot.addErrorBars(errorBars);
	}

	/** Draws a label. */
	public void addLabel(double x, double y, String label) {
		plot.addLabel(x, y, label);
	}
	
	/** Changes the drawing color. The frame and labels are
		always drawn in black. */
	public void setColor(Color c) {
		plot.setColor(c);
	}

	/** Changes the line width. */
	public void setLineWidth(int lineWidth) {
		plot.setLineWidth(lineWidth);
	}

	/** Changes the font. */
	public void changeFont(Font font) {
		plot.changeFont(font);
	}

	/** Displays the plot. */
	public void draw() {
		Panel buttons = new Panel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		list = new Button(" List ");
		list.addActionListener(this);
		buttons.add(list);
		save = new Button("Save...");
		save.addActionListener(this);
		buttons.add(save);
		copy = new Button("Copy...");
		copy.addActionListener(this);
		buttons.add(copy);
		coordinates = new Label("X=12345678, Y=12345678"); 
		coordinates.setFont(new Font("Monospaced", Font.PLAIN, 12));
		buttons.add(coordinates);
		add(buttons);
		plot.draw();
		pack();
		coordinates.setText("					 ");
		ImageProcessor ip = plot.getProcessor();
		if ((ip instanceof ColorProcessor) && (imp.getProcessor() instanceof ByteProcessor))
			imp.setProcessor(null, ip);
		else
			imp.updateAndDraw();
		if (listValues)
			showList();
	}

	int getDigits(double n1, double n2) {
		if (Math.round(n1)==n1 && Math.round(n2)==n2)
			return 0;
		else {
			n1 = Math.abs(n1);
			n2 = Math.abs(n2);
			double n = n1<n2&&n1>0.0?n1:n2;
			double diff = Math.abs(n2-n1);
			if (diff>0.0 && diff<n) n = diff;			
			int digits = 1;
			if (n<10.0) digits = 2;
			if (n<0.01) digits = 3;
			if (n<0.001) digits = 4;
			if (n<0.0001) digits = 5;
			return digits;
		}
	}

	/** Updates the graph X and Y values when the mouse is moved.
		Overrides mouseMoved() in ImageWindow. 
		@see ij.gui.ImageWindow#mouseMoved
	*/
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		if (plot!=null && plot.frame!=null && coordinates!=null)
			coordinates.setText(plot.getCoordinates(x,y));
	}
		
	void showListOLD() {
		StringBuffer sb = new StringBuffer();
		String headings;
		initDigits();
		if (plot.errorBars !=null) {
			if (saveXValues)
				headings = "X\tY\tErrorBar";
			else
				headings = "Y\tErrorBar";
			for (int i=0; i<plot.nPoints; i++) {
				if (saveXValues)
					sb.append(IJ.d2s(plot.xValues[i],xdigits)+"\t"+IJ.d2s(plot.yValues[i],ydigits)+"\t"+IJ.d2s(plot.errorBars[i],ydigits)+"\n");
				else
					sb.append(IJ.d2s(plot.yValues[i],ydigits)+"\t"+IJ.d2s(plot.errorBars[i],ydigits)+"\n");
			}
		} else {
			if (saveXValues)
				headings = "X\tY";
			else
				headings = "Y";
			for (int i=0; i<plot.nPoints; i++) {
				if (saveXValues)
					sb.append(IJ.d2s(plot.xValues[i],xdigits)+"\t"+IJ.d2s(plot.yValues[i],ydigits)+"\n");
				else
					sb.append(IJ.d2s(plot.yValues[i],ydigits)+"\n");
			}
		}
		new TextWindow("Plot Values", headings, sb.toString(), 200, 400);
		if (autoClose)
			{imp.changes=false; close();}
	}
	/** shows the data of the backing plot in a Textwindow with columns */
	void showList(){
		initDigits();
		String headings = createHeading();
		String data = createData();
		new TextWindow("Plot Values", headings, data, 230, 400);
		if (autoClose)
			{imp.changes=false; close();}
	}
	
	/** creates the headings corresponding to the showlist funcion*/
	private String createHeading(){
		String head = "";
		int sets = plot.storedData.size()/2;
		if (saveXValues)
			head += sets==1?"X\tY\t":"X0\tY0\t";
		else
			head += sets==1?"Y0\t":"Y0\t";
		if (plot.errorBars!=null)
			head += "ERR\t";
		for (int j = 1; j<sets; j++){
			if (saveXValues)
				head += "X" + j + "\tY" + j + "\t";
			else
				head += "Y" + j + "\t";
		}
		return head;
	}
	
	/** creates the data that fills the showList() function values */
	private String createData(){
		int max = 0;
		
		/** find the longest x-value data set */
		float[] column;
		for(int i = 0; i<plot.storedData.size(); i+=2){
			column = (float[])plot.storedData.get(i);
			int s = column.length;
			max = s>max?s:max;
		}
		
		/** stores the values that will be displayed*/
		ArrayList<float[]> displayed = new ArrayList<float[]>(plot.storedData);
		boolean eb_test = false;
		
		/** includes error bars.*/
		if (plot.errorBars !=null)
			displayed.add(2, plot.errorBars);
					
		StringBuffer sb = new StringBuffer();
		String v;
		for(int i = 0; i<max; i++){
			eb_test = plot.errorBars != null;
			for (int j = 0; j<displayed.size();) {
				if(saveXValues){
					column = (float[])displayed.get(j);
					v = i<column.length?IJ.d2s(column[i],xdigits):"";
					sb.append(v);
					sb.append("\t");
				}
				j++;
				column = (float[])displayed.get(j);
				v = i<column.length?IJ.d2s(column[i],ydigits):"";
				sb.append(v);
				sb.append("\t");
				j++;
				if(eb_test){
					column = (float[])displayed.get(j);
					v = i<column.length?IJ.d2s(column[i],ydigits):"";
					sb.append(v);
					sb.append("\t");
					j++;
					eb_test=false;
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
	
	void saveAsText() {
		FileDialog fd = new FileDialog(this, "Save as Text...", FileDialog.SAVE);
		if (defaultDirectory!=null)
			fd.setDirectory(defaultDirectory);
		fd.setVisible(true);
		
		String name = fd.getFile();
		if (name==null) return;
		String directory = fd.getDirectory();
		defaultDirectory = directory;
		fd.dispose();
		PrintWriter pw = null;
		try {
			FileOutputStream fos = new FileOutputStream(directory+name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		catch (IOException e) {
			IJ.error("" + e);
			return;
		}
		IJ.wait(250);  // give system time to redraw ImageJ window
		IJ.showStatus("Saving plot values...");
		initDigits();
		/*for (int i=0; i<plot.nPoints; i++) {
			if (saveXValues)
				pw.println(IJ.d2s(plot.xValues[i],xdigits)+"\t"+IJ.d2s(plot.yValues[i],ydigits));
			else
				pw.println(IJ.d2s(plot.yValues[i],ydigits));
		}*/
		pw.print(createData());
		pw.close();
		if (autoClose)
			{imp.changes=false; close();}
	}
		
	void copyToClipboard() {
		Clipboard systemClipboard = null;
		try {systemClipboard = getToolkit().getSystemClipboard();}
		catch (Exception e) {systemClipboard = null; }
		if (systemClipboard==null)
			{IJ.error("Unable to copy to Clipboard."); return;}
		IJ.showStatus("Copying plot values...");
		initDigits();
		CharArrayWriter aw = new CharArrayWriter(plot.nPoints*4);
		PrintWriter pw = new PrintWriter(aw);
		for (int i=0; i<plot.nPoints; i++) {
			if (saveXValues)
				pw.print(IJ.d2s(plot.xValues[i],xdigits)+"\t"+IJ.d2s(plot.yValues[i],ydigits)+"\n");
			else
				pw.print(IJ.d2s(plot.yValues[i],ydigits)+"\n");
		}
		String text = aw.toString();
		pw.close();
		StringSelection contents = new StringSelection(text);
		systemClipboard.setContents(contents, this);
		IJ.showStatus(text.length() + " characters copied to Clipboard");
		if (autoClose)
			{imp.changes=false; close();}
	}
	
	void initDigits() {
		int digits = 2;
		int setDigits = Analyzer.getPrecision();
		int measurements = Analyzer.getMeasurements();
		boolean scientificNotation = (measurements&Measurements.SCIENTIFIC_NOTATION)!=0;
		if (scientificNotation) {
			if (setDigits<2) setDigits = 2;
			xdigits = ydigits = -setDigits;
			return;
		}

		if (ydigits!=9 || setDigits>=6) {
			ydigits = setDigits;
			if (ydigits==0) ydigits = 2;
			digits = ydigits;
		}
		if (ydigits!=defaultDigits) {
			realXValues = false;
			for (int i=0; i<plot.xValues.length; i++) {
				if ((int)plot.xValues[i]!=plot.xValues[i]) {
					realXValues = true;
					break;
				}
			}
			boolean realYValues = false;
			for (int i=0; i<plot.yValues.length; i++) {
				if ((int)plot.yValues[i]!=plot.yValues[i]) {
					realYValues = true;
					break;
				}
			}
			if (setDigits<6&&realYValues) ydigits = 9;
			if (!realYValues) ydigits = 0;
			defaultDigits = ydigits;
		}
		xdigits =  realXValues?ydigits:0;
		if (xdigits==0 && plot.xValues.length>=2 && (plot.xValues[1]-plot.xValues[0])<1.0)
			xdigits = digits;
	}
		
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
	public void actionPerformed(ActionEvent e) {
		Object b = e.getSource();
		if (b==list)
			showList();
		else if (b==save)
			saveAsText();
		else
			copyToClipboard();
	}
	
	public float[] getXValues() {
		return plot.xValues;
	}

	public float[] getYValues() {
		return plot.yValues;
	}
	
	/** Draws a new plot in this window. */
	public void drawPlot(Plot plot) {
		this.plot = plot;
		imp.setProcessor(null, plot.getProcessor());	
	}
	
	/** Called once when ImageJ quits. */
	public static void savePreferences(Properties prefs) {
		double min = ProfilePlot.getFixedMin();
		double max = ProfilePlot.getFixedMax();
		if (!(min==0.0&&max==0.0) && min<max) {
			prefs.put(MIN, Double.toString(min));
			prefs.put(MAX, Double.toString(max));
		}
		if (plotWidth!=WIDTH || plotHeight!=HEIGHT) {
			prefs.put(PLOT_WIDTH, Integer.toString(plotWidth));
			prefs.put(PLOT_HEIGHT, Integer.toString(plotHeight));
		}
		int options = 0;
		if (saveXValues) options |= SAVE_X_VALUES;
		if (autoClose && !listValues) options |= AUTO_CLOSE;
		if (listValues) options |= LIST_VALUES;
		if (!interpolate) options |= INTERPOLATE; // true=0, false=1
		if (noGridLines) options |= NO_GRID_LINES; 
		prefs.put(OPTIONS, Integer.toString(options));
	}
	
	//public void componentHidden(ComponentEvent e) {}
	//public void componentMoved(ComponentEvent e) {}
	//public void componentResized(ComponentEvent e) {
	//	IJ.log("componentResized");
	//}
	//public void componentShown(ComponentEvent e) {}
 
}


