package project.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger implements Logger {

	FileWriter writer;

	public FileLogger(File f) throws IOException {
		writer = new FileWriter(f);
	}

	@Override
	public void finalize() throws IOException {
		writer.close();
	}

	@Override
	public void write(String str) {
		try {
			writer.write(str);
		} catch (IOException e) {
		}
	}

	@Override
	public void writeln(String str) {
		try {
			writer.write(str + "\n");
		} catch (IOException e) {
		}
	}

}
