package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import models.Move.Direction;
import models.Tile.Coordinate;

public class Board {
	
	private static final int PLAYER_UNITS = 3;
	
	private int length;
	private int width;
	private List<Row> tiles;
	private Player top;
	private Player bottom;
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
		for (int r = 0; r < length; r++) {
			Row row = new Row();
			tiles.add(row);
			for (int c = 0; c < width; c++) {
				row.add(new Tile(r, c));
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
	
	public Player applyOrders() {
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
		
		return hasWon();
	}
	
	public Player hasWon() {
		boolean bottomHasWon = tiles.get(0)
				.stream().flatMap(tile -> tile.units().stream())
				.anyMatch(unit -> !isTop(unit.player));
		boolean topHasWon = tiles.get(length-1)
				.stream().flatMap(tile -> tile.units().stream())
				.anyMatch(unit -> isTop(unit.player));
		
		if (bottomHasWon == topHasWon) {
			return null; // tie or no win yet
		} else if (bottomHasWon) {
			return bottom;
		} else {
			return top;
		}
	}
	
	public void spawn(int r, int c, Player player) {
		tiles.get(r).get(c).put(new Unit(player)); // TODO sanitize inputs
	}
	
	private void respawnAll(Player player) {
		Set<Unit> units = allUnits();
		units.removeIf(unit -> !unit.player.equals(player) );
//		System.out.println("Respawning " + (PLAYER_UNITS - units.size()) + " units for " + player.name);
		
		for (int i=0; i < PLAYER_UNITS - units.size(); i++) {
			Tile spawnTile = spawnTile(player);
			if (spawnTile != null) {
				spawnTile.put(new Unit(player));
			}
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
	
	/** Execute the given Order for the given Unit */
	private void order(Unit unit, Order order) {
		if (order instanceof Reconfigure) {
			unit.order((Reconfigure) order);
		} else if (order instanceof Move) {
			move(unit, (Move) order);
		} else {
			throw new RuntimeException("unrecognized order given: " + order);
		}
	}
	
	/** Units shoot each other and are removed from the board */
	private void combat() {
		Set<Unit> allUnits = allUnits();
		Set<Unit> dead = new HashSet<>();
		
		for (Set<Configuration> phase : Configuration.phases()) {
			Map<Unit,Set<Unit>> targets = new HashMap<>();
			for (Unit shooter : allUnits) {
				if (phase.contains(shooter.config())) {
					targets.put(shooter, targetableBy(shooter));
				}
			}
			dead.addAll(chooseTargets(targets));
			allUnits.removeAll(dead);
		}
		kill(dead);
	}
	
	private Tile locate(Unit unit) {
		for (Row row : tiles)
			for (Tile tile : row)
				if (tile.has(unit))
					return tile;
		return null;
	}
	
	private Tile tile(Coordinate coord) {
		if (coord.r < 0 || coord.r >= tiles.size()
				|| coord.c < 0 || coord.c >= this.width) {
			return null; // edge of board
		}
		List<Tile> row = tiles.get(coord.r);
		return row.get(coord.c);
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
		Tile to = goToward(from, order.direction, unit.config().speed);
		if (from != null && to != null) {
			from.remove(unit);
			to.put(unit);
		}
	}
	
	private Tile goTo(Tile from, Direction direction, int distance) {
		return tile(from.coord.goTo(direction, distance));
	}
	
	private Tile goToward(Tile from, Direction direction, int distance) {
		Tile landing = from;
		for (Coordinate toCoord : from.coord.path(direction, distance)) {
			Tile candidate = tile(toCoord);
			if (candidate == null) {
				break; // off board, stop
			} else if (false) { // handle "tile occupied" or similar
				break;
			} else {
				landing = candidate; // step forward
			}
		}
		return landing;
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
	private Set<Unit> targetableBy(Unit shooter) {
		Set<Tile> visible = radius(locate(shooter), shooter.config().range);
		
		Set<Unit> targetable = visible.stream()
				.flatMap(tile -> tile.units().stream())
				.collect(Collectors.toSet());
		targetable.removeIf(unit -> unit.player.equals(shooter.player));
		
		return targetable;
	}
	
	/* This algorithm is imperfect,
	 * but I think it's guaranteed to shoot the maximum units possible. 
	 */
	private Set<Unit> chooseTargets(Map<Unit,Set<Unit>> targetable) {
		Set<Unit> dead = new HashSet<>();
		
		// clean inputs
		if (targetable == null || targetable.isEmpty()) { return dead; }
		targetable.remove(null);
		targetable.values().forEach(set -> set.remove(null));
		Set<Unit> shootersToRemove = new HashSet<>();
		targetable.keySet().forEach(shooter -> {
			if (targetable.get(shooter).isEmpty()) {
				shootersToRemove.add(shooter);
			}
		});
		shootersToRemove.forEach(unit -> targetable.remove(unit));
		
		Set<Unit> hasShot = new HashSet<>();
		
		int iters = 0;
		while (!targetable.isEmpty()) {
			// can shoot only one target:
			for (Unit shooter : targetable.keySet()) {
				Set<Unit> canSee = targetable.get(shooter);
				if (canSee.size() == 1 && !hasShot.contains(shooter)) {
					hasShot.add(shooter);
					dead.addAll(canSee);
				}
			}
			// Can be shot by only one shooter:
			Map<Unit,Integer> targets = timesTargetable(targetable);
			for (Unit target : targets.keySet()) {
				if (targets.get(target) == 1) {
					for (Unit shooter : targetable.keySet()) {
						if (!hasShot.contains(shooter) && targetable.get(shooter).contains(target)) {
							hasShot.add(shooter);
							dead.add(target);
						}
					}
				}
			}
			// "So anyway, I started blasting..."
			for (Unit shooter : targetable.keySet()) {
				if (hasShot.contains(shooter)) { continue; }
				for (Unit target : targetable.get(shooter)) {
					if (!dead.contains(target)) {
						hasShot.add(shooter);
						dead.add(target);
					}
				}
			}
			
			hasShot.forEach(unit -> targetable.remove(unit)); // has shot
			targetable.keySet().forEach(shooter -> {
				Set<Unit> canSee = targetable.get(shooter);
				canSee.removeAll(dead);
				if (canSee.isEmpty())
					shootersToRemove.add(shooter); // can't shoot anymore
			});
			shootersToRemove.forEach(unit -> targetable.remove(unit));
			if (++iters > 10) {
				break; // I'm fairly certain this loop terminates, but just in case
			}
		}
		return dead;
	}
	
	/**
	 * @param targetable mapping of possible shooters to targets they can see
	 * @return mapping of targets to number of shooters that can see them
	 */
	private Map<Unit,Integer> timesTargetable(Map<Unit,Set<Unit>> targetable) {
		final Map<Unit,Integer> targets = new HashMap<>();
		targetable.values().stream().forEach(set -> set.forEach(unit -> targets.put(unit, 0)));
		targetable.values().stream().forEach(set -> set.forEach(unit -> targets.put(unit, targets.get(unit)+1)));
		return targets;
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
		for (int i = 0; i < length; i++) {
			List<Tile> row = tiles.get(i);
			
			builder.append("\n");
			if (i % 3 == 1) {
				builder.append(topOrders.draw(row.get(0).coord, true));
			} else {
				builder.append("   ");
			}
			builder.append(" | ");
			for (Tile tile : row) {
				builder.append(tile.draw());
			}
			builder.append("   | ");
			if (i % 3 == 1) {
				builder.append(bottomOrders.draw(row.get(0).coord, false));
			} else {
				builder.append("   ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	/** A divided space containing Orders for Units */
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
			Map<Unit,Order> orders = region(coord.r, isTop(unit.player));
			return orders.get(unit);
		}
		
		public void issue(Unit unit, Order order) {
//			System.out.println("Issuing Order: " + unit.player.name + " " + unit.draw() + " " + order);
			pending.put(unit, order);
		}
		
		public void remove(Unit unit, Order order, Coordinate coord) {
			Map<Unit,Order> orders = region(coord.r, isTop(unit.player));
			orders.remove(unit, order);
		}
		
		public void turn() {
			far = middle;
			middle = near;
			near = pending;
			pending = new HashMap<>();
		}
		
		/** Draw the orders for a player that apply to the given coordinate */
		public String draw(Coordinate coord, boolean top) { return draw(coord.r, top); }
		
		/** Draw the orders for a player that apply to the given coordinate */
		public String draw(int row, boolean top) {
			return drawRegion(region(row, top));
		}
		
		private String drawRegion(Map<Unit,Order> region) {
			String icon = "";
			for (Order order : region.values()) {
				if (order != null) {
					icon += order.draw();
				}
			}
			return String.format("%1$" + 3 + "s", icon);
		}
		
		private Map<Unit,Order> region(int row, boolean top) {
			if (row < 0) {
				throw new RuntimeException("coordinates off the top of the board");
			} else if (row < (Board.this.length/3)) { // FIXME
				return top ? near : far;
			} else if (row < (2*Board.this.length/3)) { // FIXME
				return middle;
			} else if (row < Board.this.length) {
				return top ? far : near;
			} else {
				throw new RuntimeException("coordinates off the bottom of the board");
			}
		}
	}
}
