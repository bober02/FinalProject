package project.io;

import java.io.IOException;

public class ConsoleLogger implements Logger {

	@Override
	public void write(String str) {
		System.out.print(str);
	}

	@Override
	public void writeln(String str) {
		System.out.println(str);
	}

	@Override
	public void close() throws IOException {
		//Nothing in this case
	}

}
