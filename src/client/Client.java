package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import util.LogStream;
import util.Logger;

@SuppressWarnings("deprecation")
public class Client implements Runnable {

	private Socket socket;
	private Thread thread;
	private BufferedReader inputStream;
	private DataOutputStream outputStream;
	private ClientThread client;
	private boolean accepted = false;

	public Client(String serverName, int serverPort) {
		Logger.systemLog(LogStream.OUT, "Conencting to " + serverName);
		try {
			socket = new Socket(serverName, serverPort);
			Logger.systemLog(LogStream.OUT, "Connection successful to " + socket);
			start();
		} catch (UnknownHostException e) {
			Logger.systemLog(LogStream.ERR, "Unknown host " + serverName);
			Logger.systemLog(LogStream.ERR, e.getMessage());
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Connection failed to " + serverName);
			Logger.systemLog(LogStream.ERR, e.getMessage());
		}
	}

	@Override
	public void run() {
		while (thread != null) {
			try {
				outputStream.writeUTF(inputStream.readLine());
				outputStream.flush();
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Error sending message.");
				Logger.systemLog(LogStream.ERR, e.getMessage());
				stop();
			}
		}
	}

	public void handle(String msg) {
		if (msg.equals("/quit")) {
			Logger.systemLog(LogStream.OUT, "Quitting...");
			stop();
		} else
			System.out.println(msg);
	}

	public void start() throws IOException {
		inputStream = new BufferedReader(new InputStreamReader(System.in));
		outputStream = new DataOutputStream(socket.getOutputStream());
		while (!accepted) {
			try {
				accepted = true;
				System.out.print("Username: ");
				outputStream.writeUTF(inputStream.readLine());
				if(inputStream.readLine().equals("Invalid username.")) accepted = false;
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Error sending username.");
				Logger.systemLog(LogStream.ERR, e.getMessage());
				stop();
			}
		}
		if (thread == null) {
			client = new ClientThread(this, socket);
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			thread.stop();
			thread = null;
		}
		try {
			if (inputStream != null)
				inputStream.close();
			if (outputStream != null)
				outputStream.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			Logger.systemLog(LogStream.ERR, "Error closing streams.");
			Logger.systemLog(LogStream.ERR, e.getMessage());
		}
		client.close();
		client.stop();
	}

	public static void main(String args[]) {
		new Client("0.0.0.0", 4444);
	}

}
