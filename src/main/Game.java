package main;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import models.Board;
import models.Configuration;
import models.Move;
import models.Move.Direction;
import models.Order;
import models.Player;
import models.Reconfigure;

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
		top = new Player(scanner.nextLine(), 1);
		System.out.println("Bottom Player:");
		bottom = new Player(scanner.nextLine(), 2);
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
			System.out.println(orderList());
			topOrders = scanner.nextLine().trim();
		}
		if (STOP_COMMANDS.contains(topOrders.toLowerCase())) { return false; }
		System.out.println("\n" + bottom.name + "'s orders: ");
		String bottomOrders = scanner.nextLine().trim();
		if (bottomOrders.toLowerCase().equals("help")) {
			System.out.println(orderList());
			bottomOrders = scanner.nextLine().trim();
		}
		if (STOP_COMMANDS.contains(bottomOrders.toLowerCase())) { return false; }
		
		giveOrders(topOrders, bottomOrders);
		
		return true;
	}
	
	private static void draw() {
		System.out.println("Player 1: " + top.name);
		System.out.println(board.draw());
		System.out.println("Player 2: " + bottom.name);
	}
	
	private static void giveOrders(String topOrders, String bottomOrders) {
		board.issueOrders(top, decodeOrders(topOrders));
		board.issueOrders(bottom, decodeOrders(bottomOrders));
		winner = board.applyOrders();
	}
	
	// TODO refactor out of this class
	private static List<Order> decodeOrders(String input) {
		List<Order> orders = new ArrayList<>();
		String[] words = input.split("\\s+");
		for (String word : words) {
			orders.add(decode(word));
		}
		return orders;
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
	
	private static String orderList() {
		StringBuilder builder = new StringBuilder();
		builder.append("Directions: ");
		for (Direction dir : Direction.values()) {
			builder.append(dir.name().toLowerCase() + " ");
		}
		builder.append("Configurations: ");
		for (Configuration config : Configuration.values()) {
			builder.append(config.name().toLowerCase() + " ");
		}
		return builder.toString();
	}
}
