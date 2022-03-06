package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import models.Move.Direction;

public class Board {
	
	private static final int PLAYER_UNITS = 3;
	
	private int length;
	private int width;
	private Player top;
	private Player bottom;
	private List<List<Tile>> tiles;
	private OrderField topOrders;
	private OrderField bottomOrders;
	
	public Board(Player top, Player bottom) {
		this.top = top;
		this.bottom = bottom;
		initTiles(9, 5);
		respawnAll(top);
		respawnAll(bottom);
	}
	
	private void initTiles(int rows, int columns) {
		this.length = rows;
		this.width = columns;
		this.topOrders = new OrderField();
		this.bottomOrders = new OrderField();
		tiles = new ArrayList<>();
		for (int r = 0; r < rows; r++) {
			List<Tile> list = new ArrayList<>();
			tiles.add(list);
			for (int c = 0; c < columns; c++) {
				list.add(new Tile(r, c));
			}
		}
	}
	
	public void issueOrder(Unit unit, Order order) {
		if (isTop(unit.player)) {
			topOrders.issue(unit, order);
		} else {
			bottomOrders.issue(unit, order);
		}
	}
	
	public void applyOrders() {
		// for each unit, get that units orders if any, execute them
		for (Unit unit : allUnits()) {
			Tile location = locate(unit);
			OrderField orders;
			if (isTop(unit.player)) {
				orders = topOrders;
			} else {
				orders = bottomOrders;
			}
			Order order = orders.get(unit, location.coord);
			if (order != null) {
				order(unit, order);
				orders.remove(unit, order, location.coord);
			}
		}
		
		combat();
		
		respawnAll(top);
		respawnAll(bottom);
		
		topOrders.turn();
		bottomOrders.turn();
	}
	
	public void spawn(int r, int c, Player player) {
		tiles.get(r).get(c).put(new Unit(player)); // TODO sanitize inputs
	}
	
	private void respawnAll(Player player) {
		Set<Unit> units = allUnits();
		units.removeIf(unit -> !unit.player.equals(player) );
//		System.out.println("Respawning " + (PLAYER_UNITS - units.size()) + " units for " + player.name);
		
		for (int i=0; i < PLAYER_UNITS - units.size(); i++) {
			spawnTile(player).put(new Unit(player)); // TODO null safety
		}
	}
	
	private Tile spawnTile(Player player) {
		List<Tile> backRow = tiles.get(isTop(player) ? 0 : length-1);
		int center = width / 2;
		
		Tile tile = backRow.get(center);
		if (tile.units().isEmpty()) {
			return tile;
		}
		tile = backRow.get(center-1);
		if (tile.units().isEmpty()) {
			return tile;
		}
		tile = backRow.get(center+1);
		if (tile.units().isEmpty()) {
			return tile;
		}
		return null;
	}
	
	private void order(Unit unit, Order order) {
		if (order instanceof Reconfigure) {
			unit.order((Reconfigure) order);
		} else if (order instanceof Move) {
			move(unit, (Move) order);
		} else {
			throw new RuntimeException("unrecognized order given: " + order);
		}
	}
	
	// units shoot each other and are removed from the board
	private void combat() {
		Set<Unit> allUnits = allUnits();
		Set<Unit> dead = new HashSet<>();
		
		for (Set<Configuration> phase : Configuration.phases()) {
			for (Unit shooter : allUnits) {
				if (phase.contains(shooter.config())) {
					dead.add(target(shooter)); // FIXME targeting should be optimized
				}
			}
			allUnits.removeAll(dead);
		}
		kill(dead);
	}
	
	private Tile locate(Unit unit) {
		for (List<Tile> row : tiles)
			for (Tile tile : row)
				if (tile.has(unit))
					return tile;
		return null;
	}
	
	public Set<Unit> allUnits() {
		Set<Unit> all = new HashSet<>();
		for (List<Tile> row : tiles) {
			for (Tile tile : row) {
				all.addAll(tile.units());
			}
		}
		return all;
	}
	
	private void kill(Set<Unit> dead) {
		dead.remove(null);
		if (dead.isEmpty()) { return; }
		for (List<Tile> row : tiles) {
			for (Tile tile : row) {
				if(tile.removeAll(dead)) { // performant?
//					System.out.println("Removing dead at ("+tile.coord.r+","+tile.coord.c+")");
				}
			}
		}
//		System.out.println("Remaining units: " + allUnits().size());
	}
	
	private void move(Unit unit, Move order) {
		Tile from = locate(unit);
		Tile to = goTo(from, order.direction, unit.config().speed);
		if (from != null && to != null) {
			from.remove(unit);
			to.put(unit);
		}
	}
	
