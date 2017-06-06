package pig.dream.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import freelifer.zeus.mjollnir.annotations.BindTarget;

/**
 * @author zhukun on 2017/4/15.
 */

public class BindTargetField {
    private VariableElement mFieldElement;

    public BindTargetField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                    String.format("Only fields can be annotated with @%s", BindTarget.class.getSimpleName()));
        }

        mFieldElement = (VariableElement) element;
        BindTarget bindTarget = mFieldElement.getAnnotation(BindTarget.class);
    }

    public String getFieldName() {
        return mFieldElement.getSimpleName().toString();
    }

    public TypeMirror getFieldType() {
        return mFieldElement.asType();
    }
}
