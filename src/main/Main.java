package main;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		output(bannerString());
		
		Scanner scanner = new Scanner(System.in);
		
		GameType type = null;
		while(type == null) {
			output("Start a game: \n 'local' \n 'host' \n 'join' \n 'quit' \n");
			String input = scanner.nextLine().trim();
			output("");
			if ("quit".equals(input.toLowerCase())) {
				scanner.close();
				output("Thanks for playing!");
				return;
			}
			try {
				type = GameType.valueOf(input.toUpperCase());
			} catch (IllegalArgumentException e) {
				output("Unrecognized command");
			}
		}
		
		Game game;
		
		switch (type) {
		case LOCAL:
			game = new LocalGame();
			break;
		case HOST:
			game = new OnlineGame(true);
			break;
		default:
		case JOIN:
			game = new OnlineGame(false);
			break;
		}
		
		game.setupBoard(scanner, Main::output);
		
		while(game.turn(scanner, Main::output) && game.getWinner() == null) {}
		
		scanner.close();
		
		if (game.getWinner() != null) {
			output(game.draw());
			output("<<< " +  game.getWinner().name + " wins! >>>");
		}
		
		output("");
		output("Thanks for playing!");
	}
	
	/** Outputs the given string and a newline */
	public static void output(String output) {
		System.out.println(output);
	}
	
	private static String bannerString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("  ( (  ----------------  ) )  " + "\n");
		builder.append("( ( (                    ) ) )" + "\n");
		builder.append("( (      ORDER LEDGER      ) )" + "\n");
		builder.append("( ( (                    ) ) )" + "\n");
		builder.append("  ( (  ----------------  ) )  " + "\n");
		builder.append("\n");
		return builder.toString();
	}

	enum GameType {
		LOCAL, HOST, JOIN;
	}
}
