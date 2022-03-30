package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import models.Tile.Coordinate;

public class Board implements Drawable {
	
	private static final int UNITS_PER_PLAYER = 3;
	
	private int length;
	private int width;
	private List<Row> tileRows;
	private Player top; // player one
	private Player bottom; // player two
	private OrderFields orders;
	
	public Board(Player top, Player bottom) {
		this.top = top;
		this.bottom = bottom;
		this.orders = new OrderFields(top, bottom);
		initTiles(9, 5);
		respawnAll(top);
		respawnAll(bottom);
	}
	
	private void initTiles(int rows, int columns) {
		this.length = rows;
		this.width = columns;
		
		tileRows = new ArrayList<>();
		for (int r = 0; r < length; r++) {
			Row row = new Row();
			tileRows.add(row);
			for (int c = 0; c < width; c++) {
				row.add(new Tile(r, c));
			}
		}
	}
	
	public void issueOrders(Player player, List<Order> orders) {
		for (Unit unit : playerUnits(player)) {
			int idx = unit.index - 1;
			if (idx >= 0 && idx < orders.size()) {
				this.orders.issueOrder(unit, orders.get(idx));
			}
		}
	}
	
	public Player applyOrders() {
		reconfigureAll();
		
		moveAll();
		
		combat();
		
		respawnAll(top);
		respawnAll(bottom);
		
		orders.turn();
		
		return hasWon();
	}
	
