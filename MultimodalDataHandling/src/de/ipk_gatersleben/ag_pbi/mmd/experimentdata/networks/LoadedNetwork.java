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
/*
 * Created on Jun 11, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.actions.FileSaveAction;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;

/**
 * @author klukas
 */
public class LoadedNetwork extends NetworkData implements LoadedData {
	
	private final Graph graph;
	private final Graph graphLabelField;
	
	public LoadedNetwork(Sample parent, Graph graph, Graph optGraphLabelField) {
		super(parent);
		LoadedDataHandler.registerObject(this);
		this.graph = graph;
		this.graphLabelField = optGraphLabelField;
	}
	
	public LoadedNetwork(NetworkData nd, Graph graph, Graph optGraphLabelField) {
		super(nd.getParentSample(), nd);
		LoadedDataHandler.registerObject(this);
		this.graph = graph;
		this.graphLabelField = optGraphLabelField;
	}
	
	public Graph getLoadedGraph() {
		return graph;
	}
	
	public Graph getLoadedGraphLabelField() {
		return graphLabelField;
	}
	
	public InputStream getInputStream() {
		return getInputStreamForGraph(graph);
	}
	
	public InputStream getInputStreamLabelField() {
		return getInputStreamForGraph(graphLabelField);
	}
	
	private InputStream getInputStreamForGraph(final Graph graph) {
		if (graph == null)
			return null;
		try {
			String ext = FileSaveAction.getFileExt(graph.getName(true));
			final OutputSerializer os = MainFrame.getInstance().getIoManager().createOutputSerializer(ext);
			final PipedOutputStream pout = new PipedOutputStream();
			PipedInputStream pin = new PipedInputStream(pout);
			
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						os.write(pout, graph);
						pout.close();
					} catch (IOException e) {
						ErrorMsg.addErrorMessage(e);
						System.out.println("Could not write to piped output stream!");
						try {
							pout.close();
						} catch (IOException e1) {
							ErrorMsg.addErrorMessage(e1);
						}
					}
				}
			});
			t.setName("Write graph to piped output stream (" + graph.getName(true) + ")");
			t.start();
			
			return pin;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		return new LoadedNetwork((NetworkData) super.clone(parent), getLoadedGraph(), getLoadedGraphLabelField());
	}
	
}
