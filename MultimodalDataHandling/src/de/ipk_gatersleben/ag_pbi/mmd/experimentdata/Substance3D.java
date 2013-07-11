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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.jdom.Attribute;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleAverageInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Klukas, Rohn
 */
public class Substance3D extends Substance {
	
	public Substance3D() {
		super();
	}
	
	public Substance3D(Map<String, Object> map) {
		super(map);
	}
	
	public static ExperimentInterface removeAllMeasurementsWhichAreNotOfType(
			ExperimentInterface mds, MeasurementNodeType type) {
		
		if (mds != null) {
			
			List<NumericMeasurementInterface> measlist = new ArrayList<NumericMeasurementInterface>();
			List<SampleInterface> samplelist = new ArrayList<SampleInterface>();
			List<ConditionInterface> serieslist = new ArrayList<ConditionInterface>();
			List<SubstanceInterface> mdlist = new ArrayList<SubstanceInterface>();
			
			for (NumericMeasurementInterface meas : getAllFiles(mds))
				if (((NumericMeasurement3D) meas).getType() != type)
					measlist.add(meas);
			
			for (NumericMeasurementInterface m : measlist) {
				SampleInterface parent = m.getParentSample();
				parent.remove(m);
				if (parent.size() == 0) {
					samplelist.add(parent);
				}
			}
			
			for (SampleInterface s : samplelist) {
				ConditionInterface parent = s.getParentCondition();
				parent.remove(s);
				if (parent.size() == 0)
					serieslist.add(parent);
			}
			
			for (ConditionInterface s : serieslist) {
				SubstanceInterface parent = s.getParentSubstance();
				parent.remove(s);
				if (parent.size() == 0)
					mdlist.add(parent);
			}
			mds.removeAll(mdlist);
			
		}
		
		if (type != MeasurementNodeType.OMICS)
			deleteAllEmptyMappingDataEntities(mds);
		
		return mds;
	}
	
	private static void deleteAllEmptyMappingDataEntities(
			ExperimentInterface mds3d) {
		List<SubstanceInterface> delmds = new ArrayList<SubstanceInterface>();
		for (SubstanceInterface md : mds3d) {
			List<ConditionInterface> delseries = new ArrayList<ConditionInterface>();
			for (ConditionInterface series : md) {
				List<SampleInterface> delsamples = new ArrayList<SampleInterface>();
				for (SampleInterface sample : series)
					if (sample.size() == 0)
						delsamples.add(sample);
				for (SampleInterface sample : delsamples)
					((Condition3D) sample.getParentCondition()).remove(sample);
				if (series.size() == 0)
					delseries.add(series);
			}
			for (ConditionInterface series : delseries)
				((Substance3D) series.getParentSubstance()).remove(series);
			if (md.size() == 0)
				delmds.add(md);
		}
		for (SubstanceInterface md : delmds)
			mds3d.remove(md);
		
	}
	
	public static List<ConditionInterface> getAllSeries(
			Iterable<SubstanceInterface> mds) {
		List<ConditionInterface> list = new ArrayList<ConditionInterface>();
		
		for (SubstanceInterface m : mds)
			for (ConditionInterface series : m)
				list.add(series);
		
		return list;
	}
	
	public static List<SampleInterface> getAllSamples(
			Iterable<SubstanceInterface> mds) {
		List<SampleInterface> list = new ArrayList<SampleInterface>();
		
		for (ConditionInterface series : Substance3D.getAllSeries(mds))
			for (SampleInterface sample : series)
				list.add(sample);
		
		return list;
	}
	
