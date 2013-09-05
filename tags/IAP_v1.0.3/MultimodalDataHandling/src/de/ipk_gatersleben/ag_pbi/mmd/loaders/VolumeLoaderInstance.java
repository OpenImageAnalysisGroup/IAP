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
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.color.ColorUtil;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.datahandling.TemplateLoaderInstance;
import de.ipk_gatersleben.ag_pbi.mmd.JComboBoxAutoCompleteAndSelectOnTab;
import de.ipk_gatersleben.ag_pbi.mmd.JSpinnerSelectOnTab;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class VolumeLoaderInstance extends TemplateLoaderInstance {
	
	private JSpinnerSelectOnTab replicate;
	private JTextField unit;
	private JSpinnerSelectOnTab voxelnbrx;
	private JSpinnerSelectOnTab voxelnbry;
	private JSpinnerSelectOnTab voxelnbrz;
	private JSpinnerSelectOnTab voxeldimx;
	private JSpinnerSelectOnTab voxeldimy;
	private JSpinnerSelectOnTab voxeldimz;
	private JComboBoxAutoCompleteAndSelectOnTab colordepth;
	private JLabel showVolumeBitSize;
	
	public VolumeLoaderInstance(File f, VolumeLoader parent) {
		super(f, parent);
	}
	
	@Override
	public List<NumericMeasurementInterface> addMeasurementsToHierarchy(SampleInterface sample, String experimentname) {
		VolumeData vd = new VolumeData(sample);
		vd.setURL(FileSystemHandler.getURL(file));
		File labelfield = new File(file.getAbsolutePath() + ".labelfield");
		if (labelfield.canRead())
			vd.setLabelURL(FileSystemHandler.getURL(labelfield));
		vd.setReplicateID(getReplicate());
		vd.setUnit(getUnit());
		vd.setVoxelsizeX(getVoxelsizeX());
		vd.setVoxelsizeY(getVoxelsizeY());
		vd.setVoxelsizeZ(getVoxelsizeZ());
		vd.setDimensionX(getDimensionX());
		vd.setDimensionY(getDimensionY());
		vd.setDimensionZ(getDimensionZ());
		vd.setColorDepth(getColorDepth());
		
		return toList(vd);
	}
	
	@Override
	public JPanel getAttributeDialog(int filenumber) throws Exception {
		JPanel pan = new JPanel();
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		substancename = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Substance*"), substancename, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		replicate = new JSpinnerSelectOnTab(new SpinnerNumberModel(filenumber, 0, 100000, 1));
		pan.add(TableLayout.getSplit(new JLabel("Replicate ID"), replicate, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		unit = new JTextField();
		pan.add(TableLayout.getSplit(new JLabel("Unit*"), unit, ImportDialogFile.LEFTSIZE, ImportDialogFile.RIGHTSIZE));
		
		final long volumesize = file.length();
		
		// try to get the dimensions and voxelsizes from header
		final VolumeHeader hdr = getHeaderInformation();
		
		boolean validvoxelwidth = (hdr.voxeldimx >= 0d && hdr.voxeldimx <= 100000d);
		boolean validvoxelheight = (hdr.voxeldimy >= 0d && hdr.voxeldimy <= 100000d);
		boolean validvoxeldepth = (hdr.voxeldimz >= 0d && hdr.voxeldimz <= 100000d);
		boolean validvoxelnumberwidth = (hdr.voxelnbrx > 0 && hdr.voxelnbrx < 100000);
		boolean validvoxelnumberheight = (hdr.voxelnbry > 0 && hdr.voxelnbry < 100000);
		boolean validvoxelnumberdepth = (hdr.voxelnbrz > 0 && hdr.voxelnbrz < 100000);
		
		final Runnable calculateVolumeBitSize = new Runnable() {
			public void run() {
				long bits = -1l;
				String text = "";
				String tooltiptext = "";
				
				switch (hdr.checkFileSize) {
					case APPROXIMATE:
							bits = (Integer) voxelnbrx.getValue() * (Integer) voxelnbry.getValue() * ((VolumeColorDepth) colordepth.getSelectedItem()).getBytes();
							if (Math.abs(bits - volumesize) < 5000) {
								text = "<html><font color=\"" + ColorUtil.getHexFromColor(Color.blue)
													+ "\">" + bits / 1024 + " kb match (header tolerance)";
								tooltiptext = "Size of volume file and input attributes match approximately (depending on header size)";
							} else {
								text = "<html><font color=\"" + ColorUtil.getHexFromColor(Color.red)
													+ "\">Attribute Size " + bits / 1024 + " kb, File Size " + volumesize / 1024 + " kb";
								tooltiptext = "<html>Size of file and input attributes do not match!<br>Choose another colordepth or voxelnumbers!";
							}
							break;
						case EXACT:
							bits = (Integer) voxelnbrx.getValue() * (Integer) voxelnbry.getValue() * (Integer) voxelnbrz.getValue()
												* ((VolumeColorDepth) colordepth.getSelectedItem()).getBytes();
							if (bits == volumesize) {
								text = bits / 1024 + " kb match";
								tooltiptext = "Sizes seem to be OK";
							} else {
								text = "<html><font color=\"" + ColorUtil.getHexFromColor(Color.red)
													+ "\">Attribute Size " + bits / 1024 + " kb, File Size " + volumesize / 1024 + " kb";
								tooltiptext = "<html>Size of file and input attributes do not match!<br>Choose another colordepth or voxelnumbers!";
							}
							break;
						case NONE:
							text = "filesizes not checked";
							tooltiptext = "filesizes are not checked and should work";
							break;
					}
					
					showVolumeBitSize.setText(text);
					showVolumeBitSize.setToolTipText(tooltiptext);
				}
		};
		ChangeListener reactOnSizeInput = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				calculateVolumeBitSize.run();
			}
		};
		
		voxelnbrx = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxelwidth ? hdr.voxelnbrx : 100, 0, 1000000, 1));
		voxelnbrx.addChangeListener(reactOnSizeInput);
		pan.add(TableLayout.getSplit(new JLabel("# Voxel X"), voxelnbrx, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		voxelnbry = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxelheight ? hdr.voxelnbry : 100, 0, 1000000, 1));
		voxelnbry.addChangeListener(reactOnSizeInput);
		pan.add(TableLayout.getSplit(new JLabel("# Voxel Y"), voxelnbry, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		voxelnbrz = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxeldepth ? hdr.voxelnbrz : 100, 0, 1000000, 1));
		voxelnbrz.addChangeListener(reactOnSizeInput);
		pan.add(TableLayout.getSplit(new JLabel("# Voxel Z"), voxelnbrz, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		
		voxeldimx = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxelnumberwidth ? hdr.voxeldimx : 30d, 0d,
							1000000d, 0.1d));
		pan.add(TableLayout.getSplit(new JLabel("Voxeldim X*"), voxeldimx, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		voxeldimy = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxelnumberheight ? hdr.voxeldimy : 15d, 0d,
							1000000d, 0.1d));
		voxeldimy.addChangeListener(reactOnSizeInput);
		pan.add(TableLayout.getSplit(new JLabel("Voxeldim Y*"), voxeldimy, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		voxeldimz = new JSpinnerSelectOnTab(new SpinnerNumberModel(validvoxelnumberdepth ? hdr.voxeldimz : 15d, 0d,
							1000000d, 0.1d));
		voxeldimz.addChangeListener(reactOnSizeInput);
		pan.add(TableLayout.getSplit(new JLabel("Voxeldim Z*"), voxeldimz, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		colordepth = new JComboBoxAutoCompleteAndSelectOnTab(VolumeColorDepth.values());
		colordepth.setOpaque(false);
		colordepth.setSelectedItem(hdr.fileType);
		colordepth.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				calculateVolumeBitSize.run();
			}
			
			public void popupMenuCanceled(PopupMenuEvent e) {
				calculateVolumeBitSize.run();
			}
		});
		pan.add(TableLayout.getSplit(new JLabel("Color Depth*"), colordepth, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		showVolumeBitSize = new JLabel("no size available");
		pan.add(TableLayout.getSplit(new JLabel("File Size (in kb)"), showVolumeBitSize, ImportDialogFile.LEFTSIZE,
							ImportDialogFile.RIGHTSIZE));
		calculateVolumeBitSize.run();
		return pan;
	}
	
	protected VolumeHeader getHeaderInformation() throws Exception {
		VolumeHeader hdr = readAnalyzeHeaderData(file);
		if (hdr == null)
			return new VolumeHeader();
		else
			return hdr;
	}
	
	public int getReplicate() {
		return (Integer) replicate.getValue();
	}
	
	public String getUnit() {
		return unit.getText();
	}
	
	public double getVoxelsizeX() {
		return (Double) voxeldimx.getValue();
	}
	
	public double getVoxelsizeY() {
		return (Double) voxeldimy.getValue();
	}
	
	public double getVoxelsizeZ() {
		return (Double) voxeldimz.getValue();
	}
	
	public int getDimensionX() {
		return (Integer) voxelnbrx.getValue();
	}
	
	public int getDimensionY() {
		return (Integer) voxelnbry.getValue();
	}
	
	public int getDimensionZ() {
		return (Integer) voxelnbrz.getValue();
	}
	
	public String getColorDepth() {
		return ((VolumeColorDepth) colordepth.getSelectedItem()).getName();
	}
	
	public void setReplicate(int val) {
		replicate.setValue(val);
	}
	
	public void setSubstance(String val) {
		substancename.setText(val);
	}
	
	public void setUnit(String val) {
		unit.setText(val);
	}
	
	public void setVoxelsizeX(double val) {
		voxeldimx.setValue(val);
	}
	
	public void setVoxelsizeY(double val) {
		voxeldimy.setValue(val);
	}
	
	public void setVoxelsizeZ(double val) {
		voxeldimz.setValue(val);
	}
	
	public void setColorDepth(String colordepth) {
		this.colordepth.setSelectedItem(VolumeColorDepth.getDepthFromString(colordepth));
		this.colordepth.firePopupMenuCanceled();
	}
	
	public static float[] readAnalyzeHeaderDataOld(File file) {
		
		float[] data = new float[8];
		
		if (file.getAbsolutePath().endsWith("raw") || file.getAbsolutePath().endsWith(("RAW"))) {
			List<Integer> nbrs = StringManipulationTools.getAllNumbersFromString(file.getName());
			if (nbrs.size() == 3) {
				data[0] = -1;
				data[1] = 30;
				data[2] = 15;
				data[3] = 15;
				data[4] = -1;
				data[5] = nbrs.get(0) - 1;
				data[6] = nbrs.get(1) - 1;
				data[7] = nbrs.get(2) - 1;
				return data;
			} else
				return null;
			
		}
		
		File file_header = new File(file.getAbsolutePath().replaceAll(".img", ".hdr"));
		if (!file_header.exists())
			return null;
		
		int xDim = 0, yDim = 0, zDim = 0;
		
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file_header)));
			in.readInt();
			
			byte[] dataTypeArray = new byte[10];
			in.readFully(dataTypeArray);
			byte[] dbNameArray = new byte[18];
			in.readFully(dbNameArray);
			
			in.readInt();
			in.readShort();
			byte[] regArray = new byte[1];
			in.readFully(regArray);
			byte[] hkeyArray = new byte[1];
			in.readFully(hkeyArray);
			
			short shDim[] = new short[8];
			for (int i = 0; i < 8; i++) {
				shDim[i] = in.readShort();
			}
			short noprow = shDim[1];
			xDim = noprow;
			// System.out.print("x: "+xDim+", ");
			short noprs = shDim[2];
			yDim = noprs;
			// System.out.print("y: "+yDim+", ");
			short nosv = shDim[3];
			zDim = nosv;
			// System.out.print("z: "+zDim+", ");
			in.readFloat();
			in.readFloat();
			in.readFloat();
			
			in.readShort();
			in.readShort();
			short bitpix = in.readShort();
			in.readShort();
			if (bitpix == 16)
				data[0] = 1;
			else
				data[0] = 0;
			
			float floPixDim[] = new float[8];
			for (int i = 0; i < 8; i++) {
				floPixDim[i] = in.readFloat();
			}
			@SuppressWarnings("unused")
			float voxdim = floPixDim[0];
			float vxwidth = floPixDim[1];
			float vxheight = floPixDim[2];
			float interdist = floPixDim[3];
			data[1] = vxwidth;
			data[2] = vxheight;
			data[3] = interdist;
			
			int glmax = in.readInt();
			data[4] = glmax;
			
			in.close();
			
			data[5] = xDim - 1;
			data[6] = yDim - 1;
			data[7] = zDim - 1;
			return data;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	/**
	 * Taken from ImageJ plugin to load Analyze
	 * This plugin loads Analyze format files. It parses the header file found in
	 * '<filename>.hdr' and uses this to appropriately load the raw image data
	 * found in '<filename>.img'. - Loads either big or little endian format. -
	 * Guy Williams, gbw1000@wbic.cam.ac.uk 23/9/99
	 */
	public static VolumeHeader readAnalyzeHeaderData(File file) {
		
		VolumeHeader hdr = new VolumeHeader();
		
		if (file.getAbsolutePath().toLowerCase().endsWith("raw")) {
			List<Integer> nbrs = StringManipulationTools.getAllNumbersFromString(file.getName());
			if (nbrs.size() == 3) {
				hdr.voxelnbrx = nbrs.get(0);
				hdr.voxelnbry = nbrs.get(1);
				hdr.voxelnbrz = nbrs.get(2);
				return hdr;
			} else {
				for (VolumeColorDepth d : VolumeColorDepth.values()) {
					long size = file.length() / d.getBytes();
					double cbrt = Math.cbrt(size);
					// three dimensions equal
					if (cbrt % 1 == 0) {
						hdr.voxelnbrx = (int) cbrt;
						hdr.voxelnbry = (int) cbrt;
						hdr.voxelnbrz = (int) cbrt;
						hdr.fileType = d;
						return hdr;
					}
					// tries to guess dimensions by assuming, that the images are
					// quadratic and contain 2^k pixels
					// and the number of images is tested
					for (int imagesize : new int[] { 64, 128, 256, 512 }) {
						for (int nbrimages = 10; nbrimages < 520; nbrimages++)
							if (((imagesize ^ 2 * nbrimages * d.getBytes()) - imagesize) == 0) {
								hdr.voxelnbrx = imagesize;
								hdr.voxelnbry = imagesize;
								hdr.voxelnbrz = nbrimages;
								hdr.fileType = d;
								return hdr;
							}
						
					}
				}
			}
			return hdr;
		}
		
		File file_header = new File(file.getAbsolutePath().replaceAll(".img", ".hdr"));
		if (!file_header.exists())
			return null;
		
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file_header)));
			
			byte[] units = new byte[4];
			
			int i;
			short datatype;
			
			// header_key
			
			in.readInt(); // sizeof_hdr
			for (i = 0; i < 10; i++)
				in.read(); // data_type
			for (i = 0; i < 18; i++)
				in.read(); // db_name
			in.readInt(); // extents
			in.readShort(); // session_error
			in.readByte(); // regular
			in.readByte(); // hkey_un0
			
			// image_dimension
			
			short endian = readShort(in, hdr.intelByteOrder); // dim[0]
			if ((endian < 0) || (endian > 15))
				hdr.intelByteOrder = true;
			hdr.voxelnbrx = readShort(in, hdr.intelByteOrder); // dim[1]
			hdr.voxelnbry = readShort(in, hdr.intelByteOrder); // dim[2]
			hdr.voxelnbrz = readShort(in, hdr.intelByteOrder); // dim[3]
			in.readShort(); // dim[4]
			for (i = 0; i < 3; i++)
				in.readShort(); // dim[5-7]
			in.read(units, 0, 4); // vox_units
			hdr.unit = new String(units, 0, 4);
			for (i = 0; i < 8; i++)
				in.read(); // cal_units[8]
			in.readShort(); // unused1
			datatype = readShort(in, hdr.intelByteOrder); // datatype
			readShort(in, hdr.intelByteOrder); // bitpix
			in.readShort(); // dim_un0
			in.readFloat(); // pixdim[0]
			hdr.voxeldimx = readFloat(in, hdr.intelByteOrder); // pixdim[1]
			hdr.voxeldimy = readFloat(in, hdr.intelByteOrder); // pixdim[2]
			hdr.voxeldimz = readFloat(in, hdr.intelByteOrder); // pixdim[3]
			for (i = 0; i < 4; i++)
				in.readFloat(); // pixdim[4-7]
			hdr.offset = (int) readFloat(in, hdr.intelByteOrder); // vox_offset
			in.readFloat(); // roi_scale
			in.readFloat(); // funused1
			in.readFloat(); // funused2
			in.readFloat(); // cal_max
			in.readFloat(); // cal_min
			in.readInt(); // compressed
			in.readInt(); // verified
			// ImageStatistics s = imp.getStatistics();
			readInt(in, hdr.intelByteOrder); // (int) s.max // glmax
			readInt(in, hdr.intelByteOrder); // (int) s.min // glmin
			
			// data_history
			
			for (i = 0; i < 80; i++)
				in.read(); // descrip
			for (i = 0; i < 24; i++)
				in.read(); // aux_file
			in.read(); // orient
			for (i = 0; i < 10; i++)
				in.read(); // originator
			for (i = 0; i < 10; i++)
				in.read(); // generated
			for (i = 0; i < 10; i++)
				in.read(); // scannum
			for (i = 0; i < 10; i++)
				in.read(); // patient_id
			for (i = 0; i < 10; i++)
				in.read(); // exp_date
			for (i = 0; i < 10; i++)
				in.read(); // exp_time
			for (i = 0; i < 3; i++)
				in.read(); // hist_un0
			in.readInt(); // views
			in.readInt(); // vols_added
			in.readInt(); // start_field
			in.readInt(); // field_skip
			in.readInt(); // omax
			in.readInt(); // omin
			in.readInt(); // smax
			in.readInt(); // smin
			
			switch (datatype) {
				
				case 2:
					hdr.fileType = VolumeColorDepth.BIT8;
					break;
				case 4:
					hdr.fileType = VolumeColorDepth.BIT16;
					break;
				case 8:
					hdr.fileType = VolumeColorDepth.RGB;
					break;
				case 16:
					hdr.fileType = VolumeColorDepth.RGBA;
					break;
				default:
					hdr.fileType = VolumeColorDepth.BIT8;
					break;
			}
			
			in.close();
			
			return hdr;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	private static int readInt(DataInputStream input, boolean littleEndian) throws IOException {
		if (!littleEndian)
			return input.readInt();
		byte b1 = input.readByte();
		byte b2 = input.readByte();
		byte b3 = input.readByte();
		byte b4 = input.readByte();
		return ((((b4 & 0xff) << 24) | ((b3 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b1 & 0xff)));
	}
	
	private static short readShort(DataInputStream input, boolean littleEndian) throws IOException {
		if (!littleEndian)
			return input.readShort();
		byte b1 = input.readByte();
		byte b2 = input.readByte();
		return ((short) (((b2 & 0xff) << 8) | (b1 & 0xff)));
	}
	
	private static float readFloat(DataInputStream input, boolean littleEndian) throws IOException {
		if (!littleEndian)
			return input.readFloat();
		int orig = readInt(input, littleEndian);
		return (Float.intBitsToFloat(orig));
	}
	
	@Override
	public Object[] getFormData() {
		return new Object[] { getSubstance(), getUnit(), getVoxelsizeX(), getVoxelsizeY(), getVoxelsizeZ(),
							getColorDepth() };
	}
	
	@Override
	protected void setFormData(Object[] formularData) {
		int idx = 0;
		setSubstance((String) formularData[idx++]);
		setUnit((String) formularData[idx++]);
		setVoxelsizeX((Double) formularData[idx++]);
		setVoxelsizeY((Double) formularData[idx++]);
		setVoxelsizeZ((Double) formularData[idx++]);
		setColorDepth((String) formularData[idx++]);
	}
	
}
