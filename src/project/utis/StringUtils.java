package project.utis;

public class StringUtils {

	private StringUtils(){}
	
	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$#" + n + "s", s);
	}

	public static String generateString(char c, int count) {
		StringBuilder builder = new StringBuilder(count);
		for (int i = 0; i < count; i++)
			builder.append(c);
		return builder.toString();
	}

	public static double[] convertStrings(String[] values) {
		double[] res = new double[values.length];
		for (int i = 0; i < res.length; i++) {
			try {
				res[i] = Double.parseDouble(values[i]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return res;
	}
}