	public static ExperimentInterface copyMappings(ExperimentInterface mds3d) {
		
		return mds3d.clone();
		
		// List<Substance> copy = new ArrayList<Substance>();
		//
		// if(mds3d!=null)
		// for(Substance md : mds3d) {
		// Substance mdnew = new Substance3D(md);
		// for(Condition series : md) {
		// Condition seriesnew = new Condition3D(mdnew,series);
		// mdnew.addAndMergeData(seriesnew);
		// for(Sample sample : series) {
		// Sample samplenew = new Sample3D(seriesnew,sample);
		// seriesnew.addAndMerge(samplenew);
		// for(NumericMeasurement meas :
		// ((Sample3D)sample).getAllMeasurements())
		// {
		// NumericMeasurement measnew = null;
		// switch(((NumericMeasurement3D) meas).getType()) {
		// case IMAGE : measnew = new ImageData(samplenew,(ImageData)
		// meas);break;
		// case NETWORK : measnew = new NetworkData(samplenew,(NetworkData)
		// meas);break;
		// case VOLUME : measnew = new VolumeData(samplenew,(VolumeData)
		// meas);break;
		// case OMICS : measnew = new
		// NumericMeasurement3D(samplenew,meas);break;
		// }
		// if(measnew!=null)
		// samplenew.add(measnew);
		// }
		// }
		// }
		// copy.add(mdnew);
		// }
		//
		// return new Experiment(copy);
	}
	
	public static ExperimentInterface splitMappings(ExperimentInterface mds3d,
			MeasurementNodeType type) {
		if (mds3d == null)
			return null;
		
		return Substance3D.removeAllMeasurementsWhichAreNotOfType(
				mds3d.clone(), type);
	}
	
	public static String getExperimentName(List<Substance> mds) {
		if (mds.size() > 0 && mds.get(0).iterator().hasNext())
			return mds.get(0).iterator().next().getExperimentName();
		else
			return null;
	}
	
	public static List<NumericMeasurementInterface> getAllFiles(
			ExperimentInterface mds) {
		return getAllFiles(mds, null);
	}
	
	public static int countMeasurementValues(ExperimentInterface experiment,
			MeasurementNodeType[] measurementNodeTypes) {
		int res = 0;
		for (MeasurementNodeType m : measurementNodeTypes) {
			res += getAllFiles(experiment, m).size();
		}
		return res;
	}
	
	public static int countMeasurementValues2(ExperimentInterface experiment,
			MeasurementNodeType... measurementNodeTypes) {
		int res = 0;
		for (MeasurementNodeType m : measurementNodeTypes) {
			res += getAllFiles(experiment, m).size();
		}
		return res;
	}
	
	public static List<NumericMeasurementInterface> getAllFiles(
			ExperimentInterface mds, MeasurementNodeType type) {
		List<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface m : mds)
			for (ConditionInterface series : m)
				for (SampleInterface sample : series) {
					if (sample instanceof Sample3D) {
						for (NumericMeasurementInterface meas : ((Sample3D) sample)
								.getMeasurements(type))
							list.add(meas);
					}
				}
		
