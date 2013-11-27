package de.ipk_gatersleben.ag_pbi.mmd.fluxdata;

import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;
import org.StringManipulationTools;

public class FluxReaction {
	
	private ArrayList<FluxReactant> left, right;
	private boolean isCorrect = true;
	
	public FluxReaction(String reaction) {
		
		left = new ArrayList<FluxReactant>();
		right = new ArrayList<FluxReactant>();
		
		boolean reactioncharFound = false;
		
		for (String reactionChar : new String[] { "<==>", "==>", "<==" }) {
			String[] leftright = splitReaction(reaction, reactionChar);
			if (leftright == null)
				continue;
			if (reactionChar.equals("<==>") || reactionChar.equals("==>")) {
				left = getReactants(leftright[0]);
				right = getReactants(leftright[1]);
				reactioncharFound = true;
			} else
				if (reactionChar.equals("<==")) {
					left = getReactants(leftright[1]);
					right = getReactants(leftright[0]);
					reactioncharFound = true;
				}
			break;
		}
		for (FluxReactant frl : left)
			for (FluxReactant frr : right)
				if (frl.getName().equals(frr.getName())) {
					ErrorMsg.addErrorMessage("Substance \"" + frl.getName() + "\" is educt and product of reaction<br>" +
							"\"" + reaction + "\"! Please use different substance names<br>(e.g. \"" + frl.getName() + "1\" and \"" + frl.getName() + "2\").");
					isCorrect = false;
				}
		if (!reactioncharFound) {
			ErrorMsg.addErrorMessage("Did not found any reaction arrow (<==>,==>,<==) in reaction \"" + reaction + "\"!");
			isCorrect = false;
		}
	}
	
	private ArrayList<FluxReactant> getReactants(String reactionPart) {
		HashMap<String, FluxReactant> list = new HashMap<String, FluxReactant>();
		
		// TODO: how do we treat "A+ B"? because this will be parsed into one reaction...
		// maybe check for "+ " and " +" strings?
		for (String reactant : StringManipulationTools.splitSafe(reactionPart, " + ")) {
			FluxReactant fr = new FluxReactant(reactant);
			if (fr.isCorrect()) {
				FluxReactant existing = list.get(fr.getName());
				if (existing == null)
					list.put(fr.getName(), fr);
				else
					existing.add(fr);
			} else
				isCorrect = false;
		}
		
		return new ArrayList<FluxReactant>(list.values());
	}
	
	private String[] splitReaction(String reaction, String reactionChar) {
		if (reaction.contains(reactionChar)) {
			String left = reaction.substring(0, reaction.indexOf(reactionChar));
			String right = reaction.substring(reaction.indexOf(reactionChar) + reactionChar.length(), reaction.length());
			return new String[] { left, right };
		}
		return null;
	}
	
	public ArrayList<FluxReactant> getLeftReactants() {
		return left;
	}
	
	public ArrayList<FluxReactant> getRightReactants() {
		return right;
	}
	
	public ArrayList<FluxReactant> getAllReactants() {
		ArrayList<FluxReactant> list = new ArrayList<FluxReactant>();
		list.addAll(left);
		list.addAll(right);
		return list;
	}
	
	public boolean isLeftReactant(FluxReactant fr) {
		return left.contains(fr);
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
}