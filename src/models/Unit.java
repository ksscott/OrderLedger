package models;

import static models.Configuration.*;

public class Unit implements Drawable {
	public final Player player;
	private Configuration config;
	
	public void order(Reconfigure order) {
		// TODO change this?
		if (this.config == DEFAULT) {
			this.config = order.config;
		} else if (this.config != order.config) {
			this.config = DEFAULT;
		}
	}
	
	public Unit(Player player) {
		this.player = player;
		this.config = DEFAULT;
	}
	
	public Configuration config() { return this.config; }
	
	@Override
	public String draw() { return config.draw(); }
}
