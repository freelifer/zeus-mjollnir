package pig.dream.compiler;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * @author zhukun on 2017/6/2.
 */
public class Logger {

    private static final boolean DBG = true;

    public static void i(Messager messager, String format, Object... args) {
        if (!DBG) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

    public static void e(Messager messager, String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }
}
