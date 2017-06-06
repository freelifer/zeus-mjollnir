package pig.dream.compiler;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import freelifer.zeus.mjollnir.annotations.RouterActivity;

/**
 * @author zhukun on 2017/5/16.
 */

public class RouteActivityField {
    private TypeElement mTypeElement;
    private String value;

    public RouteActivityField(Element element) throws IllegalArgumentException {
        mTypeElement = (TypeElement) element;
        RouterActivity routeActivity = mTypeElement.getAnnotation(RouterActivity.class);
        value = routeActivity.value();
    }

    public String getValue() {
        return value;
    }

    public String getFullClassName() {
        return mTypeElement.getQualifiedName().toString();
    }
}
