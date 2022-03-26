package models;

public class Move implements Order {
	public final Direction direction;
	
	public Move(Direction direction) {
		this.direction = direction;
	}
	
	public enum Direction implements Drawable {
		UP(-1,0), DOWN(1,0), LEFT(0,-1), RIGHT(0,1);
		
		public final int r;
		public final int c;
		
		Direction(int deltaRow, int deltaColumn) {
			this.r = deltaRow;
			this.c = deltaColumn;
		}
		
		@Override
		public String draw() {
			switch (this) {
			case UP:
				return "^";
			case DOWN:
				return "V";
			case LEFT:
				return "<";
			case RIGHT:
				return ">";
			default:
				return "?";
			}
		}
	}
	
	@Override
	public String draw() { return direction.draw(); }
}
