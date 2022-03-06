package models;

public class Reconfigure implements Order {
	public final Configuration config;
	
	public Reconfigure(Configuration config) {
		this.config = config;
	}
	
	public String toString() {
		return "Recon-" + config.draw();
	}
}
