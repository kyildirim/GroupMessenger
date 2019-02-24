package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import util.LogStream;
import util.Logger;

@SuppressWarnings("deprecation")
public class Server implements Runnable {

	private ServerSocket socket;
	private Thread thread;

	/**
	 * List of connected clients.
	 */
	private ArrayList<ConnectedClient> connectedClients = new ArrayList<>();

	public Server() {
		this(DEFAULT_PORT);
	}

	public Server(int port) {
		try {
			init(port);
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Failed to bind to port " + port);
			Logger.systemLog(LogStream.ERR, "Error: " + e.getMessage());
		}
	}

	/**
	 * Initializes the chat server and binds to the specified port to listen for
	 * connections.
	 * 
	 * @param port
	 *            Port to be bind to.
	 * @throws IOException
	 *             If the specified port is in use or cannot be binded to.
	 */
	private void init(int port) throws IOException {
		Logger.systemLog(LogStream.OUT, "Initializing chat server...");
		Logger.systemLog(LogStream.OUT, "Target port: " + port);
		this.socket = new ServerSocket(port);
		Logger.systemLog(LogStream.OUT, "Server started: " + socket);
		start();
	}

	/**
	 * Start a new thread for the server.
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * Stop the current thread running the server.
	 */
	public void stop() {
		if (thread != null) {
			/**
			 * This forcefully stops the thread which is not recommended. However signaling
			 * an interrupt may not always successfully terminate a thread in some cases.
			 * Despite the deprecation this method should work.
			 */
			thread.stop();
			thread = null;
		}
	}

	@Override
	public void run() {
		while (thread != null) {
			try {
				Logger.systemLog(LogStream.OUT, "Listening for client connection.");
				addThread(socket.accept());
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Server accept error: " + e.getMessage());
				stop();
			}
		}
	}

	private void addThread(Socket socket) {
		System.out.println("Client accepted: " + socket);
		ConnectedClient cc = new ConnectedClient(this, socket);
		String username = "";
		try {
			cc.open();
			boolean flag = true;
			while (flag) {
				username = cc.getInputStream().readUTF();
				if (!(username == null || username.equals("")))
					flag = false;
				for (ConnectedClient c : connectedClients)
					if (c.getUsername().equals(username))
						flag = true;
				if (flag)
					cc.send("Invalid username.");
			}
			cc.setUsername(username);
			connectedClients.add(cc);
			Logger.systemLog(LogStream.OUT, username + " connected.");
			cc.start();
		} catch (IOException ioe) {
			System.out.println("Error opening thread: " + ioe);
		}
	}

	private ConnectedClient findClient(int ID) throws InvalidConnectedClientIDException {
		for (ConnectedClient c : connectedClients)
			if (c.getID() == ID)
				return c;
		throw new InvalidConnectedClientIDException(ID);
	}

	public synchronized void handle(int ID, String input) {
		ConnectedClient cc = null;
		try {
			cc = findClient(ID);
			if (input.equals("/quit")) {
				for (ConnectedClient c : connectedClients)
					c.send(cc.getUsername() + " leaves the chat.");
				remove(ID);
			} else
				for (ConnectedClient c : connectedClients)
					if(!c.equals(cc))c.send(cc.getUsername() + ": " + input);
		} catch (InvalidConnectedClientIDException e) {
			Logger.systemLog(LogStream.ERR, "Requested client not found.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
		}
	}

	public synchronized void remove(int ID) {
		ConnectedClient cc = null;
		try {
			cc = findClient(ID);
			Logger.systemLog(LogStream.OUT, "Removing client " + ID);
			cc.close();
		} catch (InvalidConnectedClientIDException e) {
			Logger.systemLog(LogStream.ERR, "Requested client not found.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Failed to close streams for client " + ID);
			Logger.systemLog(LogStream.ERR, e.getMessage());
		}
		if (cc != null) {
			connectedClients.remove(cc);
			cc.stop();
		}

	}

	/**
	 * Default port for the constructor.
	 */
	private static final int DEFAULT_PORT = 4444;

	public static void main(String args[]) {
		new Server();
	}

}
