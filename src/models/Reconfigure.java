package models;

public class Reconfigure implements Order {
	public final Configuration config;
	
	public Reconfigure(Configuration config) {
		this.config = config;
	}
	
	@Override
	public String draw() { return config.draw(); }
}
