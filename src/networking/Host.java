package networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Host extends Server {
	
	private ServerSocket serverSocket;
	
	public Host(String playerName) {
		super(playerName);
		// TODO handle IP address?
	}
	
	@Override
	protected Socket openSocket() {
		try {
			serverSocket = new ServerSocket(PORT);
			return serverSocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected boolean isHost() {
		return true;
	}
	
	@Override
	protected void close() {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
