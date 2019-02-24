package util;

public class InvalidStreamException extends Exception {

	/**
	 * Auto-generated serial version ID.
	 */
	private static final long serialVersionUID = 3283437502082747183L;

	public InvalidStreamException(LogStream ls) {
		super("Invalid log stream option: " + ls.toString());
	}
	
}
