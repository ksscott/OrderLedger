package models;

import static models.Configuration.*;

public class Unit implements Drawable, Comparable<Unit> {
	public final Player player;
	public final int index;
	private Configuration config;
	private Order lastOrder;
	
	public Unit(Player player, int index) {
		this.player = player;
		this.index = index;
		this.config = DEFAULT;
	}
	
	public void order(Order order) {
		lastOrder = order;
		if (order instanceof Reconfigure) {
			reconfigure((Reconfigure) order);
		}
	}
	
	public Order lastOrder() {
		return lastOrder;
	}
	
	private void reconfigure(Reconfigure order) {
		// TODO change this?
		if (this.config == DEFAULT) {
			this.config = order.config;
		} else if (this.config != order.config) {
			this.config = DEFAULT;
		}
	}
	
	public Configuration config() { return this.config; }
	
	@Override
	public String draw() { return player.playerIndex + "" + index + config.draw(); }

	@Override
	public int compareTo(Unit o) {
		if (index != o.index) {
			return index - o.index;
		} else {
			return player.playerIndex - o.player.playerIndex;
		}
	}

	// I tinkered in here. I feel confident I got it right. Famous last words...
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((player == null) ? 0 : player.playerIndex);
		return result;
	}

	// I tinkered in here. I feel confident I got it right. Famous last words...
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Unit other = (Unit) obj;
		if (index != other.index)
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (player.playerIndex != other.player.playerIndex)
			return false;
		return true;
	}
}
