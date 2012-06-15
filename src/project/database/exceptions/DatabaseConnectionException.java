package project.database.exceptions;

@SuppressWarnings("serial")
public class DatabaseConnectionException extends Exception {

	public DatabaseConnectionException(String message) {
		super(message);
	}
}
