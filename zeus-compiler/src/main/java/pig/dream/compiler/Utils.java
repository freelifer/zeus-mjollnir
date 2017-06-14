package pig.dream.compiler;

/**
 * @author zhukun on 2017/6/5.
 */
final class Utils {
    static String lowerCase(String name) {
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
        return name;
    }

    static String upperCase(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    static boolean isEmpty(CharSequence str) {
        return (str == null || str.length() == 0);
    }

    static String getSimpleName(String fullName) {
        if (isEmpty(fullName)) {
            return "";
        }
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }
}
