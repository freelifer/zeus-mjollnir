package pig.dream.compiler;

import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * @author zhukun on 2017/6/5.
 */
public class BaseElement {

    private Elements elements;
    private Messager messager;

    private BaseElement(Elements elements, Messager messager) {
        this.elements = elements;
        this.messager = messager;
    }

    public void setElementsAndMessager(Elements elements, Messager messager) {
        this.elements = elements;
        this.messager = messager;
    }

    public void error(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }
}
