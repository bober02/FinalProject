package project.io;

public class ConsoleLogger implements Logger {

	@Override
	public void write(String str) {
		System.out.print(str);
	}

	@Override
	public void writeln(String str) {
		System.out.println(str);
	}

}