	public Player hasWon() {
		boolean bottomHasWon = tileRows.get(0)
				.stream().flatMap(tile -> tile.units().stream())
				.anyMatch(unit -> !isTop(unit.player));
		boolean topHasWon = tileRows.get(length-1)
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
	
	private void respawnAll(Player player) {
		Set<Unit> units = playerUnits(player);
//		System.out.println("Respawning " + (PLAYER_UNITS - units.size()) + " units for " + player.name);
		
		indexloop:
		for (int i=1; i <= UNITS_PER_PLAYER; i++) {
			for (Unit existing : units) {
				if (existing.index == i) { continue indexloop; }
			}
			spawn(player, i);
		}
	}
	
	private void spawn(Player player, int index) {
		Tile tile = spawnTile(player);
		if (tile != null) {
			Unit unit = new Unit(player, index);
			tile.put(unit);
			orders.removeAll(unit);
		}
	}
	
	private Tile spawnTile(Player player) {
		List<Tile> backRow = tileRows.get(isTop(player) ? 0 : length-1);
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
	
	/* Units receive all orders here, but only act on the Reconfigure orders */
	private void reconfigureAll() {
		Map<Unit,Order> allOrders =  unitOrders(allUnits());
		for (Unit unit : allOrders.keySet()) {
			Order order = allOrders.get(unit);
			if (order instanceof Reconfigure) {
				unit.order(allOrders.get(unit));
//				orders.remove(unit, order, locate(unit).coord);
			}
		}
	}
	
	/** Units move around the board */
	private void moveAll() { // There's a bug in here somewhere
		List<Unit> allUnits = new ArrayList<>(allUnits());
		Collections.sort(allUnits); // priority is important
		Map<Unit,Order> allOrders =  unitOrders(allUnits);
		Map<Unit,List<Coordinate>> unitPaths = unitPaths(allOrders);
		
		// remove orders from field
//		for (Unit unit : allOrders.keySet()) {
//			Order order = allOrders.get(unit);
//			if (order instanceof Move) {
//				orders.remove(unit, order, locate(unit).coord);
//			}
//		}
		
		// iteratively move all units
		boolean changes = false;
		do {
			changes = false;
			for (Unit unit : allUnits) {
				List<Coordinate> path = unitPaths.get(unit);
				if (path == null) { continue; }
				Tile from = locate(unit); // should equal tile(path.get(0))
				Tile oneAhead = tile(path.get(1)); // TODO indexing safety
				
				// attempt to move 1 along the path
				// (fail if bump into walls or same-player unit)
				if (oneAhead == null) {
					continue; // off board, stop
				} else if (!oneAhead.isEmpty() && !oneAhead.equals(from)) {
					continue; // tile occupied
				} else {
					move(unit, from, oneAhead); // step forward
					path.remove(0);
					if (path.size() == 1) { // done moving
						unitPaths.remove(unit);
					}
					changes = true;
				}
			}
		} while (changes);
	}
	
	private void move(Unit unit, Tile from, Tile to) {
		if (from != null && to != null) {
			from.remove(unit);
			to.put(unit);
		}
	}

//	private Tile goToward(Tile from, Direction direction, int distance) {
//		Tile landing = from;
//		for (Coordinate toCoord : from.coord.path(direction, distance)) {
//			Tile candidate = tile(toCoord);
//			if (candidate == null) {
//				break; // off board, stop
//			} else if (!candidate.isEmpty() && !candidate.equals(from)) {
//				System.out.println("Tile occupied: " + candidate.coord);
//				break;  // tile occupied
//			} else {
//				landing = candidate; // step forward
//			}
//		}
//		return landing;
//	}
	
	private Map<Unit,Order> unitOrders(Collection<Unit> units) {
		Map<Unit,Order> allOrders = new HashMap<>();
		for (Unit unit : units) {
			Coordinate coord = locate(unit).coord;
			Order order = orders.get(unit.player).get(unit, coord);
			if (order != null) {
				allOrders.put(unit, order);
			}
		}
		return allOrders;
	}
	
	private Map<Unit,List<Coordinate>> unitPaths(Map<Unit,Order> orders) {
		Map<Unit,List<Coordinate>> unitPaths = new HashMap<>();
		for (Unit unit : orders.keySet()) {
			Order order = orders.get(unit);
			if (order instanceof Move) {
				Coordinate from = locate(unit).coord;
				List<Coordinate> path = from.path(((Move) order).direction, unit.config().speed);
				unitPaths.put(unit, path);
			}
		}
		return unitPaths;
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
		for (Row row : tileRows)
			for (Tile tile : row)
				if (tile.has(unit))
					return tile;
		return null;
	}
	
	private Tile tile(Coordinate coord) {
		if (coord.r < 0 || coord.r >= tileRows.size()
				|| coord.c < 0 || coord.c >= this.width) {
			return null; // edge of board
		}
		List<Tile> row = tileRows.get(coord.r);
		return row.get(coord.c);
	}
	
	/** @return Set of Units that match the given check */
	private Set<Unit> allUnits(Predicate<Unit> check) {
		return tileRows.stream()
			.flatMap(Row::stream)
			.flatMap(Tile::stream)
			.filter(check)
			.collect(Collectors.toSet());
	}
	
	private Set<Unit> allUnits() { return allUnits(unit -> true); }
	
	private Set<Unit> playerUnits(Player player) { return allUnits(u -> u.player.equals(player)); }
	
	private void kill(Set<Unit> dead) {
		dead.remove(null);
		if (dead.isEmpty()) { return; }
		for (List<Tile> row : tileRows) {
			for (Tile tile : row) {
				if(tile.removeAll(dead)) { // performant?
//					System.out.println("Removing dead at ("+tile.coord.r+","+tile.coord.c+")");
				}
			}
		}
//		System.out.println("Remaining units: " + allUnits().size());
	}
	
	/* Square grid logic */
	private Set<Tile> radius(Tile from, int radius) {
		Set<Tile> adjacent = new HashSet<>();
		for (Row row : tileRows) {
			for (Tile tile : row) {
				if (Math.abs(tile.coord.c - from.coord.c) + Math.abs(tile.coord.r - from.coord.r) <= radius) {
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
					System.out.println(shooter.draw() + " killed " + canSee);
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
							System.out.println(shooter.draw() + " killed " + target);
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
						System.out.println(shooter.draw() + " killed " + target);
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
	
	@Override
	public String draw() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			List<Tile> row = tileRows.get(i);
			
			builder.append("\n");
			if (i % 3 == 1) {
				builder.append(orders.get(top).draw(row.get(0).coord, true));
			} else {
				appendSpaces(builder, 9);
			}
			builder.append(" | ");
			for (Tile tile : row) {
				builder.append(tile.draw());
			}
			builder.append("  | ");
			if (i % 3 == 1) {
				builder.append(orders.get(bottom).draw(row.get(0).coord, false));
			} else {
				appendSpaces(builder, 9);
			}
			builder.append("\n");
			appendSpaces(builder, 9);
			builder.append(" | ");
			appendSpaces(builder, 4*width);
			builder.append("  | ");
			appendSpaces(builder, 9);
		}
		return builder.toString();
	}
	
	private static StringBuilder appendSpaces(StringBuilder builder, int number) {
		for (int i=0; i<number; i++) {
			builder.append(" ");
		}
		return builder;
	}
	
	private class OrderFields {
		Map<Player,OrderField> playerOrders;
		
		public OrderFields(Player topPlayer, Player bottomPlayer) {
			this.playerOrders = new HashMap<>();
			playerOrders.put(topPlayer, new OrderField());
			playerOrders.put(bottomPlayer, new OrderField());
		}
		
		public OrderField get(Player player) { return playerOrders.get(player); }
		
		public void issueOrder(Unit unit, Order order) {
			if (unit == null || order == null) { return; }
			playerOrders.get(unit.player).issue(unit, order);
		}
		
		public void remove(Unit unit, Order order, Coordinate coord) {
			get(unit.player).remove(unit, order, coord);
		}
		
		public void removeAll(Unit unit) {
			OrderField orders = playerOrders.get(unit.player);
			orders.removeAll(unit);
			
		}
		
		public void turn() { playerOrders.values().forEach(of -> of.turn()); }
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
		
		public void removeAll(Unit unit) {
			pending.remove(unit);
			near.remove(unit);
			middle.remove(unit);
			far.remove(unit);
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
		public String draw(int row, boolean top) { return drawRegion(region(row, top)); }
		
		private String drawRegion(Map<Unit,Order> region) {
			final StringBuilder icon = new StringBuilder();
			Set<Unit> units = region.keySet();
			for (int i=1; i<=UNITS_PER_PLAYER; i++) {
				int idx = i;
				// FIXME handle duplicate indices: 
				Optional<Unit> unit = units.stream().filter(u -> u.index == idx).findAny();
				if (unit.isPresent()) {
					Order order = region.get(unit.get());
					if (order != null) {
						icon.append(i + order.draw() + " "); // 3 characters wide
					}
				}
			}
			return String.format("%1$" + 3*UNITS_PER_PLAYER + "s", icon.toString());
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
