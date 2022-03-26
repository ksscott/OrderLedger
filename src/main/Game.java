package main;


import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import models.Board;
import models.Configuration;
import models.Move;
import models.Move.Direction;
import models.Order;
import models.Player;
import models.Reconfigure;
import models.Unit;

public class Game {
	
	private static Board board;
	private static Player bottom;
	private static Player top;
	private static Player winner = null;
	
	private static int turn;
	
	private static final List<String> STOP_COMMANDS = Arrays.asList(new String[] { "stop", "end", "quit", "kill", "exit" });
	
	public static void main(String[] args) {
		
		Scanner scanner = new Scanner(System.in);
		System.out.println();
		System.out.println("  ( (  ----------------  ) )  ");
		System.out.println("( ( (                    ) ) )");
		System.out.println("( (      ORDER LEDGER      ) )");
		System.out.println("( ( (                    ) ) )");
		System.out.println("  ( (  ----------------  ) )  ");
		System.out.println();
		
		System.out.println("Top Player:");
		top = new Player(scanner.nextLine());
		System.out.println("Bottom Player:");
		bottom = new Player(scanner.nextLine());
		System.out.println();
		
		board = new Board(top, bottom);
		
		turn = 1;
		
		while(turn(scanner) && winner == null) {}
		scanner.close();
		
		if (winner != null) {
			System.out.println(board.draw());
			System.out.println("<<< " +  winner.name + " wins! >>>");
		}
		System.out.println();
		System.out.println("Thanks for playing!");
	}
	
	private static boolean turn(Scanner scanner) {
		draw();
		System.out.println();
		System.out.println("((( Turn " + turn++ + " )))");
		System.out.println("Type 'help' for order list");
		System.out.println("\n" + top.name + "'s orders: ");
		String topOrders = scanner.nextLine().trim();
		if (topOrders.toLowerCase().equals("help")) {
			System.out.println("(up/down/left/right OR default/cannons/scout/missiles)");
			topOrders = scanner.nextLine().trim();
		}
		if (STOP_COMMANDS.contains(topOrders.toLowerCase())) { return false; }
		System.out.println("\n" + bottom.name + "'s orders: ");
		String bottomOrders = scanner.nextLine().trim();
		if (bottomOrders.toLowerCase().equals("help")) {
			System.out.println("(up/down/left/right OR default/cannons/scout/missiles)");
			bottomOrders = scanner.nextLine().trim();
		}
		if (STOP_COMMANDS.contains(bottomOrders.toLowerCase())) { return false; }
		
		giveOrders(topOrders, bottomOrders);
		
		return true;
	}
	
	private static void draw() {
		System.out.println("Top Player: " + top.name);
		System.out.println(board.draw());
		System.out.println("Bottom Player: " + bottom.name);
	}
	
	private static void giveOrders(String topOrders, String bottomOrders) {
		Set<Unit> units = board.allUnits();
		
		for (Unit unit : units) {
			if (unit.player.equals(top)) {
				board.issueOrder(unit, decode(topOrders));
			} else if (unit.player == bottom) {
				board.issueOrder(unit, decode(bottomOrders));
			}
		}
		
		winner = board.applyOrders();
	}
	
	private static Order decode(String input) {
		String command = input.toUpperCase();
		for (Direction dir : Direction.values()) {
			if (command.equals(dir.name())) {
				return new Move(dir);
			}
		}
		for (Configuration config : Configuration.values()) {
			if (command.equals(config.name())) {
				return new Reconfigure(config);
			}
		}
		return null;
	}
}
