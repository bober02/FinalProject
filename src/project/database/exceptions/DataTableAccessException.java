package project.database.exceptions;

@SuppressWarnings("serial")
public class DataTableAccessException extends RuntimeException {
	public DataTableAccessException(String message){
		super(message);
	}
}
