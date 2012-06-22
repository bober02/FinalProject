package project.csv;

import java.io.IOException;

/**
 * Wrapper interface for IO CSV classes from an external package.
 *
 */
public interface CSVFileReader {

	boolean hasNext();

	String[] readNext() throws IOException;

	String getFileName();
}
