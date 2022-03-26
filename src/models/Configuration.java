package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public enum Configuration implements Drawable {
	DEFAULT(1,1,0), CANNONS(1,1,2), SCOUT(2,1,1), MISSILES(1,2,0);
	
	public final int speed;
	public final int range;
	public final int initiative;
	
	Configuration(int speed, int range, int initiative) {
		this.speed = speed;
		this.range = range;
		this.initiative = initiative;
	}
	
	public static List<Set<Configuration>> phases() {
		List<Set<Configuration>> phases = new ArrayList<>();
		phases.add(new HashSet<>(Arrays.asList(CANNONS))); // hard coded
		phases.add(new HashSet<>(Arrays.asList(SCOUT))); // hard coded
		phases.add(new HashSet<>(Arrays.asList(DEFAULT, MISSILES))); // hard coded
		return phases;
	}
	
	@Override
	public String draw() {
		switch (this) {
		case DEFAULT:
			return "D";
		case CANNONS:
			return "C";
		case SCOUT:
			return "S";
		case MISSILES:
			return "M";
		default:
			return "?";
		}
	}
}
