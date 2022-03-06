package models;

public class Move implements Order {
	public final Direction direction;
	
	public Move(Direction direction) {
		this.direction = direction;
	}
	
	public enum Direction {
		UP(-1,0), DOWN(1,0), LEFT(0,-1), RIGHT(0,1);
		
		public final int r;
		public final int c;
		
		Direction(int deltaRow, int deltaColumn) {
			this.r = deltaRow;
			this.c = deltaColumn;
		}
	}
}
