package project.utis;

public class Timer {

	private long start;
	private long end;

	public Timer() {
		start = Long.MIN_VALUE;
		end = System.currentTimeMillis();
	}

	public void start() {
		start = System.currentTimeMillis();
		end = Long.MAX_VALUE;
	}

	public void stop() {
		end = System.currentTimeMillis();
	}

	public long getTimeElapsed() {
		return end - start;
	}

}