	private Tile goTo(Tile from, Direction direction, int distance) {
		Coordinate toCoord = from.coord.goTo(direction, distance);
		if (toCoord.r < 0 || toCoord.r >= tiles.size()
				|| toCoord.c < 0 || toCoord.c >= this.width) {
			return null; // edge of board
		}
		List<Tile> row = tiles.get(toCoord.r);
		return row.get(toCoord.c);
	}
	
	/* Square grid logic */
	private Set<Tile> radius(Tile from, int radius) {
		Set<Tile> adjacent = new HashSet<>();
		for (List<Tile> row : this.tiles) {
			for (Tile tile : row) {
				if (Math.abs(tile.coord.c - from.coord.c) <= radius
						&& Math.abs(tile.coord.r - from.coord.r) <= radius) {
					adjacent.add(tile);
				}
			}
		}
		return adjacent;
	}
	
	/* Choose a target to be shot by the given shooter */
	private Unit target(Unit shooter) {
		Tile from = locate(shooter);
		
		// same tile
		for (Unit unit : from.units()) {
			if (!unit.player.equals(shooter.player)) {
				System.out.println(shooter.player.name + "'s unit targeting " + unit.player.name + "'s unit");
				return unit; // some randomness if multiple enemy units here?
			}
		}
		// adjacent tiles
		Set<Unit> targetable = radius(from, shooter.config().range).stream()
				.flatMap(tile -> tile.units().stream())
				.collect(Collectors.toSet());
//		System.out.println("Targetable units: "+ targetable.size());
		for (Unit unit : targetable) {
			if (!unit.player.equals(shooter.player)) {
				System.out.println(shooter.player.name + "'s unit targeting " + unit.player.name + "'s unit");
				return unit; // some randomness if multiple enemy units here?
			}
		}
		
		return null;
	}
	
	private boolean isTop(Player player) {
		if (player.equals(top)) {
			return true;
		} else if (player.equals(bottom)) {
			return false;
		} else {
			throw new RuntimeException("unrecognized player");
		}
	}
	
	public String draw() {
		StringBuilder builder = new StringBuilder();
		for (List<Tile> row : tiles) {
			builder.append("\n");
			for (Tile tile : row) {
				builder.append(tile.draw());
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	public static class Tile {
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
		
		public String draw() {
			String icon = "";
			if (units.isEmpty()) {				
				icon = "-";
			} else {
				for (Unit unit : units) {
					icon += unit.draw();
				}
			}
			return String.format("%1$" + 3 + "s", icon);
		}
	}
	
	private static class Coordinate {
		public final int r;
		public final int c;
		
		public Coordinate(int row, int column) {
			this.r = row;
			this.c = column;
		}
		
		public Coordinate goTo(Direction dir, int distance) {
			return new Coordinate(r+(distance*dir.r),c+(distance*dir.c));
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
			Coordinate other = (Coordinate) obj;
			if (c != other.c)
				return false;
			if (r != other.r)
				return false;
			return true;
		}
	}
	
	private class OrderField {
		private Map<Unit,Order> pending;
		private Map<Unit,Order> near;
		private Map<Unit,Order> middle;
		private Map<Unit,Order> far;
		
		public OrderField() {
			this.pending = new HashMap<>();
			this.near = new HashMap<>();
			this.middle = new HashMap<>();
			this.far = new HashMap<>();
		}
		
		public Order get(Unit unit, Coordinate coord) {
			Map<Unit,Order> orders = region(coord, isTop(unit.player));
			return orders.get(unit);
		}
		
		public void issue(Unit unit, Order order) {
//			System.out.println("Issuing Order: " + unit.player.name + " " + unit.draw() + " " + order);
			pending.put(unit, order);
		}
		
		public void remove(Unit unit, Order order, Coordinate coord) {
			Map<Unit,Order> orders = region(coord, isTop(unit.player));
			orders.remove(unit, order);
		}
		
		public void turn() {
			far = middle;
			middle = near;
			near = pending;
			pending = new HashMap<>();
		}
		
		private Map<Unit,Order> region(Coordinate coord, boolean top) {
			if (coord.r < 0) {
				throw new RuntimeException("coordinates off the top of the board");
			} else if (coord.r < 3) { // FIXME
				return top ? near : far;
			} else if (coord.r < 6) { // FIXME
				return middle;
			} else if (coord.r < Board.this.length) {
				return top ? far : near;
			} else {
				throw new RuntimeException("coordinates off the bottom of the board");
			}
		}
	}
}
