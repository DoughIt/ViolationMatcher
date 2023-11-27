package cn.edu.fudan.violationtracker.util;

/**
 * @author Beethoven
 */
public class StringsUtil {
    private StringsUtil() {
        // utility class
    }
    public static String firstLine(String str) {
        if (str == null || str.trim().isEmpty()) {
            return "";
        }
        return str.split("\n")[0];
    }
}
