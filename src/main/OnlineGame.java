package main;


import java.util.Scanner;
import java.util.function.Consumer;

import models.Board;
import models.Player;
import networking.Client;
import networking.Host;
import networking.Server;

public class OnlineGame extends Game {
	
	private final boolean hosting;
	private Server server;
	
	public OnlineGame(boolean hosting) {
		super();
		this.hosting = hosting;
	}

	@Override
	public void setupBoard(Scanner scanner, Consumer<String> output) {
		output.accept("Player Name:");
		int meIndex = hosting ? 1 : 2;
		Player me = new Player(scanner.nextLine().trim(), meIndex);
		output.accept("");
		
		server = connect(me.name, hosting, scanner, output);
		
		String theirName = server.pollDataFromServer();
		while (theirName == null) {
			try {
				Thread.sleep(Server.POLL_INTERVAL_MILLIS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			theirName = server.pollDataFromServer();
			// FIXME allow user to quit while waiting for other user
		}
		
		int themIndex = hosting ? 2 : 1;
		Player them = new Player(theirName, themIndex);
		
		top = hosting ? me : them;
		bottom = hosting ? them : me;
		
		board = new Board(top, bottom);
	}

	@Override
	protected boolean turn(Scanner scanner, Consumer<String> output) {
		output.accept(draw());
		output.accept("");
		output.accept("((( Turn " + turn++ + " )))");
		output.accept("Type 'help' for order list");
		output.accept("\n" + "Enter orders: ");
		String myOrders = scanner.nextLine().trim();
		if (myOrders.toLowerCase().equals("help")) {
			output.accept(orderList());
			output.accept("\n" + "Enter orders: ");
			myOrders = scanner.nextLine().trim();
		}
		if (shouldQuit(myOrders)) {
			server.stop();
			return false;
		}
		
		server.addUserInput(myOrders);
		String theirOrders = server.pollDataFromServer();
		while (theirOrders == null) {
			try {
				Thread.sleep(Server.POLL_INTERVAL_MILLIS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			theirOrders = server.pollDataFromServer();
			// FIXME allow user to quit while waiting for other user
		}
		if (shouldQuit(theirOrders)) {
			server.stop();
			server.addUserInput(Server.QUIT_STRING); // clunky, but whatever
			return false;
		}
		
		if (hosting) {
			giveOrders(myOrders, theirOrders);
		} else { // client
			giveOrders(theirOrders, myOrders);
		}
		
		return true;
	}
	
	/**
	 * @param host true to host a game server, false to join another host
	 * @param scanner user input, will not be closed in this method
	 * @return a connected server, or null to indicate a quit order
	 */
	private static Server connect(String playerName, boolean hosting, Scanner scanner, Consumer<String> output) {
		boolean connected = false;
		Server server = null;
		if (hosting) {
			while (!connected) {
				server = new Host(playerName);
				connected = server.start();
				// FIXME allow user to quit during connection
			}
		} else { // client
			while (!connected) {
				output.accept("Enter host IP Address:");
				String input = scanner.nextLine().trim();
				if (shouldQuit(input)) { return null; }
				server = new Client(playerName, input);
				connected = server.start();
				// FIXME allow user to quit during connection
			}
		}
		return server;
	}
}