		return list;
	}
	
	public static SubstanceInterface createnewSubstance(String substancename) {
		SubstanceInterface md = Experiment.getTypeManager().getNewSubstance();
		md.setAttribute(new Attribute("name",
				substancename != null ? substancename
						: ExperimentInterface.UNSPECIFIED_SUBSTANCE));
		md.setAttribute(new Attribute("id", "column 0"));
		return md;
	}
	
	public static Long getFileSize(List<NumericMeasurementInterface> files) {
		return getFileSize(files, null);
	}
	
	public static Long getFileSize(List<NumericMeasurementInterface> files, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		long size = 0;
		HashMap<String, ResourceIOHandler> map = new HashMap<String, ResourceIOHandler>();
		int idx = 0;
		int max = files.size();
		mainLoop: for (NumericMeasurementInterface nmi : files) {
			idx++;
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine(100d * idx / max);
			if (nmi instanceof BinaryMeasurement) {
				BinaryMeasurement binaryMeasurement = (BinaryMeasurement) nmi;
				IOurl u = binaryMeasurement.getURL();
				if (u != null) {
					String prefix = u.getPrefix();
					if (!map.containsKey(prefix)) {
						map.put(prefix,
								ResourceIOManager.getHandlerFromPrefix(prefix));
					}
					ResourceIOHandler h = map.get(prefix);
					if (h != null) {
						try {
							Long fs = h.getStreamLength(u);
							if (fs != null && fs > 0)
								size += fs;
							else {
								size = -1;
								break mainLoop;
							}
						} catch (Exception e) {
							// empty
						}
					}
				}
			}
		}
		if (size > 0)
			return size;
		else
			return -1l;
	}
	
	public static Collection<NumericMeasurementInterface> getAllMeasurements(
			ExperimentInterface e) {
		Collection<NumericMeasurementInterface> res = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				for (SampleInterface s : ci) {
					for (NumericMeasurementInterface nmi : s) {
						res.add(nmi);
					}
				}
			}
		}
		return res;
	}
	
	public static Measurement findCorrespondingMeasurement(
			ExperimentInterface experiment, Measurement prototype,
			String substanceName) {
		if (prototype instanceof NumericMeasurement3D)
			return findNumericMeasurement(experiment, (NumericMeasurement3D) prototype, substanceName);
		else
			return findSampleAverage(experiment, (SampleAverageInterface) prototype, substanceName);
	}
	
	private static NumericMeasurementInterface findNumericMeasurement(ExperimentInterface experiment, NumericMeasurement3D prototype, String substanceName) {
		ArrayList<NumericMeasurementInterface> res = new ArrayList<NumericMeasurementInterface>();
		for (SubstanceInterface s : experiment) {
			if (s.getName().equals(substanceName)) {
				String protypeConditionName = prototype.getParentSample()
						.getParentCondition().getConditionName();
				Long prototypeSampleRowID = prototype.getParentSample()
						.getSampleFineTimeOrRowId();
				int prototypeReplicateID = prototype.getReplicateID();
				String prototypeQualityAnnotation = prototype
						.getQualityAnnotation();
				double prototypePosition = prototype.getPosition() != null ? prototype
						.getPosition() : 0;
				String ptC = protypeConditionName.split(": ", 2)[1];
				for (ConditionInterface ci : s) {
					String ciC = ci.getConditionName().split(": ", 2)[1];
					if (ciC.equals(ptC)) {
						for (SampleInterface si : ci) {
							if (prototypeSampleRowID.equals(si.getSampleFineTimeOrRowId())) {
								for (NumericMeasurementInterface nmi : si) {
									if (nmi instanceof NumericMeasurement3D) {
										NumericMeasurement3D n3 = (de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D) nmi;
										double n3p = n3.getPosition() != null ? n3
												.getPosition() : 0;
										if (n3.getQualityAnnotation()
												.equals(prototypeQualityAnnotation)
												&& Math.abs(n3p
														- prototypePosition) < 0.00001)
											res.add(n3);
									}
								}
							}
						}
					}
				}
			}
		}
		if (res.size() == 0)
			System.out.println("NO MATCH !!!!! !!!!!!12345");
		if (res.size() > 1)
			System.out.println("MORE THAN ONE MATCH !!!!!!!!!!!12345");
		return res.size() == 1 ? res.get(0) : null;
	}
	
	private static SampleAverageInterface findSampleAverage(ExperimentInterface experiment, SampleAverageInterface prototype, String substanceName) {
		ArrayList<SampleAverageInterface> res = new ArrayList<SampleAverageInterface>();
		for (SubstanceInterface s : experiment) {
			if (s.getName().equals(substanceName)) {
				String protypeConditionName = prototype.getParentSample()
						.getParentCondition().getConditionName();
				Long prototypeSampleRowID = prototype.getParentSample()
						.getSampleFineTimeOrRowId();
				int prototypeReplicateID = prototype.getReplicateID();
				String ptC = protypeConditionName.split(": ", 2)[1];
				for (ConditionInterface ci : s) {
					String ciC = ci.getConditionName().split(": ", 2)[1];
					if (ciC.equals(ptC)) {
						for (SampleInterface si : ci) {
							if (prototypeSampleRowID.equals(si.getSampleFineTimeOrRowId())) {
								SampleAverageInterface n3 = si.getSampleAverage();
								if (n3.getReplicateID() == prototypeReplicateID)
									res.add(n3);
							}
						}
					}
				}
			}
		}
		if (res.size() == 0)
			System.out.println("NO MATCH !!!!! !!!!!!12345");
		if (res.size() > 1)
			System.out.println("MORE THAN ONE MATCH !!!!!!!!!!!12345");
		return res.size() == 1 ? res.get(0) : null;
	}
}
