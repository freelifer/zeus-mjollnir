package pig.dream.compiler;

/**
 * @author zhukun on 2017/6/5.
 */
public class Utils {
    public static String lowerCase(String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }

    public static String upperCase(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }
}
