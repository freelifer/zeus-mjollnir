package pig.dream.compiler;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author zhukun on 2017/6/5.
 */
public class ModuleField {

    private Elements elements;
    private TypeElement typeElement;
    private List<ProvideField> provides;

    public ModuleField(TypeElement typeElement, Elements elementUtils) {
        this.elements = elementUtils;
        this.typeElement = typeElement;
        provides = new ArrayList<>();
    }

    public void addProvide(ProvideField element) {
        provides.add(element);
    }

    public List<ProvideField> getProvides() {
        return provides;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }
}
