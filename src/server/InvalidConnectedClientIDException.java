package server;

public class InvalidConnectedClientIDException extends Exception {

	/**
	 * Auto-generated serial version ID.
	 */
	private static final long serialVersionUID = -7024592103192898730L;

	public InvalidConnectedClientIDException(int ID) {
		super("Invalid connected client id: " + ID);
	}
	
}
