package models;

public class Move implements Order {
	public Direction direction;
	
	public Move(Direction direction) {
		this.direction = direction;
	}
	
	public enum Direction implements Drawable {
		UP(-1,0), DOWN(1,0), LEFT(0,-1), RIGHT(0,1), NONE(0,0);
		
		public final int r;
		public final int c;
		
		Direction(int deltaRow, int deltaColumn) {
			this.r = deltaRow;
			this.c = deltaColumn;
		}
		
		public Direction mirror() {
			switch (this) {
			case UP:
				return DOWN;
			case DOWN:
				return UP;
			// DON'T flip left/right
//			case LEFT:
//				return RIGHT;
//			case RIGHT:
//				return LEFT;
			case NONE:
				return NONE;
			default:
				return this;
			}
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
			case NONE:
				return "#";
			default:
				return "?";
			}
		}
	}
	
	@Override
	public String draw() { return direction.draw(); }
	
	@Override
	public void mirror() {
		this.direction = this.direction.mirror();
	}
}
