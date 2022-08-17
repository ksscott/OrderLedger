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
	public static final String NO_COMMANDS = "Using this to indicate that the server handshake yielded no string";
	
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
	
	/** Game gives input for the server to send */
	public synchronized void addUserInput(String input) {
		userInputs.add(input);
		// TODO handle edge cases, e.g. if inputs back up
	}
	
	/** Server gives game the data it received */
	private synchronized void writeDataFromServer(String data) {
		receivedData.add(data);
		// TODO handle edge cases, e.g. if data backs up
	}
	
	/**
	 * Game receives data from server
	 *  
	 * @return oldest message from server, or else null
	 */
	public String pollDataFromServer() {
		return readNextItem(receivedData);
		// TODO handle edge cases, e.g. if data backs up
	}
	
	/** Server receives input from game */
	private String readUserInput() {
		return readNextItem(userInputs);
		// TODO handle edge cases, e.g. if inputs back up
	}
	
	/* Sit and wait forever until an item exists in the given queue */
	private <T> T readNextItem(Queue<T> q) {
		T nextItem = null;
		while (nextItem == null) {
			synchronized(this) {
				nextItem = q.poll();
			}
			try {
				Thread.sleep(POLL_INTERVAL_MILLIS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return nextItem;
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
					
					// I'm guessing this will lock this thread until the other server responds
					String received = sendAndReceive(toSend, isHost);
					if (received == null) { received = NO_COMMANDS; }
					writeDataFromServer(received);
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
		
		// I'm guessing this will lock its thread until the other server responds
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
//		String ip = "192.168.1.1"; // 127.0.0.1 in my current debugging
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
