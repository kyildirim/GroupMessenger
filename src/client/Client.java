package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private int counter = (int) (Math.random()*100);

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
				String msg = inputStream.readLine();
				if(msg!=null) counter++;
				outputStream.writeUTF("["+counter+"]"+msg);
				outputStream.flush();
			} catch (IOException e) {
				Logger.systemLog(LogStream.ERR, "Error sending message.");
				Logger.systemLog(LogStream.ERR, e.getMessage());
				stop();
			}
		}
	}

	public void handle(String msg) {
		Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
		Matcher m = p.matcher(msg);
		m.find();
		System.out.println(msg);
		int c = Integer.parseInt(m.group(1).replace("[", "").replace("]", ""));
		if(msg!=null) counter = (counter>c)?counter:c;
		if (msg.equals("/quit")) {
			Logger.systemLog(LogStream.OUT, "Quitting...");
			stop();
		} else
			System.out.println(msg);
		System.out.println(counter);
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
