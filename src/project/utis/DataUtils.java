package project.utis;

public class DataUtils {

	private DataUtils() {
	}

	public static double[] generateConsecutive(int start, int finish) {
		double[] res = new double[finish - start];
		for (int i = 0; start < finish; start++, i++) {
			res[i] = start;
		}
		return res;
	}

	public static double[] generateValue(double val, int nTimes) {
		double[] res = new double[nTimes];
		for (int i = 0; i < nTimes; i++)
			res[i] = val;
		return res;
	}

	public static double[][] transpose(double[][] data) {
		double[][] res = new double[2][data.length];
		for (int i = 0; i < data.length; i++) {
			res[0][i] = data[i][0];
			res[1][i] = data[i][1];
		}
		return res;
	}

}
