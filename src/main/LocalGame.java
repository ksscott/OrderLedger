package main;

import java.util.Scanner;
import java.util.function.Consumer;

import models.Board;
import models.Player;

public class LocalGame extends Game {

	@Override
	public void setupBoard(Scanner scanner, Consumer<String> output) {
		output.accept("Top Player:");
		top = new Player(scanner.nextLine().trim(), 1);
		output.accept("Bottom Player:");
		bottom = new Player(scanner.nextLine().trim(), 2);
		output.accept("");
		
		board = new Board(top, bottom);
	}
	
	@Override
	protected boolean turn(Scanner scanner, Consumer<String> output) {
		output.accept(draw());
		output.accept("");
		output.accept("((( Turn " + turn++ + " )))");
		output.accept("Type 'help' for order list");
		output.accept("\n" + top.name + "'s orders: ");
		String topOrders = scanner.nextLine().trim();
		if (topOrders.toLowerCase().equals("help")) {
			output.accept(orderList());
			output.accept("\n" + top.name + "'s orders: ");
			topOrders = scanner.nextLine().trim();
		}
		if (shouldQuit(topOrders)) { return false; }
		output.accept("\n" + bottom.name + "'s orders: ");
		String bottomOrders = scanner.nextLine().trim();
		if (bottomOrders.toLowerCase().equals("help")) {
			output.accept(orderList());
			output.accept("\n" + bottom.name + "'s orders: ");
			bottomOrders = scanner.nextLine().trim();
		}
		if (shouldQuit(bottomOrders)) { return false; }
		
		giveOrders(topOrders, bottomOrders);
		
		return true;
	}

}
