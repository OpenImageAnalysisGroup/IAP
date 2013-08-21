package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;
import org.jdom.Attribute;
import org.jdom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class which parses a given input stream of a VANTED/IAP XML data file.
 * Previously only XML DOM objects could be parsed, but this class uses the SAX
 * event model, and thus requires much less RAM than the previous approach.
 * 
 * @author Klukas
 */
public class ExperimentSaxHandler extends DefaultHandler {
	
	long readCnt = 0;
	long startTime = System.currentTimeMillis();
	
	private final class CountingInputStream extends InputStream {
		private final BackgroundTaskStatusProviderSupportingExternalCall optStatus;
		
		private CountingInputStream(BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
			this.optStatus = optStatus;
		}
		
		@Override
		public int read() throws IOException {
			if (inputStreamLength > 0) {
				readCnt++;
				if (optStatus != null) { // && ((readCnt % 1024) == 0)
					optStatus.setCurrentStatusValueFine(100d * readCnt / inputStreamLength);
				}
			}
			return is.read();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			int len = is.read(b);
			readCnt += len;
			return len;
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (optStatus != null) { // && ((readCnt % 1024) == 0)
				optStatus.setCurrentStatusValueFine(100d * readCnt / inputStreamLength);
				optStatus
						.setCurrentStatusText1("Receive data (" +
								readCnt / 1024 / 1024
								+ " MB, " + SystemAnalysis.getDataTransferSpeedString(readCnt, startTime, System.currentTimeMillis()) + ")");
			}
			int l = is.read(b, off, len);
			readCnt += l;
			return l;
		}
		
		@Override
		public long skip(long n) throws IOException {
			if (optStatus != null) { // && ((readCnt % 1024) == 0)
				optStatus.setCurrentStatusValueFine(100d * readCnt / inputStreamLength);
			}
			long l = is.skip(n);
			readCnt += l;
			return l;
		}
		
		@Override
		public int available() throws IOException {
			return is.available();
		}
		
		@Override
		public void close() throws IOException {
			is.close();
		}
		
		@Override
		public synchronized void mark(int readlimit) {
			is.mark(readlimit);
		}
		
		@Override
		public synchronized void reset() throws IOException {
			is.reset();
		}
		
		@Override
		public boolean markSupported() {
			return is.markSupported();
		}
	}
	
	private final InputStream is;
	private final Experiment e;
	private boolean headerProcessing = false;
	
	private final HashMap<String, Object> headerFields = new HashMap<String, Object>();
	private String currentHeaderItem;
	private final DataMappingTypeManagerInterface tm;
	private SubstanceInterface currentSubstance;
	private ConditionInterface currentCondition;
	private SampleInterface currentSample;
	private SampleAverageInterface currentSampleAverage;
	private NumericMeasurementInterface currentMeasurement;
	private final long inputStreamLength;
	
	public ExperimentSaxHandler(InputStream is, long inputStreamLength) {
		this.is = is;
		this.inputStreamLength = inputStreamLength;
		this.e = new Experiment();
		this.e.setHeader(new ExperimentHeader());
		tm = Experiment.getTypeManager();
	}
	
	/**
	 * Interpret given (in the constructor) input stream.
	 * 
	 * @param optStatus
	 *           The progress is returned using the given parameter object, if available.
	 * @return The according experiment structure, re-generated from the given XML input stream.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Experiment getExperiment(final BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws ParserConfigurationException, SAXException,
			IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		InputStream myIs = new CountingInputStream(optStatus);
		parser.parse(myIs, this);
		return e;
	}
	
	@Override
	public void startElement(String uri, String localName,
			String qName, Attributes attrs) throws SAXException {
		
		if (qName.equals("experiment")) {
			headerProcessing = true;
			try {
				String experimentId = attrs.getValue("experimentid");
				e.getHeader().setExperimentId(Integer.parseInt(experimentId));
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Invalid experiment ID: " + e.getMessage());
			}
		}
		
		if (qName.equals("substance")) {
			currentSubstance = tm.getNewSubstance();
			for (Attribute attr : getAttributes(attrs))
				currentSubstance.setAttribute(attr);
		}
		
		if (qName.equals("line")) {
			currentCondition = tm.getNewCondition(currentSubstance);
			for (Attribute attr : getAttributes(attrs))
				currentCondition.setAttribute(attr);
		}
		
		if (qName.equals("sample")) {
			currentSample = tm.getNewSample(currentCondition);
			for (Attribute attr : getAttributes(attrs))
				currentSample.setAttribute(attr);
		}
		
		if (qName.equals("average")) {
			currentSampleAverage = tm.getNewSampleAverage(currentSample);
			for (Attribute attr : getAttributes(attrs))
				currentSampleAverage.setAttribute(attr);
		}
		
		if (tm.isKnownMeasurementType(qName)) {
			currentMeasurement = tm.getNewMeasurementOfType(qName, currentSample);
			for (Attribute attr : getAttributes(attrs))
				currentMeasurement.setAttribute(attr);
		}
		
		if (headerProcessing)
			processHeaderField(uri, localName, qName, attrs);
	}
	
	ArrayList<Attribute> res = new ArrayList<Attribute>();
	
	private ArrayList<Attribute> getAttributes(Attributes attrs) {
		res.clear();
		for (int i = 0; i < attrs.getLength(); i++)
			res.add(new Attribute(attrs.getLocalName(i), attrs.getValue(i)));
		return res;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("experiment")) {
			e.getHeader().setAttributesFromMap(headerFields);
			headerProcessing = false;
		}
		
		if (qName.equals("substance")) {
			e.add(currentSubstance);
			currentSubstance = null;
		}
		
		if (qName.equals("line")) {
			currentSubstance.add(currentCondition);
			currentCondition = null;
		}
		
		if (qName.equals("sample")) {
			currentCondition.add(currentSample);
			currentSample = null;
		}
		
		if (qName.equals("average")) {
			currentSample.setSampleAverage(currentSampleAverage);
			currentSampleAverage = null;
		}
		
		if (tm.isKnownMeasurementType(qName)) {
			currentSample.add(currentMeasurement);
			currentMeasurement = null;
		}
	}
	
	private void processHeaderField(String uri, String localName, String qName, Attributes attrs) {
		currentHeaderItem = qName;
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (headerProcessing)
			headerFields.put(currentHeaderItem, getS(ch, start, length));
		else
			if (currentSampleAverage != null)
				currentSampleAverage.setValue(Double.parseDouble(getS(ch, start, length)));
			else
				if (currentMeasurement != null) {
					Element el = new Element("value");
					el.setText(getS(ch, start, length));
					currentMeasurement.setData(el);
				}
	}
	
	private String getS(char[] ch, int start, int length) {
		return new String(ch, start, length);
	}
}
