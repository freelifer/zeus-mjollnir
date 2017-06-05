package pig.dream.compiler;

import com.sun.tools.javac.code.Type;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * @author zhukun on 2017/6/5.
 */
public class ProvideField {
    private Element element;

    private ProvideField() {
    }

    public static ProvideField create(Element element) {
        ProvideField provideField = new ProvideField();
        provideField.element = element;
        return provideField;
    }

    public TypeElement getFatherElement() {
        return (TypeElement) element.getEnclosingElement();
    }

    public String getFatherQualifiedName() {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        return classElement.getQualifiedName().toString();
    }

    public String getFatherSimpleName() {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        return classElement.getSimpleName().toString();
    }

    public String getSimpleName() {
        return element.getSimpleName().toString();
    }

    public String getReturnType() {
        Type.MethodType methodType = (Type.MethodType) element.asType();
        return methodType.getReturnType().toString();
    }

    public void println(Messager messager) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("Element 父级类名%s, 全路径%s, 方法名%s, 未知%s",
                getFatherSimpleName(), getFatherQualifiedName(), getSimpleName(), getReturnType()));
    }
}
