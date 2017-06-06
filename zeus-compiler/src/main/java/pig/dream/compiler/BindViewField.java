package pig.dream.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import freelifer.zeus.mjollnir.annotations.BindView;

/**
 * @author zhukun on 2017/4/15.
 */

public class BindViewField {
    private VariableElement mFieldElement;
    private int mResId;

    public BindViewField(Element element) throws IllegalArgumentException {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(
                    String.format("Only fields can be annotated with @%s", BindView.class.getSimpleName()));
        }

        mFieldElement = (VariableElement) element;
        BindView bindView = mFieldElement.getAnnotation(BindView.class);
        mResId = bindView.value();
    }

    public String getFieldName() {
        return mFieldElement.getSimpleName().toString();
    }

    public TypeMirror getFieldType() {
        return mFieldElement.asType();
    }

    public int getResId() {
        return mResId;
    }
}
