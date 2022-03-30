package main;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import models.Board;
import models.Configuration;
import models.Drawable;
import models.Move;
import models.Order;
import models.Move.Direction;
import models.Player;
import models.Reconfigure;

public abstract class Game implements Drawable {
	
	protected Board board;
	protected Player bottom;
	protected Player top;
	
	protected int turn;
	protected Player winner;
	
	public static final List<String> STOP_COMMANDS = Arrays.asList(new String[] {
			/*"stop",*/ "end", "quit", "kill", "exit", "make it stop", "please god", "why" });
	
	public Game() {
		this.turn = 1;
		this.winner = null;
	}
	
	public Player getWinner() {
		return this.winner;
	}
	
	/** 
	 * Initialize the board and top player and bottom player
	 * 
	 * @param scanner User console input scanner
	 *  - may be left open when this method returns 
	 * @param output Outputs the given string and a newline
	 */
	public abstract void setupBoard(Scanner scanner, Consumer<String> output);
	
	/** 
	 * @param scanner User console input scanner
	 *  - may be left open when this method returns
	 *  @param output Outputs the given string and a newline
	 * @return true to continue playing, false to quit playing 
	 */
	protected abstract boolean turn(Scanner scanner, Consumer<String> output);

	@Override
	public String draw() {
		StringBuilder builder = new StringBuilder();
		builder.append("Player 1: " + top.name + "\n");
		builder.append(board.draw() + "\n");
		builder.append("Player 2: " + bottom.name + "\n");
		return builder.toString();
	}
	
	protected void giveOrders(String topOrders, String bottomOrders) {
		board.issueOrders(top, decodeOrders(topOrders));
		board.issueOrders(bottom, decodeOrders(bottomOrders));
		winner = board.applyOrders();
	}
	
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
	
	protected static String orderList() {
		StringBuilder builder = new StringBuilder();
		builder.append("Directions: ");
		for (Direction dir : Direction.values()) {
			builder.append(dir.name().toLowerCase() + " ");
		}
		builder.append("Configurations: ");
		for (Configuration config : Configuration.values()) {
			builder.append(config.name().toLowerCase() + " ");
		}
		builder.append("\n");
		builder.append("Example:\n" + "missiles left right");
		return builder.toString();
	}
	
	protected static boolean shouldQuit(String input) {
		return STOP_COMMANDS.contains(input.trim().toLowerCase());
	}
}
