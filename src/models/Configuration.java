package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public enum Configuration {
	DEFAULT(0), CANNONS(1), LASERS(0), MISSILES(-1);
	
	public final int initiative;
	
	Configuration(int init) { this.initiative = init; }
	
	public static List<Set<Configuration>> phases() {
		List<Set<Configuration>> phases = new ArrayList<>();
		phases.add(new HashSet<>(Arrays.asList(CANNONS))); // hard coded
		phases.add(new HashSet<>(Arrays.asList(DEFAULT, LASERS))); // hard coded
		phases.add(new HashSet<>(Arrays.asList(MISSILES))); // hard coded
		return phases;
	}
	
	public String draw() {
		switch (this) {
		case DEFAULT:
			return "D";
		case CANNONS:
			return "C";
		case LASERS:
			return "L";
		case MISSILES:
			return "M";
		default:
			return "?";
		}
	}
}
