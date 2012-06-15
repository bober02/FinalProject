package project.database.exceptions;

@SuppressWarnings("serial")
public class QueryException extends Exception {
	
	public QueryException(String message){
		super(message);
	}
}
