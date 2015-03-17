package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;

/**
 * this class loads and adds all pathways from a model represents them in an own
 * data structure
 * 
 * @author ricardo
 * 
 */
public class MyPathWay
{

	private Set<pathwayComponent> components;
	private String RDFId;
	private String Name;
	private boolean superPathWay;
	private Set<MyPathWay> subPathWays;

	public MyPathWay(pathway p)
	{
		superPathWay = false;
		this.RDFId = p.getRDFId();
		if (p.getNAME() != null)
		{
			this.Name = p.getNAME();
		} else
		{
			this.Name = "";
		}
		this.components = new HashSet<pathwayComponent>();
		this.subPathWays = new HashSet<MyPathWay>();
		for (pathwayComponent process : p.getPATHWAY_COMPONENTS())
		{
			if (process instanceof pathway && !((pathway) process).getPATHWAY_COMPONENTS().isEmpty())
			{
				// SuperPathWay
				superPathWay = true;
				MyPathWay MYP = new MyPathWay((pathway) process);
				subPathWays.add(MYP);
			} else
			{
				// real component or blackboxpathway
				components.add(process);
			}
		}
	}

	public Set<MyPathWay> getSubPathWays()
	{
		return subPathWays;
	}

	public boolean isSuperPathWay()
	{
		return superPathWay;
	}

	public String getRDFId()
	{
		return RDFId;
	}

	public String getDisplayName()
	{
		return Name;
	}

	public Set<pathwayComponent> getPathwayComponents()
	{
		return components;
	}
}
