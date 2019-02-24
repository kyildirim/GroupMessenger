package util;

import java.io.PrintStream;

public class Logger {

	public static void systemLog(LogStream ls, String str) {
		try {
			convertLogStream(ls).println(str);
		} catch (InvalidStreamException e) {
			System.err.println(e.getMessage());
		}
	}

	public static PrintStream convertLogStream(LogStream ls) throws InvalidStreamException {
		switch (ls) {
		case ERR:
			return System.err;
		case OUT:
			return System.out;
		default:
			throw new InvalidStreamException(ls);
		}
	}

}
