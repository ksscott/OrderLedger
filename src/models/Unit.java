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
		return index - o.index;
	}
}
