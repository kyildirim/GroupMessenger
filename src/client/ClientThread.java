package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import util.LogStream;
import util.Logger;

public class ClientThread extends Thread {

	private Socket socket;
	private Client client;
	private DataInputStream inputStream;

	public ClientThread(Client client, Socket socket) {
		this.client = client;
		this.socket = socket;
		open();
		start();
	}

	private void open() {
		try {
			inputStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Cannot get input stream from socket.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
			client.stop();
		}
	}

	public void close() {
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Cannot close the input stream.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
		}
	}

	public void run() {
		while (true) {
			try {
				client.handle(inputStream.readUTF());
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Failed to listen stream.");
				Logger.systemLog(LogStream.ERR, e.getMessage());
				client.stop();
			}
		}
	}

}
