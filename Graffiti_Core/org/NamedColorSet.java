/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Dec 3, 2009 by Christian Klukas
 */
package org;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author klukas
 */
public class NamedColorSet {
	
	private static HashMap<String, Color> name2colorLight = new HashMap<String, Color>();
	private static HashMap<String, Color> name2colorIntense = new HashMap<String, Color>();
	
	private static Stack<Color> unusedColorsLight = new Stack<Color>();
	private static Stack<Color> unusedColorsIntense = new Stack<Color>();
	
	private static int generationRound = 1;
	
	public static ArrayList<Color> getColors(ArrayList<String> groups, boolean light) {
		
		ArrayList<Color> lightColors = new ArrayList<Color>();
		ArrayList<Color> intenseColors = new ArrayList<Color>();
		
		// boolean generate = false;
		
		for (String group : groups) {
			if (unusedColorsLight.isEmpty() || unusedColorsIntense.isEmpty()) {
				System.out.println("Generate named colors (round " + generationRound + ")");
				for (Color c : org.Colors.get(7 * generationRound, 0.4f))
					unusedColorsLight.push(c);
				for (Color c : org.Colors.get(7 * generationRound, 0.7f))
					unusedColorsIntense.push(c);
				generationRound++;
				
				for (String k : groups)
					System.out.println(k);
			}
			
			if (!name2colorLight.containsKey(group))
				name2colorLight.put(group, unusedColorsLight.pop());
			if (!name2colorIntense.containsKey(group))
				name2colorIntense.put(group, unusedColorsIntense.pop());
			
			lightColors.add(name2colorLight.get(group));
			intenseColors.add(name2colorIntense.get(group));
			
		}
		
		if (light)
			return lightColors;
		else
			return intenseColors;
	}
	
}
