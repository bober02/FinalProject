package project.io;

public interface Logger{

	/**
	 * Writes give string to the output;
	 * @param str - string to be written
	 */
	void write(String str);
	
	/**
	 * Writes give string to the output and moves to the next line;
	 * @param str - string to be written
	 */
	void writeln(String str);
	
}
