package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.biopax;

import java.io.IOException;
import java.io.OutputStream;

import org.AttributeHelper;
import org.biopax.paxtools.impl.level3.Level3FactoryImpl;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.LVL3ModelConverter;

/**
 * Takes a graph with a biochemical pathway, filters all attributes that could
 * possibly belong to a BioPax-File and creates and OWL-File out of it
 * 
 * @author ricardo
 * 
 */
public class BioPAX_OWL_Writer extends HelperClass implements OutputSerializer
{

	@Override
	public String[] getExtensions()
	{
		return new String[] { ".owl" };
	}

	@Override
	public String[] getFileTypeDescriptions()
	{
		return new String[] { "BioPax" };
	}

	@Override
	public void write(OutputStream stream, Graph g) throws IOException
	{
		// build a default factory and model
		Level3FactoryImpl factory = (Level3FactoryImpl) BioPAXLevel.L3.getDefaultFactory();
		Model model = factory.createModel();
		// if it is Level2 the BioPaxEntities get translated into Level3
		if (g.getAttribute(Messages.getString("UtilitySuperClassToGraph.114")).getValue().toString().matches(BioPAXLevel.L2.name()))
		{
			g = translateToThree(g);

		}
		// converts a given graph into a BioPax Level 3 model
		// hand-written function
		LVL3ModelConverter converter = new LVL3ModelConverter();
		converter.readGraph(model, g);

		// handler converts a model to a stream
		// built-in function
		BioPAXIOHandler handler = new SimpleIOHandler();
		handler.convertToOWL(model, stream);
	}

	/**
	 * matches NodeTypes of level 2 to level 3 so that only one exporter for
	 * Level 3 is needed, which actually can also export level 2 models
	 * 
	 * @param g
	 * @return
	 */
	private Graph translateToThree(Graph g)
	{
		for (Node n : g.getNodes())
		{
			String nodeType = n.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")).getValue().toString();
			if (nodeType.matches("org.biopax.paxtools.impl.level2.biochemicalReactionImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.166");

			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.conversionImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.177");

			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.controlImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.178");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.transportImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.169");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.complexAssemblyImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.167");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.transportWithBiochemicalReactionImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.170");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.complexImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.150");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.dnaImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.151");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.rnaImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.154");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.proteinImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.153");
			} else if (nodeType.matches("org.biopax.paxtools.impl.level2.smallMoleculeImpl"))
			{
				nodeType = Messages.getString("UtilityClassSelectorFromGraph.156");
			}
			setAttributeSecure(n, Messages.getString("UtilitySuperClassToGraph.126"), nodeType);
		}
		for (Edge e : g.getEdges())
		{
			if (AttributeHelper.hasAttribute(e, Messages.getString("UtilitySuperClassToGraph.126")))
			{
				String nodeType = e.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")).getValue().toString();
				if (nodeType.matches("org.biopax.paxtools.impl.level2.catalysisImpl"))
				{
					nodeType = Messages.getString("UtilityClassSelectorFromGraph.174");
				} else if (nodeType.matches("org.biopax.paxtools.impl.level2.controlImpl"))
				{
					nodeType = Messages.getString("UtilityClassSelectorFromGraph.178");
				} else if (nodeType.matches("org.biopax.paxtools.impl.level2.modulationImpl"))
				{
					nodeType = Messages.getString("UtilityClassSelectorFromGraph.176");
				}
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.126"), nodeType);
			}
		}
		return g;
	}

}