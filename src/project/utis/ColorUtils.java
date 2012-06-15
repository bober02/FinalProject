package project.utis;

import java.awt.Color;
import java.awt.Paint;

public class ColorUtils {

	public static Paint[] getColorList(){
		return new Color[] { new Color(255, 85, 85), new Color(85, 85, 255), new Color(0, 157, 51), new Color(255, 255, 85),
				new Color(255, 85, 255), new Color(85, 255, 255), Color.pink, DARK_RED, DARK_BLUE, DARK_GREEN, DARK_YELLOW, DARK_MAGENTA,
				DARK_CYAN, Color.darkGray, LIGHT_RED, LIGHT_BLUE, LIGHT_GREEN, LIGHT_YELLOW, LIGHT_MAGENTA, LIGHT_CYAN, Color.lightGray, VERY_DARK_RED,
				VERY_DARK_BLUE, VERY_DARK_GREEN, VERY_DARK_YELLOW, VERY_DARK_MAGENTA, VERY_DARK_CYAN, VERY_LIGHT_RED, VERY_LIGHT_BLUE, VERY_LIGHT_GREEN,
				VERY_LIGHT_YELLOW, VERY_LIGHT_MAGENTA, VERY_LIGHT_CYAN };
	}
	
	private static final Color VERY_DARK_RED = new Color(128, 0, 0);
	private static final Color DARK_RED = new Color(192, 0, 0);
	private static final Color LIGHT_RED = new Color(255, 64, 64);
	private static final Color VERY_LIGHT_RED = new Color(255, 128, 128);
	private static final Color VERY_DARK_YELLOW = new Color(128, 128, 0);
	private static final Color DARK_YELLOW = new Color(192, 192, 0);
	private static final Color LIGHT_YELLOW = new Color(255, 255, 64);
	private static final Color VERY_LIGHT_YELLOW = new Color(255, 255, 128);
	private static final Color VERY_DARK_GREEN = new Color(0, 128, 0);
	private static final Color DARK_GREEN = new Color(0, 192, 0);
	private static final Color LIGHT_GREEN = new Color(64, 255, 64);
	private static final Color VERY_LIGHT_GREEN = new Color(128, 255, 128);
	private static final Color VERY_DARK_CYAN = new Color(0, 128, 128);
	private static final Color DARK_CYAN = new Color(0, 192, 192);
	private static final Color LIGHT_CYAN = new Color(64, 255, 255);
	private static final Color VERY_LIGHT_CYAN = new Color(128, 255, 255);
	private static final Color VERY_DARK_BLUE = new Color(0, 0, 128);
	private static final Color DARK_BLUE = new Color(0, 0, 192);
	private static final Color LIGHT_BLUE = new Color(64, 64, 255);
	private static final Color VERY_LIGHT_BLUE = new Color(128, 128, 255);
	private static final Color VERY_DARK_MAGENTA = new Color(128, 0, 128);
	private static final Color DARK_MAGENTA = new Color(192, 0, 192);
	private static final Color LIGHT_MAGENTA = new Color(255, 64, 255);
	private static final Color VERY_LIGHT_MAGENTA = new Color(255, 128, 255);


}
