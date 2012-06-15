package project.io;

import java.io.Closeable;

public interface Logger extends Closeable{

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
