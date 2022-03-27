package networking;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Server {

	private final String hostIpAddress;
	
	public Client(String playerName, String hostIpAddress) {
		super(playerName);
		this.hostIpAddress = hostIpAddress;
	}
	
	@Override
	protected Socket openSocket() {
		try {
			return new Socket(hostIpAddress, PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected boolean isHost() {
		return false;
	}
	
	@Override
	protected void close() {}
}
