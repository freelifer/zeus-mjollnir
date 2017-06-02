package pig.dream.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import pig.dream.annotations.BindIntent;
import pig.dream.annotations.BindView;

/**
 * @author zhukun on 2017/4/15.
 */

public class BindIntentField {
    private VariableElement mFieldElement;
    private String value;

    public BindIntentField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                    String.format("Only fields can be annotated with @%s", BindView.class.getSimpleName()));
        }

        mFieldElement = (VariableElement) element;
        BindIntent bindIntent = mFieldElement.getAnnotation(BindIntent.class);
        value = bindIntent.value();
    }

    public VariableElement getFieldElement() {
        return mFieldElement;
    }

    public String getFieldName() {
        return mFieldElement.getSimpleName().toString();
    }

    public TypeMirror getFieldType() {
        return mFieldElement.asType();
    }

    public String getValue() {
        return value;
    }
}
