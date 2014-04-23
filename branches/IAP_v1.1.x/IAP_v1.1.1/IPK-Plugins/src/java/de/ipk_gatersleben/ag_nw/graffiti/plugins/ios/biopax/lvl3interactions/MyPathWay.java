package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;

/**
 * this class loads and adds all pathways from a model represents them in an own
 * data structure
 * 
 * @author ricardo
 * 
 */
public class MyPathWay
{
	private String RDFId;
	private String Name;
	private boolean superPathWay;
	private Set<MyPathWay> subPathWays;
	private Set<Process> components;

	public MyPathWay(Pathway p)
	{
		this.RDFId = p.getRDFId();
		if (p.getDisplayName() != null)
		{
			this.Name = p.getDisplayName();
		} else
		{
			this.Name = "";
		}
		this.components = new HashSet<Process>();
		this.subPathWays = new HashSet<MyPathWay>();
		for (Process process : p.getPathwayComponent())
		{
			if (process instanceof Pathway && !((Pathway) process).getPathwayComponent().isEmpty())
			{
				// SuperPathWay
				superPathWay = true;
				MyPathWay MYP = new MyPathWay((Pathway) process);
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

	public Set<Process> getPathwayComponents()
	{
		return components;
	}

}
