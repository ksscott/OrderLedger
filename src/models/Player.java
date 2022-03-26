package models;

public class Player {
	public final String name;
	public int playerIndex; // player one / player two
	
	public Player(String name, int index) {
		this.name = name;
		this.playerIndex = index;
	}
}
