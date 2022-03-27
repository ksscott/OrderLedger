package networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Server {
	
	protected String playerName;
	protected String otherPlayerName;
	
	private Socket socket;
	private DataOutputStream send;
	private DataInputStream receive;
	private NetworkingThread thread;
	
	private Queue<String> userInputs = new LinkedList<>();
	private Queue<String> receivedData = new LinkedList<>();
	
	public static final int PORT = 6666;
	public static final int POLL_INTERVAL_MILLIS = 500;
	public static final String QUIT_STRING = "The Quit String - I don't know what you expected";
	
	public Server(String playerName) {
		this.playerName = playerName;
	}
	
	/** @return true for successful start, false for failure */
	public final boolean start() {
		socket = this.openSocket();
		if (socket != null) {
			thread = new NetworkingThread();
			thread.start();
		}
		return socket != null;
	}
	
	public final void stop() {
		thread.requestStop();
	}
	
	public synchronized void addUserInput(String input) {
		userInputs.add(input);
		// TODO handle edge cases, e.g. if inputs back up
	}
	
	/** @return oldest message from server, or else null */
	public synchronized String pollDataFromServer() {
		return receivedData.poll();
		// TODO handle edge cases, e.g. if data backs up
	}
	
	private synchronized String readUserInput() {
		String nextInput = userInputs.poll();
		while (nextInput == null) {
			try {
				Thread.sleep(POLL_INTERVAL_MILLIS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			nextInput = userInputs.poll();
		}
		return nextInput;
		// TODO handle edge cases, e.g. if inputs back up
	}
	
	private synchronized void writeDataFromServer(String data) {
		receivedData.add(data);
		// TODO handle edge cases, e.g. if data backs up
	}
	
	public class NetworkingThread extends Thread {
		
		private boolean stopRequested;
		
		@Override
		public void run() {
			final boolean isHost = isHost();
			try {
				send = new DataOutputStream(socket.getOutputStream());
				receive = new DataInputStream(socket.getInputStream());
				
				otherPlayerName = sendAndReceive(playerName, isHost);
				writeDataFromServer(otherPlayerName);
				
				while(!stopRequested) {
					String toSend = readUserInput();
					if (QUIT_STRING.equals(toSend)) { // clunky, but whatever
						requestStop();
						break;
					}
					
					String received = sendAndReceive(toSend, isHost);
					if (received != null) {
						writeDataFromServer(received);
					}
					if (QUIT_STRING.equals(received)) {
						requestStop();
						break;
					}
				}
				send.writeUTF(QUIT_STRING);
				send.flush();
				send.close();
				socket.close();
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private final String sendAndReceive(String toSend, boolean isHost) throws IOException {
			String received = null;
			if (isHost) { // in case order matters
				received = receive.readUTF();
				send.writeUTF(toSend);
			} else {
				send.writeUTF(toSend);
				received = receive.readUTF();
			}
			return received;
		}
		
		public void requestStop() {
			stopRequested = true;
		}
	}
	
	protected abstract Socket openSocket();
	
	protected abstract boolean isHost();
	
	/** Called at the end of the process; children should do any necessary cleanup. */
	protected abstract void close();
	
	// Notes from YouTube:
//	void receive() {
//		try {
//			ServerSocket ss = new ServerSocket(PORT);
//			Socket s = ss.accept();
//			DataInputStream dis = new DataInputStream(s.getInputStream());
//			String input = dis.readUTF();
//			System.out.println("Received:");
//			System.out.println(input);
//			
//			ss.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	void send() {
//		String ip = "192.168.1.1";
//		try {
//			Socket s = new Socket(ip, PORT);
//			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
//			dos.writeUTF("MESSAGE");
//			dos.flush();
//			dos.close();
//			s.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
