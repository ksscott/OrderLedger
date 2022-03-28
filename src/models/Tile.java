package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import models.Move.Direction;

public class Tile implements Drawable, Iterable<Unit> {
	public final Coordinate coord;
	private Set<Unit> units;
	
	public Tile(int row, int column) {
		this.coord = new Coordinate(row, column);
		this.units = new HashSet<>();
	}
	
	public Set<Unit> units() { return units; } // wcgw?
	
	public boolean has(Unit unit) { return units.contains(unit); }
	
	public boolean put(Unit unit) { return units.add(unit); }
	
	public boolean remove(Unit unit) { return units.remove(unit); }
	
	public boolean removeAll(Collection<Unit> units) { return this.units.removeAll(units); }
	
	public boolean isEmpty() { return this.units.isEmpty(); }
	
	@Override
	public String draw() {
		String icon = "";
		if (units.isEmpty()) {				
			icon = " - ";
		} else {
			for (Unit unit : units) {
				icon += unit.draw();
			}
		}
		return String.format("%1$" + 4 + "s", icon); // unit draw width + 1
	}
	
	public static class Coordinate {
		public final int r;
		public final int c;
		
		public Coordinate(int row, int column) {
			this.r = row;
			this.c = column;
		}
		
		public Coordinate goTo(Direction dir, int distance) {
			return new Coordinate(r+(distance*dir.r),c+(distance*dir.c));
		}
		
		/** A list of coordinates, including this coordinate and all coordinates in a path up to distance away */
		public List<Coordinate> path(Direction dir, int distance) {
			List<Coordinate> path = new ArrayList<>();
			for (int i=0; i<=distance; i++) {
				path.add(goTo(dir, i));
			}
			return path;
		}
		
		@Override
		public String toString() {
			return "Coord(" + r + "," + c + ")";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + c;
			result = prime * result + r;
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tile.Coordinate other = (Tile.Coordinate) obj;
			if (c != other.c)
				return false;
			if (r != other.r)
				return false;
			return true;
		}
	}

	@Override
	public Iterator<Unit> iterator() {
		return units.iterator();
	}
	
	public Stream<Unit> stream() {
		return units.stream();
	}
}