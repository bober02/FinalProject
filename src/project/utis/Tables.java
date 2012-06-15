package project.utis;

import java.util.HashMap;
import java.util.Map;

public class Tables {

	private Tables() {
	}

	private static Map<String, String> names = new HashMap<String, String>();

	public static final String DATABASE = "database.db";
	public static final String SP = "SP01";
	public static final String SP2 = "SP05";
	public static final String DowJones = "DowJones02";
	public static final String Corn = "Corn01";
	public static final String Gold = "Gold02";
	public static final String SPMid = "SPMid02";

	public static String[] getTables() {
		return new String[] { SP, SP2, DowJones, Corn, Gold, SPMid };
	}

	public static String getTableName(String table) {
		if (names.isEmpty()) {
			names.put(SP, "S&P front Contract");
			names.put(SP2, "S&P500 5-th contract");
			names.put(DowJones, "Dow Jones 2-nd contract");
			names.put(Corn, "Corn front contract");
			names.put(Gold, "Gold 2-nd contract");
			names.put(SPMid, "S&P MidCap 2-nd contract");
		}
		return names.get(table);
	}
}
