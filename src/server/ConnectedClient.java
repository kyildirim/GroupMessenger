package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import util.LogStream;
import util.Logger;

@SuppressWarnings("deprecation")
public class ConnectedClient extends Thread {
	private Server server;
	private Socket socket;
	private int ID = -1;
	private String username;

	public DataOutputStream getOutputStream() {
		return outputStream;
	}

	public String getUsername() {
		return username;
	}

	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	public ConnectedClient(Server server, Socket socket) {
		super();
		this.server = server;
		this.socket = socket;
		ID = socket.getPort();
	}

	public int getID() {
		return ID;
	}

	public void close() throws IOException {
		if (socket != null)
			socket.close();
		if (inputStream != null)
			inputStream.close();
		if (outputStream != null)
			outputStream.close();
	}

	public void open() throws IOException {
		this.inputStream = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
		this.outputStream = new DataOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public DataInputStream getInputStream() {
		return inputStream;
	}

	public void run() {
		Logger.systemLog(LogStream.OUT, "Thread running for " + ID);
		while (true) {
			try {
				server.handle(ID, inputStream.readUTF());
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Error reading " + ID);
				Logger.systemLog(LogStream.ERR, e.getMessage());
				Logger.systemLog(LogStream.ERR, ID + " will be removed.");
				server.remove(ID);
				stop();
			}
		}
	}

	public void send(String msg) {
		try {
			outputStream.writeUTF(msg);
			outputStream.flush();
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Failed to send message.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
			server.remove(ID);
			stop();
		}
	}
}
